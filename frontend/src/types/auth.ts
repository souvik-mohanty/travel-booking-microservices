// Every role a signed-in user's JWT might carry. ADMIN is never granted by
// self-registration (identity-service's RegisterRequest doesn't accept it at
// all) -- see SelfRegisterableRole below for what a client can actually
// request at signup.
export type UserRole = 'TOURIST' | 'BUSINESS' | 'ADMIN'

// Mirrors identity-service's RegisterRequest @Pattern allowlist -- a client
// can only ever request one of these two at signup (see AuthService.register).
export type SelfRegisterableRole = Exclude<UserRole, 'ADMIN'>

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
  role: SelfRegisterableRole
}

export interface AuthUser {
  userId: string
  email: string
  // Widened from the raw JWT string claim -- Google OAuth2 sign-ins predate
  // this feature and always carry "TOURIST" today, but treat anything
  // unrecognized as TOURIST rather than crashing role-gated UI.
  role: UserRole
}
