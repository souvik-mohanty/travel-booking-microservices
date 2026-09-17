import { useState, type FormEvent } from 'react'
import { Bell, CalendarCheck, CreditCard, MapPin, Search, SlidersHorizontal } from 'lucide-react'
import { Link, useNavigate } from 'react-router-dom'
import { PublicLayout } from '@/components/layout/PublicLayout'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'

const BENEFITS = [
  {
    icon: Search,
    title: 'Flexible tour discovery',
    body: 'Search by destination, price, or dates and find a tour that fits your plans.',
  },
  {
    icon: CalendarCheck,
    title: 'Easy booking',
    body: 'Pick your travel dates and traveler count, and book in a few steps.',
  },
  {
    icon: CreditCard,
    title: 'Secure payments',
    body: 'Checkout is handled by Razorpay — your card details never touch our servers.',
  },
  {
    icon: Bell,
    title: 'Booking updates',
    body: 'Get notified as your booking is confirmed, and track it from your dashboard.',
  },
]

// Tour search (search-service) requires a signed-in caller -- there is no
// public/anonymous tour browsing in this backend today. This page therefore
// stays marketing-only (no live tour data) for anonymous visitors; the real
// "Featured Tours" grid lives on the post-login Explore Tours page.
export function HomePage() {
  const navigate = useNavigate()
  const [destination, setDestination] = useState('')

  function handleSearch(event: FormEvent) {
    event.preventDefault()
    navigate('/login')
  }

  return (
    <PublicLayout>
      {/* Hero */}
      <section className="relative overflow-hidden bg-gradient-to-br from-blue-50 via-white to-green-50 dark:from-slate-950 dark:via-slate-950 dark:to-slate-950">
        <div className="tf-gradient-bar absolute inset-x-0 top-0 h-0.5" />
        <div className="mx-auto max-w-5xl px-6 py-20 text-center">
          <h1 className="animate-fade-in-up text-4xl font-bold tracking-tight text-slate-900 sm:text-5xl dark:text-slate-100">
            Discover Your Next <span className="tf-gradient-text">Adventure</span>
          </h1>
          <p className="animate-fade-in-up mx-auto mt-4 max-w-2xl text-lg text-slate-600 dark:text-slate-400">
            Explore unforgettable destinations and book your perfect journey with TourFlow.
          </p>

          <div className="animate-fade-in-up mt-8 flex flex-wrap justify-center gap-3">
            <Button asChild size="lg" className="transition-all duration-200 hover:scale-[1.02] active:scale-95">
              <Link to="/tours">Explore Tours</Link>
            </Button>
            <Button
              asChild
              size="lg"
              variant="outline"
              className="transition-all duration-200 hover:border-blue-300 hover:bg-blue-50 hover:text-blue-700"
            >
              <Link to="/destinations">Explore Destinations</Link>
            </Button>
          </div>

          <form
            onSubmit={handleSearch}
            className="animate-scale-in mx-auto mt-10 flex max-w-xl flex-col gap-2 rounded-xl border border-slate-200 bg-white p-2 shadow-lg shadow-slate-200/50 sm:flex-row dark:border-slate-800 dark:bg-slate-900 dark:shadow-none"
          >
            <div className="flex flex-1 items-center gap-2 px-2">
              <MapPin className="size-4 shrink-0 text-slate-400" />
              <Input
                placeholder="Where do you want to go?"
                value={destination}
                onChange={(e) => setDestination(e.target.value)}
                className="border-none shadow-none focus-visible:ring-0"
              />
            </div>
            <Button type="submit" className="gap-2 transition-all duration-200 hover:scale-[1.02] active:scale-95">
              <Search className="size-4" />
              Search
            </Button>
          </form>
          <p className="mt-3 text-xs text-slate-400">Sign in or create an account to search and book tours.</p>
        </div>
      </section>

      {/* Why TourFlow */}
      <section className="py-16">
        <div className="mx-auto max-w-6xl px-6">
          <h2 className="mb-8 text-center text-2xl font-semibold text-slate-900 dark:text-slate-100">Why TourFlow</h2>
          <div className="grid grid-cols-1 gap-6 sm:grid-cols-2 lg:grid-cols-4">
            {BENEFITS.map(({ icon: Icon, title, body }) => (
              <div
                key={title}
                className="rounded-lg border border-slate-200 bg-white p-5 dark:border-slate-800 dark:bg-slate-900"
              >
                <div className="mb-3 inline-flex size-10 items-center justify-center rounded-lg bg-blue-50 text-blue-600 dark:bg-blue-950 dark:text-blue-400">
                  <Icon className="size-5" />
                </div>
                <h3 className="font-medium text-slate-900 dark:text-slate-100">{title}</h3>
                <p className="mt-1 text-sm text-slate-500 dark:text-slate-400">{body}</p>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* CTA */}
      <section className="bg-slate-50 py-16 dark:bg-slate-900/50">
        <div className="mx-auto max-w-4xl px-6 text-center">
          <h2 className="text-2xl font-semibold text-slate-900 dark:text-slate-100">Ready for your next adventure?</h2>
          <p className="mt-2 text-slate-500 dark:text-slate-400">Find a tour that matches your travel plans.</p>
          <Button asChild size="lg" className="mt-6 transition-all duration-200 hover:scale-[1.02] active:scale-95">
            <Link to="/register">
              <SlidersHorizontal className="size-4" />
              Create a free account
            </Link>
          </Button>
        </div>
      </section>
    </PublicLayout>
  )
}
