import { useEffect, useRef, useState } from 'react'
import { useNavigate, useSearchParams } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'

// Landing point for app.oauth2.authorized-redirect-uri in identity-service.
// OAuth2AuthenticationSuccessHandler appends accessToken/refreshToken as query
// params; OAuth2AuthenticationFailureHandler appends an `error` param instead.
export function OAuth2RedirectPage() {
  const [searchParams] = useSearchParams()
  const { applySession } = useAuth()
  const navigate = useNavigate()
  const [error, setError] = useState<string | null>(null)
  const handled = useRef(false)

  useEffect(() => {
    if (handled.current) return
    handled.current = true

    const accessToken = searchParams.get('accessToken')
    const refreshToken = searchParams.get('refreshToken')
    const oauthError = searchParams.get('error')

    if (oauthError) {
      setError('Google sign-in failed. Please try again.')
      return
    }

    if (!accessToken || !refreshToken) {
      setError('Missing tokens in the sign-in redirect.')
      return
    }

    applySession({ accessToken, refreshToken })
    navigate('/tours', { replace: true })
  }, [applySession, navigate, searchParams])

  if (error) {
    return (
      <div className="flex min-h-screen flex-col items-center justify-center gap-4 bg-slate-50 dark:bg-slate-950">
        <p className="text-sm text-red-600 dark:text-red-400">{error}</p>
        <button
          onClick={() => navigate('/login', { replace: true })}
          className="text-sm font-medium text-blue-600 underline decoration-blue-300 underline-offset-2 transition-colors duration-150 hover:text-blue-700 dark:text-blue-400"
        >
          Back to sign in
        </button>
      </div>
    )
  }

  return (
    <div className="flex min-h-screen items-center justify-center bg-slate-50 dark:bg-slate-950">
      <p className="text-sm text-slate-500 dark:text-slate-400">Signing you in…</p>
    </div>
  )
}
