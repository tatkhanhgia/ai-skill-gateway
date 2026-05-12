import type { PublishPayload } from "@/lib/gateway-types";

export const PUBLISH_CATEGORIES = ["ai", "backend", "frontend", "devops", "security", "data", "testing", "utility"] as const;
export const DEFAULT_PUBLISH_CATEGORY = "utility";

const requiredFields = ["name", "version", "description", "category"] as const;

export function validatePublishPayload(payload: Partial<PublishPayload>): string[] {
  const errors = requiredFields
    .filter((field) => !payload[field]?.trim())
    .map((field) => `${field} is required`);

  const category = payload.category?.trim().toLowerCase();
  if (category && !PUBLISH_CATEGORIES.includes(category as (typeof PUBLISH_CATEGORIES)[number])) {
    errors.push(`category must be one of: ${PUBLISH_CATEGORIES.join(", ")}`);
  }

  return errors;
}

export function isPublishPayload(payload: Partial<PublishPayload>): payload is PublishPayload {
  return validatePublishPayload(payload).length === 0;
}
