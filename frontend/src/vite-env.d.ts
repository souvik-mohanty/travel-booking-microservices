/// <reference types="vite/client" />

interface ImportMetaEnv {
  readonly VITE_IDENTITY_SERVICE_URL: string
  readonly VITE_TOUR_SERVICE_URL: string
  readonly VITE_BOOKING_SERVICE_URL: string
  readonly VITE_SEARCH_SERVICE_URL: string
}

interface ImportMeta {
  readonly env: ImportMetaEnv
}
