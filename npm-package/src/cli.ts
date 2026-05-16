import { readFileSync } from 'node:fs';
import path from 'node:path';
import { Command } from 'commander';
import { packageRoot } from './manifest/paths.js';
import { addDoctorCommand } from './commands/doctor-command.js';
import { addInstallCommand } from './commands/install-command.js';
import { addListCommand } from './commands/list-command.js';
import { addUpdateCommand } from './commands/update-command.js';

const packageJson = JSON.parse(readFileSync(path.join(packageRoot(), 'package.json'), 'utf8')) as { version: string };
const program = new Command();

program
  .name('gtk-skill')
  .description('Install curated Claude, Codex, and OpenCode skills into projects')
  .version(packageJson.version)
  .addHelpText('after', `
Examples:
  gtk-skill install --dry-run
  gtk-skill install --backup --overwrite
  gtk-skill list --json
  gtk-skill doctor
`);

addInstallCommand(program);
addListCommand(program);
addDoctorCommand(program);
addUpdateCommand(program);

program.command('version')
  .description('Print package version')
  .action(() => console.log(packageJson.version));

await program.parseAsync(process.argv);
