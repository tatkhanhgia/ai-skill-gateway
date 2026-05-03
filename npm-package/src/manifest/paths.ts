import path from 'node:path';
import { fileURLToPath } from 'node:url';

const currentFile = fileURLToPath(import.meta.url);

export function packageRoot(): string {
  return path.resolve(path.dirname(currentFile), '..', '..', '..');
}

export function repoRoot(): string {
  return path.resolve(packageRoot(), '..');
}
