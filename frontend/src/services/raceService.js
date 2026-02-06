import api from './api'

const raceService = {
  // Récupérer toutes les races
  async getAllRaces() {
    try {
      const response = await api.get('/races-poisson')
      return response.data
    } catch (error) {
      console.error('Erreur récupération races:', error)
      throw error
    }
  },

  // Créer une nouvelle race
  async createRace(raceData) {
    try {
      const response = await api.post('/races-poisson', raceData)
      return response.data
    } catch (error) {
      console.error('Erreur création race:', error)
      throw error
    }
  },

  // Mettre à jour une race
  async updateRace(id, raceData) {
    try {
      const response = await api.put(`/races-poisson/${id}`, raceData)
      return response.data
    } catch (error) {
      console.error(`Erreur mise à jour race ${id}:`, error)
      throw error
    }
  },

  // Supprimer une race
  async deleteRace(id) {
    try {
      await api.delete(`/races-poisson/${id}`)
    } catch (error) {
      console.error(`Erreur suppression race ${id}:`, error)
      throw error
    }
  }
}

export default raceService

