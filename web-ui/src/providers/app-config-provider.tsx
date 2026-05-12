import { createContext, useContext, useMemo, useState, type ReactNode } from "react";
import { createGatewayApiClient } from "@/lib/gateway-api-client";

interface AppConfigContextValue {
  baseUrl: string;
  apiKey: string;
  apiKeyConfigured: boolean;
  setBaseUrl: (value: string) => void;
  setApiKey: (value: string) => void;
  clearApiKey: () => void;
  client: ReturnType<typeof createGatewayApiClient>;
}

const AppConfigContext = createContext<AppConfigContextValue | null>(null);

export function AppConfigProvider({ children }: { children: ReactNode }) {
  const [baseUrl, setBaseUrl] = useState("http://localhost:18080");
  const [apiKey, setApiKey] = useState("");
  const client = useMemo(() => createGatewayApiClient(baseUrl, apiKey), [baseUrl, apiKey]);

  const value = useMemo<AppConfigContextValue>(
    () => ({
      baseUrl,
      apiKey,
      apiKeyConfigured: apiKey.trim().length > 0,
      setBaseUrl,
      setApiKey,
      clearApiKey: () => setApiKey(""),
      client,
    }),
    [apiKey, baseUrl, client],
  );

  return <AppConfigContext.Provider value={value}>{children}</AppConfigContext.Provider>;
}

export function useAppConfig(): AppConfigContextValue {
  const value = useContext(AppConfigContext);
  if (!value) {
    throw new Error("useAppConfig must be used within AppConfigProvider");
  }
  return value;
}
