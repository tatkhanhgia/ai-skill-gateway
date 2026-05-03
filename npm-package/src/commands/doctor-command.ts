import { stat } from 'node:fs/promises';
import path from 'node:path';
import type { Command } from 'commander';
import { sha256File } from '../manifest/checksum.js';
import { detectProjectRoot } from '../project/detect-root.js';
import { readInstallState } from '../installer/install-state.js';

async function exists(filePath: string): Promise<boolean> {
  try {
    await stat(filePath);
    return true;
  } catch {
    return false;
  }
}

export function addDoctorCommand(program: Command): void {
  program.command('doctor')
    .description('Check installed gtk-skill files')
    .option('--cwd <path>', 'target project directory')
    .option('--json', 'print machine-readable JSON')
    .action(async (options: { cwd?: string; json?: boolean }) => {
      const project = await detectProjectRoot(options.cwd);
      const state = await readInstallState(project.root);
      const result = { targetRoot: project.root, installed: Boolean(state), missing: 0, changed: 0, unchanged: 0 };

      if (state) {
        for (const file of state.files) {
          const target = path.join(project.root, file.target);
          if (!(await exists(target))) {
            result.missing += 1;
          } else if ((await sha256File(target)) === file.sha256) {
            result.unchanged += 1;
          } else {
            result.changed += 1;
          }
        }
      }

      if (options.json) {
        console.log(JSON.stringify(result, null, 2));
        return;
      }

      console.log(`Target: ${result.targetRoot}`);
      console.log(`Installed: ${result.installed ? 'yes' : 'no'}`);
      console.log(`Missing: ${result.missing}`);
      console.log(`Changed: ${result.changed}`);
      console.log(`Unchanged: ${result.unchanged}`);
      process.exitCode = result.missing > 0 || result.changed > 0 ? 1 : 0;
    });
}
