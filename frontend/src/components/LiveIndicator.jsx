export default function LiveIndicator({ label = 'LIVE' }) {
  return (
    <span className="inline-flex items-center gap-2 font-mono text-[11px] uppercase tracking-widest text-success">
      <span className="pulse-dot" />
      {label}
    </span>
  )
}
