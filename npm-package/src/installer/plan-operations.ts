import { stat } from 'node:fs/promises';
import path from 'node:path';
import type { AssetManifestFile, ConflictPolicy, PlannedOperation } from '../types.js';
import { packageRoot } from '../manifest/paths.js';
import { sha256File } from '../manifest/checksum.js';

async function fileExists(filePath: string): Promise<boolean> {
  try {
    const fileStat = await stat(filePath);
    return fileStat.isFile();
  } catch {
    return false;
  }
}

function backupPath(root: string, relativeTarget: string): string {
  const timestamp = new Date().toISOString().replace(/[:.]/g, '-');
  return path.join(root, '.gtk-skill', 'backups', timestamp, relativeTarget);
}

export async function planOperations(root: string, files: AssetManifestFile[], policy: ConflictPolicy): Promise<PlannedOperation[]> {
  const operations: PlannedOperation[] = [];

  for (const file of files) {
    const source = path.join(packageRoot(), file.source);
    const target = path.join(root, file.target);
    const exists = await fileExists(target);

    if (!exists) {
      operations.push({ action: 'create', source, target, relativeTarget: file.target, type: file.type, sha256: file.sha256 });
      continue;
    }

    const targetHash = await sha256File(target);
    if (targetHash === file.sha256) {
      operations.push({ action: 'skip-unchanged', source, target, relativeTarget: file.target, type: file.type, sha256: file.sha256 });
      continue;
    }

    if (policy === 'skip') {
      operations.push({ action: 'conflict', source, target, relativeTarget: file.target, type: file.type, sha256: file.sha256, reason: 'target differs' });
      continue;
    }

    operations.push({
      action: 'overwrite',
      source,
      target,
      relativeTarget: file.target,
      type: file.type,
      sha256: file.sha256,
      backup: policy === 'backup-and-overwrite' ? backupPath(root, file.target) : undefined
    });
  }

  return operations;
}
