import { useState } from 'react'
import { zodResolver } from '@hookform/resolvers/zod'
import { isAxiosError } from 'axios'
import { useForm } from 'react-hook-form'
import { Link, useNavigate } from 'react-router-dom'
import { z } from 'zod'
import { register as registerApi, startGoogleLogin } from '@/features/auth/api'
import { AuthLayout, FormError } from '@/components/layout/AuthLayout'
import { Button } from '@/components/ui/button'
import { Form, FormControl, FormField, FormItem, FormLabel, FormMessage } from '@/components/ui/form'
import { Input } from '@/components/ui/input'
import { useAuthStore } from '@/store/authStore'
import type { SelfRegisterableRole } from '@/types/auth'
import { cn } from 'cn'

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

const registerSchema = z.object({
  firstName: z.string().min(1, 'First name is required').max(100),
  lastName: z.string().max(100).optional(),
  email: z.string().min(1, 'Email is required').email('Enter a valid email address'),
  password: z.string().min(8, 'Password needs at least 8 characters').max(100),
})

type RegisterValues = z.infer<typeof registerSchema>

export function RegisterPage() {
  const setSession = useAuthStore((state) => state.setSession)
  const navigate = useNavigate()
  const [role, setRole] = useState<SelfRegisterableRole>('TOURIST')
  const [error, setError] = useState<string | null>(null)
  const [submitting, setSubmitting] = useState(false)

  const form = useForm<RegisterValues>({
    resolver: zodResolver(registerSchema),
    defaultValues: { firstName: '', lastName: '', email: '', password: '' },
  })

  async function onSubmit(values: RegisterValues) {
    setError(null)
    setSubmitting(true)
    try {
      const auth = await registerApi({ ...values, role })
      setSession(auth)
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
            className={cn(
              'rounded-md px-3 py-2 text-sm font-medium transition-all duration-200',
              role === option
                ? 'bg-white text-blue-700 shadow-sm dark:bg-slate-950 dark:text-blue-400'
                : 'text-slate-500 hover:text-slate-900 dark:text-slate-400 dark:hover:text-slate-100',
            )}
          >
            {option === 'TOURIST' ? 'Tourist' : 'Business'}
          </button>
        ))}
      </div>
      <p className="mb-5 text-sm text-slate-500 dark:text-slate-400">{ROLE_COPY[role].blurb}</p>

      <FormError message={error} />
      <Form {...form}>
        <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-4">
          <FormField
            control={form.control}
            name="firstName"
            render={({ field }) => (
              <FormItem>
                <FormLabel>First name</FormLabel>
                <FormControl>
                  <Input {...field} />
                </FormControl>
                <FormMessage />
              </FormItem>
            )}
          />
          <FormField
            control={form.control}
            name="lastName"
            render={({ field }) => (
              <FormItem>
                <FormLabel>Last name</FormLabel>
                <FormControl>
                  <Input {...field} />
                </FormControl>
                <FormMessage />
              </FormItem>
            )}
          />
          <FormField
            control={form.control}
            name="email"
            render={({ field }) => (
              <FormItem>
                <FormLabel>Email</FormLabel>
                <FormControl>
                  <Input type="email" autoComplete="email" {...field} />
                </FormControl>
                <FormMessage />
              </FormItem>
            )}
          />
          <FormField
            control={form.control}
            name="password"
            render={({ field }) => (
              <FormItem>
                <FormLabel>Password</FormLabel>
                <FormControl>
                  <Input type="password" autoComplete="new-password" {...field} />
                </FormControl>
                <FormMessage />
              </FormItem>
            )}
          />
          <Button
            type="submit"
            disabled={submitting}
            className="w-full transition-all duration-200 hover:scale-[1.02] active:scale-95"
          >
            {submitting ? 'Creating account…' : ROLE_COPY[role].cta}
          </Button>
        </form>
      </Form>

      {role === 'TOURIST' ? (
        <>
          <div className="my-4 flex items-center gap-3 text-xs text-slate-400">
            <div className="h-px flex-1 bg-slate-200 dark:bg-slate-800" />
            or
            <div className="h-px flex-1 bg-slate-200 dark:bg-slate-800" />
          </div>
          <Button
            type="button"
            variant="outline"
            onClick={startGoogleLogin}
            className="w-full transition-all duration-200 hover:border-blue-300 hover:bg-blue-50 hover:text-blue-700"
          >
            Continue with Google
          </Button>
        </>
      ) : (
        <p className="mt-4 text-center text-xs text-slate-400 dark:text-slate-500">
          Google sign-in always creates a tourist account for now — use email and password for a business account.
        </p>
      )}

      <p className="mt-6 text-center text-sm text-slate-500 dark:text-slate-400">
        Already have an account?{' '}
        <Link
          to="/login"
          className="font-medium text-blue-600 underline decoration-blue-300 underline-offset-2 transition-colors duration-150 hover:text-blue-700 dark:text-blue-400"
        >
          Sign in
        </Link>
      </p>
    </AuthLayout>
  )
}
