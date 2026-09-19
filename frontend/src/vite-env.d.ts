/// <reference types="vite/client" />

interface ImportMetaEnv {
  // Every backend service is reached through the api-gateway's single base
  // URL -- see src/lib/apiClient.ts for why this replaced the old
  // one-axios-instance-per-service setup.
  readonly VITE_API_BASE_URL: string
  // Comma-separated public URLs of every backend service, pinged once on load
  // to wake sleeping free-tier services in parallel -- see src/lib/warmUp.ts.
  readonly VITE_WARMUP_URLS?: string
}

interface ImportMeta {
  readonly env: ImportMetaEnv
}
