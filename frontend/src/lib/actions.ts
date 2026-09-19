import { toast } from 'sonner'
import { apiErrorMessage } from '@/lib/errors'

// Runs a mutation and reports the outcome with a toast. Rethrows on failure so
// a caller that cares (e.g. ConfirmDialog, which stays open) can react, while
// callers that don't can simply ignore the rejection.
export async function runAction(action: () => Promise<unknown>, successMessage: string): Promise<void> {
  try {
    await action()
    toast.success(successMessage)
  } catch (error) {
    toast.error(apiErrorMessage(error, 'Something went wrong. Please try again.'))
    throw error
  }
}
