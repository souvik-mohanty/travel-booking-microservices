import logoUrl from '@/assets/logo.png'
import { cn } from 'cn'

interface LogoProps {
  // Height of the suitcase mark in px; the wordmark scales with it.
  size?: number
  // Hide the "TourFlow" text and show the mark alone.
  markOnly?: boolean
  className?: string
}

// The TourFlow suitcase mark plus a two-tone wordmark in the logo's own blue and
// orange. The image is decorative when the wordmark is shown (the text already
// names the brand) and labelled when it stands alone.
export function Logo({ size = 36, markOnly = false, className }: LogoProps) {
  return (
    <span className={cn('inline-flex items-center gap-2.5', className)}>
      <img
        src={logoUrl}
        alt={markOnly ? 'TourFlow' : ''}
        style={{ height: size }}
        className="w-auto select-none"
        draggable={false}
      />
      {!markOnly && (
        <span className="font-bold tracking-tight" style={{ fontSize: size * 0.6 }}>
          <span className="text-blue-600">Tour</span>
          <span className="text-orange-600">Flow</span>
        </span>
      )}
    </span>
  )
}
