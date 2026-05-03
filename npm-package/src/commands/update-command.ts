import type { Command } from 'commander';
import { installAssets } from '../installer/install-assets.js';
import { countActions, renderSummary } from '../output/summarize-result.js';

export function addUpdateCommand(program: Command): void {
  program.command('update')
    .description('Update packaged skills without overwriting local changes by default')
    .option('--cwd <path>', 'target project directory')
    .option('--dry-run', 'show planned writes without changing files')
    .option('--overwrite', 'overwrite changed target files')
    .option('--backup', 'backup changed target files before overwrite')
    .option('--allow-non-project-dir', 'allow update in a directory without common project markers')
    .option('--json', 'print machine-readable JSON')
    .action(async (options: { cwd?: string; dryRun?: boolean; overwrite?: boolean; backup?: boolean; allowNonProjectDir?: boolean; json?: boolean }) => {
      const result = await installAssets({
        cwd: options.cwd,
        dryRun: options.dryRun,
        policy: options.overwrite ? (options.backup ? 'backup-and-overwrite' : 'overwrite') : 'skip',
        allowNonProjectDir: options.allowNonProjectDir
      });
      const counts = countActions(result);
      console.log(options.json ? JSON.stringify(result, null, 2) : renderSummary(result));
      process.exitCode = result.errors.length > 0 || (counts.conflict > 0 && !options.dryRun) ? 1 : 0;
    });
}
