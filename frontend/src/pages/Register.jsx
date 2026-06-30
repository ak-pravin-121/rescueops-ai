import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { Radio, Wrench, ShieldCheck } from 'lucide-react'
import { useAuth } from '../context/AuthContext'

export default function Register() {
  const { register } = useAuth()
  const navigate = useNavigate()
  const [form, setForm] = useState({ name: '', email: '', password: '', role: 'ENGINEER' })
  const [error, setError] = useState('')
  const [submitting, setSubmitting] = useState(false)

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError('')
    setSubmitting(true)
    try {
      await register(form)
      navigate('/dashboard')
    } catch (err) {
      setError(err.response?.data?.error || 'Could not create your account. Please check your details.')
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div className="flex min-h-[85vh] items-center justify-center px-4 py-10">
      <div className="w-full max-w-md rounded-xl border border-border bg-panel p-8 shadow-2xl">
        <div className="mb-6 flex items-center gap-2 text-brand">
          <Radio size={22} />
          <span className="font-mono text-xs uppercase tracking-widest text-ink-muted">New Operator</span>
        </div>
        <h1 className="font-display text-3xl font-bold">Join RescueOps AI</h1>
        <p className="mt-1 text-sm text-ink-muted">Set up your account to start triaging incidents.</p>

        <form onSubmit={handleSubmit} className="mt-6 space-y-4">
          <div>
            <label className="mb-1 block text-xs font-semibold uppercase tracking-wide text-ink-muted">Full name</label>
            <input
              required
              value={form.name}
              onChange={(e) => setForm({ ...form, name: e.target.value })}
              className="w-full rounded-md border border-border bg-void px-3 py-2 text-ink outline-none focus:border-brand"
              placeholder="Jordan Lee"
            />
          </div>

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
              minLength={6}
              value={form.password}
              onChange={(e) => setForm({ ...form, password: e.target.value })}
              className="w-full rounded-md border border-border bg-void px-3 py-2 text-ink outline-none focus:border-brand"
              placeholder="At least 6 characters"
            />
          </div>

          <div>
            <label className="mb-2 block text-xs font-semibold uppercase tracking-wide text-ink-muted">Role</label>
            <div className="grid grid-cols-2 gap-3">
              <RoleOption
                icon={Wrench}
                label="Engineer"
                description="Triage & resolve incidents"
                selected={form.role === 'ENGINEER'}
                onClick={() => setForm({ ...form, role: 'ENGINEER' })}
              />
              <RoleOption
                icon={ShieldCheck}
                label="Admin"
                description="Full platform access"
                selected={form.role === 'ADMIN'}
                onClick={() => setForm({ ...form, role: 'ADMIN' })}
              />
            </div>
          </div>

          {error && <p className="rounded-md bg-critical/10 px-3 py-2 text-sm text-critical">{error}</p>}

          <button
            type="submit"
            disabled={submitting}
            className="w-full rounded-md bg-brand py-2.5 font-semibold text-void transition-opacity hover:opacity-90 disabled:opacity-60"
          >
            {submitting ? 'Creating account…' : 'Create account'}
          </button>
        </form>

        <p className="mt-6 text-center text-sm text-ink-muted">
          Already registered?{' '}
          <Link to="/login" className="font-semibold text-brand hover:underline">
            Sign in
          </Link>
        </p>
      </div>
    </div>
  )
}

function RoleOption({ icon: Icon, label, description, selected, onClick }) {
  return (
    <button
      type="button"
      onClick={onClick}
      className={`flex flex-col items-start gap-1 rounded-md border px-3 py-3 text-left transition-colors ${
        selected ? 'border-brand bg-brand/10' : 'border-border hover:border-ink-muted'
      }`}
    >
      <Icon size={18} className={selected ? 'text-brand' : 'text-ink-muted'} />
      <span className="text-sm font-semibold">{label}</span>
      <span className="text-xs text-ink-muted">{description}</span>
    </button>
  )
}
