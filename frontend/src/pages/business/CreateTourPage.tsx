import { zodResolver } from '@hookform/resolvers/zod'
import { isAxiosError } from 'axios'
import { useForm } from 'react-hook-form'
import { useNavigate } from 'react-router-dom'
import { z } from 'zod'
import { AppLayout } from '@/components/layout/AppLayout'
import { Button } from '@/components/ui/button'
import { Card, CardContent } from '@/components/ui/card'
import { Form, FormControl, FormField, FormItem, FormLabel, FormMessage } from '@/components/ui/form'
import { Input } from '@/components/ui/input'
import { Textarea } from '@/components/ui/textarea'
import { useCreateTourMutation } from '@/features/tours/api'

// price/maxParticipants stay strings in the form itself (matching what
// <Input type="number"> actually hands RHF via onChange) and get converted
// to numbers in onSubmit -- avoids the z.coerce input/output type mismatch
// with useForm's single type parameter.
const createTourSchema = z
  .object({
    title: z.string().min(1, 'Title is required').max(255),
    description: z.string().max(2000).optional(),
    destination: z.string().min(1, 'Destination is required').max(255),
    startDate: z.string().min(1, 'Start date is required'),
    endDate: z.string().min(1, 'End date is required'),
    price: z
      .string()
      .min(1, 'Price is required')
      .refine((v) => Number(v) >= 0, 'Price cannot be negative'),
    maxParticipants: z
      .string()
      .min(1, 'Required')
      .refine((v) => Number.isInteger(Number(v)) && Number(v) >= 1, 'Must allow at least 1 participant'),
  })
  .refine((values) => values.endDate >= values.startDate, {
    message: 'End date must be on or after the start date',
    path: ['endDate'],
  })

type CreateTourValues = z.infer<typeof createTourSchema>

export function CreateTourPage() {
  const navigate = useNavigate()
  const createTour = useCreateTourMutation()

  const form = useForm<CreateTourValues>({
    resolver: zodResolver(createTourSchema),
    defaultValues: {
      title: '',
      description: '',
      destination: '',
      startDate: '',
      endDate: '',
      price: '',
      maxParticipants: '1',
    },
  })

  async function onSubmit(values: CreateTourValues) {
    try {
      const tour = await createTour.mutateAsync({
        ...values,
        price: Number(values.price),
        maxParticipants: Number(values.maxParticipants),
      })
      navigate(`/tours/${tour.id}`)
    } catch (err) {
      const message = isAxiosError(err) ? (err.response?.data as { message?: string })?.message : undefined
      form.setError('root', { message: message ?? 'Could not create tour. Please try again.' })
    }
  }

  return (
    <AppLayout>
      <h1 className="mb-6 text-2xl font-semibold text-slate-900 dark:text-slate-100">Create a Tour</h1>
      <Card className="max-w-xl">
        <CardContent className="px-6">
          <Form {...form}>
            <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-4">
              <FormField
                control={form.control}
                name="title"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Title</FormLabel>
                    <FormControl>
                      <Input {...field} />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />
              <FormField
                control={form.control}
                name="destination"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Destination</FormLabel>
                    <FormControl>
                      <Input {...field} />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />
              <FormField
                control={form.control}
                name="description"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Description</FormLabel>
                    <FormControl>
                      <Textarea rows={4} {...field} />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />
              <div className="grid grid-cols-2 gap-4">
                <FormField
                  control={form.control}
                  name="startDate"
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel>Start date</FormLabel>
                      <FormControl>
                        <Input type="date" {...field} />
                      </FormControl>
                      <FormMessage />
                    </FormItem>
                  )}
                />
                <FormField
                  control={form.control}
                  name="endDate"
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel>End date</FormLabel>
                      <FormControl>
                        <Input type="date" {...field} />
                      </FormControl>
                      <FormMessage />
                    </FormItem>
                  )}
                />
              </div>
              <div className="grid grid-cols-2 gap-4">
                <FormField
                  control={form.control}
                  name="price"
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel>Price (₹)</FormLabel>
                      <FormControl>
                        <Input type="number" min={0} step="0.01" {...field} />
                      </FormControl>
                      <FormMessage />
                    </FormItem>
                  )}
                />
                <FormField
                  control={form.control}
                  name="maxParticipants"
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel>Max participants</FormLabel>
                      <FormControl>
                        <Input type="number" min={1} {...field} />
                      </FormControl>
                      <FormMessage />
                    </FormItem>
                  )}
                />
              </div>

              {form.formState.errors.root && (
                <p className="text-sm text-red-600 dark:text-red-400">{form.formState.errors.root.message}</p>
              )}

              <Button
                type="submit"
                disabled={createTour.isPending}
                className="transition-all duration-200 hover:scale-[1.02] active:scale-95"
              >
                {createTour.isPending ? 'Creating…' : 'Create tour (as draft)'}
              </Button>
            </form>
          </Form>
        </CardContent>
      </Card>
    </AppLayout>
  )
}
