import type { Command } from 'commander';
import { installAssets } from '../installer/install-assets.js';
import type { ConflictPolicy } from '../types.js';
import { countActions, renderSummary } from '../output/summarize-result.js';

interface InstallCommandOptions {
  cwd?: string;
  dryRun?: boolean;
  claude?: boolean;
  codex?: boolean;
  opencode?: boolean;
  skills?: boolean;
  agents?: boolean;
  hooks?: boolean;
  rules?: boolean;
  scripts?: boolean;
  overwrite?: boolean;
  backup?: boolean;
  json?: boolean;
  allowNonProjectDir?: boolean;
}

function groupsFromOptions(options: InstallCommandOptions): string[] {
  return ['claude', 'codex', 'opencode', 'skills', 'agents', 'hooks', 'rules', 'scripts'].filter((group) => Boolean(options[group as keyof InstallCommandOptions]));
}

function policyFromOptions(options: InstallCommandOptions): ConflictPolicy {
  if (options.overwrite && options.backup) return 'backup-and-overwrite';
  if (options.overwrite) return 'overwrite';
  return 'skip';
}

export function addInstallCommand(program: Command): void {
  program.command('install')
    .description('Install packaged skills into a project')
    .option('--cwd <path>', 'target project directory')
    .option('--dry-run', 'show planned writes without changing files')
    .option('--claude', 'install Claude assets')
    .option('--codex', 'install Codex assets')
    .option('--opencode', 'install OpenCode assets')
    .option('--skills', 'install skill assets')
    .option('--agents', 'install agent assets')
    .option('--hooks', 'install hook assets')
    .option('--rules', 'install rule assets')
    .option('--scripts', 'install script assets')
    .option('--overwrite', 'overwrite changed target files')
    .option('--backup', 'backup changed target files before overwrite')
    .option('--allow-non-project-dir', 'allow install into a directory without common project markers')
    .option('--json', 'print machine-readable JSON')
    .action(async (options: InstallCommandOptions) => {
      const result = await installAssets({
        cwd: options.cwd,
        dryRun: options.dryRun,
        groups: groupsFromOptions(options),
        policy: policyFromOptions(options),
        allowNonProjectDir: options.allowNonProjectDir
      });
      const counts = countActions(result);
      console.log(options.json ? JSON.stringify(result, null, 2) : renderSummary(result));
      process.exitCode = result.errors.length > 0 || (counts.conflict > 0 && !options.dryRun) ? 1 : 0;
    });
}
