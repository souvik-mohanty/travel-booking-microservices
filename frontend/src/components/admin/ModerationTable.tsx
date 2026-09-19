import { useState } from 'react'
import { ConfirmDialog } from '@/components/common/ConfirmDialog'
import { StatusBadge } from '@/components/common/StatusBadge'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Skeleton } from '@/components/ui/skeleton'
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table'
import { runAction } from '@/lib/actions'

export interface ModerationRow {
  id: string
  name: string
  location: string
  owner: string
  contact?: string
  status: string
}

interface ModerationTableProps {
  // "business" or "hotel" -- used in the dialog wording.
  entity: string
  rows: ModerationRow[]
  isLoading: boolean
  isError: boolean
  onRetry: () => void
  onSuspend: (id: string) => Promise<unknown>
  onReinstate: (id: string) => Promise<unknown>
}

// Shared list for the admin Businesses and Hotels pages: search, and
// suspend / reinstate with a confirmation (both are ADMIN-only server-side).
export function ModerationTable({ entity, rows, isLoading, isError, onRetry, onSuspend, onReinstate }: ModerationTableProps) {
  const [search, setSearch] = useState('')
  const term = search.trim().toLowerCase()
  const visible = term
    ? rows.filter((row) => [row.name, row.location, row.owner, row.contact ?? ''].some((v) => v.toLowerCase().includes(term)))
    : rows

  if (isLoading) {
    return (
      <div className="space-y-2">
        {Array.from({ length: 4 }).map((_, i) => (
          <Skeleton key={i} className="h-12 rounded-md" />
        ))}
      </div>
    )
  }

  if (isError) {
    return (
      <div className="rounded-lg border border-red-200 bg-red-50 p-4 text-sm text-red-700">
        Could not load the list.{' '}
        <button onClick={onRetry} className="font-medium underline">
          Try again
        </button>
      </div>
    )
  }

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between gap-4">
        <Input
          value={search}
          onChange={(event) => setSearch(event.target.value)}
          placeholder={`Search ${entity}s by name, place or owner`}
          aria-label={`Search ${entity}s`}
          className="max-w-sm"
        />
        <p className="text-sm text-slate-500 dark:text-slate-400">
          {visible.length} of {rows.length}
        </p>
      </div>

      {visible.length === 0 ? (
        <p className="rounded-lg border border-dashed border-slate-300 p-8 text-center text-sm text-slate-500 dark:border-slate-700 dark:text-slate-400">
          {rows.length === 0 ? `No ${entity}s yet.` : `No ${entity}s match your search.`}
        </p>
      ) : (
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>Name</TableHead>
              <TableHead>Location</TableHead>
              <TableHead>Owner</TableHead>
              <TableHead>Status</TableHead>
              <TableHead className="text-right">Action</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {visible.map((row) => (
              <TableRow key={row.id}>
                <TableCell className="font-medium">
                  {row.name}
                  {row.contact && <div className="text-xs font-normal text-slate-500">{row.contact}</div>}
                </TableCell>
                <TableCell>{row.location || '—'}</TableCell>
                <TableCell>{row.owner}</TableCell>
                <TableCell>
                  <StatusBadge status={row.status} />
                </TableCell>
                <TableCell className="text-right">
                  {row.status === 'SUSPENDED' ? (
                    <Button
                      size="sm"
                      onClick={() => runAction(() => onReinstate(row.id), `${row.name} reinstated.`).catch(() => {})}
                    >
                      Reinstate
                    </Button>
                  ) : (
                    <ConfirmDialog
                      destructive
                      title={`Suspend “${row.name}”?`}
                      description={`This ${entity} will be marked suspended and stop being usable until you reinstate it.`}
                      confirmLabel={`Suspend ${entity}`}
                      onConfirm={() => runAction(() => onSuspend(row.id), `${row.name} suspended.`)}
                      trigger={
                        <Button size="sm" variant="outline" className="text-red-600 hover:text-red-700">
                          Suspend
                        </Button>
                      }
                    />
                  )}
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      )}
    </div>
  )
}
