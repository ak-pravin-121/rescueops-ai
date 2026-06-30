import { Link } from 'react-router-dom'
import { Clock, Server, UserRound } from 'lucide-react'
import SeverityBadge from './SeverityBadge'
import StatusBadge from './StatusBadge'

export default function IncidentCard({ incident }) {
  const opened = new Date(incident.createdAt)

  return (
    <Link
      to={`/incidents/${incident.id}`}
      className="block rounded-lg border border-border bg-panel p-4 transition-colors hover:border-brand/50 hover:bg-panel-raised"
    >
      <div className="flex flex-wrap items-start justify-between gap-2">
        <div className="min-w-0">
          <p className="font-mono text-[11px] text-ink-muted">#{String(incident.id).padStart(5, '0')}</p>
          <h3 className="truncate font-display text-lg font-bold">{incident.title}</h3>
        </div>
        <div className="flex items-center gap-2">
          <SeverityBadge severity={incident.severity} size="sm" />
          <StatusBadge status={incident.status} size="sm" />
        </div>
      </div>

      {incident.rootCause && (
        <p className="mt-2 line-clamp-2 text-sm text-ink-muted">{incident.rootCause}</p>
      )}

      <div className="mt-3 flex flex-wrap items-center gap-4 text-xs text-ink-muted">
        <span className="flex items-center gap-1">
          <Server size={12} /> {incident.serviceName}
        </span>
        <span className="flex items-center gap-1">
          <UserRound size={12} /> {incident.assignedToName || 'Unassigned'}
        </span>
        <span className="flex items-center gap-1">
          <Clock size={12} /> {opened.toLocaleString()}
        </span>
      </div>
    </Link>
  )
}
