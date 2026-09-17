// Mirrors identity-service's AdminUserResponse record.
export interface AdminUser {
  id: string
  email: string
  firstName: string
  lastName: string | null
  role: string
  enabled: boolean
  provider: string
  createdAt: string
  updatedAt: string
}
