import { Loader2, Sparkles } from 'lucide-react'

export default function TextAgentPanel({ title, description, icon: Icon = Sparkles, content, loading, onRun, runLabel = 'Run agent' }) {
  return (
    <div className="rounded-lg border border-border bg-panel p-4">
      <div className="flex items-start justify-between gap-3">
        <div className="flex items-start gap-2">
          <Icon size={18} className="mt-0.5 text-brand" />
          <div>
            <h3 className="font-display text-base font-bold">{title}</h3>
            {description && <p className="text-xs text-ink-muted">{description}</p>}
          </div>
        </div>
        <button
          onClick={onRun}
          disabled={loading}
          className="flex items-center gap-1.5 whitespace-nowrap rounded-md bg-brand/15 px-3 py-1.5 text-xs font-semibold text-brand transition-colors hover:bg-brand/25 disabled:opacity-50"
        >
          {loading ? <Loader2 size={14} className="animate-spin" /> : <Sparkles size={14} />}
          {loading ? 'Running…' : runLabel}
        </button>
      </div>

      {content && (
        <pre className="mt-3 max-h-80 overflow-y-auto whitespace-pre-wrap rounded-md border border-border bg-void/60 p-3 font-mono text-xs leading-relaxed text-ink">
          {content}
        </pre>
      )}
    </div>
  )
}
