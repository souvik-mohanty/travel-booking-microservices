import { identityClient } from './client'
import type { AdminUser } from '../types/adminUser'

export async function getUsers(role?: string): Promise<AdminUser[]> {
  const { data } = await identityClient.get<AdminUser[]>('/api/admin/users', {
    params: role ? { role } : undefined,
  })
  return data
}

export async function getUser(id: string): Promise<AdminUser> {
  const { data } = await identityClient.get<AdminUser>(`/api/admin/users/${id}`)
  return data
}

export async function disableUser(id: string): Promise<AdminUser> {
  const { data } = await identityClient.patch<AdminUser>(`/api/admin/users/${id}/disable`)
  return data
}

export async function enableUser(id: string): Promise<AdminUser> {
  const { data } = await identityClient.patch<AdminUser>(`/api/admin/users/${id}/enable`)
  return data
}
