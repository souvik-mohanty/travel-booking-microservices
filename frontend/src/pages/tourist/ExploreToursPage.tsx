import { useMemo, useState, type FormEvent } from 'react'
import { useSearchParams } from 'react-router-dom'
import { Link } from 'react-router-dom'
import { AppLayout } from '@/components/layout/AppLayout'
import { TourCard } from '@/components/cards/TourCard'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select'
import { Skeleton } from '@/components/ui/skeleton'
import { useTourSearchQuery } from '@/features/search/api'
import { useAuthStore } from '@/store/authStore'

type SortOrder = 'recommended' | 'price-asc' | 'price-desc'

export function ExploreToursPage() {
  const user = useAuthStore((state) => state.user)
  const [searchParams] = useSearchParams()
  const [q, setQ] = useState('')
  const [destination, setDestination] = useState(searchParams.get('destination') ?? '')
  const [minPrice, setMinPrice] = useState('')
  const [maxPrice, setMaxPrice] = useState('')
  const [sort, setSort] = useState<SortOrder>('recommended')
  const [submittedFilters, setSubmittedFilters] = useState({
    q: '',
    destination: searchParams.get('destination') ?? '',
    minPrice: undefined as number | undefined,
    maxPrice: undefined as number | undefined,
  })

  const { data, isLoading, isError } = useTourSearchQuery({ ...submittedFilters, size: 30 })

  const sortedResults = useMemo(() => {
    const results = data?.results ?? []
    if (sort === 'price-asc') return [...results].sort((a, b) => a.price - b.price)
    if (sort === 'price-desc') return [...results].sort((a, b) => b.price - a.price)
    return results
  }, [data, sort])

  function handleSubmit(event: FormEvent) {
    event.preventDefault()
    setSubmittedFilters({
      q,
      destination,
      minPrice: minPrice ? Number(minPrice) : undefined,
      maxPrice: maxPrice ? Number(maxPrice) : undefined,
    })
  }

  return (
    <AppLayout>
      <div className="mb-6 flex items-center justify-between">
        <h1 className="text-2xl font-semibold text-slate-900 dark:text-slate-100">Explore Tours</h1>
        {user?.role === 'BUSINESS' && (
          <Button asChild className="transition-all duration-200 hover:scale-[1.02] active:scale-95">
            <Link to="/tours/new">Create a tour</Link>
          </Button>
        )}
      </div>

      <form
        onSubmit={handleSubmit}
        className="mb-8 grid grid-cols-1 gap-3 rounded-lg border border-slate-200 bg-white p-4 sm:grid-cols-5 dark:border-slate-800 dark:bg-slate-900"
      >
        <Input placeholder="Search tours…" value={q} onChange={(e) => setQ(e.target.value)} className="sm:col-span-2" />
        <Input placeholder="Destination" value={destination} onChange={(e) => setDestination(e.target.value)} />
        <Input placeholder="Min price" type="number" min={0} value={minPrice} onChange={(e) => setMinPrice(e.target.value)} />
        <Input placeholder="Max price" type="number" min={0} value={maxPrice} onChange={(e) => setMaxPrice(e.target.value)} />
        <Button type="submit" className="sm:col-span-5 transition-all duration-200 hover:scale-[1.02] active:scale-95">
          Search
        </Button>
      </form>

      <div className="mb-4 flex items-center justify-between">
        <p className="text-sm text-slate-500 dark:text-slate-400">
          {isLoading ? 'Searching…' : `${data?.totalResults ?? 0} tour(s) found`}
        </p>
        <Select value={sort} onValueChange={(v) => setSort(v as SortOrder)}>
          <SelectTrigger className="w-44">
            <SelectValue />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="recommended">Recommended</SelectItem>
            <SelectItem value="price-asc">Price: low to high</SelectItem>
            <SelectItem value="price-desc">Price: high to low</SelectItem>
          </SelectContent>
        </Select>
      </div>

      {isLoading && (
        <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {Array.from({ length: 6 }).map((_, i) => (
            <Skeleton key={i} className="h-48 rounded-lg" />
          ))}
        </div>
      )}

      {isError && (
        <p className="rounded-lg border border-dashed border-red-300 p-8 text-center text-sm text-red-600 dark:border-red-800 dark:text-red-400">
          Unable to load tours. Try again in a moment.
        </p>
      )}

      {!isLoading && !isError && sortedResults.length === 0 && (
        <p className="rounded-lg border border-dashed border-slate-300 p-8 text-center text-sm text-slate-500 dark:border-slate-700 dark:text-slate-400">
          No published tours match your search yet. Try clearing the filters
          {user?.role === 'BUSINESS' && (
            <>
              , or{' '}
              <Link to="/tours/new" className="text-blue-600 underline dark:text-blue-400">
                create one
              </Link>
            </>
          )}
          .
        </p>
      )}

      {!isLoading && !isError && sortedResults.length > 0 && (
        <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {sortedResults.map((tour, i) => (
            <TourCard key={tour.id} tour={tour} index={i} />
          ))}
        </div>
      )}
    </AppLayout>
  )
}
