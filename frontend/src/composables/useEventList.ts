import { ref, computed, onMounted } from 'vue'
import { fetchEvents } from '../api/events'

export function useEventList() {
  const events = ref<Event[]>([])
  const loading = ref(true)
  const error = ref<string | null>(null)

  async function loadEvents() {
    loading.value = true
    error.value = null
    try {
      events.value = await fetchEvents()
    } catch (err) {
      error.value = err instanceof Error ? err.message : 'Unknown error'
    } finally {
      loading.value = false
    }
  }

  onMounted(loadEvents)

  return {
    events,
    loading,
    error,
    loadEvents,
  }
}

interface Event {
  id: number
  name: string
  venue: string
  date: string
  availableTickets: number
}
