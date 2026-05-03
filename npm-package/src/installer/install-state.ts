import { mkdir, readFile, writeFile } from 'node:fs/promises';
import path from 'node:path';
import type { AssetManifest, AssetManifestFile } from '../types.js';

export interface InstallState {
  packageName: string;
  packageVersion: string;
  installedAt: string;
  files: Pick<AssetManifestFile, 'target' | 'sha256' | 'type'>[];
}

export async function readInstallState(root: string): Promise<InstallState | null> {
  try {
    return JSON.parse(await readFile(path.join(root, '.gtk-skill', 'install-manifest.json'), 'utf8')) as InstallState;
  } catch {
    return null;
  }
}

export async function writeInstallState(root: string, manifest: AssetManifest, files: AssetManifestFile[]): Promise<void> {
  const state: InstallState = {
    packageName: manifest.packageName,
    packageVersion: manifest.packageVersion,
    installedAt: new Date().toISOString(),
    files: files.map((file) => ({ target: file.target, sha256: file.sha256, type: file.type }))
  };
  const statePath = path.join(root, '.gtk-skill', 'install-manifest.json');
  await mkdir(path.dirname(statePath), { recursive: true });
  await writeFile(statePath, `${JSON.stringify(state, null, 2)}\n`);
}
