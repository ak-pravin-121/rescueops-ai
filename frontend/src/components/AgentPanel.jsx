import { Loader2, Sparkles } from 'lucide-react'
import SeverityBadge from './SeverityBadge'

export default function AgentPanel({ title, description, icon: Icon = Sparkles, result, loading, onRun, runLabel = 'Run agent' }) {
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

      {result && (
        <div className="mt-3 space-y-2 border-t border-border pt-3">
          <div className="flex items-center gap-3">
            {result.severity && <SeverityBadge severity={result.severity} size="sm" />}
            {typeof result.confidence === 'number' && (
              <span className="font-mono text-xs text-ink-muted">confidence: {result.confidence}%</span>
            )}
          </div>
          {result.rootCause && (
            <p className="text-sm text-ink">{result.rootCause}</p>
          )}
          {result.suggestedFix && (
            <p className="rounded-md bg-success/10 px-3 py-2 text-sm text-success">
              <span className="font-mono text-[10px] uppercase tracking-wide opacity-80">Suggested fix · </span>
              {result.suggestedFix}
            </p>
          )}
        </div>
      )}
    </div>
  )
}
