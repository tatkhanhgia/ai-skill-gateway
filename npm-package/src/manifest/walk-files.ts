import { readdir } from 'node:fs/promises';
import path from 'node:path';

export async function walkFiles(root: string, shouldSkipDirectory: (directory: string) => boolean = () => false): Promise<string[]> {
  const entries = await readdir(root, { withFileTypes: true });
  const files = await Promise.all(entries.map(async (entry) => {
    const fullPath = path.join(root, entry.name);
    if (entry.isDirectory()) {
      if (shouldSkipDirectory(fullPath)) return [];
      return walkFiles(fullPath, shouldSkipDirectory);
    }
    if (entry.isFile()) return [fullPath];
    return [];
  }));

  return files.flat().sort();
}
