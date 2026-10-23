// Usage: node tools/compat/build-audit.mjs /path/to/research-downloads [repository-root]
// Inputs: selected.json, implemented.json, CurseForge metadata, release JARs, Vineflower output.
// This builds a STATIC review inventory, not a claim of runtime/visual compatibility.
import fs from 'node:fs';
import path from 'node:path';
import crypto from 'node:crypto';
import {execFileSync} from 'node:child_process';

const research = process.argv[2];
const root = process.argv[3] || process.cwd();
const selected = JSON.parse(fs.readFileSync(path.join(research, 'selected.json')));
const implemented = JSON.parse(fs.readFileSync(path.join(research, 'implemented.json')))
    .filter(row => !(row.artifact === 'to-tweaks-irons-spells' && row.mc === '1.21.1'));
const walk = folder => fs.existsSync(folder) ? fs.readdirSync(folder, {withFileTypes: true})
    .flatMap(item => item.isDirectory() ? walk(path.join(folder, item.name)) : [path.join(folder, item.name)]) : [];
const inventory = [];
const releases = [];

for (const release of selected) {
    const artifact = `${release.slug}-${release.version}`;
    const jar = path.join(research, `${artifact}.jar`);
    if (!fs.existsSync(jar)) continue;
    const metadata = JSON.parse(fs.readFileSync(path.join(research, `${release.slug}.json`)));
    const entries = execFileSync('unzip', ['-Z1', jar], {encoding: 'utf8'}).split('\n');
    const languages = {};
    for (const entry of entries.filter(e => /^assets\/[^/]+\/lang\/en_us.json$/.test(e))) {
        Object.assign(languages, JSON.parse(execFileSync('unzip', ['-p', jar, entry], {encoding: 'utf8'})));
    }
    const namedSpells = Object.keys(languages).filter(key => /^spell\.[^.]+\.[^.]+$/.test(key))
        .map(key => key.slice(6).replace('.', ':'));
    const base = path.join(research, 'decompiled', artifact);
    const sources = walk(base).filter(file => file.endsWith('.java'))
        .map(file => ({file, code: fs.readFileSync(file, 'utf8')}))
        .filter(({code}) => /\bclass\s+\w+\s+extends\s+\w*Spell\b/.test(code));
    const found = new Set();
    for (const {file, code} of sources) {
        const className = code.match(/\bclass\s+(\w+)\s+extends/)?.[1];
        const implementation = `${code.match(/package\s+([\w.]+);/)?.[1]}.${className}`;
        let id;
        // Only a spell's own resource field: do not mistake sounds, summoned entities, or damage IDs for spells.
        const field = code.match(/ResourceLocation\s+(?:spellId|spellResource|SPELL_ID|SPELL_RESOURCE)\s*=\s*([^;]+);/)?.[1];
        if (field) {
            const strings = [...field.matchAll(/"([a-z0-9_:./-]+)"/g)].map(match => match[1]);
            if (strings.length === 2) id = `${strings[0]}:${strings[1]}`;
            else if (strings.length === 1 && strings[0].includes(':')) id = strings[0];
            else if (strings.length === 1) id = namedSpells.find(value => value.endsWith(`:${strings[0]}`));
        }
        if (!id) {
            const shortName = className.replace(/Spell$/, '').replace(/([a-z0-9])([A-Z])/g, '$1_$2').toLowerCase();
            id = namedSpells.find(value => value.endsWith(`:${shortName}`));
        }
        if (id) found.add(id);
        const mc = release.version === '1.21' ? '1.21.1' : release.version;
        const adapter = implemented.find(row => row.mc === mc && row.implementation === implementation);
        const flags = [];
        if (/getRecastCount|RecastInstance|hasRecastForSpell/.test(code)) flags.push('recast');
        if (/SummonedEntitiesCastData|AbstractSummonSpell/.test(code)) flags.push('summon lifecycle');
        if (/checkPreCastConditions/.test(code)) flags.push('cast predicate');
        if (/getItemBySlot|EquipmentSlot|ItemStack|\.m_6844_\(/.test(code)) flags.push('equipment/inventory');
        if (/isInWater|isUnderWater|\.m_20069_\(|\.m_20072_\(/.test(code)) flags.push('water');
        if (/sendToPlayer|sendToPlayers|sendToTracking|PacketDistributor/.test(code)) flags.push('custom packet path');
        if (/changeDimension|teleportTo|ServerPlayer|Minecraft\.getInstance/.test(code)) flags.push('player/world-sensitive');
        let status = adapter ? `adapter: ${adapter.action}` : 'generic candidate; visual test pending';
        if (!adapter && flags.includes('summon lifecycle')) status = 'generic summon; do not auto-dismiss with a recast';
        if (!adapter && (flags.includes('equipment/inventory') || flags.includes('player/world-sensitive')))
            status = 'manual validation: player/inventory/world conditions; no adapter';
        if (!id) status = 'unresolved ID / helper class; not counted as compatible';
        if (release.slug === 'to-tweaks-irons-spells' && mc === '1.21.1') status = 'excluded: upstream deprecated release';
        if (mc === '1.20.1' && id === 'cataclysm_spellbooks:thoths_witness')
            status = 'adapter: registered Pharaoh helmet before cast validation; real-client summon observed';
        if (id === 'legendary_spellbooks:annihilation_geyser')
            status = 'adapter: SPELLBOOK source and equipped registry-resolved Annihilators Protocol Curios book';
        if (id === 'legendary_spellbooks:energy_beam')
            status = 'adapter: SPELLBOOK source required by canBeCastedBy';
        if (mc === '1.20.1' && ['apex_harvest', 'skypiercer', 'shear_of_the_stars', 'supernova', 'extinction', 'tectonic_rift', 'gyro_slash', 'galena_shatter'].some(name => id === `traveloptics:${name}`))
            status = 'adapter: registry-resolved exclusive weapon before original checkPreCastConditions';
        if (mc === '1.20.1' && id === 'traveloptics:call_forth_the_dead_king')
            status = 'adapter: SCROLL source required by canBeCastedBy';
        if (mc === '1.20.1' && id === 'traveloptics:jet_steam')
            status = 'adapter: submerged swimming scene, flying ability off, brief follow-up';
        if (mc === '1.21.1' && id === 'alshanex_familiars:switcheroo')
            status = 'adapter: registry-resolved mage pet target with reflected owner UUID prerequisite';
        if (mc === '1.20.1' && ['ias_spellbooks:handemic', 'ias_spellbooks:spirits_chosen', 'ias_spellbooks:venomous_reborn'].includes(id))
            status = 'restricted when removed Iron OwnerGetter API is absent; upstream gameplay incompatible, not an effect-test pass';
        inventory.push({artifact: release.slug, minecraft: mc, id: id || null, implementation,
            source: path.relative(base, file), flags, status});
    }
    for (const id of namedSpells.filter(id => !found.has(id))) {
        inventory.push({artifact: release.slug, minecraft: release.version === '1.21' ? '1.21.1' : release.version,
            id, implementation: null, source: null, flags: [],
            status: 'language entry only: stale/removed/unresolved; not counted as compatible'});
    }
    const modFile = entries.find(e => /^META-INF\/(?:neoforge\.)?mods.toml$/.test(e));
    const toml = modFile ? execFileSync('unzip', ['-p', jar, modFile], {encoding: 'utf8'}) : '';
    const modEntry = toml.match(/\[\[mods\]\]([\s\S]*?)(?=\n\s*\[|$)/)?.[1] || '';
    let runtimeVersion = modEntry.match(/version\s*=\s*"([^"]+)"/)?.[1];
    if (runtimeVersion === '${file.jarVersion}') {
        runtimeVersion = execFileSync('unzip', ['-p', jar, 'META-INF/MANIFEST.MF'], {encoding: 'utf8'})
            .match(/Implementation-Version: ([^\r\n]+)/)?.[1];
    }
    const dependencies = [...toml.matchAll(/\[\[dependencies\.[^\]]+\]\]([\s\S]*?)(?=\n\s*\[|$)/g)].map(match => ({
        mod: match[1].match(/modId\s*=\s*"([^"]+)"/)?.[1],
        version: match[1].match(/versionRange\s*=\s*"([^"]+)"/)?.[1] || '*',
        required: /mandatory\s*=\s*true|type\s*=\s*"required"/.test(match[1])
    }));
    releases.push({artifact: release.slug, title: metadata.title, minecraft: release.version,
        modId: modEntry.match(/modId\s*=\s*"([^"]+)"/)?.[1], runtimeVersion,
        projectDownloads: metadata.downloads?.total ?? metadata.downloads,
        file: release.id, filename: release.name, url: release.url,
        sha256: crypto.createHash('sha256').update(fs.readFileSync(jar)).digest('hex'), dependencies});
}

const lines = ['# Spell-by-spell static audit', '',
    'Release 1.1.0 research snapshot: 2026-10-23. This is a source/bytecode inventory, **not a gameplay certification**.', '',
    'Each row identifies the implementation in the exact release JAR listed in `releases.json`. Review `onCast`,',
    '`checkPreCastConditions`, recast lifecycle and related effect/event handlers at that class before changing a rule.',
    'Names from language files without a matching implementation remain explicitly unresolved (they may be stale).',
    'Generic candidates intentionally receive no compatibility code. Dependency declarations do not prove compatibility',
    'with every newer Iron\'s release; only our two base builds have been verified against 3.16.3.', '',
    'Flags are conservative source-search hints, not proof that a special condition blocks the preview.',
    'Summon recasts often dismiss the entity; never blindly replay every spell with `getRecastCount`.', ''];
for (const release of releases) {
    const mc = release.minecraft === '1.21' ? '1.21.1' : release.minecraft;
    const rows = inventory.filter(row => row.artifact === release.artifact && row.minecraft === mc);
    lines.push(`## ${release.title} — ${mc}`, '', `CurseForge file: ${release.file}; ${rows.length} inventory rows.`, '',
        '| Spell ID | Implementation (within the JAR) | Conditions / action |', '| --- | --- | --- |');
    for (const row of rows.sort((a,b) => (a.id||a.implementation).localeCompare(b.id||b.implementation))) {
        lines.push(`| ${row.id ? '`'+row.id+'`' : '(unresolved)'} | ${row.implementation ? '`'+row.implementation+'`' : 'language only'} | ${row.status}; ${row.flags.join(', ') || 'no extra source-search flags'} |`);
    }
    lines.push('');
}
const output = path.join(root, 'docs/compatibility');
fs.mkdirSync(output, {recursive: true});
for (const [name, text] of [['SPELL_AUDIT.md', lines.join('\n')],
    ['spell-audit.json', JSON.stringify(inventory, null, 2)+'\n'], ['releases.json', JSON.stringify(releases, null, 2)+'\n']]) {
    const target = path.join(output, name);
    const relative = path.relative(root, target);
    const patch = `*** Begin Patch\n${fs.existsSync(target) ? `*** Delete File: ${relative}\n` : ''}*** Add File: ${relative}\n${text.trimEnd().split('\n').map(line=>'+'+line).join('\n')}\n*** End Patch\n`;
    execFileSync('apply_patch', [], {cwd: root, input: patch});
}
console.log(`${inventory.length} inventory rows, ${releases.length} release artifacts`);
