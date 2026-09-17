import type { ReactNode } from 'react'

export function AuthLayout({ title, children }: { title: string; children: ReactNode }) {
  return (
    <div className="flex min-h-screen items-center justify-center bg-gradient-to-br from-blue-50 via-white to-green-50 px-4 dark:from-slate-950 dark:via-slate-950 dark:to-slate-950">
      <div className="animate-scale-in w-full max-w-sm overflow-hidden rounded-xl border border-slate-200 bg-white shadow-lg shadow-slate-200/50 dark:border-slate-800 dark:bg-slate-900 dark:shadow-none">
        <div className="tf-gradient-bar h-1" />
        <div className="p-8">
          <h1 className="mb-6 text-xl font-semibold text-slate-900 dark:text-slate-100">{title}</h1>
          {children}
        </div>
      </div>
    </div>
  )
}

export function FormError({ message }: { message: string | null }) {
  if (!message) return null
  return (
    <p className="mb-4 rounded-md bg-red-50 px-3 py-2 text-sm text-red-700 dark:bg-red-950 dark:text-red-300">
      {message}
    </p>
  )
}
