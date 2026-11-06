<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { fetchEvents } from '../api/events'

interface Event {
  id: number
  name: string
  venue: string
  date: string
  availableTickets: number
}

interface Props {
  events?: Event[]
  loading?: boolean
  error?: string | null
}

const props = defineProps<Props>()

const localEvents = ref<Event[]>([])
const localLoading = ref(true)
const localError = ref<string | null>(null)

if (props.events !== undefined) {
  localEvents.value = props.events
  localLoading.value = false
} else {
  async function loadEvents() {
    localLoading.value = true
    localError.value = null
    try {
      localEvents.value = await fetchEvents()
    } catch (err) {
      localError.value = err instanceof Error ? err.message : 'Unknown error'
    } finally {
      localLoading.value = false
    }
  }

  onMounted(loadEvents)
}

const displayEvents = computed(() => props.events !== undefined ? props.events : localEvents.value)
const displayLoading = computed(() => props.loading !== undefined ? props.loading : localLoading.value)
const displayError = computed(() => props.error !== undefined ? props.error : localError.value)
</script>

<template>
  <div class="event-list">
    <h2>Events</h2>
    <p v-if="displayLoading">Loading...</p>
    <p v-else-if="displayError">Error: {{ displayError }}</p>
    <ul v-else>
      <li v-for="event in displayEvents" :key="event.id">
        <strong>{{ event.name }}</strong><br />
        {{ event.venue }}<br />
        {{ event.date }}<br />
        {{ event.availableTickets }} tickets available
      </li>
    </ul>
  </div>
</template>

<style scoped>
.event-list {
  max-width: 600px;
  margin: 0 auto;
}

.event-list h2 {
  margin-bottom: 1rem;
}

.event-list ul {
  list-style: none;
  padding: 0;
}

.event-list li {
  margin-bottom: 1rem;
  padding: 1rem;
  border: 1px solid #ddd;
  border-radius: 4px;
}
</style>
