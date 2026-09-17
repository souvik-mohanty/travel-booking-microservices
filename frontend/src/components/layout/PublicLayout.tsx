import type { ReactNode } from 'react'
import { Link } from 'react-router-dom'
import { Button } from '@/components/ui/button'

const NAV_LINKS = [
  { to: '/tours', label: 'Explore Tours' },
  { to: '/destinations', label: 'Destinations' },
  { to: '/about', label: 'About' },
  { to: '/contact', label: 'Contact' },
]

export function PublicLayout({ children }: { children: ReactNode }) {
  return (
    <div className="flex min-h-screen flex-col bg-white dark:bg-slate-950">
      <header className="relative border-b border-slate-200 bg-white dark:border-slate-800 dark:bg-slate-900">
        <div className="tf-gradient-bar absolute inset-x-0 top-0 h-0.5" />
        <div className="mx-auto flex max-w-6xl items-center justify-between gap-6 px-6 py-4">
          <Link
            to="/"
            className="tf-gradient-text text-lg font-bold tracking-tight transition-transform duration-200 hover:scale-[1.03]"
          >
            TourFlow
          </Link>
          <nav className="hidden gap-6 text-sm text-slate-600 md:flex dark:text-slate-400">
            {NAV_LINKS.map((link) => (
              <Link key={link.to} to={link.to} className="transition-colors hover:text-blue-700 dark:hover:text-blue-400">
                {link.label}
              </Link>
            ))}
          </nav>
          <div className="flex items-center gap-2">
            <Button asChild variant="ghost" size="sm">
              <Link to="/login">Sign in</Link>
            </Button>
            <Button asChild size="sm" className="transition-all duration-200 hover:scale-[1.02] active:scale-95">
              <Link to="/register">Register</Link>
            </Button>
          </div>
        </div>
      </header>

      <main className="animate-fade-in flex-1">{children}</main>

      <footer className="border-t border-slate-200 py-8 text-center text-sm text-slate-400 dark:border-slate-800">
        © {new Date().getFullYear()} TourFlow
      </footer>
    </div>
  )
}
