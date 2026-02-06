import api from './api'

const poissonService = {
  // Récupérer tous les poissons
  async getAllPoissons() {
      try {
          const response = await api.get('/poissons')
          console.log('Données reçues:', response.data) // AJOUTEZ CE LOG
          return response.data
      } catch (error) {
          console.error('Erreur récupération poissons:', error)
          throw error
      }
  },

  // Récupérer les poissons en vie
  async getPoissonsEnVie() {
    try {
      const response = await api.get('/poissons/en-vie')
      return response.data
    } catch (error) {
      console.error('Erreur récupération poissons en vie:', error)
      throw error
    }
  },

  // Récupérer les poissons affamés
  async getPoissonsAffames() {
    try {
      const response = await api.get('/poissons/affames')
      return response.data
    } catch (error) {
      console.error('Erreur récupération poissons affamés:', error)
      throw error
    }
  },

  // Récupérer les poissons prêts à vendre
  async getPoissonsPretAVendre() {
    try {
      const response = await api.get('/poissons/pret-a-vendre')
      return response.data
    } catch (error) {
      console.error('Erreur récupération poissons prêts à vendre:', error)
      throw error
    }
  },

  // Récupérer un poisson par ID
  async getPoissonById(id) {
    try {
      const response = await api.get(`/poissons/${id}`)
      return response.data
    } catch (error) {
      console.error(`Erreur récupération poisson ${id}:`, error)
      throw error
    }
  },

  // Créer un nouveau poisson
  async createPoisson(poissonData) {
    try {
      const response = await api.post('/poissons', poissonData)
      return response.data
    } catch (error) {
      console.error('Erreur création poisson:', error)
      throw error
    }
  },

  // Mettre à jour un poisson
  async updatePoisson(id, poissonData) {
    try {
      console.log('Envoi mise à jour poisson:', { id, poissonData })
      const response = await api.put(`/poissons/${id}`, poissonData)
      console.log('Réponse mise à jour:', response.data)
      return response.data
    } catch (error) {
      console.error(`Erreur détaillée mise à jour poisson ${id}:`, {
        error: error.message,
        response: error.response?.data,
        status: error.response?.status
      })
      throw error
    }
  },

  // Supprimer un poisson
  async deletePoisson(id) {
    try {
      await api.delete(`/poissons/${id}`)
    } catch (error) {
      console.error(`Erreur suppression poisson ${id}:`, error)
      throw error
    }
  },

  // Vendre un poisson
  async vendrePoisson(id) {
    try {
      const response = await api.put(`/poissons/${id}/vendre`)
      return response.data
    } catch (error) {
      console.error(`Erreur vente poisson ${id}:`, error)
      throw error
    }
  },

  // Récupérer les statistiques
  async getStatistiques() {
    try {
      const response = await api.get('/poissons/statistiques')
      return response.data
    } catch (error) {
      console.error('Erreur récupération statistiques:', error)
      throw error
    }
  },

  // Récupérer les races de poissons
  async getRacesPoisson() {
    try {
      const response = await api.get('/races-poisson')
      return response.data
    } catch (error) {
      console.error('Erreur récupération races:', error)
      // Retourner un tableau vide si l'endpoint n'existe pas encore
      return []
    }
  },

   async getPoissonsSansBassin() {
        try {
            const response = await api.get('/poissons/sans-bassin');
            return response.data;
        } catch (error) {
            console.error('Erreur récupération poissons sans bassin:', error);
            throw error;
        }
    },

    async updateWithBassin(id, poissonData) {
        try {
            const response = await api.put(`/poissons/${id}/with-bassin`, poissonData);
            return response.data;
        } catch (error) {
            console.error('Erreur mise à jour poisson avec bassin:', error);
            throw error;
        }
    }
}

export default poissonService


