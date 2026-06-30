import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  define: {
    // sockjs-client expects the Node.js `global` to exist; map it to globalThis in the browser.
    global: 'globalThis',
  },
})
