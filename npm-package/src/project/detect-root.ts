import { stat } from 'node:fs/promises';
import path from 'node:path';

const markers = ['.git', 'package.json', 'pom.xml', 'pyproject.toml', 'Cargo.toml', 'go.mod'];

async function exists(filePath: string): Promise<boolean> {
  try {
    await stat(filePath);
    return true;
  } catch {
    return false;
  }
}

export interface ProjectRoot {
  root: string;
  hasMarker: boolean;
}

export async function detectProjectRoot(cwd = process.cwd()): Promise<ProjectRoot> {
  const root = path.resolve(cwd);
  const checks = await Promise.all(markers.map((marker) => exists(path.join(root, marker))));
  return { root, hasMarker: checks.some(Boolean) };
}
