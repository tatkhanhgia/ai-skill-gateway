import { errorFromResponse, GatewayApiError } from "@/lib/api-errors";
import type {
  DependencyResolution,
  EmbeddingStatus,
  PublishPayload,
  PublishResponse,
  SkillBundlePublishResponse,
  SkillDetail,
  SkillSummary,
  VersionInfo,
  VersionResolution,
} from "@/lib/gateway-types";

type QueryValue = string | number | boolean | undefined | null;

interface RequestOptions {
  method?: "GET" | "POST";
  query?: Record<string, QueryValue>;
  body?: unknown;
  apiKey?: string;
  protectedCall?: boolean;
}

async function requestForm<T>(baseUrl: string, path: string, body: FormData, apiKey: string): Promise<T> {
  if (!apiKey.trim()) {
    throw new GatewayApiError("API key required for this action");
  }

  let response: Response;
  try {
    response = await fetch(buildUrl(baseUrl, path), {
      method: "POST",
      headers: { "X-API-Key": apiKey.trim() },
      body,
    });
  } catch (error) {
    if (error instanceof TypeError) {
      throw new GatewayApiError(backendHint(baseUrl));
    }
    throw error;
  }

  if (!response.ok) {
    throw await errorFromResponse(response);
  }

  return (await response.json()) as T;
}

function buildUrl(baseUrl: string, path: string, query?: Record<string, QueryValue>): string {
  const url = new URL(path, baseUrl.endsWith("/") ? baseUrl : `${baseUrl}/`);
  Object.entries(query ?? {}).forEach(([key, value]) => {
    if (value !== undefined && value !== null && `${value}`.trim() !== "") {
      url.searchParams.set(key, `${value}`);
    }
  });
  return url.toString();
}

function backendHint(baseUrl: string): string {
  const url = new URL(baseUrl);
  if (url.hostname === "localhost" && url.port === "8080") {
    return `Cannot reach ${url.origin}. Check Maven dev server, port, and CORS.`;
  }
  if (url.hostname === "localhost" && url.port === "18080") {
    return `Cannot reach ${url.origin}. Check Docker Compose backend, port, and CORS.`;
  }
  return `Cannot reach ${url.origin}. Check backend server, port, and CORS.`;
}

async function requestJson<T>(baseUrl: string, path: string, options: RequestOptions = {}): Promise<T> {
  if (options.protectedCall && !options.apiKey?.trim()) {
    throw new GatewayApiError("API key required for this action");
  }

  const headers = new Headers();
  if (options.body !== undefined) {
    headers.set("Content-Type", "application/json");
  }
  if (options.apiKey?.trim()) {
    headers.set("X-API-Key", options.apiKey.trim());
  }

  let response: Response;
  try {
    response = await fetch(buildUrl(baseUrl, path, options.query), {
      method: options.method ?? "GET",
      headers,
      body: options.body === undefined ? undefined : JSON.stringify(options.body),
    });
  } catch (error) {
    if (error instanceof TypeError) {
      throw new GatewayApiError(backendHint(baseUrl));
    }
    throw error;
  }

  if (!response.ok) {
    throw await errorFromResponse(response);
  }

  if (response.status === 204) {
    return undefined as T;
  }

  const text = await response.text();
  return (text ? JSON.parse(text) : undefined) as T;
}

export function createGatewayApiClient(baseUrl: string, apiKey: string) {
  return {
    health: () => requestJson<unknown>(baseUrl, "/q/health"),
    embeddingStatus: () => requestJson<EmbeddingStatus>(baseUrl, "/api/v1/embedding/status"),
    listSkills: (query: Record<string, QueryValue>) =>
      requestJson<SkillSummary[]>(baseUrl, "/api/v1/skills", { query }),
    searchSkills: (query: Record<string, QueryValue>) =>
      requestJson<SkillSummary[]>(baseUrl, "/api/v1/skills/search", { query }),
    getSkill: (name: string) => requestJson<SkillDetail>(baseUrl, `/api/v1/skills/${encodeURIComponent(name)}`),
    versions: (name: string) =>
      requestJson<VersionInfo[]>(baseUrl, `/api/v1/skills/${encodeURIComponent(name)}/versions`),
    resolve: (name: string, constraint: string) =>
      requestJson<VersionResolution>(baseUrl, `/api/v1/skills/${encodeURIComponent(name)}/resolve`, {
        query: { constraint },
      }),
    dependencies: (name: string, version: string) =>
      requestJson<DependencyResolution>(
        baseUrl,
        `/api/v1/skills/${encodeURIComponent(name)}/dependencies/${encodeURIComponent(version)}`,
      ),
    publish: (body: PublishPayload) =>
      requestJson<PublishResponse>(baseUrl, "/api/v1/skills/publish", {
        method: "POST",
        body,
        apiKey,
        protectedCall: true,
      }),
    publishBundle: (bundle: File) => {
      const form = new FormData();
      form.set("bundle", bundle);
      return requestForm<SkillBundlePublishResponse>(baseUrl, "/api/v1/skills/publish-bundle", form, apiKey);
    },
    yankVersion: (name: string, version: string, reason: string) =>
      requestJson<void>(baseUrl, `/api/v1/skills/${encodeURIComponent(name)}/versions/${encodeURIComponent(version)}/yank`, {
        method: "POST",
        query: { reason: reason.trim() || "unspecified" },
        apiKey,
        protectedCall: true,
      }),
  };
}
