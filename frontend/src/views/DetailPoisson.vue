<template>
  <div class="poisson-detail-page" v-if="!loading">
    <!-- Header avec navigation -->
    <div class="page-header">
      <div class="header-top">
        <button @click="$router.push('/poissons')" class="btn-back">
          ← Retour
        </button>
        <div class="header-actions">
          <button @click="refreshData" class="btn-refresh" title="Rafraîchir">
            🔄
          </button>
          <button @click="editPoisson" class="btn-edit">
            ✏️ Modifier
          </button>
          <button v-if="poisson.estEnViePoisson && !poisson.estVenduPoisson" 
                  @click="vendrePoisson" 
                  class="btn-sell">
            💰 Vendre
          </button>
        </div>
      </div>
      
      <div class="header-content">
        <div class="poisson-avatar">
          <span class="avatar-icon">🐟</span>
          <span v-if="poisson.estPretAVendre" class="badge ready">💰</span>
          <span v-if="!poisson.estRassasiePoisson && poisson.estEnViePoisson" class="badge hungry">🍽️</span>
        </div>
        
        <div class="poisson-info">
          <h1>{{ poisson.nomPoisson }}</h1>
          <div class="poisson-meta">
            <span class="race">{{ poisson.racePoisson?.nomRacePoisson || 'Non spécifié' }}</span>
            <span class="status-badge" :class="getStatusClass()">
              {{ getStatusText() }}
            </span>
          </div>
        </div>
      </div>
    </div>

    <!-- Statistiques principales -->
    <div class="main-stats">
      <div class="stat-card">
        <div class="stat-icon">⚖️</div>
        <div class="stat-content">
          <h3>Poids actuel</h3>
          <div class="stat-value">{{ formatPoids(poisson.poidsActuelPoisson) }}g</div>
          <div class="stat-change" :class="getWeightTrendClass()">
            {{ getWeightTrend() }}
          </div>
        </div>
      </div>
      
      <div class="stat-card">
        <div class="stat-icon">🏆</div>
        <div class="stat-content">
          <h3>Progression</h3>
          <div class="stat-value">{{ progression }}%</div>
          <div class="progress-bar">
            <div class="progress-fill" :style="{ width: progression + '%' }"></div>
          </div>
        </div>
      </div>
      
      <div class="stat-card">
        <div class="stat-icon">📈</div>
        <div class="stat-content">
          <h3>Gain maximum/jour</h3>
          <div class="stat-value">{{ formatPoids(poisson.capaciteAugmentationPoisson) }}g</div>
          <div class="stat-label">Capacité</div>
        </div>
      </div>
      
      <div class="stat-card">
        <div class="stat-icon">💰</div>
        <div class="stat-content">
          <h3>Valeur</h3>
          <div class="stat-value">{{ formatPrice(poisson.prixVentePoisson) }} MGA</div>
          <div class="stat-label">Achat: {{ formatPrice(poisson.prixAchatPoisson) }} MGA</div>
        </div>
      </div>
    </div>

    <!-- Section d'informations -->
    <div class="info-sections">
      <!-- Informations générales -->
      <div class="info-section">
        <h2>Informations générales</h2>
        <div class="info-grid">
          <div class="info-item">
            <span class="info-label">Nom</span>
            <span class="info-value">{{ poisson.nomPoisson }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">Race</span>
            <span class="info-value">{{ poisson.racePoisson?.nomRacePoisson || 'Non spécifié' }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">Date d'arrivée</span>
            <span class="info-value">{{ formatDate(poisson.dateArriveePoisson) }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">Poids initial</span>
            <span class="info-value">{{ formatPoids(poisson.poidsInitialPoisson) }}g</span>
          </div>
          <div class="info-item">
            <span class="info-label">Poids maximum</span>
            <span class="info-value">{{ formatPoids(poisson.poidsMaximalPoisson) }}g</span>
          </div>
          <div class="info-item">
            <span class="info-label">Poids actuel</span>
            <span class="info-value">{{ formatPoids(poisson.poidsActuelPoisson) }}g</span>
          </div>
          <div class="info-item">
            <span class="info-label">Progression</span>
            <span class="info-value">{{ progression }}%</span>
          </div>
          <div class="info-item">
            <span class="info-label">Capacité augmentation/jour</span>
            <span class="info-value">{{ formatPoids(poisson.capaciteAugmentationPoisson) }}g</span>
          </div>
          <div class="info-item">
            <span class="info-label">Prix d'achat</span>
            <span class="info-value">{{ formatPrice(poisson.prixAchatPoisson) }} MGA</span>
          </div>
          <div class="info-item">
            <span class="info-label">Prix de vente</span>
            <span class="info-value">{{ formatPrice(poisson.prixVentePoisson) }} MGA</span>
          </div>
          <div class="info-item">
            <span class="info-label">Date de vente</span>
            <span class="info-value">{{ poisson.dateVentePoisson ? formatDate(poisson.dateVentePoisson) : 'Non vendu' }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">Statut</span>
            <span class="info-value">{{ getStatusText() }}</span>
          </div>
        </div>
      </div>

      <!-- Historique de nourrissage -->
      <div class="info-section">
        <div class="section-header">
          <h2>Historique de nourrissage</h2>
          <button @click="ouvrirNourrissageModal" class="btn-nourrir" 
                  v-if="poisson.estEnViePoisson && !poisson.estVenduPoisson && !poisson.estRassasiePoisson">
            🍽️ Nourrir maintenant
          </button>
        </div>
        
        <div v-if="historique.length > 0" class="historique-table">
          <div class="table-responsive">
            <table>
              <thead>
                <tr>
                  <th>Date</th>
                  <th>Heure</th>
                  <th>Quantité nourriture (g)</th>
                  <th>Protéines reçues (g)</th>
                  <th>Glucides reçus (g)</th>
                  <th>Poids avant (g)</th>
                  <th>Poids après (g)</th>
                  <th>Gain (g)</th>
                  <th>Satisfaction</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="nourrissage in historique" :key="nourrissage.idFisakafoanana">
                  <td>{{ formatDate(nourrissage.dateNourrissageFisakafoanana) }}</td>
                  <td>{{ formatTime(nourrissage.heureNourrissageFisakafoanana) }}</td>
                  <td>{{ formatNutrient(nourrissage.quantiteNourritureFisakafoanana) }}</td>
                  <td>{{ formatNutrient(nourrissage.proteinesRecuesFisakafoanana) }}</td>
                  <td>{{ formatNutrient(nourrissage.glucidesRecusFisakafoanana) }}</td>
                  <td>{{ formatPoids(nourrissage.ancienPoidsFisakafoanana) }}</td>
                  <td>{{ formatPoids(nourrissage.nouveauPoidsFisakafoanana) }}</td>
                  <td>
                    <span class="gain-positive" v-if="nourrissage.gainPoidsFisakafoanana > 0">
                      +{{ formatPoids(nourrissage.gainPoidsFisakafoanana) }}
                    </span>
                    <span v-else>
                      {{ formatPoids(nourrissage.gainPoidsFisakafoanana) }}
                    </span>
                  </td>
                  <td>
                    <div class="satisfaction-bar">
                      <div class="satisfaction-fill" 
                           :style="{ width: nourrissage.tauxSatisfactionFisakafoanana + '%' }"
                           :class="getSatisfactionClass(nourrissage.tauxSatisfactionFisakafoanana)">
                      </div>
                      <span class="satisfaction-text">{{ nourrissage.tauxSatisfactionFisakafoanana }}%</span>
                    </div>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
        
        <div v-else class="no-history">
          <div class="no-history-icon">🍽️</div>
          <h3>Aucun historique de nourrissage</h3>
          <p v-if="poisson.estEnViePoisson && !poisson.estVenduPoisson && !poisson.estRassasiePoisson">
            Ce poisson est affamé ! Nourrissez-le maintenant.
          </p>
          <p v-else-if="poisson.estRassasiePoisson">
            Ce poisson est rassasié pour le moment.
          </p>
        </div>
        
        <!-- Statistiques de nourrissage -->
        <div v-if="historique.length > 0" class="nourrissage-stats">
          <div class="stats-grid">
            <div class="stat-item">
              <span class="stat-label">Total nourrissages</span>
              <span class="stat-value">{{ historique.length }}</span>
            </div>
            <div class="stat-item">
              <span class="stat-label">Gain total</span>
              <span class="stat-value">{{ formatPoids(totalGain) }}g</span>
            </div>
            <div class="stat-item">
              <span class="stat-label">Nourriture totale</span>
              <span class="stat-value">{{ formatNutrient(totalNourriture) }}g</span>
            </div>
            <div class="stat-item">
              <span class="stat-label">Satisfaction moyenne</span>
              <span class="stat-value">{{ satisfactionMoyenne }}%</span>
            </div>
          </div>
        </div>
      </div>

      <!-- Graphique d'évolution -->
      <div class="info-section">
        <h2>Évolution du poids</h2>
        <div class="chart-container">
          <canvas ref="evolutionChartRef"></canvas>
        </div>
      </div>
    </div>

    <!-- Actions rapides -->
    <div class="quick-actions">
      <h2>Actions rapides</h2>
      <div class="actions-grid">
        <button @click="toggleNourri" class="action-btn" 
                :class="{ 'fed': poisson.estRassasiePoisson }"
                v-if="poisson.estEnViePoisson && !poisson.estVenduPoisson">
          <span class="action-icon">{{ poisson.estRassasiePoisson ? '✅' : '🍽️' }}</span>
          <span class="action-label">
            {{ poisson.estRassasiePoisson ? 'Marquer affamé' : 'Marquer rassasié' }}
          </span>
        </button>
        
        <button @click="vendrePoisson" class="action-btn sell"
                v-if="poisson.estEnViePoisson && !poisson.estVenduPoisson && poisson.estPretAVendre">
          <span class="action-icon">💰</span>
          <span class="action-label">Vendre maintenant</span>
        </button>
        
        <button @click="showTransferModal" class="action-btn"
                v-if="poisson.estEnViePoisson && !poisson.estVenduPoisson">
          <span class="action-icon">🔄</span>
          <span class="action-label">Transférer</span>
        </button>
        
        <button @click="ouvrirDeleteModal" class="action-btn delete">
          <span class="action-icon">🗑️</span>
          <span class="action-label">Supprimer</span>
        </button>
        
        <router-link :to="'/poissons/' + poisson.idPoisson + '/edit'" class="action-btn edit">
          <span class="action-icon">✏️</span>
          <span class="action-label">Modifier</span>
        </router-link>
      </div>
    </div>

    <!-- Modale de nourrissage -->
    <div v-if="showNourrissageModal" class="modal-overlay">
      <div class="modal">
        <h2>Nourrir {{ poisson.nomPoisson }}</h2>
        
        <div class="nourrissage-form">
          <div class="form-group">
            <label>Quantité de nourriture (g)</label>
            <input type="number" v-model="nourrissageQuantite" min="0" step="0.1" 
                   :max="maxQuantiteNourrissage" placeholder="Ex: 5">
          </div>
          
          <div class="form-group">
            <label>Type d'aliment</label>
            <select v-model="selectedAlimentId">
              <option value="">Choisir un aliment</option>
              <option v-for="aliment in aliments" :key="aliment.idAliment" :value="aliment.idAliment">
                {{ aliment.nomAliment }} ({{ aliment.stockAliment }}kg)
              </option>
            </select>
          </div>
          
          <div v-if="selectedAliment" class="aliment-info">
            <h4>Informations nutritionnelles</h4>
            <div class="nutrients-grid">
              <div class="nutrient">
                <span class="nutrient-label">Protéines:</span>
                <span class="nutrient-value">{{ selectedAliment.proteinesParKgAliment }}g/kg</span>
              </div>
              <div class="nutrient">
                <span class="nutrient-label">Glucides:</span>
                <span class="nutrient-value">{{ selectedAliment.glucidesParKgAliment }}g/kg</span>
              </div>
              <div class="nutrient">
                <span class="nutrient-label">Prix:</span>
                <span class="nutrient-value">{{ selectedAliment.prixKgAliment }} MGA/kg</span>
              </div>
            </div>
          </div>
          
          <div class="calculations">
            <h4>Calculs</h4>
            <div class="calc-grid">
              <div class="calc-item">
                <span class="calc-label">Protéines reçues:</span>
                <span class="calc-value">{{ calculerProteines() }}g</span>
              </div>
              <div class="calc-item">
                <span class="calc-label">Glucides reçus:</span>
                <span class="calc-value">{{ calculerGlucides() }}g</span>
              </div>
              <div class="calc-item">
                <span class="calc-label">Gain estimé:</span>
                <span class="calc-value">{{ calculerGainEstime() }}g</span>
              </div>
              <div class="calc-item">
                <span class="calc-label">Coût estimé:</span>
                <span class="calc-value">{{ calculerCout() }} MGA</span>
              </div>
            </div>
          </div>
          
          <div class="modal-actions">
            <button @click="executerNourrissage" class="btn-save" :disabled="!canNourrir || loading">
              <span v-if="loading" class="spinner"></span>
              {{ loading ? 'Nourrissage...' : 'Nourrir' }}
            </button>
            <button @click="fermerNourrissageModal" class="btn-cancel">
              Annuler
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Modale de suppression -->
    <div v-if="showDeleteModal" class="modal-overlay">
      <div class="modal">
        <h3>Confirmer la suppression</h3>
        <p>Voulez-vous vraiment supprimer le poisson "{{ poisson.nomPoisson }}" ?</p>
        <p class="warning-text">⚠️ Cette action est irréversible !</p>
        <div class="modal-actions">
          <button @click="deletePoisson" class="btn-delete-confirm">
            Oui, supprimer
          </button>
          <button @click="fermerDeleteModal" class="btn-cancel">
            Annuler
          </button>
        </div>
      </div>
    </div>
  </div>
  
  <!-- Chargement -->
  <div v-else class="loading-container">
    <div class="spinner-large"></div>
    <p>Chargement des données...</p>
  </div>
</template>

<script>
import { ref, computed, onMounted, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import api from '../services/api'
import { Chart, registerables } from 'chart.js'
import { watch } from 'vue'

Chart.register(...registerables)

export default {
  name: 'PoissonDetail',
  setup() {
    const route = useRoute()
    const router = useRouter()
    
    // Données
    const poisson = ref({})
    const historique = ref([])
    const aliments = ref([])
    const loading = ref(true)
    
    // Modales
    const showNourrissageModal = ref(false)
    const showDeleteModal = ref(false)
    
    // Nourrissage
    const nourrissageQuantite = ref(5)
    const selectedAlimentId = ref('')
    const evolutionChart = ref(null)
    const evolutionChartRef = ref(null)

    // ID du poisson depuis la route
    const poissonId = computed(() => route.params.id)

    // Calculs
    const progression = computed(() => {
      if (!poisson.value.poidsActuelPoisson || !poisson.value.poidsMaximalPoisson) return 0
      const progress = (poisson.value.poidsActuelPoisson / poisson.value.poidsMaximalPoisson) * 100
      return Math.round(progress)
    })

    const selectedAliment = computed(() => {
      return aliments.value.find(a => a.idAliment == selectedAlimentId.value)
    })

    const maxQuantiteNourrissage = computed(() => {
      return selectedAliment.value ? selectedAliment.value.stockAliment * 1000 : 1000
    })

    const canNourrir = computed(() => {
      return nourrissageQuantite.value > 0 && 
             selectedAlimentId.value && 
             nourrissageQuantite.value <= maxQuantiteNourrissage.value
    })

    // Statistiques historique
    const totalGain = computed(() => {
      return historique.value.reduce((sum, h) => sum + (h.gainPoidsFisakafoanana || 0), 0)
    })

    const totalNourriture = computed(() => {
      return historique.value.reduce((sum, h) => sum + (h.quantiteNourritureFisakafoanana || 0), 0)
    })

    const satisfactionMoyenne = computed(() => {
      if (historique.value.length === 0) return 0
      const total = historique.value.reduce((sum, h) => sum + (h.tauxSatisfactionFisakafoanana || 0), 0)
      return Math.round(total / historique.value.length)
    })

    // Charger les données
   const loadData = async () => {
      loading.value = true
      try {
        // Charger le poisson
        const poissonResponse = await api.get(`/poissons/${poissonId.value}`)
        poisson.value = poissonResponse.data
        
        // Charger l'historique
        const historiqueResponse = await api.get(`/fisakafoanana/poisson/${poissonId.value}`)
        historique.value = historiqueResponse.data
        
        // Charger les aliments
        const alimentsResponse = await api.get('/aliments')
        aliments.value = alimentsResponse.data
        
        // Attendre que le DOM soit mis à jour avant d'initialiser le graphique
        await nextTick()
        
        // Initialiser le graphique seulement si on a des données
        if (historique.value.length > 0 && evolutionChartRef.value) {
          initChart()
        }
      } catch (error) {
        console.error('Erreur chargement données:', error)
        alert('Impossible de charger les données du poisson')
      } finally {
        loading.value = false
      }
    }

    // Formater les données
    const formatPoids = (poids) => {
      if (!poids && poids !== 0) return '0'
      return parseFloat(poids).toFixed(2)
    }

    const formatPrice = (price) => {
      if (!price && price !== 0) return '0'
      return parseFloat(price).toFixed(0).replace(/\B(?=(\d{3})+(?!\d))/g, ' ')
    }

    const formatDate = (dateString) => {
      if (!dateString) return 'N/A'
      return new Date(dateString).toLocaleDateString('fr-FR', {
        day: 'numeric',
        month: 'long',
        year: 'numeric'
      })
    }

    const formatTime = (timeString) => {
      if (!timeString) return 'N/A'
      return new Date(`2000-01-01T${timeString}`).toLocaleTimeString('fr-FR', {
        hour: '2-digit',
        minute: '2-digit'
      })
    }

    const formatNutrient = (nutrient) => {
      if (!nutrient && nutrient !== 0) return '0.00'
      return parseFloat(nutrient).toFixed(2)
    }

    // Statut
    const getStatusClass = () => {
      if (!poisson.value.estEnViePoisson) return 'dead'
      if (poisson.value.estVenduPoisson) return 'sold'
      if (poisson.value.estPretAVendre) return 'ready'
      if (poisson.value.estRassasiePoisson) return 'fed'
      return 'hungry'
    }

    const getStatusText = () => {
      if (!poisson.value.estEnViePoisson) return 'Mort'
      if (poisson.value.estVenduPoisson) return 'Vendu'
      if (poisson.value.estPretAVendre) return 'Prêt à vendre'
      if (poisson.value.estRassasiePoisson) return 'Rassasié'
      return 'Affamé'
    }

    const getWeightTrendClass = () => {
      if (historique.value.length === 0) return 'neutral'
      const dernierGain = historique.value[0]?.gainPoidsFisakafoanana || 0
      if (dernierGain > 0) return 'positive'
      if (dernierGain < 0) return 'negative'
      return 'neutral'
    }

    const getWeightTrend = () => {
      if (historique.value.length === 0) return 'Aucun historique'
      const dernierGain = historique.value[0]?.gainPoidsFisakafoanana || 0
      if (dernierGain > 0) return `+${formatPoids(dernierGain)}g depuis dernier repas`
      if (dernierGain < 0) return `${formatPoids(dernierGain)}g depuis dernier repas`
      return 'Poids stable'
    }

    const getSatisfactionClass = (pourcentage) => {
      if (pourcentage >= 100) return 'excellent'
      if (pourcentage >= 80) return 'good'
      if (pourcentage >= 50) return 'medium'
      return 'low'
    }

    // Actions
    const refreshData = () => {
      loadData()
    }

    const editPoisson = () => {
      router.push(`/poissons/${poissonId.value}/edit`)
    }

    const vendrePoisson = async () => {
      if (!confirm(`Vendre ${poisson.value.nomPoisson} pour ${formatPrice(poisson.value.prixVentePoisson)} MGA ?`)) {
        return
      }

      try {
        await api.put(`/poissons/${poissonId.value}/vendre`)
        alert('Poisson vendu avec succès !')
        await loadData()
      } catch (error) {
        console.error('Erreur vente:', error)
        alert('Erreur lors de la vente')
      }
    }

    const toggleNourri = async () => {
      try {
        const updatedPoisson = {
          ...poisson.value,
          estRassasiePoisson: !poisson.value.estRassasiePoisson
        }
        
        await api.put(`/poissons/${poissonId.value}`, updatedPoisson)
        await loadData()
        alert(`Poisson marqué comme ${updatedPoisson.estRassasiePoisson ? 'rassasié' : 'affamé'}`)
      } catch (error) {
        console.error('Erreur mise à jour:', error)
        alert('Erreur lors de la mise à jour')
      }
    }

    // Nourrissage
    const ouvrirNourrissageModal = () => {
      nourrissageQuantite.value = 5
      selectedAlimentId.value = aliments.value[0]?.idAliment || ''
      showNourrissageModal.value = true
    }

    const fermerNourrissageModal = () => {
      showNourrissageModal.value = false
    }

    const calculerProteines = () => {
      if (!selectedAliment.value || !nourrissageQuantite.value) return 0
      
      // Règle de trois : 5g de plat → 2g de protéines
      const proteinesRecues = (2 * nourrissageQuantite.value) / 5
      return proteinesRecues.toFixed(2)
    }

    const calculerGlucides = () => {
      if (!selectedAliment.value || !nourrissageQuantite.value) return 0
      
      // Règle de trois : 5g de plat → 4g de glucides
      const glucidesRecus = (4 * nourrissageQuantite.value) / 5
      return glucidesRecus.toFixed(2)
    }

    const calculerGainEstime = () => {
      const proteines = parseFloat(calculerProteines())
      const glucides = parseFloat(calculerGlucides())
      
      // Règles exactes selon le cahier des charges
      if (proteines >= 4 && glucides >= 8) {
        return '20' // Besoins totaux satisfaits
      } else if (proteines >= 4 && glucides >= 4 && glucides < 8) {
        return '15' // Protéines doublées mais glucides minimum
      } else if (proteines >= 2 && glucides >= 4) {
        return '10' // Besoins minimums satisfaits
      } else if ((proteines >= 2 && glucides < 4) || (proteines < 2 && glucides >= 4)) {
        return '5'  // Un seul type de nutriment reçu
      } else {
        return '0'  // Nutrition insuffisante
      }
    }

    const calculerCout = () => {
      if (!selectedAliment.value || !nourrissageQuantite.value) return 0
      
      // Coût = prix par kg * quantité en kg
      const quantiteKg = nourrissageQuantite.value / 1000
      const cout = selectedAliment.value.prixKgAliment * quantiteKg
      return cout.toFixed(2)
    }

    const executerNourrissage = async () => {
      try {
        loading.value = true
        
        // Calculer les nutriments par kg
        const proteinesParKg = selectedAliment.value.proteinesParKgAliment
        const glucidesParKg = selectedAliment.value.glucidesParKgAliment
        
        // Convertir en kg pour l'API
        const quantiteKg = nourrissageQuantite.value / 1000
        
        // Appeler l'API de nourrissage
        await api.post('/nourrissage/nourrir', null, {
          params: {
            quantitePlat: quantiteKg,
            proteinesParKg: proteinesParKg,
            glucidesParKg: glucidesParKg
          }
        })
        
        await loadData()
        fermerNourrissageModal()
        alert('Poisson nourri avec succès !')
      } catch (error) {
        console.error('Erreur nourrissage:', error)
        alert('Erreur lors du nourrissage: ' + (error.response?.data?.message || error.message))
      } finally {
        loading.value = false
      }
    }

    // Suppression
    const ouvrirDeleteModal = () => {
      showDeleteModal.value = true
    }

    const fermerDeleteModal = () => {
      showDeleteModal.value = false
    }

    const deletePoisson = async () => {
      try {
        await api.delete(`/poissons/${poissonId.value}`)
        alert('Poisson supprimé avec succès')
        router.push('/poissons')
      } catch (error) {
        console.error('Erreur suppression:', error)
        alert('Erreur lors de la suppression')
      }
    }

    // Graphique
   const initChart = () => {
    try {
      // Détruire le graphique existant
      if (evolutionChart.value) {
        evolutionChart.value.destroy()
      }

      // Vérifier que la référence existe
      if (!evolutionChartRef.value) {
        console.error('Canvas reference not found')
        return
      }

      const ctx = evolutionChartRef.value.getContext('2d')
      if (!ctx) return

      // Vérifier qu'il y a des données
      if (historique.value.length === 0) {
        console.warn('Aucune donnée pour le graphique')
        return
      }

      // Préparer les données - s'assurer qu'elles sont dans le bon ordre
      const historiqueSorted = [...historique.value].sort((a, b) => 
        new Date(a.dateNourrissageFisakafoanana) - new Date(b.dateNourrissageFisakafoanana)
      )
      
      const dates = historiqueSorted.map(h => {
        try {
          const date = new Date(h.dateNourrissageFisakafoanana)
          return `${date.getDate()}/${date.getMonth() + 1}`
        } catch (e) {
          return 'N/A'
        }
      })
      
      const poidsApres = historiqueSorted.map(h => h.nouveauPoidsFisakafoanana || 0)
      const gains = historiqueSorted.map(h => h.gainPoidsFisakafoanana || 0)

      // Vérifier que les données sont valides
      if (poidsApres.length === 0 || gains.length === 0) {
        console.warn('Données insuffisantes pour le graphique')
        return
      }

      // Créer le graphique
      evolutionChart.value = new Chart(ctx, {
        type: 'line',
        data: {
          labels: dates,
          datasets: [
            {
              label: 'Poids (g)',
              data: poidsApres,
              borderColor: '#667eea',
              backgroundColor: 'rgba(102, 126, 234, 0.1)',
              borderWidth: 2,
              fill: true,
              tension: 0.4,
              yAxisID: 'y'
            },
            {
              label: 'Gains (g)',
              data: gains,
              borderColor: '#48bb78',
              backgroundColor: 'rgba(72, 187, 120, 0.1)',
              borderWidth: 2,
              fill: false,
              tension: 0.4,
              yAxisID: 'y1'
            }
          ]
        },
        options: {
          responsive: true,
          maintainAspectRatio: false,
          interaction: {
            mode: 'index',
            intersect: false,
          },
          plugins: {
            legend: {
              display: true,
              position: 'top',
              labels: {
                usePointStyle: true,
              }
            },
            tooltip: {
              enabled: true,
              mode: 'index',
              intersect: false
            }
          },
          scales: {
            x: {
              title: {
                display: true,
                text: 'Date',
                color: '#4a5568'
              },
              grid: {
                display: false
              }
            },
            y: {
              type: 'linear',
              display: true,
              position: 'left',
              title: {
                display: true,
                text: 'Poids (g)',
                color: '#4a5568'
              },
              grid: {
                color: 'rgba(226, 232, 240, 0.5)'
              }
            },
            y1: {
              type: 'linear',
              display: true,
              position: 'right',
              title: {
                display: true,
                text: 'Gain (g)',
                color: '#4a5568'
              },
              grid: {
                drawOnChartArea: false,
              }
            }
          }
        }
      })
      
      console.log('Graphique créé avec succès')
    } catch (error) {
      console.error('Erreur lors de la création du graphique:', error)
    }
  }

  watch(historique, (newVal) => {
    if (newVal.length > 0) {
      // Attendre un peu pour que le DOM soit prêt
      setTimeout(() => {
        initChart()
      }, 300)
    }
  }, { deep: true })

    // Transfert (à implémenter si besoin)
    const showTransferModal = () => {
      alert('Fonctionnalité de transfert à implémenter')
    }

    // Initialisation
    onMounted(() => {
      loadData()
    })

    return {
      // Données
      poisson,
      historique,
      aliments,
      loading,
      showNourrissageModal,
      showDeleteModal,
      nourrissageQuantite,
      selectedAlimentId,
      evolutionChartRef,
      
      // Computed
      progression,
      selectedAliment,
      maxQuantiteNourrissage,
      canNourrir,
      totalGain,
      totalNourriture,
      satisfactionMoyenne,
      
      // Méthodes
      formatPoids,
      formatPrice,
      formatDate,
      formatTime,
      formatNutrient,
      getStatusClass,
      getStatusText,
      getWeightTrendClass,
      getWeightTrend,
      getSatisfactionClass,
      refreshData,
      editPoisson,
      vendrePoisson,
      toggleNourri,
      ouvrirNourrissageModal,
      fermerNourrissageModal,
      calculerProteines,
      calculerGlucides,
      calculerGainEstime,
      calculerCout,
      executerNourrissage,
      ouvrirDeleteModal,
      fermerDeleteModal,
      deletePoisson,
      showTransferModal
    }
  }
}
</script>

<style scoped>
@import '../assets/styles/detail.css';
</style>

