import path from 'node:path'
import react from '@vitejs/plugin-react'
import tailwindcss from '@tailwindcss/vite'
import { defineConfig, loadEnv } from 'vite'

// https://vite.dev/config/
export default defineConfig(({ mode }) => {
  // VITE_* values are inlined into the bundle at build time, and
  // src/lib/apiClient.ts falls back to http://localhost:8080 when
  // VITE_API_BASE_URL is missing. On Vercel that silently ships a site whose
  // every API call and "Continue with Google" link points at the visitor's own
  // localhost -- so refuse to build instead.
  const env = loadEnv(mode, process.cwd(), '')
  const apiBaseUrl = env.VITE_API_BASE_URL
  if (process.env.VERCEL && (!apiBaseUrl || /localhost|127\.0\.0\.1/.test(apiBaseUrl))) {
    throw new Error(
      'VITE_API_BASE_URL is missing or points at localhost. Set it in the Vercel project ' +
        '(Settings -> Environment Variables, Production) to the gateway public URL, then redeploy.',
    )
  }

  return {
    plugins: [react(), tailwindcss()],
    resolve: {
      alias: {
        '@': path.resolve(__dirname, './src'),
      },
    },
    server: {
      // Matches app.oauth2.authorized-redirect-uri (http://localhost:3000/oauth2/redirect)
      // in identity-service's application.yml.
      port: 3000,
    },
  }
})
