import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { PieChart, Pie, Cell, ResponsiveContainer, Tooltip } from 'recharts'
import { ActivitySquare, CheckCircle2, AlertTriangle, TrendingUp, PlusCircle } from 'lucide-react'
import { dashboardApi, incidentsApi, predictionsApi } from '../api/endpoints'
import { SEVERITIES } from '../utils/constants'
import LoadingSpinner from '../components/LoadingSpinner'
import IncidentCard from '../components/IncidentCard'
import LiveIndicator from '../components/LiveIndicator'

export default function Dashboard() {
  const [summary, setSummary] = useState(null)
  const [recentIncidents, setRecentIncidents] = useState([])
  const [predictions, setPredictions] = useState([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    let active = true
    async function load() {
      try {
        const [summaryData, incidents, preds] = await Promise.all([
          dashboardApi.summary(),
          incidentsApi.list({}),
          predictionsApi.recent(),
        ])
        if (!active) return
        setSummary(summaryData)
        setRecentIncidents(incidents.slice(0, 5))
        setPredictions(preds.slice(0, 4))
      } finally {
        if (active) setLoading(false)
      }
    }
    load()
    const interval = setInterval(load, 20000)
    return () => {
      active = false
      clearInterval(interval)
    }
  }, [])

  if (loading) return <LoadingSpinner label="Pulling live metrics" />

  const pieData = summary
    ? Object.entries(summary.severityDistribution)
        .filter(([, count]) => count > 0)
        .map(([key, count]) => ({ name: SEVERITIES[key]?.label || key, value: count, color: SEVERITIES[key]?.color }))
    : []

  return (
    <div className="mx-auto max-w-7xl px-4 py-8">
      <div className="mb-6 flex flex-wrap items-end justify-between gap-4">
        <div>
          <div className="flex items-center gap-3">
            <p className="font-mono text-xs uppercase tracking-widest text-ink-muted">Mission Control</p>
            <LiveIndicator />
          </div>
          <h1 className="font-display text-4xl font-bold">Operations Dashboard</h1>
        </div>
        <Link
          to="/incidents/new"
          className="flex items-center gap-2 rounded-md bg-brand px-4 py-2.5 font-semibold text-void transition-transform hover:scale-105"
        >
          <PlusCircle size={18} /> Report incident
        </Link>
      </div>

      <div className="mb-6 grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-4">
        <StatCard icon={ActivitySquare} label="Active Incidents" value={summary.activeIncidents} accentClass="text-critical" />
        <StatCard icon={CheckCircle2} label="Resolved (30d)" value={summary.resolvedLast30Days} accentClass="text-success" />
        <StatCard icon={TrendingUp} label="Resolution Rate" value={`${summary.resolutionRatePercent}%`} accentClass="text-brand" />
        <StatCard icon={AlertTriangle} label="Total Incidents" value={summary.totalIncidents} accentClass="text-high" />
      </div>

      <div className="grid grid-cols-1 gap-6 lg:grid-cols-3">
        <div className="rounded-lg border border-border bg-panel p-5 lg:col-span-1">
          <h2 className="mb-3 font-display text-lg font-bold">Severity Distribution</h2>
          {pieData.length === 0 ? (
            <p className="py-10 text-center text-sm text-ink-muted">No incidents recorded yet.</p>
          ) : (
            <div className="h-56">
              <ResponsiveContainer width="100%" height="100%">
                <PieChart>
                  <Pie data={pieData} dataKey="value" nameKey="name" innerRadius={50} outerRadius={75} paddingAngle={3}>
                    {pieData.map((entry, i) => (
                      <Cell key={i} fill={entry.color} stroke="transparent" />
                    ))}
                  </Pie>
                  <Tooltip contentStyle={{ background: '#131722', border: '1px solid #232938', borderRadius: 8, fontSize: 12 }} />
                </PieChart>
              </ResponsiveContainer>
            </div>
          )}
          <div className="mt-2 flex flex-wrap justify-center gap-3">
            {pieData.map((entry, i) => (
              <span key={i} className="flex items-center gap-1.5 text-xs text-ink-muted">
                <span className="h-2 w-2 rounded-full" style={{ backgroundColor: entry.color }} />
                {entry.name} ({entry.value})
              </span>
            ))}
          </div>
        </div>

        <div className="rounded-lg border border-border bg-panel p-5 lg:col-span-1">
          <h2 className="mb-3 flex items-center gap-2 font-display text-lg font-bold">
            <TrendingUp size={18} className="text-brand" /> Top Risk Services
          </h2>
          {predictions.length === 0 ? (
            <p className="py-10 text-center text-sm text-ink-muted">
              No predictions yet. Visit the Predictions page to run the failure-risk engine for a service.
            </p>
          ) : (
            <div className="space-y-3">
              {predictions.map((p) => (
                <div key={p.id} className="rounded-md border border-border p-3">
                  <div className="flex items-center justify-between">
                    <span className="font-mono text-sm font-semibold">{p.serviceName}</span>
                    <span className="font-mono text-sm font-bold text-high">{Math.round(p.riskScore)}%</span>
                  </div>
                  <div className="mt-1.5 h-1.5 w-full overflow-hidden rounded-full bg-void">
                    <div className="h-full rounded-full bg-high" style={{ width: `${Math.min(100, p.riskScore)}%` }} />
                  </div>
                </div>
              ))}
            </div>
          )}
          <Link to="/predictions" className="mt-3 block text-center text-xs font-semibold text-brand hover:underline">
            View predictive engine →
          </Link>
        </div>

        <div className="rounded-lg border border-border bg-panel p-5 lg:col-span-1">
          <h2 className="mb-3 font-display text-lg font-bold">Recent Incidents</h2>
          {recentIncidents.length === 0 ? (
            <p className="py-10 text-center text-sm text-ink-muted">No incidents reported yet. RescueOps is standing by.</p>
          ) : (
            <div className="space-y-3">
              {recentIncidents.map((i) => (
                <IncidentCard key={i.id} incident={i} />
              ))}
            </div>
          )}
          <Link to="/incidents" className="mt-3 block text-center text-xs font-semibold text-brand hover:underline">
            View all incidents →
          </Link>
        </div>
      </div>
    </div>
  )
}

function StatCard({ icon: Icon, label, value, accentClass }) {
  return (
    <div className="rounded-lg border border-border bg-panel p-4">
      <div className="flex items-center justify-between">
        <p className="font-mono text-xs uppercase tracking-wide text-ink-muted">{label}</p>
        <Icon size={16} className={accentClass} />
      </div>
      <p className="mt-2 font-display text-3xl font-bold">{value}</p>
    </div>
  )
}
