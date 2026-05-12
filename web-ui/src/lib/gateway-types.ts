export interface SkillSummary {
  id?: number;
  name: string;
  category?: string;
  description?: string;
  downloadCount?: number;
  score?: number;
  tags?: string[];
}

export interface SkillDetail extends SkillSummary {
  author?: string;
  repositoryUrl?: string;
  latestVersion?: VersionInfo;
}

export interface VersionInfo {
  id?: number;
  version: string;
  prerelease?: boolean;
  latest?: boolean;
  yanked?: boolean;
  publishedAt?: string;
}

export interface VersionResolution {
  skillName?: string;
  constraint?: string;
  selectedVersion?: string;
  candidates?: string[];
}

export interface DependencyResolution {
  name?: string;
  version?: string;
  dependencies?: unknown[];
}

export interface EmbeddingStatus {
  provider?: string;
  url?: string;
  model?: string;
  dimension?: number;
  timeoutSeconds?: number;
  configured?: boolean;
  message?: string;
}

export interface PublishPayload {
  name: string;
  version: string;
  description: string;
  category: string;
  tags?: string[];
  author?: string;
  repositoryUrl?: string;
  requires?: unknown[];
  releaseNotes?: string;
}

export interface PublishResponse {
  skillId?: number;
  versionId?: number;
  name?: string;
  version?: string;
}

export interface SkillBundleFileInfo {
  path: string;
  sha256: string;
  size: number;
  mediaType?: string;
  role?: string;
}

export interface SkillBundlePublishResponse extends PublishResponse {
  bundleSha256?: string;
  bundleSize?: number;
  fileCount?: number;
  files?: SkillBundleFileInfo[];
}

export interface GatewayErrorBody {
  error?: string;
  details?: string[] | string;
}
