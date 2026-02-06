const API_URL = 'http://localhost:8080/api'

export default {
  // Récupérer tous les plats
  async getPlats() {
    try {
      const response = await fetch(`${API_URL}/plats`)
      if (!response.ok) throw new Error('Erreur réseau')
      return await response.json()
    } catch (error) {
      console.error('Erreur getPlats:', error)
      throw error
    }
  },

  // Récupérer un plat par ID
  async getPlatById(id) {
    try {
      const response = await fetch(`${API_URL}/plats/${id}`)
      if (!response.ok) throw new Error('Plat non trouvé')
      return await response.json()
    } catch (error) {
      console.error('Erreur getPlatById:', error)
      throw error
    }
  },

  // Créer un nouveau plat
  async createPlat(platData) {
    try {
      const token = localStorage.getItem('token')
      const response = await fetch(`${API_URL}/plats`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`
        },
        body: JSON.stringify(platData)
      })
      
      if (!response.ok) {
        const error = await response.json()
        throw new Error(error.message || 'Erreur création')
      }
      
      return await response.json()
    } catch (error) {
      console.error('Erreur createPlat:', error)
      throw error
    }
  },

  // Mettre à jour un plat
  async updatePlat(id, platData) {
    try {
      const token = localStorage.getItem('token')
      const response = await fetch(`${API_URL}/plats/${id}`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`
        },
        body: JSON.stringify(platData)
      })
      
      if (!response.ok) {
        const error = await response.json()
        throw new Error(error.message || 'Erreur mise à jour')
      }
      
      return await response.json()
    } catch (error) {
      console.error('Erreur updatePlat:', error)
      throw error
    }
  },

  // Supprimer un plat
  async deletePlat(id) {
    try {
      const token = localStorage.getItem('token')
      const response = await fetch(`${API_URL}/plats/${id}`, {
        method: 'DELETE',
        headers: {
          'Authorization': `Bearer ${token}`
        }
      })
      
      if (!response.ok) {
        const error = await response.json()
        throw new Error(error.message || 'Erreur suppression')
      }
      
      return await response.json()
    } catch (error) {
      console.error('Erreur deletePlat:', error)
      throw error
    }
  },

  // Calculer la nutrition d'un plat
  async calculerNutrition(platId) {
    try {
      const response = await fetch(`${API_URL}/plats/${platId}/nutrition`)
      if (!response.ok) throw new Error('Erreur calcul nutrition')
      return await response.json()
    } catch (error) {
      console.error('Erreur calculerNutrition:', error)
      throw error
    }
  },

  // Vérifier les stocks pour un plat
  async verifierStocks(platId, quantite) {
    try {
      const response = await fetch(`${API_URL}/plats/${platId}/verifier-stocks?quantite=${quantite}`)
      if (!response.ok) throw new Error('Erreur vérification stocks')
      return await response.json()
    } catch (error) {
      console.error('Erreur verifierStocks:', error)
      throw error
    }
  }
}

