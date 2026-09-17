// Mirrors payment-service's PaymentResponse/CreatePaymentResponse
// (backend/services/payment-service .../payment/dto/*.java).
export type PaymentStatus = 'CREATED' | 'PENDING' | 'PAID' | 'FAILED' | 'REFUNDED'
export type PayoutStatus = 'NOT_ELIGIBLE' | 'ELIGIBLE' | 'PROCESSING' | 'PAID' | 'FAILED'

export interface Payment {
  id: string
  bookingId: string
  customerId: string
  amount: number
  currency: string
  paymentStatus: PaymentStatus
  payoutStatus: PayoutStatus
  platformFee: number
  businessAmount: number
  gateway: string
  gatewayOrderId: string | null
  gatewayPaymentId: string | null
  paidAt: string | null
  payoutEligibleAt: string | null
  paidOutAt: string | null
  createdAt: string
  updatedAt: string
}

export interface CreatePaymentRequest {
  bookingId: string
}

// The fields the frontend actually needs to open Razorpay Checkout --
// paymentId here is payment-service's own PENDING payment row id, not
// Razorpay's.
export interface CreatePaymentResponse {
  paymentId: string
  razorpayOrderId: string
  razorpayKeyId: string
  amount: number
  currency: string
  paymentStatus: PaymentStatus
}

// Exact snake_case field names Razorpay's Checkout.js success handler hands
// back -- verify-endpoint expects them verbatim, not camelCased.
export interface VerifyPaymentRequest {
  razorpay_order_id: string
  razorpay_payment_id: string
  razorpay_signature: string
}
