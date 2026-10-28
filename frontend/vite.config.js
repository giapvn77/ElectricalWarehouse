import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

// In development the API runs on :8090; the proxy avoids CORS and keeps API_BASE_URL relative.
export default defineConfig({
  plugins: [react()],
  server: {
    port: 5180,
    // Fail instead of silently moving to another port (5173/8080 are used by another project here).
    strictPort: true,
    proxy: {
      '/api': 'http://localhost:8090',
      '/uploads': 'http://localhost:8090',
    },
  },
});
