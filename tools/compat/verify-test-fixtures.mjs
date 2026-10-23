// Static closure check only: validates downloaded JARs, hashes and declared required mod IDs.
// Does not prove ABI compatibility, evaluate version ranges or start Minecraft.
import fs from 'node:fs';
import path from 'node:path';
import os from 'node:os';
import crypto from 'node:crypto';
import {execFileSync} from 'node:child_process';

const directory = process.argv[2];
if (!directory) throw new Error('Usage: node tools/compat/verify-test-fixtures.mjs run/compat/<profile>-<mode>');
const manifest = JSON.parse(fs.readFileSync(path.join(directory, 'compat-fixtures.json')));
const repository = path.join(process.env.GRADLE_USER_HOME || path.join(os.homedir(), '.gradle'), 'caches/iss-ponder-compat/maven');
// Supplied by this project's normal development dependencies, not by addon fixtures.
const available = new Set(['minecraft', 'forge', 'neoforge', 'iss_ponder', 'irons_spellbooks',
    'irons_lib', 'curios', 'geckolib', 'playeranimator', 'player_animation_library', 'ponder', 'flywheel', 'appleskin']);
const required = [];
let warnings = 0;
for (const mod of manifest.fixtures) {
    const artifact = `${mod.slug}-${mod.project}`;
    const jar = path.join(repository, 'issponder/compat', artifact, String(mod.file), `${artifact}-${mod.file}.jar`);
    const hash = crypto.createHash('sha256').update(fs.readFileSync(jar)).digest('hex');
    if (hash !== mod.sha256) throw new Error(`Hash mismatch: ${mod.key}`);
    execFileSync('unzip', ['-tqq', jar]);
    const entries = execFileSync('unzip', ['-Z1', jar], {encoding: 'utf8'}).split('\n');
    const entry = entries.find(name => /^META-INF\/(neoforge\.)?mods.toml$/.test(name));
    if (!entry) throw new Error(`No mod metadata: ${mod.key}`);
    const text = execFileSync('unzip', ['-p', jar, entry], {encoding: 'utf8'});
    const declarations = text.split('\n').filter(line => !line.trimStart().startsWith('#')).join('\n');
    for (const block of declarations.split(/\[\[mods\]\]/).slice(1)) {
        const id = block.match(/modId\s*=\s*"([^"\n]+)"/)?.[1];
        if (id) available.add(id);
    }
    for (const block of declarations.matchAll(/\[\[dependencies\.[^\]]+\]\]([\s\S]*?)(?=\n\s*\[|$)/g)) {
        if (/mandatory\s*=\s*true|type\s*=\s*"required"/.test(block[1])) {
            required.push([mod.key, block[1].match(/modId\s*=\s*"([^"\n]+)"/)?.[1]]);
        }
    }
    // Strip comments: upstream template comments are harmless; active placeholders need review.
    // ${file.jarVersion} is a supported loader substitution, not a broken Gradle template.
    const active = text.replaceAll('${file.jarVersion}', '').split('\n')
        .filter(line => /^(modLoader|loaderVersion|modId|version|versionRange)\s*=/.test(line.trim())).join('\n');
    if (active.includes('${')) {
        warnings++;
        console.warn(`UPSTREAM METADATA WARNING: ${mod.key} contains unexpanded placeholders; loader acceptance unverified`);
    }
}
const missing = required.filter(([, id]) => !id || !available.has(id));
if (missing.length) throw new Error(`Missing required mod IDs: ${JSON.stringify(missing)}`);
console.log(`STATIC ONLY: ${manifest.fixtures.length} JAR hashes/ZIPs verified; declared required mod IDs present; ${warnings} metadata warnings. Version ranges and undeclared dependencies need loader testing.`);
