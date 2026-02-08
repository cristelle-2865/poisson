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
        <div class="card">
          <h2>1. Informations du plat</h2>
          
          <div class="form-group">
            <label for="nomPlat">Nom du plat *</label>
            <input 
              type="text" 
              id="nomPlat" 
              v-model="nouveauPlat.nomPlat" 
              placeholder="Ex: Mélange croissance rapide"
              required>
          </div>
          
          <div class="form-group">
            <label for="descriptionPlat">Description</label>
            <textarea 
              id="descriptionPlat" 
              v-model="nouveauPlat.descriptionPlat" 
              placeholder="Description du plat..."
              rows="3"></textarea>
          </div>
        </div>

        <!-- Sélection des aliments -->
        <div class="card">
          <h2>2. Composition du plat</h2>
          
          <div class="aliments-disponibles">
            <h3>Aliments disponibles</h3>
            <div class="aliments-list">
              <div v-for="aliment in aliments" 
                   :key="aliment.idAliment"
                   class="aliment-item"
                   @click="ajouterAuPlat(aliment)">
                <div class="aliment-header">
                  <h4>{{ aliment.nomAliment }}</h4>
                  <span class="aliment-stock">{{ aliment.stockAliment }} kg</span>
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
                   class="composition-item">
                <div class="composition-header">
                  <h4>{{ comp.aliment.nomAliment }}</h4>
                  <button @click="retirerAliment(index)" class="btn-retirer">×</button>
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
                        @input="calculerTotaux">
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

          <div v-else class="empty-state">
            <p>👈 Cliquez sur un aliment pour l'ajouter au plat</p>
          </div>
        </div>
      </div>

      <!-- Panneau de visualisation -->
      <div class="preview-panel">
        <div class="card">
          <h2>Résumé du plat</h2>
          
          <!-- Totaux -->
          <div class="totaux-section">
            <div class="total-item">
              <span class="label">Poids total:</span>
              <span class="value">{{ poidsTotal.toFixed(2) }} kg</span>
            </div>
            <div class="total-item">
              <span class="label">Protéines totales:</span>
              <span class="value">{{ proteinesTotal.toFixed(2) }} g</span>
            </div>
            <div class="total-item">
              <span class="label">Glucides totales:</span>
              <span class="value">{{ glucidesTotal.toFixed(2) }} g</span>
            </div>
            <div class="total-item">
              <span class="label">Protéines par kg:</span>
              <span class="value">{{ proteinesParKg.toFixed(2) }} g/kg</span>
            </div>
            <div class="total-item">
              <span class="label">Glucides par kg:</span>
              <span class="value">{{ glucidesParKg.toFixed(2) }} g/kg</span>
            </div>
            <div class="total-item total-cout">
              <span class="label">Coût total:</span>
              <span class="value">{{ coutTotal.toFixed(2) }} MGA</span>
            </div>
          </div>

          <!-- Analyse nutritionnelle -->
          <div class="analyse-section">
            <h3>Analyse nutritionnelle</h3>
            
            <div class="analyse-item">
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

            <div class="analyse-item">
              <div class="analyse-header">
                <span class="label">Coût par kg</span>
                <span class="value">{{ coutParKg.toFixed(2) }} MGA/kg</span>
              </div>
            </div>

            <div class="analyse-item">
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
              class="btn-creer">
              {{ loading ? 'Création en cours...' : 'Créer le plat' }}
            </button>
            
            <button 
              @click="reinitialiser" 
              :disabled="loading"
              class="btn-reset">
              Réinitialiser
            </button>
          </div>

          <!-- Messages -->
          <div v-if="message" class="message" :class="messageClass">
            {{ message }}
          </div>

          <!-- Validation -->
          <div v-if="!peutCreer && compositions.length > 0" class="validation-errors">
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
      <div class="card">
        <h2>Plats existants</h2>
        <div class="plats-list">
          <div v-for="plat in platsExistants" 
               :key="plat.idPlat"
               class="plat-item">
            <div class="plat-header">
              <h3>{{ plat.nomPlat }}</h3>
              <span class="plat-status" :class="{ 
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
                <!-- Afficher seulement si il y a des compositions -->
                <template v-if="plat.compositions && plat.compositions.length > 0">
                  <!-- Afficher les 3 premières compositions -->
                  <span v-for="comp in plat.compositions.slice(0, 3)" 
                        :key="comp.idCompositionPlat || comp.aliment?.idAliment || comp.aliment?.id"
                        class="tag">
                    {{ getAlimentName(comp) }} ({{ comp.poidsAlimentComposition }} kg)
                  </span>
                  <!-- Afficher "+X autres" si plus de 3 compositions -->
                  <span v-if="plat.compositions.length > 3" class="tag">
                    +{{ plat.compositions.length - 3 }} autres
                  </span>
                </template>
                <!-- Message si pas de compositions -->
                <span v-else class="tag">
                  Aucun aliment
                </span>
              </div>
            </div>
            <!-- FIN SECTION COMPOSITION -->
            
          </div>
        </div>
        
        <div v-if="platsExistants.length === 0" class="no-plats">
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
.plat-creation-page {
  padding: 20px;
  max-width: 1400px;
  margin: 0 auto;
}

.page-header {
  margin-bottom: 30px;
}

.page-header h1 {
  margin: 0 0 10px 0;
  color: #2c3e50;
}

.header-info {
  display: flex;
  gap: 20px;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 5px;
  padding: 5px 15px;
  background: #f8f9fa;
  border-radius: 20px;
}

.info-item .label {
  color: #6c757d;
}

.info-item .value {
  font-weight: bold;
  color: #2c3e50;
}

.content-grid {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 20px;
  margin-bottom: 40px;
}

.card {
  background: white;
  border-radius: 10px;
  padding: 20px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.1);
  margin-bottom: 20px;
}

.card h2 {
  margin-top: 0;
  margin-bottom: 20px;
  color: #2c3e50;
  font-size: 1.2em;
  border-bottom: 2px solid #f0f0f0;
  padding-bottom: 10px;
}

.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  margin-bottom: 5px;
  color: #495057;
  font-weight: 500;
}

.form-group input,
.form-group textarea {
  width: 100%;
  padding: 10px;
  border: 1px solid #ced4da;
  border-radius: 5px;
  font-size: 1em;
}

.form-group input:focus,
.form-group textarea:focus {
  border-color: #007bff;
  outline: none;
  box-shadow: 0 0 0 3px rgba(0,123,255,0.25);
}

.aliments-disponibles h3 {
  margin-bottom: 15px;
  color: #495057;
}

.aliments-list {
  max-height: 400px;
  overflow-y: auto;
  border: 1px solid #eee;
  border-radius: 5px;
}

.aliment-item {
  padding: 15px;
  border-bottom: 1px solid #f0f0f0;
  cursor: pointer;
  transition: background 0.3s;
}

.aliment-item:hover {
  background: #f8f9fa;
}

.aliment-item:last-child {
  border-bottom: none;
}

.aliment-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.aliment-header h4 {
  margin: 0;
  color: #2c3e50;
}

.aliment-stock {
  background: #e7f3ff;
  padding: 3px 10px;
  border-radius: 15px;
  font-size: 0.9em;
  color: #007bff;
}

.aliment-details {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 10px;
  font-size: 0.9em;
}

.aliment-details .detail {
  display: flex;
  justify-content: space-between;
  padding: 5px 10px;
  background: #f8f9fa;
  border-radius: 3px;
}

.aliment-details .label {
  color: #6c757d;
}

.aliment-details .value {
  font-weight: bold;
  color: #28a745;
}

.aliments-ajoutes {
  margin-top: 30px;
}

.compositions-list {
  max-height: 500px;
  overflow-y: auto;
}

.composition-item {
  padding: 15px;
  border: 1px solid #dee2e6;
  border-radius: 8px;
  margin-bottom: 15px;
  background: #f8f9fa;
}

.composition-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.composition-header h4 {
  margin: 0;
  color: #2c3e50;
}

.btn-retirer {
  background: #dc3545;
  color: white;
  border: none;
  width: 30px;
  height: 30px;
  border-radius: 50%;
  cursor: pointer;
  font-size: 1.2em;
  line-height: 1;
}

.btn-retirer:hover {
  background: #c82333;
}

.composition-controls {
  display: grid;
  grid-template-columns: 1fr 2fr;
  gap: 20px;
}

.quantity-control label {
  display: block;
  margin-bottom: 5px;
  color: #495057;
}

.input-group {
  display: flex;
  align-items: center;
  gap: 10px;
}

.input-group input {
  flex: 1;
  padding: 8px;
  border: 1px solid #ced4da;
  border-radius: 4px;
}

.input-group .unit {
  color: #6c757d;
  min-width: 30px;
}

.composition-calculs {
  display: grid;
  grid-template-columns: 1fr;
  gap: 8px;
}

.calcul-item {
  display: flex;
  justify-content: space-between;
  padding: 5px 10px;
  background: white;
  border-radius: 3px;
  font-size: 0.9em;
}

.empty-state {
  text-align: center;
  padding: 40px 20px;
  color: #6c757d;
  font-style: italic;
  background: #f8f9fa;
  border-radius: 8px;
  margin-top: 20px;
}

/* Panneau de prévisualisation */
.totaux-section {
  margin-bottom: 30px;
}

.total-item {
  display: flex;
  justify-content: space-between;
  padding: 12px 15px;
  margin-bottom: 8px;
  background: #f8f9fa;
  border-radius: 5px;
  border-left: 4px solid #007bff;
}

.total-item.total-cout {
  border-left-color: #28a745;
  background: #d4edda;
}

.total-item .label {
  font-weight: 500;
  color: #495057;
}

.total-item .value {
  font-weight: bold;
  color: #2c3e50;
}

.total-cout .value {
  color: #28a745;
}

.analyse-section {
  margin-bottom: 30px;
}

.analyse-section h3 {
  margin-bottom: 15px;
  color: #495057;
}

.analyse-item {
  margin-bottom: 20px;
  padding: 15px;
  background: #f8f9fa;
  border-radius: 8px;
}

.analyse-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 10px;
}

.analyse-bar {
  display: flex;
  height: 30px;
  border-radius: 4px;
  overflow: hidden;
}

.bar-proteines {
  background: #007bff;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.9em;
}

.bar-glucides {
  background: #28a745;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.9em;
}

.gain-explanation {
  margin-top: 10px;
  padding: 10px;
  background: white;
  border-radius: 5px;
  font-size: 0.9em;
}

.gain-explanation ul {
  margin: 5px 0 0 0;
  padding-left: 20px;
  color: #6c757d;
}

.action-buttons {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
}

.btn-creer, .btn-reset {
  flex: 1;
  padding: 12px;
  border: none;
  border-radius: 5px;
  font-weight: bold;
  cursor: pointer;
  transition: background 0.3s;
}

.btn-creer {
  background: #28a745;
  color: white;
}

.btn-creer:hover:not(:disabled) {
  background: #218838;
}

.btn-creer:disabled {
  background: #6c757d;
  cursor: not-allowed;
}

.btn-reset {
  background: #6c757d;
  color: white;
}

.btn-reset:hover:not(:disabled) {
  background: #545b62;
}

.message {
  padding: 15px;
  border-radius: 5px;
  text-align: center;
  margin-top: 20px;
}

.message.success {
  background: #d4edda;
  color: #155724;
  border: 1px solid #c3e6cb;
}

.message.error {
  background: #f8d7da;
  color: #721c24;
  border: 1px solid #f5c6cb;
}

.validation-errors {
  padding: 15px;
  background: #fff3cd;
  border: 1px solid #ffeaa7;
  border-radius: 5px;
  color: #856404;
  margin-top: 20px;
}

.validation-errors ul {
  margin: 10px 0 0 0;
  padding-left: 20px;
}

/* Plats existants */
.existing-plats {
  margin-top: 40px;
}

.plats-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 20px;
}

.plat-item {
  padding: 20px;
  border: 1px solid #dee2e6;
  border-radius: 8px;
  background: white;
}

.plat-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
  padding-bottom: 10px;
  border-bottom: 1px solid #f0f0f0;
}

.plat-header h3 {
  margin: 0;
  color: #2c3e50;
}

.plat-status {
  padding: 3px 10px;
  border-radius: 15px;
  font-size: 0.8em;
  font-weight: bold;
}

.plat-status.utilise {
  background: #dc3545;
  color: white;
}

.plat-status.disponible {
  background: #28a745;
  color: white;
}

.plat-details {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 10px;
  margin-bottom: 15px;
}

.plat-details .detail {
  display: flex;
  justify-content: space-between;
  padding: 5px 10px;
  background: #f8f9fa;
  border-radius: 3px;
  font-size: 0.9em;
}

.plat-details .label {
  color: #6c757d;
}

.plat-details .value {
  font-weight: bold;
  color: #2c3e50;
}

.plat-composition {
  padding-top: 15px;
  border-top: 1px dashed #dee2e6;
}

.composition-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 5px;
  margin-top: 8px;
}

.tag {
  padding: 3px 8px;
  background: #e9ecef;
  border-radius: 15px;
  font-size: 0.8em;
  color: #495057;
}

.no-plats {
  text-align: center;
  padding: 40px 20px;
  color: #6c757d;
  font-style: italic;
}

/* Responsive */
@media (max-width: 1200px) {
  .content-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .composition-controls {
    grid-template-columns: 1fr;
  }
  
  .plats-list {
    grid-template-columns: 1fr;
  }
}
</style>

