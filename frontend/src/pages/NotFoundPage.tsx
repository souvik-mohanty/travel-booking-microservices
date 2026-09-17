import { Link } from 'react-router-dom'

export function NotFoundPage() {
  return (
    <div className="flex min-h-screen flex-col items-center justify-center gap-4 bg-slate-50 dark:bg-slate-950">
      <p className="text-sm text-slate-500 dark:text-slate-400">Page not found.</p>
      <Link to="/" className="text-sm font-medium text-slate-900 underline dark:text-slate-100">
        Go home
      </Link>
    </div>
  )
}
