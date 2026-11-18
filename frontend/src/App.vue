<script setup lang="ts">
import { onMounted, ref } from 'vue'
import EventList from './components/EventList.vue'
import { useEventList } from './composables/useEventList'

const { events, loading, error, loadEvents } = useEventList()

onMounted(() => {
  loadEvents()
})
</script>

<template>
  <div id="app">
    <h1>Ticket Booking</h1>
    <EventList :events="events" :loading="loading" :error="error" @retry="loadEvents" />
  </div>
</template>

<style>
#app {
  font-family: Arial, sans-serif;
  padding: 2rem;
  max-width: 800px;
  margin: 0 auto;
}

h1 {
  text-align: center;
  margin-bottom: 2rem;
}
</style>
