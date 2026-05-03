import type { InstallResult, PlannedAction } from '../types.js';

export function countActions(result: InstallResult): Record<PlannedAction, number> {
  return result.operations.reduce<Record<PlannedAction, number>>((counts, operation) => {
    counts[operation.action] += 1;
    return counts;
  }, { create: 0, 'skip-unchanged': 0, conflict: 0, overwrite: 0 });
}

export function renderSummary(result: InstallResult): string {
  const counts = countActions(result);
  const lines = [
    `Target: ${result.targetRoot}`,
    `Mode: ${result.dryRun ? 'dry-run' : 'write'}`,
    `Created: ${counts.create}`,
    `Overwritten: ${counts.overwrite}`,
    `Unchanged: ${counts['skip-unchanged']}`,
    `Conflicts: ${counts.conflict}`
  ];

  if (counts.conflict > 0) lines.push('Next: rerun with --backup --overwrite to replace conflicts safely.');
  if (result.errors.length > 0) lines.push(...result.errors.map((error) => `Error: ${error}`));

  return lines.join('\n');
}
