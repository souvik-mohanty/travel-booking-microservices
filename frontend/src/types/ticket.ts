// Mirrors engagement-service's TicketResponse.
export type TicketStatus = 'OPEN' | 'ASSIGNED' | 'IN_PROGRESS' | 'ESCALATED' | 'RESOLVED' | 'CLOSED'

export interface SupportTicket {
  id: string
  userId: string
  subject: string
  description: string
  status: TicketStatus
  createdAt: string
  updatedAt: string
}

export interface CreateTicketRequest {
  subject: string
  description: string
}
