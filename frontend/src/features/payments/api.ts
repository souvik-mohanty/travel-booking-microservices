import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { apiClient } from '@/lib/apiClient'
import { bookingKeys } from '@/features/bookings/api'
import type { CreatePaymentRequest, CreatePaymentResponse, Payment, VerifyPaymentRequest } from '@/types/payment'

async function createPayment(request: CreatePaymentRequest): Promise<CreatePaymentResponse> {
  const { data } = await apiClient.post<CreatePaymentResponse>('/api/payments', request)
  return data
}

async function verifyPayment(paymentId: string, request: VerifyPaymentRequest): Promise<Payment> {
  const { data } = await apiClient.post<Payment>(`/api/payments/${paymentId}/verify`, request)
  return data
}

async function getPayment(id: string): Promise<Payment> {
  const { data } = await apiClient.get<Payment>(`/api/payments/${id}`)
  return data
}

export function usePaymentQuery(id: string | undefined) {
  return useQuery({
    queryKey: ['payments', id],
    queryFn: () => getPayment(id as string),
    enabled: Boolean(id),
  })
}

export function useCreatePaymentMutation() {
  return useMutation({ mutationFn: createPayment })
}

export function useVerifyPaymentMutation() {
  const queryClient = useQueryClient()
  return useMutation({
    mutationFn: ({ paymentId, request }: { paymentId: string; request: VerifyPaymentRequest }) =>
      verifyPayment(paymentId, request),
    onSuccess: () => {
      // The booking only flips PENDING->PAID once payment-service's
      // best-effort mark-paid call to booking-service lands (or the
      // Razorpay webhook does it independently) -- invalidate rather than
      // assume, so the UI re-fetches the real status instead of trusting
      // the Checkout success callback alone.
      queryClient.invalidateQueries({ queryKey: bookingKeys.mine })
    },
  })
}
