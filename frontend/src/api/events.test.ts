import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'

import { fetchEvents } from './events'

describe('fetchEvents', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('returns list of events on successful response', async () => {
    const mockEvents = [
      { id: 1, name: 'Rock Fest 2025', venue: 'Main Stadium', date: '2025-06-15', availableTickets: 5000 },
      { id: 2, name: 'Jazz Evening', venue: 'Blue Note Club', date: '2025-07-20', availableTickets: 200 },
    ]

    vi.spyOn(globalThis, 'fetch').mockResolvedValue({
      ok: true,
      json: async () => mockEvents,
    } as Response)

    const result = await fetchEvents()

    expect(result).toEqual(mockEvents)
    expect(globalThis.fetch).toHaveBeenCalledWith('/api/events')
  })

  it('handles empty list as valid result', async () => {
    vi.spyOn(globalThis, 'fetch').mockResolvedValue({
      ok: true,
      json: async () => [],
    } as Response)

    const result = await fetchEvents()

    expect(result).toEqual([])
  })

  it('throws error on non-OK response', async () => {
    vi.spyOn(globalThis, 'fetch').mockResolvedValue({
      ok: false,
      status: 500,
      statusText: 'Internal Server Error',
    } as Response)

    await expect(fetchEvents()).rejects.toThrow('Failed to fetch events')
  })

  it('throws error on network failure', async () => {
    vi.spyOn(globalThis, 'fetch').mockRejectedValue(new Error('Network error'))

    await expect(fetchEvents()).rejects.toThrow('Network error')
  })
})
