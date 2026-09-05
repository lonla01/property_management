import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import { VitePWA } from 'vite-plugin-pwa'

export default defineConfig({
  plugins: [
    react(),
    VitePWA({
      registerType: 'autoUpdate',
      includeAssets: ['favicon.ico', 'icons/icon-192.png', 'icons/icon-512.png'],
      manifest: {
        name: 'Gestion Locative',
        short_name: 'GestionLocative',
        description: 'Gérez vos biens locatifs et encaissez vos loyers via Mobile Money',
        theme_color: '#0f172a',
        background_color: '#0f172a',
        display: 'standalone',
        start_url: '/',
        icons: [
          { src: '/icons/icon-192.png', sizes: '192x192', type: 'image/png' },
          { src: '/icons/icon-512.png', sizes: '512x512', type: 'image/png' }
        ]
      },
      workbox: {
        // Consultation basique hors-ligne: on met en cache les pages déjà visitées,
        // pas les données dynamiques (paiements) qui doivent rester à jour.
        runtimeCaching: [
          {
            urlPattern: /^https:\/\/.*\/api\/(biens|locataires|baux)(\?.*)?$/,
            handler: 'NetworkFirst',
            options: { cacheName: 'api-cache-lecture-seule', networkTimeoutSeconds: 5 }
          }
        ]
      }
    })
  ],
  server: {
    port: 5173
  }
})
