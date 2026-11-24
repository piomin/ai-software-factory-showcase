import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, VueWrapper } from '@vue/test-utils'
import { ref, defineComponent } from 'vue'

import { fetchEvents } from '../api/events'
import { useEventList } from './useEventList'
import type { Event } from '../api/events'

vi.mock('../api/events', () => ({
  fetchEvents: vi.fn<() => Promise<Event[]>>(),
}))

describe('useEventList', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  const createTestComponent = (setupFn: Function) => {
    return defineComponent({
      setup: () => {
        const result = setupFn()
        return { ...result }
      },
      template: '<div />',
    })
  }

  it('initial state has loading true, empty events, and no error', () => {
    const wrapper = mount(createTestComponent(() => useEventList()))

    expect((wrapper.vm as any).loading).toBe(true)
    expect((wrapper.vm as any).events).toEqual([])
    expect((wrapper.vm as any).error).toBeNull()
  })

  it('loads events successfully and updates state', async () => {
    const mockEvents: Event[] = [
      { id: 1, name: 'Rock Fest 2025', venue: 'Main Stadium', date: '2025-06-15', availableTickets: 5000, price: 50 },
    ]

    vi.mocked(fetchEvents).mockResolvedValue(mockEvents)

    const wrapper = mount(createTestComponent(() => useEventList()))

    expect((wrapper.vm as any).loading).toBe(true)
    expect((wrapper.vm as any).events).toEqual([])
    expect((wrapper.vm as any).error).toBeNull()

    await (wrapper.vm as any).loadEvents()

    expect((wrapper.vm as any).loading).toBe(false)
    expect((wrapper.vm as any).events).toEqual(mockEvents)
    expect((wrapper.vm as any).error).toBeNull()
  })

  it('handles error when fetch fails with Error', async () => {
    vi.mocked(fetchEvents).mockRejectedValue(new Error('Network error'))

    const wrapper = mount(createTestComponent(() => useEventList()))

    await (wrapper.vm as any).loadEvents()

    expect((wrapper.vm as any).loading).toBe(false)
    expect((wrapper.vm as any).events).toEqual([])
    expect((wrapper.vm as any).error).toBe('Network error')
  })

  it('handles error when fetch fails with non-Error', async () => {
    vi.mocked(fetchEvents).mockRejectedValue('Unknown failure')

    const wrapper = mount(createTestComponent(() => useEventList()))

    await (wrapper.vm as any).loadEvents()

    expect((wrapper.vm as any).loading).toBe(false)
    expect((wrapper.vm as any).events).toEqual([])
    expect((wrapper.vm as any).error).toBe('Unknown error')
  })

  it('reload clears previous error and loads data again', async () => {
    vi.mocked(fetchEvents)
      .mockRejectedValueOnce(new Error('First failure'))
      .mockResolvedValueOnce([
        { id: 1, name: 'Rock Fest 2025', venue: 'Main Stadium', date: '2025-06-15', availableTickets: 5000, price: 50 },
      ])

    const wrapper = mount(createTestComponent(() => {
      const result = useEventList()
      return result
    }))

    await new Promise(resolve => setTimeout(resolve, 0))
    expect((wrapper.vm as any).error).toBe('First failure')

    await (wrapper.vm as any).loadEvents()

    expect((wrapper.vm as any).loading).toBe(false)
    expect((wrapper.vm as any).error).toBeNull()
    expect((wrapper.vm as any).events).toEqual([
      { id: 1, name: 'Rock Fest 2025', venue: 'Main Stadium', date: '2025-06-15', availableTickets: 5000, price: 50 },
    ])
  })

  it('automatically loads events on mount', async () => {
    vi.mocked(fetchEvents).mockResolvedValue([
      { id: 1, name: 'Rock Fest 2025', venue: 'Main Stadium', date: '2025-06-15', availableTickets: 5000, price: 50 },
    ])

    const wrapper = mount(createTestComponent(() => {
      const { events, loading, error } = useEventList()
      return { events, loading, error }
    }))

    expect((wrapper.vm as any).loading).toBe(true)

    await new Promise(resolve => setTimeout(resolve, 0))

    expect((wrapper.vm as any).loading).toBe(false)
    expect((wrapper.vm as any).events).toEqual([
      { id: 1, name: 'Rock Fest 2025', venue: 'Main Stadium', date: '2025-06-15', availableTickets: 5000, price: 50 },
    ])
    expect((wrapper.vm as any).error).toBeNull()
  })
})
