import { Link, useNavigate, useLocation } from 'react-router-dom'
import { LayoutDashboard, Siren, BookOpenText, TrendingUp, ShieldAlert, LogOut, Radio } from 'lucide-react'
import { useAuth } from '../context/AuthContext'

function NavLink({ to, children, icon: Icon }) {
  const location = useLocation()
  const active = location.pathname.startsWith(to)
  return (
    <Link
      to={to}
      className={`flex items-center gap-1.5 rounded-md px-3 py-2 text-sm font-medium transition-colors ${
        active ? 'bg-brand/15 text-brand' : 'text-ink-muted hover:text-ink'
      }`}
    >
      <Icon size={15} strokeWidth={2.25} />
      {children}
    </Link>
  )
}

export default function Navbar() {
  const { user, logout } = useAuth()
  const navigate = useNavigate()

  const handleLogout = () => {
    logout()
    navigate('/login')
  }

  return (
    <header className="sticky top-0 z-40 border-b border-border bg-panel/95 backdrop-blur">
      <div className="mx-auto flex max-w-7xl items-center justify-between px-4 py-3">
        <Link to="/" className="flex items-center gap-2">
          <Radio size={20} className="text-brand" />
          <span className="font-display text-xl font-bold tracking-tight">
            RescueOps<span className="text-brand">AI</span>
          </span>
        </Link>

        {user && (
          <nav className="flex items-center gap-1">
            <NavLink to="/dashboard" icon={LayoutDashboard}>Dashboard</NavLink>
            <NavLink to="/incidents" icon={Siren}>Incidents</NavLink>
            <NavLink to="/knowledge" icon={BookOpenText}>Knowledge Base</NavLink>
            <NavLink to="/predictions" icon={TrendingUp}>Predictions</NavLink>
            <NavLink to="/security" icon={ShieldAlert}>Security</NavLink>

            <div className="ml-3 flex items-center gap-3 border-l border-border pl-3">
              <div className="text-right leading-tight">
                <p className="text-sm font-semibold">{user.name}</p>
                <p className="font-mono text-[10px] uppercase tracking-wide text-ink-muted">{user.role}</p>
              </div>
              <button
                onClick={handleLogout}
                className="rounded-md p-2 text-ink-muted transition-colors hover:bg-white/5 hover:text-critical"
                aria-label="Log out"
              >
                <LogOut size={17} />
              </button>
            </div>
          </nav>
        )}

        {!user && (
          <Link
            to="/login"
            className="rounded-md bg-brand px-4 py-2 text-sm font-semibold text-void transition-transform hover:scale-105"
          >
            Sign in
          </Link>
        )}
      </div>
    </header>
  )
}
