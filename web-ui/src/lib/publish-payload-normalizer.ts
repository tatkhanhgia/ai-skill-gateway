import type { PublishPayload } from "@/lib/gateway-types";
import { DEFAULT_PUBLISH_CATEGORY } from "@/lib/publish-payload-validator";

const DEFAULT_VERSION = "1.0.0";

export function normalizePublishPayload(input: Record<string, unknown>): Partial<PublishPayload> {
  const metadata = isRecord(input.metadata) ? input.metadata : {};
  const tags = toStringArray(input.tags ?? metadata.tags);

  return clean({
    name: asString(input.name),
    version: asString(input.version) || DEFAULT_VERSION,
    description: asString(input.description),
    category: categoryOrDefault(input),
    tags: tags.length ? tags : undefined,
    author: asString(input.author ?? metadata.author),
    repositoryUrl: asString(input.repositoryUrl ?? input.repository ?? metadata.repositoryUrl),
    requires: Array.isArray(input.requires) ? input.requires : undefined,
    releaseNotes: asString(input.releaseNotes ?? input.release_notes),
  });
}

function categoryOrDefault(input: Record<string, unknown>): string {
  if (!Object.hasOwn(input, "category")) {
    return DEFAULT_PUBLISH_CATEGORY;
  }
  return asString(input.category) ?? String(input.category).trim();
}

function clean<T extends Record<string, unknown>>(value: T): Partial<PublishPayload> {
  return Object.fromEntries(Object.entries(value).filter(([, item]) => item !== undefined && item !== "")) as Partial<PublishPayload>;
}

function asString(value: unknown): string | undefined {
  return typeof value === "string" ? value.trim() : undefined;
}

function toStringArray(value: unknown): string[] {
  if (Array.isArray(value)) {
    return value.map((item) => String(item).trim()).filter(Boolean);
  }
  if (typeof value === "string") {
    return value.split(",").map((item) => item.trim()).filter(Boolean);
  }
  return [];
}

function isRecord(value: unknown): value is Record<string, unknown> {
  return typeof value === "object" && value !== null && !Array.isArray(value);
}
