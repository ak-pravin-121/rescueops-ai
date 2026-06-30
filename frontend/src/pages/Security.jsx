import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { ShieldAlert, ExternalLink } from 'lucide-react'
import { aiApi } from '../api/endpoints'
import SeverityBadge from '../components/SeverityBadge'
import LoadingSpinner from '../components/LoadingSpinner'

export default function Security() {
  const [alerts, setAlerts] = useState([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    aiApi.securityAlerts().then(setAlerts).finally(() => setLoading(false))
  }, [])

  return (
    <div className="mx-auto max-w-4xl px-4 py-8">
      <div className="mb-6">
        <p className="font-mono text-xs uppercase tracking-widest text-ink-muted">Security Intelligence</p>
        <h1 className="font-display text-4xl font-bold">Threat alerts</h1>
        <p className="mt-1 text-sm text-ink-muted">
          Findings from the Security Agent scanning incident logs for SQLi, brute force, XSS and DDoS indicators.
        </p>
      </div>

      {loading ? (
        <LoadingSpinner label="Loading security findings" />
      ) : alerts.length === 0 ? (
        <div className="rounded-lg border border-dashed border-border bg-panel py-16 text-center text-ink-muted">
          <ShieldAlert size={28} className="mx-auto mb-2 text-ink-muted" />
          <p className="font-display text-xl font-bold text-ink">No threats detected</p>
          <p className="mt-1 text-sm">Run the Security Agent from an incident's detail page to scan its logs.</p>
        </div>
      ) : (
        <div className="space-y-3">
          {alerts.map((a) => (
            <div key={a.id} className="rounded-lg border border-border bg-panel p-4">
              <div className="flex items-start justify-between gap-3">
                <div className="flex items-start gap-2">
                  <ShieldAlert size={16} className="mt-0.5 text-critical" />
                  <p className="text-sm text-ink">{a.rootCause}</p>
                </div>
                <SeverityBadge severity={a.severity} size="sm" />
              </div>
              {a.suggestedFix && (
                <p className="mt-2 rounded-md bg-success/10 px-3 py-2 text-sm text-success">
                  <span className="font-mono text-[10px] uppercase tracking-wide opacity-80">Recommendation · </span>
                  {a.suggestedFix}
                </p>
              )}
              <div className="mt-2 flex items-center justify-between text-xs text-ink-muted">
                <span>{new Date(a.createdAt).toLocaleString()}</span>
                <Link to={`/incidents/${a.incidentId}`} className="flex items-center gap-1 font-semibold text-brand hover:underline">
                  View incident <ExternalLink size={11} />
                </Link>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  )
}
