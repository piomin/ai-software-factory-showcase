import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  server: {
    proxy: {
      '/api/events': 'http://localhost:8081',
      '/api/bookings': 'http://localhost:8082',
    },
  },
  test: {
    environment: 'jsdom',
  },
})
