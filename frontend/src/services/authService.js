// services/authService.js
import api from './api'

const authService = {
  async login(credentials) {
    try {
      console.log('Tentative de connexion avec:', credentials)
      const response = await api.post('/auth/login', {
        email: credentials.email,
        password: credentials.password,
        remember: credentials.remember
      })
      console.log('Réponse login:', response.data)
      
      if (response.data.token) {
        // Stocker le token avec la clé correcte
        localStorage.setItem('auth_token', response.data.token)
        localStorage.setItem('user', JSON.stringify({
          email: response.data.email,
          name: response.data.nom || 'Utilisateur',
          role: response.data.role || 'UTILISATEUR'
        }))
        console.log('Token stocké avec succès')
      }
      return response.data
    } catch (error) {
      console.error('Erreur login API:', {
        status: error.response?.status,
        data: error.response?.data,
        message: error.message
      })
      throw error.response?.data?.message || 'Erreur de connexion'
    }
  },

  async demoLogin() {
    try {
      console.log('Tentative de connexion démo')
      const response = await api.post('/auth/demo')
      console.log('Réponse démo:', response.data)
      
      if (response.data.token) {
        localStorage.setItem('auth_token', response.data.token)
        localStorage.setItem('user', JSON.stringify({
          email: response.data.email,
          name: response.data.nom || 'Utilisateur Démo',
          role: response.data.role || 'ADMIN'
        }))
        console.log('Token démo stocké avec succès')
      }
      return response.data
    } catch (error) {
      console.error('Erreur démo login:', error.response?.data || error.message)
      throw error.response?.data?.message || 'Erreur de connexion démo'
    }
  },

  async register(userData) {
    try {
      console.log('Tentative d\'inscription avec:', userData)
      const response = await api.post('/auth/register', userData)
      console.log('Réponse register:', response.data)
      
      if (response.data.token) {
        localStorage.setItem('auth_token', response.data.token)
        localStorage.setItem('user', JSON.stringify({
          email: response.data.email,
          name: response.data.nom,
          role: response.data.role
        }))
      }
      return response.data
    } catch (error) {
      console.error('Erreur register:', error.response?.data || error.message)
      throw error.response?.data?.message || 'Erreur d\'inscription'
    }
  },

  logout() {
    console.log('Déconnexion')
    localStorage.removeItem('auth_token')
    localStorage.removeItem('user')
    sessionStorage.clear()
  },

  getToken() {
    return localStorage.getItem('auth_token')
  },

  getUser() {
    const userStr = localStorage.getItem('user')
    return userStr ? JSON.parse(userStr) : null
  },

  isAuthenticated() {
    const token = this.getToken()
    const hasToken = !!token
    console.log('Vérification authentification - Token présent:', hasToken)
    return hasToken
  },

  isAdmin() {
    const user = this.getUser()
    return user && user.role === 'ADMIN'
  }
}

export default authService

