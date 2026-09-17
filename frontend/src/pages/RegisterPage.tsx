import { useState, type FormEvent } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { isAxiosError } from 'axios'
import { startGoogleLogin } from '../api/auth'
import { AuthLayout, FormError, TextField } from '../components/AuthLayout'
import { useAuth } from '../context/AuthContext'
import type { SelfRegisterableRole } from '../types/auth'

const ROLE_COPY: Record<SelfRegisterableRole, { cta: string; blurb: string; home: string }> = {
  TOURIST: {
    cta: 'Create tourist account',
    blurb: "You'll be able to browse and book tours.",
    home: '/tours',
  },
  BUSINESS: {
    cta: 'Create business account',
    blurb: "You'll be able to create, publish, and manage your own tours.",
    home: '/business',
  },
}

export function RegisterPage() {
  const { register } = useAuth()
  const navigate = useNavigate()
  const [role, setRole] = useState<SelfRegisterableRole>('TOURIST')
  const [form, setForm] = useState({ email: '', password: '', firstName: '', lastName: '' })
  const [error, setError] = useState<string | null>(null)
  const [submitting, setSubmitting] = useState(false)

  function update<K extends keyof typeof form>(key: K, value: string) {
    setForm((prev) => ({ ...prev, [key]: value }))
  }

  async function handleSubmit(event: FormEvent) {
    event.preventDefault()
    setError(null)
    setSubmitting(true)
    try {
      await register({ ...form, role })
      navigate(ROLE_COPY[role].home, { replace: true })
    } catch (err) {
      if (isAxiosError(err) && err.response?.status === 409) {
        setError('An account with that email already exists.')
      } else if (isAxiosError(err) && err.response?.status === 400) {
        setError('Please check your details: password needs at least 8 characters.')
      } else {
        setError('Registration failed. Please try again.')
      }
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <AuthLayout title="Create your TourFlow account">
      <div className="mb-5 grid grid-cols-2 gap-2 rounded-lg bg-slate-100 p-1 dark:bg-slate-800">
        {(['TOURIST', 'BUSINESS'] as const).map((option) => (
          <button
            key={option}
            type="button"
            onClick={() => setRole(option)}
            className={
              'rounded-md px-3 py-2 text-sm font-medium transition-all duration-200 ' +
              (role === option
                ? 'bg-white text-blue-700 shadow-sm dark:bg-slate-950 dark:text-blue-400'
                : 'text-slate-500 hover:text-slate-900 dark:text-slate-400 dark:hover:text-slate-100')
            }
          >
            {option === 'TOURIST' ? 'Tourist' : 'Business'}
          </button>
        ))}
      </div>
      <p className="mb-5 text-sm text-slate-500 dark:text-slate-400">{ROLE_COPY[role].blurb}</p>

      <FormError message={error} />
      <form onSubmit={handleSubmit}>
        <TextField
          label="First name"
          required
          value={form.firstName}
          onChange={(e) => update('firstName', e.target.value)}
        />
        <TextField label="Last name" value={form.lastName} onChange={(e) => update('lastName', e.target.value)} />
        <TextField
          label="Email"
          type="email"
          autoComplete="email"
          required
          value={form.email}
          onChange={(e) => update('email', e.target.value)}
        />
        <TextField
          label="Password"
          type="password"
          autoComplete="new-password"
          minLength={8}
          required
          value={form.password}
          onChange={(e) => update('password', e.target.value)}
        />
        <button
          type="submit"
          disabled={submitting}
          className="mt-2 w-full rounded-md bg-blue-600 px-3 py-2 text-sm font-medium text-white transition-all duration-200 hover:bg-blue-700 hover:scale-[1.02] active:scale-95 disabled:opacity-50 dark:bg-blue-500 dark:text-white"
        >
          {submitting ? 'Creating account…' : ROLE_COPY[role].cta}
        </button>
      </form>

      {role === 'TOURIST' ? (
        <>
          <div className="my-4 flex items-center gap-3 text-xs text-slate-400">
            <div className="h-px flex-1 bg-slate-200 dark:bg-slate-800" />
            or
            <div className="h-px flex-1 bg-slate-200 dark:bg-slate-800" />
          </div>
          <button
            type="button"
            onClick={startGoogleLogin}
            className="w-full rounded-md border border-slate-300 px-3 py-2 text-sm font-medium text-slate-700 transition-all duration-200 hover:border-blue-300 hover:bg-blue-50 hover:text-blue-700 dark:border-slate-700 dark:text-slate-200 dark:hover:border-blue-600 dark:hover:bg-slate-800 dark:hover:text-blue-300"
          >
            Continue with Google
          </button>
        </>
      ) : (
        <p className="mt-4 text-center text-xs text-slate-400 dark:text-slate-500">
          Google sign-in always creates a tourist account for now — use email and password for a business account.
        </p>
      )}

      <p className="mt-6 text-center text-sm text-slate-500 dark:text-slate-400">
        Already have an account?{' '}
        <Link to="/login" className="font-medium text-blue-600 underline decoration-blue-300 underline-offset-2 transition-colors duration-150 hover:text-blue-700 dark:text-blue-400">
          Sign in
        </Link>
      </p>
    </AuthLayout>
  )
}
