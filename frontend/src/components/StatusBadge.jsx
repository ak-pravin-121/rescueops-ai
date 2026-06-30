import { STATUSES } from '../utils/constants'

export default function StatusBadge({ status, size = 'md' }) {
  const info = STATUSES[status] || { label: status, color: 'var(--color-ink-muted)' }
  const sizeClasses = size === 'sm' ? 'text-[10px] px-1.5 py-0.5' : 'text-xs px-2 py-1'

  return (
    <span
      className={`inline-flex items-center rounded-full border font-mono font-medium uppercase tracking-wide ${sizeClasses}`}
      style={{ color: info.color, borderColor: info.color + '55', backgroundColor: info.color + '14' }}
    >
      {info.label}
    </span>
  )
}
