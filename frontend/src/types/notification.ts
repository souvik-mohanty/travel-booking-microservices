// Mirrors platform-service's NotificationResponse. No updatedAt field --
// notifications are only ever created and marked read, never edited.
export interface Notification {
  id: string
  userId: string
  title: string
  message: string
  read: boolean
  createdAt: string
}
