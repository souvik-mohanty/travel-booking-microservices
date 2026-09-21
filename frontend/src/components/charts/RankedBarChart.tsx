import { Bar, BarChart, CartesianGrid, ResponsiveContainer, Tooltip, XAxis, YAxis } from 'recharts'
import { formatMoney } from '@/lib/format'

export interface RankedBarDatum {
  id: string
  label: string
  value: number
}

export function RankedBarChart({ data }: { data: RankedBarDatum[] }) {
  return (
    <ResponsiveContainer width="100%" height={280}>
      <BarChart data={data} layout="vertical" margin={{ top: 8, right: 16, left: 8, bottom: 0 }}>
        <CartesianGrid strokeDasharray="3 3" horizontal={false} stroke="var(--color-slate-200)" />
        <XAxis type="number" tickFormatter={(value: number) => formatMoney(value)} fontSize={12} stroke="var(--color-slate-400)" />
        <YAxis type="category" dataKey="label" width={110} fontSize={12} stroke="var(--color-slate-400)" />
        <Tooltip
          formatter={(value) => formatMoney(Number(value))}
          contentStyle={{ borderRadius: 8, borderColor: 'var(--color-slate-200)', fontSize: 13 }}
        />
        <Bar dataKey="value" fill="var(--color-orange-500)" radius={[0, 4, 4, 0]} />
      </BarChart>
    </ResponsiveContainer>
  )
}
