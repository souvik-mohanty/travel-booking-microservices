import { useState } from 'react'
import { ConfirmDialog } from '@/components/common/ConfirmDialog'
import { StatusBadge } from '@/components/common/StatusBadge'
import { AppLayout } from '@/components/layout/AppLayout'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select'
import { Skeleton } from '@/components/ui/skeleton'
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table'
import { useAdminUsersQuery, useDisableUserMutation, useEnableUserMutation } from '@/features/admin/api'
import { runAction } from '@/lib/actions'
import { formatDate } from '@/lib/format'
import { useAuthStore } from '@/store/authStore'

const ROLE_FILTERS = [
  { value: 'ALL', label: 'All roles' },
  { value: 'TOURIST', label: 'Tourists' },
  { value: 'BUSINESS', label: 'Businesses' },
  { value: 'ADMIN', label: 'Admins' },
]

export function AdminUsersPage() {
  const me = useAuthStore((state) => state.user)
  const [role, setRole] = useState('ALL')
  const [search, setSearch] = useState('')
  const { data: users, isLoading, isError, refetch } = useAdminUsersQuery(role === 'ALL' ? undefined : role)
  const disableUser = useDisableUserMutation()
  const enableUser = useEnableUserMutation()

  const term = search.trim().toLowerCase()
  const visible = (users ?? []).filter(
    (user) => !term || [user.email, user.firstName, user.lastName ?? ''].some((value) => value.toLowerCase().includes(term)),
  )

  return (
    <AppLayout>
      <h1 className="mb-1 text-2xl font-semibold text-slate-900 dark:text-slate-100">Users</h1>
      <p className="mb-6 text-sm text-slate-500 dark:text-slate-400">
        Everyone with an account. Disabling a user blocks them from signing in.
      </p>

      <div className="mb-4 flex flex-wrap items-center gap-3">
        <Input
          value={search}
          onChange={(event) => setSearch(event.target.value)}
          placeholder="Search by name or email"
          aria-label="Search users"
          className="max-w-sm"
        />
        <Select value={role} onValueChange={setRole}>
          <SelectTrigger className="w-40" aria-label="Filter by role">
            <SelectValue />
          </SelectTrigger>
          <SelectContent>
            {ROLE_FILTERS.map((filter) => (
              <SelectItem key={filter.value} value={filter.value}>
                {filter.label}
              </SelectItem>
            ))}
          </SelectContent>
        </Select>
        {users && (
          <p className="text-sm text-slate-500 dark:text-slate-400">
            {visible.length} of {users.length}
          </p>
        )}
      </div>

      {isLoading && (
        <div className="space-y-2">
          {Array.from({ length: 5 }).map((_, i) => (
            <Skeleton key={i} className="h-12 rounded-md" />
          ))}
        </div>
      )}

      {isError && (
        <div className="rounded-lg border border-red-200 bg-red-50 p-4 text-sm text-red-700">
          Could not load users.{' '}
          <button onClick={() => refetch()} className="font-medium underline">
            Try again
          </button>
        </div>
      )}

      {!isLoading && !isError && visible.length === 0 && (
        <p className="rounded-lg border border-dashed border-slate-300 p-8 text-center text-sm text-slate-500 dark:border-slate-700 dark:text-slate-400">
          No users match.
        </p>
      )}

      {visible.length > 0 && (
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>Name</TableHead>
              <TableHead>Email</TableHead>
              <TableHead>Role</TableHead>
              <TableHead>Sign-in</TableHead>
              <TableHead>Joined</TableHead>
              <TableHead>Status</TableHead>
              <TableHead className="text-right">Action</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {visible.map((user) => {
              const isMe = user.id === me?.userId
              const name = [user.firstName, user.lastName].filter(Boolean).join(' ')
              return (
                <TableRow key={user.id}>
                  <TableCell className="font-medium">
                    {name || '—'}
                    {isMe && <span className="ml-2 text-xs font-normal text-slate-500">(you)</span>}
                  </TableCell>
                  <TableCell>{user.email}</TableCell>
                  <TableCell>{user.role}</TableCell>
                  <TableCell>{user.provider}</TableCell>
                  <TableCell>{formatDate(user.createdAt)}</TableCell>
                  <TableCell>
                    <StatusBadge status={user.enabled ? 'ENABLED' : 'DISABLED'} />
                  </TableCell>
                  <TableCell className="text-right">
                    {isMe ? (
                      <span className="text-xs text-slate-400">—</span>
                    ) : user.enabled ? (
                      <ConfirmDialog
                        destructive
                        title={`Disable ${user.email}?`}
                        description="They will be blocked from signing in, and from renewing an existing session, until you enable the account again. A session that is already open keeps working until its short-lived token expires (about 15 minutes)."
                        confirmLabel="Disable user"
                        onConfirm={() => runAction(() => disableUser.mutateAsync(user.id), `${user.email} disabled.`)}
                        trigger={
                          <Button size="sm" variant="outline" className="text-red-600 hover:text-red-700">
                            Disable
                          </Button>
                        }
                      />
                    ) : (
                      <Button
                        size="sm"
                        onClick={() => runAction(() => enableUser.mutateAsync(user.id), `${user.email} enabled.`).catch(() => {})}
                      >
                        Enable
                      </Button>
                    )}
                  </TableCell>
                </TableRow>
              )
            })}
          </TableBody>
        </Table>
      )}
    </AppLayout>
  )
}
