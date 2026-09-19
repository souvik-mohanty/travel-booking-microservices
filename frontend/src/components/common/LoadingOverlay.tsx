import { useEffect, useState } from 'react'
import { Loader2 } from 'lucide-react'

interface LoadingOverlayProps {
  open: boolean
  title: string
  message?: string
}

// Full-screen "please wait" cover, used while signing in / registering /
// bouncing to Google. It blocks clicks on the page underneath, and after a few
// seconds explains why it's taking long: the free-tier backend sleeps when
// idle and the first request can take up to a minute to wake it.
export function LoadingOverlay({ open, title, message }: LoadingOverlayProps) {
  return open ? <OverlayBody title={title} message={message} /> : null
}

// Separate component so its "still waiting" timer starts fresh every time the
// overlay opens and is cleaned up when it closes.
function OverlayBody({ title, message }: Omit<LoadingOverlayProps, 'open'>) {
  const [slow, setSlow] = useState(false)

  useEffect(() => {
    const timer = setTimeout(() => setSlow(true), 5000)
    return () => clearTimeout(timer)
  }, [])

  return (
    <div
      role="status"
      aria-live="polite"
      aria-busy="true"
      className="animate-fade-in fixed inset-0 z-50 flex flex-col items-center justify-center gap-4 bg-white/90 px-6 text-center backdrop-blur-sm"
    >
      <Loader2 className="size-10 animate-spin text-blue-600" aria-hidden="true" />
      <p className="text-base font-semibold text-slate-900">{title}</p>
      <p className="max-w-sm text-sm text-slate-500">
        {slow
          ? 'Waking up the server — free-tier hosting can take up to a minute on the first request. Please keep this page open.'
          : message}
      </p>
    </div>
  )
}
