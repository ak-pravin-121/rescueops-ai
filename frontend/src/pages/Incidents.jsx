import { useEffect, useState, useCallback } from 'react'
import { Link } from 'react-router-dom'
import { PlusCircle, SlidersHorizontal } from 'lucide-react'
import { incidentsApi } from '../api/endpoints'
import { SEVERITY_ORDER, SEVERITIES, STATUS_ORDER, STATUSES } from '../utils/constants'
import IncidentCard from '../components/IncidentCard'
import LoadingSpinner from '../components/LoadingSpinner'

export default function Incidents() {
  const [severity, setSeverity] = useState('')
  const [status, setStatus] = useState('')
  const [serviceName, setServiceName] = useState('')
  const [services, setServices] = useState([])
  const [incidents, setIncidents] = useState([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    incidentsApi.serviceNames().then(setServices).catch(() => {})
  }, [])

  const fetchIncidents = useCallback(async () => {
    setLoading(true)
    try {
      const params = {}
      if (severity) params.severity = severity
      if (status) params.status = status
      if (serviceName) params.serviceName = serviceName
      const data = await incidentsApi.list(params)
      setIncidents(data)
    } finally {
      setLoading(false)
    }
  }, [severity, status, serviceName])

  useEffect(() => {
    fetchIncidents()
  }, [fetchIncidents])

  return (
    <div className="mx-auto max-w-5xl px-4 py-8">
      <div className="mb-6 flex flex-wrap items-end justify-between gap-4">
        <div>
          <p className="font-mono text-xs uppercase tracking-widest text-ink-muted">Incident Register</p>
          <h1 className="font-display text-4xl font-bold">All Incidents</h1>
        </div>
        <Link
          to="/incidents/new"
          className="flex items-center gap-2 rounded-md bg-brand px-4 py-2.5 font-semibold text-void transition-transform hover:scale-105"
        >
          <PlusCircle size={18} /> Report incident
        </Link>
      </div>

      <div className="mb-6 flex flex-wrap items-center gap-3 rounded-lg border border-border bg-panel p-4">
        <SlidersHorizontal size={16} className="text-ink-muted" />

        <select
          value={severity}
          onChange={(e) => setSeverity(e.target.value)}
          className="rounded-md border border-border bg-void px-3 py-1.5 text-sm text-ink outline-none focus:border-brand"
        >
          <option value="">All severities</option>
          {SEVERITY_ORDER.map((s) => (
            <option key={s} value={s}>{SEVERITIES[s].label}</option>
          ))}
        </select>

        <select
          value={status}
          onChange={(e) => setStatus(e.target.value)}
          className="rounded-md border border-border bg-void px-3 py-1.5 text-sm text-ink outline-none focus:border-brand"
        >
          <option value="">All statuses</option>
          {STATUS_ORDER.map((s) => (
            <option key={s} value={s}>{STATUSES[s].label}</option>
          ))}
        </select>

        <select
          value={serviceName}
          onChange={(e) => setServiceName(e.target.value)}
          className="rounded-md border border-border bg-void px-3 py-1.5 text-sm text-ink outline-none focus:border-brand"
        >
          <option value="">All services</option>
          {services.map((s) => (
            <option key={s} value={s}>{s}</option>
          ))}
        </select>
      </div>

      {loading ? (
        <LoadingSpinner label="Fetching incidents" />
      ) : incidents.length === 0 ? (
        <div className="rounded-lg border border-dashed border-border bg-panel py-16 text-center text-ink-muted">
          <p className="font-display text-xl font-bold text-ink">No incidents match these filters</p>
          <p className="mt-1 text-sm">Adjust the filters above, or report a new incident.</p>
        </div>
      ) : (
        <div className="space-y-3">
          {incidents.map((incident) => (
            <IncidentCard key={incident.id} incident={incident} />
          ))}
        </div>
      )}
    </div>
  )
}
