// PlatService.js - CORRIGÉ POUR DTO
const API_URL = 'http://localhost:8080/api'

export default {
  // Récupérer tous les plats
  async getAllPlats() {
    try {
      const url = `${API_URL}/plats`
      console.log('📡 GET vers:', url)
      
      const response = await fetch(url, {
        headers: {
          'Accept': 'application/json',
          'Content-Type': 'application/json'
        }
      })
      
      console.log('✅ Réponse GET, status:', response.status)
      
      if (!response.ok) {
        const errorText = await response.text()
        console.error('❌ Erreur HTTP GET:', response.status, errorText)
        throw new Error(`Erreur HTTP ${response.status}: ${errorText}`)
      }
      
      const data = await response.json()
      console.log('📊 Données reçues:', data.length, 'plats')
      return data
      
    } catch (error) {
      console.error('💥 Erreur fetch getAllPlats:', error)
      // Retourner un tableau vide pour éviter de bloquer l'interface
      return []
    }
  },
  
  // Créer un nouveau plat (avec DTO)
  // PlatService.js - CORRECTION POUR LE FORMAT DTO
async createPlat(platData) {
  try {
    const url = `${API_URL}/plats`
    console.log('📡 POST vers:', url)
    
    // Convertir les données pour correspondre EXACTEMENT au DTO
    const dtoData = {
      nomPlat: platData.nomPlat?.trim() || '',
      descriptionPlat: platData.descriptionPlat?.trim() || '',
      datePreparationPlat: platData.datePreparationPlat || new Date().toISOString().split('T')[0],
      estUtilisePlat: platData.estUtilisePlat || false,
      compositions: []
    }
    
    console.log('📋 Données originales:', platData)
    
    // CORRECTION ICI : Extraire directement idAliment de l'objet aliment
    if (platData.compositions && Array.isArray(platData.compositions)) {
      dtoData.compositions = platData.compositions.map(comp => {
        console.log('🧾 Composition à convertir:', comp)
        
        // IMPORTANT : Le DTO attend idAliment directement, pas un objet aliment
        let idAliment = null
        
        // Essayez différents formats possibles
        if (comp.aliment && comp.aliment.idAliment) {
          idAliment = comp.aliment.idAliment
        } else if (comp.idAliment) {
          idAliment = comp.idAliment
        } else if (comp.aliment && comp.aliment.id) {
          idAliment = comp.aliment.id
        } else if (comp.alimentId) {
          idAliment = comp.alimentId
        }
        
        // Trouver le poids
        let poidsAlimentComposition = 0
        if (comp.quantite !== undefined && comp.quantite !== null) {
          poidsAlimentComposition = parseFloat(comp.quantite)
        } else if (comp.poidsAlimentComposition !== undefined && comp.poidsAlimentComposition !== null) {
          poidsAlimentComposition = parseFloat(comp.poidsAlimentComposition)
        }
        
        console.log(`📊 Conversion: idAliment=${idAliment}, poids=${poidsAlimentComposition}`)
        
        if (!idAliment) {
          throw new Error(`ID de l'aliment manquant dans la composition: ${JSON.stringify(comp)}`)
        }
        
        if (isNaN(poidsAlimentComposition) || poidsAlimentComposition <= 0) {
          throw new Error(`Poids invalide dans la composition: ${poidsAlimentComposition}`)
        }
        
        // FORMAT EXACT DU DTO
        return {
          idAliment: idAliment,
          poidsAlimentComposition: poidsAlimentComposition
        }
      })
    }
    
    console.log('📦 DTO final envoyé:', JSON.stringify(dtoData, null, 2))
    
    const response = await fetch(url, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Accept': 'application/json'
      },
      body: JSON.stringify(dtoData)
    })
    
    console.log('✅ Réponse POST, status:', response.status)
    console.log('📋 Headers réponse:', Object.fromEntries(response.headers.entries()))
    
    if (!response.ok) {
      let errorText = 'Erreur inconnue'
      try {
        errorText = await response.text()
        console.error('❌ Erreur HTTP POST - Texte brut:', errorText)
        
        // Essayer de parser le JSON d'erreur
        if (errorText && errorText.startsWith('{')) {
          try {
            const errorJson = JSON.parse(errorText)
            console.error('❌ Erreur HTTP POST - JSON:', errorJson)
            
            if (errorJson.message) {
              errorText = errorJson.message
            } else if (errorJson.error) {
              errorText = errorJson.error
            }
          } catch (jsonError) {
            console.error('❌ Impossible de parser l\'erreur comme JSON')
          }
        }
      } catch (e) {
        console.error('❌ Impossible de lire le message d\'erreur:', e)
      }
      
      let errorMessage = `Erreur HTTP ${response.status}: ${errorText}`
      
      // Messages d'erreur spécifiques
      switch (response.status) {
        case 400:
          errorMessage = `Données invalides (400): ${errorText}`
          break
        case 404:
          errorMessage = `Ressource non trouvée (404): ${errorText}`
          break
        case 500:
          errorMessage = `Erreur serveur (500): ${errorText}`
          break
        default:
          errorMessage = `Erreur ${response.status}: ${errorText}`
      }
      
      throw new Error(errorMessage)
    }
    
    const data = await response.json()
    console.log('🎉 Plat créé avec succès! ID:', data.idPlat)
    return data
    
  } catch (error) {
    console.error('💥 Erreur complète createPlat:', {
      name: error.name,
      message: error.message,
      stack: error.stack
    })
    throw error
  }
},
  
  // Récupérer les plats disponibles
  async getPlatsDisponibles() {
    try {
      const response = await fetch(`${API_URL}/plats/disponibles`)
      if (!response.ok) throw new Error('Erreur réseau')
      return await response.json()
    } catch (error) {
      console.error('Erreur getPlatsDisponibles:', error)
      return []
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
  
  // Mettre à jour un plat
  async updatePlat(id, platData) {
    try {
      // Convertir au format DTO
      const dtoData = {
        nomPlat: platData.nomPlat?.trim() || '',
        descriptionPlat: platData.descriptionPlat?.trim() || '',
        datePreparationPlat: platData.datePreparationPlat || new Date().toISOString().split('T')[0],
        estUtilisePlat: platData.estUtilisePlat || false,
        compositions: []
      }
      
      if (platData.compositions && Array.isArray(platData.compositions)) {
        dtoData.compositions = platData.compositions.map(comp => ({
          idAliment: comp.aliment?.idAliment || comp.aliment?.id,
          poidsAlimentComposition: parseFloat(comp.poidsAlimentComposition) || 0
        }))
      }
      
      const response = await fetch(`${API_URL}/plats/${id}`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(dtoData)
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
      const response = await fetch(`${API_URL}/plats/${id}`, {
        method: 'DELETE'
      })
      
      if (!response.ok) {
        const error = await response.json()
        throw new Error(error.message || 'Erreur suppression')
      }
    } catch (error) {
      console.error('Erreur deletePlat:', error)
      throw error
    }
  },
  
  // Ajouter un aliment à un plat
  async ajouterAliment(idPlat, idAliment, poids) {
    try {
      const response = await fetch(
        `${API_URL}/plats/${idPlat}/ajouter-aliment/${idAliment}?poids=${poids}`,
        {
          method: 'POST'
        }
      )
      
      if (!response.ok) {
        const error = await response.json()
        throw new Error(error.message || 'Erreur ajout aliment')
      }
      
      return await response.json()
    } catch (error) {
      console.error('Erreur ajouterAliment:', error)
      throw error
    }
  },
  
  // Retirer un aliment d'un plat
  async retirerAliment(idPlat, idAliment) {
    try {
      const response = await fetch(
        `${API_URL}/plats/${idPlat}/retirer-aliment/${idAliment}`,
        {
          method: 'DELETE'
        }
      )
      
      if (!response.ok) {
        const error = await response.json()
        throw new Error(error.message || 'Erreur retrait aliment')
      }
      
      return await response.json()
    } catch (error) {
      console.error('Erreur retirerAliment:', error)
      throw error
    }
  },
  
  // Utiliser un plat (marquer comme utilisé)
  async utiliserPlat(id) {
    try {
      const response = await fetch(`${API_URL}/plats/${id}/utiliser`, {
        method: 'POST'
      })
      
      if (!response.ok) {
        const error = await response.json()
        throw new Error(error.message || 'Erreur utilisation plat')
      }
      
      return await response.json()
    } catch (error) {
      console.error('Erreur utiliserPlat:', error)
      throw error
    }
  },
  
  // Récupérer les statistiques d'un plat
  async getStatistiquesPlat(id) {
    try {
      const response = await fetch(`${API_URL}/plats/${id}/statistiques`)
      if (!response.ok) throw new Error('Erreur statistiques')
      return await response.json()
    } catch (error) {
      console.error('Erreur getStatistiquesPlat:', error)
      throw error
    }
  },
  
  // Récupérer le stock total des plats
  async getStockTotalPlats() {
    try {
      const response = await fetch(`${API_URL}/plats/stock-total`)
      if (!response.ok) throw new Error('Erreur stock total')
      const result = await response.json()
      return result
    } catch (error) {
      console.error('Erreur getStockTotalPlats:', error)
      throw error
    }
  }
}

