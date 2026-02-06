<template>
  <router-view />
</template>

<script>
export default {
  name: 'App',
  mounted() {
    // Écouter les changements de route pour analytics
    this.$watch(
      () => this.$route.path,
      (newPath) => {
        console.log('Route changed to:', newPath)
        // Ici vous pourriez ajouter Google Analytics
        // if (window.gtag) {
        //   window.gtag('config', 'GA_MEASUREMENT_ID', { page_path: newPath })
        // }
      }
    )
    
    // Vérifier l'authentification au chargement
    this.checkAuth()
  },
  methods: {
    checkAuth() {
      const token = localStorage.getItem('token')
      const requiresAuth = this.$route.meta.requiresAuth
      
      if (requiresAuth && !token) {
        this.$router.push('/auth/login')
      }
    }
  }
}
</script>

<style>
/* Styles globaux */
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
  line-height: 1.6;
  color: #333;
  background-color: #f5f7fa;
}

#app {
  min-height: 100vh;
}

/* Scrollbar personnalisée */
::-webkit-scrollbar {
  width: 8px;
  height: 8px;
}

::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 4px;
}

::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 4px;
}

::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}

/* Transitions de page */
.page-enter-active,
.page-leave-active {
  transition: opacity 0.3s ease;
}

.page-enter-from,
.page-leave-to {
  opacity: 0;
}

/* Utilitaires */
.text-center { text-align: center; }
.text-right { text-align: right; }
.text-left { text-align: left; }

.mt-1 { margin-top: 0.25rem; }
.mt-2 { margin-top: 0.5rem; }
.mt-3 { margin-top: 1rem; }
.mt-4 { margin-top: 1.5rem; }
.mt-5 { margin-top: 3rem; }

.mb-1 { margin-bottom: 0.25rem; }
.mb-2 { margin-bottom: 0.5rem; }
.mb-3 { margin-bottom: 1rem; }
.mb-4 { margin-bottom: 1.5rem; }
.mb-5 { margin-bottom: 3rem; }

.p-1 { padding: 0.25rem; }
.p-2 { padding: 0.5rem; }
.p-3 { padding: 1rem; }
.p-4 { padding: 1.5rem; }
.p-5 { padding: 3rem; }

.flex { display: flex; }
.flex-col { flex-direction: column; }
.items-center { align-items: center; }
.justify-center { justify-content: center; }
.justify-between { justify-content: space-between; }
.gap-1 { gap: 0.25rem; }
.gap-2 { gap: 0.5rem; }
.gap-3 { gap: 1rem; }
.gap-4 { gap: 1.5rem; }
.gap-5 { gap: 3rem; }

/* Classes de couleur */
.text-primary { color: #4299e1; }
.text-success { color: #48bb78; }
.text-warning { color: #ed8936; }
.text-danger { color: #f56565; }
.text-muted { color: #718096; }

.bg-primary { background-color: #4299e1; }
.bg-success { background-color: #48bb78; }
.bg-warning { background-color: #ed8936; }
.bg-danger { background-color: #f56565; }
.bg-light { background-color: #f7fafc; }

/* Classes de taille */
.text-sm { font-size: 0.875rem; }
.text-base { font-size: 1rem; }
.text-lg { font-size: 1.125rem; }
.text-xl { font-size: 1.25rem; }
.text-2xl { font-size: 1.5rem; }

/* Classes de bordure */
.rounded { border-radius: 0.375rem; }
.rounded-lg { border-radius: 0.5rem; }
.rounded-xl { border-radius: 0.75rem; }

.shadow { box-shadow: 0 1px 3px 0 rgba(0, 0, 0, 0.1), 0 1px 2px 0 rgba(0, 0, 0, 0.06); }
.shadow-md { box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06); }
.shadow-lg { box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.1), 0 4px 6px -2px rgba(0, 0, 0, 0.05); }

/* Responsive helpers */
@media (max-width: 640px) {
  .sm\:hidden { display: none; }
}

@media (max-width: 768px) {
  .md\:hidden { display: none; }
}

@media (max-width: 1024px) {
  .lg\:hidden { display: none; }
}

/* Animations */
@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

@keyframes slideInUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}

.fade-in {
  animation: fadeIn 0.3s ease-in-out;
}

.slide-in-up {
  animation: slideInUp 0.3s ease-out;
}

.pulse {
  animation: pulse 2s cubic-bezier(0.4, 0, 0.6, 1) infinite;
}
</style>

