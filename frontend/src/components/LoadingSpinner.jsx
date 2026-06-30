export default function LoadingSpinner({ label = 'Loading...' }) {
  return (
    <div className="flex flex-col items-center justify-center gap-3 py-16 text-ink-muted">
      <div className="h-8 w-8 animate-spin rounded-full border-[3px] border-border border-t-brand" />
      <p className="font-mono text-xs uppercase tracking-wide">{label}</p>
    </div>
  )
}
