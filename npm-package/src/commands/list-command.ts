import type { Command } from 'commander';
import { readAssetManifest } from '../manifest/read-manifest.js';

export function addListCommand(program: Command): void {
  program.command('list')
    .description('List packaged assets')
    .option('--json', 'print machine-readable JSON')
    .action(async (options: { json?: boolean }) => {
      const manifest = await readAssetManifest();
      const counts = manifest.files.reduce<Record<string, number>>((accumulator, file) => {
        accumulator[file.type] = (accumulator[file.type] ?? 0) + 1;
        return accumulator;
      }, {});

      if (options.json) {
        console.log(JSON.stringify({ packageVersion: manifest.packageVersion, counts, total: manifest.files.length }, null, 2));
        return;
      }

      console.log(`Package: ${manifest.packageName}@${manifest.packageVersion}`);
      console.log(`Total: ${manifest.files.length}`);
      for (const [type, count] of Object.entries(counts).sort()) console.log(`${type}: ${count}`);
    });
}
