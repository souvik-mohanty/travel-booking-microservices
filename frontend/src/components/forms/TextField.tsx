import type { Control, FieldPath, FieldValues } from 'react-hook-form'
import { FormControl, FormField, FormItem, FormLabel, FormMessage } from '@/components/ui/form'
import { Input } from '@/components/ui/input'
import { Textarea } from '@/components/ui/textarea'

interface TextFieldProps<T extends FieldValues> {
  control: Control<T>
  name: FieldPath<T>
  label: string
  type?: string
  multiline?: boolean
  min?: number
  step?: string
  placeholder?: string
}

// One labelled input wired to react-hook-form, with its validation message.
export function TextField<T extends FieldValues>({
  control,
  name,
  label,
  type = 'text',
  multiline = false,
  min,
  step,
  placeholder,
}: TextFieldProps<T>) {
  return (
    <FormField
      control={control}
      name={name}
      render={({ field }) => {
        const { value, ...rest } = field
        const text = (value ?? '') as string
        return (
          <FormItem>
            <FormLabel>{label}</FormLabel>
            <FormControl>
              {multiline ? (
                <Textarea rows={3} placeholder={placeholder} {...rest} value={text} />
              ) : (
                <Input type={type} min={min} step={step} placeholder={placeholder} {...rest} value={text} />
              )}
            </FormControl>
            <FormMessage />
          </FormItem>
        )
      }}
    />
  )
}
