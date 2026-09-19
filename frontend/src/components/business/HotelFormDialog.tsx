import { useState, type ReactNode } from 'react'
import { zodResolver } from '@hookform/resolvers/zod'
import { useForm } from 'react-hook-form'
import { z } from 'zod'
import { TextField } from '@/components/forms/TextField'
import { Button } from '@/components/ui/button'
import { Dialog, DialogContent, DialogDescription, DialogHeader, DialogTitle, DialogTrigger } from '@/components/ui/dialog'
import { Form } from '@/components/ui/form'
import { useCreateHotelMutation, useUpdateHotelMutation } from '@/features/hotels/api'
import { runAction } from '@/lib/actions'
import { blankToUndefined } from '@/lib/errors'
import type { Hotel, HotelFormValues } from '@/types/hotel'

const hotelSchema = z.object({
  name: z.string().trim().min(1, 'Hotel name is required').max(255),
  description: z.string().max(2000),
  address: z.string().max(255),
  city: z.string().max(100),
  state: z.string().max(100),
  country: z.string().max(100),
})

type HotelFormInput = z.infer<typeof hotelSchema>

function toFormInput(hotel?: Hotel): HotelFormInput {
  return {
    name: hotel?.name ?? '',
    description: hotel?.description ?? '',
    address: hotel?.address ?? '',
    city: hotel?.city ?? '',
    state: hotel?.state ?? '',
    country: hotel?.country ?? '',
  }
}

function toRequest(values: HotelFormInput): HotelFormValues {
  return {
    name: values.name.trim(),
    description: blankToUndefined(values.description),
    address: blankToUndefined(values.address),
    city: blankToUndefined(values.city),
    state: blankToUndefined(values.state),
    country: blankToUndefined(values.country),
  }
}

// Create (no `hotel`) or edit (with `hotel`) in a dialog opened by `trigger`.
export function HotelFormDialog({ hotel, trigger }: { hotel?: Hotel; trigger: ReactNode }) {
  const [open, setOpen] = useState(false)
  const createHotel = useCreateHotelMutation()
  const updateHotel = useUpdateHotelMutation()
  const form = useForm<HotelFormInput>({ resolver: zodResolver(hotelSchema), defaultValues: toFormInput(hotel) })
  const saving = createHotel.isPending || updateHotel.isPending

  async function onSubmit(values: HotelFormInput) {
    const request = toRequest(values)
    try {
      if (hotel) {
        await runAction(() => updateHotel.mutateAsync({ id: hotel.id, request }), 'Hotel updated.')
      } else {
        await runAction(() => createHotel.mutateAsync(request), 'Hotel added.')
        form.reset(toFormInput())
      }
      setOpen(false)
    } catch {
      // runAction already showed the error toast; keep the dialog open.
    }
  }

  return (
    <Dialog
      open={open}
      onOpenChange={(next) => {
        setOpen(next)
        if (next) form.reset(toFormInput(hotel))
      }}
    >
      <DialogTrigger asChild>{trigger}</DialogTrigger>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>{hotel ? 'Edit hotel' : 'Add a hotel'}</DialogTitle>
          <DialogDescription>{hotel ? 'Update this hotel’s details.' : 'Add a hotel, then add its rooms.'}</DialogDescription>
        </DialogHeader>
        <Form {...form}>
          <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-4">
            <TextField control={form.control} name="name" label="Hotel name" />
            <TextField control={form.control} name="description" label="Description" multiline />
            <TextField control={form.control} name="address" label="Address" />
            <div className="grid gap-4 sm:grid-cols-3">
              <TextField control={form.control} name="city" label="City" />
              <TextField control={form.control} name="state" label="State" />
              <TextField control={form.control} name="country" label="Country" />
            </div>
            <Button type="submit" disabled={saving} className="w-full">
              {saving ? 'Saving…' : hotel ? 'Save changes' : 'Add hotel'}
            </Button>
          </form>
        </Form>
      </DialogContent>
    </Dialog>
  )
}
