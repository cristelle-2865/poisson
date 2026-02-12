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
                <div class="aliment-details-grid">
                  <!-- AJOUT : Lipides et Vitamines -->
                  <div class="detail-row">
                    <span class="label">Protéines:</span>
                    <span class="value protein">{{ aliment.proteinesParKgAliment }} g/kg</span>
                  </div>
                  <div class="detail-row">
                    <span class="label">Glucides:</span>
                    <span class="value carbs">{{ aliment.glucidesParKgAliment }} g/kg</span>
                  </div>
                  <div class="detail-row">
                    <span class="label">Lipides:</span>
                    <span class="value lipid">{{ aliment.lipidesParKgAliment || 5 }} g/kg</span>
                  </div>
                  <div class="detail-row">
                    <span class="label">Vitamines:</span>
                    <span class="value vitamin">{{ (aliment.vitaminesParKgAliment || 0.5).toFixed(2) }} g/kg</span>
                  </div>
                  <div class="detail-row">
                    <span class="label">Prix:</span>
                    <span class="value price">{{ formatPrice(aliment.prixKgAliment) }} MGA/kg</span>
                  </div>
                  <!-- AJOUT : Indicateur de qualité -->
                  <div class="detail-row quality" v-if="alimentQuality[aliment.idAliment]">
                    <span class="label">Qualité:</span>
                    <span class="value" :class="alimentQuality[aliment.idAliment].class">
                      {{ alimentQuality[aliment.idAliment].text }}
                    </span>
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
                  
                  <!-- AJOUT : Calculs complets incluant lipides et vitamines -->
                  <div class="composition-calculs">
                    <div class="calculs-grid">
                      <div class="calcul-item">
                        <span class="label protein">💪 Protéines:</span>
                        <span class="value">{{ calculerProteines(comp).toFixed(2) }} g</span>
                      </div>
                      <div class="calcul-item">
                        <span class="label carbs">🍚 Glucides:</span>
                        <span class="value">{{ calculerGlucides(comp).toFixed(2) }} g</span>
                      </div>
                      <div class="calcul-item">
                        <span class="label lipid">⚡ Lipides:</span>
                        <span class="value">{{ calculerLipides(comp).toFixed(2) }} g</span>
                      </div>
                      <div class="calcul-item">
                        <span class="label vitamin">💊 Vitamines:</span>
                        <span class="value">{{ calculerVitamines(comp).toFixed(2) }} g</span>
                      </div>
                      <div class="calcul-item">
                        <span class="label">💰 Coût:</span>
                        <span class="value">{{ formatPrice(calculerCout(comp)) }} MGA</span>
                      </div>
                      <div class="calcul-item">
                        <span class="label">📊 Gain potentiel:</span>
                        <span class="value">{{ calculerGainAliment(comp).toFixed(2) }} g</span>
                      </div>
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
          
          <!-- Totaux complets -->
          <div class="totaux-section">
            <div class="total-item glass-inner">
              <span class="label">📦 Poids total:</span>
              <span class="value">{{ poidsTotal.toFixed(2) }} kg</span>
            </div>
            
            <div class="total-item glass-inner">
              <span class="label">💪 Protéines totales:</span>
              <span class="value protein">{{ proteinesTotal.toFixed(2) }} g</span>
            </div>
            
            <div class="total-item glass-inner">
              <span class="label">🍚 Glucides totales:</span>
              <span class="value carbs">{{ glucidesTotal.toFixed(2) }} g</span>
            </div>
            
            <!-- AJOUT : Lipides et Vitamines totaux -->
            <div class="total-item glass-inner">
              <span class="label">⚡ Lipides totaux:</span>
              <span class="value lipid">{{ lipidesTotal.toFixed(2) }} g</span>
            </div>
            
            <div class="total-item glass-inner">
              <span class="label">💊 Vitamines totales:</span>
              <span class="value vitamin">{{ vitaminesTotal.toFixed(2) }} g</span>
            </div>
            
            <div class="total-item glass-inner">
              <span class="label">📊 Protéines/kg:</span>
              <span class="value protein">{{ proteinesParKg.toFixed(2) }} g/kg</span>
            </div>
            
            <div class="total-item glass-inner">
              <span class="label">📊 Glucides/kg:</span>
              <span class="value carbs">{{ glucidesParKg.toFixed(2) }} g/kg</span>
            </div>
            
            <!-- AJOUT : Nutriments par kg -->
            <div class="total-item glass-inner">
              <span class="label">📊 Lipides/kg:</span>
              <span class="value lipid">{{ lipidesParKg.toFixed(2) }} g/kg</span>
            </div>
            
            <div class="total-item glass-inner">
              <span class="label">📊 Vitamines/kg:</span>
              <span class="value vitamin">{{ vitaminesParKg.toFixed(2) }} g/kg</span>
            </div>
            
            <div class="total-item total-cout glass-inner highlight">
              <span class="label">💰 Coût total:</span>
              <span class="value">{{ formatPrice(coutTotal) }} MGA</span>
            </div>
          </div>

          <!-- Analyse nutritionnelle complète -->
          <div class="analyse-section">
            <h3>Analyse nutritionnelle complète</h3>
            
            <!-- Graphique des ratios avec tous les nutriments -->
            <div class="nutriments-chart">
              <div class="chart-label">Composition nutritionnelle</div>
              <div class="chart-bars">
                <div class="chart-bar-container">
                  <div class="chart-bar protein" :style="{ width: pourcentageProteines + '%' }">
                    <span class="bar-text">{{ pourcentageProteines.toFixed(1) }}%</span>
                  </div>
                  <span class="bar-label">Protéines</span>
                </div>
                <div class="chart-bar-container">
                  <div class="chart-bar carbs" :style="{ width: pourcentageGlucides + '%' }">
                    <span class="bar-text">{{ pourcentageGlucides.toFixed(1) }}%</span>
                  </div>
                  <span class="bar-label">Glucides</span>
                </div>
                <div class="chart-bar-container">
                  <div class="chart-bar lipid" :style="{ width: pourcentageLipides + '%' }">
                    <span class="bar-text">{{ pourcentageLipides.toFixed(1) }}%</span>
                  </div>
                  <span class="bar-label">Lipides</span>
                </div>
                <div class="chart-bar-container">
                  <div class="chart-bar vitamin" :style="{ width: pourcentageVitamines + '%' }">
                    <span class="bar-text">{{ pourcentageVitamines.toFixed(1) }}%</span>
                  </div>
                  <span class="bar-label">Vitamines</span>
                </div>
              </div>
            </div>

            <!-- Ratios nutritionnels -->
            <div class="ratios-grid">
              <div class="ratio-item glass-inner">
                <span class="label">Ratio Protéines/Glucides:</span>
                <span class="value">{{ ratioProteinesGlucides }}</span>
              </div>
              
              <!-- AJOUT : Nouveaux ratios -->
              <div class="ratio-item glass-inner">
                <span class="label">Ratio Protéines/Lipides:</span>
                <span class="value">{{ ratioProteinesLipides }}</span>
              </div>
              
              <div class="ratio-item glass-inner">
                <span class="label">Coût par kg:</span>
                <span class="value">{{ formatPrice(coutParKg) }} MGA/kg</span>
              </div>
              
              <div class="ratio-item glass-inner">
                <span class="label">Coût par gain:</span>
                <span class="value">{{ formatPrice(coutParGain) }} MGA/g</span>
              </div>
            </div>

            <!-- Gain potentiel avec analyse complète -->
            <div class="gain-analysis glass-inner">
              <h4>Gain potentiel pour 5g de plat</h4>
              
              <div class="gain-details">
                <div class="gain-value">
                  <span class="big-value">{{ gainPotentiel.toFixed(1) }}</span>
                  <span class="unit">g</span>
                </div>
                
                <div class="nutriments-recus">
                  <p>Un poisson recevra:</p>
                  <ul>
                    <li>💪 <span class="protein">{{ (proteinesParKg * 0.005).toFixed(3) }} g</span> de protéines</li>
                    <li>🍚 <span class="carbs">{{ (glucidesParKg * 0.005).toFixed(3) }} g</span> de glucides</li>
                    <li>⚡ <span class="lipid">{{ (lipidesParKg * 0.005).toFixed(3) }} g</span> de lipides</li>
                    <li>💊 <span class="vitamin">{{ (vitaminesParKg * 0.005).toFixed(3) }} g</span> de vitamines</li>
                  </ul>
                </div>
              </div>
              
              <!-- Évaluation de la qualité -->
              <div class="quality-evaluation" :class="qualityClass">
                <span class="quality-icon">{{ qualityIcon }}</span>
                <span class="quality-text">{{ qualityText }}</span>
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

    <!-- Liste des plats existants avec analyses complètes -->
    <div class="existing-plats">
      <div class="card glass-card">
        <h2>Plats existants</h2>
        <div class="plats-list">
          <div v-for="plat in platsExistants" 
               :key="plat.idPlat"
               class="plat-item glass-inner">
            <div class="plat-header">
              <h3>{{ plat.nomPlat }}</h3>
              <div class="plat-badges">
                <span class="plat-status glass-badge" :class="{ 
                  'utilise': plat.estUtilisePlat, 
                  'disponible': !plat.estUtilisePlat 
                }">
                  {{ plat.estUtilisePlat ? 'Utilisé' : 'Disponible' }}
                </span>
                <!-- AJOUT : Badge qualité -->
                <span v-if="platQuality[plat.idPlat]" 
                      class="quality-badge glass-badge"
                      :class="platQuality[plat.idPlat].class">
                  {{ platQuality[plat.idPlat].text }}
                </span>
              </div>
            </div>
            
            <!-- Détails complets du plat -->
            <div class="plat-details-grid">
              <div class="detail">
                <span class="label">📦 Poids:</span>
                <span class="value">{{ plat.poidsTotalPlat }} kg</span>
              </div>
              <div class="detail">
                <span class="label">💪 Protéines:</span>
                <span class="value protein">{{ plat.proteinesParKgPlat }} g/kg</span>
              </div>
              <div class="detail">
                <span class="label">🍚 Glucides:</span>
                <span class="value carbs">{{ plat.glucidesParKgPlat }} g/kg</span>
              </div>
              <!-- AJOUT : Lipides et Vitamines -->
              <div class="detail">
                <span class="label">⚡ Lipides:</span>
                <span class="value lipid">{{ plat.lipidesParKgPlat || 0 }} g/kg</span>
              </div>
              <div class="detail">
                <span class="label">💊 Vitamines:</span>
                <span class="value vitamin">{{ plat.vitaminesParKgPlat || 0 }} g/kg</span>
              </div>
              <div class="detail">
                <span class="label">💰 Coût:</span>
                <span class="value price">{{ formatPrice(plat.coutTotalPlat) }} MGA</span>
              </div>
              <div class="detail">
                <span class="label">📊 Gain/5g:</span>
                <span class="value gain">{{ calculerGainPlat(plat).toFixed(1) }} g</span>
              </div>
            </div>
            
            <!-- Composition détaillée -->
            <div class="plat-composition">
              <span class="label">Composition:</span>
              <div class="composition-tags">
                <template v-if="plat.compositions && plat.compositions.length > 0">
                  <span v-for="comp in plat.compositions.slice(0, 3)" 
                        :key="comp.idCompositionPlat || comp.aliment?.idAliment"
                        class="tag glass-badge small"
                        :title="getCompositionDetails(comp)">
                    {{ getAlimentName(comp) }} 
                    <span class="tag-weight">{{ comp.poidsAlimentComposition }} kg</span>
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
            
            <!-- AJOUT : Analyse nutritionnelle rapide -->
            <div v-if="platQuality[plat.idPlat]" class="plat-analysis">
              <div class="analysis-meter">
                <div class="meter-bar">
                  <div class="meter-fill" :style="{ width: platQuality[plat.idPlat].score + '%' }"></div>
                </div>
                <span class="meter-score">{{ platQuality[plat.idPlat].score }}/100</span>
              </div>
              <div class="analysis-recommendation">
                {{ platQuality[plat.idPlat].recommendation }}
              </div>
            </div>
            
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
import platService from '../services/platService'
import alimentService from '../services/alimentService'
import { calculService } from '../services/calculService'

export default {
  name: 'PlatCreation',
  setup() {
    // Données
    const aliments = ref([])
    const platsExistants = ref([])
    const loading = ref(false)
    const message = ref('')
    const messageClass = ref('')
    
    // Cache pour les analyses
    const alimentQuality = ref({})
    const platQuality = ref({})
    
    // Nouveau plat
    const nouveauPlat = ref({
      nomPlat: '',
      descriptionPlat: ''
    })
    
    // Composition du plat
    const compositions = ref([])
    
    // Méthodes utilitaires
    const formatPrice = (price) => {
      if (!price && price !== 0) return '0'
      return parseFloat(price).toFixed(0).replace(/\B(?=(\d{3})+(?!\d))/g, ' ')
    }
    
    const getAlimentName = (composition) => {
      if (!composition.aliment) return 'Aliment inconnu'
      return composition.aliment.nomAliment || composition.aliment.nom || 'Aliment sans nom'
    }
    
    const getCompositionDetails = (comp) => {
      if (!comp.aliment) return ''
      return `${comp.aliment.nomAliment}\n` +
             `Protéines: ${comp.aliment.proteinesParKgAliment} g/kg\n` +
             `Glucides: ${comp.aliment.glucidesParKgAliment} g/kg\n` +
             `Lipides: ${comp.aliment.lipidesParKgAliment || 5} g/kg\n` +
             `Vitamines: ${comp.aliment.vitaminesParKgAliment || 0.5} g/kg`
    }
    
    // Analyser la qualité d'un aliment
    const analyserAliment = (aliment) => {
      if (!aliment) return
      
      const analysis = calculService.evaluerQualiteAliment({
        proteinesParKg: aliment.proteinesParKgAliment,
        glucidesParKg: aliment.glucidesParKgAliment,
        lipidesParKg: aliment.lipidesParKgAliment || 5,
        vitaminesParKg: aliment.vitaminesParKgAliment || 0.5
      })
      
      let qualityClass = 'neutral'
      let qualityText = analysis.qualite
      
      if (analysis.score >= 90) qualityClass = 'excellent'
      else if (analysis.score >= 75) qualityClass = 'good'
      else if (analysis.score >= 50) qualityClass = 'medium'
      else qualityClass = 'low'
      
      return {
        score: analysis.score,
        text: qualityText,
        class: qualityClass,
        details: analysis.details
      }
    }
    
    // Analyser la qualité d'un plat
    const analyserPlat = (plat) => {
      if (!plat) return
      
      // Reconstruire les données du plat pour analyse
      const platData = {
        proteinesParKg: plat.proteinesParKgPlat || 0,
        glucidesParKg: plat.glucidesParKgPlat || 0,
        lipidesParKg: plat.lipidesParKgPlat || 0,
        vitaminesParKg: plat.vitaminesParKgPlat || 0
      }
      
      const analysis = calculService.evaluerQualiteAliment(platData)
      
      let qualityClass = 'neutral'
      if (analysis.score >= 90) qualityClass = 'excellent'
      else if (analysis.score >= 75) qualityClass = 'good'
      else if (analysis.score >= 50) qualityClass = 'medium'
      else qualityClass = 'low'
      
      let recommendation = ''
      if (analysis.score >= 80) {
        recommendation = 'Excellent plat, parfait pour la croissance'
      } else if (analysis.score >= 60) {
        recommendation = 'Bon plat, peut être amélioré'
      } else {
        recommendation = 'Plat à équilibrer, manque de nutriments'
      }
      
      return {
        score: analysis.score,
        text: analysis.qualite,
        class: qualityClass,
        recommendation,
        details: analysis.details
      }
    }
    
    // Charger les données
    const chargerDonnees = async () => {
      try {
        // Charger les aliments
        const alimentsData = await alimentService.getAliments()
        aliments.value = alimentsData.filter(a => a.estActifAliment !== false)
        
        // Analyser chaque aliment
        aliments.value.forEach(aliment => {
          alimentQuality.value[aliment.idAliment] = analyserAliment(aliment)
        })
        
        // Charger les plats existants
        const platsData = await platService.getAllPlats()
        platsExistants.value = platsData
        
        // Analyser chaque plat
        platsExistants.value.forEach(plat => {
          platQuality.value[plat.idPlat] = analyserPlat(plat)
        })
        
      } catch (error) {
        console.error('Erreur chargement données:', error)
        afficherMessage('Erreur de chargement', 'error')
      }
    }
    
    // Calculs complets incluant lipides et vitamines
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
    
    // AJOUT : Calculs des lipides et vitamines
    const lipidesTotal = computed(() => {
      return compositions.value.reduce((total, comp) => {
        const quantiteKg = parseFloat(comp.quantite) || 0
        const lipidesParKg = parseFloat(comp.aliment.lipidesParKgAliment) || 5
        return total + (quantiteKg * lipidesParKg)
      }, 0)
    })
    
    const vitaminesTotal = computed(() => {
      return compositions.value.reduce((total, comp) => {
        const quantiteKg = parseFloat(comp.quantite) || 0
        const vitaminesParKg = parseFloat(comp.aliment.vitaminesParKgAliment) || 0.5
        return total + (quantiteKg * vitaminesParKg)
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
    
    // AJOUT : Nutriments par kg
    const lipidesParKg = computed(() => {
      if (poidsTotal.value <= 0) return 0
      return lipidesTotal.value / poidsTotal.value
    })
    
    const vitaminesParKg = computed(() => {
      if (poidsTotal.value <= 0) return 0
      return vitaminesTotal.value / poidsTotal.value
    })
    
    const coutParKg = computed(() => {
      if (poidsTotal.value <= 0) return 0
      return coutTotal.value / poidsTotal.value
    })
    
    const coutParGain = computed(() => {
      if (gainPotentiel.value <= 0) return 0
      return (coutParKg.value * 5) / gainPotentiel.value // Coût pour 5g de plat / gain
    })
    
    const ratioProteinesGlucides = computed(() => {
      if (glucidesTotal.value === 0) return '∞'
      const ratio = proteinesTotal.value / glucidesTotal.value
      return ratio.toFixed(2) + ':1'
    })
    
    // AJOUT : Nouveau ratio
    const ratioProteinesLipides = computed(() => {
      if (lipidesTotal.value === 0) return '∞'
      const ratio = proteinesTotal.value / lipidesTotal.value
      return ratio.toFixed(2) + ':1'
    })
    
    const pourcentageProteines = computed(() => {
      const total = proteinesTotal.value + glucidesTotal.value + lipidesTotal.value + vitaminesTotal.value
      if (total === 0) return 0
      return (proteinesTotal.value / total) * 100
    })
    
    const pourcentageGlucides = computed(() => {
      const total = proteinesTotal.value + glucidesTotal.value + lipidesTotal.value + vitaminesTotal.value
      if (total === 0) return 0
      return (glucidesTotal.value / total) * 100
    })
    
    // AJOUT : Pourcentages pour lipides et vitamines
    const pourcentageLipides = computed(() => {
      const total = proteinesTotal.value + glucidesTotal.value + lipidesTotal.value + vitaminesTotal.value
      if (total === 0) return 0
      return (lipidesTotal.value / total) * 100
    })
    
    const pourcentageVitamines = computed(() => {
      const total = proteinesTotal.value + glucidesTotal.value + lipidesTotal.value + vitaminesTotal.value
      if (total === 0) return 0
      return (vitaminesTotal.value / total) * 100
    })
    
    // Calcul du gain potentiel avec calculService
    const gainPotentiel = computed(() => {
      if (poidsTotal.value <= 0) return 0
      
      // Utiliser calculService pour un calcul précis
      const gain = calculService.calculerGainPoidsComplet(
        proteinesParKg.value * 0.005, // Convertir kg en g pour 5g de plat
        glucidesParKg.value * 0.005,
        lipidesParKg.value * 0.005,
        vitaminesParKg.value * 0.005
      )
      
      return gain
    })
    
    const qualityClass = computed(() => {
      if (gainPotentiel.value >= 20) return 'excellent'
      if (gainPotentiel.value >= 15) return 'good'
      if (gainPotentiel.value >= 10) return 'medium'
      return 'low'
    })
    
    const qualityIcon = computed(() => {
      if (gainPotentiel.value >= 20) return '🏆'
      if (gainPotentiel.value >= 15) return '👍'
      if (gainPotentiel.value >= 10) return '👌'
      return '⚠️'
    })
    
    const qualityText = computed(() => {
      if (gainPotentiel.value >= 20) return 'Excellent - Croissance optimale'
      if (gainPotentiel.value >= 15) return 'Bon - Très bonne croissance'
      if (gainPotentiel.value >= 10) return 'Satisfaisant - Croissance correcte'
      return 'Insuffisant - Augmentez les quantités'
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
    
    // Méthodes de calcul pour les compositions
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
    
    // AJOUT : Calculs lipides et vitamines
    const calculerLipides = (composition) => {
      const quantiteKg = parseFloat(composition.quantite) || 0
      const lipidesParKg = parseFloat(composition.aliment.lipidesParKgAliment) || 5
      return quantiteKg * lipidesParKg
    }
    
    const calculerVitamines = (composition) => {
      const quantiteKg = parseFloat(composition.quantite) || 0
      const vitaminesParKg = parseFloat(composition.aliment.vitaminesParKgAliment) || 0.5
      return quantiteKg * vitaminesParKg
    }
    
    const calculerCout = (composition) => {
      const quantiteKg = parseFloat(composition.quantite) || 0
      const prixKg = parseFloat(composition.aliment.prixKgAliment) || 0
      return quantiteKg * prixKg
    }
    
    // AJOUT : Calcul du gain pour un aliment individuel
    const calculerGainAliment = (composition) => {
      const proteines = calculerProteines(composition)
      const glucides = calculerGlucides(composition)
      const lipides = calculerLipides(composition)
      const vitamines = calculerVitamines(composition)
      
      return calculService.calculerGainPoidsComplet(proteines, glucides, lipides, vitamines)
    }
    
    // Calcul du gain pour un plat existant
    const calculerGainPlat = (plat) => {
      const proteines = (plat.proteinesParKgPlat || 0) * 0.005
      const glucides = (plat.glucidesParKgPlat || 0) * 0.005
      const lipides = (plat.lipidesParKgPlat || 0) * 0.005
      const vitamines = (plat.vitaminesParKgPlat || 0) * 0.005
      
      return calculService.calculerGainPoidsComplet(proteines, glucides, lipides, vitamines)
    }
    
    const ajouterAuPlat = (aliment) => {
      const index = compositions.value.findIndex(
        comp => comp.aliment.idAliment === aliment.idAliment
      )
      
      if (index === -1) {
        compositions.value.push({
          aliment: { ...aliment },
          quantite: 1.0
        })
      } else {
        compositions.value[index].quantite += 0.5
      }
      
      calculerTotaux()
    }
    
    const retirerAliment = (index) => {
      compositions.value.splice(index, 1)
      calculerTotaux()
    }
    
    const calculerTotaux = () => {
      // Cette fonction est maintenant vide car nous utilisons computed properties
      // Mais elle peut être utile pour déclencher des réactivités supplémentaires
    }
    
    const creerPlat = async () => {
      if (!peutCreer.value) return
      
      loading.value = true
      message.value = ''
      
      try {
        // Préparer les données DTO avec tous les nutriments
        const platData = {
          nomPlat: nouveauPlat.value.nomPlat.trim(),
          descriptionPlat: nouveauPlat.value.descriptionPlat || '',
          datePreparationPlat: new Date().toISOString().split('T')[0],
          estUtilisePlat: false,
          compositions: []
        }
        
        platData.compositions = compositions.value.map(comp => {
          if (!comp.aliment || !comp.aliment.idAliment) {
            throw new Error(`Aliment invalide dans la composition: ${JSON.stringify(comp)}`)
          }
          
          const quantite = parseFloat(comp.quantite)
          if (isNaN(quantite) || quantite <= 0) {
            throw new Error(`Quantité invalide: ${comp.quantite}`)
          }
          
          return {
            idAliment: comp.aliment.idAliment,
            poidsAlimentComposition: quantite
          }
        })
        
        console.log('📤 Création du plat avec données complètes:', platData)
        
        const result = await platService.createPlat(platData)
        console.log('✅ Plat créé:', result)
        
        afficherMessage('Plat créé avec succès!', 'success')
        
        await chargerDonnees()
        reinitialiser()
        
      } catch (error) {
        console.error('❌ Erreur création plat:', error)
        
        let errorMsg = error.message
        if (error.message.includes('idAliment') && error.message.includes('null')) {
          errorMsg = 'Erreur: Un aliment n\'a pas d\'ID valide. Essayez de le retirer et de le réajouter.'
        } else if (error.message.includes('Stock insuffisant')) {
          errorMsg = 'Erreur: Stock insuffisant pour un des aliments.'
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
      alimentQuality,
      platQuality,
      
      // Computed
      poidsTotal,
      proteinesTotal,
      glucidesTotal,
      lipidesTotal,
      vitaminesTotal,
      coutTotal,
      proteinesParKg,
      glucidesParKg,
      lipidesParKg,
      vitaminesParKg,
      coutParKg,
      coutParGain,
      ratioProteinesGlucides,
      ratioProteinesLipides,
      pourcentageProteines,
      pourcentageGlucides,
      pourcentageLipides,
      pourcentageVitamines,
      gainPotentiel,
      stockTotal,
      peutCreer,
      qualityClass,
      qualityIcon,
      qualityText,
      
      // Méthodes
      formatPrice,
      getAlimentName,
      getCompositionDetails,
      ajouterAuPlat,
      retirerAliment,
      calculerProteines,
      calculerGlucides,
      calculerLipides,
      calculerVitamines,
      calculerCout,
      calculerGainAliment,
      calculerGainPlat,
      calculerTotaux,
      creerPlat,
      reinitialiser
    }
  }
}
</script>

<style scoped>
@import '../assets/styles/plats.css';

/* Styles supplémentaires pour les nouveaux éléments */
.aliment-details-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 8px;
  margin-top: 8px;
}

.detail-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 4px 0;
  font-size: 13px;
}

.detail-row .label {
  color: #64748b;
}

.detail-row .value {
  font-weight: 600;
}

.detail-row .value.protein { color: #3b82f6; }
.detail-row .value.carbs { color: #10b981; }
.detail-row .value.lipid { color: #f59e0b; }
.detail-row .value.vitamin { color: #8b5cf6; }
.detail-row .value.price { color: #64748b; }

.detail-row.quality {
  grid-column: span 2;
  border-top: 1px solid #e2e8f0;
  margin-top: 4px;
  padding-top: 8px;
}

.calculs-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
  margin-top: 8px;
}

.calcul-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
  padding: 8px;
  background-color: #f8fafc;
  border-radius: 6px;
}

.calcul-item .label {
  font-size: 11px;
  color: #64748b;
}

.calcul-item .label.protein { color: #3b82f6; }
.calcul-item .label.carbs { color: #10b981; }
.calcul-item .label.lipid { color: #f59e0b; }
.calcul-item .label.vitamin { color: #8b5cf6; }

.calcul-item .value {
  font-size: 14px;
  font-weight: 600;
}

.nutriments-chart {
  margin: 20px 0;
}

.chart-label {
  font-size: 14px;
  font-weight: 600;
  margin-bottom: 12px;
  color: #334155;
}

.chart-bars {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.chart-bar-container {
  display: flex;
  align-items: center;
  gap: 8px;
}

.chart-bar {
  height: 24px;
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  padding-right: 8px;
  transition: width 0.3s ease;
}

.chart-bar.protein { background: linear-gradient(90deg, #3b82f6, #60a5fa); }
.chart-bar.carbs { background: linear-gradient(90deg, #10b981, #34d399); }
.chart-bar.lipid { background: linear-gradient(90deg, #f59e0b, #fbbf24); }
.chart-bar.vitamin { background: linear-gradient(90deg, #8b5cf6, #a78bfa); }

.bar-text {
  color: white;
  font-size: 12px;
  font-weight: 600;
  text-shadow: 0 1px 2px rgba(0,0,0,0.2);
}

.bar-label {
  font-size: 12px;
  color: #64748b;
  min-width: 70px;
}

.ratios-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
  margin-bottom: 20px;
}

.ratio-item {
  padding: 12px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.gain-analysis {
  margin-top: 20px;
  padding: 20px;
}

.gain-analysis h4 {
  margin-top: 0;
  margin-bottom: 16px;
  color: #334155;
}

.gain-details {
  display: flex;
  align-items: center;
  gap: 30px;
  margin-bottom: 20px;
}

.gain-value {
  display: flex;
  align-items: baseline;
  gap: 4px;
}

.big-value {
  font-size: 48px;
  font-weight: 700;
  color: #0f172a;
}

.unit {
  font-size: 16px;
  color: #64748b;
}

.nutriments-recus {
  flex: 1;
}

.nutriments-recus p {
  margin-top: 0;
  margin-bottom: 8px;
  font-weight: 500;
  color: #334155;
}

.nutriments-recus ul {
  margin: 0;
  padding-left: 20px;
  list-style-type: none;
}

.nutriments-recus li {
  margin-bottom: 4px;
  font-size: 13px;
}

.quality-evaluation {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px;
  border-radius: 8px;
}

.quality-evaluation.excellent {
  background-color: #dcfce7;
  color: #166534;
}

.quality-evaluation.good {
  background-color: #fef3c7;
  color: #92400e;
}

.quality-evaluation.medium {
  background-color: #fef3c7;
  color: #92400e;
}

.quality-evaluation.low {
  background-color: #fee2e2;
  color: #991b1b;
}

.quality-icon {
  font-size: 24px;
}

.quality-text {
  font-weight: 600;
}

.plat-badges {
  display: flex;
  gap: 8px;
  align-items: center;
}

.quality-badge {
  padding: 4px 8px;
  border-radius: 12px;
  font-size: 11px;
  font-weight: 600;
}

.quality-badge.excellent {
  background-color: #dcfce7;
  color: #166534;
}

.quality-badge.good {
  background-color: #fef3c7;
  color: #92400e;
}

.quality-badge.medium {
  background-color: #fef3c7;
  color: #92400e;
}

.quality-badge.low {
  background-color: #fee2e2;
  color: #991b1b;
}

.plat-details-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
  margin: 12px 0;
}

.plat-details-grid .detail {
  display: flex;
  flex-direction: column;
  gap: 4px;
  padding: 8px;
  background-color: #f8fafc;
  border-radius: 6px;
}

.plat-details-grid .detail .label {
  font-size: 11px;
  color: #64748b;
}

.plat-details-grid .detail .value {
  font-size: 14px;
  font-weight: 600;
}

.plat-analysis {
  margin-top: 12px;
  padding: 12px;
  background-color: #f8fafc;
  border-radius: 6px;
}

.analysis-meter {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 8px;
}

.meter-bar {
  flex: 1;
  height: 8px;
  background-color: #e2e8f0;
  border-radius: 4px;
  overflow: hidden;
}

.meter-fill {
  height: 100%;
  background: linear-gradient(90deg, #3b82f6, #10b981);
  border-radius: 4px;
}

.meter-score {
  font-weight: 600;
  font-size: 12px;
  color: #334155;
}

.analysis-recommendation {
  font-size: 12px;
  color: #64748b;
  font-style: italic;
}

.tag-weight {
  font-size: 10px;
  color: #64748b;
  margin-left: 4px;
}
</style>

