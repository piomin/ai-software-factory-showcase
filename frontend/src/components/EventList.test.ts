import { describe, it, expect, vi } from 'vitest'
import { mount } from '@vue/test-utils'

import EventList from './EventList.vue'

describe('EventList', () => {
  it('renders events when loaded', async () => {
    const mockEvents = [
      { id: 1, name: 'Rock Fest 2025', venue: 'Main Stadium', date: '2025-06-15', availableTickets: 5000 },
      { id: 2, name: 'Jazz Evening', venue: 'Blue Note Club', date: '2025-07-20', availableTickets: 200 },
    ]

    const wrapper = mount(EventList, {
      props: { events: mockEvents },
    })

    expect(wrapper.text()).toContain('Rock Fest 2025')
    expect(wrapper.text()).toContain('Main Stadium')
    expect(wrapper.text()).toContain('Jazz Evening')
    expect(wrapper.text()).toContain('Blue Note Club')
  })

  it('renders error state when fetch fails', async () => {
    const wrapper = mount(EventList, {
      props: { error: 'Network error' },
    })

    expect(wrapper.text()).toContain('Error')
    expect(wrapper.text()).toContain('Network error')
  })
})
