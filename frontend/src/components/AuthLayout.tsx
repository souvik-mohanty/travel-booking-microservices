import type { InputHTMLAttributes, ReactNode } from 'react'

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

export function TextField({
  label,
  ...props
}: { label: string } & InputHTMLAttributes<HTMLInputElement>) {
  return (
    <label className="mb-4 block text-sm font-medium text-slate-700 dark:text-slate-300">
      {label}
      <input
        {...props}
        className="mt-1 w-full rounded-md border border-slate-300 px-3 py-2 text-sm text-slate-900 outline-none transition-all duration-200 focus:border-blue-500 focus:ring-2 focus:ring-blue-100 dark:focus:ring-blue-950 dark:border-slate-700 dark:bg-slate-800 dark:text-slate-100"
      />
    </label>
  )
}
