import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import tailwindcss from '@tailwindcss/vite'

// Le build écrit directement dans les ressources statiques de Spring Boot :
// le même serveur sert l'API et les pages (pas de Node en production).
export default defineConfig({
  plugins: [react(), tailwindcss()],
  build: {
    outDir: '../target/classes/static',
    emptyOutDir: true,
  },
  // En développement : le front tourne sur :5173 et appelle l'API Spring Boot sur :8080
  server: {
    proxy: { '/api': 'http://localhost:8080' },
  },
})
