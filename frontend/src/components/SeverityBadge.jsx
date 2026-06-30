import { SEVERITIES } from '../utils/constants'

export default function SeverityBadge({ severity, size = 'md' }) {
  const info = SEVERITIES[severity] || { label: severity, color: 'var(--color-ink-muted)' }
  const sizeClasses = size === 'sm' ? 'text-[10px] px-1.5 py-0.5' : 'text-xs px-2 py-1'

  return (
    <span
      className={`inline-flex items-center gap-1.5 rounded-md border font-mono font-semibold uppercase tracking-wide ${sizeClasses}`}
      style={{ color: info.color, borderColor: info.color + '55', backgroundColor: info.color + '14' }}
    >
      <span className="h-1.5 w-1.5 rounded-full" style={{ backgroundColor: info.color }} />
      {info.label}
    </span>
  )
}
