import { ref } from 'vue'

export interface Event {
  id: number
  name: string
  venue: string
  date: string
  availableTickets: number
}

export async function fetchEvents(): Promise<Event[]> {
  const response = await fetch('/api/events')
  if (!response.ok) {
    throw new Error('Failed to fetch events')
  }
  return response.json()
}
