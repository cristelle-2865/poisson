<template>
  <div class="historique">
    <!-- Header avec titre et filtres -->
    <div class="header">
      <h1>📜 Historique des Nourrissages</h1>
      <p class="subtitle">Suivi complet de la croissance et de la nutrition des poissons</p>
      
      <div class="filters">
        <div class="filter-group">
          <label for="date-range">Période :</label>
          <select v-model="dateRange" @change="loadHistorique">
            <option value="7">7 derniers jours</option>
            <option value="30">30 derniers jours</option>
            <option value="90">3 derniers mois</option>
            <option value="365">1 an</option>
            <option value="all">Tout l'historique</option>
          </select>
        </div>
        
        <div class="filter-group">
          <label for="poisson-filter">Poisson :</label>
          <select v-model="selectedPoisson" @change="loadHistorique">
            <option value="">Tous les poissons</option>
            <option v-for="poisson in poissonsList" :key="poisson.idPoisson" :value="poisson.idPoisson">
              {{ poisson.nomPoisson }} ({{ poisson.racePoisson?.nomRacePoisson || 'Non défini' }})
            </option>
          </select>
        </div>
        
        <div class="filter-group">
          <label for="statut-filter">Statut :</label>
          <select v-model="statutFilter" @change="loadHistorique">
            <option value="">Tous</option>
            <option value="rassasie">Rassasiés</option>
            <option value="affame">Affamés</option>
            <option value="satisfait">Besoins satisfaits</option>
          </select>
        </div>
        
        <button class="btn-refresh" @click="loadHistorique">
          🔄 Actualiser
        </button>
        
        <button class="btn-export" @click="exportData">
          📤 Exporter Excel
        </button>
      </div>
    </div>

    <!-- Statistiques résumées -->
    <div class="stats-summary" v-if="historique.length > 0">
      <div class="stat-card">
        <div class="stat-icon">📅</div>
        <div class="stat-content">
          <h3>Période couverte</h3>
          <p>{{ formatDate(periodStart) }} - {{ formatDate(periodEnd) }}</p>
        </div>
      </div>
      
      <div class="stat-card">
        <div class="stat-icon">🐟</div>
        <div class="stat-content">
          <h3>Poissons nourris</h3>
          <p>{{ stats.poissonsNourris }} sur {{ stats.totalPoissons }}</p>
        </div>
      </div>
      
      <div class="stat-card">
        <div class="stat-icon">⚖️</div>
        <div class="stat-content">
          <h3>Gain de poids total</h3>
          <p>{{ formatPoids(stats.gainTotal) }} g</p>
        </div>
      </div>
      
      <div class="stat-card">
        <div class="stat-icon">📈</div>
        <div class="stat-content">
          <h3>Taux de satisfaction moyen</h3>
          <p>{{ stats.tauxSatisfactionMoyen }}%</p>
        </div>
      </div>
      
      <div class="stat-card">
        <div class="stat-icon">🍽️</div>
        <div class="stat-content">
          <h3>Nourriture utilisée</h3>
          <p>{{ formatPoids(stats.nourritureTotale) }} kg</p>
        </div>
      </div>
    </div>

    <!-- Tableau principal de l'historique -->
    <div class="table-container">
      <div class="table-header">
        <h2>Historique détaillé</h2>
        <div class="table-actions">
          <input 
            type="text" 
            v-model="searchQuery" 
            placeholder="Rechercher par nom, race..." 
            class="search-input"
          />
          <button class="btn-toggle-view" @click="toggleView">
            {{ viewMode === 'table' ? '📊 Vue graphique' : '📋 Vue tableau' }}
          </button>
        </div>
      </div>

      <!-- Vue tableau -->
      <div v-if="viewMode === 'table'" class="table-responsive">
        <table class="data-table">
          <thead>
            <tr>
              <th @click="sortBy('dateNourrissageFisakafoanana')">
                Date 📅
                <span v-if="sortColumn === 'dateNourrissageFisakafoanana'">
                  {{ sortDirection === 'asc' ? '↑' : '↓' }}
                </span>
              </th>
              <th @click="sortBy('nomPoisson')">
                Poisson 🐟
                <span v-if="sortColumn === 'nomPoisson'">
                  {{ sortDirection === 'asc' ? '↑' : '↓' }}
                </span>
              </th>
              <th @click="sortBy('race')">
                Race
                <span v-if="sortColumn === 'race'">
                  {{ sortDirection === 'asc' ? '↑' : '↓' }}
                </span>
              </th>
              <th @click="sortBy('ancienPoidsFisakafoanana')">
                Poids avant ⚖️
                <span v-if="sortColumn === 'ancienPoidsFisakafoanana'">
                  {{ sortDirection === 'asc' ? '↑' : '↓' }}
                </span>
              </th>
              <th @click="sortBy('nouveauPoidsFisakafoanana')">
                Poids après ⚖️
                <span v-if="sortColumn === 'nouveauPoidsFisakafoanana'">
                  {{ sortDirection === 'asc' ? '↑' : '↓' }}
                </span>
              </th>
              <th @click="sortBy('gainPoidsFisakafoanana')">
                Gain 📈
                <span v-if="sortColumn === 'gainPoidsFisakafoanana'">
                  {{ sortDirection === 'asc' ? '↑' : '↓' }}
                </span>
              </th>
              <th @click="sortBy('quantiteNourritureFisakafoanana')">
                Nourriture 🍽️
                <span v-if="sortColumn === 'quantiteNourritureFisakafoanana'">
                  {{ sortDirection === 'asc' ? '↑' : '↓' }}
                </span>
              </th>
              <th @click="sortBy('tauxSatisfactionFisakafoanana')">
                Satisfaction
                <span v-if="sortColumn === 'tauxSatisfactionFisakafoanana'">
                  {{ sortDirection === 'asc' ? '↑' : '↓' }}
                </span>
              </th>
              <th>Statut</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="item in filteredHistorique" :key="item.idFisakafoanana">
              <td>
                <div class="date-cell">
                  <span class="date">{{ formatDate(item.dateNourrissageFisakafoanana) }}</span>
                  <span class="time">{{ formatTime(item.heureNourrissageFisakafoanana) }}</span>
                </div>
              </td>
              <td>
                <div class="poisson-cell">
                  <span class="poisson-name">{{ item.poisson?.nomPoisson || 'N/A' }}</span>
                  <span class="poisson-id">#{{ item.poisson?.idPoisson }}</span>
                </div>
              </td>
              <td>{{ item.poisson?.racePoisson?.nomRacePoisson || 'Non défini' }}</td>
              <td>{{ formatPoids(item.ancienPoidsFisakafoanana) }} g</td>
              <td>
                <div class="weight-cell">
                  {{ formatPoids(item.nouveauPoidsFisakafoanana) }} g
                  <span class="weight-diff positive" v-if="item.gainPoidsFisakafoanana > 0">
                    +{{ formatPoids(item.gainPoidsFisakafoanana) }}g
                  </span>
                </div>
              </td>
              <td>
                <span class="gain-badge" :class="getGainClass(item.gainPoidsFisakafoanana)">
                  {{ formatPoids(item.gainPoidsFisakafoanana) }} g
                </span>
              </td>
              <td>{{ formatPoids(item.quantiteNourritureFisakafoanana) }} g</td>
              <td>
                <div class="satisfaction-cell">
                  <div class="satisfaction-bar">
                    <div 
                      class="satisfaction-fill" 
                      :style="{ width: item.tauxSatisfactionFisakafoanana + '%' }"
                      :class="getSatisfactionClass(item.tauxSatisfactionFisakafoanana)"
                    ></div>
                  </div>
                  <span class="satisfaction-text">{{ formatNumber(item.tauxSatisfactionFisakafoanana) }}%</span>
                </div>
              </td>
              <td>
                <span class="status-badge" :class="getStatusClass(item)">
                  {{ getStatusText(item) }}
                </span>
              </td>
              <td>
                <div class="action-buttons">
                  <button 
                    class="btn-details" 
                    @click="showDetails(item)"
                    title="Voir les détails"
                  >
                    👁️
                  </button>
                  <button 
                    class="btn-chart" 
                    @click="showEvolution(item.poisson?.idPoisson)"
                    title="Voir l'évolution"
                  >
                    📊
                  </button>
                  <button 
                    class="btn-delete" 
                    @click="confirmDelete(item)"
                    v-if="userRole === 'ADMIN'"
                    title="Supprimer"
                  >
                    🗑️
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
        
        <!-- Pagination -->
        <div class="pagination" v-if="filteredHistorique.length > itemsPerPage">
          <button @click="prevPage" :disabled="currentPage === 1">← Précédent</button>
          <span class="page-info">
            Page {{ currentPage }} sur {{ totalPages }}
          </span>
          <button @click="nextPage" :disabled="currentPage === totalPages">Suivant →</button>
        </div>
        
        <div v-if="filteredHistorique.length === 0" class="no-data">
          <p>📭 Aucun historique trouvé pour les critères sélectionnés</p>
        </div>
      </div>

      <!-- Vue graphique -->
      <div v-else class="graph-view">
        <div class="graph-container">
          <h3>Évolution du gain de poids moyen par jour</h3>
          <canvas ref="gainChart"></canvas>
        </div>
        
        <div class="graph-container">
          <h3>Distribution des taux de satisfaction</h3>
          <canvas ref="satisfactionChart"></canvas>
        </div>
        
        <div class="graph-container">
          <h3>Consommation de nourriture</h3>
          <canvas ref="nourritureChart"></canvas>
        </div>
      </div>
    </div>

    <!-- Graphique d'évolution d'un poisson spécifique -->
    <div class="poisson-evolution" v-if="selectedPoissonEvolution">
      <div class="evolution-header">
        <h3>Évolution détaillée - {{ selectedPoissonEvolution.nom }}</h3>
        <button class="btn-close" @click="selectedPoissonEvolution = null">×</button>
      </div>
      <div class="evolution-chart">
        <canvas ref="poissonEvolutionChart"></canvas>
      </div>
    </div>

    <!-- Modal des détails -->
    <div class="modal-overlay" v-if="showModal" @click.self="closeModal">
      <div class="modal-content">
        <div class="modal-header">
          <h2>Détails du nourrissage</h2>
          <button class="modal-close" @click="closeModal">×</button>
        </div>
        
        <div class="modal-body" v-if="selectedItem">
          <div class="detail-section">
            <h3>📋 Informations générales</h3>
            <div class="detail-grid">
              <div class="detail-item">
                <label>Date :</label>
                <span>{{ formatDate(selectedItem.dateNourrissageFisakafoanana) }}</span>
              </div>
              <div class="detail-item">
                <label>Heure :</label>
                <span>{{ formatTime(selectedItem.heureNourrissageFisakafoanana) }}</span>
              </div>
              <div class="detail-item">
                <label>Poisson :</label>
                <span>{{ selectedItem.poisson?.nomPoisson }} ({{ selectedItem.poisson?.racePoisson?.nomRacePoisson }})</span>
              </div>
              <div class="detail-item">
                <label>ID Poisson :</label>
                <span>#{{ selectedItem.poisson?.idPoisson }}</span>
              </div>
            </div>
          </div>
          
          <div class="detail-section">
            <h3>⚖️ Évolution du poids</h3>
            <div class="detail-grid">
              <div class="detail-item">
                <label>Poids avant :</label>
                <span>{{ formatPoids(selectedItem.ancienPoidsFisakafoanana) }} g</span>
              </div>
              <div class="detail-item">
                <label>Poids après :</label>
                <span>{{ formatPoids(selectedItem.nouveauPoidsFisakafoanana) }} g</span>
              </div>
              <div class="detail-item">
                <label>Gain de poids :</label>
                <span class="gain-positive">
                  +{{ formatPoids(selectedItem.gainPoidsFisakafoanana) }} g
                </span>
              </div>
              <div class="detail-item">
                <label>Taux de croissance :</label>
                <span>{{ calculateTauxCroissance(selectedItem) }}%</span>
              </div>
            </div>
          </div>
          
          <div class="detail-section">
            <h3>🍽️ Nutrition</h3>
            <div class="detail-grid">
              <div class="detail-item">
                <label>Quantité de nourriture :</label>
                <span>{{ formatPoids(selectedItem.quantiteNourritureFisakafoanana) }} g</span>
              </div>
              <div class="detail-item">
                <label>Protéines reçues :</label>
                <span>{{ formatPoids(selectedItem.proteinesRecuesFisakafoanana) }} g</span>
              </div>
              <div class="detail-item">
                <label>Glucides reçus :</label>
                <span>{{ formatPoids(selectedItem.glucidesRecusFisakafoanana) }} g</span>
              </div>
              <div class="detail-item">
                <label>Besoins journaliers :</label>
                <span>2g protéines + 4g glucides</span>
              </div>
            </div>
          </div>
          
          <div class="detail-section">
            <h3>📊 Satisfaction nutritionnelle</h3>
            <div class="satisfaction-detail">
              <div class="satisfaction-bar-large">
                <div 
                  class="satisfaction-fill-large" 
                  :style="{ width: selectedItem.tauxSatisfactionFisakafoanana + '%' }"
                  :class="getSatisfactionClass(selectedItem.tauxSatisfactionFisakafoanana)"
                ></div>
                <span class="satisfaction-text-large">
                  {{ formatNumber(selectedItem.tauxSatisfactionFisakafoanana) }}%
                </span>
              </div>
              <div class="satisfaction-info">
                <p v-if="selectedItem.tauxSatisfactionFisakafoanana >= 100">
                  ✅ Tous les besoins nutritionnels sont satisfaits
                </p>
                <p v-else-if="selectedItem.tauxSatisfactionFisakafoanana >= 80">
                  ⚠️ Besoins partiellement satisfaits
                </p>
                <p v-else>
                  ❌ Besoins nutritionnels insuffisants
                </p>
                <p>
                  <strong>Statut rassasiement :</strong> 
                  {{ selectedItem.besoinsSatisfaitsFisakafoanana ? 'Rassasié ✓' : 'Non rassasié ✗' }}
                </p>
              </div>
            </div>
          </div>
          
          <div class="detail-section">
            <h3>🏷️ Plat utilisé</h3>
            <div v-if="selectedItem.plat" class="plat-info">
              <p><strong>Nom :</strong> {{ selectedItem.plat.nomPlat || 'Mélange standard' }}</p>
              <p><strong>Description :</strong> {{ selectedItem.plat.descriptionPlat || 'Non spécifié' }}</p>
            </div>
            <div v-else class="plat-info">
              <p>Pas d'information sur le plat utilisé</p>
            </div>
          </div>
        </div>
        
        <div class="modal-footer">
          <button class="btn-secondary" @click="closeModal">Fermer</button>
          <button class="btn-primary" @click="printDetails">Imprimer</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, computed, onMounted, watch } from 'vue'
import { Chart, registerables } from 'chart.js'
import nourrissageService from '../services/nourrissageService'
import poissonService from '../services/poissonService'
import * as XLSX from 'xlsx'
import { calculService } from '../services/calculService'

Chart.register(...registerables)

export default {
  name: 'Historique',
  
  setup() {
    // État réactif
    const historique = ref([])
    const poissonsList = ref([])
    const dateRange = ref('30')
    const selectedPoisson = ref('')
    const statutFilter = ref('')
    const searchQuery = ref('')
    const viewMode = ref('table')
    const sortColumn = ref('dateNourrissageFisakafoanana')
    const sortDirection = ref('desc')
    const currentPage = ref(1)
    const itemsPerPage = ref(20)
    const showModal = ref(false)
    const selectedItem = ref(null)
    const selectedPoissonEvolution = ref(null)
    const userRole = ref('UTILISATEUR') // À remplacer par la vraie valeur d'authentification
    
    // Références pour les graphiques
    const gainChart = ref(null)
    const satisfactionChart = ref(null)
    const nourritureChart = ref(null)
    const poissonEvolutionChart = ref(null)
    
    // Statistiques
    const stats = ref({
      poissonsNourris: 0,
      totalPoissons: 0,
      gainTotal: 0,
      tauxSatisfactionMoyen: 0,
      nourritureTotale: 0
    })
    
    const periodStart = ref(new Date())
    const periodEnd = ref(new Date())

    // Charger les données initiales
    const loadInitialData = async () => {
      try {
        console.log('Chargement des données initiales...')
        
        // Charger la liste des poissons
        const poissons = await poissonService.getPoissonsEnVie()
        poissonsList.value = poissons
        stats.value.totalPoissons = poissons.length
        
        // Charger l'historique
        await loadHistorique()
        
      } catch (error) {
        console.error('Erreur chargement données initiales:', error)
        alert('Impossible de charger les données. Vérifiez votre connexion.')
      }
    }

    // Charger l'historique
    const loadHistorique = async () => {
      try {
        console.log('Chargement de l\'historique...')
        
        // Calculer les dates de la période
        const endDate = new Date()
        const startDate = new Date()
        
        if (dateRange.value !== 'all') {
          const days = parseInt(dateRange.value)
          startDate.setDate(startDate.getDate() - days)
        } else {
          // Pour "tout l'historique", prendre il y a 2 ans
          startDate.setFullYear(startDate.getFullYear() - 2)
        }
        
        periodStart.value = startDate
        periodEnd.value = endDate
        
        // Formater les dates pour l'API
        const formatDateForAPI = (date) => {
          return date.toISOString().split('T')[0]
        }
        
        // Si un poisson spécifique est sélectionné
        if (selectedPoisson.value) {
          const data = await nourrissageService.getHistoriquePoisson(selectedPoisson.value)
          historique.value = Array.isArray(data) ? data : []
        } else {
          // Pour l'instant, on récupère tout et on filtre côté client
          // Dans une vraie app, il faudrait un endpoint avec filtres
          const allData = []
          // Simuler des données pour chaque poisson
          for (const poisson of poissonsList.value) {
            try {
              const data = await nourrissageService.getHistoriquePoisson(poisson.idPoisson)
              if (Array.isArray(data)) {
                allData.push(...data)
              }
            } catch (error) {
              console.warn(`Pas d'historique pour le poisson ${poisson.idPoisson}`)
            }
          }
          historique.value = allData
        }
        
        // Calculer les statistiques
        calculateStats()
        
        // Initialiser les graphiques
        if (viewMode.value === 'graph') {
          setTimeout(() => {
            initCharts()
          }, 100)
        }
        
      } catch (error) {
        console.error('Erreur chargement historique:', error)
        historique.value = []
      }
    }

    // Calculer les statistiques
    const calculateStats = () => {
      const data = historique.value
      
      if (data.length === 0) {
        stats.value = {
          poissonsNourris: 0,
          totalPoissons: poissonsList.value.length,
          gainTotal: 0,
          tauxSatisfactionMoyen: 0,
          nourritureTotale: 0
        }
        return
      }
      
      // Nombre de poissons uniques nourris
      const poissonsNourris = new Set(data.map(item => item.poisson?.idPoisson)).size
      
      // Somme des gains de poids
      const gainTotal = data.reduce((sum, item) => 
        sum + (item.gainPoidsFisakafoanana || 0), 0)
      
      // Moyenne du taux de satisfaction
      const tauxSatisfactionMoyen = data.length > 0 
        ? data.reduce((sum, item) => sum + (item.tauxSatisfactionFisakafoanana || 0), 0) / data.length
        : 0
      
      // Somme de la nourriture utilisée
      const nourritureTotale = data.reduce((sum, item) => 
        sum + (item.quantiteNourritureFisakafoanana || 0), 0)
      
      stats.value = {
        poissonsNourris,
        totalPoissons: poissonsList.value.length,
        gainTotal,
        tauxSatisfactionMoyen: Math.round(tauxSatisfactionMoyen * 10) / 10,
        nourritureTotale: nourritureTotale / 1000 // Convertir en kg
      }
    }

    // Filtrer et trier les données
    const filteredHistorique = computed(() => {
      let filtered = [...historique.value]
      
      // Filtre de recherche
      if (searchQuery.value) {
        const query = searchQuery.value.toLowerCase()
        filtered = filtered.filter(item => {
          const poissonName = item.poisson?.nomPoisson?.toLowerCase() || ''
          const raceName = item.poisson?.racePoisson?.nomRacePoisson?.toLowerCase() || ''
          return poissonName.includes(query) || raceName.includes(query)
        })
      }
      
      // Filtre par statut
      if (statutFilter.value) {
        filtered = filtered.filter(item => {
          switch (statutFilter.value) {
            case 'rassasie':
              return item.besoinsSatisfaitsFisakafoanana === true
            case 'affame':
              return item.besoinsSatisfaitsFisakafoanana === false
            case 'satisfait':
              return item.tauxSatisfactionFisakafoanana >= 100
            default:
              return true
          }
        })
      }
      
      // Trier les données
      filtered.sort((a, b) => {
        let aValue, bValue
        
        switch (sortColumn.value) {
          case 'dateNourrissageFisakafoanana':
            aValue = new Date(a.dateNourrissageFisakafoanana)
            bValue = new Date(b.dateNourrissageFisakafoanana)
            break
          case 'nomPoisson':
            aValue = a.poisson?.nomPoisson || ''
            bValue = b.poisson?.nomPoisson || ''
            break
          case 'race':
            aValue = a.poisson?.racePoisson?.nomRacePoisson || ''
            bValue = b.poisson?.racePoisson?.nomRacePoisson || ''
            break
          default:
            aValue = a[sortColumn.value] || 0
            bValue = b[sortColumn.value] || 0
        }
        
        if (typeof aValue === 'string') {
          return sortDirection.value === 'asc' 
            ? aValue.localeCompare(bValue)
            : bValue.localeCompare(aValue)
        } else {
          return sortDirection.value === 'asc'
            ? aValue - bValue
            : bValue - aValue
        }
      })
      
      // Pagination
      const startIndex = (currentPage.value - 1) * itemsPerPage.value
      return filtered.slice(startIndex, startIndex + itemsPerPage.value)
    })

    const totalPages = computed(() => {
      return Math.ceil(historique.value.length / itemsPerPage.value)
    })

    // Méthodes de pagination
    const nextPage = () => {
      if (currentPage.value < totalPages.value) {
        currentPage.value++
      }
    }

    const prevPage = () => {
      if (currentPage.value > 1) {
        currentPage.value--
      }
    }

    // Trier par colonne
    const sortBy = (column) => {
      if (sortColumn.value === column) {
        sortDirection.value = sortDirection.value === 'asc' ? 'desc' : 'asc'
      } else {
        sortColumn.value = column
        sortDirection.value = 'asc'
      }
    }

    // Formater les données
    const formatDate = (dateString) => {
      if (!dateString) return 'N/A'
      const date = new Date(dateString)
      return date.toLocaleDateString('fr-FR', {
        day: '2-digit',
        month: '2-digit',
        year: 'numeric'
      })
    }

    const formatTime = (timeString) => {
      if (!timeString) return 'N/A'
      return timeString.substring(0, 5) // Format HH:mm
    }

    const formatPoids = (poids) => {
      if (!poids) return '0'
      return Math.round(poids * 100) / 100
    }

    const formatNumber = (num) => {
      if (!num) return '0'
      return Math.round(num * 100) / 100
    }

    const calculateTauxCroissance = (item) => {
      if (!item.ancienPoidsFisakafoanana || item.ancienPoidsFisakafoanana === 0) return '0'
      
      // Calculer la satisfaction nutritionnelle
      const satisfaction = calculService.calculerSatisfaction(
        item.proteinesRecuesFisakafoanana || 0,
        item.glucidesRecusFisakafoanana || 0
      )
      
      // Le gain devrait correspondre aux règles
      const gainAttendu = calculService.calculerGainPoids(
        item.proteinesRecuesFisakafoanana || 0,
        item.glucidesRecusFisakafoanana || 0
      )
      
      const gainReel = item.gainPoidsFisakafoanana || 0
      const efficacite = gainAttendu > 0 ? (gainReel / gainAttendu) * 100 : 0
      
      return {
        taux: ((item.nouveauPoidsFisakafoanana - item.ancienPoidsFisakafoanana) / item.ancienPoidsFisakafoanana) * 100,
        satisfaction: satisfaction.moyenne,
        efficacite: Math.round(efficacite),
        gainAttendu,
        gainReel
      }
    }

    // Classes CSS dynamiques
    const getGainClass = (gain) => {
      if (gain > 15) return 'gain-high'
      if (gain > 10) return 'gain-medium'
      if (gain > 5) return 'gain-low'
      return 'gain-very-low'
    }

    const getSatisfactionClass = (taux) => {
      if (taux >= 100) return 'satisfaction-high'
      if (taux >= 80) return 'satisfaction-medium'
      if (taux >= 60) return 'satisfaction-low'
      return 'satisfaction-very-low'
    }

    const getStatusClass = (item) => {
      if (item.besoinsSatisfaitsFisakafoanana) return 'status-satisfied'
      if (item.tauxSatisfactionFisakafoanana >= 80) return 'status-partial'
      return 'status-unsatisfied'
    }

    const getStatusText = (item) => {
      if (item.besoinsSatisfaitsFisakafoanana) return 'Satisfait ✓'
      if (item.tauxSatisfactionFisakafoanana >= 80) return 'Partiel ⚠️'
      return 'Insuffisant ✗'
    }

    // Afficher les détails
    const showDetails = (item) => {
      selectedItem.value = item
      showModal.value = true
    }

    const closeModal = () => {
      showModal.value = false
      selectedItem.value = null
    }

    // Afficher l'évolution d'un poisson
    const showEvolution = async (poissonId) => {
      if (!poissonId) return
      
      try {
        const poisson = poissonsList.value.find(p => p.idPoisson === poissonId)
        if (!poisson) return
        
        selectedPoissonEvolution.value = {
          id: poissonId,
          nom: poisson.nomPoisson
        }
        
        // Charger l'historique spécifique
        const historiquePoisson = await nourrissageService.getHistoriquePoisson(poissonId)
        if (historiquePoisson && historiquePoisson.length > 0) {
          setTimeout(() => {
            createEvolutionChart(historiquePoisson)
          }, 100)
        }
      } catch (error) {
        console.error('Erreur chargement évolution:', error)
      }
    }

    // Initialiser les graphiques
    const initCharts = () => {
      if (historique.value.length === 0) return
      
      destroyCharts()
      createGainChart()
      createSatisfactionChart()
      createNourritureChart()
    }

    const destroyCharts = () => {
      [gainChart.value, satisfactionChart.value, nourritureChart.value, poissonEvolutionChart.value]
        .forEach(chart => {
          if (chart && chart.destroy) {
            chart.destroy()
          }
        })
    }

    const createGainChart = () => {
      const ctx = document.getElementById('gainChart')?.getContext('2d')
      if (!ctx) return
      
      // Grouper par date
      const gainsByDate = {}
      historique.value.forEach(item => {
        const date = item.dateNourrissageFisakafoanana
        if (!gainsByDate[date]) {
          gainsByDate[date] = { total: 0, count: 0 }
        }
        gainsByDate[date].total += item.gainPoidsFisakafoanana || 0
        gainsByDate[date].count++
      })
      
      const dates = Object.keys(gainsByDate).sort()
      const avgGains = dates.map(date => gainsByDate[date].total / gainsByDate[date].count)
      
      gainChart.value = new Chart(ctx, {
        type: 'line',
        data: {
          labels: dates.map(date => formatDate(date)),
          datasets: [{
            label: 'Gain moyen (g)',
            data: avgGains,
            borderColor: '#48bb78',
            backgroundColor: 'rgba(72, 187, 120, 0.1)',
            borderWidth: 2,
            fill: true,
            tension: 0.4
          }]
        },
        options: {
          responsive: true,
          plugins: {
            legend: { position: 'top' }
          },
          scales: {
            y: { beginAtZero: true, title: { display: true, text: 'Grammes' } },
            x: { title: { display: true, text: 'Date' } }
          }
        }
      })
    }

    const createSatisfactionChart = () => {
      const ctx = document.getElementById('satisfactionChart')?.getContext('2d')
      if (!ctx) return
      
      const categories = {
        'Excellent (100%)': 0,
        'Bon (80-99%)': 0,
        'Moyen (60-79%)': 0,
        'Faible (<60%)': 0
      }
      
      historique.value.forEach(item => {
        const taux = item.tauxSatisfactionFisakafoanana || 0
        if (taux >= 100) categories['Excellent (100%)']++
        else if (taux >= 80) categories['Bon (80-99%)']++
        else if (taux >= 60) categories['Moyen (60-79%)']++
        else categories['Faible (<60%)']++
      })
      
      satisfactionChart.value = new Chart(ctx, {
        type: 'doughnut',
        data: {
          labels: Object.keys(categories),
          datasets: [{
            data: Object.values(categories),
            backgroundColor: [
              '#48bb78', // Vert
              '#4299e1', // Bleu
              '#ed8936', // Orange
              '#f56565'  // Rouge
            ]
          }]
        },
        options: {
          responsive: true,
          plugins: {
            legend: { position: 'right' }
          }
        }
      })
    }

    const createNourritureChart = () => {
      const ctx = document.getElementById('nourritureChart')?.getContext('2d')
      if (!ctx) return
      
      // Grouper par poisson
      const nourritureByPoisson = {}
      historique.value.forEach(item => {
        const poissonName = item.poisson?.nomPoisson || 'Inconnu'
        if (!nourritureByPoisson[poissonName]) {
          nourritureByPoisson[poissonName] = 0
        }
        nourritureByPoisson[poissonName] += item.quantiteNourritureFisakafoanana || 0
      })
      
      // Prendre les 10 premiers
      const sorted = Object.entries(nourritureByPoisson)
        .sort(([, a], [, b]) => b - a)
        .slice(0, 10)
      
      nourritureChart.value = new Chart(ctx, {
        type: 'bar',
        data: {
          labels: sorted.map(([name]) => name),
          datasets: [{
            label: 'Nourriture consommée (g)',
            data: sorted.map(([, value]) => value),
            backgroundColor: '#667eea'
          }]
        },
        options: {
          responsive: true,
          indexAxis: 'y',
          plugins: {
            legend: { display: false }
          },
          scales: {
            x: { beginAtZero: true, title: { display: true, text: 'Grammes' } }
          }
        }
      })
    }

    const createEvolutionChart = (historiquePoisson) => {
      const ctx = document.getElementById('poissonEvolutionChart')?.getContext('2d')
      if (!ctx) return
      
      const dates = historiquePoisson.map(item => formatDate(item.dateNourrissageFisakafoanana))
      const poids = historiquePoisson.map(item => item.nouveauPoidsFisakafoanana)
      const gains = historiquePoisson.map(item => item.gainPoidsFisakafoanana)
      
      poissonEvolutionChart.value = new Chart(ctx, {
        type: 'line',
        data: {
          labels: dates,
          datasets: [
            {
              label: 'Poids (g)',
              data: poids,
              borderColor: '#667eea',
              backgroundColor: 'rgba(102, 126, 234, 0.1)',
              borderWidth: 2,
              yAxisID: 'y'
            },
            {
              label: 'Gain journalier (g)',
              data: gains,
              borderColor: '#48bb78',
              backgroundColor: 'rgba(72, 187, 120, 0.1)',
              borderWidth: 2,
              yAxisID: 'y1',
              type: 'bar'
            }
          ]
        },
        options: {
          responsive: true,
          interaction: {
            mode: 'index',
            intersect: false
          },
          scales: {
            y: {
              type: 'linear',
              display: true,
              position: 'left',
              title: { display: true, text: 'Poids (g)' }
            },
            y1: {
              type: 'linear',
              display: true,
              position: 'right',
              title: { display: true, text: 'Gain (g)' },
              grid: { drawOnChartArea: false }
            }
          }
        }
      })
    }

    // Basculer entre les vues
    const toggleView = () => {
      viewMode.value = viewMode.value === 'table' ? 'graph' : 'table'
      if (viewMode.value === 'graph') {
        setTimeout(() => {
          initCharts()
        }, 100)
      }
    }

    // Exporter les données
    const exportData = () => {
      const ws = XLSX.utils.json_to_sheet(historique.value.map(item => ({
        'Date': formatDate(item.dateNourrissageFisakafoanana),
        'Heure': formatTime(item.heureNourrissageFisakafoanana),
        'Poisson': item.poisson?.nomPoisson,
        'Race': item.poisson?.racePoisson?.nomRacePoisson,
        'Poids avant (g)': formatPoids(item.ancienPoidsFisakafoanana),
        'Poids après (g)': formatPoids(item.nouveauPoidsFisakafoanana),
        'Gain (g)': formatPoids(item.gainPoidsFisakafoanana),
        'Nourriture (g)': formatPoids(item.quantiteNourritureFisakafoanana),
        'Protéines (g)': formatPoids(item.proteinesRecuesFisakafoanana),
        'Glucides (g)': formatPoids(item.glucidesRecusFisakafoanana),
        'Satisfaction (%)': formatNumber(item.tauxSatisfactionFisakafoanana),
        'Besoins satisfaits': item.besoinsSatisfaitsFisakafoanana ? 'Oui' : 'Non',
        'Date création': formatDate(item.dateCreationFisakafoanana)
      })))
      
      const wb = XLSX.utils.book_new()
      XLSX.utils.book_append_sheet(wb, ws, 'Historique Nourrissage')
      
      const date = new Date().toISOString().split('T')[0]
      XLSX.writeFile(wb, `historique_nourrissage_${date}.xlsx`)
    }

    // Imprimer les détails
    const printDetails = () => {
      window.print()
    }

    // Confirmer la suppression
    const confirmDelete = (item) => {
      if (confirm(`Supprimer l'entrée du ${formatDate(item.dateNourrissageFisakafoanana)} pour ${item.poisson?.nomPoisson} ?`)) {
        // Ici, appeler l'API de suppression
        console.log('Suppression de:', item.idFisakafoanana)
        alert('Fonctionnalité de suppression à implémenter avec l\'API')
      }
    }

    // Watchers
    watch(viewMode, (newVal) => {
      if (newVal === 'graph' && historique.value.length > 0) {
        setTimeout(() => {
          initCharts()
        }, 100)
      }
    })

    watch([dateRange, selectedPoisson, statutFilter], () => {
      currentPage.value = 1
      loadHistorique()
    })

    // Cycle de vie
    onMounted(() => {
      loadInitialData()
    })

    return {
      // Données
      historique,
      poissonsList,
      filteredHistorique,
      stats,
      selectedItem,
      selectedPoissonEvolution,
      
      // Filtres et états
      dateRange,
      selectedPoisson,
      statutFilter,
      searchQuery,
      viewMode,
      sortColumn,
      sortDirection,
      currentPage,
      itemsPerPage,
      showModal,
      userRole,
      periodStart,
      periodEnd,
      totalPages,
      
      // Références
      gainChart,
      satisfactionChart,
      nourritureChart,
      poissonEvolutionChart,
      
      // Méthodes
      loadHistorique,
      nextPage,
      prevPage,
      sortBy,
      formatDate,
      formatTime,
      formatPoids,
      formatNumber,
      calculateTauxCroissance,
      getGainClass,
      getSatisfactionClass,
      getStatusClass,
      getStatusText,
      showDetails,
      closeModal,
      showEvolution,
      toggleView,
      exportData,
      printDetails,
      confirmDelete
    }
  }
}
</script>

<style scoped>
.historique {
  padding: 20px;
  background: #f7fafc;
  min-height: 100vh;
}

.header {
  background: white;
  padding: 25px;
  border-radius: 12px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.05);
  margin-bottom: 25px;
}

.header h1 {
  margin: 0 0 10px 0;
  color: #2d3748;
  font-size: 28px;
}

.subtitle {
  color: #718096;
  margin-bottom: 25px;
  font-size: 16px;
}

.filters {
  display: flex;
  gap: 15px;
  flex-wrap: wrap;
  align-items: center;
}

.filter-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.filter-group label {
  font-weight: 600;
  color: #4a5568;
  font-size: 14px;
}

.filter-group select {
  padding: 10px 15px;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  background: white;
  min-width: 180px;
  cursor: pointer;
  transition: border-color 0.2s;
}

.filter-group select:hover {
  border-color: #cbd5e0;
}

.filter-group select:focus {
  outline: none;
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.btn-refresh, .btn-export {
  padding: 10px 20px;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-weight: 600;
  transition: all 0.2s;
  align-self: flex-end;
  margin-top: 8px;
}

.btn-refresh {
  background: #4299e1;
  color: white;
}

.btn-refresh:hover {
  background: #3182ce;
}

.btn-export {
  background: #48bb78;
  color: white;
}

.btn-export:hover {
  background: #38a169;
}

/* Statistiques */
.stats-summary {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
  margin-bottom: 25px;
}

.stat-card {
  background: white;
  padding: 20px;
  border-radius: 12px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.05);
  display: flex;
  align-items: center;
  gap: 15px;
}

.stat-icon {
  font-size: 32px;
}

.stat-content h3 {
  margin: 0 0 5px 0;
  font-size: 16px;
  color: #4a5568;
}

.stat-content p {
  margin: 0;
  font-size: 24px;
  font-weight: bold;
  color: #2d3748;
}

/* Tableau */
.table-container {
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.05);
  padding: 25px;
}

.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.table-header h2 {
  margin: 0;
  color: #2d3748;
}

.table-actions {
  display: flex;
  gap: 15px;
  align-items: center;
}

.search-input {
  padding: 10px 15px;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  width: 250px;
  transition: border-color 0.2s;
}

.search-input:focus {
  outline: none;
  border-color: #667eea;
}

.btn-toggle-view {
  padding: 10px 20px;
  background: #ed8936;
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-weight: 600;
  transition: background 0.2s;
}

.btn-toggle-view:hover {
  background: #dd6b20;
}

/* Table */
.data-table {
  width: 100%;
  border-collapse: collapse;
}

.data-table th {
  background: #f7fafc;
  padding: 15px;
  text-align: left;
  font-weight: 600;
  color: #4a5568;
  border-bottom: 2px solid #e2e8f0;
  cursor: pointer;
  user-select: none;
  transition: background 0.2s;
}

.data-table th:hover {
  background: #edf2f7;
}

.data-table td {
  padding: 15px;
  border-bottom: 1px solid #e2e8f0;
  vertical-align: middle;
}

.data-table tbody tr:hover {
  background: #f7fafc;
}

/* Cellules spéciales */
.date-cell {
  display: flex;
  flex-direction: column;
}

.date-cell .date {
  font-weight: 600;
}

.date-cell .time {
  font-size: 12px;
  color: #718096;
}

.poisson-cell {
  display: flex;
  flex-direction: column;
}

.poisson-cell .poisson-name {
  font-weight: 600;
}

.poisson-cell .poisson-id {
  font-size: 12px;
  color: #718096;
}

.weight-cell {
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.weight-diff {
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 4px;
  display: inline-block;
}

.weight-diff.positive {
  background: #c6f6d5;
  color: #22543d;
}

.gain-badge {
  padding: 5px 10px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
  display: inline-block;
}

.gain-high { background: #c6f6d5; color: #22543d; }
.gain-medium { background: #fed7d7; color: #742a2a; }
.gain-low { background: #feebc8; color: #744210; }
.gain-very-low { background: #e2e8f0; color: #2d3748; }

.satisfaction-cell {
  display: flex;
  align-items: center;
  gap: 10px;
}

.satisfaction-bar {
  flex: 1;
  height: 8px;
  background: #e2e8f0;
  border-radius: 4px;
  overflow: hidden;
}

.satisfaction-fill {
  height: 100%;
  transition: width 0.3s;
}

.satisfaction-high { background: #48bb78; }
.satisfaction-medium { background: #ed8936; }
.satisfaction-low { background: #ecc94b; }
.satisfaction-very-low { background: #f56565; }

.satisfaction-text {
  min-width: 50px;
  text-align: right;
  font-weight: 600;
}

.status-badge {
  padding: 6px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
  display: inline-block;
}

.status-satisfied { background: #c6f6d5; color: #22543d; }
.status-partial { background: #feebc8; color: #744210; }
.status-unsatisfied { background: #fed7d7; color: #742a2a; }

.action-buttons {
  display: flex;
  gap: 8px;
}

.action-buttons button {
  padding: 8px 12px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 16px;
  transition: all 0.2s;
}

.btn-details { background: #4299e1; color: white; }
.btn-chart { background: #9f7aea; color: white; }
.btn-delete { background: #f56565; color: white; }

.action-buttons button:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 6px rgba(0,0,0,0.1);
}

/* Pagination */
.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 20px;
  margin-top: 30px;
  padding: 20px;
}

.pagination button {
  padding: 10px 20px;
  border: 1px solid #e2e8f0;
  background: white;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
}

.pagination button:hover:not(:disabled) {
  background: #4299e1;
  color: white;
  border-color: #4299e1;
}

.pagination button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.page-info {
  font-weight: 600;
  color: #4a5568;
}

.no-data {
  text-align: center;
  padding: 40px;
  color: #718096;
  font-size: 18px;
}

/* Vue graphique */
.graph-view {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(400px, 1fr));
  gap: 30px;
  margin-top: 20px;
}

.graph-container {
  background: white;
  padding: 25px;
  border-radius: 12px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.05);
}

.graph-container h3 {
  margin: 0 0 20px 0;
  color: #2d3748;
  text-align: center;
}

.graph-container canvas {
  max-height: 300px;
}

/* Évolution poisson */
.poisson-evolution {
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 20px rgba(0,0,0,0.1);
  margin-top: 30px;
  padding: 25px;
}

.evolution-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.evolution-header h3 {
  margin: 0;
  color: #2d3748;
}

.btn-close {
  background: none;
  border: none;
  font-size: 24px;
  cursor: pointer;
  color: #718096;
}

.btn-close:hover {
  color: #f56565;
}

.evolution-chart {
  height: 400px;
}

/* Modal */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0,0,0,0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
  padding: 20px;
}

.modal-content {
  background: white;
  border-radius: 12px;
  max-width: 800px;
  width: 100%;
  max-height: 90vh;
  overflow-y: auto;
  animation: modalSlide 0.3s ease;
}

@keyframes modalSlide {
  from {
    opacity: 0;
    transform: translateY(-20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 25px;
  border-bottom: 1px solid #e2e8f0;
}

.modal-header h2 {
  margin: 0;
  color: #2d3748;
}

.modal-close {
  background: none;
  border: none;
  font-size: 28px;
  cursor: pointer;
  color: #718096;
  line-height: 1;
}

.modal-close:hover {
  color: #f56565;
}

.modal-body {
  padding: 25px;
}

.detail-section {
  margin-bottom: 30px;
}

.detail-section h3 {
  margin: 0 0 20px 0;
  color: #2d3748;
  font-size: 18px;
  padding-bottom: 10px;
  border-bottom: 2px solid #e2e8f0;
}

.detail-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 20px;
}

.detail-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.detail-item label {
  font-weight: 600;
  color: #4a5568;
  font-size: 14px;
}

.detail-item span {
  color: #2d3748;
  font-size: 16px;
}

.gain-positive {
  color: #48bb78;
  font-weight: bold;
}

.satisfaction-detail {
  background: #f7fafc;
  padding: 25px;
  border-radius: 8px;
}

.satisfaction-bar-large {
  height: 20px;
  background: #e2e8f0;
  border-radius: 10px;
  overflow: hidden;
  position: relative;
  margin-bottom: 20px;
}

.satisfaction-fill-large {
  height: 100%;
  transition: width 1s ease;
}

.satisfaction-text-large {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  font-weight: bold;
  color: white;
  text-shadow: 0 1px 2px rgba(0,0,0,0.2);
}

.satisfaction-info p {
  margin: 10px 0;
  color: #4a5568;
}

.plat-info {
  background: white;
  padding: 20px;
  border-radius: 8px;
  border: 1px solid #e2e8f0;
}

.plat-info p {
  margin: 10px 0;
}

.modal-footer {
  padding: 20px 25px;
  border-top: 1px solid #e2e8f0;
  display: flex;
  justify-content: flex-end;
  gap: 15px;
}

.modal-footer button {
  padding: 12px 24px;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-weight: 600;
  transition: all 0.2s;
}

.btn-secondary {
  background: #e2e8f0;
  color: #4a5568;
}

.btn-secondary:hover {
  background: #cbd5e0;
}

.btn-primary {
  background: #4299e1;
  color: white;
}

.btn-primary:hover {
  background: #3182ce;
}

/* Responsive */
@media (max-width: 1024px) {
  .filters {
    flex-direction: column;
    align-items: stretch;
  }
  
  .filter-group select {
    width: 100%;
  }
  
  .table-actions {
    flex-direction: column;
    width: 100%;
  }
  
  .search-input {
    width: 100%;
  }
  
  .graph-view {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .data-table {
    display: block;
    overflow-x: auto;
  }
  
  .stats-summary {
    grid-template-columns: 1fr;
  }
  
  .detail-grid {
    grid-template-columns: 1fr;
  }
  
  .modal-content {
    margin: 10px;
    max-height: 80vh;
  }
}
</style>

