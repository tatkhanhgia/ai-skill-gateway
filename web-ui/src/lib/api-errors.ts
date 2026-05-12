import type { GatewayErrorBody } from "@/lib/gateway-types";

export class GatewayApiError extends Error {
  readonly status?: number;
  readonly details?: string[];

  constructor(message: string, status?: number, details?: string[]) {
    super(message);
    this.name = "GatewayApiError";
    this.status = status;
    this.details = details;
  }
}

export function normalizeError(error: unknown): GatewayApiError {
  if (error instanceof GatewayApiError) {
    return error;
  }

  if (error instanceof Error) {
    return new GatewayApiError(error.message);
  }

  return new GatewayApiError("Unknown gateway error");
}

export async function errorFromResponse(response: Response): Promise<GatewayApiError> {
  const fallback = `HTTP ${response.status} ${response.statusText}`.trim();

  try {
    const body = (await response.json()) as GatewayErrorBody;
    const details = Array.isArray(body.details)
      ? body.details
      : body.details
        ? [body.details]
        : undefined;
    const message = body.error ? `${fallback}: ${body.error}` : fallback;
    return new GatewayApiError(message, response.status, details);
  } catch {
    return new GatewayApiError(fallback, response.status);
  }
}
