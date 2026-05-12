import { useState } from "react";
import { Activity, Boxes, KeyRound, PackagePlus, Server, X } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { DashboardView } from "@/features/dashboard/dashboard-view";
import { PublishView } from "@/features/publish/publish-view";
import { SkillExplorerView } from "@/features/skills/skill-explorer-view";
import { useAppConfig } from "@/providers/app-config-provider";
import { cn } from "@/lib/utils";

type View = "dashboard" | "explorer" | "publish";

const navItems = [
  { id: "dashboard", label: "Dashboard", icon: Activity },
  { id: "explorer", label: "Explorer", icon: Boxes },
  { id: "publish", label: "Publish", icon: PackagePlus },
] satisfies Array<{ id: View; label: string; icon: typeof Activity }>;

export function App() {
  const [view, setView] = useState<View>("dashboard");
  const { baseUrl, setBaseUrl, apiKey, setApiKey, apiKeyConfigured, clearApiKey } = useAppConfig();

  return (
    <div className="min-h-screen bg-zinc-100 text-zinc-950">
      <header className="sticky top-0 z-10 border-b border-zinc-300 bg-white">
        <div className="flex flex-col gap-3 px-4 py-3 lg:flex-row lg:items-center lg:justify-between">
          <div className="flex items-center gap-3">
            <div className="flex h-9 w-9 items-center justify-center rounded-md bg-emerald-700 text-white">
              <Server className="h-5 w-5" />
            </div>
            <div>
              <h1 className="text-base font-semibold">AI Skill Gateway Console</h1>
              <p className="text-xs text-zinc-500">Local REST operations</p>
            </div>
          </div>
          <div className="grid gap-2 md:grid-cols-[220px_280px_auto] lg:w-auto">
            <select
              className="h-9 rounded-md border border-zinc-300 bg-white px-3 text-sm"
              value={baseUrl}
              onChange={(event) => setBaseUrl(event.target.value)}
            >
              <option value="http://localhost:18080">Docker :18080</option>
              <option value="http://localhost:8080">Maven :8080</option>
            </select>
            <div className="relative">
              <KeyRound className="pointer-events-none absolute left-3 top-2.5 h-4 w-4 text-zinc-400" />
              <Input
                className="pl-9 pr-9"
                type="password"
                value={apiKey}
                placeholder={apiKeyConfigured ? "API key loaded in memory" : "API key for protected actions"}
                onChange={(event) => setApiKey(event.target.value)}
                autoComplete="off"
              />
              {apiKeyConfigured ? (
                <button
                  className="absolute right-2 top-2 rounded p-0.5 text-zinc-500 hover:bg-zinc-100"
                  type="button"
                  title="Clear API key"
                  onClick={clearApiKey}
                >
                  <X className="h-4 w-4" />
                </button>
              ) : null}
            </div>
            <div className="flex items-center rounded-md border border-zinc-300 bg-zinc-50 px-3 text-sm text-zinc-700">
              Key: {apiKeyConfigured ? "memory only" : "not set"}
            </div>
          </div>
        </div>
      </header>
      <div className="grid lg:grid-cols-[220px_1fr]">
        <nav className="border-b border-zinc-300 bg-white p-2 lg:min-h-[calc(100vh-66px)] lg:border-b-0 lg:border-r">
          <div className="grid grid-cols-3 gap-2 lg:grid-cols-1">
            {navItems.map((item) => {
              const Icon = item.icon;
              return (
                <Button
                  key={item.id}
                  variant="ghost"
                  className={cn("justify-start", view === item.id && "bg-emerald-50 text-emerald-900")}
                  onClick={() => setView(item.id)}
                >
                  <Icon className="h-4 w-4" />
                  <span className="hidden sm:inline">{item.label}</span>
                </Button>
              );
            })}
          </div>
        </nav>
        <main className="p-4">
          {view === "dashboard" ? <DashboardView /> : null}
          {view === "explorer" ? <SkillExplorerView onPublishFirst={() => setView("publish")} /> : null}
          {view === "publish" ? <PublishView onViewExplorer={() => setView("explorer")} /> : null}
        </main>
      </div>
    </div>
  );
}
