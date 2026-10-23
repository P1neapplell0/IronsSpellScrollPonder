// A Gradle runClient exit code alone is insufficient: FML may show an error screen and exit zero.
import fs from 'node:fs';
import path from 'node:path';
const directory = process.argv[2];
if (!directory) throw new Error('Usage: node tools/compat/check-smoke-results.mjs run/compat/<profile>-<mode>');
const log = fs.readFileSync(path.join(directory, 'logs/latest.log'), 'utf8');
const complete = log.match(/\[ISS-COMPAT-TEST\] FINISHED (\d+) cases;/);
if (!complete || /\bFATAL\b|Stopped experimental adapter|\[ISS-COMPAT-TEST\] Runner failed/.test(log)) {
    throw new Error('Latest game log has no clean completed smoke suite (check FML, crash reports and adapter warnings)');
}
const report = JSON.parse(fs.readFileSync(path.join(directory, 'compat-test-results.json')));
if (!report.finished || !report.results.length || report.results.length !== Number(complete[1])) {
    throw new Error('Missing, stale, unfinished or empty smoke report');
}
const failures = report.results.filter(row => row.result !== 'RENDERED_COMPLETE');
const knownRestrictions = new Set(['ias_spellbooks:handemic', 'ias_spellbooks:spirits_chosen', 'ias_spellbooks:venomous_reborn']);
const acknowledgeRestrictions = process.argv.includes('--acknowledge-illage-restrictions');
const unexpected = failures.filter(row => !acknowledgeRestrictions
    || JSON.parse(report.fixtureIdentity).minecraft !== '1.20.1'
    || row.result !== 'SKIPPED_BY_SAFETY_POLICY' || !knownRestrictions.has(row.spell));
if (unexpected.length) throw new Error(`Failed cases: ${JSON.stringify(unexpected)}`);
if (failures.length) {
    console.log(`COMPLETED WITH UPSTREAM RESTRICTIONS: ${report.results.length - failures.length} rendered-complete; ${failures.length} Illage cases SAFELY BLOCKED, NOT effect-test passes.`);
} else {
    console.log(`SMOKE ONLY: ${report.results.length} rendered-complete cases; visual/mechanical and dedicated-server review still required.`);
}
