import { PublicLayout } from '@/components/layout/PublicLayout'

export function AboutPage() {
  return (
    <PublicLayout>
      <section className="mx-auto max-w-3xl px-6 py-16">
        <h1 className="text-3xl font-semibold text-slate-900 dark:text-slate-100">About TourFlow</h1>
        <p className="mt-4 text-slate-600 dark:text-slate-400">
          TourFlow connects tourists with tour operators. Businesses publish tours — complete with itineraries,
          hotel stays, and activities — and travelers search, book, and pay for them directly on the platform.
        </p>
        <p className="mt-4 text-slate-600 dark:text-slate-400">
          Every booking runs through a real payment flow, and a tour's itinerary can include hotel reservations
          and bookable activities managed by the same business that created it.
        </p>
      </section>
    </PublicLayout>
  )
}
