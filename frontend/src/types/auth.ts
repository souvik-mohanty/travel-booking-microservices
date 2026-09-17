// Mirrors identity-service's AuthResponse record (see
// backend/services/identity-service .../auth/dto/AuthResponse.java).
export interface AuthResponse {
  accessToken: string
  refreshToken: string
  userId: string
  email: string
  role: string
}

export interface LoginRequest {
  email: string
  password: string
}

export interface RegisterRequest {
  email: string
  password: string
  firstName: string
  lastName?: string
}

export interface AuthUser {
  userId: string
  email: string
  role: string
}
