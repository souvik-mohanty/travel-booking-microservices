import { useEffect, useState } from 'react'
import { disableUser, enableUser, getUsers } from '../api/adminUsers'
import { AdminUserDetail } from '../components/AdminUserDetail'
import { AppLayout } from '../components/AppLayout'
import type { AdminUser } from '../types/adminUser'
import { formatDate } from '../lib/format'

const ROLE_TABS = ['ALL', 'TOURIST', 'BUSINESS', 'ADMIN'] as const

export function AdminUsersPage() {
  const [roleFilter, setRoleFilter] = useState<(typeof ROLE_TABS)[number]>('ALL')
  const [users, setUsers] = useState<AdminUser[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [actioningId, setActioningId] = useState<string | null>(null)
  const [expandedId, setExpandedId] = useState<string | null>(null)

  async function load() {
    setLoading(true)
    setError(null)
    try {
      setUsers(await getUsers(roleFilter === 'ALL' ? undefined : roleFilter))
    } catch {
      setError('Could not load users. Is identity-service running?')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    load()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [roleFilter])

  async function handleDisable(id: string) {
    setActioningId(id)
    try {
      const updated = await disableUser(id)
      setUsers((prev) => prev.map((u) => (u.id === id ? updated : u)))
    } catch {
      setError('Could not disable that account.')
    } finally {
      setActioningId(null)
    }
  }

  async function handleEnable(id: string) {
    setActioningId(id)
    try {
      const updated = await enableUser(id)
      setUsers((prev) => prev.map((u) => (u.id === id ? updated : u)))
    } catch {
      setError('Could not enable that account.')
    } finally {
      setActioningId(null)
    }
  }

  return (
    <AppLayout>
      <h1 className="mb-6 text-2xl font-semibold text-slate-900 dark:text-slate-100">Users</h1>

      <div className="mb-4 flex gap-2">
        {ROLE_TABS.map((tab) => (
          <button
            key={tab}
            onClick={() => setRoleFilter(tab)}
            className={
              roleFilter === tab
                ? 'rounded-full bg-blue-600 px-3 py-1 text-sm font-medium text-white dark:bg-blue-500 dark:text-white'
                : 'rounded-full border border-slate-300 px-3 py-1 text-sm text-slate-700 transition-all duration-200 hover:border-blue-300 hover:bg-blue-50 hover:text-blue-700 dark:border-slate-700 dark:text-slate-200 dark:hover:border-blue-600 dark:hover:bg-slate-800 dark:hover:text-blue-300'
            }
          >
            {tab === 'ALL' ? 'All' : tab.charAt(0) + tab.slice(1).toLowerCase()}
          </button>
        ))}
      </div>

      {loading && <p className="text-sm text-slate-500 dark:text-slate-400">Loading…</p>}
      {error && <p className="mb-4 text-sm text-red-600 dark:text-red-400">{error}</p>}

      {!loading && !error && users.length === 0 && (
        <p className="text-sm text-slate-500 dark:text-slate-400">No accounts found.</p>
      )}

      <div className="space-y-3">
        {users.map((user) => (
          <div
            key={user.id}
            className="rounded-lg border border-slate-200 bg-white p-4 dark:border-slate-800 dark:bg-slate-900"
          >
            <div className="flex items-center justify-between">
              <div>
                <div className="flex items-center gap-2">
                  <span className="font-medium text-slate-900 dark:text-slate-100">
                    {user.firstName} {user.lastName ?? ''}
                  </span>
                  <span className="rounded-full bg-slate-100 px-2 py-0.5 text-xs font-medium text-slate-600 dark:bg-slate-800 dark:text-slate-300">
                    {user.role}
                  </span>
                  {!user.enabled && (
                    <span className="rounded-full bg-red-100 px-2 py-0.5 text-xs font-medium text-red-700 dark:bg-red-950 dark:text-red-300">
                      Disabled
                    </span>
                  )}
                </div>
                <p className="text-sm text-slate-500 dark:text-slate-400">
                  {user.email} · {user.provider} · joined {formatDate(user.createdAt)}
                </p>
              </div>

              <div className="flex gap-2">
                <button
                  onClick={() => setExpandedId(expandedId === user.id ? null : user.id)}
                  className="rounded-md border border-slate-300 px-3 py-1.5 text-sm text-slate-700 transition-all duration-200 hover:border-blue-300 hover:bg-blue-50 hover:text-blue-700 dark:border-slate-700 dark:text-slate-200 dark:hover:border-blue-600 dark:hover:bg-slate-800 dark:hover:text-blue-300"
                >
                  {expandedId === user.id ? 'Hide details' : 'View details'}
                </button>
                {user.enabled ? (
                  <button
                    onClick={() => handleDisable(user.id)}
                    disabled={actioningId === user.id}
                    className="rounded-md border border-red-300 px-3 py-1.5 text-sm text-red-600 transition hover:bg-red-50 disabled:opacity-50 dark:border-red-800 dark:text-red-400 dark:hover:bg-red-950"
                  >
                    Disable
                  </button>
                ) : (
                  <button
                    onClick={() => handleEnable(user.id)}
                    disabled={actioningId === user.id}
                    className="rounded-md bg-blue-600 px-3 py-1.5 text-sm font-medium text-white transition-all duration-200 hover:bg-blue-700 hover:scale-[1.02] active:scale-95 disabled:opacity-50 dark:bg-blue-500 dark:text-white"
                  >
                    Enable
                  </button>
                )}
              </div>
            </div>

            {expandedId === user.id && (
              <div className="mt-3 border-t border-slate-200 pt-3 dark:border-slate-800">
                <AdminUserDetail user={user} />
              </div>
            )}
          </div>
        ))}
      </div>
    </AppLayout>
  )
}
