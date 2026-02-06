<template>
  <header class="app-header">
    <div class="header-left">
      <button @click="$emit('toggle-sidebar')" class="menu-toggle">
        ☰
      </button>
      <div class="header-title">
        <h1>{{ currentTitle }}</h1>
        <p class="header-subtitle" v-if="currentSubtitle">{{ currentSubtitle }}</p>
      </div>
    </div>
    
    <div class="header-right">
      <!-- Search -->
      <div class="search-box">
        <input 
          type="text" 
          v-model="searchQuery" 
          placeholder="Rechercher..." 
          @keyup.enter="performSearch"
        />
        <button @click="performSearch" class="search-btn">🔍</button>
      </div>
      
      <!-- Notifications -->
      <div class="notifications-dropdown">
        <button @click="toggleNotifications" class="notifications-btn" :class="{ 'has-notifications': unreadNotifications > 0 }">
          🔔
          <span v-if="unreadNotifications > 0" class="notification-count">{{ unreadNotifications }}</span>
        </button>
        <div v-if="showNotifications" class="notifications-panel">
          <div class="notifications-header">
            <h3>Notifications</h3>
            <button @click="markAllAsRead" class="mark-read-btn">Tout marquer comme lu</button>
          </div>
          <div class="notifications-list">
            <div v-for="notification in notifications" :key="notification.id" 
                 :class="['notification-item', { unread: !notification.read }]">
              <div class="notification-icon">{{ notification.icon }}</div>
              <div class="notification-content">
                <div class="notification-title">{{ notification.title }}</div>
                <div class="notification-time">{{ notification.time }}</div>
              </div>
            </div>
            <div v-if="notifications.length === 0" class="no-notifications">
              Aucune notification
            </div>
          </div>
        </div>
      </div>
      
      <!-- Quick Actions -->
      <div class="quick-actions">
        <button @click="quickNourrir" class="quick-btn" title="Nourrir rapidement">
          🍽️
        </button>
        <button @click="quickVendre" class="quick-btn" title="Vendre un poisson">
          💰
        </button>
        <button @click="quickAjouter" class="quick-btn primary" title="Ajouter rapidement">
          +
        </button>
      </div>
      
      <!-- User Menu -->
      <div class="user-menu">
        <button @click="toggleUserMenu" class="user-btn">
          <div class="user-avatar-small">{{ userInitials }}</div>
          <span class="user-name">{{ userName }}</span>
          <span class="dropdown-arrow">▼</span>
        </button>
        <div v-if="showUserMenu" class="user-menu-panel">
          <router-link to="/parametres" class="menu-item">⚙️ Paramètres</router-link>
          <router-link to="/profile" class="menu-item">👤 Profil</router-link>
          <div class="menu-divider"></div>
          <button @click="logout" class="menu-item logout">🚪 Déconnexion</button>
        </div>
      </div>
    </div>
  </header>
</template>

<script>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'

export default {
  name: 'Header',
  emits: ['toggle-sidebar'],
  setup() {
    const route = useRoute()
    const router = useRouter()
    
    // État
    const searchQuery = ref('')
    const showNotifications = ref(false)
    const showUserMenu = ref(false)
    const unreadNotifications = ref(3)
    
    // Données
    const notifications = ref([
      { id: 1, title: 'Poissons affamés', message: '5 poissons nécessitent d\'être nourris', icon: '🍽️', time: 'Il y a 2h', read: false },
      { id: 2, title: 'Stock bas', message: 'Stock de larves presque épuisé', icon: '⚠️', time: 'Il y a 1 jour', read: false },
      { id: 3, title: 'Poisson prêt à vendre', message: 'Tilapia-001 atteint 95% de son poids max', icon: '💰', time: 'Il y a 2 jours', read: true },
      { id: 4, title: 'Bénéfice réalisé', message: 'Vente de 3 poissons pour 150 000 MGA', icon: '🎉', time: 'Il y a 3 jours', read: true }
    ])
    
    const userName = ref('Admin Pisciculture')
    const userInitials = computed(() => {
      return userName.value
        .split(' ')
        .map(n => n[0])
        .join('')
        .toUpperCase()
    })
    
    // Computed
    const currentTitle = computed(() => {
      return route.meta?.title || 'Système Pisciculture'
    })
    
    const currentSubtitle = computed(() => {
      const path = route.path
      if (path.includes('poissons')) return 'Gestion des poissons'
      if (path.includes('bassins')) return 'Gestion des bassins'
      if (path.includes('nourrissage')) return 'Distribution de nourriture'
      if (path.includes('aliments')) return 'Stocks et nutrition'
      return ''
    })
    
    // Méthodes
    const performSearch = () => {
      if (searchQuery.value.trim()) {
        router.push({
          path: '/poissons',
          query: { search: searchQuery.value }
        })
        searchQuery.value = ''
      }
    }
    
    const toggleNotifications = () => {
      showNotifications.value = !showNotifications.value
      showUserMenu.value = false
    }
    
    const toggleUserMenu = () => {
      showUserMenu.value = !showUserMenu.value
      showNotifications.value = false
    }
    
    const markAllAsRead = () => {
      notifications.value.forEach(n => n.read = true)
      unreadNotifications.value = 0
    }
    
    const quickNourrir = () => {
      router.push('/nourrissage')
    }
    
    const quickVendre = () => {
      // Trouver un poisson prêt à vendre
      router.push('/poissons?filter=pret-a-vendre')
    }
    
    const quickAjouter = () => {
      router.push('/poissons/nouveau')
    }
    
    const logout = () => {
      if (confirm('Voulez-vous vraiment vous déconnecter ?')) {
        localStorage.removeItem('token')
        router.push('/auth/login')
      }
    }
    
    // Fermer les menus en cliquant ailleurs
    const closeMenus = (event) => {
      if (!event.target.closest('.notifications-dropdown')) {
        showNotifications.value = false
      }
      if (!event.target.closest('.user-menu')) {
        showUserMenu.value = false
      }
    }
    
    // Écouter les clics pour fermer les menus
    onMounted(() => {
      document.addEventListener('click', closeMenus)
    })
    
    onUnmounted(() => {
      document.removeEventListener('click', closeMenus)
    })
    
    return {
      searchQuery,
      showNotifications,
      showUserMenu,
      unreadNotifications,
      notifications,
      userName,
      userInitials,
      currentTitle,
      currentSubtitle,
      performSearch,
      toggleNotifications,
      toggleUserMenu,
      markAllAsRead,
      quickNourrir,
      quickVendre,
      quickAjouter,
      logout
    }
  }
}
</script>

<style scoped>
.app-header {
  background: white;
  padding: 0 20px;
  border-bottom: 1px solid #e2e8f0;
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 70px;
  position: sticky;
  top: 0;
  z-index: 99;
  box-shadow: 0 2px 4px rgba(0,0,0,0.05);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 15px;
}

.menu-toggle {
  background: none;
  border: none;
  font-size: 24px;
  cursor: pointer;
  color: #4a5568;
  width: 40px;
  height: 40px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background 0.2s;
}

.menu-toggle:hover {
  background: #f7fafc;
}

.header-title h1 {
  margin: 0;
  font-size: 20px;
  color: #2d3748;
  font-weight: 600;
}

.header-subtitle {
  margin: 0;
  font-size: 14px;
  color: #718096;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 20px;
}

.search-box {
  position: relative;
  display: flex;
  align-items: center;
}

.search-box input {
  padding: 10px 15px;
  padding-right: 40px;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  width: 250px;
  font-size: 14px;
  transition: border-color 0.2s;
}

.search-box input:focus {
  outline: none;
  border-color: #4299e1;
  box-shadow: 0 0 0 3px rgba(66, 153, 225, 0.1);
}

.search-btn {
  position: absolute;
  right: 10px;
  background: none;
  border: none;
  color: #718096;
  cursor: pointer;
}

.notifications-dropdown {
  position: relative;
}

.notifications-btn {
  background: none;
  border: none;
  font-size: 20px;
  cursor: pointer;
  color: #4a5568;
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  transition: background 0.2s;
}

.notifications-btn:hover {
  background: #f7fafc;
}

.notifications-btn.has-notifications {
  color: #ed8936;
}

.notification-count {
  position: absolute;
  top: -5px;
  right: -5px;
  background: #f56565;
  color: white;
  font-size: 12px;
  font-weight: bold;
  width: 18px;
  height: 18px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.notifications-panel {
  position: absolute;
  top: 50px;
  right: 0;
  background: white;
  border-radius: 8px;
  box-shadow: 0 10px 15px -3px rgba(0,0,0,0.1), 0 4px 6px -2px rgba(0,0,0,0.05);
  width: 350px;
  z-index: 1000;
  animation: slideDown 0.2s ease;
}

@keyframes slideDown {
  from {
    opacity: 0;
    transform: translateY(-10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.notifications-header {
  padding: 15px 20px;
  border-bottom: 1px solid #e2e8f0;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.notifications-header h3 {
  margin: 0;
  font-size: 16px;
  color: #2d3748;
}

.mark-read-btn {
  background: none;
  border: none;
  color: #4299e1;
  cursor: pointer;
  font-size: 12px;
}

.mark-read-btn:hover {
  text-decoration: underline;
}

.notifications-list {
  max-height: 400px;
  overflow-y: auto;
}

.notification-item {
  padding: 15px 20px;
  display: flex;
  align-items: flex-start;
  gap: 15px;
  border-bottom: 1px solid #f7fafc;
  transition: background 0.2s;
}

.notification-item:hover {
  background: #f7fafc;
}

.notification-item.unread {
  background: #ebf8ff;
}

.notification-icon {
  font-size: 20px;
  margin-top: 2px;
}

.notification-content {
  flex: 1;
}

.notification-title {
  font-weight: 600;
  color: #2d3748;
  margin-bottom: 5px;
}

.notification-time {
  font-size: 12px;
  color: #718096;
}

.no-notifications {
  padding: 30px 20px;
  text-align: center;
  color: #718096;
}

.quick-actions {
  display: flex;
  gap: 10px;
  align-items: center;
}

.quick-btn {
  background: #f7fafc;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  font-size: 18px;
  transition: all 0.2s;
}

.quick-btn:hover {
  background: #edf2f7;
  transform: translateY(-2px);
}

.quick-btn.primary {
  background: #4299e1;
  color: white;
  border-color: #4299e1;
}

.quick-btn.primary:hover {
  background: #3182ce;
}

.user-menu {
  position: relative;
}

.user-btn {
  display: flex;
  align-items: center;
  gap: 10px;
  background: none;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  padding: 8px 12px;
  cursor: pointer;
  transition: background 0.2s;
}

.user-btn:hover {
  background: #f7fafc;
}

.user-avatar-small {
  width: 32px;
  height: 32px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-weight: bold;
  font-size: 14px;
}

.user-name {
  font-size: 14px;
  font-weight: 600;
  color: #2d3748;
}

.dropdown-arrow {
  font-size: 12px;
  color: #718096;
}

.user-menu-panel {
  position: absolute;
  top: 50px;
  right: 0;
  background: white;
  border-radius: 8px;
  box-shadow: 0 10px 15px -3px rgba(0,0,0,0.1), 0 4px 6px -2px rgba(0,0,0,0.05);
  width: 200px;
  z-index: 1000;
  animation: slideDown 0.2s ease;
}

.menu-item {
  display: block;
  padding: 12px 20px;
  color: #4a5568;
  text-decoration: none;
  transition: background 0.2s;
  font-size: 14px;
}

.menu-item:hover {
  background: #f7fafc;
  color: #2d3748;
}

.menu-item.logout {
  color: #f56565;
}

.menu-divider {
  height: 1px;
  background: #e2e8f0;
  margin: 5px 0;
}

@media (max-width: 1024px) {
  .search-box input {
    width: 200px;
  }
}

@media (max-width: 768px) {
  .app-header {
    padding: 0 15px;
  }
  
  .header-title h1 {
    font-size: 18px;
  }
  
  .search-box input {
    width: 150px;
  }
  
  .quick-actions {
    display: none;
  }
  
  .user-name {
    display: none;
  }
  
  .user-btn {
    padding: 8px;
  }
}

@media (max-width: 480px) {
  .search-box {
    display: none;
  }
  
  .notifications-dropdown {
    display: none;
  }
}
</style>

