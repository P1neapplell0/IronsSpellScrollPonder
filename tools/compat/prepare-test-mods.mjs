// Explicitly downloads development-only fixtures. Never modifies a normal run directory or release dependencies.
import fs from 'node:fs';
import os from 'node:os';
import path from 'node:path';
import crypto from 'node:crypto';
import {fileURLToPath} from 'node:url';
import {spawnSync} from 'node:child_process';

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '../..');
const args = process.argv.slice(2);
const argument = (name, fallback) => args.includes(name) ? args[args.indexOf(name) + 1] : fallback;
const mc = fs.readFileSync(path.join(root, 'gradle.properties'), 'utf8').match(/^minecraft_version=(.+)$/m)[1].trim();
const manifest = JSON.parse(fs.readFileSync(path.join(root, 'gradle/compat-test-lock.json')));
const target = manifest.targets[mc];
const profile = argument('--profile', 'smoke');
const mode = argument('--mode', 'on');
const scope = argument('--scope', 'all');
if (!['all', 'targeted'].includes(scope)) throw new Error('--scope must be all or targeted');
if (!['on', 'off'].includes(mode) || !/^[a-z0-9_,-]+$/.test(profile)) throw new Error('Invalid profile/mode');
const names = [...new Set(profile.split(','))].sort();
const keys = [...new Set(names.flatMap(name => {
    if (name === 'all') return Object.keys(target.profiles).filter(key => key !== 'smoke' && !(target.isolatedProfiles || []).includes(key))
        .flatMap(key => target.profiles[key]);
    if (!target.profiles[name]) throw new Error(target.unavailableProfiles?.[name]
        || `Unknown ${mc} profile ${name}; choose ${Object.keys(target.profiles)}`);
    return target.profiles[name];
}))];
const repository = path.join(process.env.GRADLE_USER_HOME || path.join(os.homedir(), '.gradle'), 'caches/iss-ponder-compat/maven');
const versionsByMod = new Map();
for (const key of keys) {
    const mod = target.artifacts[key];
    if (versionsByMod.has(mod.modId) && versionsByMod.get(mod.modId) !== mod.file) {
        throw new Error(`Conflicting versions of ${mod.modId}; run isolated profiles separately`);
    }
    versionsByMod.set(mod.modId, mod.file);
}
const seed = argument('--seed', '');
const digest = file => crypto.createHash('sha256').update(fs.readFileSync(file)).digest('hex');

for (const key of keys) {
    const mod = target.artifacts[key];
    if (!mod?.sha256) throw new Error(`Unpinned fixture ${key}`);
    const artifact = `${mod.slug}-${mod.project}`;
    const folder = path.join(repository, 'issponder/compat', artifact, String(mod.file));
    const jar = path.join(folder, `${artifact}-${mod.file}.jar`);
    fs.mkdirSync(folder, {recursive: true});
    if (!fs.existsSync(jar) || digest(jar) !== mod.sha256) {
        const candidates = seed ? [path.join(seed, `${mod.slug}-${mc}.jar`),
            path.join(seed, `${mod.slug}-${mc === '1.21.1' ? '1.21' : mc}.jar`), path.join(seed, mod.filename)] : [];
        const cached = candidates.find(file => fs.existsSync(file) && digest(file) === mod.sha256);
        if (cached) {
            fs.copyFileSync(cached, jar);
        } else {
            const id = String(mod.file);
            const url = `https://mediafilez.forgecdn.net/files/${id.slice(0, -3)}/${Number(id.slice(-3))}/${encodeURIComponent(mod.filename)}`;
            console.log(`Downloading ${mod.filename}`);
            const temporary = jar + '.part';
            // Retain partial downloads across invocations. curl verifies HTTP range support;
            // the pinned SHA-256 below remains the final authority, not Content-Length.
            const download = spawnSync('curl', ['--fail', '--location', '--retry', '3',
                '--connect-timeout', '20', '--max-time', '600', '--continue-at', '-',
                '--output', temporary, url], {stdio: 'inherit'});
            if (download.status !== 0) throw new Error(`Download failed: ${mod.filename}; rerun to resume`);
            if (digest(temporary) !== mod.sha256) throw new Error(`SHA-256 mismatch: ${mod.filename}`);
            fs.renameSync(temporary, jar);
        }
    }
    const pom = `<project><modelVersion>4.0.0</modelVersion><groupId>issponder.compat</groupId><artifactId>${artifact}</artifactId><version>${mod.file}</version></project>\n`;
    fs.writeFileSync(path.join(folder, `${artifact}-${mod.file}.pom`), pom);
    console.log(`Verified ${key}: ${mod.filename}`);
}

const run = path.join(root, 'run/compat', `${names.join('+')}-${mode}`);
fs.mkdirSync(path.join(run, 'config'), {recursive: true});
const config = path.join(run, 'config/iss_ponder-common.toml');
if (!fs.existsSync(config) || args.includes('--reset-config')) {
    fs.writeFileSync(config, `# Development fixture only; never copied to normal run/config.\n[experimental]\nenabled = ${mode === 'on'}\ncataclysmLegacyPatch = ${mc === '1.20.1' && keys.includes('cataclysm') && mode === 'on'}\n`);
}
const namespaces = new Set(keys.map(key => target.artifacts[key].modId));
const audit = JSON.parse(fs.readFileSync(path.join(root, 'docs/compatibility/spell-audit.json')));
const cases = [...new Set(audit.filter(row => row.minecraft === mc && row.id && namespaces.has(row.id.split(':')[0])
    && (scope === 'all' || row.status.startsWith('adapter:') || row.artifact.startsWith('winds-spellbooks'))).map(row => row.id))].sort();
fs.writeFileSync(path.join(run, 'compat-cases.json'), JSON.stringify(cases, null, 2) + '\n');
fs.writeFileSync(path.join(run, 'compat-fixtures.json'), JSON.stringify({minecraft: mc, profile: names, mode, scope,
    fixtures: keys.map(key => ({key, ...target.artifacts[key]}))}, null, 2) + '\n');
const options = path.join(run, 'options.txt');
if (!fs.existsSync(options)) fs.writeFileSync(options,
    'onboardAccessibility:false\nlang:en_us\nrenderDistance:4\nsimulationDistance:5\nmaxFps:30\nenableVsync:false\n');
console.log(`Prepared ${run} (${cases.length} spell IDs). No game has been launched.`);
console.log(`./gradlew runClient -PcompatTestProfile=${names.join(',')} -PcompatTestMode=${mode}`);
