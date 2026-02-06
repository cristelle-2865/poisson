<template>
  <div class="app-layout">
    <!-- Sidebar -->
    <Sidebar v-if="showSidebar" />
    
    <!-- Main Content -->
    <div class="main-content" :class="{ 'with-sidebar': showSidebar }">
      <!-- Header -->
      <Header @toggle-sidebar="toggleSidebar" />
      
      <!-- Content -->
      <main class="content-area">
        <!-- Breadcrumb -->
        <div v-if="breadcrumb.length > 0" class="breadcrumb">
          <router-link to="/">🏠</router-link>
          <span v-for="(item, index) in breadcrumb" :key="index">
            <span class="separator">/</span>
            <router-link v-if="item.path" :to="item.path">{{ item.label }}</router-link>
            <span v-else>{{ item.label }}</span>
          </span>
        </div>
        
        <!-- Router View -->
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </main>
      
      <!-- Footer -->
      <footer class="app-footer">
        <p>© 2024 Système Pisciculture - Version 1.0.0</p>
      </footer>
    </div>
    
    <!-- Notifications -->
    <div class="notifications">
      <div v-for="notification in notifications" :key="notification.id" 
           :class="['notification', notification.type]">
        {{ notification.message }}
        <button @click="removeNotification(notification.id)" class="close-btn">×</button>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, computed, watch } from 'vue'
import { useRoute } from 'vue-router'
import Sidebar from '../components/Sidebar.vue'
import Header from '../components/Header.vue'

export default {
  name: 'MainLayout',
  components: {
    Sidebar,
    Header
  },
  setup() {
    const route = useRoute()
    const showSidebar = ref(true)
    const notifications = ref([])
    
    // Breadcrumb dynamique
    const breadcrumb = computed(() => {
      const crumbs = []
      const matchedRoutes = route.matched
      
      matchedRoutes.forEach(routeRecord => {
        if (routeRecord.meta?.breadcrumb) {
          crumbs.push({
            label: routeRecord.meta.breadcrumb,
            path: routeRecord.path
          })
        }
      })
      
      return crumbs
    })
    
    const toggleSidebar = () => {
      showSidebar.value = !showSidebar.value
    }
    
    const addNotification = (message, type = 'info') => {
      const id = Date.now()
      notifications.value.push({ id, message, type })
      
      // Auto-remove after 5 seconds
      setTimeout(() => {
        removeNotification(id)
      }, 5000)
    }
    
    const removeNotification = (id) => {
      notifications.value = notifications.value.filter(n => n.id !== id)
    }
    
    // Écouter les changements de route
    watch(() => route.path, () => {
      // Scroll to top on route change
      window.scrollTo(0, 0)
    })
    
    return {
      showSidebar,
      breadcrumb,
      notifications,
      toggleSidebar,
      addNotification,
      removeNotification
    }
  }
}
</script>

<style scoped>
.app-layout {
  display: flex;
  min-height: 100vh;
  background: #f5f7fa;
}

.main-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  transition: margin-left 0.3s ease;
}

.main-content.with-sidebar {
  margin-left: 250px;
}

.content-area {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
}

.breadcrumb {
  background: white;
  padding: 15px 20px;
  border-radius: 8px;
  margin-bottom: 20px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.05);
}

.breadcrumb a {
  color: #4299e1;
  text-decoration: none;
}

.breadcrumb a:hover {
  text-decoration: underline;
}

.breadcrumb .separator {
  margin: 0 10px;
  color: #cbd5e0;
}

.app-footer {
  background: white;
  padding: 15px 20px;
  text-align: center;
  border-top: 1px solid #e2e8f0;
  color: #718096;
  font-size: 14px;
}

.notifications {
  position: fixed;
  top: 20px;
  right: 20px;
  z-index: 1000;
}

.notification {
  background: white;
  padding: 15px 20px;
  border-radius: 8px;
  margin-bottom: 10px;
  box-shadow: 0 4px 6px rgba(0,0,0,0.1);
  display: flex;
  align-items: center;
  justify-content: space-between;
  min-width: 300px;
  animation: slideIn 0.3s ease;
}

.notification.success {
  border-left: 4px solid #48bb78;
  background: #f0fff4;
}

.notification.error {
  border-left: 4px solid #f56565;
  background: #fff5f5;
}

.notification.info {
  border-left: 4px solid #4299e1;
  background: #ebf8ff;
}

.notification.warning {
  border-left: 4px solid #ed8936;
  background: #fffaf0;
}

.close-btn {
  background: none;
  border: none;
  font-size: 20px;
  cursor: pointer;
  color: #718096;
}

.close-btn:hover {
  color: #4a5568;
}

@keyframes slideIn {
  from {
    transform: translateX(100%);
    opacity: 0;
  }
  to {
    transform: translateX(0);
    opacity: 1;
  }
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

@media (max-width: 768px) {
  .main-content.with-sidebar {
    margin-left: 0;
  }
  
  .content-area {
    padding: 15px;
  }
  
  .notification {
    min-width: 250px;
  }
}
</style>

