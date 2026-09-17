const STYLES: Record<string, string> = {
  DRAFT: 'bg-slate-100 text-slate-700 dark:bg-slate-800 dark:text-slate-300',
  PUBLISHED: 'bg-green-100 text-green-700 dark:bg-green-950 dark:text-green-300',
  PENDING: 'animate-pulse-soft bg-amber-100 text-amber-700 dark:bg-amber-950 dark:text-amber-300',
  PAID: 'bg-green-100 text-green-700 dark:bg-green-950 dark:text-green-300',
  COMPLETED: 'bg-blue-100 text-blue-700 dark:bg-blue-950 dark:text-blue-300',
  CANCELLED: 'bg-red-100 text-red-700 dark:bg-red-950 dark:text-red-300',
}

export function StatusBadge({ status }: { status: string }) {
  return (
    <span
      className={`inline-block rounded-full px-2.5 py-0.5 text-xs font-medium transition-colors duration-200 ${STYLES[status] ?? 'bg-slate-100 text-slate-700 dark:bg-slate-800 dark:text-slate-300'}`}
    >
      {status}
    </span>
  )
}
