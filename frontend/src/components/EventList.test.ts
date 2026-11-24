import { describe, it, expect, vi } from 'vitest'
import { mount } from '@vue/test-utils'

import EventList from './EventList.vue'
import type { Event } from '../api/events'

const normalize = (text: string) => text.replace(/\u00A0/g, ' ')

describe('EventList', () => {
  it('renders events when loaded', async () => {
    const mockEvents: Event[] = [
      { id: 1, name: 'Rock Fest 2025', venue: 'Main Stadium', date: '2025-06-15', availableTickets: 5000, price: 50 },
      { id: 2, name: 'Jazz Evening', venue: 'Blue Note Club', date: '2025-07-20', availableTickets: 200, price: 129.99 },
    ]

    const wrapper = mount(EventList, {
      props: { events: mockEvents },
    })

    expect(wrapper.text()).toContain('Rock Fest 2025')
    expect(wrapper.text()).toContain('Main Stadium')
    expect(wrapper.text()).toContain('Jazz Evening')
    expect(wrapper.text()).toContain('Blue Note Club')
  })

  it('renders the formatted PLN price for each event', async () => {
    const mockEvents: Event[] = [
      { id: 1, name: 'Rock Fest 2025', venue: 'Main Stadium', date: '2025-06-15', availableTickets: 5000, price: 50 },
      { id: 2, name: 'Jazz Evening', venue: 'Blue Note Club', date: '2025-07-20', availableTickets: 200, price: 129.99 },
    ]

    const wrapper = mount(EventList, {
      props: { events: mockEvents },
    })

    const text = normalize(wrapper.text())
    expect(text).toContain('50,00 zł')
    expect(text).toContain('129,99 zł')
  })

  it('renders zero price as 0,00 zł', async () => {
    const mockEvents: Event[] = [
      { id: 1, name: 'Free Festival', venue: 'City Square', date: '2025-08-01', availableTickets: 1000, price: 0 },
    ]

    const wrapper = mount(EventList, {
      props: { events: mockEvents },
    })

    expect(normalize(wrapper.text())).toContain('0,00 zł')
  })

  it('renders empty state when no events', async () => {
    const wrapper = mount(EventList, {
      props: { events: [] },
    })

    expect(wrapper.text()).toContain('No events available')
  })

  it('renders error state when fetch fails', async () => {
    const wrapper = mount(EventList, {
      props: { error: 'Network error' },
    })

    expect(wrapper.text()).toContain('Error')
    expect(wrapper.text()).toContain('Network error')
  })

  it('shows retry button on error', async () => {
    const wrapper = mount(EventList, {
      props: { error: 'Network error' },
    })

    const retryButton = wrapper.find('button')
    expect(retryButton.exists()).toBe(true)
    expect(retryButton.text()).toBe('Retry')
  })

  it('emits retry event when retry button is clicked', async () => {
    const wrapper = mount(EventList, {
      props: { error: 'Network error' },
    })

    await wrapper.find('button').trigger('click')

    expect(wrapper.emitted('retry')).toBeTruthy()
  })
})
