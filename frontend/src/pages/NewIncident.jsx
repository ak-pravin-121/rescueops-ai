import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { Siren } from 'lucide-react'
import { incidentsApi } from '../api/endpoints'
import { SEVERITY_ORDER, SEVERITIES } from '../utils/constants'

export default function NewIncident() {
  const navigate = useNavigate()
  const [form, setForm] = useState({ title: '', description: '', serviceName: '', severity: '' })
  const [error, setError] = useState('')
  const [submitting, setSubmitting] = useState(false)

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError('')
    setSubmitting(true)
    try {
      const payload = { ...form, severity: form.severity || null }
      const incident = await incidentsApi.create(payload)
      navigate(`/incidents/${incident.id}`)
    } catch (err) {
      setError(err.response?.data?.error || 'Could not create the incident. Please check the details.')
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div className="mx-auto max-w-2xl px-4 py-8">
      <div className="mb-6 flex items-center gap-2 text-brand">
        <Siren size={20} />
        <span className="font-mono text-xs uppercase tracking-widest text-ink-muted">New Incident</span>
      </div>
      <h1 className="font-display text-3xl font-bold">Report an incident</h1>
      <p className="mt-1 text-sm text-ink-muted">
        Describe what's happening. You can attach logs and run AI agents once it's created.
      </p>

      <form onSubmit={handleSubmit} className="mt-6 space-y-4 rounded-lg border border-border bg-panel p-6">
        <div>
          <label className="mb-1 block text-xs font-semibold uppercase tracking-wide text-ink-muted">Title</label>
          <input
            required
            value={form.title}
            onChange={(e) => setForm({ ...form, title: e.target.value })}
            className="w-full rounded-md border border-border bg-void px-3 py-2 text-ink outline-none focus:border-brand"
            placeholder="payment-service returning 500s for checkout"
          />
        </div>

        <div>
          <label className="mb-1 block text-xs font-semibold uppercase tracking-wide text-ink-muted">Service / component</label>
          <input
            required
            value={form.serviceName}
            onChange={(e) => setForm({ ...form, serviceName: e.target.value })}
            className="w-full rounded-md border border-border bg-void px-3 py-2 text-ink outline-none focus:border-brand"
            placeholder="payment-service"
          />
        </div>

        <div>
          <label className="mb-1 block text-xs font-semibold uppercase tracking-wide text-ink-muted">Description</label>
          <textarea
            required
            rows={5}
            value={form.description}
            onChange={(e) => setForm({ ...form, description: e.target.value })}
            className="w-full resize-none rounded-md border border-border bg-void px-3 py-2 text-ink outline-none focus:border-brand"
            placeholder="What happened? When did it start? What's the user/business impact?"
          />
        </div>

        <div>
          <label className="mb-1 block text-xs font-semibold uppercase tracking-wide text-ink-muted">
            Severity <span className="text-ink-muted/70">(optional - the Analyzer Agent can set this for you)</span>
          </label>
          <select
            value={form.severity}
            onChange={(e) => setForm({ ...form, severity: e.target.value })}
            className="w-full rounded-md border border-border bg-void px-3 py-2 text-ink outline-none focus:border-brand"
          >
            <option value="">Let AI decide (defaults to Medium)</option>
            {SEVERITY_ORDER.map((s) => (
              <option key={s} value={s}>{SEVERITIES[s].label}</option>
            ))}
          </select>
        </div>

        {error && <p className="rounded-md bg-critical/10 px-3 py-2 text-sm text-critical">{error}</p>}

        <button
          type="submit"
          disabled={submitting}
          className="w-full rounded-md bg-brand py-2.5 font-semibold text-void transition-opacity hover:opacity-90 disabled:opacity-60"
        >
          {submitting ? 'Creating…' : 'Create incident'}
        </button>
      </form>
    </div>
  )
}
