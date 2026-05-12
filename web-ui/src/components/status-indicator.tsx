import { cn } from "@/lib/utils";

interface StatusIndicatorProps {
  state: "ok" | "warn" | "error" | "idle";
  label: string;
}

const colors = {
  ok: "bg-emerald-600",
  warn: "bg-amber-500",
  error: "bg-red-600",
  idle: "bg-zinc-400",
};

export function StatusIndicator({ state, label }: StatusIndicatorProps) {
  return (
    <span className="inline-flex items-center gap-2 text-sm text-zinc-700">
      <span className={cn("h-2.5 w-2.5 rounded-full", colors[state])} />
      {label}
    </span>
  );
}
