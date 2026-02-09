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
          <span class="label">Total nutriments:</span>
          <span class="value">{{ calculateTotalNutrients() }} g</span>
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
                    <span class="nutrient protein">P: {{ aliment.proteinesParKgAliment }}g/kg</span>
                    <span class="nutrient carbs">G: {{ aliment.glucidesParKgAliment }}g/kg</span>
                    <span class="nutrient lipid">L: {{ aliment.lipidesParKgAliment || 5 }}g/kg</span>
                    <span class="nutrient vitamin">V: {{ aliment.vitaminesParKgAliment || 0.5 }}g/kg</span>
                    <span>Stock: {{ formatStock(aliment.stockAliment) }} kg</span>
                  </div>
                </div>
                <div class="aliment-price">{{ formatPrice(aliment.prixKgAliment) }} MGA/kg</div>
                <div class="aliment-quality" :class="getAlimentQualityClass(aliment)">
                  {{ getAlimentQualityText(aliment) }}
                </div>
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
                  <label>Lipides (g/kg)</label>
                  <input type="number" v-model="customAliment.lipidesParKg" min="0" step="0.1" value="5">
                </div>
                <div class="input-group">
                  <label>Vitamines (g/kg)</label>
                  <input type="number" v-model="customAliment.vitaminesParKg" min="0" step="0.01" value="0.5">
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

            <!-- Calculs nutritionnels complets -->
            <div class="calculations-section">
              <h4>Calculs nutritionnels complets</h4>
              <div class="calculations-grid">
                <div class="calc-item">
                  <span class="calc-label">Protéines totales:</span>
                  <span class="calc-value protein">{{ proteinesTotales.toFixed(1) }} g</span>
                </div>
                <div class="calc-item">
                  <span class="calc-label">Glucides totales:</span>
                  <span class="calc-value carbs">{{ glucidesTotales.toFixed(1) }} g</span>
                </div>
                <div class="calc-item">
                  <span class="calc-label">Lipides totales:</span>
                  <span class="calc-value lipid">{{ lipidesTotales.toFixed(1) }} g</span>
                </div>
                <div class="calc-item">
                  <span class="calc-label">Vitamines totales:</span>
                  <span class="calc-value vitamin">{{ vitaminesTotales.toFixed(2) }} g</span>
                </div>
                <div class="calc-item">
                  <span class="calc-label">Par poisson:</span>
                  <span class="calc-value small">
                    P:{{ proteinesParPoisson.toFixed(1) }}g G:{{ glucidesParPoisson.toFixed(1) }}g
                    L:{{ lipidesParPoisson.toFixed(1) }}g V:{{ vitaminesParPoisson.toFixed(2) }}g
                  </span>
                </div>
                <div class="calc-item">
                  <span class="calc-label">Coût total:</span>
                  <span class="calc-value price">{{ coutTotal }} MGA</span>
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
                    <span>P:{{ scenario.proteines }}g G:{{ scenario.glucides }}g L:{{ scenario.lipides }}g V:{{ scenario.vitamines }}g</span>
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
        <!-- Résumé nutritionnel complet -->
        <div class="card">
          <h2>Résumé nutritionnel complet</h2>
          <div class="nutrition-summary">
            <div class="satisfaction-meters">
              <div class="meter">
                <div class="meter-header">
                  <span class="meter-label">Protéines</span>
                  <span class="meter-value">{{ satisfaction.proteines }}%</span>
                </div>
                <div class="meter-bar">
                  <div class="meter-fill protein" :style="{ width: Math.min(satisfaction.proteines, 100) + '%' }"></div>
                </div>
                <div class="meter-details">
                  {{ proteinesParPoisson.toFixed(1) }}g / 2g besoins
                </div>
              </div>
              
              <div class="meter">
                <div class="meter-header">
                  <span class="meter-label">Glucides</span>
                  <span class="meter-value">{{ satisfaction.glucides }}%</span>
                </div>
                <div class="meter-bar">
                  <div class="meter-fill carbs" :style="{ width: Math.min(satisfaction.glucides, 100) + '%' }"></div>
                </div>
                <div class="meter-details">
                  {{ glucidesParPoisson.toFixed(1) }}g / 4g besoins
                </div>
              </div>
              
              <div class="meter">
                <div class="meter-header">
                  <span class="meter-label">Lipides</span>
                  <span class="meter-value">{{ satisfaction.lipides }}%</span>
                </div>
                <div class="meter-bar">
                  <div class="meter-fill lipid" :style="{ width: Math.min(satisfaction.lipides, 100) + '%' }"></div>
                </div>
                <div class="meter-details">
                  {{ lipidesParPoisson.toFixed(1) }}g / 1g besoins
                </div>
              </div>
              
              <div class="meter">
                <div class="meter-header">
                  <span class="meter-label">Vitamines</span>
                  <span class="meter-value">{{ satisfaction.vitamines }}%</span>
                </div>
                <div class="meter-bar">
                  <div class="meter-fill vitamin" :style="{ width: Math.min(satisfaction.vitamines, 100) + '%' }"></div>
                </div>
                <div class="meter-details">
                  {{ vitaminesParPoisson.toFixed(2) }}g / 0.5g besoins
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
              <div class="overall-status" v-if="satisfaction.tousSatisfaits">
                ✅ Tous les besoins nutritionnels sont satisfaits
              </div>
              <div class="overall-status warning" v-else>
                ⚠️ Certains besoins ne sont pas entièrement satisfaits
              </div>
            </div>
          </div>
        </div>

        <!-- Prévisions de gain -->
        <div class="card">
          <h2>Prévisions de gain complet</h2>
          <div class="gain-predictions">
            <div class="prediction-chart">
              <div class="chart-bars">
                <div v-for="(gain, index) in gainsParPoisson" :key="index" class="chart-bar-container">
                  <div class="chart-bar" :style="{ height: (gain / 25 * 100) + '%' }"
                       :class="getGainClass(gain)">
                    <span class="bar-value">{{ gain.toFixed(1) }}g</span>
                  </div>
                  <div class="bar-label">P{{ index + 1 }}</div>
                </div>
              </div>
            </div>
            
            <div class="prediction-stats">
              <div class="stat-item">
                <span class="stat-label">Gain minimum:</span>
                <span class="stat-value">{{ gainMin.toFixed(1) }} g</span>
              </div>
              <div class="stat-item">
                <span class="stat-label">Gain maximum:</span>
                <span class="stat-value">{{ gainMax.toFixed(1) }} g</span>
              </div>
              <div class="stat-item">
                <span class="stat-label">Gain moyen:</span>
                <span class="stat-value">{{ gainMoyen.toFixed(1) }} g</span>
              </div>
              <div class="stat-item total">
                <span class="stat-label">Gain total:</span>
                <span class="stat-value">{{ gainTotalPrevu.toFixed(1) }} g</span>
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
              <div class="fish-nutrition">
                <div class="nutrition-bars">
                  <div class="nutrient-bar protein" :style="{ width: getNutrientPercentage(poisson, 'proteines') + '%' }"
                       title="Protéines"></div>
                  <div class="nutrient-bar carbs" :style="{ width: getNutrientPercentage(poisson, 'glucides') + '%' }"
                       title="Glucides"></div>
                  <div class="nutrient-bar lipid" :style="{ width: getNutrientPercentage(poisson, 'lipides') + '%' }"
                       title="Lipides"></div>
                  <div class="nutrient-bar vitamin" :style="{ width: getNutrientPercentage(poisson, 'vitamines') + '%' }"
                       title="Vitamines"></div>
                </div>
              </div>
              <div class="fish-gain">
                <div class="gain-indicator">
                  <div class="gain-bar">
                    <div class="gain-fill" :style="{ width: getAllocation(poisson) + '%' }"></div>
                  </div>
                  <span class="gain-text">{{ getGainPrevu(poisson).toFixed(1) }}g</span>
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
          
          <div class="nutrition-result">
            <h4>📊 Apports nutritionnels</h4>
            <div class="nutrition-grid">
              <div class="nutrition-item protein">
                <span class="label">Protéines</span>
                <span class="value">{{ resultatNourrissage.proteinesTotales }}g</span>
              </div>
              <div class="nutrition-item carbs">
                <span class="label">Glucides</span>
                <span class="value">{{ resultatNourrissage.glucidesTotales }}g</span>
              </div>
              <div class="nutrition-item lipid">
                <span class="label">Lipides</span>
                <span class="value">{{ resultatNourrissage.lipidesTotales }}g</span>
              </div>
              <div class="nutrition-item vitamin">
                <span class="label">Vitamines</span>
                <span class="value">{{ resultatNourrissage.vitaminesTotales }}g</span>
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
          <button @click="exporterRapport" class="btn-export">
            📥 Exporter rapport
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import api from '../services/api'
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
      lipidesParKg: 5,
      vitaminesParKg: 0.5,
      prixParKg: 2000
    })
    
    // Charger les données
    const loadData = async () => {
      try {
        // Charger les aliments
        const response = await api.get('/aliments')
        aliments.value = response.data
        if (aliments.value.length > 0) {
          selectedAlimentId.value = aliments.value[0].idAliment
        }
        
        // Charger les poissons affamés
        const poissonsResponse = await api.get('/poissons/affames')
        poissonsAffames.value = poissonsResponse.data
        
        // Charger les statistiques
        const statsResponse = await api.get('/poissons/statistiques')
        stats.value = statsResponse.data
        
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
      return Math.min(selectedAliment.value.stockAliment || 0, 10)
    })
    
    // Calculs nutritionnels complets
    const proteinesTotales = computed(() => {
      if (!selectedAliment.value || quantitePlat.value <= 0) return 0
      const apports = calculService.calculerApportsTotaux(
        quantitePlat.value,
        selectedAliment.value.proteinesParKg || selectedAliment.value.proteinesParKgAliment,
        selectedAliment.value.glucidesParKg || selectedAliment.value.glucidesParKgAliment,
        selectedAliment.value.lipidesParKg || selectedAliment.value.lipidesParKgAliment || 5,
        selectedAliment.value.vitaminesParKg || selectedAliment.value.vitaminesParKgAliment || 0.5
      )
      return apports.proteinesTotal
    })
    
    const glucidesTotales = computed(() => {
      if (!selectedAliment.value || quantitePlat.value <= 0) return 0
      const apports = calculService.calculerApportsTotaux(
        quantitePlat.value,
        selectedAliment.value.proteinesParKg || selectedAliment.value.proteinesParKgAliment,
        selectedAliment.value.glucidesParKg || selectedAliment.value.glucidesParKgAliment,
        selectedAliment.value.lipidesParKg || selectedAliment.value.lipidesParKgAliment || 5,
        selectedAliment.value.vitaminesParKg || selectedAliment.value.vitaminesParKgAliment || 0.5
      )
      return apports.glucidesTotal
    })
    
    const lipidesTotales = computed(() => {
      if (!selectedAliment.value || quantitePlat.value <= 0) return 0
      const apports = calculService.calculerApportsTotaux(
        quantitePlat.value,
        selectedAliment.value.proteinesParKg || selectedAliment.value.proteinesParKgAliment,
        selectedAliment.value.glucidesParKg || selectedAliment.value.glucidesParKgAliment,
        selectedAliment.value.lipidesParKg || selectedAliment.value.lipidesParKgAliment || 5,
        selectedAliment.value.vitaminesParKg || selectedAliment.value.vitaminesParKgAliment || 0.5
      )
      return apports.lipidesTotal || 0
    })
    
    const vitaminesTotales = computed(() => {
      if (!selectedAliment.value || quantitePlat.value <= 0) return 0
      const apports = calculService.calculerApportsTotaux(
        quantitePlat.value,
        selectedAliment.value.proteinesParKg || selectedAliment.value.proteinesParKgAliment,
        selectedAliment.value.glucidesParKg || selectedAliment.value.glucidesParKgAliment,
        selectedAliment.value.lipidesParKg || selectedAliment.value.lipidesParKgAliment || 5,
        selectedAliment.value.vitaminesParKg || selectedAliment.value.vitaminesParKgAliment || 0.5
      )
      return apports.vitaminesTotal || 0
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
    
    const lipidesParPoisson = computed(() => {
      if (stats.value.poissonsAffames === 0) return 0
      return lipidesTotales.value / stats.value.poissonsAffames
    })
    
    const vitaminesParPoisson = computed(() => {
      if (stats.value.poissonsAffames === 0) return 0
      return vitaminesTotales.value / stats.value.poissonsAffames
    })
    
    // Satisfaction complète
    const satisfaction = computed(() => {
      return calculService.calculerSatisfactionComplet(
        proteinesParPoisson.value,
        glucidesParPoisson.value,
        lipidesParPoisson.value,
        vitaminesParPoisson.value
      )
    })
    
    // Gain par poisson (complet)
    const gainParPoisson = computed(() => {
      return calculService.calculerGainPoidsComplet(
        proteinesParPoisson.value,
        glucidesParPoisson.value,
        lipidesParPoisson.value,
        vitaminesParPoisson.value
      )
    })
    
    // Gains totaux
    const gainTotalPrevu = computed(() => {
      return gainParPoisson.value * stats.value.poissonsAffames
    })
    
    // Coût total
    const coutTotal = computed(() => {
      if (!selectedAliment.value) return 0
      const prix = selectedAliment.value.prixParKg || selectedAliment.value.prixKgAliment
      return calculService.calculerCoutNourrissage(quantitePlat.value, prix)
    })
    
    // Total nutriments
    const calculateTotalNutrients = () => {
      return (proteinesTotales.value + glucidesTotales.value + lipidesTotales.value + vitaminesTotales.value).toFixed(1)
    }
    
    // Scénarios de gain complets
    const scenariosGain = computed(() => {
      return [
        {
          id: 1,
          title: 'Minimum',
          proteines: 2,
          glucides: 0,
          lipides: 0,
          vitamines: 0,
          gain: calculService.calculerGainPoidsComplet(2, 0, 0, 0)
        },
        {
          id: 2,
          title: 'Basique',
          proteines: 2,
          glucides: 4,
          lipides: 0,
          vitamines: 0,
          gain: calculService.calculerGainPoidsComplet(2, 4, 0, 0)
        },
        {
          id: 3,
          title: 'Équilibré',
          proteines: 2,
          glucides: 4,
          lipides: 1,
          vitamines: 0.5,
          gain: calculService.calculerGainPoidsComplet(2, 4, 1, 0.5)
        },
        {
          id: 4,
          title: 'Optimal',
          proteines: 4,
          glucides: 8,
          lipides: 2,
          vitamines: 1,
          gain: calculService.calculerGainPoidsComplet(4, 8, 2, 1)
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
      return stats.value.poissonsAffames * scenariosGain.value[0].gain
    })
    
    const gainMax = computed(() => {
      return stats.value.poissonsAffames * scenariosGain.value[3].gain
    })
    
    const gainMoyen = computed(() => {
      return gainTotalPrevu.value
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
    
    // Méthodes utilitaires
    const formatStock = (stock) => {
      if (!stock && stock !== 0) return '0.00'
      return parseFloat(stock).toFixed(2)
    }
    
    const formatPrice = (price) => {
      if (!price && price !== 0) return '0'
      return parseFloat(price).toFixed(0).replace(/\B(?=(\d{3})+(?!\d))/g, ' ')
    }
    
    const formatPoids = (poids) => {
      return poids ? Math.round(poids * 100) / 100 : 0
    }
    
    const getAlimentQualityClass = (aliment) => {
      const analysis = calculService.evaluerQualiteAliment({
        proteinesParKg: aliment.proteinesParKgAliment,
        glucidesParKg: aliment.glucidesParKgAliment,
        lipidesParKg: aliment.lipidesParKgAliment || 5,
        vitaminesParKg: aliment.vitaminesParKgAliment || 0.5
      })
      
      if (analysis.score >= 90) return 'excellent'
      if (analysis.score >= 75) return 'good'
      if (analysis.score >= 50) return 'medium'
      return 'low'
    }
    
    const getAlimentQualityText = (aliment) => {
      const analysis = calculService.evaluerQualiteAliment({
        proteinesParKg: aliment.proteinesParKgAliment,
        glucidesParKg: aliment.glucidesParKgAliment,
        lipidesParKg: aliment.lipidesParKgAliment || 5,
        vitaminesParKg: aliment.vitaminesParKgAliment || 0.5
      })
      return analysis.qualite
    }
    
    const getProgression = (poisson) => {
      if (!poisson.poidsActuelPoisson || !poisson.poidsMaximalPoisson) return 0
      return Math.round((poisson.poidsActuelPoisson / poisson.poidsMaximalPoisson) * 100)
    }
    
    const getNutrientPercentage = (poisson, nutrient) => {
      const besoins = {
        proteines: 2,
        glucides: 4,
        lipides: 1,
        vitamines: 0.5
      }
      
      const recu = {
        proteines: proteinesParPoisson.value,
        glucides: glucidesParPoisson.value,
        lipides: lipidesParPoisson.value,
        vitamines: vitaminesParPoisson.value
      }
      
      return Math.min((recu[nutrient] / besoins[nutrient]) * 100, 100)
    }
    
    const getAllocation = (poisson) => {
      switch (distributionMode.value) {
        case 'proportionnel':
          const totalPoids = poissonsAffames.value.reduce((sum, p) => sum + (p.poidsActuelPoisson || 0), 0)
          return totalPoids > 0 ? ((poisson.poidsActuelPoisson || 0) / totalPoids) * 100 : 100 / poissonsAffames.value.length
        default:
          return 100 / poissonsAffames.value.length
      }
    }
    
    const getGainPrevu = (poisson) => {
      const allocation = getAllocation(poisson) / 100
      const proteinesAllouees = proteinesTotales.value * allocation
      const glucidesAlloues = glucidesTotales.value * allocation
      const lipidesAlloues = lipidesTotales.value * allocation
      const vitaminesAllouees = vitaminesTotales.value * allocation
      
      return calculService.calculerGainPoidsComplet(
        proteinesAllouees,
        glucidesAlloues,
        lipidesAlloues,
        vitaminesAllouees
      )
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
      if (gain >= 20) return 'high'
      if (gain >= 15) return 'medium-high'
      if (gain >= 10) return 'medium'
      if (gain >= 5) return 'low'
      return 'very-low'
    }
    
    // Méthodes d'interaction
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
    
    // Exécuter le nourrissage
    const executerNourrissage = async () => {
      if (!canNourrir.value) return
      
      loading.value = true
      try {
        const alimentData = showCustomAliment.value ? customAliment.value : selectedAliment.value
        
        const result = await api.post('/nourrissage/nourrir', null, {
          params: {
            quantitePlat: quantitePlat.value,
            proteinesParKg: alimentData.proteinesParKg || alimentData.proteinesParKgAliment,
            glucidesParKg: alimentData.glucidesParKg || alimentData.glucidesParKgAliment,
            lipidesParKg: alimentData.lipidesParKg || alimentData.lipidesParKgAliment || 5,
            vitaminesParKg: alimentData.vitaminesParKg || alimentData.vitaminesParKgAliment || 0.5
          }
        })
        
        // Afficher le résultat
        resultatNourrissage.value = {
          message: `Nourrissage réussi ! ${result.data.message || ''}`,
          poissonsNourris: stats.value.poissonsAffames,
          nourritureUtilisee: quantitePlat.value,
          gainTotal: gainTotalPrevu.value,
          coutTotal: coutTotal.value,
          proteinesTotales: proteinesTotales.value.toFixed(1),
          glucidesTotales: glucidesTotales.value.toFixed(1),
          lipidesTotales: lipidesTotales.value.toFixed(1),
          vitaminesTotales: vitaminesTotales.value.toFixed(2),
          details: poissonsAffames.value.map(p => ({
            id: p.idPoisson,
            nom: p.nomPoisson,
            gain: getGainPrevu(p).toFixed(1)
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
    
    const exporterRapport = () => {
      const rapport = {
        date: new Date().toISOString(),
        nourrissage: resultatNourrissage.value,
        aliment: selectedAliment.value.nomAliment || 'Personnalisé',
        parametres: {
          quantitePlat: quantitePlat.value,
          modeDistribution: distributionMode.value
        },
        poissonsNourris: stats.value.poissonsAffames
      }
      
      const blob = new Blob([JSON.stringify(rapport, null, 2)], { type: 'application/json' })
      const link = document.createElement('a')
      link.href = URL.createObjectURL(blob)
      link.download = `rapport_nourrissage_${new Date().toISOString().split('T')[0]}.json`
      link.click()
    }
    
    // Initialisation
    onMounted(() => {
      loadData()
    })
    
    // Watchers
    watch(quantitePlat, updateCalculs)
    watch(distributionMode, updateCalculs)
    watch(selectedAlimentId, updateCalculs)
    
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
      lipidesTotales,
      vitaminesTotales,
      proteinesParPoisson,
      glucidesParPoisson,
      lipidesParPoisson,
      vitaminesParPoisson,
      satisfaction,
      gainParPoisson,
      gainTotalPrevu,
      coutTotal,
      scenariosGain,
      gainsParPoisson,
      gainMin,
      gainMax,
      gainMoyen,
      canNourrir,
      validationMessage,
      validationClass,
      
      // Méthodes
      loadData,
      selectAliment,
      toggleCustomAliment,
      incrementQuantity,
      decrementQuantity,
      updateCalculs,
      formatStock,
      formatPrice,
      formatPoids,
      getAlimentQualityClass,
      getAlimentQualityText,
      getProgression,
      getNutrientPercentage,
      getAllocation,
      getGainPrevu,
      getOverallClass,
      getSatisfactionMessage,
      getGainClass,
      calculateTotalNutrients,
      executerNourrissage,
      fermerResultat,
      voirHistorique,
      exporterRapport
    }
  }
}
</script>

<style scoped>
@import '../assets/styles/nourissage.css';

/* Styles supplémentaires pour les nouveaux éléments */

.aliment-details .nutrient {
  font-size: 11px;
  padding: 2px 6px;
  border-radius: 4px;
  margin-right: 4px;
}

.nutrient.protein {
  background-color: #eff6ff;
  color: #1d4ed8;
}

.nutrient.carbs {
  background-color: #f0fdf4;
  color: #047857;
}

.nutrient.lipid {
  background-color: #fefce8;
  color: #ca8a04;
}

.nutrient.vitamin {
  background-color: #f5f3ff;
  color: #7c3aed;
}

.aliment-quality {
  font-size: 11px;
  padding: 2px 8px;
  border-radius: 12px;
  font-weight: 600;
  margin-left: 8px;
}

.aliment-quality.excellent {
  background-color: #dcfce7;
  color: #166534;
}

.aliment-quality.good {
  background-color: #fef3c7;
  color: #92400e;
}

.aliment-quality.medium {
  background-color: #fef3c7;
  color: #92400e;
}

.aliment-quality.low {
  background-color: #fee2e2;
  color: #991b1b;
}

.calc-item .calc-value.small {
  font-size: 11px;
}

.calc-value.protein {
  color: #1d4ed8;
}

.calc-value.carbs {
  color: #047857;
}

.calc-value.lipid {
  color: #ca8a04;
}

.calc-value.vitamin {
  color: #7c3aed;
}

.calc-value.price {
  color: #059669;
  font-weight: 700;
}

.meter-fill.protein {
  background-color: #3b82f6;
}

.meter-fill.carbs {
  background-color: #10b981;
}

.meter-fill.lipid {
  background-color: #f59e0b;
}

.meter-fill.vitamin {
  background-color: #8b5cf6;
}

.overall-status {
  margin-top: 10px;
  padding: 8px 12px;
  border-radius: 6px;
  font-size: 14px;
  font-weight: 500;
}

.overall-status.warning {
  background-color: #fef3c7;
  color: #92400e;
  border-left: 4px solid #f59e0b;
}

.nutrition-bars {
  display: flex;
  height: 4px;
  border-radius: 2px;
  overflow: hidden;
  margin: 8px 0;
}

.nutrient-bar {
  height: 100%;
}

.nutrient-bar.protein {
  background-color: #3b82f6;
}

.nutrient-bar.carbs {
  background-color: #10b981;
}

.nutrient-bar.lipid {
  background-color: #f59e0b;
}

.nutrient-bar.vitamin {
  background-color: #8b5cf6;
}

.nutrition-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
  margin: 15px 0;
}

.nutrition-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px;
  border-radius: 6px;
  font-weight: 600;
}

.nutrition-item.protein {
  background-color: #eff6ff;
  color: #1d4ed8;
}

.nutrition-item.carbs {
  background-color: #f0fdf4;
  color: #047857;
}

.nutrition-item.lipid {
  background-color: #fefce8;
  color: #ca8a04;
}

.nutrition-item.vitamin {
  background-color: #f5f3ff;
  color: #7c3aed;
}

.chart-bar.high {
  background-color: #10b981;
}

.chart-bar.medium-high {
  background-color: #22c55e;
}

.chart-bar.medium {
  background-color: #f59e0b;
}

.chart-bar.low {
  background-color: #f97316;
}

.chart-bar.very-low {
  background-color: #ef4444;
}
</style>

