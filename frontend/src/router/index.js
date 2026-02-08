import { createRouter, createWebHistory } from 'vue-router'
import MainLayout from '../layouts/MainLayout.vue'
import AuthLayout from '../layouts/AuthLayout.vue'

const routes = [
  {
    path: '/',
    component: MainLayout,
    meta: { requiresAuth: true },
    children: [
      // Dashboard
      {
        path: '',
        name: 'Dashboard',
        component: () => import('../views/Dashboard.vue'),
        meta: { 
          title: 'Dashboard - Pisciculture',
          icon: '🏠',
          breadcrumb: 'Tableau de bord'
        }
      },
      
      // Poissons
      {
        path: 'poissons',
        name: 'Poissons',
        component: () => import('../views/PoissonsList.vue'),
        meta: { 
          title: 'Gestion des Poissons',
          icon: '🐟',
          breadcrumb: 'Poissons'
        }
      },
      {
        path: 'poissons/nouveau',
        name: 'NouveauPoisson',
        component: () => import('../views/NouveauPoisson.vue'),
        meta: { 
          title: 'Ajouter un Poisson',
          breadcrumb: 'Nouveau'
        }
      },
      {
        path: 'poissons/edit/:id',
        name: 'ModifierPoisson',
        component: () => import('../views/ModifierPoisson.vue'),
        meta: { 
          title: 'Modifier un Poisson',
          breadcrumb: 'Modifier'
        },
        props: true
      },
      {
        path: 'poissons/:id',
        name: 'DetailPoisson',
        component: () => import('../views/DetailPoisson.vue'),
        meta: { 
          title: 'Détail Poisson',
          breadcrumb: 'Détails'
        },
        props: true
      },
      
      // Bassins (NOUVEAU)
      {
        path: 'bassins',
        name: 'Bassins',
        component: () => import('../views/BassinsList.vue'),
        meta: { 
          title: 'Gestion des Bassins',
          icon: '🏊',
          breadcrumb: 'Bassins'
        }
      },

      
      {
        path: 'bassins/nouveau',
        name: 'NouveauBassin',
        component: () => import('../views/NouveauBassin.vue'),
        meta: { 
          title: 'Ajouter un Bassin',
          breadcrumb: 'Nouveau'
        }
      },
      {
        path: 'bassins/:id',
        name: 'DetailBassin',
        component: () => import('../views/DetailBassin.vue'),
        meta: { 
          title: 'Détail Bassin',
          breadcrumb: 'Détails'
        },
        props: true
      },
      
      // Nourrissage
      {
        path: 'nourrissage',
        name: 'Nourrissage',
        component: () => import('../views/Nourrissage.vue'),
        meta: { 
          title: 'Nourrissage des Poissons',
          icon: '🍽️',
          breadcrumb: 'Nourrissage'
        }
      },
      {
        path: 'nourrissage-new',
        name: 'NourrissageNew',
        component: () => import('../views/NourrissageNew.vue'),
        meta: { 
          title: 'Nourrissage des Poissons',
          icon: '🍽️',
          breadcrumb: 'Nourrissage'
        }
      },
      
      // Plats (NOUVEAU)
      {
        path: 'plats',
        name: 'Plats',
        component: () => import('../views/PlatsList.vue'),
        meta: { 
          title: 'Gestion des Plats',
          icon: '🥘',
          breadcrumb: 'Plats'
        }
      },
      {
        path: 'plats/nouveau',
        name: 'NouveauPlat',
        component: () => import('../views/NouveauPlat.vue'),
        meta: { 
          title: 'Créer un Plat',
          breadcrumb: 'Nouveau'
        }
      },
      
      // Aliments
      {
        path: 'aliments',
        name: 'Aliments',
        component: () => import('../views/Aliments.vue'),
        meta: { 
          title: 'Gestion des Aliments',
          icon: '📦',
          breadcrumb: 'Aliments'
        }
      },
      
      // Historique
      {
        path: 'historique',
        name: 'Historique',
        component: () => import('../views/Historique.vue'),
        meta: { 
          title: 'Historique des Nourrissages',
          icon: '📜',
          breadcrumb: 'Historique'
        }
      },
      
      // Bénéfices
      {
        path: 'benefices',
        name: 'Benefices',
        component: () => import('../views/Benefices.vue'),
        meta: { 
          title: 'Analyse des Bénéfices',
          icon: '💰',
          breadcrumb: 'Bénéfices'
        }
      },
      
      // Rapports (NOUVEAU)
      {
        path: 'rapports',
        name: 'Rapports',
        component: () => import('../views/Rapports.vue'),
        meta: { 
          title: 'Rapports Avancés',
          icon: '📊',
          breadcrumb: 'Rapports'
        }
      },
      
      // Paramètres (NOUVEAU)
      {
        path: 'parametres',
        name: 'Parametres',
        component: () => import('../views/Parametres.vue'),
        meta: { 
          title: 'Paramètres du Système',
          icon: '⚙️',
          breadcrumb: 'Paramètres'
        }
      }
    ]
  },
  
  // Routes d'authentification
  {
    path: '/auth',
    component: AuthLayout,
    meta: { requiresGuest: true },
    children: [
      {
        path: 'login',
        name: 'Login',
        component: () => import('../views/auth/Login.vue'),
        meta: { title: 'Connexion' }
      },
      {
        path: 'register',
        name: 'Register',
        component: () => import('../views/auth/Register.vue'),
        meta: { title: 'Inscription' }
      }
    ]
  },
  
  // Routes d'erreur
  {
    path: '/403',
    name: 'Forbidden',
    component: () => import('../views/errors/Forbidden.vue'),
    meta: { title: 'Accès refusé' }
  },
  {
    path: '/404',
    name: 'NotFound',
    component: () => import('../views/errors/NotFound.vue'),
    meta: { title: 'Page non trouvée' }
  },
  {
    path: '/500',
    name: 'ServerError',
    component: () => import('../views/errors/ServerError.vue'),
    meta: { title: 'Erreur serveur' }
  },
  
  // Catch all 404
  {
    path: '/:pathMatch(.*)*',
    redirect: '/404'
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior(to, from, savedPosition) {
    if (savedPosition) {
      return savedPosition
    } else if (to.hash) {
      return {
        el: to.hash,
        behavior: 'smooth'
      }
    } else {
      return { top: 0, behavior: 'smooth' }
    }
  }
})

// Middleware d'authentification
router.beforeEach((to, from, next) => {
  // Mettre à jour le titre
  document.title = to.meta.title || 'Système Pisciculture'
  
  // Simuler l'authentification (à remplacer par votre vraie logique)
  const token = localStorage.getItem('token')
  const isAuthenticated = !!token
  
  const requiresAuth = to.matched.some(record => record.meta.requiresAuth)
  const requiresGuest = to.matched.some(record => record.meta.requiresGuest)
  
  if (requiresAuth && !isAuthenticated) {
    // Rediriger vers login si page protégée
    next('/auth/login')
  } else if (requiresGuest && isAuthenticated) {
    // Rediriger vers dashboard si déjà connecté
    next('/')
  } else {
    next()
  }
})

export default router


