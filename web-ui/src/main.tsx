import { StrictMode } from "react";
import { createRoot } from "react-dom/client";
import { App } from "@/app/app";
import { AppConfigProvider } from "@/providers/app-config-provider";
import "@/styles.css";

createRoot(document.getElementById("root")!).render(
  <StrictMode>
    <AppConfigProvider>
      <App />
    </AppConfigProvider>
  </StrictMode>,
);
