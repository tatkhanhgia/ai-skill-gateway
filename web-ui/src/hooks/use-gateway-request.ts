import { useCallback, useRef, useState } from "react";
import { normalizeError, type GatewayApiError } from "@/lib/api-errors";

export interface RequestState<T> {
  data?: T;
  error?: GatewayApiError;
  loading: boolean;
}

export function useGatewayRequest<T>() {
  const [state, setState] = useState<RequestState<T>>({ loading: false });
  const requestId = useRef(0);

  const run = useCallback(async (request: () => Promise<T>): Promise<T | undefined> => {
    const currentRequestId = requestId.current + 1;
    requestId.current = currentRequestId;
    setState({ loading: true });
    try {
      const data = await request();
      if (requestId.current !== currentRequestId) {
        return undefined;
      }
      setState({ data, loading: false });
      return data;
    } catch (error) {
      if (requestId.current !== currentRequestId) {
        return undefined;
      }
      const normalized = normalizeError(error);
      setState((previous) => ({ ...previous, error: normalized, loading: false }));
      return undefined;
    }
  }, []);

  const reset = useCallback(() => {
    requestId.current += 1;
    setState({ loading: false });
  }, []);

  return { ...state, run, reset };
}
