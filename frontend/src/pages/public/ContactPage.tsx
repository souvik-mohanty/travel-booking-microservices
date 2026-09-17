import { Link } from 'react-router-dom'
import { PublicLayout } from '@/components/layout/PublicLayout'

export function ContactPage() {
  return (
    <PublicLayout>
      <section className="mx-auto max-w-3xl px-6 py-16">
        <h1 className="text-3xl font-semibold text-slate-900 dark:text-slate-100">Contact</h1>
        <p className="mt-4 text-slate-600 dark:text-slate-400">
          Already have an account? The fastest way to reach us is to{' '}
          <Link to="/login" className="font-medium text-blue-600 underline dark:text-blue-400">
            sign in
          </Link>{' '}
          and raise a support ticket from your dashboard — it's tied to your account so we can look up your
          bookings directly.
        </p>
        <p className="mt-4 text-slate-600 dark:text-slate-400">
          For anything else, reach us at{' '}
          <a href="mailto:support@tourflow.example" className="font-medium text-blue-600 underline dark:text-blue-400">
            support@tourflow.example
          </a>
          .
        </p>
      </section>
    </PublicLayout>
  )
}
