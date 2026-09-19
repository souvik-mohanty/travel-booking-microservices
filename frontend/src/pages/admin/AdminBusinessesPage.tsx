import { ModerationTable, type ModerationRow } from '@/components/admin/ModerationTable'
import { AppLayout } from '@/components/layout/AppLayout'
import { useAdminUsersQuery } from '@/features/admin/api'
import {
  useAllBusinessesQuery,
  useReinstateBusinessMutation,
  useSuspendBusinessMutation,
} from '@/features/businesses/api'
import { ownerLabel } from '@/lib/owners'

export function AdminBusinessesPage() {
  const businesses = useAllBusinessesQuery()
  const users = useAdminUsersQuery()
  const suspend = useSuspendBusinessMutation()
  const reinstate = useReinstateBusinessMutation()

  const rows: ModerationRow[] = (businesses.data ?? []).map((business) => ({
    id: business.id,
    name: business.name,
    location: [business.city, business.state, business.country].filter(Boolean).join(', '),
    owner: ownerLabel(business.ownerId, users.data),
    contact: [business.email, business.phone].filter(Boolean).join(' · ') || undefined,
    status: business.status,
  }))

  return (
    <AppLayout>
      <h1 className="mb-1 text-2xl font-semibold text-slate-900 dark:text-slate-100">Businesses</h1>
      <p className="mb-6 text-sm text-slate-500 dark:text-slate-400">Review and moderate every business on the platform.</p>
      <ModerationTable
        entity="business"
        rows={rows}
        isLoading={businesses.isLoading}
        isError={businesses.isError}
        onRetry={() => businesses.refetch()}
        onSuspend={(id) => suspend.mutateAsync(id)}
        onReinstate={(id) => reinstate.mutateAsync(id)}
      />
    </AppLayout>
  )
}
