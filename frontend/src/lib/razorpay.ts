// Thin wrapper around Razorpay's Checkout.js. This is real, live payment
// integration against payment-service's Razorpay test-mode setup -- not a
// mock. Loading it as a runtime <script> tag (rather than an npm package)
// matches Razorpay's own documented integration and keeps the payment
// gateway's own JS/security posture out of the app's bundle.
const CHECKOUT_SRC = 'https://checkout.razorpay.com/v1/checkout.js'

interface RazorpaySuccessResponse {
  razorpay_order_id: string
  razorpay_payment_id: string
  razorpay_signature: string
}

interface RazorpayOptions {
  key: string
  order_id: string
  amount: number
  currency: string
  name: string
  description?: string
  prefill?: { name?: string; email?: string }
  theme?: { color?: string }
  handler: (response: RazorpaySuccessResponse) => void
  modal?: { ondismiss?: () => void }
}

interface RazorpayInstance {
  open: () => void
}

declare global {
  interface Window {
    Razorpay?: new (options: RazorpayOptions) => RazorpayInstance
  }
}

let loadPromise: Promise<void> | null = null

function loadCheckoutScript(): Promise<void> {
  if (window.Razorpay) return Promise.resolve()

  if (!loadPromise) {
    loadPromise = new Promise((resolve, reject) => {
      const script = document.createElement('script')
      script.src = CHECKOUT_SRC
      script.async = true
      script.onload = () => resolve()
      script.onerror = () => {
        loadPromise = null
        reject(new Error('Failed to load Razorpay Checkout'))
      }
      document.body.appendChild(script)
    })
  }

  return loadPromise
}

export interface OpenCheckoutParams {
  keyId: string
  orderId: string
  amount: number
  currency: string
  tourTitle: string
  customerEmail?: string
  onSuccess: (response: RazorpaySuccessResponse) => void
  onDismiss?: () => void
}

// Opens the Razorpay Checkout modal. The success handler only hands back
// the raw gateway response -- the caller MUST still call the backend's
// /api/payments/{id}/verify endpoint and treat only that response as proof
// of payment. Never mark a booking paid client-side off this callback
// alone.
export async function openRazorpayCheckout(params: OpenCheckoutParams): Promise<void> {
  await loadCheckoutScript()

  if (!window.Razorpay) {
    throw new Error('Razorpay Checkout script did not initialize')
  }

  const checkout = new window.Razorpay({
    key: params.keyId,
    order_id: params.orderId,
    amount: Math.round(params.amount * 100),
    currency: params.currency,
    name: 'TourFlow',
    description: params.tourTitle,
    prefill: params.customerEmail ? { email: params.customerEmail } : undefined,
    theme: { color: '#2563eb' },
    handler: params.onSuccess,
    modal: { ondismiss: params.onDismiss },
  })

  checkout.open()
}
