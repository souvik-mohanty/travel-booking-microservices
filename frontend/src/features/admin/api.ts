import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { apiClient } from '@/lib/apiClient'
import type { AdminUser } from '@/types/adminUser'

// All of these are gated to the ADMIN role by identity-service.
async function getUsers(role?: string): Promise<AdminUser[]> {
  const { data } = await apiClient.get<AdminUser[]>('/api/admin/users', { params: role ? { role } : undefined })
  return data
}

async function disableUser(id: string): Promise<AdminUser> {
  const { data } = await apiClient.patch<AdminUser>(`/api/admin/users/${id}/disable`)
  return data
}

async function enableUser(id: string): Promise<AdminUser> {
  const { data } = await apiClient.patch<AdminUser>(`/api/admin/users/${id}/enable`)
  return data
}

export const adminUserKeys = {
  all: ['admin-users'] as const,
  byRole: (role?: string) => ['admin-users', role ?? 'ALL'] as const,
}

export function useAdminUsersQuery(role?: string) {
  return useQuery({ queryKey: adminUserKeys.byRole(role), queryFn: () => getUsers(role) })
}

function useUserMutation(mutationFn: (id: string) => Promise<AdminUser>) {
  const queryClient = useQueryClient()
  return useMutation({ mutationFn, onSuccess: () => queryClient.invalidateQueries({ queryKey: adminUserKeys.all }) })
}

export const useDisableUserMutation = () => useUserMutation(disableUser)
export const useEnableUserMutation = () => useUserMutation(enableUser)
