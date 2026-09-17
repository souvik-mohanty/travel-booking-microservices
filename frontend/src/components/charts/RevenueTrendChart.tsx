import { Area, AreaChart, CartesianGrid, ResponsiveContainer, Tooltip, XAxis, YAxis } from 'recharts'
import { formatDate, formatMoney } from '@/lib/format'
import type { DailyBookingStats } from '@/types/analytics'

export function RevenueTrendChart({ data }: { data: DailyBookingStats[] }) {
  return (
    <ResponsiveContainer width="100%" height={280}>
      <AreaChart data={data} margin={{ top: 8, right: 8, left: 8, bottom: 0 }}>
        <defs>
          <linearGradient id="revenueFill" x1="0" y1="0" x2="0" y2="1">
            <stop offset="0%" stopColor="var(--color-blue-600)" stopOpacity={0.35} />
            <stop offset="100%" stopColor="var(--color-blue-600)" stopOpacity={0} />
          </linearGradient>
        </defs>
        <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="var(--color-slate-200)" />
        <XAxis
          dataKey="date"
          tickFormatter={(value: string) => formatDate(value)}
          fontSize={12}
          stroke="var(--color-slate-400)"
        />
        <YAxis
          tickFormatter={(value: number) => formatMoney(value)}
          width={80}
          fontSize={12}
          stroke="var(--color-slate-400)"
        />
        <Tooltip
          formatter={(value) => formatMoney(Number(value))}
          labelFormatter={(value) => formatDate(String(value))}
          contentStyle={{ borderRadius: 8, borderColor: 'var(--color-slate-200)', fontSize: 13 }}
        />
        <Area
          type="monotone"
          dataKey="revenue"
          stroke="var(--color-blue-600)"
          strokeWidth={2}
          fill="url(#revenueFill)"
        />
      </AreaChart>
    </ResponsiveContainer>
  )
}
