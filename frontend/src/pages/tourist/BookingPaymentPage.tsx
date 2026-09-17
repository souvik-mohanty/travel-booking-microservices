import { useState } from 'react'
import { isAxiosError } from 'axios'
import { Navigate, useNavigate, useParams } from 'react-router-dom'
import { AppLayout } from '@/components/layout/AppLayout'
import { StatusBadge } from '@/components/common/StatusBadge'
import { Button } from '@/components/ui/button'
import { Card, CardContent } from '@/components/ui/card'
import { Skeleton } from '@/components/ui/skeleton'
import { useBookingQuery } from '@/features/bookings/api'
import { useCreatePaymentMutation, useVerifyPaymentMutation } from '@/features/payments/api'
import { useTourQuery } from '@/features/tours/api'
import { formatDateRange, formatMoney } from '@/lib/format'
import { openRazorpayCheckout } from '@/lib/razorpay'
import { useAuthStore } from '@/store/authStore'

export function BookingPaymentPage() {
  const { id } = useParams<{ id: string }>()
  const user = useAuthStore((state) => state.user)
  const navigate = useNavigate()
  const { data: booking, isLoading: bookingLoading } = useBookingQuery(id)
  const { data: tour } = useTourQuery(booking?.tourId)
  const createPayment = useCreatePaymentMutation()
  const verifyPayment = useVerifyPaymentMutation()
  const [error, setError] = useState<string | null>(null)
  const [paying, setPaying] = useState(false)

  if (bookingLoading) {
    return (
      <AppLayout>
        <Skeleton className="h-56 rounded-lg" />
      </AppLayout>
    )
  }

  if (!booking) {
    return (
      <AppLayout>
        <p className="text-sm text-red-600 dark:text-red-400">Booking not found.</p>
      </AppLayout>
    )
  }

  if (booking.status !== 'PENDING') {
    // Already paid (or cancelled) -- nothing to check out for.
    return <Navigate to={`/bookings/${booking.id}`} replace />
  }

  async function handlePay() {
    setError(null)
    setPaying(true)
    try {
      const order = await createPayment.mutateAsync({ bookingId: booking!.id })

      await openRazorpayCheckout({
        keyId: order.razorpayKeyId,
        orderId: order.razorpayOrderId,
        amount: order.amount,
        currency: order.currency,
        tourTitle: tour?.title ?? 'TourFlow booking',
        customerEmail: user?.email,
        onSuccess: async (response) => {
          try {
            await verifyPayment.mutateAsync({
              paymentId: order.paymentId,
              request: {
                razorpay_order_id: response.razorpay_order_id,
                razorpay_payment_id: response.razorpay_payment_id,
                razorpay_signature: response.razorpay_signature,
              },
            })
            navigate(`/bookings/${booking!.id}/confirmation`, { replace: true })
          } catch {
            setError('Payment could not be verified. If money was deducted, contact support with your booking ID.')
            setPaying(false)
          }
        },
        onDismiss: () => setPaying(false),
      })
    } catch (err) {
      const data = isAxiosError(err) ? (err.response?.data as { message?: string } | undefined) : undefined
      setError(typeof data?.message === 'string' ? data.message : 'Could not start payment.')
      setPaying(false)
    }
  }

  return (
    <AppLayout>
      <div className="mx-auto max-w-lg">
        <h1 className="mb-6 text-2xl font-semibold text-slate-900 dark:text-slate-100">Payment</h1>

        <Card>
          <CardContent className="space-y-4 px-5">
            {tour && (
              <div>
                <h2 className="font-medium text-slate-900 dark:text-slate-100">{tour.title}</h2>
                <p className="text-sm text-slate-500 dark:text-slate-400">{tour.destination}</p>
                <p className="text-sm text-slate-500 dark:text-slate-400">
                  {formatDateRange(tour.startDate, tour.endDate)}
                </p>
              </div>
            )}

            <div className="space-y-2 border-t border-slate-100 pt-4 text-sm dark:border-slate-800">
              <div className="flex justify-between">
                <span className="text-slate-500 dark:text-slate-400">Travelers</span>
                <span className="text-slate-900 dark:text-slate-100">{booking.numberOfParticipants}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-slate-500 dark:text-slate-400">Status</span>
                <StatusBadge status={booking.status} />
              </div>
              <div className="flex justify-between text-base font-semibold">
                <span className="text-slate-900 dark:text-slate-100">Total</span>
                <span className="text-slate-900 dark:text-slate-100">{formatMoney(booking.totalPrice)}</span>
              </div>
            </div>

            {error && <p className="text-sm text-red-600 dark:text-red-400">{error}</p>}

            <Button
              onClick={handlePay}
              disabled={paying}
              className="w-full transition-all duration-200 hover:scale-[1.02] active:scale-95"
            >
              {paying ? 'Opening secure checkout…' : `Pay ${formatMoney(booking.totalPrice)}`}
            </Button>
            <p className="text-center text-xs text-slate-400">Payments are processed securely by Razorpay.</p>
          </CardContent>
        </Card>
      </div>
    </AppLayout>
  )
}
