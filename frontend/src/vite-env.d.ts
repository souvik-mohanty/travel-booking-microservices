/// <reference types="vite/client" />

interface ImportMetaEnv {
  // Every backend service is reached through the api-gateway's single base
  // URL -- see src/lib/apiClient.ts for why this replaced the old
  // one-axios-instance-per-service setup.
  readonly VITE_API_BASE_URL: string
}

interface ImportMeta {
  readonly env: ImportMetaEnv
}
