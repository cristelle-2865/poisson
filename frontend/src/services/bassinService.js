import api from './api'

const bassinService = {
  // Récupérer tous les bassins
  async getAllBassins() {
    try {
      const response = await api.get('/piscines')
      return response.data
    } catch (error) {
      console.error('Erreur récupération bassins:', error)
      throw error
    }
  },

  // Récupérer un bassin par ID
  async getBassinById(id) {
    try {
      const response = await api.get(`/piscines/${id}`)
      return response.data
    } catch (error) {
      console.error(`Erreur récupération bassin ${id}:`, error)
      throw error
    }
  },

  // Récupérer les bassins avec capacité disponible
  async getBassinsAvecCapacite() {
    try {
      const response = await api.get('/piscines/avec-capacite')
      return response.data
    } catch (error) {
      console.error('Erreur récupération bassins avec capacité:', error)
      throw error
    }
  },

  // Récupérer les bassins par type
  async getBassinsParType(type) {
    try {
      const response = await api.get(`/piscines/type/${type}`)
      return response.data
    } catch (error) {
      console.error(`Erreur récupération bassins type ${type}:`, error)
      throw error
    }
  },

  // Créer un nouveau bassin
  async createBassin(bassinData) {
    try {
      const response = await api.post('/piscines', bassinData)
      return response.data
    } catch (error) {
      console.error('Erreur création bassin:', error)
      throw error
    }
  },

  // Mettre à jour un bassin
  async updateBassin(id, bassinData) {
    try {
      const response = await api.put(`/piscines/${id}`, bassinData)
      return response.data
    } catch (error) {
      console.error(`Erreur mise à jour bassin ${id}:`, error)
      throw error
    }
  },

  // Supprimer un bassin
  async deleteBassin(id) {
    try {
      await api.delete(`/piscines/${id}`)
    } catch (error) {
      console.error(`Erreur suppression bassin ${id}:`, error)
      throw error
    }
  },

  // Récupérer les statistiques des bassins
  async getStatistiquesBassins() {
    try {
      const response = await api.get('/piscines/statistiques')
      return response.data
    } catch (error) {
      console.error('Erreur récupération statistiques bassins:', error)
      throw error
    }
  }
}

export default bassinService


