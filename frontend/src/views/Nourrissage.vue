<!-- NourrissageCombined.vue -->
<template>
  <div class="nourrissage-page">
    <div class="page-header">
      <h1>
    Nourrissage des Poissons
    <span v-if="stats.bassinNom" class="bassin-info">
      - Bassin: {{ stats.bassinNom }}
    </span>
  </h1>
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
          
          <!-- Mode de sélection : Plat ou Aliment -->
          <div class="mode-select-section">
            <h3>1. Mode de nourrissage</h3>
            <div class="mode-select-options">
              <div class="mode-option" 
                   :class="{ selected: modeNourrissage === 'aliment' }"
                   @click="modeNourrissage = 'aliment'">
                <div class="mode-icon">🥫</div>
                <div class="mode-content">
                  <h4>Aliment simple</h4>
                  <p>Utiliser un aliment directement depuis le stock</p>
                </div>
              </div>
              
              <div class="mode-option" 
                   :class="{ selected: modeNourrissage === 'plat' }"
                   @click="modeNourrissage = 'plat'">
                <div class="mode-icon">🍽️</div>
                <div class="mode-content">
                  <h4>Plat préparé</h4>
                  <p>Utiliser un plat déjà composé</p>
                </div>
              </div>
            </div>
          </div>

          <!-- Sélection de PLAT (nouvelle section) -->
          <div v-if="modeNourrissage === 'plat'" class="plat-section">
            <h3>2. Sélectionnez un plat</h3>
            <div v-if="platsDisponibles.length === 0" class="no-data">
              <p>Aucun plat disponible. Créez d'abord des plats dans la section "Plats".</p>
            </div>
            <div v-else class="plat-options">
                <div v-for="plat in platsDisponibles" :key="plat.idPlat" 
                    class="plat-option" 
                    :class="{ 
                      selected: selectedPlatId === plat.idPlat,
                      'unavailable': plat.poidsTotalPlat <= 0
                    }"
                    @click="selectPlat(plat)"
                    :title="plat.poidsTotalPlat <= 0 ? 'Plat épuisé' : ''">
                  
                  <div class="plat-icon">🍽️</div>
                  <div class="plat-info">
                    <h4>{{ plat.nomPlat }}</h4>
                    <div class="plat-status" :class="{
                      'available': plat.poidsTotalPlat > 0,
                      'unavailable': plat.poidsTotalPlat <= 0
                    }">
                      {{ plat.poidsTotalPlat > 0 ? 'Disponible' : 'Épuisé' }}
                    </div>
                    <div class="plat-details">
                      <span><strong>Quantité:</strong> {{ plat.poidsTotalPlat }} kg</span>
                      <span><strong>Protéines:</strong> {{ plat.proteinesParKgPlat }} g/kg</span>
                      <span><strong>Glucides:</strong> {{ plat.glucidesParKgPlat }} g/kg</span>
                      <span><strong>Coût:</strong> {{ plat.coutTotalPlat }} MGA</span>
                    </div>
                  </div>
                  
                  <!-- Indicateur de sélection -->
                  <div v-if="selectedPlatId === plat.idPlat" class="selection-indicator">
                    ✓ Sélectionné
                  </div>
                </div>
              </div>
            
            <!-- Détails du plat sélectionné -->
            <div v-if="selectedPlat" class="plat-details-section">
              <h4>Détails du plat sélectionné</h4>
              <div class="plat-details-card">
                <div class="plat-detail-row">
                  <span class="detail-label">Nom:</span>
                  <span class="detail-value">{{ selectedPlat.nomPlat }}</span>
                </div>
                <div class="plat-detail-row">
                  <span class="detail-label">Quantité totale:</span>
                  <span class="detail-value">{{ selectedPlat.poidsTotalPlat }} kg</span>
                </div>
                <div class="plat-detail-row">
                  <span class="detail-label">Protéines par kg:</span>
                  <span class="detail-value">{{ selectedPlat.proteinesParKgPlat }} g</span>
                </div>
                <div class="plat-detail-row">
                  <span class="detail-label">Glucides par kg:</span>
                  <span class="detail-value">{{ selectedPlat.glucidesParKgPlat }} g</span>
                </div>
                <div class="plat-detail-row">
                  <span class="detail-label">Coût total:</span>
                  <span class="detail-value">{{ selectedPlat.coutTotalPlat }} MGA</span>
                </div>
              </div>
              
              <!-- Composition du plat -->
              <div v-if="selectedPlat.compositions && selectedPlat.compositions.length > 0" class="composition-section">
                <h5>Composition:</h5>
                <div class="composition-list">
                  <div v-for="comp in selectedPlat.compositions" :key="comp.idCompositionPlat" class="composition-item">
                    <span class="composition-name">{{ comp.nomAliment }}</span>
                    <span class="composition-quantity">{{ comp.poidsAlimentComposition }} kg</span>
                    <span class="composition-cost">{{ comp.coutAlimentComposition }} MGA</span>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- Sélection d'ALIMENT (section existante de Nourrissage.vue) -->
          <div v-else-if="modeNourrissage === 'aliment'" class="food-section">
            <h3>2. Sélectionnez l'aliment</h3>
            <div class="aliment-options">
              <div v-for="aliment in aliments" :key="aliment.idAliment" 
                   class="aliment-option" 
                   :class="{ selected: selectedAlimentId === aliment.idAliment }"
                   @click="selectAliment(aliment)">
                <div class="aliment-icon">🥫</div>
                <div class="aliment-info">
                  <h4>{{ aliment.nomAliment }}</h4>
                  <div class="aliment-details">
                    <span>Protéines: {{ aliment.proteinesParKgAliment }} g/kg</span>
                    <span>Glucides: {{ aliment.glucidesParKgAliment }} g/kg</span>
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

          <!-- Quantité et calculs (COMMUN aux deux modes) -->
          <div class="quantity-section">
            <h3>3. Définissez la quantité</h3>
            <div class="quantity-control">
              <label>Quantité à utiliser (kg)</label>
              <div class="quantity-input-group">
                <button @click="decrementQuantity" class="btn-quantity" :disabled="quantitePlat <= 0.1">-</button>
                <input type="number" v-model="quantitePlat" min="0.1" step="0.1" 
                       :max="maxQuantite" @input="updateCalculs">
                <button @click="incrementQuantity" class="btn-quantity" :disabled="quantitePlat >= maxQuantite">+</button>
                <span class="quantity-unit">kg</span>
                <div class="quantity-info">
                  <small>Valeur actuelle: {{ quantitePlat }} kg ({{ quantitePlat * 1000 }} g)</small>
                </div>
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
            <h3>4. Mode de distribution</h3>
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
              {{ loading ? 'Nourrissage en cours...' : getActionButtonText() }}
            </button>
            <div class="validation-message" :class="validationClass">
              {{ validationMessage }}
            </div>
          </div>
        </div>
      </div>

      <!-- Panneau de visualisation (inchangé) -->
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
import { useRouter, useRoute } from 'vue-router'
import poissonService from '../services/poissonService'
import nourrissageService from '../services/nourrissageService'
import platService from '../services/platService'
import { calculService } from '../services/calculService'

export default {
  name: 'NourrissageCombined',
  setup() {
    const router = useRouter()
     const route = useRoute()
    
    // Données
    const aliments = ref([])
    const platsDisponibles = ref([])
    const poissonsAffames = ref([])
    const stats = ref({})
    const loading = ref(false)
    const resultatNourrissage = ref(null)

     // Récupérer l'ID et le nom du bassin depuis les query params
    const bassinId = ref(null)
    const bassinNom = ref('')

    // Initialiser les paramètres du bassin
    onMounted(() => {
      if (route.query.bassinId) {
        bassinId.value = route.query.bassinId
        bassinNom.value = route.query.bassinNom || ''
      }
    })
    
    // Sélections
    const modeNourrissage = ref('aliment') // 'aliment' ou 'plat'
    const selectedAlimentId = ref(null)
    const selectedPlatId = ref(null)
    const showCustomAliment = ref(false)
    const quantitePlat = ref(0.1) // 500g par défaut
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
        if (alimentsData.length > 0 && modeNourrissage.value === 'aliment') {
          selectedAlimentId.value = alimentsData[0].idAliment
        }
        
        // Charger les plats disponibles
        const platsData = await platService.getPlatsDisponibles()
        platsDisponibles.value = platsData
        if (platsData.length > 0 && modeNourrissage.value === 'plat') {
          selectedPlatId.value = platsData[0].idPlat
        }
        
        // MODIFIÉ : Charger les poissons selon le bassin ou tous
        let poissonsData
        if (bassinId.value) {
          // Option 1: Récupérer les poissons du bassin spécifique
          try {
            poissonsData = await poissonService.getPoissonsByBassin(bassinId.value)
          } catch (error) {
            console.warn('Méthode getPoissonsByBassin non disponible, fallback sur getPoissonsAffames:', error)
            // Fallback: Récupérer tous les poissons affamés
            poissonsData = await poissonService.getPoissonsAffames()
            // Puis filtrer localement (moins efficace mais fonctionnel)
            // Note: Cette approche nécessite que les poissons aient un champ bassinId
            // Si non, on ne peut pas filtrer côté frontend
          }
        } else {
          // Charger tous les poissons affamés
          poissonsData = await poissonService.getPoissonsAffames()
        }
        
        // MODIFIÉ : Filtrer les poissons affamés et appartenant au bassin si spécifié
        poissonsAffames.value = poissonsData.filter(poisson => {
          // Vérifier si le poisson est affamé
          const estAffame = !poisson.estRassasiePoisson && 
                           !poisson.estVenduPoisson && 
                           poisson.estEnViePoisson
          
          // Si un bassin est spécifié, vérifier que le poisson appartient à ce bassin
          if (bassinId.value) {
            // Vérifier selon la structure de données
            const poissonBassinId = poisson.piscineActuelle?.idPiscine || 
                                    poisson.idPiscine || 
                                    poisson.bassinId
            
            return estAffame && (poissonBassinId == bassinId.value)
          }
          
          return estAffame
        })
        
        // MODIFIÉ : Mettre à jour les statistiques avec info bassin
        const statsData = await poissonService.getStatistiques()
        stats.value = {
          ...statsData,
          poissonsAffames: poissonsAffames.value.length,
          bassinId: bassinId.value,
          bassinNom: bassinNom.value || (bassinId.value ? `Bassin ${bassinId.value}` : '')
        }
        
        // Mettre à jour le titre de la page
        if (bassinId.value) {
          document.title = `Nourrissage - ${bassinNom.value || 'Bassin ' + bassinId.value}`
        }
        
      } catch (error) {
        console.error('Erreur chargement données:', error)
        alert('Impossible de charger les données')
      }
    }

    
    // Aliment ou Plat sélectionné
    const selectedAliment = computed(() => {
      if (showCustomAliment.value) {
        return customAliment.value
      }
      return aliments.value.find(a => a.idAliment === selectedAlimentId.value)
    })
    
    const selectedPlat = computed(() => {
      return platsDisponibles.value.find(p => p.idPlat === selectedPlatId.value)
    })
    
    // Nutriments selon le mode
    const currentNutriments = computed(() => {
      if (modeNourrissage.value === 'plat' && selectedPlat.value) {
        return {
          proteinesParKg: selectedPlat.value.proteinesParKgPlat,
          glucidesParKg: selectedPlat.value.glucidesParKgPlat,
          prixParKg: selectedPlat.value.coutTotalPlat / selectedPlat.value.poidsTotalPlat
        }
      } else if (modeNourrissage.value === 'aliment' && selectedAliment.value) {
        return {
          proteinesParKg: selectedAliment.value.proteinesParKgAliment,
          glucidesParKg: selectedAliment.value.glucidesParKgAliment,
          prixParKg: selectedAliment.value.prixKgAliment
        }
      }
      return { proteinesParKg: 0, glucidesParKg: 0, prixParKg: 0 }
    })
    
    // Quantité maximale
    const maxQuantite = computed(() => {
      if (modeNourrissage.value === 'plat' && selectedPlat.value) {
        return selectedPlat.value.poidsTotalPlat
      } else if (modeNourrissage.value === 'aliment' && selectedAliment.value) {
        if (showCustomAliment.value) return 10
        return Math.min(selectedAliment.value.stockAliment, 10)
      }
      return 10
    })
    
    // Calculs nutritionnels
    const proteinesTotales = computed(() => {
      if (quantitePlat.value <= 0) return 0
      const nutriments = currentNutriments.value
      const { proteinesTotal } = calculService.calculerApportsTotaux(
        quantitePlat.value,
        nutriments.proteinesParKg,
        nutriments.glucidesParKg
      )
      return proteinesTotal
    })
    
    const glucidesTotales = computed(() => {
      if (quantitePlat.value <= 0) return 0
      const nutriments = currentNutriments.value
      const { glucidesTotal } = calculService.calculerApportsTotaux(
        quantitePlat.value,
        nutriments.proteinesParKg,
        nutriments.glucidesParKg
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
      if (!currentNutriments.value) return 0
      return calculService.calculerCoutNourrissage(
        quantitePlat.value,
        currentNutriments.value.prixParKg
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
             quantitePlat.value <= maxQuantite.value &&
             ((modeNourrissage.value === 'aliment' && (selectedAliment.value || showCustomAliment.value)) ||
              (modeNourrissage.value === 'plat' && selectedPlat.value))
    })
    
    const validationMessage = computed(() => {
      if (stats.value.poissonsAffames === 0) return 'Aucun poisson affamé à nourrir'
      if (quantitePlat.value <= 0) return 'Veuillez saisir une quantité valide'
      
      if (modeNourrissage.value === 'plat') {
        if (!selectedPlat.value) return 'Veuillez sélectionner un plat'
      } else {
        if (!selectedAliment.value && !showCustomAliment.value) return 'Veuillez sélectionner un aliment'
      }
      
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
      selectedPlatId.value = null
    }
    
    const selectPlat = (plat) => {
      selectedPlatId.value = plat.idPlat
      selectedAlimentId.value = null
      showCustomAliment.value = false
    }
    
    const toggleCustomAliment = () => {
      showCustomAliment.value = !showCustomAliment.value
      if (showCustomAliment.value) {
        selectedAlimentId.value = null
        selectedPlatId.value = null
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
    
    const getActionButtonText = () => {
      if (modeNourrissage.value === 'plat' && selectedPlat.value) {
        return `Nourrir avec "${selectedPlat.value.nomPlat}"`
      }
      return `Nourrir ${stats.value.poissonsAffames} poissons`
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
    let result
    
    if (modeNourrissage.value === 'plat' && selectedPlat.value) {
      // Nourrir avec un plat
      console.log('Nourrissage avec plat:', selectedPlat.value.idPlat)
      result = await nourrissageService.nourrirAvecPlat(selectedPlat.value.idPlat)
      
    } else if (modeNourrissage.value === 'aliment') {
      // Nourrir avec un aliment (personnalisé ou existant)
      const alimentData = showCustomAliment.value ? customAliment.value : selectedAliment.value
      
      // MODIFICATION : Utiliser la nouvelle méthode qui accepte l'ID du bassin
      if (bassinId.value) {
        // Nourrir uniquement les poissons du bassin spécifié
        result = await nourrissageService.nourrirPoissonsDansBassin(
          quantitePlat.value,
          alimentData.proteinesParKg || alimentData.proteinesParKgAliment,
          alimentData.glucidesParKg || alimentData.glucidesParKgAliment,
          bassinId.value
        )
      } else {
        // Nourrir tous les poissons (comportement par défaut)
        result = await nourrissageService.nourrirPoissons(
          quantitePlat.value,
          alimentData.proteinesParKg || alimentData.proteinesParKgAliment,
          alimentData.glucidesParKg || alimentData.glucidesParKgAliment
        )
      }
    }
    
    // Personnaliser le message selon le bassin
    let message = result.message || `Nourrissage réussi !`
    if (bassinId.value) {
      message = `${message} (Bassin: ${bassinNom.value || bassinId.value})`
    }
    
    // Afficher le résultat
    resultatNourrissage.value = {
      message: message,
      poissonsNourris: result.poissonsNourris || stats.value.poissonsAffames,
      nourritureUtilisee: result.nourritureUtilisee || quantitePlat.value,
      gainTotal: result.gainTotal || gainTotalPrevu.value,
      coutTotal: result.coutTotal || coutTotal.value,
      bassinId: bassinId.value,
      bassinNom: bassinNom.value,
      details: poissonsAffames.value.map(p => ({
        id: p.idPoisson,
        nom: p.nomPoisson,
        gain: getGainPrevu(p)
      }))
    }
    
    if (modeNourrissage.value === 'plat' && selectedPlat.value) {
      resultatNourrissage.value.message = `Plat "${selectedPlat.value.nomPlat}" utilisé avec succès !`
      if (bassinId.value) {
        resultatNourrissage.value.message += ` (Bassin: ${bassinNom.value || bassinId.value})`
      }
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
    watch(modeNourrissage, (newMode) => {
      if (newMode === 'plat' && platsDisponibles.value.length > 0) {
        selectedPlatId.value = platsDisponibles.value[0].idPlat
        selectedAlimentId.value = null
      } else if (newMode === 'aliment' && aliments.value.length > 0) {
        selectedAlimentId.value = aliments.value[0].idAliment
        selectedPlatId.value = null
      }
      showCustomAliment.value = false
      updateCalculs()
    })
    
    watch(quantitePlat, updateCalculs)
    watch(distributionMode, () => {
      // Forcer le recalcul des allocations
    })
    
    return {
      // Données
      aliments,
      platsDisponibles,
      poissonsAffames,
      stats,
      loading,
      resultatNourrissage,
      bassinId,
      bassinNom,
      
      // Sélections
      modeNourrissage,
      selectedAlimentId,
      selectedPlatId,
      showCustomAliment,
      quantitePlat,
      distributionMode,
      customAliment,
      
      // Computed
      selectedAliment,
      selectedPlat,
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
      selectPlat,
      toggleCustomAliment,
      incrementQuantity,
      decrementQuantity,
      updateCalculs,
      getActionButtonText,
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
      voirHistorique,
      loadData 
    }
  }
}
</script>

<style scoped>
@import '../assets/styles/nourissage.css';
</style>



