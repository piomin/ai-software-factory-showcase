<script setup lang="ts">
import { computed } from 'vue'
import type { Event } from '../api/events'

interface Props {
  events?: Event[]
  loading?: boolean
  error?: string | null
}

const props = defineProps<Props>()

const emit = defineEmits<{
  (e: 'retry'): void
}>()

const hasEvents = computed(() => props.events !== undefined && props.events.length > 0)
</script>

<template>
  <div class="event-list">
    <h2>Events</h2>
    <p v-if="props.loading">Loading...</p>
    <p v-else-if="props.error">Error: {{ props.error }}</p>
    <p v-else-if="!hasEvents">No events available</p>
    <ul v-else>
      <li v-for="event in props.events" :key="event.id">
        <strong>{{ event.name }}</strong><br />
        {{ event.venue }}<br />
        {{ event.date }}<br />
        {{ event.availableTickets }} tickets available
      </li>
    </ul>
    <button v-if="props.error" @click="emit('retry')">Retry</button>
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
