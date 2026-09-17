import { Link } from 'react-router-dom'

export function UnauthorizedPage() {
  return (
    <div className="flex min-h-screen flex-col items-center justify-center gap-4 bg-slate-50 px-4 text-center dark:bg-slate-950">
      <p className="text-lg font-medium text-slate-900 dark:text-slate-100">You don't have permission to access this page.</p>
      <Link
        to="/tours"
        className="text-sm font-medium text-blue-600 underline decoration-blue-300 underline-offset-2 transition-colors duration-150 hover:text-blue-700 dark:text-blue-400"
      >
        Back to Explore Tours
      </Link>
    </div>
  )
}
