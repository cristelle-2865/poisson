<template>
  <aside class="sidebar" :class="{ 'collapsed': collapsed }">
    <div class="sidebar-header">
      <div class="logo">
        <span class="logo-icon">🐟</span>
        <span class="logo-text" v-if="!collapsed">Pisciculture</span>
      </div>
      <button @click="toggleCollapse" class="collapse-btn" :title="collapsed ? 'Déplier' : 'Replier'">
        {{ collapsed ? '→' : '←' }}
      </button>
    </div>
    
    <nav class="sidebar-nav">
      <div class="nav-section" v-for="section in navSections" :key="section.title">
        <h3 v-if="section.title && !collapsed" class="section-title">{{ section.title }}</h3>
        <router-link 
          v-for="item in section.items" 
          :key="item.name"
          :to="{ name: item.name }"
          :class="['nav-item', { active: isActive(item) }]"
          :title="collapsed ? item.label : ''"
        >
          <span class="nav-icon">{{ item.icon }}</span>
          <span class="nav-label" v-if="!collapsed">{{ item.label }}</span>
          <span v-if="item.badge && !collapsed" class="nav-badge">{{ item.badge }}</span>
        </router-link>
      </div>
    </nav>
    
    <div class="sidebar-footer" v-if="!collapsed">
      <div class="user-info">
        <div class="user-avatar">{{ userInitials }}</div>
        <div class="user-details">
          <div class="user-name">{{ userName }}</div>
          <div class="user-role">{{ userRole }}</div>
        </div>
      </div>
      <button @click="logout" class="logout-btn">
        <span class="logout-icon">🚪</span>
        <span>Déconnexion</span>
      </button>
    </div>
    <div v-else class="sidebar-footer-collapsed">
      <button @click="logout" class="logout-btn-collapsed" title="Déconnexion">
        🚪
      </button>
    </div>
  </aside>
</template>

<script>
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'

export default {
  name: 'Sidebar',
  setup() {
    const route = useRoute()
    const router = useRouter()
    const collapsed = ref(false)
    
    // Données utilisateur (à remplacer par votre store)
    const userName = ref('Admin Pisciculture')
    const userRole = ref('Administrateur')
    const userInitials = computed(() => {
      return userName.value
        .split(' ')
        .map(n => n[0])
        .join('')
        .toUpperCase()
    })
    
    // Structure de navigation
    const navSections = [
      {
        title: 'Général',
        items: [
          { name: 'Dashboard', label: 'Dashboard', icon: '🏠' }
        ]
      },
      {
        title: 'Gestion',
        items: [
          { name: 'Poissons', label: 'Poissons', icon: '🐟', badge: 42 },
          { name: 'Bassins', label: 'Bassins', icon: '🏊', badge: 5 },
          { name: 'Aliments', label: 'Aliments', icon: '📦', badge: 12 }
        ]
      },
      {
        title: 'Opérations',
        items: [
          { name: 'Nourrissage', label: 'Nourrissage', icon: '🍽️' },
          { name: 'Plats', label: 'Plats', icon: '🥘' }
        ]
      },
      {
        title: 'Analyses',
        items: [
          { name: 'Historique', label: 'Historique', icon: '📜' },
          { name: 'Benefices', label: 'Bénéfices', icon: '💰' },
          { name: 'Rapports', label: 'Rapports', icon: '📊' }
        ]
      },
      {
        title: 'Administration',
        items: [
          { name: 'Parametres', label: 'Paramètres', icon: '⚙️' }
        ]
      }
    ]
    
    const isActive = (item) => {
      return route.name === item.name
    }
    
    const toggleCollapse = () => {
      collapsed.value = !collapsed.value
    }
    
    const logout = () => {
      if (confirm('Voulez-vous vraiment vous déconnecter ?')) {
        localStorage.removeItem('token')
        router.push('/auth/login')
      }
    }
    
    return {
      collapsed,
      navSections,
      userName,
      userRole,
      userInitials,
      isActive,
      toggleCollapse,
      logout
    }
  }
}
</script>

<style scoped>
.sidebar {
  width: 250px;
  background: linear-gradient(180deg, #2d3748 0%, #1a202c 100%);
  color: white;
  display: flex;
  flex-direction: column;
  position: fixed;
  left: 0;
  top: 0;
  bottom: 0;
  z-index: 100;
  transition: width 0.3s ease;
  box-shadow: 2px 0 10px rgba(0,0,0,0.1);
}

.sidebar.collapsed {
  width: 70px;
}

.sidebar-header {
  padding: 20px;
  border-bottom: 1px solid #4a5568;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.logo {
  display: flex;
  align-items: center;
  gap: 10px;
}

.logo-icon {
  font-size: 24px;
}

.logo-text {
  font-size: 18px;
  font-weight: bold;
  color: #63b3ed;
}

.collapse-btn {
  background: #4a5568;
  border: none;
  color: white;
  width: 30px;
  height: 30px;
  border-radius: 50%;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background 0.2s;
}

.collapse-btn:hover {
  background: #5a6578;
}

.sidebar-nav {
  flex: 1;
  padding: 20px 0;
  overflow-y: auto;
}

.section-title {
  color: #a0aec0;
  font-size: 12px;
  text-transform: uppercase;
  letter-spacing: 1px;
  padding: 10px 20px;
  margin: 0;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 20px;
  color: #cbd5e0;
  text-decoration: none;
  transition: all 0.2s;
  position: relative;
}

.nav-item:hover {
  background: rgba(255,255,255,0.05);
  color: white;
}

.nav-item.active {
  background: #4299e1;
  color: white;
  border-left: 4px solid #63b3ed;
}

.nav-item.active::before {
  content: '';
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 3px;
  background: #63b3ed;
}

.nav-icon {
  font-size: 18px;
  min-width: 24px;
  text-align: center;
}

.nav-label {
  flex: 1;
  font-size: 14px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.nav-badge {
  background: #ed8936;
  color: white;
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 12px;
  font-weight: bold;
}

.sidebar-footer {
  border-top: 1px solid #4a5568;
  padding: 20px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 15px;
}

.user-avatar {
  width: 40px;
  height: 40px;
  background: #4299e1;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
  font-size: 14px;
}

.user-details {
  flex: 1;
}

.user-name {
  font-size: 14px;
  font-weight: 600;
}

.user-role {
  font-size: 12px;
  color: #a0aec0;
}

.logout-btn {
  width: 100%;
  background: #4a5568;
  border: none;
  color: white;
  padding: 10px;
  border-radius: 6px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  transition: background 0.2s;
}

.logout-btn:hover {
  background: #5a6578;
}

.logout-icon {
  font-size: 16px;
}

.sidebar-footer-collapsed {
  padding: 20px;
  text-align: center;
}

.logout-btn-collapsed {
  background: #4a5568;
  border: none;
  color: white;
  width: 40px;
  height: 40px;
  border-radius: 50%;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background 0.2s;
}

.logout-btn-collapsed:hover {
  background: #5a6578;
}

/* Responsive */
@media (max-width: 768px) {
  .sidebar {
    width: 0;
    transform: translateX(-100%);
  }
  
  .sidebar.collapsed {
    width: 0;
  }
  
  .sidebar.show-mobile {
    width: 250px;
    transform: translateX(0);
  }
}

/* Scrollbar styling */
.sidebar-nav::-webkit-scrollbar {
  width: 4px;
}

.sidebar-nav::-webkit-scrollbar-track {
  background: transparent;
}

.sidebar-nav::-webkit-scrollbar-thumb {
  background: #4a5568;
  border-radius: 2px;
}

.sidebar-nav::-webkit-scrollbar-thumb:hover {
  background: #5a6578;
}
</style>

