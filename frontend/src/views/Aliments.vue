<template>
  <div class="aliments-page">
    <!-- Header -->
    <div class="page-header">
      <h1>Gestion des Aliments</h1>
      <div class="header-actions">
        <button @click="refreshData" class="btn-refresh" title="Rafraîchir">
          🔄
        </button>
        <button @click="showAddModal" class="btn-add">
          <span>+</span> Ajouter un aliment
        </button>
      </div>
    </div>

    <!-- Statistiques des stocks -->
    <div class="stats-container">
      <div class="stat-card">
        <div class="stat-icon">📦</div>
        <div class="stat-content">
          <h3>Total stock</h3>
          <div class="stat-value">{{ totalStock }} kg</div>
        </div>
      </div>
      
      <div class="stat-card">
        <div class="stat-icon">⚠️</div>
        <div class="stat-content">
          <h3>Stocks bas</h3>
          <div class="stat-value warning">{{ stocksBasCount }}</div>
        </div>
      </div>
      
      <div class="stat-card">
        <div class="stat-icon">💰</div>
        <div class="stat-content">
          <h3>Valeur stock</h3>
          <div class="stat-value">{{ valeurStock }} MGA</div>
        </div>
      </div>
      
      <div class="stat-card">
        <div class="stat-icon">🍽️</div>
        <div class="stat-content">
          <h3>Qualité moyenne</h3>
          <div class="stat-value">{{ qualiteMoyenne }}</div>
        </div>
      </div>
    </div>

    <!-- Filtres -->
    <div class="filters-container">
      <div class="search-box">
        <input 
          type="text" 
          v-model="searchQuery" 
          placeholder="Rechercher un aliment..."
          @input="filterAliments"
        />
        <span class="search-icon">🔍</span>
      </div>
      
      <div class="filter-group">
        <select v-model="filterStatus" @change="filterAliments">
          <option value="">Tous les statuts</option>
          <option value="actif">Actif</option>
          <option value="inactif">Inactif</option>
        </select>
        
        <select v-model="filterStock" @change="filterAliments">
          <option value="">Tous les stocks</option>
          <option value="bas">Stock bas (&lt; 10kg)</option>
          <option value="normal">Stock normal</option>
          <option value="bon">Bon stock</option>
        </select>
        
        <select v-model="filterQualite" @change="filterAliments">
          <option value="">Toutes qualités</option>
          <option value="excellent">Excellent</option>
          <option value="bon">Bon</option>
          <option value="moyen">Moyen</option>
          <option value="faible">Faible</option>
        </select>
      </div>
    </div>

    <!-- Tableau des aliments -->
    <div class="table-container">
      <div class="table-responsive">
        <table class="aliments-table">
          <thead>
            <tr>
              <th @click="sortBy('nomAliment')" class="sortable">
                Nom
                <span v-if="sortField === 'nomAliment'" class="sort-icon">
                  {{ sortDirection === 'asc' ? '↑' : '↓' }}
                </span>
              </th>
              <th @click="sortBy('stockAliment')" class="sortable">
                Stock
                <span v-if="sortField === 'stockAliment'" class="sort-icon">
                  {{ sortDirection === 'asc' ? '↑' : '↓' }}
                </span>
              </th>
              <th>Protéines</th>
              <th>Glucides</th>
              <th>Lipides</th>
              <th>Vitamines</th>
              <th @click="sortBy('prixKgAliment')" class="sortable">
                Prix/kg
                <span v-if="sortField === 'prixKgAliment'" class="sort-icon">
                  {{ sortDirection === 'asc' ? '↑' : '↓' }}
                </span>
              </th>
              <th>Qualité</th>
              <th>Rentabilité</th>
              <th>Seuil min</th>
              <th>Statut</th>
              <th>Valeur</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="aliment in paginatedAliments" :key="aliment.idAliment" 
                :class="getRowClass(aliment)">
              <td>
                <div class="aliment-name">
                  <span class="name">{{ aliment.nomAliment }}</span>
                  <span v-if="!aliment.estActifAliment" class="badge inactive">Inactif</span>
                </div>
              </td>
              <td>
                <div class="stock-display">
                  <div class="stock-bar">
                    <div class="stock-fill" :style="{ width: getStockPercentage(aliment) + '%' }"></div>
                  </div>
                  <span class="stock-value" :class="getStockClass(aliment)">
                    {{ formatStock(aliment.stockAliment) }} kg
                  </span>
                </div>
              </td>
              <td>
                <span class="nutrient-value protein">
                  {{ formatNutrient(aliment.proteinesParKgAliment) }} g/kg
                </span>
              </td>
              <td>
                <span class="nutrient-value carbs">
                  {{ formatNutrient(aliment.glucidesParKgAliment) }} g/kg
                </span>
              </td>
              <td>
                <span class="nutrient-value lipid">
                  {{ formatNutrient(aliment.lipidesParKgAliment || 0) }} g/kg
                </span>
              </td>
              <td>
                <span class="nutrient-value vitamin">
                  {{ formatNutrient(aliment.vitaminesParKgAliment || 0, 2) }} g/kg
                </span>
              </td>
              <td>
                <span class="price-value">
                  {{ formatPrice(aliment.prixKgAliment) }} MGA
                </span>
              </td>
              <td>
                <div class="quality-indicator" :class="getQualityClass(aliment)">
                  <span class="quality-dot"></span>
                  <span class="quality-text">{{ getQualityText(aliment) }}</span>
                </div>
              </td>
              <td>
                <div class="rentability-indicator" :class="getRentabilityClass(aliment)">
                  <span class="rentability-text">{{ getRentabilityText(aliment) }}</span>
                </div>
              </td>
              <td>
                <span class="seuil-value">
                  {{ formatStock(aliment.seuilMinimumAliment) }} kg
                </span>
              </td>
              <td>
                <div class="status-container">
                  <span class="status-badge" :class="getStatusClass(aliment)">
                    {{ getStatusText(aliment) }}
                  </span>
                  <div class="stock-indicator" v-if="isStockLow(aliment)">
                    <span class="indicator-icon" title="Stock bas">⚠️</span>
                  </div>
                </div>
              </td>
              <td>
                <span class="valeur-stock">
                  {{ calculateValeurStock(aliment) }} MGA
                </span>
              </td>
              <td>
                <div class="action-buttons">
                  <button @click="editAliment(aliment)" class="btn-action edit" title="Modifier">
                    ✏️
                  </button>
                  
                  <button @click="toggleActive(aliment)" class="btn-action toggle" 
                          :title="aliment.estActifAliment ? 'Désactiver' : 'Activer'">
                    {{ aliment.estActifAliment ? '⏸️' : '▶️' }}
                  </button>
                  
                  <button @click="showStockModal(aliment)" class="btn-action stock" title="Gérer stock">
                    📦
                  </button>
                  
                  <button @click="showAnalytics(aliment)" class="btn-action analytics" title="Analyses">
                    📊
                  </button>
                  
                  <button @click="deleteAliment(aliment.idAliment, aliment.nomAliment)" 
                          class="btn-action delete" title="Supprimer">
                    🗑️
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- Pagination -->
      <div class="pagination" v-if="totalPages > 1">
        <button @click="prevPage" :disabled="currentPage === 1" class="page-btn">
          ←
        </button>
        
        <span class="page-info">
          Page {{ currentPage }} sur {{ totalPages }}
        </span>
        
        <button @click="nextPage" :disabled="currentPage === totalPages" class="page-btn">
          →
        </button>
        
        <select v-model="itemsPerPage" @change="resetPagination">
          <option value="10">10 par page</option>
          <option value="25">25 par page</option>
          <option value="50">50 par page</option>
        </select>
      </div>

      <!-- Aucun résultat -->
      <div v-if="filteredAliments.length === 0" class="no-results">
        <div class="no-results-icon">📦</div>
        <h3>Aucun aliment trouvé</h3>
        <p v-if="searchQuery || filterStatus || filterStock || filterQualite">
          Essayez de modifier vos critères de recherche
        </p>
        <button @click="showAddModal" class="btn-add-empty">
          Ajouter votre premier aliment
        </button>
      </div>
    </div>

    <!-- Modale d'ajout/modification -->
    <div v-if="showAlimentModal" class="modal-overlay">
      <div class="modal">
        <h3>{{ isEditing ? 'Modifier l\'aliment' : 'Nouvel aliment' }}</h3>
        
        <form @submit.prevent="saveAliment" class="aliment-form">
          <div class="form-group">
            <label>Nom de l'aliment *</label>
            <input type="text" v-model="currentAliment.nomAliment" required 
                   placeholder="Ex: Granules poissons croissance">
          </div>
          
          <div class="form-row">
            <div class="form-group">
              <label>Protéines (g/kg) *</label>
              <input type="number" v-model="currentAliment.proteinesParKgAliment" 
                     step="0.1" min="0" max="100" required>
              <div class="hint">Valeur typique: 10-40 g/kg</div>
            </div>
            
            <div class="form-group">
              <label>Glucides (g/kg) *</label>
              <input type="number" v-model="currentAliment.glucidesParKgAliment" 
                     step="0.1" min="0" max="100" required>
              <div class="hint">Valeur typique: 10-30 g/kg</div>
            </div>
          </div>
          
          <div class="form-row">
            <div class="form-group">
              <label>Lipides (g/kg) *</label>
              <input type="number" v-model="currentAliment.lipidesParKgAliment" 
                     step="0.1" min="0" max="50" required>
              <div class="hint">Valeur typique: 2-15 g/kg</div>
            </div>
            
            <div class="form-group">
              <label>Vitamines (g/kg) *</label>
              <input type="number" v-model="currentAliment.vitaminesParKgAliment" 
                     step="0.01" min="0" max="5" required>
              <div class="hint">Valeur typique: 0.1-2 g/kg</div>
            </div>
          </div>
          
          <div class="form-row">
            <div class="form-group">
              <label>Prix par kg (MGA) *</label>
              <input type="number" v-model="currentAliment.prixKgAliment" 
                     step="100" min="0" required>
            </div>
            
            <div class="form-group">
              <label>Stock initial (kg)</label>
              <input type="number" v-model="currentAliment.stockAliment" 
                     step="0.1" min="0">
            </div>
          </div>
          
          <div class="form-row">
            <div class="form-group">
              <label>Seuil minimum (kg)</label>
              <input type="number" v-model="currentAliment.seuilMinimumAliment" 
                     step="0.1" min="0" placeholder="10">
            </div>
            
            <div class="form-group checkbox-group">
              <label>
                <input type="checkbox" v-model="currentAliment.estActifAliment">
                Actif
              </label>
            </div>
          </div>
          
          <!-- Analyse nutritionnelle en temps réel -->
          <div class="form-preview" v-if="showAnalysisPreview">
            <h4>Analyse nutritionnelle</h4>
            <div class="analysis-grid">
              <div class="analysis-item">
                <span class="analysis-label">Qualité:</span>
                <span class="analysis-value" :class="analysis.qualityClass">
                  {{ analysis.quality }}
                </span>
              </div>
              <div class="analysis-item">
                <span class="analysis-label">Rentabilité:</span>
                <span class="analysis-value" :class="analysis.rentabilityClass">
                  {{ analysis.rentability }}
                </span>
              </div>
              <div class="analysis-item">
                <span class="analysis-label">Score:</span>
                <span class="analysis-value">{{ analysis.score }}/100</span>
              </div>
              <div class="analysis-item">
                <span class="analysis-label">Coût/gain:</span>
                <span class="analysis-value">{{ analysis.costPerGain }} MGA/g</span>
              </div>
            </div>
            
            <div class="nutrient-ratios">
              <h5>Ratios nutritionnels</h5>
              <div class="ratios-grid">
                <div class="ratio-item">
                  <div class="ratio-bar">
                    <div class="ratio-fill protein" :style="{ width: analysis.ratios.proteines + '%' }"></div>
                  </div>
                  <span class="ratio-label">Protéines {{ analysis.ratios.proteines }}%</span>
                </div>
                <div class="ratio-item">
                  <div class="ratio-bar">
                    <div class="ratio-fill carbs" :style="{ width: analysis.ratios.glucides + '%' }"></div>
                  </div>
                  <span class="ratio-label">Glucides {{ analysis.ratios.glucides }}%</span>
                </div>
                <div class="ratio-item">
                  <div class="ratio-bar">
                    <div class="ratio-fill lipid" :style="{ width: analysis.ratios.lipides + '%' }"></div>
                  </div>
                  <span class="ratio-label">Lipides {{ analysis.ratios.lipides }}%</span>
                </div>
              </div>
              <div class="ratio-recommendation">
                {{ analysis.recommendation }}
              </div>
            </div>
          </div>
          
          <button type="button" @click="toggleAnalysisPreview" class="btn-toggle-analysis">
            {{ showAnalysisPreview ? 'Masquer l\'analyse' : 'Voir l\'analyse nutritionnelle' }}
          </button>
          
          <div class="modal-actions">
            <button type="submit" class="btn-save" :disabled="loading">
              <span v-if="loading" class="spinner"></span>
              {{ loading ? 'Enregistrement...' : 'Enregistrer' }}
            </button>
            <button type="button" @click="closeModal" class="btn-cancel">
              Annuler
            </button>
          </div>
        </form>
      </div>
    </div>

    <!-- Modale d'analyse détaillée -->
    <div v-if="showAnalyticsModal" class="modal-overlay">
      <div class="modal large">
        <h3>Analyse détaillée - {{ currentAliment?.nomAliment }}</h3>
        
        <div class="analytics-grid">
          <!-- Scores -->
          <div class="analytics-card">
            <h4>📊 Scores nutritionnels</h4>
            <div class="score-meter">
              <div class="meter-background">
                <div class="meter-fill" :style="{ width: detailedAnalysis.score + '%' }"></div>
              </div>
              <div class="score-value">{{ detailedAnalysis.score }}/100</div>
            </div>
            <div class="score-details">
              <div class="score-item">
                <span>Protéines:</span>
                <span>{{ detailedAnalysis.scores.proteines }}/100</span>
              </div>
              <div class="score-item">
                <span>Glucides:</span>
                <span>{{ detailedAnalysis.scores.glucides }}/100</span>
              </div>
              <div class="score-item">
                <span>Lipides:</span>
                <span>{{ detailedAnalysis.scores.lipides }}/100</span>
              </div>
              <div class="score-item">
                <span>Vitamines:</span>
                <span>{{ detailedAnalysis.scores.vitamines }}/100</span>
              </div>
            </div>
          </div>
          
          <!-- Coût-efficacité -->
          <div class="analytics-card">
            <h4>💰 Coût-efficacité</h4>
            <div class="efficiency-stats">
              <div class="stat">
                <div class="stat-label">Coût par gain</div>
                <div class="stat-value">{{ detailedAnalysis.costPerGain }} MGA/g</div>
              </div>
              <div class="stat">
                <div class="stat-label">Gain par kg</div>
                <div class="stat-value">{{ detailedAnalysis.gainPerKg }} g/kg</div>
              </div>
              <div class="stat">
                <div class="stat-label">Efficacité</div>
                <div class="stat-value">{{ detailedAnalysis.efficiency }}%</div>
              </div>
              <div class="stat">
                <div class="stat-label">Rentabilité</div>
                <div class="stat-value" :class="detailedAnalysis.rentabilityClass">
                  {{ detailedAnalysis.rentability }}
                </div>
              </div>
            </div>
          </div>
          
          <!-- Composition -->
          <div class="analytics-card full-width">
            <h4>🍽️ Composition nutritionnelle</h4>
            <div class="composition-chart">
              <div class="chart-bars">
                <div class="chart-bar-container">
                  <div class="chart-bar protein" :style="{ height: detailedAnalysis.composition.proteines + '%' }">
                    <span class="bar-label">{{ detailedAnalysis.composition.proteines }}g/kg</span>
                  </div>
                  <div class="bar-title">Protéines</div>
                </div>
                <div class="chart-bar-container">
                  <div class="chart-bar carbs" :style="{ height: detailedAnalysis.composition.glucides + '%' }">
                    <span class="bar-label">{{ detailedAnalysis.composition.glucides }}g/kg</span>
                  </div>
                  <div class="bar-title">Glucides</div>
                </div>
                <div class="chart-bar-container">
                  <div class="chart-bar lipid" :style="{ height: detailedAnalysis.composition.lipides + '%' }">
                    <span class="bar-label">{{ detailedAnalysis.composition.lipides }}g/kg</span>
                  </div>
                  <div class="bar-title">Lipides</div>
                </div>
                <div class="chart-bar-container">
                  <div class="chart-bar vitamin" :style="{ height: detailedAnalysis.composition.vitamines * 20 + '%' }">
                    <span class="bar-label">{{ detailedAnalysis.composition.vitamines }}g/kg</span>
                  </div>
                  <div class="bar-title">Vitamines</div>
                </div>
              </div>
            </div>
          </div>
          
          <!-- Recommandations -->
          <div class="analytics-card full-width">
            <h4>💡 Recommandations</h4>
            <div class="recommendations">
              <div class="recommendation" :class="{ 'warning': rec.priority === 'high', 'info': rec.priority === 'medium' }"
                   v-for="(rec, index) in detailedAnalysis.recommendations" :key="index">
                <span class="rec-icon">{{ rec.icon }}</span>
                <span class="rec-text">{{ rec.text }}</span>
              </div>
            </div>
            <div class="best-usage">
              <h5>🏆 Meilleur usage</h5>
              <p>{{ detailedAnalysis.bestUsage }}</p>
            </div>
          </div>
        </div>
        
        <div class="modal-actions">
          <button @click="closeAnalyticsModal" class="btn-close">
            Fermer
          </button>
          <button @click="exportAnalysis" class="btn-export">
            📥 Exporter l'analyse
          </button>
        </div>
      </div>
    </div>

    <!-- Modale de gestion de stock -->
    <div v-if="showStockManagementModal" class="modal-overlay">
      <div class="modal">
        <h3>Gestion du stock</h3>
        <p class="aliment-name">{{ currentAliment?.nomAliment }}</p>
        
        <div class="stock-info">
          <div class="stock-current">
            <span class="label">Stock actuel:</span>
            <span class="value">{{ formatStock(currentAliment?.stockAliment) }} kg</span>
          </div>
          <div class="stock-seuil">
            <span class="label">Seuil minimum:</span>
            <span class="value">{{ formatStock(currentAliment?.seuilMinimumAliment) }} kg</span>
          </div>
        </div>
        
        <div class="stock-form">
          <div class="form-group">
            <label>Action</label>
            <select v-model="stockAction">
              <option value="add">Ajouter du stock</option>
              <option value="remove">Retirer du stock</option>
              <option value="set">Définir le stock</option>
            </select>
          </div>
          
          <div class="form-group">
            <label>Quantité (kg)</label>
            <input type="number" v-model="stockQuantity" 
                   step="0.1" min="0" :max="stockAction === 'remove' ? currentAliment?.stockAliment : null">
          </div>
          
          <div class="stock-result">
            <span class="label">Nouveau stock:</span>
            <span class="value">{{ calculateNewStock() }} kg</span>
          </div>
        </div>
        
        <div class="modal-actions">
          <button @click="updateStock" class="btn-save" :disabled="!stockQuantity || loading">
            <span v-if="loading" class="spinner"></span>
            {{ loading ? 'Mise à jour...' : 'Mettre à jour' }}
          </button>
          <button @click="closeStockModal" class="btn-cancel">
            Annuler
          </button>
        </div>
      </div>
    </div>

    <!-- Modale de suppression -->
    <div v-if="showDeleteModal" class="modal-overlay">
      <div class="modal">
        <h3>Confirmer la suppression</h3>
        <p>Voulez-vous vraiment supprimer l'aliment "{{ alimentToDelete?.nomAliment }}" ?</p>
        <p class="warning-text">⚠️ Cette action est irréversible !</p>
        <div class="modal-actions">
          <button @click="confirmDelete" class="btn-delete-confirm">
            Oui, supprimer
          </button>
          <button @click="cancelDelete" class="btn-cancel">
            Annuler
          </button>
        </div>
      </div>
    </div>

    <!-- Export et actions -->
    <div class="batch-actions">
      <button @click="exportToCSV" class="btn-export">
        📊 Exporter en CSV
      </button>
      <button @click="exportNutritionReport" class="btn-export">
        📈 Rapport nutritionnel
      </button>
      <button @click="reorderAllLowStock" class="btn-batch" v-if="stocksBasCount > 0">
        📋 Générer commande pour stocks bas
      </button>
    </div>
  </div>
</template>

<script>
import { ref, computed, onMounted, watch } from 'vue'
import api from '../services/api'
import { calculService } from '../services/calculService'

export default {
  name: 'Aliments',
  setup() {
    // Données
    const aliments = ref([])
    const filteredAliments = ref([])
    const searchQuery = ref('')
    const filterStatus = ref('')
    const filterStock = ref('')
    const filterQualite = ref('')
    const sortField = ref('nomAliment')
    const sortDirection = ref('asc')
    const currentPage = ref(1)
    const itemsPerPage = ref(25)
    const loading = ref(false)
    
    // Modales
    const showAlimentModal = ref(false)
    const showStockManagementModal = ref(false)
    const showDeleteModal = ref(false)
    const showAnalyticsModal = ref(false)
    const showAnalysisPreview = ref(false)
    const isEditing = ref(false)
    
    // Données courantes
    const currentAliment = ref({
      nomAliment: '',
      proteinesParKgAliment: 10,
      glucidesParKgAliment: 10,
      lipidesParKgAliment: 5,
      vitaminesParKgAliment: 0.5,
      prixKgAliment: 2000,
      stockAliment: 0,
      seuilMinimumAliment: 10,
      estActifAliment: true
    })
    
    const alimentToDelete = ref(null)
    const stockAction = ref('add')
    const stockQuantity = ref(0)
    const detailedAnalysis = ref({})

    // Calculs
    const totalStock = computed(() => {
      return aliments.value.reduce((sum, aliment) => {
        return sum + parseFloat(aliment.stockAliment || 0)
      }, 0).toFixed(2)
    })

    const stocksBasCount = computed(() => {
      return aliments.value.filter(aliment => {
        const stock = parseFloat(aliment.stockAliment || 0)
        const seuil = parseFloat(aliment.seuilMinimumAliment || 10)
        return stock < seuil
      }).length
    })

    const valeurStock = computed(() => {
      return aliments.value.reduce((sum, aliment) => {
        const stock = parseFloat(aliment.stockAliment || 0)
        const prix = parseFloat(aliment.prixKgAliment || 0)
        const valeur = stock * prix
        return sum + (isNaN(valeur) ? 0 : valeur)
      }, 0).toFixed(0)
    })

    const qualiteMoyenne = computed(() => {
      const scores = aliments.value.map(aliment => {
        const analysis = calculService.evaluerQualiteAliment({
          proteinesParKg: aliment.proteinesParKgAliment,
          glucidesParKg: aliment.glucidesParKgAliment,
          lipidesParKg: aliment.lipidesParKgAliment || 5,
          vitaminesParKg: aliment.vitaminesParKgAliment || 0.5
        })
        return analysis.score
      })
      
      if (scores.length === 0) return 'N/A'
      const moyenne = scores.reduce((a, b) => a + b, 0) / scores.length
      return Math.round(moyenne) + '/100'
    })

    // Analyse en temps réel
    const analysis = computed(() => {
      const aliment = currentAliment.value
      if (!aliment.nomAliment) {
        return {
          quality: 'Non évalué',
          qualityClass: 'neutral',
          rentability: 'Non évalué',
          rentabilityClass: 'neutral',
          score: 0,
          costPerGain: 0,
          ratios: { proteines: 0, glucides: 0, lipides: 0 },
          recommendation: ''
        }
      }
      
      const qualityAnalysis = calculService.evaluerQualiteAliment({
        proteinesParKg: aliment.proteinesParKgAliment,
        glucidesParKg: aliment.glucidesParKgAliment,
        lipidesParKg: aliment.lipidesParKgAliment || 5,
        vitaminesParKg: aliment.vitaminesParKgAliment || 0.5
      })
      
      const costAnalysis = calculService.calculerCoutEfficacite({
        ...aliment,
        lipidesParKg: aliment.lipidesParKgAliment || 5,
        vitaminesParKg: aliment.vitaminesParKgAliment || 0.5
      })
      
      // Calcul des ratios
      const total = aliment.proteinesParKgAliment + aliment.glucidesParKgAliment + (aliment.lipidesParKgAliment || 5)
      const ratioProteines = total > 0 ? (aliment.proteinesParKgAliment / total) * 100 : 0
      const ratioGlucides = total > 0 ? (aliment.glucidesParKgAliment / total) * 100 : 0
      const ratioLipides = total > 0 ? ((aliment.lipidesParKgAliment || 5) / total) * 100 : 0
      
      let recommendation = ''
      if (ratioProteines < 30) {
        recommendation = 'Augmenter les protéines pour une meilleure croissance'
      } else if (ratioLipides < 10) {
        recommendation = 'Ajouter des lipides pour plus d\'énergie'
      } else if (qualityAnalysis.score >= 80) {
        recommendation = 'Composition excellente !'
      } else {
        recommendation = 'Composition correcte'
      }
      
      return {
        quality: qualityAnalysis.qualite,
        qualityClass: getQualityClassFromScore(qualityAnalysis.score),
        rentability: costAnalysis.rentabilite,
        rentabilityClass: getRentabilityClassFromText(costAnalysis.rentabilite),
        score: qualityAnalysis.score,
        costPerGain: costAnalysis.coutParGain,
        ratios: {
          proteines: Math.round(ratioProteines),
          glucides: Math.round(ratioGlucides),
          lipides: Math.round(ratioLipides)
        },
        recommendation
      }
    })

    // Pagination
    const totalPages = computed(() => {
      return Math.ceil(filteredAliments.value.length / itemsPerPage.value)
    })

    const paginatedAliments = computed(() => {
      const start = (currentPage.value - 1) * itemsPerPage.value
      const end = start + itemsPerPage.value
      return filteredAliments.value.slice(start, end)
    })

    // Charger les données
    const loadData = async () => {
      loading.value = true
      try {
        const response = await api.get('/aliments')
        aliments.value = response.data
        filteredAliments.value = aliments.value
        filterAliments()
      } catch (error) {
        console.error('Erreur chargement aliments:', error)
        alert('Impossible de charger les aliments')
      } finally {
        loading.value = false
      }
    }

    // Filtrer les aliments
    const filterAliments = () => {
      let filtered = [...aliments.value]

      // Recherche par nom
      if (searchQuery.value) {
        const query = searchQuery.value.toLowerCase()
        filtered = filtered.filter(aliment => 
          aliment.nomAliment.toLowerCase().includes(query)
        )
      }

      // Filtre par statut
      if (filterStatus.value) {
        if (filterStatus.value === 'actif') {
          filtered = filtered.filter(aliment => aliment.estActifAliment)
        } else {
          filtered = filtered.filter(aliment => !aliment.estActifAliment)
        }
      }

      // Filtre par stock
      if (filterStock.value) {
        switch (filterStock.value) {
          case 'bas':
            filtered = filtered.filter(aliment => {
              const stock = parseFloat(aliment.stockAliment || 0)
              const seuil = parseFloat(aliment.seuilMinimumAliment || 10)
              return stock < seuil
            })
            break
          case 'normal':
            filtered = filtered.filter(aliment => {
              const stock = parseFloat(aliment.stockAliment || 0)
              const seuil = parseFloat(aliment.seuilMinimumAliment || 10)
              return stock >= seuil && stock < 50
            })
            break
          case 'bon':
            filtered = filtered.filter(aliment => {
              const stock = parseFloat(aliment.stockAliment || 0)
              return stock >= 50
            })
            break
        }
      }

      // Filtre par qualité
      if (filterQualite.value) {
        filtered = filtered.filter(aliment => {
          const analysis = calculService.evaluerQualiteAliment({
            proteinesParKg: aliment.proteinesParKgAliment,
            glucidesParKg: aliment.glucidesParKgAliment,
            lipidesParKg: aliment.lipidesParKgAliment || 5,
            vitaminesParKg: aliment.vitaminesParKgAliment || 0.5
          })
          
          switch (filterQualite.value) {
            case 'excellent': return analysis.score >= 90
            case 'bon': return analysis.score >= 75 && analysis.score < 90
            case 'moyen': return analysis.score >= 50 && analysis.score < 75
            case 'faible': return analysis.score < 50
            default: return true
          }
        })
      }

      // Trier les résultats
      filtered.sort((a, b) => {
        let aValue = a[sortField.value]
        let bValue = b[sortField.value]

        // Pour les propriétés imbriquées
        if (sortField.value.includes('.')) {
          const fields = sortField.value.split('.')
          aValue = fields.reduce((obj, field) => obj?.[field], a)
          bValue = fields.reduce((obj, field) => obj?.[field], b)
        }

        if (typeof aValue === 'string') {
          return sortDirection.value === 'asc' 
            ? aValue.localeCompare(bValue)
            : bValue.localeCompare(aValue)
        } else {
          // Convertir en nombre pour la comparaison
          aValue = parseFloat(aValue || 0)
          bValue = parseFloat(bValue || 0)
          return sortDirection.value === 'asc'
            ? aValue - bValue
            : bValue - aValue
        }
      })

      filteredAliments.value = filtered
      currentPage.value = 1
    }

    // Trier par colonne
    const sortBy = (field) => {
      if (sortField.value === field) {
        sortDirection.value = sortDirection.value === 'asc' ? 'desc' : 'asc'
      } else {
        sortField.value = field
        sortDirection.value = 'asc'
      }
      filterAliments()
    }

    // Formater les nombres
    const formatStock = (stock) => {
      if (!stock && stock !== 0) return '0.00'
      return parseFloat(stock).toFixed(2)
    }

    const formatNutrient = (nutrient, decimals = 1) => {
      if (!nutrient && nutrient !== 0) return '0.0'
      return parseFloat(nutrient).toFixed(decimals)
    }

    const formatPrice = (price) => {
      if (!price && price !== 0) return '0'
      return parseFloat(price).toFixed(0).replace(/\B(?=(\d{3})+(?!\d))/g, ' ')
    }

    // Classes CSS
    const getRowClass = (aliment) => {
      const classes = []
      if (!aliment.estActifAliment) classes.push('inactive')
      if (isStockLow(aliment)) classes.push('stock-low')
      return classes.join(' ')
    }

    const getStockClass = (aliment) => {
      if (isStockLow(aliment)) return 'stock-low'
      if (parseFloat(aliment.stockAliment || 0) >= 50) return 'stock-high'
      return 'stock-normal'
    }

    const getStatusClass = (aliment) => {
      if (!aliment.estActifAliment) return 'inactive'
      if (isStockLow(aliment)) return 'warning'
      return 'active'
    }

    const getStatusText = (aliment) => {
      if (!aliment.estActifAliment) return 'Inactif'
      if (isStockLow(aliment)) return 'Stock bas'
      return 'Actif'
    }

    const getQualityClass = (aliment) => {
      const analysis = calculService.evaluerQualiteAliment({
        proteinesParKg: aliment.proteinesParKgAliment,
        glucidesParKg: aliment.glucidesParKgAliment,
        lipidesParKg: aliment.lipidesParKgAliment || 5,
        vitaminesParKg: aliment.vitaminesParKgAliment || 0.5
      })
      return getQualityClassFromScore(analysis.score)
    }

    const getQualityText = (aliment) => {
      const analysis = calculService.evaluerQualiteAliment({
        proteinesParKg: aliment.proteinesParKgAliment,
        glucidesParKg: aliment.glucidesParKgAliment,
        lipidesParKg: aliment.lipidesParKgAliment || 5,
        vitaminesParKg: aliment.vitaminesParKgAliment || 0.5
      })
      return analysis.qualite
    }

    const getRentabilityClass = (aliment) => {
      const analysis = calculService.calculerCoutEfficacite({
        ...aliment,
        lipidesParKg: aliment.lipidesParKgAliment || 5,
        vitaminesParKg: aliment.vitaminesParKgAliment || 0.5
      })
      return getRentabilityClassFromText(analysis.rentabilite)
    }

    const getRentabilityText = (aliment) => {
      const analysis = calculService.calculerCoutEfficacite({
        ...aliment,
        lipidesParKg: aliment.lipidesParKgAliment || 5,
        vitaminesParKg: aliment.vitaminesParKgAliment || 0.5
      })
      return analysis.rentabilite
    }

    // Fonctions utilitaires pour les classes
    const getQualityClassFromScore = (score) => {
      if (score >= 90) return 'excellent'
      if (score >= 75) return 'good'
      if (score >= 50) return 'medium'
      return 'low'
    }

    const getRentabilityClassFromText = (rentability) => {
      switch(rentability) {
        case 'Très rentable': return 'excellent'
        case 'Rentable': return 'good'
        case 'Moyenne': return 'medium'
        case 'Peu rentable': return 'low'
        case 'Non rentable': return 'very-low'
        default: return 'neutral'
      }
    }

    // Calculs
    const getStockPercentage = (aliment) => {
      const max = 100 // On considère 100kg comme 100%
      const stock = parseFloat(aliment.stockAliment || 0)
      return Math.min((stock / max) * 100, 100)
    }

    const isStockLow = (aliment) => {
      const stock = parseFloat(aliment.stockAliment || 0)
      const seuil = parseFloat(aliment.seuilMinimumAliment || 10)
      return stock < seuil
    }

    const calculateValeurStock = (aliment) => {
      const stock = parseFloat(aliment.stockAliment || 0)
      const prix = parseFloat(aliment.prixKgAliment || 0)
      const valeur = stock * prix
      return valeur.toFixed(0).replace(/\B(?=(\d{3})+(?!\d))/g, ' ')
    }

    // Pagination
    const prevPage = () => {
      if (currentPage.value > 1) currentPage.value--
    }

    const nextPage = () => {
      if (currentPage.value < totalPages.value) currentPage.value++
    }

    const resetPagination = () => {
      currentPage.value = 1
    }

    // Rafraîchir
    const refreshData = () => {
      loadData()
    }

    // Modale d'ajout
    const showAddModal = () => {
      isEditing.value = false
      currentAliment.value = {
        nomAliment: '',
        proteinesParKgAliment: 10,
        glucidesParKgAliment: 10,
        lipidesParKgAliment: 5,
        vitaminesParKgAliment: 0.5,
        prixKgAliment: 2000,
        stockAliment: 0,
        seuilMinimumAliment: 10,
        estActifAliment: true
      }
      showAnalysisPreview.value = false
      showAlimentModal.value = true
    }

    // Modifier un aliment
    const editAliment = (aliment) => {
      isEditing.value = true
      currentAliment.value = { 
        ...aliment,
        lipidesParKgAliment: aliment.lipidesParKgAliment || 5,
        vitaminesParKgAliment: aliment.vitaminesParKgAliment || 0.5
      }
      showAnalysisPreview.value = false
      showAlimentModal.value = true
    }

    // Toggle analyse prévisualisation
    const toggleAnalysisPreview = () => {
      showAnalysisPreview.value = !showAnalysisPreview.value
    }

    // Sauvegarder l'aliment
    const saveAliment = async () => {
      try {
        loading.value = true
        
        // Préparer les données
        const alimentData = {
          ...currentAliment.value,
          // S'assurer que les nombres sont bien des BigDecimals (strings)
          proteinesParKgAliment: parseFloat(currentAliment.value.proteinesParKgAliment),
          glucidesParKgAliment: parseFloat(currentAliment.value.glucidesParKgAliment),
          lipidesParKgAliment: parseFloat(currentAliment.value.lipidesParKgAliment || 5),
          vitaminesParKgAliment: parseFloat(currentAliment.value.vitaminesParKgAliment || 0.5),
          prixKgAliment: parseFloat(currentAliment.value.prixKgAliment),
          stockAliment: parseFloat(currentAliment.value.stockAliment || 0),
          seuilMinimumAliment: parseFloat(currentAliment.value.seuilMinimumAliment || 10)
        }
        
        if (isEditing.value) {
          // Mise à jour
          await api.put(`/aliments/${currentAliment.value.idAliment}`, alimentData)
        } else {
          // Création
          await api.post('/aliments', alimentData)
        }
        
        await loadData()
        closeModal()
        alert(isEditing.value ? 'Aliment mis à jour avec succès' : 'Aliment créé avec succès')
      } catch (error) {
        console.error('Erreur sauvegarde:', error)
        alert('Erreur lors de la sauvegarde: ' + (error.response?.data?.message || error.message))
      } finally {
        loading.value = false
      }
    }

    // Fermer modale
    const closeModal = () => {
      showAlimentModal.value = false
      currentAliment.value = {}
      showAnalysisPreview.value = false
    }

    // Basculer actif/inactif
    const toggleActive = async (aliment) => {
      try {
        const updatedAliment = {
          ...aliment,
          estActifAliment: !aliment.estActifAliment
        }
        
        await api.put(`/aliments/${aliment.idAliment}`, updatedAliment)
        await loadData()
        alert(`Aliment ${updatedAliment.estActifAliment ? 'activé' : 'désactivé'} avec succès`)
      } catch (error) {
        console.error('Erreur changement statut:', error)
        alert('Erreur lors du changement de statut')
      }
    }

    // Gestion de stock
    const showStockModal = (aliment) => {
      currentAliment.value = { ...aliment }
      stockAction.value = 'add'
      stockQuantity.value = 0
      showStockManagementModal.value = true
    }

    const calculateNewStock = () => {
      const current = parseFloat(currentAliment.value?.stockAliment || 0)
      const quantity = parseFloat(stockQuantity.value || 0)
      
      switch (stockAction.value) {
        case 'add':
          return (current + quantity).toFixed(2)
        case 'remove':
          return (current - quantity).toFixed(2)
        case 'set':
          return quantity.toFixed(2)
        default:
          return current.toFixed(2)
      }
    }

    const updateStock = async () => {
      try {
        loading.value = true
        
        // Calculer le nouveau stock selon l'action
        let newStock
        const current = parseFloat(currentAliment.value.stockAliment || 0)
        const quantity = parseFloat(stockQuantity.value || 0)
        
        switch (stockAction.value) {
          case 'add':
            newStock = current + quantity
            break
          case 'remove':
            newStock = current - quantity
            if (newStock < 0) newStock = 0
            break
          case 'set':
            newStock = quantity
            break
        }
        
        // Mettre à jour via l'API
        await api.put(`/aliments/${currentAliment.value.idAliment}/stock`, null, {
          params: {
            quantite: newStock
          }
        })
        
        await loadData()
        closeStockModal()
        alert('Stock mis à jour avec succès')
      } catch (error) {
        console.error('Erreur mise à jour stock:', error)
        alert('Erreur lors de la mise à jour du stock: ' + (error.response?.data?.message || error.message))
      } finally {
        loading.value = false
      }
    }

    const closeStockModal = () => {
      showStockManagementModal.value = false
      currentAliment.value = {}
      stockQuantity.value = 0
    }

    // Analyse détaillée
    const showAnalytics = (aliment) => {
      currentAliment.value = { 
        ...aliment,
        lipidesParKgAliment: aliment.lipidesParKgAliment || 5,
        vitaminesParKgAliment: aliment.vitaminesParKgAliment || 0.5
      }
      
      // Préparer l'analyse détaillée
      const qualityAnalysis = calculService.evaluerQualiteAliment({
        proteinesParKg: aliment.proteinesParKgAliment,
        glucidesParKg: aliment.glucidesParKgAliment,
        lipidesParKg: aliment.lipidesParKgAliment || 5,
        vitaminesParKg: aliment.vitaminesParKgAliment || 0.5
      })
      
      const costAnalysis = calculService.calculerCoutEfficacite({
        ...aliment,
        lipidesParKg: aliment.lipidesParKgAliment || 5,
        vitaminesParKg: aliment.vitaminesParKgAliment || 0.5
      })
      
      // Calcul des ratios
      const total = aliment.proteinesParKgAliment + aliment.glucidesParKgAliment + (aliment.lipidesParKgAliment || 5)
      const ratioProteines = total > 0 ? (aliment.proteinesParKgAliment / total) * 100 : 0
      const ratioGlucides = total > 0 ? (aliment.glucidesParKgAliment / total) * 100 : 0
      const ratioLipides = total > 0 ? ((aliment.lipidesParKgAliment || 5) / total) * 100 : 0
      
      // Recommandations
      const recommendations = []
      if (ratioProteines < 30) {
        recommendations.push({
          icon: '⚠️',
          text: 'Taux de protéines faible (< 30%). Ajouter plus de protéines.',
          priority: 'high'
        })
      }
      if (ratioLipides < 10) {
        recommendations.push({
          icon: '⚡',
          text: 'Lipides insuffisants (< 10%). Important pour l\'énergie.',
          priority: 'medium'
        })
      }
      if (aliment.vitaminesParKgAliment < 0.2) {
        recommendations.push({
          icon: '💊',
          text: 'Faible teneur en vitamines. Considérer une supplémentation.',
          priority: 'medium'
        })
      }
      if (qualityAnalysis.score >= 80) {
        recommendations.push({
          icon: '✅',
          text: 'Excellent équilibre nutritionnel !',
          priority: 'info'
        })
      }
      
      // Meilleur usage
      let bestUsage = ''
      if (ratioProteines > 40) {
        bestUsage = 'Idéal pour la croissance rapide des juvéniles'
      } else if (ratioLipides > 15) {
        bestUsage = 'Parfait pour l\'engraissement avant la vente'
      } else if (aliment.vitaminesParKgAliment > 1) {
        bestUsage = 'Excellent pour renforcer le système immunitaire'
      } else {
        bestUsage = 'Aliment d\'entretien général pour poissons adultes'
      }
      
      detailedAnalysis.value = {
        score: qualityAnalysis.score,
        scores: qualityAnalysis.details,
        costPerGain: costAnalysis.coutParGain,
        gainPerKg: costAnalysis.gainParKg,
        efficiency: costAnalysis.efficacite,
        rentability: costAnalysis.rentabilite,
        rentabilityClass: getRentabilityClassFromText(costAnalysis.rentabilite),
        composition: {
          proteines: aliment.proteinesParKgAliment,
          glucides: aliment.glucidesParKgAliment,
          lipides: aliment.lipidesParKgAliment || 5,
          vitamines: aliment.vitaminesParKgAliment || 0.5
        },
        ratios: {
          proteines: Math.round(ratioProteines),
          glucides: Math.round(ratioGlucides),
          lipides: Math.round(ratioLipides)
        },
        recommendations,
        bestUsage
      }
      
      showAnalyticsModal.value = true
    }

    const closeAnalyticsModal = () => {
      showAnalyticsModal.value = false
      detailedAnalysis.value = {}
    }

    const exportAnalysis = () => {
      const data = {
        aliment: currentAliment.value.nomAliment,
        date: new Date().toISOString(),
        analyse: detailedAnalysis.value
      }
      
      const blob = new Blob([JSON.stringify(data, null, 2)], { type: 'application/json' })
      const link = document.createElement('a')
      link.href = URL.createObjectURL(blob)
      link.download = `analyse_${currentAliment.value.nomAliment.replace(/[^a-z0-9]/gi, '_')}_${new Date().toISOString().split('T')[0]}.json`
      link.click()
    }

    // Supprimer un aliment
    const deleteAliment = (id, nom) => {
      alimentToDelete.value = { id, nom }
      showDeleteModal.value = true
    }

    const confirmDelete = async () => {
      try {
        await api.delete(`/aliments/${alimentToDelete.value.id}`)
        await loadData()
        alert('Aliment supprimé avec succès')
      } catch (error) {
        alert('Erreur lors de la suppression: ' + (error.response?.data?.message || error.message))
      } finally {
        cancelDelete()
      }
    }

    const cancelDelete = () => {
      showDeleteModal.value = false
      alimentToDelete.value = null
    }

    // Exporter en CSV
    const exportToCSV = () => {
      const headers = ['Nom', 'Stock (kg)', 'Protéines (g/kg)', 'Glucides (g/kg)', 'Lipides (g/kg)', 'Vitamines (g/kg)',
                      'Prix/kg (MGA)', 'Qualité', 'Rentabilité', 'Seuil min (kg)', 'Statut', 'Valeur stock (MGA)']
      
      const csvData = filteredAliments.value.map(aliment => {
        const quality = getQualityText(aliment)
        const rentability = getRentabilityText(aliment)
        
        return [
          aliment.nomAliment,
          formatStock(aliment.stockAliment),
          formatNutrient(aliment.proteinesParKgAliment),
          formatNutrient(aliment.glucidesParKgAliment),
          formatNutrient(aliment.lipidesParKgAliment || 0),
          formatNutrient(aliment.vitaminesParKgAliment || 0, 2),
          aliment.prixKgAliment,
          quality,
          rentability,
          formatStock(aliment.seuilMinimumAliment),
          getStatusText(aliment),
          calculateValeurStock(aliment)
        ]
      })

      const csvContent = [
        headers.join(','),
        ...csvData.map(row => row.join(','))
      ].join('\n')

      const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' })
      const link = document.createElement('a')
      link.href = URL.createObjectURL(blob)
      link.download = `aliments_complets_${new Date().toISOString().split('T')[0]}.csv`
      link.click()
    }

    // Rapport nutritionnel
    const exportNutritionReport = () => {
      const reportData = aliments.value.map(aliment => {
        const qualityAnalysis = calculService.evaluerQualiteAliment({
          proteinesParKg: aliment.proteinesParKgAliment,
          glucidesParKg: aliment.glucidesParKgAliment,
          lipidesParKg: aliment.lipidesParKgAliment || 5,
          vitaminesParKg: aliment.vitaminesParKgAliment || 0.5
        })
        
        const costAnalysis = calculService.calculerCoutEfficacite({
          ...aliment,
          lipidesParKg: aliment.lipidesParKgAliment || 5,
          vitaminesParKg: aliment.vitaminesParKgAliment || 0.5
        })
        
        return {
          nom: aliment.nomAliment,
          stock: formatStock(aliment.stockAliment),
          composition: {
            proteines: aliment.proteinesParKgAliment,
            glucides: aliment.glucidesParKgAliment,
            lipides: aliment.lipidesParKgAliment || 5,
            vitamines: aliment.vitaminesParKgAliment || 0.5
          },
          analyse: {
            qualite: qualityAnalysis.qualite,
            score: qualityAnalysis.score,
            coutParGain: costAnalysis.coutParGain,
            efficacite: costAnalysis.efficacite,
            rentabilite: costAnalysis.rentabilite
          },
          statut: getStatusText(aliment)
        }
      })
      
      const report = {
        titre: 'Rapport nutritionnel des aliments',
        date: new Date().toISOString(),
        statistiques: {
          totalAliments: aliments.value.length,
          qualiteMoyenne: qualiteMoyenne.value,
          valeurStock: valeurStock.value,
          stocksBas: stocksBasCount.value
        },
        aliments: reportData
      }
      
      const blob = new Blob([JSON.stringify(report, null, 2)], { type: 'application/json' })
      const link = document.createElement('a')
      link.href = URL.createObjectURL(blob)
      link.download = `rapport_nutritionnel_${new Date().toISOString().split('T')[0]}.json`
      link.click()
    }

    // Générer commande pour stocks bas
    const reorderAllLowStock = () => {
      const lowStockAliments = aliments.value.filter(isStockLow)
      
      if (lowStockAliments.length === 0) {
        alert('Aucun aliment en stock bas')
        return
      }

      const commande = lowStockAliments.map(aliment => {
        const seuil = parseFloat(aliment.seuilMinimumAliment || 10)
        const stock = parseFloat(aliment.stockAliment || 0)
        const quantiteACommander = Math.max(seuil * 2 - stock, 10) // Commander au moins 10kg
        const prix = parseFloat(aliment.prixKgAliment || 0)
        const montant = quantiteACommander * prix
        
        return {
          nom: aliment.nomAliment,
          stockActuel: stock,
          seuil: seuil,
          quantiteACommander: quantiteACommander.toFixed(2),
          montant: montant.toFixed(0)
        }
      })

      // Afficher la commande
      let message = 'Commande suggérée pour stocks bas:\n\n'
      let totalMontant = 0
      
      commande.forEach(item => {
        message += `• ${item.nom}: ${item.quantiteACommander}kg (${item.montant} MGA)\n`
        totalMontant += parseFloat(item.montant)
      })
      
      message += `\nTotal: ${totalMontant.toFixed(0)} MGA`
      
      alert(message)
    }

    // Watcher pour l'analyse en temps réel
    watch(() => currentAliment.value, () => {
      // L'analyse se met à jour automatiquement via le computed
    }, { deep: true })

    // Initialisation
    onMounted(() => {
      loadData()
    })

    return {
      // Données
      aliments,
      filteredAliments,
      paginatedAliments,
      searchQuery,
      filterStatus,
      filterStock,
      filterQualite,
      currentPage,
      itemsPerPage,
      totalPages,
      loading,
      showAlimentModal,
      showStockManagementModal,
      showDeleteModal,
      showAnalyticsModal,
      showAnalysisPreview,
      isEditing,
      currentAliment,
      alimentToDelete,
      stockAction,
      stockQuantity,
      detailedAnalysis,
      analysis,
      
      // Computed
      totalStock,
      stocksBasCount,
      valeurStock,
      qualiteMoyenne,
      
      // Méthodes
      loadData,
      filterAliments,
      sortBy,
      formatStock,
      formatNutrient,
      formatPrice,
      getRowClass,
      getStockClass,
      getStatusClass,
      getStatusText,
      getQualityClass,
      getQualityText,
      getRentabilityClass,
      getRentabilityText,
      getStockPercentage,
      isStockLow,
      calculateValeurStock,
      prevPage,
      nextPage,
      resetPagination,
      refreshData,
      showAddModal,
      editAliment,
      toggleAnalysisPreview,
      saveAliment,
      closeModal,
      toggleActive,
      showStockModal,
      calculateNewStock,
      updateStock,
      closeStockModal,
      showAnalytics,
      closeAnalyticsModal,
      exportAnalysis,
      deleteAliment,
      confirmDelete,
      cancelDelete,
      exportToCSV,
      exportNutritionReport,
      reorderAllLowStock
    }
  }
}
</script>

<style scoped>
@import '../assets/styles/aliment.css';

/* Styles supplémentaires pour les nouvelles fonctionnalités */

.nutrient-value.protein {
  color: #3b82f6;
  font-weight: 600;
}

.nutrient-value.carbs {
  color: #10b981;
  font-weight: 600;
}

.nutrient-value.lipid {
  color: #f59e0b;
  font-weight: 600;
}

.nutrient-value.vitamin {
  color: #8b5cf6;
  font-weight: 600;
}

.quality-indicator {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 4px 8px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 600;
  width: fit-content;
}

.quality-indicator.excellent {
  background-color: #dcfce7;
  color: #166534;
}

.quality-indicator.good {
  background-color: #fef3c7;
  color: #92400e;
}

.quality-indicator.medium {
  background-color: #fef3c7;
  color: #92400e;
}

.quality-indicator.low {
  background-color: #fee2e2;
  color: #991b1b;
}

.quality-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
}

.quality-indicator.excellent .quality-dot {
  background-color: #16a34a;
}

.quality-indicator.good .quality-dot {
  background-color: #f59e0b;
}

.quality-indicator.medium .quality-dot {
  background-color: #f59e0b;
}

.quality-indicator.low .quality-dot {
  background-color: #ef4444;
}

.rentability-indicator {
  padding: 4px 8px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 600;
  text-align: center;
  width: fit-content;
}

.rentability-indicator.excellent {
  background-color: #dcfce7;
  color: #166534;
}

.rentability-indicator.good {
  background-color: #fef3c7;
  color: #92400e;
}

.rentability-indicator.medium {
  background-color: #fef3c7;
  color: #92400e;
}

.rentability-indicator.low {
  background-color: #fee2e2;
  color: #991b1b;
}

.rentability-indicator.very-low {
  background-color: #fee2e2;
  color: #991b1b;
}

.btn-action.analytics {
  background-color: #8b5cf6;
  color: white;
}

.btn-action.analytics:hover {
  background-color: #7c3aed;
}

.form-preview {
  margin-top: 20px;
  padding: 15px;
  background-color: #f8fafc;
  border-radius: 8px;
  border: 1px solid #e2e8f0;
}

.analysis-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
  margin-bottom: 15px;
}

.analysis-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 12px;
  background-color: white;
  border-radius: 6px;
  border: 1px solid #e2e8f0;
}

.analysis-label {
  font-size: 14px;
  color: #64748b;
}

.analysis-value {
  font-weight: 600;
  font-size: 14px;
}

.analysis-value.excellent {
  color: #16a34a;
}

.analysis-value.good {
  color: #f59e0b;
}

.analysis-value.medium {
  color: #f59e0b;
}

.analysis-value.low {
  color: #ef4444;
}

.analysis-value.neutral {
  color: #64748b;
}

.nutrient-ratios {
  margin-top: 15px;
}

.ratios-grid {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin: 10px 0;
}

.ratio-item {
  display: flex;
  align-items: center;
  gap: 10px;
}

.ratio-bar {
  flex: 1;
  height: 8px;
  background-color: #e2e8f0;
  border-radius: 4px;
  overflow: hidden;
}

.ratio-fill {
  height: 100%;
}

.ratio-fill.protein {
  background-color: #3b82f6;
}

.ratio-fill.carbs {
  background-color: #10b981;
}

.ratio-fill.lipid {
  background-color: #f59e0b;
}

.ratio-label {
  font-size: 12px;
  color: #64748b;
  min-width: 100px;
}

.ratio-recommendation {
  font-size: 12px;
  color: #64748b;
  font-style: italic;
  margin-top: 5px;
}

.btn-toggle-analysis {
  width: 100%;
  padding: 8px;
  background-color: #f1f5f9;
  border: 1px dashed #cbd5e1;
  border-radius: 6px;
  color: #64748b;
  cursor: pointer;
  margin-top: 10px;
  transition: all 0.2s;
}

.btn-toggle-analysis:hover {
  background-color: #e2e8f0;
}

/* Modal d'analyse détaillée */
.modal.large {
  max-width: 800px;
}

.analytics-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
  margin-bottom: 20px;
}

.analytics-card {
  background-color: white;
  padding: 15px;
  border-radius: 8px;
  border: 1px solid #e2e8f0;
}

.analytics-card.full-width {
  grid-column: 1 / -1;
}

.analytics-card h4 {
  margin-top: 0;
  margin-bottom: 15px;
  color: #334155;
}

.score-meter {
  position: relative;
  height: 20px;
  background-color: #e2e8f0;
  border-radius: 10px;
  margin: 15px 0;
  overflow: hidden;
}

.meter-fill {
  position: absolute;
  top: 0;
  left: 0;
  height: 100%;
  background: linear-gradient(90deg, #ef4444, #f59e0b, #10b981);
  transition: width 0.5s ease;
}

.score-value {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-weight: 600;
  text-shadow: 0 1px 2px rgba(0,0,0,0.2);
}

.score-details {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.score-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 6px 0;
  border-bottom: 1px solid #f1f5f9;
}

.efficiency-stats {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.stat {
  display: flex;
  flex-direction: column;
  gap: 4px;
  padding: 12px;
  background-color: #f8fafc;
  border-radius: 6px;
}

.stat-label {
  font-size: 12px;
  color: #64748b;
}

.stat-value {
  font-weight: 600;
  font-size: 16px;
  color: #334155;
}

.composition-chart {
  margin-top: 15px;
}

.chart-bars {
  display: flex;
  justify-content: space-around;
  align-items: flex-end;
  height: 200px;
  padding: 0 20px;
}

.chart-bar-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  width: 60px;
}

.chart-bar {
  width: 40px;
  border-radius: 4px 4px 0 0;
  position: relative;
  transition: height 0.5s ease;
}

.chart-bar.protein {
  background-color: #3b82f6;
}

.chart-bar.carbs {
  background-color: #10b981;
}

.chart-bar.lipid {
  background-color: #f59e0b;
}

.chart-bar.vitamin {
  background-color: #8b5cf6;
}

.bar-label {
  position: absolute;
  top: -25px;
  left: 0;
  right: 0;
  text-align: center;
  font-size: 12px;
  color: #64748b;
  font-weight: 600;
}

.bar-title {
  font-size: 12px;
  color: #64748b;
  text-align: center;
}

.recommendations {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-bottom: 15px;
}

.recommendation {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px;
  border-radius: 6px;
  background-color: #f8fafc;
}

.recommendation.warning {
  background-color: #fef3c7;
  border-left: 4px solid #f59e0b;
}

.recommendation.info {
  background-color: #dbeafe;
  border-left: 4px solid #3b82f6;
}

.rec-icon {
  font-size: 18px;
}

.rec-text {
  flex: 1;
  font-size: 14px;
}

.best-usage {
  padding: 12px;
  background-color: #dcfce7;
  border-radius: 6px;
  border-left: 4px solid #16a34a;
}

.best-usage h5 {
  margin-top: 0;
  margin-bottom: 8px;
  color: #166534;
}

.best-usage p {
  margin: 0;
  color: #166534;
  font-size: 14px;
}

.hint {
  font-size: 12px;
  color: #64748b;
  margin-top: 4px;
}
</style>

