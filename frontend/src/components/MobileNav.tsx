import { CalendarDays, ClipboardList, Search, Sparkles } from 'lucide-react'
import { NavLink } from 'react-router-dom'

const links = [
  { to: '/my-day', label: 'My Day', icon: Sparkles },
  { to: '/tasks', label: 'Tasks', icon: ClipboardList },
  { to: '/planned', label: 'Planned', icon: CalendarDays },
  { to: '/search', label: 'Search', icon: Search },
]

export function MobileNav() {
  return <nav aria-label="Mobile navigation" className="mobile-bottom-nav">
    {links.map(({ to, label, icon: Icon }) => <NavLink className={({ isActive }) => `mobile-nav-link ${isActive ? 'is-active' : ''}`} key={to} to={to}>
      <Icon size={20} /><span>{label}</span>
    </NavLink>)}
  </nav>
}
