import { cp, mkdir, rm, stat } from 'node:fs/promises';
import path from 'node:path';
import { assetRoots, isExcludedAsset } from './asset-rules.js';
import { packageRoot, repoRoot } from './paths.js';
import { walkFiles } from './walk-files.js';

async function exists(filePath: string): Promise<boolean> {
  try {
    await stat(filePath);
    return true;
  } catch {
    return false;
  }
}

async function main(): Promise<void> {
  const sourceRoot = repoRoot();
  const targetRoot = packageRoot();
  const assetsRoot = path.join(targetRoot, 'assets');

  await rm(assetsRoot, { recursive: true, force: true });

  for (const root of assetRoots) {
    const absoluteSourceRoot = path.join(sourceRoot, root.source);
    if (!(await exists(absoluteSourceRoot))) continue;

    const files = await walkFiles(absoluteSourceRoot);
    for (const file of files) {
      const repoRelative = path.relative(sourceRoot, file);
      if (isExcludedAsset(repoRelative)) continue;

      const rootRelative = path.relative(absoluteSourceRoot, file);
      const target = path.join(targetRoot, root.target, rootRelative);
      await mkdir(path.dirname(target), { recursive: true });
      await cp(file, target, { force: true, errorOnExist: false, preserveTimestamps: true });
    }
  }
}

await main();
