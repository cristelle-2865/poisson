<template>
  <div class="plat-creation-page">
    <div class="page-header">
      <h1>Création de Plat</h1>
      <div class="header-info">
        <div class="info-item">
          <span class="label">Aliments disponibles:</span>
          <span class="value">{{ aliments.length }}</span>
        </div>
        <div class="info-item">
          <span class="label">Stock total:</span>
          <span class="value">{{ stockTotal.toFixed(2) }} kg</span>
        </div>
      </div>
    </div>

    <div class="content-grid">
      <!-- Formulaire de création -->
      <div class="creation-form">
        <div class="card glass-card">
          <h2><span class="step-number">1</span> Informations du plat</h2>
          
          <div class="form-group">
            <label for="nomPlat">Nom du plat *</label>
            <input 
              type="text" 
              id="nomPlat" 
              v-model="nouveauPlat.nomPlat" 
              placeholder="Ex: Mélange croissance rapide"
              required
              class="glass-input">
          </div>
          
          <div class="form-group">
            <label for="descriptionPlat">Description</label>
            <textarea 
              id="descriptionPlat" 
              v-model="nouveauPlat.descriptionPlat" 
              placeholder="Description du plat..."
              rows="3"
              class="glass-input"></textarea>
          </div>
        </div>

        <!-- Sélection des aliments -->
        <div class="card glass-card">
          <h2><span class="step-number">2</span> Composition du plat</h2>
          
          <div class="aliments-disponibles">
            <h3>Aliments disponibles</h3>
            <div class="aliments-list">
              <div v-for="aliment in aliments" 
                   :key="aliment.idAliment"
                   class="aliment-item glass-hover"
                   @click="ajouterAuPlat(aliment)">
                <div class="aliment-header">
                  <h4>{{ aliment.nomAliment }}</h4>
                  <span class="aliment-stock glass-badge">{{ aliment.stockAliment }} kg</span>
                </div>
                <div class="aliment-details">
                  <div class="detail">
                    <span class="label">Protéines:</span>
                    <span class="value">{{ aliment.proteinesParKgAliment }} g/kg</span>
                  </div>
                  <div class="detail">
                    <span class="label">Glucides:</span>
                    <span class="value">{{ aliment.glucidesParKgAliment }} g/kg</span>
                  </div>
                  <div class="detail">
                    <span class="label">Prix:</span>
                    <span class="value">{{ aliment.prixKgAliment }} MGA/kg</span>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- Aliments ajoutés -->
          <div class="aliments-ajoutes" v-if="compositions.length > 0">
            <h3>Aliments dans le plat</h3>
            <div class="compositions-list">
              <div v-for="(comp, index) in compositions" 
                   :key="index"
                   class="composition-item glass-inner">
                <div class="composition-header">
                  <h4>{{ comp.aliment.nomAliment }}</h4>
                  <button @click="retirerAliment(index)" class="btn-retirer glass-btn-close">×</button>
                </div>
                <div class="composition-controls">
                  <div class="quantity-control">
                    <label>Quantité (kg)</label>
                    <div class="input-group">
                      <input 
                        type="number" 
                        v-model="comp.quantite"
                        :max="comp.aliment.stockAliment"
                        min="0.1" 
                        step="0.1"
                        @input="calculerTotaux"
                        class="glass-input">
                      <span class="unit">kg</span>
                    </div>
                  </div>
                  <div class="composition-calculs">
                    <div class="calcul-item">
                      <span>Protéines:</span>
                      <span>{{ calculerProteines(comp).toFixed(2) }} g</span>
                    </div>
                    <div class="calcul-item">
                      <span>Glucides:</span>
                      <span>{{ calculerGlucides(comp).toFixed(2) }} g</span>
                    </div>
                    <div class="calcul-item">
                      <span>Coût:</span>
                      <span>{{ calculerCout(comp).toFixed(2) }} MGA</span>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <div v-else class="empty-state glass-inner">
            <div class="empty-icon">👈</div>
            <p>Cliquez sur un aliment pour l'ajouter au plat</p>
          </div>
        </div>
      </div>

      <!-- Panneau de visualisation -->
      <div class="preview-panel">
        <div class="card glass-card">
          <h2>Résumé du plat</h2>
          
          <!-- Totaux -->
          <div class="totaux-section">
            <div class="total-item glass-inner">
              <span class="label">Poids total:</span>
              <span class="value">{{ poidsTotal.toFixed(2) }} kg</span>
            </div>
            <div class="total-item glass-inner">
              <span class="label">Protéines totales:</span>
              <span class="value">{{ proteinesTotal.toFixed(2) }} g</span>
            </div>
            <div class="total-item glass-inner">
              <span class="label">Glucides totales:</span>
              <span class="value">{{ glucidesTotal.toFixed(2) }} g</span>
            </div>
            <div class="total-item glass-inner">
              <span class="label">Protéines par kg:</span>
              <span class="value">{{ proteinesParKg.toFixed(2) }} g/kg</span>
            </div>
            <div class="total-item glass-inner">
              <span class="label">Glucides par kg:</span>
              <span class="value">{{ glucidesParKg.toFixed(2) }} g/kg</span>
            </div>
            <div class="total-item total-cout glass-inner highlight">
              <span class="label">Coût total:</span>
              <span class="value">{{ coutTotal.toFixed(2) }} MGA</span>
            </div>
          </div>

          <!-- Analyse nutritionnelle -->
          <div class="analyse-section">
            <h3>Analyse nutritionnelle</h3>
            
            <div class="analyse-item glass-inner">
              <div class="analyse-header">
                <span class="label">Ratio protéines/glucides</span>
                <span class="value">{{ ratioProteinesGlucides }}</span>
              </div>
              <div class="analyse-bar">
                <div class="bar-proteines" :style="{ width: pourcentageProteines + '%' }">
                  <span>Protéines {{ pourcentageProteines.toFixed(1) }}%</span>
                </div>
                <div class="bar-glucides" :style="{ width: pourcentageGlucides + '%' }">
                  <span>Glucides {{ pourcentageGlucides.toFixed(1) }}%</span>
                </div>
              </div>
            </div>

            <div class="analyse-item glass-inner">
              <div class="analyse-header">
                <span class="label">Coût par kg</span>
                <span class="value">{{ coutParKg.toFixed(2) }} MGA/kg</span>
              </div>
            </div>

            <div class="analyse-item glass-inner">
              <div class="analyse-header">
                <span class="label">Gain potentiel pour 5g de plat</span>
                <span class="value">{{ gainPotentiel.toFixed(1) }} g</span>
              </div>
              <div class="gain-explanation">
                <p>Pour 5g de ce plat, un poisson recevra:</p>
                <ul>
                  <li>{{ (proteinesParKg * 0.005).toFixed(3) }} g de protéines</li>
                  <li>{{ (glucidesParKg * 0.005).toFixed(3) }} g de glucides</li>
                </ul>
              </div>
            </div>
          </div>

          <!-- Boutons d'action -->
          <div class="action-buttons">
            <button 
              @click="creerPlat" 
              :disabled="!peutCreer || loading"
              class="btn-creer glass-btn">
              {{ loading ? 'Création en cours...' : 'Créer le plat' }}
            </button>
            
            <button 
              @click="reinitialiser" 
              :disabled="loading"
              class="btn-reset glass-btn secondary">
              Réinitialiser
            </button>
          </div>

          <!-- Messages -->
          <div v-if="message" class="message" :class="messageClass">
            {{ message }}
          </div>

          <!-- Validation -->
          <div v-if="!peutCreer && compositions.length > 0" class="validation-errors glass-inner">
            <p>⚠️ Pour créer un plat:</p>
            <ul>
              <li v-if="!nouveauPlat.nomPlat">Un nom est requis</li>
              <li v-if="poidsTotal <= 0">Ajoutez au moins un aliment</li>
            </ul>
          </div>
        </div>
      </div>
    </div>

    <!-- Liste des plats existants -->
    <div class="existing-plats">
      <div class="card glass-card">
        <h2>Plats existants</h2>
        <div class="plats-list">
          <div v-for="plat in platsExistants" 
               :key="plat.idPlat"
               class="plat-item glass-inner">
            <div class="plat-header">
              <h3>{{ plat.nomPlat }}</h3>
              <span class="plat-status glass-badge" :class="{ 
                'utilise': plat.estUtilisePlat, 
                'disponible': !plat.estUtilisePlat 
              }">
                {{ plat.estUtilisePlat ? 'Utilisé' : 'Disponible' }}
              </span>
            </div>
            <div class="plat-details">
              <div class="detail">
                <span class="label">Poids:</span>
                <span class="value">{{ plat.poidsTotalPlat }} kg</span>
              </div>
              <div class="detail">
                <span class="label">Protéines:</span>
                <span class="value">{{ plat.proteinesParKgPlat }} g/kg</span>
              </div>
              <div class="detail">
                <span class="label">Glucides:</span>
                <span class="value">{{ plat.glucidesParKgPlat }} g/kg</span>
              </div>
              <div class="detail">
                <span class="label">Coût:</span>
                <span class="value">{{ plat.coutTotalPlat }} MGA</span>
              </div>
            </div>
            
            <!-- SECTION COMPOSITION CORRIGÉE -->
            <div class="plat-composition">
              <span class="label">Composition:</span>
              <div class="composition-tags">
                <template v-if="plat.compositions && plat.compositions.length > 0">
                  <span v-for="comp in plat.compositions.slice(0, 3)" 
                        :key="comp.idCompositionPlat || comp.aliment?.idAliment || comp.aliment?.id"
                        class="tag glass-badge small">
                    {{ getAlimentName(comp) }} ({{ comp.poidsAlimentComposition }} kg)
                  </span>
                  <span v-if="plat.compositions.length > 3" class="tag glass-badge small">
                    +{{ plat.compositions.length - 3 }} autres
                  </span>
                </template>
                <span v-else class="tag glass-badge small">
                  Aucun aliment
                </span>
              </div>
            </div>
            <!-- FIN SECTION COMPOSITION -->
            
          </div>
        </div>
        
        <div v-if="platsExistants.length === 0" class="no-plats glass-inner">
          <div class="empty-icon">🍽️</div>
          <p>Aucun plat créé pour le moment</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, computed, onMounted } from 'vue'
import platService from '../services/PlatService'
import alimentService from '../services/alimentService'

export default {
  name: 'PlatCreation',
  setup() {
    // Données
    const aliments = ref([])
    const platsExistants = ref([])
    const loading = ref(false)
    const message = ref('')
    const messageClass = ref('')
    
    // Nouveau plat
    const nouveauPlat = ref({
      nomPlat: '',
      descriptionPlat: ''
    })
    
    // Composition du plat
    const compositions = ref([])
    
    // Méthode pour obtenir le nom de l'aliment
    const getAlimentName = (composition) => {
      if (!composition.aliment) return 'Aliment inconnu'
      return composition.aliment.nomAliment || composition.aliment.nom || 'Aliment sans nom'
    }
    
    // Charger les données
    const chargerDonnees = async () => {
      try {
        // Charger les aliments
        const alimentsData = await alimentService.getAliments()
        aliments.value = alimentsData.filter(a => a.estActifAliment !== false)
        
        // Charger les plats existants
        const platsData = await platService.getAllPlats()
        platsExistants.value = platsData
        
      } catch (error) {
        console.error('Erreur chargement données:', error)
        afficherMessage('Erreur de chargement', 'error')
      }
    }
    
    // Calculs
    const poidsTotal = computed(() => {
      return compositions.value.reduce((total, comp) => {
        return total + (parseFloat(comp.quantite) || 0)
      }, 0)
    })
    
    const proteinesTotal = computed(() => {
      return compositions.value.reduce((total, comp) => {
        const quantiteKg = parseFloat(comp.quantite) || 0
        const proteinesParKg = parseFloat(comp.aliment.proteinesParKgAliment) || 0
        return total + (quantiteKg * proteinesParKg)
      }, 0)
    })
    
    const glucidesTotal = computed(() => {
      return compositions.value.reduce((total, comp) => {
        const quantiteKg = parseFloat(comp.quantite) || 0
        const glucidesParKg = parseFloat(comp.aliment.glucidesParKgAliment) || 0
        return total + (quantiteKg * glucidesParKg)
      }, 0)
    })
    
    const coutTotal = computed(() => {
      return compositions.value.reduce((total, comp) => {
        const quantiteKg = parseFloat(comp.quantite) || 0
        const prixKg = parseFloat(comp.aliment.prixKgAliment) || 0
        return total + (quantiteKg * prixKg)
      }, 0)
    })
    
    const proteinesParKg = computed(() => {
      if (poidsTotal.value <= 0) return 0
      return proteinesTotal.value / poidsTotal.value
    })
    
    const glucidesParKg = computed(() => {
      if (poidsTotal.value <= 0) return 0
      return glucidesTotal.value / poidsTotal.value
    })
    
    const coutParKg = computed(() => {
      if (poidsTotal.value <= 0) return 0
      return coutTotal.value / poidsTotal.value
    })
    
    const ratioProteinesGlucides = computed(() => {
      if (glucidesTotal.value === 0) return '∞'
      const ratio = proteinesTotal.value / glucidesTotal.value
      return ratio.toFixed(2) + ':1'
    })
    
    const pourcentageProteines = computed(() => {
      const total = proteinesTotal.value + glucidesTotal.value
      if (total === 0) return 0
      return (proteinesTotal.value / total) * 100
    })
    
    const pourcentageGlucides = computed(() => {
      const total = proteinesTotal.value + glucidesTotal.value
      if (total === 0) return 0
      return (glucidesTotal.value / total) * 100
    })
    
    const gainPotentiel = computed(() => {
      // Gain pour 5g de plat selon les règles
      const proteinesPour5g = (proteinesParKg.value * 5) / 1000
      const glucidesPour5g = (glucidesParKg.value * 5) / 1000
      
      // Calcul selon les règles du système
      if (proteinesPour5g >= 4 && glucidesPour5g >= 8) return 20
      if (proteinesPour5g >= 4 && glucidesPour5g >= 4) return 15
      if (proteinesPour5g >= 2 && glucidesPour5g >= 4) return 10
      if (proteinesPour5g >= 2 && glucidesPour5g >= 0) return 5
      if (proteinesPour5g >= 0 && glucidesPour5g >= 4) return 5
      
      // Gain proportionnel
      const tauxProteines = Math.min(proteinesPour5g / 2, 2)
      const tauxGlucides = Math.min(glucidesPour5g / 4, 2)
      const tauxMoyen = (tauxProteines + tauxGlucides) / 2
      
      return tauxMoyen * 10
    })
    
    const stockTotal = computed(() => {
      return aliments.value.reduce((total, aliment) => {
        return total + (parseFloat(aliment.stockAliment) || 0)
      }, 0)
    })
    
    const peutCreer = computed(() => {
      return nouveauPlat.value.nomPlat.trim() !== '' && 
             compositions.value.length > 0 && 
             poidsTotal.value > 0
    })
    
    // Méthodes
    const ajouterAuPlat = (aliment) => {
      // Vérifier si l'aliment est déjà dans le plat
      const index = compositions.value.findIndex(
        comp => comp.aliment.idAliment === aliment.idAliment
      )
      
      if (index === -1) {
        // Ajouter nouvel aliment
        compositions.value.push({
          aliment: { ...aliment },
          quantite: 1.0 // kg par défaut
        })
      } else {
        // Augmenter la quantité
        compositions.value[index].quantite += 0.5
      }
      
      calculerTotaux()
    }
    
    const retirerAliment = (index) => {
      compositions.value.splice(index, 1)
      calculerTotaux()
    }
    
    const calculerProteines = (composition) => {
      const quantiteKg = parseFloat(composition.quantite) || 0
      const proteinesParKg = parseFloat(composition.aliment.proteinesParKgAliment) || 0
      return quantiteKg * proteinesParKg
    }
    
    const calculerGlucides = (composition) => {
      const quantiteKg = parseFloat(composition.quantite) || 0
      const glucidesParKg = parseFloat(composition.aliment.glucidesParKgAliment) || 0
      return quantiteKg * glucidesParKg
    }
    
    const calculerCout = (composition) => {
      const quantiteKg = parseFloat(composition.quantite) || 0
      const prixKg = parseFloat(composition.aliment.prixKgAliment) || 0
      return quantiteKg * prixKg
    }
    
    const calculerTotaux = () => {
      // Cette méthode est appelée automatiquement par les computed properties
    }

    
    
    // Dans PlatsList.vue - partie script
const creerPlat = async () => {
  if (!peutCreer.value) return
  
  loading.value = true
  message.value = ''
  
  try {
    // PRÉPARER LES DONNÉES DANS LE FORMAT EXACT ATTENDU PAR LE DTO
    const platData = {
      nomPlat: nouveauPlat.value.nomPlat.trim(),
      descriptionPlat: nouveauPlat.value.descriptionPlat || '',
      datePreparationPlat: new Date().toISOString().split('T')[0],
      estUtilisePlat: false,
      compositions: []
    }
    
    // CONVERSION DES COMPOSITIONS AU FORMAT DTO
    platData.compositions = compositions.value.map(comp => {
      console.log('🔍 Composition avant conversion:', comp)
      
      // Vérifier que l'aliment existe
      if (!comp.aliment || !comp.aliment.idAliment) {
        throw new Error(`Aliment invalide dans la composition: ${JSON.stringify(comp)}`)
      }
      
      const quantite = parseFloat(comp.quantite)
      if (isNaN(quantite) || quantite <= 0) {
        throw new Error(`Quantité invalide: ${comp.quantite}`)
      }
      
      // FORMAT EXACT DU DTO : { idAliment: X, poidsAlimentComposition: Y }
      return {
        idAliment: comp.aliment.idAliment,
        poidsAlimentComposition: quantite
      }
    })
    
    console.log('📤 Envoi DTO au format correct:', JSON.stringify(platData, null, 2))
    
    // Afficher un aperçu pour débogage
    console.log('📋 Aperçu des compositions:')
    platData.compositions.forEach((comp, index) => {
      const aliment = aliments.value.find(a => a.idAliment === comp.idAliment)
      console.log(`  ${index + 1}. Aliment ID: ${comp.idAliment}, Nom: ${aliment?.nomAliment || 'Inconnu'}, Poids: ${comp.poidsAlimentComposition}kg`)
    })
    
    // Créer le plat
    const result = await platService.createPlat(platData)
    console.log('✅ Réponse du serveur:', result)
    
    afficherMessage('Plat créé avec succès!', 'success')
    
    // Recharger les données
    await chargerDonnees()
    
    // Réinitialiser le formulaire
    reinitialiser()
    
  } catch (error) {
    console.error('❌ Erreur détaillée création plat:', error)
    
    let errorMsg = error.message
    
    // Traductions des messages d'erreur courants
    if (error.message.includes('idAliment') && error.message.includes('null')) {
      errorMsg = 'Erreur: Un aliment n\'a pas d\'ID valide. Essayez de le retirer et de le réajouter.'
    } else if (error.message.includes('Stock insuffisant')) {
      errorMsg = 'Erreur: Stock insuffisant pour un des aliments.'
    } else if (error.message.includes('400')) {
      errorMsg = 'Erreur: Données invalides envoyées au serveur.'
    } else if (error.message.includes('404')) {
      errorMsg = 'Erreur: Aliment non trouvé. Les aliments ont-ils été modifiés?'
    } else if (error.message.includes('500')) {
      errorMsg = 'Erreur serveur interne. Contactez l\'administrateur.'
    }
    
    afficherMessage(errorMsg, 'error')
    
  } finally {
    loading.value = false
  }
}

    const reinitialiser = () => {
      nouveauPlat.value = {
        nomPlat: '',
        descriptionPlat: ''
      }
      compositions.value = []
      message.value = ''
    }
    
    const afficherMessage = (text, type) => {
      message.value = text
      messageClass.value = type
      
      setTimeout(() => {
        message.value = ''
      }, 5000)
    }

    
    
    // Initialisation
    onMounted(() => {
      chargerDonnees()
    })
    
    return {
      // Données
      aliments,
      platsExistants,
      loading,
      message,
      messageClass,
      nouveauPlat,
      compositions,
      
      // Computed
      poidsTotal,
      proteinesTotal,
      glucidesTotal,
      coutTotal,
      proteinesParKg,
      glucidesParKg,
      coutParKg,
      ratioProteinesGlucides,
      pourcentageProteines,
      pourcentageGlucides,
      gainPotentiel,
      stockTotal,
      peutCreer,
      
      // Méthodes
      getAlimentName,
      ajouterAuPlat,
      retirerAliment,
      calculerProteines,
      calculerGlucides,
      calculerCout,
      calculerTotaux,
      creerPlat,
      reinitialiser
    }
  }
}
</script>

<style scoped>
@import '../assets/styles/plats.css';
</style>

