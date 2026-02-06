<template>
  <div class="benefices">
    <!-- Header -->
    <div class="page-header">
      <div class="header-left">
        <router-link to="/" class="btn-back">
          ← Retour au dashboard
        </router-link>
        <h1>💰 Analyse des Bénéfices</h1>
        <p class="subtitle">Suivi financier et analyse de rentabilité</p>
      </div>
      <div class="header-right">
        <button @click="refreshData" class="btn-refresh" :disabled="loading">
          🔄 {{ loading ? 'Chargement...' : 'Actualiser' }}
        </button>
        <button @click="exportFinancialReport" class="btn-export">
          📊 Exporter rapport
        </button>
      </div>
    </div>

    <!-- Période de filtrage -->
    <div class="period-filter">
      <div class="filter-group">
        <label for="period">Période :</label>
        <select v-model="selectedPeriod" @change="loadFinancialData">
          <option value="today">Aujourd'hui</option>
          <option value="week">Cette semaine</option>
          <option value="month">Ce mois</option>
          <option value="quarter">Ce trimestre</option>
          <option value="year">Cette année</option>
          <option value="all">Tout</option>
          <option value="custom">Personnalisée</option>
        </select>
      </div>

      <div v-if="selectedPeriod === 'custom'" class="custom-period">
        <input type="date" v-model="customStartDate" @change="loadFinancialData">
        <span>à</span>
        <input type="date" v-model="customEndDate" @change="loadFinancialData">
      </div>

      <div class="view-toggle">
        <button @click="viewMode = 'summary'" :class="{ active: viewMode === 'summary' }">
          📋 Résumé
        </button>
        <button @click="viewMode = 'details'" :class="{ active: viewMode === 'details' }">
          📊 Détails
        </button>
        <button @click="viewMode = 'charts'" :class="{ active: viewMode === 'charts' }">
          📈 Graphiques
        </button>
      </div>
    </div>

    <!-- Vue Résumé -->
    <div v-if="viewMode === 'summary'" class="summary-view">
      <!-- Cartes principales -->
      <div class="financial-overview">
        <div class="overview-card total-revenue" @click="showDetails('revenue')">
          <div class="card-icon">💰</div>
          <div class="card-content">
            <h3>Chiffre d'affaires</h3>
            <p class="card-value">{{ formatCurrency(stats.totalRevenue) }}</p>
            <div class="card-trend" :class="getTrendClass(stats.revenueTrend)">
              <span v-if="stats.revenueTrend > 0">↗</span>
              <span v-else-if="stats.revenueTrend < 0">↘</span>
              <span v-else>→</span>
              {{ Math.abs(stats.revenueTrend) }}%
            </div>
          </div>
        </div>

        <div class="overview-card total-cost" @click="showDetails('cost')">
          <div class="card-icon">📉</div>
          <div class="card-content">
            <h3>Coûts totaux</h3>
            <p class="card-value">{{ formatCurrency(stats.totalCost) }}</p>
            <div class="card-breakdown">
              <span>Aliments: {{ formatCurrency(stats.foodCost) }}</span>
              <span>Poissons: {{ formatCurrency(stats.fishCost) }}</span>
            </div>
          </div>
        </div>

        <div class="overview-card net-profit" @click="showDetails('profit')">
          <div class="card-icon">📈</div>
          <div class="card-content">
            <h3>Bénéfice net</h3>
            <p class="card-value" :class="getProfitClass(stats.netProfit)">
              {{ formatCurrency(stats.netProfit) }}
            </p>
            <div class="profit-margin">
              <span>Marge: {{ stats.profitMargin }}%</span>
              <span>ROI: {{ stats.roi }}%</span>
            </div>
          </div>
        </div>

        <div class="overview-card potential-profit" @click="showDetails('potential')">
          <div class="card-icon">🎯</div>
          <div class="card-content">
            <h3>Bénéfice potentiel</h3>
            <p class="card-value">{{ formatCurrency(stats.potentialProfit) }}</p>
            <div class="potential-details">
              <span>{{ stats.fishReadyToSell }} poissons prêts</span>
              <span>{{ formatCurrency(stats.avgPricePerFish) }} en moyenne</span>
            </div>
          </div>
        </div>
      </div>

      <!-- Statistiques détaillées -->
      <div class="detailed-stats">
        <div class="stat-card">
          <h3>📦 Ventes</h3>
          <div class="stat-list">
            <div class="stat-item">
              <span class="label">Poissons vendus</span>
              <span class="value">{{ stats.fishSold }}</span>
            </div>
            <div class="stat-item">
              <span class="label">Prix moyen de vente</span>
              <span class="value">{{ formatCurrency(stats.avgSalePrice) }}</span>
            </div>
            <div class="stat-item">
              <span class="label">Vente la plus élevée</span>
              <span class="value">{{ formatCurrency(stats.highestSale) }}</span>
            </div>
            <div class="stat-item">
              <span class="label">Ventes journalières moy.</span>
              <span class="value">{{ stats.avgDailySales }}</span>
            </div>
          </div>
        </div>

        <div class="stat-card">
          <h3>🛒 Achats</h3>
          <div class="stat-list">
            <div class="stat-item">
              <span class="label">Poissons achetés</span>
              <span class="value">{{ stats.fishBought }}</span>
            </div>
            <div class="stat-item">
              <span class="label">Coût moyen d'achat</span>
              <span class="value">{{ formatCurrency(stats.avgPurchaseCost) }}</span>
            </div>
            <div class="stat-item">
              <span class="label">Dépenses alimentaires</span>
              <span class="value">{{ formatCurrency(stats.foodExpenses) }}</span>
            </div>
            <div class="stat-item">
              <span class="label">Autres dépenses</span>
              <span class="value">{{ formatCurrency(stats.otherExpenses) }}</span>
            </div>
          </div>
        </div>

        <div class="stat-card">
          <h3>📊 Ratios</h3>
          <div class="stat-list">
            <div class="stat-item">
              <span class="label">Marge brute</span>
              <span class="value" :class="getRatioClass(stats.grossMargin)">
                {{ stats.grossMargin }}%
              </span>
            </div>
            <div class="stat-item">
              <span class="label">Marge nette</span>
              <span class="value" :class="getRatioClass(stats.netMargin)">
                {{ stats.netMargin }}%
              </span>
            </div>
            <div class="stat-item">
              <span class="label">ROI (Retour sur invest.)</span>
              <span class="value" :class="getRatioClass(stats.roi)">
                {{ stats.roi }}%
              </span>
            </div>
            <div class="stat-item">
              <span class="label">Taux croissance CA</span>
              <span class="value" :class="getTrendClass(stats.revenueGrowth)">
                {{ stats.revenueGrowth }}%
              </span>
            </div>
          </div>
        </div>
      </div>

      <!-- Projections -->
      <div class="projections">
        <h3>🎯 Projections</h3>
        <div class="projection-cards">
          <div class="projection-card">
            <h4>30 jours</h4>
            <p class="projection-value">{{ formatCurrency(projections.monthly) }}</p>
            <div class="projection-details">
              <span>+{{ projections.monthlyGrowth }}% vs période</span>
            </div>
          </div>
          <div class="projection-card">
            <h4>3 mois</h4>
            <p class="projection-value">{{ formatCurrency(projections.quarterly) }}</p>
            <div class="projection-details">
              <span>+{{ projections.quarterlyGrowth }}%</span>
            </div>
          </div>
          <div class="projection-card">
            <h4>1 an</h4>
            <p class="projection-value">{{ formatCurrency(projections.yearly) }}</p>
            <div class="projection-details">
              <span>Objectif: {{ formatCurrency(stats.revenueTarget) }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Vue Détails -->
    <div v-if="viewMode === 'details'" class="details-view">
      <div class="transactions-section">
        <h3>📋 Transactions récentes</h3>
        <div class="table-responsive">
          <table class="transactions-table">
            <thead>
              <tr>
                <th>Date</th>
                <th>Type</th>
                <th>Description</th>
                <th>Montant</th>
                <th>Poisson</th>
                <th>Statut</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="transaction in recentTransactions" :key="transaction.id">
                <td>{{ formatDate(transaction.date) }}</td>
                <td>
                  <span class="transaction-type" :class="getTransactionTypeClass(transaction.type)">
                    {{ getTransactionTypeLabel(transaction.type) }}
                  </span>
                </td>
                <td>{{ transaction.description }}</td>
                <td :class="getAmountClass(transaction.amount, transaction.type)">
                  {{ formatCurrency(transaction.amount) }}
                </td>
                <td>
                  <span v-if="transaction.fishName" class="fish-link">
                    {{ transaction.fishName }}
                  </span>
                  <span v-else class="na">N/A</span>
                </td>
                <td>
                  <span class="status-badge" :class="getStatusClass(transaction.status)">
                    {{ transaction.status }}
                  </span>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
        <button @click="loadMoreTransactions" class="btn-load-more" v-if="hasMoreTransactions">
          Charger plus
        </button>
      </div>

      <div class="expenses-breakdown">
        <h3>🛒 Détail des dépenses</h3>
        <div class="expenses-chart">
          <canvas ref="expensesChartRef"></canvas>
        </div>
        <div class="expenses-list">
          <div v-for="expense in expensesBreakdown" :key="expense.category" class="expense-item">
            <div class="expense-category">
              <span class="category-color" :style="{ backgroundColor: expense.color }"></span>
              <span class="category-name">{{ expense.category }}</span>
            </div>
            <div class="expense-amount">{{ formatCurrency(expense.amount) }}</div>
            <div class="expense-percentage">{{ expense.percentage }}%</div>
          </div>
        </div>
      </div>

      <div class="profit-analysis">
        <h3>📈 Analyse par race</h3>
        <div class="analysis-table">
          <table>
            <thead>
              <tr>
                <th>Race</th>
                <th>Ventes</th>
                <th>Coûts</th>
                <th>Bénéfice</th>
                <th>Marge</th>
                <th>ROI</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="race in raceAnalysis" :key="race.name">
                <td>{{ race.name }}</td>
                <td>{{ formatCurrency(race.sales) }}</td>
                <td>{{ formatCurrency(race.costs) }}</td>
                <td :class="getProfitClass(race.profit)">{{ formatCurrency(race.profit) }}</td>
                <td :class="getRatioClass(race.margin)">{{ race.margin }}%</td>
                <td :class="getRatioClass(race.roi)">{{ race.roi }}%</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>

    <!-- Vue Graphiques -->
    <div v-if="viewMode === 'charts'" class="charts-view">
      <div class="chart-row">
        <div class="chart-card">
          <h3>📈 Évolution des revenus</h3>
          <div class="chart-container">
            <canvas ref="revenueChartRef"></canvas>
          </div>
        </div>
        <div class="chart-card">
          <h3>📊 Répartition des revenus</h3>
          <div class="chart-container">
            <canvas ref="revenueDistributionChartRef"></canvas>
          </div>
        </div>
      </div>

      <div class="chart-row">
        <div class="chart-card">
          <h3>📉 Évolution des coûts</h3>
          <div class="chart-container">
            <canvas ref="costsChartRef"></canvas>
          </div>
        </div>
        <div class="chart-card">
          <h3>📊 Répartition des coûts</h3>
          <div class="chart-container">
            <canvas ref="costsDistributionChartRef"></canvas>
          </div>
        </div>
      </div>

      <div class="chart-row">
        <div class="chart-card">
          <h3>💰 Évolution des bénéfices</h3>
          <div class="chart-container">
            <canvas ref="profitChartRef"></canvas>
          </div>
        </div>
        <div class="chart-card">
          <h3>🎯 Objectifs vs Réalisation</h3>
          <div class="chart-container">
            <canvas ref="goalsChartRef"></canvas>
          </div>
        </div>
      </div>
    </div>

    <!-- Actions rapides -->
    <div class="quick-actions">
      <h3>⚡ Actions rapides</h3>
      <div class="actions-grid">
        <button @click="registerSale" class="action-btn primary">
          <span class="action-icon">💰</span>
          <span>Enregistrer une vente</span>
        </button>
        <button @click="registerPurchase" class="action-btn warning">
          <span class="action-icon">🛒</span>
          <span>Enregistrer un achat</span>
        </button>
        <button @click="generateFinancialReport" class="action-btn info">
          <span class="action-icon">📊</span>
          <span>Générer rapport</span>
        </button>
        <button @click="setFinancialGoals" class="action-btn success">
          <span class="action-icon">🎯</span>
          <span>Définir objectifs</span>
        </button>
      </div>
    </div>

    <!-- Modale d'ajout de transaction -->
    <div v-if="showTransactionModal" class="modal-overlay">
      <div class="modal">
        <div class="modal-header">
          <h3>{{ modalMode === 'sale' ? '💰 Enregistrer une vente' : '🛒 Enregistrer un achat' }}</h3>
          <button @click="closeModal" class="modal-close">×</button>
        </div>
        <div class="modal-body">
          <div class="form-group">
            <label>Type</label>
            <select v-model="newTransaction.type">
              <option v-if="modalMode === 'sale'" value="VENTE_POISSON">Vente de poisson</option>
              <option v-if="modalMode === 'sale'" value="AUTRE_RECETTE">Autre recette</option>
              <option v-if="modalMode === 'purchase'" value="ACHAT_POISSON">Achat de poisson</option>
              <option v-if="modalMode === 'purchase'" value="ACHAT_ALIMENT">Achat d'aliment</option>
              <option v-if="modalMode === 'purchase'" value="FRAIS_ENTRETIEN">Frais d'entretien</option>
            </select>
          </div>

          <div class="form-group">
            <label>Montant (MGA)</label>
            <input type="number" v-model="newTransaction.amount" min="0" step="0.01">
          </div>

          <div class="form-group">
            <label>Description</label>
            <textarea v-model="newTransaction.description" rows="3"></textarea>
          </div>

          <div v-if="modalMode === 'sale'" class="form-group">
            <label>Poisson vendu (optionnel)</label>
            <select v-model="newTransaction.fishId">
              <option value="">Sélectionner un poisson</option>
              <option v-for="fish in fishReadyToSell" :key="fish.id" :value="fish.id">
                {{ fish.name }} - {{ formatCurrency(fish.price) }}
              </option>
            </select>
          </div>

          <div v-if="modalMode === 'purchase' && newTransaction.type === 'ACHAT_ALIMENT'" class="form-group">
            <label>Aliment (optionnel)</label>
            <select v-model="newTransaction.foodId">
              <option value="">Sélectionner un aliment</option>
              <option v-for="food in foodItems" :key="food.id" :value="food.id">
                {{ food.name }} - Stock: {{ food.stock }}kg
              </option>
            </select>
          </div>
        </div>
        <div class="modal-footer">
          <button @click="closeModal" class="btn-cancel">Annuler</button>
          <button @click="saveTransaction" class="btn-confirm">
            Enregistrer
          </button>
        </div>
      </div>
    </div>

    <!-- Modale objectifs financiers -->
    <div v-if="showGoalsModal" class="modal-overlay">
      <div class="modal">
        <div class="modal-header">
          <h3>🎯 Objectifs financiers</h3>
          <button @click="closeGoalsModal" class="modal-close">×</button>
        </div>
        <div class="modal-body">
          <div class="form-group">
            <label>Objectif de revenus mensuel (MGA)</label>
            <input type="number" v-model="financialGoals.monthlyRevenue" min="0">
          </div>
          <div class="form-group">
            <label>Objectif de marge bénéficiaire (%)</label>
            <input type="number" v-model="financialGoals.profitMargin" min="0" max="100">
          </div>
          <div class="form-group">
            <label>Réduction des coûts cible (%)</label>
            <input type="number" v-model="financialGoals.costReduction" min="0" max="100">
          </div>
          <div class="progress-summary">
            <h4>Progression actuelle</h4>
            <div class="progress-item">
              <span>Revenus mensuels</span>
              <div class="progress-bar">
                <div class="progress-fill" :style="{ width: revenueProgress + '%' }"></div>
                <span class="progress-text">{{ revenueProgress }}%</span>
              </div>
            </div>
            <div class="progress-item">
              <span>Marge bénéficiaire</span>
              <div class="progress-bar">
                <div class="progress-fill" :style="{ width: marginProgress + '%' }"></div>
                <span class="progress-text">{{ marginProgress }}%</span>
              </div>
            </div>
          </div>
        </div>
        <div class="modal-footer">
          <button @click="closeGoalsModal" class="btn-cancel">Annuler</button>
          <button @click="saveFinancialGoals" class="btn-confirm">
            Enregistrer
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, computed, onMounted, watch, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { Chart, registerables } from 'chart.js'
import poissonService from '../services/poissonService'
import nourrissageService from '../services/nourrissageService'
import alimentService from '../services/alimentService'

Chart.register(...registerables)

export default {
  name: 'Benefices',
  setup() {
    const router = useRouter()
    
    // États
    const loading = ref(false)
    const selectedPeriod = ref('month')
    const customStartDate = ref('')
    const customEndDate = ref('')
    const viewMode = ref('summary')
    const showTransactionModal = ref(false)
    const showGoalsModal = ref(false)
    const modalMode = ref('sale') // 'sale' or 'purchase'
    const hasMoreTransactions = ref(true)
    
    // Données
    const stats = ref({
      totalRevenue: 0,
      totalCost: 0,
      netProfit: 0,
      profitMargin: 0,
      roi: 0,
      revenueTrend: 0,
      fishSold: 0,
      fishBought: 0,
      fishReadyToSell: 0,
      avgSalePrice: 0,
      avgPurchaseCost: 0,
      foodCost: 0,
      fishCost: 0,
      foodExpenses: 0,
      otherExpenses: 0,
      highestSale: 0,
      avgDailySales: 0,
      grossMargin: 0,
      netMargin: 0,
      revenueGrowth: 0,
      potentialProfit: 0,
      avgPricePerFish: 0,
      revenueTarget: 10000000
    })
    
    const recentTransactions = ref([])
    const expensesBreakdown = ref([])
    const raceAnalysis = ref([])
    const projections = ref({
      monthly: 0,
      monthlyGrowth: 0,
      quarterly: 0,
      quarterlyGrowth: 0,
      yearly: 0
    })
    
    const newTransaction = ref({
      type: '',
      amount: 0,
      description: '',
      fishId: '',
      foodId: ''
    })
    
    const financialGoals = ref({
      monthlyRevenue: 5000000,
      profitMargin: 30,
      costReduction: 10
    })
    
    const fishReadyToSell = ref([])
    const foodItems = ref([])
    
    // Références graphiques - utiliser des refs pour chaque canvas
    const revenueChart = ref(null)
    const revenueDistributionChart = ref(null)
    const costsChart = ref(null)
    const costsDistributionChart = ref(null)
    const profitChart = ref(null)
    const goalsChart = ref(null)
    const expensesChart = ref(null)
    
    const revenueChartRef = ref(null)
    const revenueDistributionChartRef = ref(null)
    const costsChartRef = ref(null)
    const costsDistributionChartRef = ref(null)
    const profitChartRef = ref(null)
    const goalsChartRef = ref(null)
    const expensesChartRef = ref(null)

    // Computed
    const revenueProgress = computed(() => {
      if (financialGoals.value.monthlyRevenue === 0) return 0
      const progress = (stats.value.totalRevenue / financialGoals.value.monthlyRevenue) * 100
      return Math.min(Math.round(progress), 100)
    })
    
    const marginProgress = computed(() => {
      if (financialGoals.value.profitMargin === 0) return 0
      const progress = (stats.value.profitMargin / financialGoals.value.profitMargin) * 100
      return Math.min(Math.round(progress), 100)
    })

    // Méthodes
    const formatCurrency = (amount) => {
      if (!amount && amount !== 0) return '0 MGA'
      return new Intl.NumberFormat('fr-MG', {
        style: 'currency',
        currency: 'MGA',
        minimumFractionDigits: 0,
        maximumFractionDigits: 0
      }).format(amount)
    }

    const formatDate = (dateString) => {
      if (!dateString) return 'N/A'
      return new Date(dateString).toLocaleDateString('fr-FR')
    }

    const getTrendClass = (trend) => {
      if (trend > 0) return 'trend-positive'
      if (trend < 0) return 'trend-negative'
      return 'trend-neutral'
    }

    const getProfitClass = (profit) => {
      if (profit > 0) return 'profit-positive'
      if (profit < 0) return 'profit-negative'
      return 'profit-neutral'
    }

    const getRatioClass = (ratio) => {
      if (ratio >= 30) return 'ratio-excellent'
      if (ratio >= 20) return 'ratio-good'
      if (ratio >= 10) return 'ratio-average'
      return 'ratio-poor'
    }

    const getTransactionTypeClass = (type) => {
      const typeMap = {
        'VENTE_POISSON': 'sale',
        'AUTRE_RECETTE': 'revenue',
        'ACHAT_POISSON': 'purchase',
        'ACHAT_ALIMENT': 'food',
        'FRAIS_ENTRETIEN': 'expense'
      }
      return typeMap[type] || 'other'
    }

    const getTransactionTypeLabel = (type) => {
      const labelMap = {
        'VENTE_POISSON': 'Vente poisson',
        'AUTRE_RECETTE': 'Autre recette',
        'ACHAT_POISSON': 'Achat poisson',
        'ACHAT_ALIMENT': 'Achat aliment',
        'FRAIS_ENTRETIEN': 'Frais entretien'
      }
      return labelMap[type] || type
    }

    const getAmountClass = (amount, type) => {
      const isRevenue = type === 'VENTE_POISSON' || type === 'AUTRE_RECETTE'
      return isRevenue ? 'amount-revenue' : 'amount-expense'
    }

    const getStatusClass = (status) => {
      const statusMap = {
        'COMPLETED': 'completed',
        'PENDING': 'pending',
        'CANCELLED': 'cancelled'
      }
      return statusMap[status] || 'unknown'
    }

    const loadFinancialData = async () => {
      loading.value = true
      try {
        // Charger les poissons pour calculer les bénéfices potentiels
        const allPoissons = await poissonService.getAllPoissons()
        
        // Calculer les statistiques
        calculateFinancialStats(allPoissons)
        
        // Charger les transactions (simulées pour l'exemple)
        loadSampleTransactions()
        
        // Charger la répartition des dépenses
        loadExpensesBreakdown()
        
        // Charger l'analyse par race
        loadRaceAnalysis(allPoissons)
        
        // Calculer les projections
        calculateProjections()
        
        // Initialiser les graphiques si nous sommes en vue graphiques
        if (viewMode.value === 'charts') {
          await nextTick()
          initCharts()
        }
        
      } catch (error) {
        console.error('Erreur chargement données financières:', error)
        alert('Impossible de charger les données financières')
      } finally {
        loading.value = false
      }
    }

    const calculateFinancialStats = (poissons) => {
      // Calculs basés sur les poissons
      const soldFish = poissons.filter(p => p.estVenduPoisson)
      const aliveFish = poissons.filter(p => p.estEnViePoisson && !p.estVenduPoisson)
      const readyFish = aliveFish.filter(p => p.estPretAVendre)
      
      // Chiffre d'affaires (ventes réalisées)
      const totalRevenue = soldFish.reduce((sum, fish) => sum + (fish.prixVentePoisson || 0), 0)
      
      // Coûts d'achat des poissons vendus
      const fishCost = soldFish.reduce((sum, fish) => sum + (fish.prixAchatPoisson || 0), 0)
      
      // Coût des poissons encore en vie
      const currentFishCost = aliveFish.reduce((sum, fish) => sum + (fish.prixAchatPoisson || 0), 0)
      
      // Bénéfice potentiel (poissons prêts à vendre)
      const potentialProfit = readyFish.reduce((sum, fish) => sum + (fish.prixVentePoisson || 0), 0)
      
      // Coûts estimés des aliments (simulation)
      const foodCost = calculateFoodCost(aliveFish)
      
      const netProfit = totalRevenue - fishCost - foodCost
      const profitMargin = totalRevenue > 0 ? (netProfit / totalRevenue) * 100 : 0
      const roi = fishCost > 0 ? (netProfit / fishCost) * 100 : 0
      
      stats.value = {
        totalRevenue,
        totalCost: fishCost + foodCost,
        netProfit,
        profitMargin: Math.round(profitMargin * 10) / 10,
        roi: Math.round(roi * 10) / 10,
        revenueTrend: 5.2, // Simulé
        fishCost,
        foodCost,
        fishSold: soldFish.length,
        fishBought: poissons.length,
        fishReadyToSell: readyFish.length,
        avgSalePrice: soldFish.length > 0 ? totalRevenue / soldFish.length : 0,
        avgPurchaseCost: poissons.length > 0 ? 
          (fishCost + currentFishCost) / poissons.length : 0,
        foodExpenses: foodCost,
        otherExpenses: 0,
        highestSale: soldFish.length > 0 ? 
          Math.max(...soldFish.map(f => f.prixVentePoisson || 0)) : 0,
        avgDailySales: 2, // Simulé
        grossMargin: Math.round(profitMargin * 10) / 10,
        netMargin: Math.round(profitMargin * 0.8 * 10) / 10,
        revenueGrowth: 12.5, // Simulé
        potentialProfit,
        avgPricePerFish: readyFish.length > 0 ? 
          potentialProfit / readyFish.length : 0,
        revenueTarget: 10000000
      }
      
      // Mettre à jour la liste des poissons prêts à vendre
      fishReadyToSell.value = readyFish.map(fish => ({
        id: fish.idPoisson,
        name: fish.nomPoisson,
        price: fish.prixVentePoisson
      }))
    }

    const calculateFoodCost = (poissons) => {
      // Simulation du coût alimentaire
      // En réalité, vous devriez calculer cela à partir de l'historique des nourrissages
      const avgFoodCostPerFishPerDay = 100 // MGA par jour
      const avgDaysAlive = 60 // jours moyens d'élevage
      
      return poissons.length * avgFoodCostPerFishPerDay * avgDaysAlive
    }

    const loadSampleTransactions = () => {
      // Transactions simulées - À remplacer par votre API
      recentTransactions.value = [
        {
          id: 1,
          date: '2024-01-15',
          type: 'VENTE_POISSON',
          description: 'Vente Tilapia-001',
          amount: 25000,
          fishName: 'Tilapia-001',
          status: 'COMPLETED'
        },
        {
          id: 2,
          date: '2024-01-14',
          type: 'ACHAT_ALIMENT',
          description: 'Achat granulés standard',
          amount: -150000,
          fishName: null,
          status: 'COMPLETED'
        },
        {
          id: 3,
          date: '2024-01-13',
          type: 'ACHAT_POISSON',
          description: 'Achat 10 alevins Tilapia',
          amount: -50000,
          fishName: null,
          status: 'COMPLETED'
        },
        {
          id: 4,
          date: '2024-01-12',
          type: 'VENTE_POISSON',
          description: 'Vente Carpe-005',
          amount: 35000,
          fishName: 'Carpe-005',
          status: 'COMPLETED'
        },
        {
          id: 5,
          date: '2024-01-11',
          type: 'FRAIS_ENTRETIEN',
          description: 'Maintenance bassin',
          amount: -20000,
          fishName: null,
          status: 'COMPLETED'
        }
      ]
    }

    const loadExpensesBreakdown = () => {
      expensesBreakdown.value = [
        { category: 'Achats poissons', amount: 1500000, percentage: 40, color: '#4299e1' },
        { category: 'Aliments', amount: 1000000, percentage: 27, color: '#48bb78' },
        { category: 'Entretien', amount: 500000, percentage: 13, color: '#ed8936' },
        { category: 'Équipement', amount: 400000, percentage: 11, color: '#9f7aea' },
        { category: 'Autres', amount: 300000, percentage: 9, color: '#ecc94b' }
      ]
    }

    const loadRaceAnalysis = (poissons) => {
      // Regrouper par race
      const races = {}
      
      poissons.forEach(poisson => {
        const raceName = poisson.racePoisson?.nomRacePoisson || 'Inconnue'
        if (!races[raceName]) {
          races[raceName] = {
            name: raceName,
            sales: 0,
            costs: 0,
            profit: 0,
            margin: 0,
            roi: 0,
            count: 0
          }
        }
        
        if (poisson.estVenduPoisson) {
          races[raceName].sales += poisson.prixVentePoisson || 0
          races[raceName].costs += poisson.prixAchatPoisson || 0
          races[raceName].profit = races[raceName].sales - races[raceName].costs
          races[raceName].margin = races[raceName].sales > 0 ? 
            (races[raceName].profit / races[raceName].sales) * 100 : 0
          races[raceName].roi = races[raceName].costs > 0 ? 
            (races[raceName].profit / races[raceName].costs) * 100 : 0
        }
        
        races[raceName].count++
      })
      
      raceAnalysis.value = Object.values(races).map(race => ({
        ...race,
        margin: Math.round(race.margin * 10) / 10,
        roi: Math.round(race.roi * 10) / 10
      }))
    }

    const calculateProjections = () => {
      const dailyRevenue = stats.value.totalRevenue / 30 // Approximation
      projections.value = {
        monthly: dailyRevenue * 30,
        monthlyGrowth: 15,
        quarterly: dailyRevenue * 90,
        quarterlyGrowth: 25,
        yearly: dailyRevenue * 365
      }
    }

    const loadFoodItems = async () => {
      try {
        const aliments = await alimentService.getAliments()
        foodItems.value = aliments.map(aliment => ({
          id: aliment.idAliment,
          name: aliment.nomAliment,
          stock: aliment.stockAliment || 0
        }))
      } catch (error) {
        console.error('Erreur chargement aliments:', error)
      }
    }

    const initCharts = () => {
      destroyCharts()
      createRevenueChart()
      createRevenueDistributionChart()
      createCostsChart()
      createCostsDistributionChart()
      createProfitChart()
      createGoalsChart()
      createExpensesChart()
    }

    const destroyCharts = () => {
      const charts = [
        revenueChart.value,
        revenueDistributionChart.value,
        costsChart.value,
        costsDistributionChart.value,
        profitChart.value,
        goalsChart.value,
        expensesChart.value
      ]
      
      charts.forEach(chart => {
        if (chart && chart.destroy) {
          chart.destroy()
        }
      })
    }

    const createRevenueChart = () => {
      const ctx = revenueChartRef.value?.getContext('2d')
      if (!ctx) return
      
      // Détruire le graphique existant
      if (revenueChart.value) {
        revenueChart.value.destroy()
      }
      
      const labels = ['Jan', 'Fév', 'Mar', 'Avr', 'Mai', 'Jun', 'Jul', 'Aoû', 'Sep', 'Oct', 'Nov', 'Déc']
      const revenueData = labels.map(() => Math.random() * 5000000 + 2000000)
      
      revenueChart.value = new Chart(ctx, {
        type: 'line',
        data: {
          labels: labels,
          datasets: [{
            label: 'Revenus (MGA)',
            data: revenueData,
            borderColor: '#48bb78',
            backgroundColor: 'rgba(72, 187, 120, 0.1)',
            borderWidth: 3,
            fill: true,
            tension: 0.4
          }]
        },
        options: {
          responsive: true,
          maintainAspectRatio: false,
          plugins: {
            legend: { display: false }
          },
          scales: {
            y: {
              beginAtZero: true,
              ticks: {
                callback: function(value) {
                  return (value / 1000000).toFixed(0) + 'M'
                }
              }
            }
          }
        }
      })
    }

    const createRevenueDistributionChart = () => {
      const ctx = revenueDistributionChartRef.value?.getContext('2d')
      if (!ctx) return
      
      // Détruire le graphique existant
      if (revenueDistributionChart.value) {
        revenueDistributionChart.value.destroy()
      }
      
      revenueDistributionChart.value = new Chart(ctx, {
        type: 'doughnut',
        data: {
          labels: ['Tilapia', 'Carpe', 'Truite', 'Autres'],
          datasets: [{
            data: [45, 30, 20, 5],
            backgroundColor: [
              '#48bb78',
              '#4299e1',
              '#ed8936',
              '#ecc94b'
            ]
          }]
        },
        options: {
          responsive: true,
          maintainAspectRatio: false,
          plugins: {
            legend: { position: 'right' }
          }
        }
      })
    }

    const createCostsChart = () => {
      const ctx = costsChartRef.value?.getContext('2d')
      if (!ctx) return
      
      // Détruire le graphique existant
      if (costsChart.value) {
        costsChart.value.destroy()
      }
      
      const labels = ['Jan', 'Fév', 'Mar', 'Avr', 'Mai', 'Jun']
      const costsData = labels.map(() => Math.random() * 3000000 + 1000000)
      
      costsChart.value = new Chart(ctx, {
        type: 'bar',
        data: {
          labels: labels,
          datasets: [{
            label: 'Coûts (MGA)',
            data: costsData,
            backgroundColor: '#f56565',
            borderColor: '#e53e3e',
            borderWidth: 1
          }]
        },
        options: {
          responsive: true,
          maintainAspectRatio: false,
          plugins: {
            legend: { display: false }
          },
          scales: {
            y: {
              beginAtZero: true,
              ticks: {
                callback: function(value) {
                  return (value / 1000000).toFixed(0) + 'M'
                }
              }
            }
          }
        }
      })
    }

    const createCostsDistributionChart = () => {
      const ctx = costsDistributionChartRef.value?.getContext('2d')
      if (!ctx) return
      
      // Détruire le graphique existant
      if (costsDistributionChart.value) {
        costsDistributionChart.value.destroy()
      }
      
      costsDistributionChart.value = new Chart(ctx, {
        type: 'pie',
        data: {
          labels: expensesBreakdown.value.map(e => e.category),
          datasets: [{
            data: expensesBreakdown.value.map(e => e.amount),
            backgroundColor: expensesBreakdown.value.map(e => e.color)
          }]
        },
        options: {
          responsive: true,
          maintainAspectRatio: false,
          plugins: {
            legend: { position: 'right' }
          }
        }
      })
    }

    const createProfitChart = () => {
      const ctx = profitChartRef.value?.getContext('2d')
      if (!ctx) return
      
      // Détruire le graphique existant
      if (profitChart.value) {
        profitChart.value.destroy()
      }
      
      const labels = ['Q1', 'Q2', 'Q3', 'Q4']
      const profitData = labels.map(() => Math.random() * 2000000 + 500000)
      
      profitChart.value = new Chart(ctx, {
        type: 'line',
        data: {
          labels: labels,
          datasets: [{
            label: 'Bénéfice net (MGA)',
            data: profitData,
            borderColor: '#9f7aea',
            backgroundColor: 'rgba(159, 122, 234, 0.1)',
            borderWidth: 3,
            fill: true,
            tension: 0.4
          }]
        },
        options: {
          responsive: true,
          maintainAspectRatio: false,
          plugins: {
            legend: { display: false }
          },
          scales: {
            y: {
              beginAtZero: false,
              ticks: {
                callback: function(value) {
                  return (value / 1000000).toFixed(0) + 'M'
                }
              }
            }
          }
        }
      })
    }

    const createGoalsChart = () => {
      const ctx = goalsChartRef.value?.getContext('2d')
      if (!ctx) return
      
      // Détruire le graphique existant
      if (goalsChart.value) {
        goalsChart.value.destroy()
      }
      
      goalsChart.value = new Chart(ctx, {
        type: 'bar',
        data: {
          labels: ['Revenus', 'Marge', 'ROI'],
          datasets: [
            {
              label: 'Objectif',
              data: [100, 30, 50],
              backgroundColor: 'rgba(102, 126, 234, 0.7)'
            },
            {
              label: 'Actuel',
              data: [revenueProgress.value, marginProgress.value, stats.value.roi],
              backgroundColor: 'rgba(72, 187, 120, 0.7)'
            }
          ]
        },
        options: {
          responsive: true,
          maintainAspectRatio: false,
          scales: {
            y: {
              beginAtZero: true,
              max: 100
            }
          }
        }
      })
    }

    const createExpensesChart = () => {
      const ctx = expensesChartRef.value?.getContext('2d')
      if (!ctx) return
      
      // Détruire le graphique existant
      if (expensesChart.value) {
        expensesChart.value.destroy()
      }
      
      expensesChart.value = new Chart(ctx, {
        type: 'bar',
        data: {
          labels: expensesBreakdown.value.map(e => e.category),
          datasets: [{
            label: 'Dépenses (MGA)',
            data: expensesBreakdown.value.map(e => e.amount),
            backgroundColor: expensesBreakdown.value.map(e => e.color)
          }]
        },
        options: {
          responsive: true,
          maintainAspectRatio: false,
          indexAxis: 'y',
          plugins: {
            legend: { display: false }
          },
          scales: {
            x: {
              beginAtZero: true,
              ticks: {
                callback: function(value) {
                  return (value / 1000000).toFixed(0) + 'M'
                }
              }
            }
          }
        }
      })
    }

    const refreshData = () => {
      loadFinancialData()
    }

    const exportFinancialReport = () => {
      // Simuler l'export
      const reportData = {
        stats: stats.value,
        period: selectedPeriod.value,
        date: new Date().toISOString()
      }
      
      const blob = new Blob([JSON.stringify(reportData, null, 2)], { type: 'application/json' })
      const url = URL.createObjectURL(blob)
      const link = document.createElement('a')
      link.href = url
      link.download = `rapport_financier_${new Date().toISOString().split('T')[0]}.json`
      link.click()
      
      alert('Rapport exporté avec succès')
    }

    const registerSale = () => {
      modalMode.value = 'sale'
      newTransaction.value = {
        type: 'VENTE_POISSON',
        amount: 0,
        description: '',
        fishId: '',
        foodId: ''
      }
      showTransactionModal.value = true
    }

    const registerPurchase = () => {
      modalMode.value = 'purchase'
      newTransaction.value = {
        type: 'ACHAT_ALIMENT',
        amount: 0,
        description: '',
        fishId: '',
        foodId: ''
      }
      showTransactionModal.value = true
    }

    const saveTransaction = () => {
      // Ici, vous enverriez la transaction à votre API
      console.log('Nouvelle transaction:', newTransaction.value)
      
      // Ajouter à la liste
      recentTransactions.value.unshift({
        id: recentTransactions.value.length + 1,
        date: new Date().toISOString().split('T')[0],
        type: newTransaction.value.type,
        description: newTransaction.value.description || 'Nouvelle transaction',
        amount: newTransaction.value.type.includes('VENTE') || newTransaction.value.type === 'AUTRE_RECETTE' 
          ? Math.abs(newTransaction.value.amount) 
          : -Math.abs(newTransaction.value.amount),
        fishName: null,
        status: 'COMPLETED'
      })
      
      closeModal()
      alert('Transaction enregistrée avec succès')
      
      // Recharger les données
      loadFinancialData()
    }

    const setFinancialGoals = () => {
      showGoalsModal.value = true
    }

    const saveFinancialGoals = () => {
      // Ici, vous sauvegarderiez les objectifs dans votre API
      console.log('Objectifs financiers:', financialGoals.value)
      closeGoalsModal()
      alert('Objectifs enregistrés avec succès')
    }

    const showDetails = (section) => {
      console.log('Voir détails:', section)
      // Vous pourriez naviguer vers une vue détaillée ou ouvrir un modal
    }

    const loadMoreTransactions = () => {
      // Simuler le chargement de plus de transactions
      console.log('Charger plus de transactions')
    }

    const generateFinancialReport = () => {
      const report = `
        RAPPORT FINANCIER - PISCICULTURE
        Période: ${selectedPeriod.value}
        Date: ${new Date().toLocaleDateString('fr-FR')}
        
        CHIFFRE D'AFFAIRES: ${formatCurrency(stats.value.totalRevenue)}
        COÛTS TOTAUX: ${formatCurrency(stats.value.totalCost)}
        BÉNÉFICE NET: ${formatCurrency(stats.value.netProfit)}
        MARGE BÉNÉFICIAIRE: ${stats.value.profitMargin}%
        ROI: ${stats.value.roi}%
        
        POISSONS VENDUS: ${stats.value.fishSold}
        POISSONS PRÊTS À VENDRE: ${stats.value.fishReadyToSell}
        BÉNÉFICE POTENTIEL: ${formatCurrency(stats.value.potentialProfit)}
      `
      
      const blob = new Blob([report], { type: 'text/plain' })
      const url = URL.createObjectURL(blob)
      const link = document.createElement('a')
      link.href = url
      link.download = `rapport_financier_${new Date().toISOString().split('T')[0]}.txt`
      link.click()
      
      alert('Rapport généré avec succès')
    }

    const closeModal = () => {
      showTransactionModal.value = false
    }

    const closeGoalsModal = () => {
      showGoalsModal.value = false
    }

    // Initialisation
    onMounted(() => {
      // Définir les dates par défaut pour la période personnalisée
      const today = new Date()
      const oneMonthAgo = new Date()
      oneMonthAgo.setMonth(today.getMonth() - 1)
      
      customStartDate.value = oneMonthAgo.toISOString().split('T')[0]
      customEndDate.value = today.toISOString().split('T')[0]
      
      loadFinancialData()
      loadFoodItems()
    })

    // Watcher pour changer la vue
    watch(viewMode, (newMode) => {
      if (newMode === 'charts') {
        setTimeout(() => {
          initCharts()
        }, 100)
      }
    })

    return {
      // États
      loading,
      selectedPeriod,
      customStartDate,
      customEndDate,
      viewMode,
      showTransactionModal,
      showGoalsModal,
      modalMode,
      hasMoreTransactions,
      
      // Données
      stats,
      recentTransactions,
      expensesBreakdown,
      raceAnalysis,
      projections,
      newTransaction,
      financialGoals,
      fishReadyToSell,
      foodItems,
      
      // Computed
      revenueProgress,
      marginProgress,
      
      // Références
      revenueChartRef,
      revenueDistributionChartRef,
      costsChartRef,
      costsDistributionChartRef,
      profitChartRef,
      goalsChartRef,
      expensesChartRef,
      
      // Méthodes
      formatCurrency,
      formatDate,
      getTrendClass,
      getProfitClass,
      getRatioClass,
      getTransactionTypeClass,
      getTransactionTypeLabel,
      getAmountClass,
      getStatusClass,
      loadFinancialData,
      refreshData,
      exportFinancialReport,
      registerSale,
      registerPurchase,
      saveTransaction,
      setFinancialGoals,
      saveFinancialGoals,
      showDetails,
      loadMoreTransactions,
      generateFinancialReport,
      closeModal,
      closeGoalsModal
    }
  }
}
</script>

<style scoped>
@import '../assets/styles/benefice.css';

/* Styles spécifiques pour les graphiques */
.chart-container {
  height: 300px;
  position: relative;
}

.expenses-chart {
  height: 300px;
  margin-bottom: 1rem;
}

/* S'assurer que les canvas ont une taille définie */
.chart-container canvas,
.expenses-chart canvas {
  width: 100% !important;
  height: 100% !important;
}
</style>

