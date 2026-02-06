import axios from 'axios'

const api = axios.create({
  baseURL: 'http://localhost:8080/api',
  headers: {
    'Content-Type': 'application/json',
    'Accept': 'application/json'
  },
  timeout: 10000
})

// Intercepteur pour les requêtes
api.interceptors.request.use(
  config => {
    const token = localStorage.getItem('auth_token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  error => Promise.reject(error)
)

// Intercepteur pour les réponses
api.interceptors.response.use(
  response => response,
  error => {
    if (error.response?.status === 401) {
      // Rediriger vers la page de connexion
      localStorage.removeItem('auth_token')
      window.location.href = '/login'
    }
    return Promise.reject(error)
  }
)

// Intercepteur pour les requêtes
api.interceptors.request.use(
  config => {
    console.log('Requête envoyée:', config.method, config.url)
    console.log('Données:', config.data)
    console.log('Headers:', config.headers)
    
    const token = localStorage.getItem('auth_token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  error => Promise.reject(error)
)

// Intercepteur pour les réponses
api.interceptors.response.use(
  response => {
    console.log('Réponse reçue:', response.status, response.config.url)
    console.log('Données réponse:', response.data)
    return response
  },
  error => {
    console.error('Erreur API complète:', {
      status: error.response?.status,
      url: error.config?.url,
      method: error.config?.method,
      data: error.config?.data,
      headers: error.config?.headers,
      responseData: error.response?.data
    })
    
    if (error.response?.status === 401) {
      localStorage.removeItem('auth_token')
      window.location.href = '/login'
    }
    return Promise.reject(error)
  }
)

export default api


