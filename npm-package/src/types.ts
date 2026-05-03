export type AssetType =
  | 'claude-skill'
  | 'claude-agent'
  | 'claude-hook'
  | 'claude-rule'
  | 'claude-script'
  | 'opencode-skill'
  | 'opencode-agent';

export interface AssetManifestFile {
  source: string;
  target: string;
  type: AssetType;
  sha256: string;
  size: number;
}

export interface AssetManifest {
  packageName: string;
  packageVersion: string;
  generatedAt: string;
  files: AssetManifestFile[];
}

export type ConflictPolicy = 'skip' | 'overwrite' | 'backup-and-overwrite';

export interface InstallOptions {
  cwd?: string;
  dryRun?: boolean;
  policy?: ConflictPolicy;
  groups?: string[];
  allowNonProjectDir?: boolean;
}

export type PlannedAction = 'create' | 'skip-unchanged' | 'conflict' | 'overwrite';

export interface PlannedOperation {
  action: PlannedAction;
  source: string;
  target: string;
  relativeTarget: string;
  type: AssetType;
  sha256: string;
  backup?: string;
  reason?: string;
}

export interface InstallResult {
  targetRoot: string;
  dryRun: boolean;
  operations: PlannedOperation[];
  errors: string[];
}
