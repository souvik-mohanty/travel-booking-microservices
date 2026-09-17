import react from '@vitejs/plugin-react'
import tailwindcss from '@tailwindcss/vite'
import { defineConfig } from 'vite'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react(), tailwindcss()],
  server: {
    // Matches app.oauth2.authorized-redirect-uri (http://localhost:3000/oauth2/redirect)
    // in identity-service's application.yml.
    port: 3000,
  },
})
