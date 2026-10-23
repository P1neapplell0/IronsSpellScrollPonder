// Static profile checks against the exact JAR metadata captured by build-audit.mjs.
// No addon dependencies, Minecraft launch, or new test framework are required.
import fs from 'node:fs';
import assert from 'node:assert/strict';
const root = process.argv[2] || process.cwd();
const read = relative => fs.readFileSync(`${root}/${relative}`, 'utf8');
const releases = JSON.parse(read('docs/compatibility/releases.json'));
const spells = JSON.parse(read('docs/compatibility/spell-audit.json'));
const dispatch = read('src/main/java/com/p1nero/iss_ponder/compat/ExperimentalCompat.java');
let checked = 0;
for (const match of dispatch.matchAll(/Map\.entry\("([^"]+)", "([^"]+)"\)/g)) {
    const [, modId, provider] = match;
    const text = read(`src/main/java/com/p1nero/iss_ponder/compat/${provider.replaceAll('.', '/')}.java`);
    const versions = [...text.match(/super\("([^"]+)", "([^"]+)"\)/)].slice(1);
    for (const [index, minecraft] of ['1.20.1', '1.21.1'].entries()) {
        if (versions[index] === 'unavailable') continue;
        assert(releases.some(row => row.modId === modId && row.minecraft === minecraft
            && row.runtimeVersion === versions[index]), `${provider}: unaudited runtime version ${versions[index]}`);
    }
    for (const rule of text.matchAll(/rule\("([^"]+)", "([^"]+)"/g)) {
        assert(spells.some(row => row.id === `${modId}:${rule[1]}` && row.implementation === rule[2]),
            `${provider}: unmatched spell ID/class ${rule[1]}`);
        checked++;
    }
    assert(!/^import (?:com\.gametechbc|net\.acetheeldritchking|dev\.higurashi|net\.hazen|net\.alshanex)\./m.test(text),
        `${provider}: hard addon API import`);
}
assert(read('src/main/java/com/p1nero/iss_ponder/config/CompatConfig.java').includes('.define("enabled", false)'),
    'experimental master must default to false');
assert.match(read('gradle.properties'), /^mod_version=1\.1\.0$/m);
console.log(`PASS: ${checked} provider rules match audited ID/class pairs; runtime versions match JAR metadata; default off; version 1.1.0`);
