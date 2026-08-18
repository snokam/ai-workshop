import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// The frontend is served by Vite on 5173 and Spring Boot runs on 8080, so calls to /api are
// proxied across. This is why the app can use plain relative URLs and why no CORS config exists
// on the Java side — as far as the browser is concerned, there is only one origin.
export default defineConfig({
  // The Snøkam navbar/footer are built for Next and import `next/link`, which reads `process.env`
  // at module load. Under plain Vite there is no `process`, so shim it to an empty env — enough for
  // those reads to resolve to undefined instead of throwing "process is not defined" on first render.
  define: {
    'process.env': {},
  },
  server: {
    proxy: {
      '/api': 'http://localhost:8080',
    },
  },
  plugins: [react()],
})
