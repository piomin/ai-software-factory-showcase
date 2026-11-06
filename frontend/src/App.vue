<script setup lang="ts">
import EventList from './components/EventList.vue'
</script>

<template>
  <div id="app">
    <h1>Ticket Booking</h1>
    <EventList />
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
