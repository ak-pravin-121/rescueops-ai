import { useEffect, useState } from 'react'
import { TrendingUp, Play, Loader2 } from 'lucide-react'
import { predictionsApi, incidentsApi } from '../api/endpoints'
import LoadingSpinner from '../components/LoadingSpinner'

export default function Predictions() {
  const [predictions, setPredictions] = useState([])
  const [services, setServices] = useState([])
  const [selectedService, setSelectedService] = useState('')
  const [loading, setLoading] = useState(true)
  const [running, setRunning] = useState(false)

  const load = async () => {
    setLoading(true)
    try {
      const [preds, svc] = await Promise.all([predictionsApi.recent(), incidentsApi.serviceNames()])
      setPredictions(preds)
      setServices(svc)
      if (!selectedService && svc.length > 0) setSelectedService(svc[0])
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    load()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])

  const runPrediction = async () => {
    if (!selectedService) return
    setRunning(true)
    try {
      await predictionsApi.run(selectedService)
      await load()
    } finally {
      setRunning(false)
    }
  }

  return (
    <div className="mx-auto max-w-4xl px-4 py-8">
      <div className="mb-6">
        <p className="font-mono text-xs uppercase tracking-widest text-ink-muted">Predictive Failure Engine</p>
        <h1 className="font-display text-4xl font-bold">Risk before it becomes an outage</h1>
        <p className="mt-1 text-sm text-ink-muted">
          Combines incident frequency &amp; severity trends per service with Gemini's reasoning over that history.
        </p>
      </div>

      <div className="mb-6 flex flex-wrap items-center gap-3 rounded-lg border border-border bg-panel p-4">
        <select
          value={selectedService}
          onChange={(e) => setSelectedService(e.target.value)}
          className="rounded-md border border-border bg-void px-3 py-2 text-sm outline-none focus:border-brand"
        >
          {services.length === 0 && <option value="">No services yet</option>}
          {services.map((s) => (
            <option key={s} value={s}>{s}</option>
          ))}
        </select>
        <button
          onClick={runPrediction}
          disabled={running || !selectedService}
          className="flex items-center gap-2 rounded-md bg-brand px-4 py-2 text-sm font-semibold text-void transition-opacity hover:opacity-90 disabled:opacity-50"
        >
          {running ? <Loader2 size={15} className="animate-spin" /> : <Play size={15} />}
          {running ? 'Running risk model…' : 'Run prediction'}
        </button>
      </div>

      {loading ? (
        <LoadingSpinner label="Loading predictions" />
      ) : predictions.length === 0 ? (
        <div className="rounded-lg border border-dashed border-border bg-panel py-16 text-center text-ink-muted">
          <p className="font-display text-xl font-bold text-ink">No predictions yet</p>
          <p className="mt-1 text-sm">Pick a service above and run the failure-risk engine.</p>
        </div>
      ) : (
        <div className="space-y-4">
          {predictions.map((p) => (
            <PredictionCard key={p.id} prediction={p} />
          ))}
        </div>
      )}
    </div>
  )
}

function PredictionCard({ prediction }) {
  const riskColor = prediction.riskScore >= 70 ? 'var(--color-critical)' : prediction.riskScore >= 40 ? 'var(--color-high)' : 'var(--color-success)'

  return (
    <div className="rounded-lg border border-border bg-panel p-5">
      <div className="flex flex-wrap items-start justify-between gap-3">
        <div className="flex items-center gap-2">
          <TrendingUp size={18} style={{ color: riskColor }} />
          <h3 className="font-display text-lg font-bold">{prediction.serviceName}</h3>
        </div>
        <span className="font-mono text-2xl font-bold" style={{ color: riskColor }}>
          {Math.round(prediction.riskScore)}%
        </span>
      </div>

      <div className="mt-2 h-2 w-full overflow-hidden rounded-full bg-void">
        <div className="h-full rounded-full" style={{ width: `${Math.min(100, prediction.riskScore)}%`, backgroundColor: riskColor }} />
      </div>

      <p className="mt-3 text-sm text-ink">{prediction.predictedFailure}</p>
      {prediction.reasoning && <p className="mt-2 text-xs text-ink-muted">{prediction.reasoning}</p>}

      <div className="mt-3 flex gap-4 font-mono text-[11px] text-ink-muted">
        <span>{prediction.incidentCountLast30Days} incidents / 30d</span>
        <span>{Math.round(prediction.criticalRatioLast30Days * 100)}% critical ratio</span>
        <span>{new Date(prediction.createdAt).toLocaleString()}</span>
      </div>
    </div>
  )
}
