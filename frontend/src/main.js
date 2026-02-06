// frontend/src/main.js
import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import PrimeVue from 'primevue/config'
import 'primevue/resources/themes/saga-blue/theme.css'
import 'primevue/resources/primevue.min.css'
import 'primeicons/primeicons.css'
import './assets/styles/main.css'

// Import PrimeVue components
import DataTable from 'primevue/datatable'
import Column from 'primevue/column'
import Button from 'primevue/button'
import Card from 'primevue/card'
import Dialog from 'primevue/dialog'
import InputText from 'primevue/inputtext'
import InputNumber from 'primevue/inputnumber'
import Calendar from 'primevue/calendar'
import Dropdown from 'primevue/dropdown'
import Toast from 'primevue/toast'
import ToastService from 'primevue/toastservice'
import ProgressSpinner from 'primevue/progressspinner'
import Chart from 'primevue/chart'
import TabView from 'primevue/tabview'
import TabPanel from 'primevue/tabpanel'

const app = createApp(App)

// Use plugins
app.use(router)
app.use(PrimeVue)
app.use(ToastService)

// Register components
app.component('DataTable', DataTable)
app.component('Column', Column)
app.component('Button', Button)
app.component('Card', Card)
app.component('Dialog', Dialog)
app.component('InputText', InputText)
app.component('InputNumber', InputNumber)
app.component('Calendar', Calendar)
app.component('Dropdown', Dropdown)
app.component('Toast', Toast)
app.component('ProgressSpinner', ProgressSpinner)
app.component('Chart', Chart)
app.component('TabView', TabView)
app.component('TabPanel', TabPanel)


// Intercepteur pour l'authentification
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  
  if (to.meta.requiresAuth && !token) {
    next('/auth/login')
  } else if (to.meta.requiresGuest && token) {
    next('/')
  } else {
    next()
  }
})

app.use(router)

// Directive globale pour focus
app.directive('focus', {
  mounted(el) {
    el.focus()
  }
})

// Directive pour permissions
app.directive('permission', {
  mounted(el, binding) {
    const user = JSON.parse(localStorage.getItem('user') || '{}')
    const requiredRole = binding.value
    
    if (!user.roles || !user.roles.includes(requiredRole)) {
      el.style.display = 'none'
    }
  }
})



app.mount('#app')

