import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { Radio } from 'lucide-react'
import { useAuth } from '../context/AuthContext'

export default function Login() {
  const { login } = useAuth()
  const navigate = useNavigate()
  const [form, setForm] = useState({ email: '', password: '' })
  const [error, setError] = useState('')
  const [submitting, setSubmitting] = useState(false)

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError('')
    setSubmitting(true)
    try {
      await login(form.email, form.password)
      navigate('/dashboard')
    } catch (err) {
      setError(err.response?.data?.error || 'Could not sign in. Check your details and try again.')
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div className="flex min-h-[85vh] items-center justify-center px-4">
      <div className="w-full max-w-md rounded-xl border border-border bg-panel p-8 shadow-2xl">
        <div className="mb-6 flex items-center gap-2 text-brand">
          <Radio size={22} />
          <span className="font-mono text-xs uppercase tracking-widest text-ink-muted">Operator Access</span>
        </div>
        <h1 className="font-display text-3xl font-bold">Sign in to RescueOps</h1>
        <p className="mt-1 text-sm text-ink-muted">Access the incident intelligence platform.</p>

        <form onSubmit={handleSubmit} className="mt-6 space-y-4">
          <div>
            <label className="mb-1 block text-xs font-semibold uppercase tracking-wide text-ink-muted">Email</label>
            <input
              type="email"
              required
              value={form.email}
              onChange={(e) => setForm({ ...form, email: e.target.value })}
              className="w-full rounded-md border border-border bg-void px-3 py-2 text-ink outline-none focus:border-brand"
              placeholder="you@company.com"
            />
          </div>
          <div>
            <label className="mb-1 block text-xs font-semibold uppercase tracking-wide text-ink-muted">Password</label>
            <input
              type="password"
              required
              value={form.password}
              onChange={(e) => setForm({ ...form, password: e.target.value })}
              className="w-full rounded-md border border-border bg-void px-3 py-2 text-ink outline-none focus:border-brand"
              placeholder="••••••••"
            />
          </div>

          {error && <p className="rounded-md bg-critical/10 px-3 py-2 text-sm text-critical">{error}</p>}

          <button
            type="submit"
            disabled={submitting}
            className="w-full rounded-md bg-brand py-2.5 font-semibold text-void transition-opacity hover:opacity-90 disabled:opacity-60"
          >
            {submitting ? 'Signing in…' : 'Sign in'}
          </button>
        </form>

        <p className="mt-6 text-center text-sm text-ink-muted">
          New operator?{' '}
          <Link to="/register" className="font-semibold text-brand hover:underline">
            Create an account
          </Link>
        </p>
      </div>
    </div>
  )
}
