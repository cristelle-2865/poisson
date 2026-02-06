import api from './api'

const alimentService = {
  // Récupérer tous les aliments
  async getAliments() {
    try {
      const response = await api.get('/aliments')
      return response.data
    } catch (error) {
      console.error('Erreur récupération aliments:', error)
      throw error
    }
  },

  // Récupérer un aliment par ID
  async getAlimentById(id) {
    try {
      const response = await api.get(`/aliments/${id}`)
      return response.data
    } catch (error) {
      console.error(`Erreur récupération aliment ${id}:`, error)
      throw error
    }
  },

  // Créer un nouvel aliment
  async createAliment(alimentData) {
    try {
      const response = await api.post('/aliments', alimentData)
      return response.data
    } catch (error) {
      console.error('Erreur création aliment:', error)
      throw error
    }
  },

  // Mettre à jour un aliment
  async updateAliment(id, alimentData) {
    try {
      const response = await api.put(`/aliments/${id}`, alimentData)
      return response.data
    } catch (error) {
      console.error(`Erreur mise à jour aliment ${id}:`, error)
      throw error
    }
  },

  // Supprimer un aliment
  async deleteAliment(id) {
    try {
      await api.delete(`/aliments/${id}`)
    } catch (error) {
      console.error(`Erreur suppression aliment ${id}:`, error)
      throw error
    }
  },

  // Mettre à jour le stock d'un aliment
  async updateStock(id, quantite) {
    try {
      const response = await api.put(`/aliments/${id}/stock`, null, {
        params: { quantite }
      })
      return response.data
    } catch (error) {
      console.error(`Erreur mise à jour stock ${id}:`, error)
      throw error
    }
  },

  // Récupérer les aliments avec stock bas
  async getAlimentsStockBas() {
    try {
      const aliments = await this.getAliments()
      return aliments.filter(aliment => 
        aliment.stockAliment < (aliment.seuilMinimumAliment || 10)
      )
    } catch (error) {
      console.error('Erreur récupération aliments stock bas:', error)
      throw error
    }
  },

  // Récupérer le stock total
  async getStockTotal() {
    try {
      const aliments = await this.getAliments()
      return aliments.reduce((total, aliment) => 
        total + (aliment.stockAliment || 0), 0
      )
    } catch (error) {
      console.error('Erreur calcul stock total:', error)
      throw error
    }
  },

  // Rechercher des aliments
  async searchAliments(query) {
    try {
      const aliments = await this.getAliments()
      return aliments.filter(aliment => 
        aliment.nomAliment.toLowerCase().includes(query.toLowerCase()) ||
        (aliment.description || '').toLowerCase().includes(query.toLowerCase())
      )
    } catch (error) {
      console.error('Erreur recherche aliments:', error)
      throw error
    }
  },

  // Récupérer les aliments actifs
  async getAlimentsActifs() {
    try {
      const aliments = await this.getAliments()
      return aliments.filter(aliment => aliment.estActifAliment !== false)
    } catch (error) {
      console.error('Erreur récupération aliments actifs:', error)
      throw error
    }
  }
}

export default alimentService


