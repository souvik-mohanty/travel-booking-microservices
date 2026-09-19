import { zodResolver } from '@hookform/resolvers/zod'
import { useForm } from 'react-hook-form'
import { z } from 'zod'
import { StatusBadge } from '@/components/common/StatusBadge'
import { TextField } from '@/components/forms/TextField'
import { AppLayout } from '@/components/layout/AppLayout'
import { Button } from '@/components/ui/button'
import { Card, CardContent } from '@/components/ui/card'
import { Form } from '@/components/ui/form'
import { Skeleton } from '@/components/ui/skeleton'
import {
  useAllBusinessesQuery,
  useCreateBusinessMutation,
  useUpdateBusinessMutation,
} from '@/features/businesses/api'
import { runAction } from '@/lib/actions'
import { blankToUndefined } from '@/lib/errors'
import { useAuthStore } from '@/store/authStore'
import type { Business, BusinessFormValues } from '@/types/business'

const businessSchema = z.object({
  name: z.string().trim().min(1, 'Business name is required').max(255),
  description: z.string().max(2000),
  phone: z.string().max(50),
  email: z.union([z.literal(''), z.string().trim().email('Enter a valid email address')]),
  address: z.string().max(255),
  city: z.string().max(100),
  state: z.string().max(100),
  country: z.string().max(100),
})

type BusinessFormInput = z.infer<typeof businessSchema>

function toFormInput(business?: Business): BusinessFormInput {
  return {
    name: business?.name ?? '',
    description: business?.description ?? '',
    phone: business?.phone ?? '',
    email: business?.email ?? '',
    address: business?.address ?? '',
    city: business?.city ?? '',
    state: business?.state ?? '',
    country: business?.country ?? '',
  }
}

// The update endpoint is a full replace, so a cleared field really is cleared.
function toRequest(values: BusinessFormInput): BusinessFormValues {
  return {
    name: values.name.trim(),
    description: blankToUndefined(values.description),
    phone: blankToUndefined(values.phone),
    email: blankToUndefined(values.email),
    address: blankToUndefined(values.address),
    city: blankToUndefined(values.city),
    state: blankToUndefined(values.state),
    country: blankToUndefined(values.country),
  }
}

function BusinessForm({ business }: { business?: Business }) {
  const createBusiness = useCreateBusinessMutation()
  const updateBusiness = useUpdateBusinessMutation()
  const form = useForm<BusinessFormInput>({
    resolver: zodResolver(businessSchema),
    defaultValues: toFormInput(business),
  })
  const saving = createBusiness.isPending || updateBusiness.isPending

  async function onSubmit(values: BusinessFormInput) {
    const request = toRequest(values)
    try {
      if (business) {
        await runAction(() => updateBusiness.mutateAsync({ id: business.id, request }), 'Business details saved.')
      } else {
        await runAction(() => createBusiness.mutateAsync(request), 'Your business has been created.')
      }
    } catch {
      // runAction already showed the error toast.
    }
  }

  return (
    <Card>
      <CardContent className="px-6">
        <Form {...form}>
          <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-4">
            <TextField control={form.control} name="name" label="Business name" />
            <TextField control={form.control} name="description" label="Description" multiline />
            <div className="grid gap-4 sm:grid-cols-2">
              <TextField control={form.control} name="phone" label="Phone" type="tel" />
              <TextField control={form.control} name="email" label="Contact email" type="email" />
            </div>
            <TextField control={form.control} name="address" label="Address" />
            <div className="grid gap-4 sm:grid-cols-3">
              <TextField control={form.control} name="city" label="City" />
              <TextField control={form.control} name="state" label="State" />
              <TextField control={form.control} name="country" label="Country" />
            </div>
            <Button type="submit" disabled={saving} className="transition-all duration-200 hover:scale-[1.02] active:scale-95">
              {saving ? 'Saving…' : business ? 'Save changes' : 'Create business'}
            </Button>
          </form>
        </Form>
      </CardContent>
    </Card>
  )
}

export function MyBusinessPage() {
  const user = useAuthStore((state) => state.user)
  const { data: businesses, isLoading, isError, refetch } = useAllBusinessesQuery()

  // No "my business" endpoint exists, so find the caller's among all of them.
  const business = businesses?.find((candidate) => candidate.ownerId === user?.userId)

  return (
    <AppLayout>
      <div className="mb-6 flex items-center gap-3">
        <h1 className="text-2xl font-semibold text-slate-900 dark:text-slate-100">My Business</h1>
        {business && <StatusBadge status={business.status} />}
      </div>

      <div className="max-w-2xl space-y-4">
        {business?.status === 'SUSPENDED' && (
          <p className="rounded-lg border border-red-200 bg-red-50 p-4 text-sm text-red-700">
            An administrator has suspended this business. You can still edit its details, but it may not be usable until it
            is reinstated.
          </p>
        )}

        {isLoading && <Skeleton className="h-96 rounded-lg" />}

        {isError && (
          <div className="rounded-lg border border-red-200 bg-red-50 p-4 text-sm text-red-700">
            Could not load your business.{' '}
            <button onClick={() => refetch()} className="font-medium underline">
              Try again
            </button>
          </div>
        )}

        {!isLoading && !isError && (
          <>
            {!business && (
              <p className="text-sm text-slate-500 dark:text-slate-400">
                You haven&apos;t set up your business yet. Fill in the details below to create it.
              </p>
            )}
            {/* key: remount with the saved values once a business exists / changes */}
            <BusinessForm key={business?.id ?? 'new'} business={business} />
          </>
        )}
      </div>
    </AppLayout>
  )
}
