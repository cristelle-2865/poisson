import api from './api'
import { calculService } from './calculService'

const nourrissageService = {
  
  /**
   * Nourrir les poissons avec calculs exacts selon les règles métiers
   * @param {number} quantitePlat - Quantité de plat en kg
   * @param {number} proteinesParKg - Protéines par kg d'aliment (g/kg)
   * @param {number} glucidesParKg - Glucides par kg d'aliment (g/kg)
   * @returns {Object} Résultat du nourrissage avec calculs détaillés
   */
  async nourrirPoissons(quantitePlat, proteinesParKg, glucidesParKg) {
    try {
      // 1. Calculer les apports nutritionnels totaux
      const apports = calculService.calculerApportsTotaux(
        quantitePlat,
        proteinesParKg,
        glucidesParKg
      )
      
      // 2. Récupérer la liste des poissons affamés
      let poissonsAffames
      try {
        // Appel à l'API pour les poissons affamés (à adapter selon votre API)
        const response = await api.get('/poissons/affames')
        poissonsAffames = response.data
      } catch (error) {
        console.warn('Impossible de récupérer les poissons affamés, utilisation de données simulées')
        // Données simulées pour le test
        poissonsAffames = [
          { idPoisson: 1, nomPoisson: 'Tilapia-001', poidsActuelPoisson: 500 },
          { idPoisson: 2, nomPoisson: 'Tilapia-002', poidsActuelPoisson: 450 }
        ]
      }
      
      // 3. Calculer les apports par poisson
      const nbPoissons = poissonsAffames.length
      const proteinesParPoisson = apports.proteinesTotal / nbPoissons
      const glucidesParPoisson = apports.glucidesTotal / nbPoissons
      
      // 4. Calculer le gain de poids pour chaque poisson
      const gainParPoisson = calculService.calculerGainPoids(
        proteinesParPoisson,
        glucidesParPoisson
      )
      
      // 5. Préparer les données pour chaque poisson
      const poissonsAvecGain = poissonsAffames.map(poisson => ({
        idPoisson: poisson.idPoisson,
        nomPoisson: poisson.nomPoisson,
        proteinesRecues: proteinesParPoisson,
        glucidesRecus: glucidesParPoisson,
        gainPrevu: gainParPoisson,
        poidsInitial: poisson.poidsActuelPoisson,
        poidsFinal: poisson.poidsActuelPoisson + gainParPoisson,
        satisfaction: calculService.calculerSatisfaction(proteinesParPoisson, glucidesParPoisson)
      }))
      
      // 6. Calculer les totaux
      const gainTotal = gainParPoisson * nbPoissons
      const satisfactionMoyenne = poissonsAvecGain.reduce((sum, p) => sum + p.satisfaction.moyenne, 0) / nbPoissons
      
      // 7. Préparer les données pour l'API
      const nourrissageData = {
        quantitePlat,
        proteinesParKg,
        glucidesParKg,
        dateNourrissage: new Date().toISOString(),
        poissons: poissonsAvecGain,
        resume: {
          apportsTotaux: apports,
          nbPoissonsNourris: nbPoissons,
          gainTotal,
          satisfactionMoyenne: Math.round(satisfactionMoyenne)
        }
      }
      
      console.log('📤 Données envoyées au backend:', nourrissageData)
      
      // 8. Appeler l'API
      const response = await api.post('/nourrissage/nourrir', null, {
        params: {
          quantitePlat: quantitePlat,
          proteinesParKg: proteinesParKg,
          glucidesParKg: glucidesParKg
        }
      })
      
      // 9. Combiner la réponse avec nos calculs
      const resultat = {
        ...response.data,
        calculs: {
          apports,
          proteinesParPoisson,
          glucidesParPoisson,
          gainParPoisson,
          gainTotal
        },
        poissons: poissonsAvecGain,
        resume: nourrissageData.resume
      }
      
      return resultat
      
    } catch (error) {
      console.error('❌ Erreur nourrissage:', error)
      
      // Fournir des informations détaillées sur l'erreur
      const errorMessage = error.response?.data?.message || error.message
      const errorDetails = {
        message: `Erreur lors du nourrissage: ${errorMessage}`,
        quantitePlat,
        proteinesParKg,
        glucidesParKg,
        status: error.response?.status,
        timestamp: new Date().toISOString()
      }
      
      throw new Error(JSON.stringify(errorDetails))
    }
  },

  /**
   * Nourrir les poissons (version simple - pour compatibilité)
   */
  async nourrirPoissonsSimple(quantitePlat) {
    try {
      // Utiliser des valeurs par défaut pour l'aliment
      const proteinesParKg = 10 // g/kg (valeur par défaut)
      const glucidesParKg = 10  // g/kg (valeur par défaut)
      
      return await this.nourrirPoissons(quantitePlat, proteinesParKg, glucidesParKg)
      
    } catch (error) {
      console.error('Erreur nourrissage simple:', error)
      throw error
    }
  },

  /**
   * Récupérer l'historique d'un poisson avec analyse des gains
   */
  async getHistoriquePoisson(poissonId) {
    try {
      const response = await api.get(`/fisakafoanana/poisson/${poissonId}`)
      const historique = response.data
      
      // Analyser chaque entrée selon les règles métiers
      const historiqueAnalyse = historique.map(entry => {
        const satisfaction = calculService.calculerSatisfaction(
          entry.proteinesRecuesFisakafoanana || 0,
          entry.glucidesRecusFisakafoanana || 0
        )
        
        const gainAttendu = calculService.calculerGainPoids(
          entry.proteinesRecuesFisakafoanana || 0,
          entry.glucidesRecusFisakafoanana || 0
        )
        
        const efficacite = gainAttendu > 0 
          ? ((entry.gainPoidsFisakafoanana || 0) / gainAttendu) * 100 
          : 0
        
        return {
          ...entry,
          analyse: {
            satisfaction,
            gainAttendu,
            gainReel: entry.gainPoidsFisakafoanana || 0,
            efficacite: Math.round(efficacite),
            besoinsSatisfaits: satisfaction.moyenne >= 100
          }
        }
      })
      
      return historiqueAnalyse
      
    } catch (error) {
      console.error(`Erreur historique poisson ${poissonId}:`, error)
      throw error
    }
  },

  /**
   * Récupérer l'historique par date avec statistiques
   */
  async getHistoriqueByDate(date) {
    try {
      const response = await api.get(`/fisakafoanana/date/${date}`)
      const historique = response.data
      
      // Calculer les statistiques de la journée
      let stats = {
        totalNourrissages: historique.length,
        nourritureTotale: 0,
        gainTotal: 0,
        satisfactionMoyenne: 0,
        poissonsNourris: new Set()
      }
      
      historique.forEach(entry => {
        stats.nourritureTotale += entry.quantiteNourritureFisakafoanana || 0
        stats.gainTotal += entry.gainPoidsFisakafoanana || 0
        stats.satisfactionMoyenne += entry.tauxSatisfactionFisakafoanana || 0
        
        if (entry.poisson?.idPoisson) {
          stats.poissonsNourris.add(entry.poisson.idPoisson)
        }
      })
      
      if (historique.length > 0) {
        stats.satisfactionMoyenne = Math.round(stats.satisfactionMoyenne / historique.length)
      }
      
      stats.poissonsNourris = stats.poissonsNourris.size
      
      return {
        historique,
        stats
      }
      
    } catch (error) {
      console.error(`Erreur historique date ${date}:`, error)
      throw error
    }
  },

  /**
   * Récupérer les aliments disponibles avec analyse nutritionnelle
   */
  async getAliments() {
    try {
      const response = await api.get('/aliments')
      const aliments = response.data
      
      // Analyser chaque aliment
      const alimentsAnalyse = aliments.map(aliment => {
        // Calculer le coût nutritionnel
        const coutProteine = aliment.prixKgAliment / (aliment.proteinesParKgAliment || 1)
        const coutGlucide = aliment.prixKgAliment / (aliment.glucidesParKgAliment || 1)
        
        // Évaluer la qualité nutritionnelle
        let qualite = 'moyenne'
        if (aliment.proteinesParKgAliment >= 15 && aliment.glucidesParKgAliment >= 15) {
          qualite = 'excellente'
        } else if (aliment.proteinesParKgAliment >= 10 || aliment.glucidesParKgAliment >= 10) {
          qualite = 'bonne'
        }
        
        return {
          ...aliment,
          analyse: {
            coutProteine: Math.round(coutProteine),
            coutGlucide: Math.round(coutGlucide),
            qualite,
            ratioProteinesGlucides: aliment.glucidesParKgAliment > 0 
              ? (aliment.proteinesParKgAliment / aliment.glucidesParKgAliment).toFixed(2)
              : '∞'
          }
        }
      })
      
      return alimentsAnalyse
      
    } catch (error) {
      console.error('Erreur récupération aliments:', error)
      throw error
    }
  },

  /**
   * Récupérer les plats préparés avec calculs nutritionnels
   */
  async getPlats() {
    try {
      const response = await api.get('/plats')
      const plats = response.data
      
      // Analyser chaque plat
      const platsAnalyse = plats.map(plat => {
        // Calculer les apports totaux du plat
        let proteinesTotal = 0
        let glucidesTotal = 0
        let coutTotal = 0
        
        if (plat.aliments && Array.isArray(plat.aliments)) {
          plat.aliments.forEach(aliment => {
            const proteines = (aliment.proteinesParKg || 0) * (aliment.quantite || 0) / 1000
            const glucides = (aliment.glucidesParKg || 0) * (aliment.quantite || 0) / 1000
            const cout = (aliment.prixParKg || 0) * (aliment.quantite || 0)
            
            proteinesTotal += proteines
            glucidesTotal += glucides
            coutTotal += cout
          })
        }
        
        // Calculer le gain potentiel
        const gainPotentiel = calculService.calculerGainPoids(proteinesTotal, glucidesTotal)
        
        // Calculer la satisfaction
        const satisfaction = calculService.calculerSatisfaction(proteinesTotal, glucidesTotal)
        
        return {
          ...plat,
          analyse: {
            proteinesTotal: Math.round(proteinesTotal * 100) / 100,
            glucidesTotal: Math.round(glucidesTotal * 100) / 100,
            coutTotal: Math.round(coutTotal),
            gainPotentiel,
            satisfaction
          }
        }
      })
      
      return platsAnalyse
      
    } catch (error) {
      console.error('Erreur récupération plats:', error)
      throw error
    }
  },

  /**
   * Analyser les performances de nourrissage sur une période
   */
  async analyserPerformances(debut, fin) {
    try {
      // Récupérer l'historique sur la période
      const params = {}
      if (debut) params.dateDebut = debut
      if (fin) params.dateFin = fin
      
      let historique
      try {
        const response = await api.get('/fisakafoanana', { params })
        historique = response.data
      } catch (error) {
        console.warn('Impossible de récupérer l\'historique complet, utilisation de données limitées')
        historique = []
      }
      
      // Calculer les statistiques détaillées
      const stats = {
        periode: { debut, fin },
        totalNourrissages: historique.length,
        nourritureTotale: 0,
        gainTotal: 0,
        coutTotalEstime: 0,
        satisfactionMoyenne: 0,
        efficaciteMoyenne: 0,
        poissonsNourris: new Set(),
        parJour: {}
      }
      
      // Analyser chaque jour
      historique.forEach(entry => {
        const date = entry.dateNourrissageFisakafoanana?.split('T')[0] || 'inconnue'
        
        if (!stats.parJour[date]) {
          stats.parJour[date] = {
            nourrissages: 0,
            nourriture: 0,
            gain: 0,
            satisfaction: 0
          }
        }
        
        stats.nourritureTotale += entry.quantiteNourritureFisakafoanana || 0
        stats.gainTotal += entry.gainPoidsFisakafoanana || 0
        stats.satisfactionMoyenne += entry.tauxSatisfactionFisakafoanana || 0
        
        stats.parJour[date].nourrissages++
        stats.parJour[date].nourriture += entry.quantiteNourritureFisakafoanana || 0
        stats.parJour[date].gain += entry.gainPoidsFisakafoanana || 0
        stats.parJour[date].satisfaction += entry.tauxSatisfactionFisakafoanana || 0
        
        if (entry.poisson?.idPoisson) {
          stats.poissonsNourris.add(entry.poisson.idPoisson)
        }
      })
      
      // Calculer les moyennes
      if (historique.length > 0) {
        stats.satisfactionMoyenne = Math.round(stats.satisfactionMoyenne / historique.length)
        stats.efficaciteMoyenne = stats.nourritureTotale > 0 
          ? Math.round((stats.gainTotal / stats.nourritureTotale) * 100) / 100
          : 0
      }
      
      stats.poissonsNourris = stats.poissonsNourris.size
      
      // Calculer les moyennes par jour
      Object.keys(stats.parJour).forEach(date => {
        if (stats.parJour[date].nourrissages > 0) {
          stats.parJour[date].satisfaction = Math.round(
            stats.parJour[date].satisfaction / stats.parJour[date].nourrissages
          )
        }
      })
      
      return stats
      
    } catch (error) {
      console.error('Erreur analyse performances:', error)
      throw error
    }
  },

  /**
   * Recommander une quantité de nourriture
   */
  recommanderQuantite(nbPoissonsAffames) {
    if (nbPoissonsAffames <= 0) return 0
    
    // Règle : 5g de plat par poisson pour satisfaire les besoins minimums
    const platMinimum = nbPoissonsAffames * 5 // en grammes
    const platRecommandé = nbPoissonsAffames * 10 // double pour bonne satisfaction
    const platMaximum = nbPoissonsAffames * 20 // pour satisfaction totale
    
    return {
      minimum: platMinimum / 1000, // convertir en kg
      recommandé: platRecommandé / 1000,
      maximum: platMaximum / 1000,
      explication: `Pour ${nbPoissonsAffames} poissons: 5g minimum, 10g recommandé, 20g maximum par poisson`
    }
  },

  /**
   * Simuler un nourrissage (pour prévisualisation)
   */
  simulerNourrissage(quantitePlat, proteinesParKg, glucidesParKg, nbPoissons) {
    if (nbPoissons <= 0) {
      return {
        erreur: 'Aucun poisson à nourrir'
      }
    }
    
    // Calculer les apports
    const apports = calculService.calculerApportsTotaux(quantitePlat, proteinesParKg, glucidesParKg)
    
    // Calculer par poisson
    const proteinesParPoisson = apports.proteinesTotal / nbPoissons
    const glucidesParPoisson = apports.glucidesTotal / nbPoissons
    
    // Calculer le gain
    const gainParPoisson = calculService.calculerGainPoids(proteinesParPoisson, glucidesParPoisson)
    
    // Calculer la satisfaction
    const satisfaction = calculService.calculerSatisfaction(proteinesParPoisson, glucidesParPoisson)
    
    // Scénarios possibles
    const scenarios = [
      { proteines: 2, glucides: 0, gain: 5, description: 'Protéines seulement' },
      { proteines: 0, glucides: 4, gain: 5, description: 'Glucides seulement' },
      { proteines: 2, glucides: 4, gain: 10, description: 'Besoins minimums' },
      { proteines: 4, glucides: 4, gain: 15, description: 'Protéines doublées' },
      { proteines: 4, glucides: 8, gain: 20, description: 'Besoins totaux' }
    ]
    
    return {
      simulation: true,
      quantitePlat,
      apports,
      parPoisson: {
        proteines: proteinesParPoisson,
        glucides: glucidesParPoisson,
        gain: gainParPoisson,
        satisfaction
      },
      totaux: {
        gain: gainParPoisson * nbPoissons,
        satisfactionMoyenne: satisfaction.moyenne
      },
      scenarios,
      recommandation: this.recommanderQuantite(nbPoissons),
      evaluation: this.evaluerNourrissage(proteinesParPoisson, glucidesParPoisson)
    }
  },

  /**
   * Évaluer la qualité d'un nourrissage
   */
  evaluerNourrissage(proteinesParPoisson, glucidesParPoisson) {
    const gain = calculService.calculerGainPoids(proteinesParPoisson, glucidesParPoisson)
    const satisfaction = calculService.calculerSatisfaction(proteinesParPoisson, glucidesParPoisson)
    
    let evaluation = 'insuffisant'
    let conseil = 'Augmentez la quantité de nourriture'
    
    if (gain >= 20) {
      evaluation = 'excellent'
      conseil = 'Nutrition optimale, tous les besoins satisfaits'
    } else if (gain >= 15) {
      evaluation = 'très bon'
      conseil = 'Bonne nutrition, gains élevés'
    } else if (gain >= 10) {
      evaluation = 'bon'
      conseil = 'Besoins minimums satisfaits'
    } else if (gain >= 5) {
      evaluation = 'moyen'
      conseil = 'Nutrition partielle, certains besoins non satisfaits'
    }
    
    return {
      evaluation,
      gain,
      satisfaction,
      conseil
    }
  },

  // nourrissageService.js - Ajoutez cette nouvelle méthode

/**
 * Nourrir les poissons avec données complètes
 */
async nourrirPoissonsComplet(data) {
  try {
    const response = await api.post('/nourrissage/nourrir-complet', data)
    return response.data
  } catch (error) {
    console.error('❌ Erreur nourrissage complet:', error)
    throw error
  }
},

async nourrirAvecPlat(platId) {
  try {
    console.log('📤 Envoi nourrissage avec plat:', platId)
    
    const response = await api.post(`/nourrissage/nourrir-avec-plat/${platId}`)
    
    console.log('✅ Nourrissage avec plat réussi:', response.data)
    
    // Combiner avec nos calculs pour l'affichage
    const resultat = {
      ...response.data,
      message: `Plat utilisé avec succès: ${response.data.platUtilise}`,
      poissonsNourris: response.data.poissonsNourris || 0,
      nourritureUtilisee: response.data.quantitePlat || 0,
      success: true
    }
    
    return resultat
    
  } catch (error) {
    console.error('❌ Erreur nourrissage avec plat:', error)
    
    const errorMessage = error.response?.data?.message || error.message
    const errorDetails = {
      message: `Erreur avec le plat: ${errorMessage}`,
      platId,
      status: error.response?.status,
      timestamp: new Date().toISOString()
    }
    
    throw new Error(JSON.stringify(errorDetails))
  }
}

}

export default nourrissageService

