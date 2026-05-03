import { copyFile, mkdir, rename } from 'node:fs/promises';
import path from 'node:path';
import type { PlannedOperation } from '../types.js';

export async function executeOperations(operations: PlannedOperation[]): Promise<string[]> {
  const errors: string[] = [];

  for (const operation of operations) {
    if (operation.action !== 'create' && operation.action !== 'overwrite') continue;

    try {
      if (operation.backup) {
        await mkdir(path.dirname(operation.backup), { recursive: true });
        await copyFile(operation.target, operation.backup);
      }

      await mkdir(path.dirname(operation.target), { recursive: true });
      const tempTarget = `${operation.target}.gtk-skill-tmp`;
      await copyFile(operation.source, tempTarget);
      await rename(tempTarget, operation.target);
    } catch (error) {
      errors.push(`${operation.relativeTarget}: ${error instanceof Error ? error.message : String(error)}`);
    }
  }

  return errors;
}
