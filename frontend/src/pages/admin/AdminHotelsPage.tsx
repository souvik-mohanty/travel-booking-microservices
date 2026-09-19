import { ModerationTable, type ModerationRow } from '@/components/admin/ModerationTable'
import { AppLayout } from '@/components/layout/AppLayout'
import { useAdminUsersQuery } from '@/features/admin/api'
import { useAllHotelsQuery, useReinstateHotelMutation, useSuspendHotelMutation } from '@/features/hotels/api'
import { ownerLabel } from '@/lib/owners'

export function AdminHotelsPage() {
  const hotels = useAllHotelsQuery()
  const users = useAdminUsersQuery()
  const suspend = useSuspendHotelMutation()
  const reinstate = useReinstateHotelMutation()

  const rows: ModerationRow[] = (hotels.data ?? []).map((hotel) => ({
    id: hotel.id,
    name: hotel.name,
    location: [hotel.city, hotel.state, hotel.country].filter(Boolean).join(', '),
    owner: ownerLabel(hotel.ownerId, users.data),
    status: hotel.status,
  }))

  return (
    <AppLayout>
      <h1 className="mb-1 text-2xl font-semibold text-slate-900 dark:text-slate-100">Hotels</h1>
      <p className="mb-6 text-sm text-slate-500 dark:text-slate-400">Review and moderate every hotel on the platform.</p>
      <ModerationTable
        entity="hotel"
        rows={rows}
        isLoading={hotels.isLoading}
        isError={hotels.isError}
        onRetry={() => hotels.refetch()}
        onSuspend={(id) => suspend.mutateAsync(id)}
        onReinstate={(id) => reinstate.mutateAsync(id)}
      />
    </AppLayout>
  )
}
