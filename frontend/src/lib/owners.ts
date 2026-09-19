import type { AdminUser } from '@/types/adminUser'

// "Name (email)" for an owner id, or a shortened id if that user isn't in the
// list (e.g. the users request failed or is still loading).
export function ownerLabel(ownerId: string, users: AdminUser[] | undefined): string {
  const owner = users?.find((user) => user.id === ownerId)
  if (!owner) return `${ownerId.slice(0, 8)}…`
  const name = [owner.firstName, owner.lastName].filter(Boolean).join(' ')
  return name ? `${name} (${owner.email})` : owner.email
}
