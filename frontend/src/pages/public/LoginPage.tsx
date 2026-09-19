import { useState } from 'react'
import { zodResolver } from '@hookform/resolvers/zod'
import { isAxiosError } from 'axios'
import { useForm } from 'react-hook-form'
import { Link, useLocation, useNavigate } from 'react-router-dom'
import { z } from 'zod'
import { login } from '@/features/auth/api'
import { useGoogleLogin } from '@/features/auth/useGoogleLogin'
import { LoadingOverlay } from '@/components/common/LoadingOverlay'
import { AuthLayout, FormError } from '@/components/layout/AuthLayout'
import { Button } from '@/components/ui/button'
import { Form, FormControl, FormField, FormItem, FormLabel, FormMessage } from '@/components/ui/form'
import { Input } from '@/components/ui/input'
import { homePathForRole } from '@/lib/roles'
import { useAuthStore } from '@/store/authStore'

const loginSchema = z.object({
  email: z.string().min(1, 'Email is required').email('Enter a valid email address'),
  password: z.string().min(1, 'Password is required'),
})

type LoginValues = z.infer<typeof loginSchema>

export function LoginPage() {
  const setSession = useAuthStore((state) => state.setSession)
  const navigate = useNavigate()
  const location = useLocation()
  const [error, setError] = useState<string | null>(null)
  const [submitting, setSubmitting] = useState(false)
  const google = useGoogleLogin()

  const form = useForm<LoginValues>({
    resolver: zodResolver(loginSchema),
    defaultValues: { email: '', password: '' },
  })

  const from = (location.state as { from?: { pathname: string } })?.from?.pathname

  async function onSubmit(values: LoginValues) {
    setError(null)
    setSubmitting(true)
    try {
      const auth = await login(values)
      const user = setSession(auth)
      navigate(from ?? homePathForRole(user?.role), { replace: true })
    } catch (err) {
      const status = isAxiosError(err) ? err.response?.status : undefined
      setError(
        status === 401
          ? 'Invalid email or password.'
          : status === 403
            ? 'This account has been disabled. Please contact support.'
            : 'Login failed. Please try again.',
      )
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <AuthLayout title="Sign in to TourFlow">
      <LoadingOverlay
        open={submitting || google.redirecting}
        title={google.redirecting ? 'Redirecting to Google…' : 'Signing you in…'}
        message={google.redirecting ? 'Taking you to Google to finish signing in.' : 'Checking your details.'}
      />
      <FormError message={error} />
      <Form {...form}>
        <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-4">
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
                  <Input type="password" autoComplete="current-password" {...field} />
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
            {submitting ? 'Signing in…' : 'Sign in'}
          </Button>
        </form>
      </Form>

      <div className="my-4 flex items-center gap-3 text-xs text-slate-400">
        <div className="h-px flex-1 bg-slate-200 dark:bg-slate-800" />
        or
        <div className="h-px flex-1 bg-slate-200 dark:bg-slate-800" />
      </div>

      <Button
        type="button"
        variant="outline"
        onClick={google.start}
        disabled={submitting || google.redirecting}
        className="w-full transition-all duration-200 hover:border-blue-300 hover:bg-blue-50 hover:text-blue-700"
      >
        Continue with Google
      </Button>

      <p className="mt-6 text-center text-sm text-slate-500 dark:text-slate-400">
        Don&apos;t have an account?{' '}
        <Link
          to="/register"
          className="font-medium text-blue-600 underline decoration-blue-300 underline-offset-2 transition-colors duration-150 hover:text-blue-700 dark:text-blue-400"
        >
          Register
        </Link>
      </p>
    </AuthLayout>
  )
}
