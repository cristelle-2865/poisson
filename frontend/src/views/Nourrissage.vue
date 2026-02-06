<template>
  <div class="nourrissage-page">
    <div class="page-header">
      <h1>Nourrissage des Poissons</h1>
      <div class="header-info">
        <div class="info-item">
          <span class="label">Poissons affamés:</span>
          <span class="value hungry">{{ stats.poissonsAffames || 0 }}</span>
        </div>
        <div class="info-item">
          <span class="label">Besoin total:</span>
          <span class="value">{{ besoinTotal }} g</span>
        </div>
        <div class="info-item">
          <span class="label">Coût estimé:</span>
          <span class="value">{{ coutTotal }} MGA</span>
        </div>
      </div>
    </div>

    <div class="content-grid">
      <!-- Formulaire de nourrissage -->
      <div class="form-section">
        <div class="card">
          <h2>Nouveau Nourrissage</h2>
          
          <!-- Sélection de l'aliment -->
          <div class="food-section">
            <h3>1. Sélectionnez l'aliment</h3>
            <div class="aliment-options">
              <div v-for="aliment in aliments" :key="aliment.idAliment" 
                   class="aliment-option" 
                   :class="{ selected: selectedAlimentId === aliment.idAliment }"
                   @click="selectAliment(aliment)">
                <div class="aliment-icon">🍽️</div>
                <div class="aliment-info">
                  <h4>{{ aliment.nomAliment }}</h4>
                  <div class="aliment-details">
                    <span>Protéines: {{ aliment.proteinesParKg }} g/kg</span>
                    <span>Glucides: {{ aliment.glucidesParKg }} g/kg</span>
                    <span>Stock: {{ aliment.stockAliment }} kg</span>
                  </div>
                </div>
                <div class="aliment-price">{{ aliment.prixKgAliment }} MGA/kg</div>
              </div>
            </div>
            
            <div v-if="showCustomAliment" class="custom-aliment">
              <h4>Aliment personnalisé</h4>
              <div class="custom-inputs">
                <div class="input-group">
                  <label>Protéines (g/kg)</label>
                  <input type="number" v-model="customAliment.proteinesParKg" min="0" step="0.1">
                </div>
                <div class="input-group">
                  <label>Glucides (g/kg)</label>
                  <input type="number" v-model="customAliment.glucidesParKg" min="0" step="0.1">
                </div>
                <div class="input-group">
                  <label>Prix (MGA/kg)</label>
                  <input type="number" v-model="customAliment.prixParKg" min="0" step="100">
                </div>
              </div>
            </div>
            
            <button @click="toggleCustomAliment" class="btn-toggle-custom">
              {{ showCustomAliment ? '← Choisir un aliment existant' : '+ Créer un aliment personnalisé' }}
            </button>
          </div>

          <!-- Quantité et calculs -->
          <div class="quantity-section">
            <h3>2. Définissez la quantité</h3>
            <div class="quantity-control">
              <label>Quantité de plat (kg)</label>
              <div class="quantity-input-group">
                <button @click="decrementQuantity" class="btn-quantity" :disabled="quantitePlat <= 0.1">-</button>
                <input type="number" v-model="quantitePlat" min="0.1" step="0.1" 
                       :max="maxQuantite" @input="updateCalculs">
                <button @click="incrementQuantity" class="btn-quantity" :disabled="quantitePlat >= maxQuantite">+</button>
                <span class="quantity-unit">kg</span>
              </div>
              <div class="quantity-slider">
                <input type="range" v-model="quantitePlat" :min="0.1" :max="maxQuantite" 
                       step="0.1" @input="updateCalculs" class="slider">
                <div class="slider-labels">
                  <span>0.1 kg</span>
                  <span>{{ (maxQuantite / 2).toFixed(1) }} kg</span>
                  <span>{{ maxQuantite.toFixed(1) }} kg</span>
                </div>
              </div>
            </div>

            <!-- Calculs nutritionnels -->
            <div class="calculations-section">
              <h4>Calculs nutritionnels</h4>
              <div class="calculations-grid">
                <div class="calc-item">
                  <span class="calc-label">Protéines totales:</span>
                  <span class="calc-value">{{ proteinesTotales.toFixed(1) }} g</span>
                </div>
                <div class="calc-item">
                  <span class="calc-label">Glucides totales:</span>
                  <span class="calc-value">{{ glucidesTotales.toFixed(1) }} g</span>
                </div>
                <div class="calc-item">
                  <span class="calc-label">Par poisson:</span>
                  <span class="calc-value">{{ proteinesParPoisson.toFixed(1) }}g P / {{ glucidesParPoisson.toFixed(1) }}g G</span>
                </div>
                <div class="calc-item">
                  <span class="calc-label">Coût total:</span>
                  <span class="calc-value">{{ coutTotal }} MGA</span>
                </div>
              </div>
            </div>

            <!-- Gains prévus -->
            <div class="gains-section" v-if="stats.poissonsAffames > 0">
              <h4>Gains de poids prévus</h4>
              <div class="gains-grid">
                <div class="gain-item" v-for="scenario in scenariosGain" :key="scenario.id">
                  <div class="gain-header">
                    <span class="gain-title">{{ scenario.title }}</span>
                    <span class="gain-value">{{ scenario.gain }} g/poisson</span>
                  </div>
                  <div class="gain-details">
                    <span>{{ scenario.proteines }}g P + {{ scenario.glucides }}g G</span>
                  </div>
                </div>
              </div>
              <div class="gain-total">
                <span>Gain total estimé:</span>
                <span class="total-value">{{ gainTotalPrevu }} g</span>
              </div>
            </div>
          </div>

          <!-- Distribution -->
          <div class="distribution-section">
            <h3>3. Mode de distribution</h3>
            <div class="distribution-options">
              <div class="distribution-option" 
                   :class="{ selected: distributionMode === 'equitable' }"
                   @click="distributionMode = 'equitable'">
                <div class="option-icon">⚖️</div>
                <div class="option-content">
                  <h5>Répartition équitable</h5>
                  <p>Même quantité pour chaque poisson affamé</p>
                </div>
              </div>
              
              <div class="distribution-option"
                   :class="{ selected: distributionMode === 'proportionnel' }"
                   @click="distributionMode = 'proportionnel'">
                <div class="option-icon">📊</div>
                <div class="option-content">
                  <h5>Proportionnel au poids</h5>
                  <p>Plus pour les gros poissons</p>
                </div>
              </div>
              
              <div class="distribution-option"
                   :class="{ selected: distributionMode === 'prioritaire' }"
                   @click="distributionMode = 'prioritaire'">
                <div class="option-icon">🎯</div>
                <div class="option-content">
                  <h5>Par priorité</h5>
                  <p>Les plus affamés d'abord</p>
                </div>
              </div>
            </div>
          </div>

          <!-- Actions -->
          <div class="action-section">
            <button @click="executerNourrissage" :disabled="!canNourrir || loading" class="btn-nourrir">
              <span v-if="loading" class="spinner"></span>
              {{ loading ? 'Nourrissage en cours...' : `Nourrir ${stats.poissonsAffames} poissons` }}
            </button>
            <div class="validation-message" :class="validationClass">
              {{ validationMessage }}
            </div>
          </div>
        </div>
      </div>

      <!-- Panneau de visualisation -->
      <div class="visualization-section">
        <!-- Résumé nutritionnel -->
        <div class="card">
          <h2>Résumé nutritionnel</h2>
          <div class="nutrition-summary">
            <div class="satisfaction-meters">
              <div class="meter">
                <div class="meter-header">
                  <span class="meter-label">Protéines</span>
                  <span class="meter-value">{{ satisfaction.proteines }}%</span>
                </div>
                <div class="meter-bar">
                  <div class="meter-fill" :style="{ width: satisfaction.proteines + '%' }"
                       :class="getSatisfactionClass(satisfaction.proteines)"></div>
                </div>
                <div class="meter-details">
                  {{ proteinesParPoisson.toFixed(1) }}g reçus / 2g besoins
                </div>
              </div>
              
              <div class="meter">
                <div class="meter-header">
                  <span class="meter-label">Glucides</span>
                  <span class="meter-value">{{ satisfaction.glucides }}%</span>
                </div>
                <div class="meter-bar">
                  <div class="meter-fill" :style="{ width: satisfaction.glucides + '%' }"
                       :class="getSatisfactionClass(satisfaction.glucides)"></div>
                </div>
                <div class="meter-details">
                  {{ glucidesParPoisson.toFixed(1) }}g reçus / 4g besoins
                </div>
              </div>
            </div>
            
            <div class="satisfaction-overall">
              <div class="overall-value" :class="getOverallClass(satisfaction.moyenne)">
                {{ satisfaction.moyenne }}%
              </div>
              <div class="overall-label">
                Satisfaction nutritionnelle moyenne
              </div>
              <div class="overall-message">
                {{ getSatisfactionMessage(satisfaction.moyenne) }}
              </div>
            </div>
          </div>
        </div>

        <!-- Prévisions de gain -->
        <div class="card">
          <h2>Prévisions de gain</h2>
          <div class="gain-predictions">
            <div class="prediction-chart">
              <div class="chart-bars">
                <div v-for="(gain, index) in gainsParPoisson" :key="index" class="chart-bar-container">
                  <div class="chart-bar" :style="{ height: (gain / 20 * 100) + '%' }"
                       :class="getGainClass(gain)">
                    <span class="bar-value">{{ gain }}g</span>
                  </div>
                  <div class="bar-label">P{{ index + 1 }}</div>
                </div>
              </div>
            </div>
            
            <div class="prediction-stats">
              <div class="stat-item">
                <span class="stat-label">Gain minimum:</span>
                <span class="stat-value">{{ gainMin }} g</span>
              </div>
              <div class="stat-item">
                <span class="stat-label">Gain maximum:</span>
                <span class="stat-value">{{ gainMax }} g</span>
              </div>
              <div class="stat-item">
                <span class="stat-label">Gain moyen:</span>
                <span class="stat-value">{{ gainMoyen }} g</span>
              </div>
              <div class="stat-item total">
                <span class="stat-label">Gain total:</span>
                <span class="stat-value">{{ gainTotalPrevu }} g</span>
              </div>
            </div>
          </div>
        </div>

        <!-- Liste des poissons -->
        <div class="card">
          <h2>Poissons à nourrir ({{ poissonsAffames.length }})</h2>
          <div class="fish-list">
            <div v-for="poisson in poissonsAffames" :key="poisson.idPoisson" class="fish-item">
              <div class="fish-main">
                <div class="fish-name">{{ poisson.nomPoisson }}</div>
                <div class="fish-race">{{ poisson.racePoisson?.nomRacePoisson }}</div>
              </div>
              <div class="fish-stats">
                <div class="stat">
                  <span class="label">Poids:</span>
                  <span class="value">{{ formatPoids(poisson.poidsActuelPoisson) }}g</span>
                </div>
                <div class="stat">
                  <span class="label">Max:</span>
                  <span class="value">{{ formatPoids(poisson.poidsMaximalPoisson) }}g</span>
                </div>
                <div class="stat">
                  <span class="label">Prog:</span>
                  <span class="value">{{ getProgression(poisson) }}%</span>
                </div>
              </div>
              <div class="fish-gain">
                <div class="gain-indicator">
                  <div class="gain-bar">
                    <div class="gain-fill" :style="{ width: getAllocation(poisson) + '%' }"></div>
                  </div>
                  <span class="gain-text">{{ getGainPrevu(poisson) }}g</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Résultat du nourrissage -->
    <div v-if="resultatNourrissage" class="result-modal">
      <div class="result-content">
        <div class="result-header">
          <h2>✅ Nourrissage terminé</h2>
          <button @click="fermerResultat" class="btn-close">×</button>
        </div>
        
        <div class="result-body">
          <div class="result-icon">🎉</div>
          <div class="result-message">
            <p>{{ resultatNourrissage.message }}</p>
          </div>
          
          <div class="result-stats">
            <div class="stat-grid">
              <div class="stat-item">
                <div class="stat-icon">🐟</div>
                <div class="stat-content">
                  <div class="stat-label">Poissons nourris</div>
                  <div class="stat-value">{{ resultatNourrissage.poissonsNourris }}</div>
                </div>
              </div>
              
              <div class="stat-item">
                <div class="stat-icon">⚖️</div>
                <div class="stat-content">
                  <div class="stat-label">Nourriture utilisée</div>
                  <div class="stat-value">{{ resultatNourrissage.nourritureUtilisee }} kg</div>
                </div>
              </div>
              
              <div class="stat-item">
                <div class="stat-icon">📈</div>
                <div class="stat-content">
                  <div class="stat-label">Gain total</div>
                  <div class="stat-value">{{ resultatNourrissage.gainTotal }} g</div>
                </div>
              </div>
              
              <div class="stat-item">
                <div class="stat-icon">💰</div>
                <div class="stat-content">
                  <div class="stat-label">Coût total</div>
                  <div class="stat-value">{{ resultatNourrissage.coutTotal }} MGA</div>
                </div>
              </div>
            </div>
          </div>
          
          <div class="result-details" v-if="resultatNourrissage.details">
            <h4>Détails par poisson</h4>
            <div class="details-list">
              <div v-for="detail in resultatNourrissage.details" :key="detail.id" class="detail-item">
                <span class="detail-name">{{ detail.nom }}</span>
                <span class="detail-gain">{{ detail.gain }}g</span>
              </div>
            </div>
          </div>
        </div>
        
        <div class="result-footer">
          <button @click="fermerResultat" class="btn-continue">
            Continuer
          </button>
          <button @click="voirHistorique" class="btn-history">
            Voir l'historique
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import poissonService from '../services/poissonService'
import nourrissageService from '../services/nourrissageService'
import { calculService } from '../services/calculService'

export default {
  name: 'Nourrissage',
  setup() {
    const router = useRouter()
    
    // Données
    const aliments = ref([])
    const poissonsAffames = ref([])
    const stats = ref({})
    const loading = ref(false)
    const resultatNourrissage = ref(null)
    
    // Sélections
    const selectedAlimentId = ref(null)
    const showCustomAliment = ref(false)
    const quantitePlat = ref(0.5) // 500g par défaut
    const distributionMode = ref('equitable')
    
    // Aliment personnalisé
    const customAliment = ref({
      proteinesParKg: 10,
      glucidesParKg: 10,
      prixParKg: 2000
    })
    
    // Charger les données
    const loadData = async () => {
      try {
        // Charger les aliments
        const alimentsData = await nourrissageService.getAliments()
        aliments.value = alimentsData
        if (alimentsData.length > 0) {
          selectedAlimentId.value = alimentsData[0].idAliment
        }
        
        // Charger les poissons affamés
        const poissonsData = await poissonService.getPoissonsAffames()
        poissonsAffames.value = poissonsData
        
        // Charger les statistiques
        const statsData = await poissonService.getStatistiques()
        stats.value = statsData
        
      } catch (error) {
        console.error('Erreur chargement données:', error)
        alert('Impossible de charger les données')
      }
    }
    
    // Aliment sélectionné
    const selectedAliment = computed(() => {
      if (showCustomAliment.value) {
        return customAliment.value
      }
      return aliments.value.find(a => a.idAliment === selectedAlimentId.value)
    })
    
    // Quantité maximale
    const maxQuantite = computed(() => {
      if (!selectedAliment.value || showCustomAliment.value) return 10
      return Math.min(selectedAliment.value.stockAliment, 10)
    })
    
    // Calculs nutritionnels
    const proteinesTotales = computed(() => {
      if (!selectedAliment.value || quantitePlat.value <= 0) return 0
      const { proteinesTotal } = calculService.calculerApportsTotaux(
        quantitePlat.value,
        selectedAliment.value.proteinesParKg,
        selectedAliment.value.glucidesParKg
      )
      return proteinesTotal
    })
    
    const glucidesTotales = computed(() => {
      if (!selectedAliment.value || quantitePlat.value <= 0) return 0
      const { glucidesTotal } = calculService.calculerApportsTotaux(
        quantitePlat.value,
        selectedAliment.value.proteinesParKg,
        selectedAliment.value.glucidesParKg
      )
      return glucidesTotal
    })
    
    // Par poisson
    const proteinesParPoisson = computed(() => {
      if (stats.value.poissonsAffames === 0) return 0
      return proteinesTotales.value / stats.value.poissonsAffames
    })
    
    const glucidesParPoisson = computed(() => {
      if (stats.value.poissonsAffames === 0) return 0
      return glucidesTotales.value / stats.value.poissonsAffames
    })
    
    // Satisfaction
    const satisfaction = computed(() => {
      return calculService.calculerSatisfaction(
        proteinesParPoisson.value,
        glucidesParPoisson.value
      )
    })
    
    // Gain par poisson
    const gainParPoisson = computed(() => {
      return calculService.calculerGainPoids(
        proteinesParPoisson.value,
        glucidesParPoisson.value
      )
    })
    
    // Gains totaux
    const gainTotalPrevu = computed(() => {
      return gainParPoisson.value * stats.value.poissonsAffames
    })
    
    // Coût total
    const coutTotal = computed(() => {
      if (!selectedAliment.value) return 0
      return calculService.calculerCoutNourrissage(
        quantitePlat.value,
        selectedAliment.value.prixParKg || selectedAliment.value.prixKgAliment
      )
    })
    
    // Besoins totaux
    const besoinTotal = computed(() => {
      // 2g protéines + 4g glucides = 6g par poisson
      // Pour 5g de plat
      const besoinParPoisson = 5 // g de plat
      return stats.value.poissonsAffames * besoinParPoisson
    })
    
    // Scénarios de gain
    const scenariosGain = computed(() => {
      return [
        {
          id: 1,
          title: 'Minimum',
          proteines: 2,
          glucides: 0,
          gain: 5
        },
        {
          id: 2,
          title: 'Intermédiaire',
          proteines: 2,
          glucides: 4,
          gain: 10
        },
        {
          id: 3,
          title: 'Bon',
          proteines: 4,
          glucides: 4,
          gain: 15
        },
        {
          id: 4,
          title: 'Optimal',
          proteines: 4,
          glucides: 8,
          gain: 20
        }
      ]
    })
    
    // Gains prévus pour chaque poisson
    const gainsParPoisson = computed(() => {
      const gains = []
      for (let i = 0; i < Math.min(stats.value.poissonsAffames, 10); i++) {
        gains.push(gainParPoisson.value)
      }
      return gains
    })
    
    const gainMin = computed(() => {
      return stats.value.poissonsAffames * 5
    })
    
    const gainMax = computed(() => {
      return stats.value.poissonsAffames * 20
    })
    
    const gainMoyen = computed(() => {
      return stats.value.poissonsAffames * gainParPoisson.value
    })
    
    // Validation
    const canNourrir = computed(() => {
      return quantitePlat.value > 0 &&
             stats.value.poissonsAffames > 0 &&
             selectedAliment.value &&
             quantitePlat.value <= maxQuantite.value
    })
    
    const validationMessage = computed(() => {
      if (stats.value.poissonsAffames === 0) return 'Aucun poisson affamé à nourrir'
      if (quantitePlat.value <= 0) return 'Veuillez saisir une quantité valide'
      if (!selectedAliment.value) return 'Veuillez sélectionner un aliment'
      if (quantitePlat.value > maxQuantite.value) {
        return `Quantité maximale: ${maxQuantite.value} kg`
      }
      return `Prêt à nourrir ${stats.value.poissonsAffames} poissons`
    })
    
    const validationClass = computed(() => {
      if (!canNourrir.value) return 'error'
      return 'success'
    })
    
    // Méthodes
    const selectAliment = (aliment) => {
      selectedAlimentId.value = aliment.idAliment
      showCustomAliment.value = false
    }
    
    const toggleCustomAliment = () => {
      showCustomAliment.value = !showCustomAliment.value
      if (showCustomAliment.value) {
        selectedAlimentId.value = null
      }
    }
    
    const incrementQuantity = () => {
      if (quantitePlat.value < maxQuantite.value) {
        quantitePlat.value = Math.round((quantitePlat.value + 0.1) * 10) / 10
        updateCalculs()
      }
    }
    
    const decrementQuantity = () => {
      if (quantitePlat.value > 0.1) {
        quantitePlat.value = Math.round((quantitePlat.value - 0.1) * 10) / 10
        updateCalculs()
      }
    }
    
    const updateCalculs = () => {
      quantitePlat.value = Math.max(0.1, Math.min(quantitePlat.value, maxQuantite.value))
    }
    
    const formatPoids = (poids) => {
      return poids ? Math.round(poids * 100) / 100 : 0
    }
    
    const getProgression = (poisson) => {
      if (!poisson.poidsActuelPoisson || !poisson.poidsMaximalPoisson) return 0
      return Math.round((poisson.poidsActuelPoisson / poisson.poidsMaximalPoisson) * 100)
    }
    
    const getAllocation = (poisson) => {
      switch (distributionMode.value) {
        case 'proportionnel':
          const totalPoids = poissonsAffames.value.reduce((sum, p) => sum + p.poidsActuelPoisson, 0)
          return totalPoids > 0 ? (poisson.poidsActuelPoisson / totalPoids) * 100 : 100 / poissonsAffames.value.length
        default:
          return 100 / poissonsAffames.value.length
      }
    }
    
    const getGainPrevu = (poisson) => {
      const allocation = getAllocation(poisson) / 100
      const proteinesAllouees = proteinesTotales.value * allocation
      const glucidesAlloues = glucidesTotales.value * allocation
      return calculService.calculerGainPoids(proteinesAllouees, glucidesAlloues)
    }
    
    const getSatisfactionClass = (pourcentage) => {
      if (pourcentage >= 100) return 'excellent'
      if (pourcentage >= 80) return 'good'
      if (pourcentage >= 50) return 'medium'
      return 'low'
    }
    
    const getOverallClass = (pourcentage) => {
      if (pourcentage >= 100) return 'excellent'
      if (pourcentage >= 80) return 'good'
      if (pourcentage >= 50) return 'medium'
      return 'low'
    }
    
    const getSatisfactionMessage = (pourcentage) => {
      if (pourcentage >= 100) return 'Tous les besoins sont satisfaits'
      if (pourcentage >= 80) return 'Besoins presque satisfaits'
      if (pourcentage >= 50) return 'Besoins partiellement satisfaits'
      return 'Besoins insuffisants'
    }
    
    const getGainClass = (gain) => {
      if (gain >= 15) return 'high'
      if (gain >= 10) return 'medium'
      if (gain >= 5) return 'low'
      return 'very-low'
    }
    
    // Exécuter le nourrissage
    const executerNourrissage = async () => {
      if (!canNourrir.value) return
      
      loading.value = true
      try {
        // Préparer les données
        const alimentData = showCustomAliment.value ? customAliment.value : selectedAliment.value
        
        const result = await nourrissageService.nourrirPoissons(
          quantitePlat.value,
          alimentData.proteinesParKg || alimentData.proteinesParKgAliment,
          alimentData.glucidesParKg || alimentData.glucidesParKgAliment
        )
        
        // Afficher le résultat
        resultatNourrissage.value = {
          message: `Nourrissage réussi !`,
          poissonsNourris: result.poissonsNourris || stats.value.poissonsAffames,
          nourritureUtilisee: quantitePlat.value,
          gainTotal: gainTotalPrevu.value,
          coutTotal: coutTotal.value,
          details: poissonsAffames.value.map(p => ({
            id: p.idPoisson,
            nom: p.nomPoisson,
            gain: getGainPrevu(p)
          }))
        }
        
        // Recharger les données
        await loadData()
        
      } catch (error) {
        console.error('Erreur nourrissage:', error)
        alert(error.response?.data?.message || 'Erreur lors du nourrissage')
      } finally {
        loading.value = false
      }
    }
    
    const fermerResultat = () => {
      resultatNourrissage.value = null
    }
    
    const voirHistorique = () => {
      router.push('/historique')
    }
    
    // Initialisation
    onMounted(() => {
      loadData()
    })
    
    // Watchers
    watch(quantitePlat, updateCalculs)
    watch(distributionMode, () => {
      // Forcer le recalcul des allocations
    })
    
    return {
      // Données
      aliments,
      poissonsAffames,
      stats,
      loading,
      resultatNourrissage,
      
      // Sélections
      selectedAlimentId,
      showCustomAliment,
      quantitePlat,
      distributionMode,
      customAliment,
      
      // Computed
      selectedAliment,
      maxQuantite,
      proteinesTotales,
      glucidesTotales,
      proteinesParPoisson,
      glucidesParPoisson,
      satisfaction,
      gainParPoisson,
      gainTotalPrevu,
      coutTotal,
      besoinTotal,
      scenariosGain,
      gainsParPoisson,
      gainMin,
      gainMax,
      gainMoyen,
      canNourrir,
      validationMessage,
      validationClass,
      
      // Méthodes
      selectAliment,
      toggleCustomAliment,
      incrementQuantity,
      decrementQuantity,
      updateCalculs,
      formatPoids,
      getProgression,
      getAllocation,
      getGainPrevu,
      getSatisfactionClass,
      getOverallClass,
      getSatisfactionMessage,
      getGainClass,
      executerNourrissage,
      fermerResultat,
      voirHistorique
    }
  }
}
</script>

<style scoped>
@import '../assets/styles/nourissage.css';
</style>

