<template>
  <div class="historique">
    <!-- Header avec titre et filtres -->
    <div class="header">
      <h1>📜 Historique des Nourrissages</h1>
      <p class="subtitle">Suivi complet de la croissance et de la nutrition des poissons</p>
      
      <div class="filters">
        <div class="filter-group">
          <label for="date-range">Période :</label>
          <select v-model="dateRange" @change="onDateRangeChange">
            <option value="7">7 derniers jours</option>
            <option value="30">30 derniers jours</option>
            <option value="90">3 derniers mois</option>
            <option value="365">1 an</option>
            <option value="custom">Personnalisée</option>
            <option value="all">Tout l'historique</option>
          </select>
        </div>
        
        <!-- Filtres par date personnalisée -->
        <div v-if="dateRange === 'custom'" class="date-range-filters">
          <div class="filter-group">
            <label for="date-min">Date début :</label>
            <input 
              type="date" 
              v-model="dateMin" 
              @change="loadHistorique"
              id="date-min"
              class="date-input"
            />
          </div>
          <div class="filter-group">
            <label for="date-max">Date fin :</label>
            <input 
              type="date" 
              v-model="dateMax" 
              @change="loadHistorique"
              id="date-max"
              class="date-input"
            />
          </div>
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
          <label for="bassin-filter">Bassin :</label>
          <select v-model="selectedBassin" @change="loadHistorique">
            <option value="">Tous les bassins</option>
            <option v-for="bassin in bassinsList" :key="bassin.idPiscine" :value="bassin.idPiscine">
              {{ bassin.nomPiscine }} ({{ getNombrePoissons(bassin) }} poissons)
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
        
        <!-- Bouton pour réinitialiser tous les filtres -->
        <button class="btn-reset" @click="resetFilters" title="Réinitialiser tous les filtres">
          ♻️ Réinitialiser
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
            placeholder="Rechercher par nom, race, bassin..." 
            class="search-input"
            @input="onSearchInput"
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
              <th @click="sortBy('bassin')">
                Bassin 🏊
                <span v-if="sortColumn === 'bassin'">
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
              <td>
                <div class="bassin-cell" v-if="item.poisson?.piscineActuelle">
                  <span class="bassin-name">{{ item.poisson.piscineActuelle.nomPiscine }}</span>
                  <span class="bassin-id">#{{ item.poisson.piscineActuelle.idPiscine }}</span>
                </div>
                <span v-else class="no-bassin">Non affecté</span>
              </td>
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
          <canvas id="gainChart" ref="gainChart"></canvas>
        </div>
        
        <div class="graph-container">
          <h3>Distribution des taux de satisfaction</h3>
          <canvas id="satisfactionChart" ref="satisfactionChart"></canvas>
        </div>
        
        <div class="graph-container">
          <h3>Consommation de nourriture</h3>
          <canvas id="nourritureChart" ref="nourritureChart"></canvas>
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
        <canvas id="poissonEvolutionChart" ref="poissonEvolutionChart"></canvas>
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
                <label>Bassin :</label>
                <span v-if="selectedItem.poisson?.piscineActuelle">
                  {{ selectedItem.poisson.piscineActuelle.nomPiscine }} (ID: {{ selectedItem.poisson.piscineActuelle.idPiscine }})
                </span>
                <span v-else class="no-bassin">Non affecté</span>
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
import { ref, computed, onMounted, watch, nextTick } from 'vue'
import { Chart, registerables } from 'chart.js'
import nourrissageService from '../services/nourrissageService'
import poissonService from '../services/poissonService'
import bassinService from '../services/bassinService'
import * as XLSX from 'xlsx'
import { calculService } from '../services/calculService'

Chart.register(...registerables)

export default {
  name: 'Historique',
  
  setup() {
    // État réactif
    const historique = ref([])
    const poissonsList = ref([])
    const bassinsList = ref([])
    const dateRange = ref('30')
    const dateMin = ref('')
    const dateMax = ref('')
    const selectedPoisson = ref('')
    const selectedBassin = ref('')
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
    const userRole = ref('UTILISATEUR')
    
    // Références pour les graphiques
    const gainChart = ref(null)
    const satisfactionChart = ref(null)
    const nourritureChart = ref(null)
    const poissonEvolutionChart = ref(null)
    
    // Graphiques instances
    let gainChartInstance = null
    let satisfactionChartInstance = null
    let nourritureChartInstance = null
    let poissonEvolutionChartInstance = null
    
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

    // Initialiser les dates par défaut
    const initDefaultDates = () => {
      const today = new Date()
      const thirtyDaysAgo = new Date()
      thirtyDaysAgo.setDate(today.getDate() - 30)
      
      dateMin.value = formatDateForInput(thirtyDaysAgo)
      dateMax.value = formatDateForInput(today)
    }

    // Formater la date pour l'input type="date"
    const formatDateForInput = (date) => {
      const year = date.getFullYear()
      const month = String(date.getMonth() + 1).padStart(2, '0')
      const day = String(date.getDate()).padStart(2, '0')
      return `${year}-${month}-${day}`
    }

    // Charger les données initiales
    const loadInitialData = async () => {
      try {
        console.log('Chargement des données initiales...')
        
        // Initialiser les dates par défaut
        initDefaultDates()
        
        // Charger la liste des poissons avec leurs bassins
        const poissons = await poissonService.getPoissonsEnVie()
        poissonsList.value = poissons
        stats.value.totalPoissons = poissons.length
        
        // Charger la liste des bassins avec leurs poissons
        const bassins = await bassinService.getAll()
        bassinsList.value = bassins
        
        console.log('Bassins chargés:', bassinsList.value)
        console.log('Poissons chargés:', poissonsList.value)
        
        // Charger l'historique
        await loadHistorique()
        
      } catch (error) {
        console.error('Erreur chargement données initiales:', error)
        alert('Impossible de charger les données. Vérifiez votre connexion.')
      }
    }

    // Gérer le changement de période
    const onDateRangeChange = () => {
      if (dateRange.value !== 'custom') {
        const today = new Date()
        const startDate = new Date()
        
        if (dateRange.value !== 'all') {
          const days = parseInt(dateRange.value)
          startDate.setDate(startDate.getDate() - days)
        } else {
          // Pour "tout l'historique", prendre il y a 2 ans
          startDate.setFullYear(startDate.getFullYear() - 2)
        }
        
        dateMin.value = formatDateForInput(startDate)
        dateMax.value = formatDateForInput(today)
      }
      
      loadHistorique()
    }

    // Méthode utilitaire pour compter les poissons dans un bassin
    const getNombrePoissons = (bassin) => {
      if (!bassin.poissons || !Array.isArray(bassin.poissons)) {
        // Essayer de compter depuis la liste des poissons
        const poissonsDansBassin = poissonsList.value.filter(p => 
          p.piscineActuelle && p.piscineActuelle.idPiscine === bassin.idPiscine
        )
        return poissonsDansBassin.length
      }
      return bassin.poissons.length
    }

    // Charger l'historique
    const loadHistorique = async () => {
      try {
        console.log('Chargement de l\'historique...')
        console.log('Filtres actuels:', {
          selectedBassin: selectedBassin.value,
          selectedPoisson: selectedPoisson.value,
          dateMin: dateMin.value,
          dateMax: dateMax.value
        })
        
        // Mettre à jour les dates de période pour l'affichage
        periodStart.value = new Date(dateMin.value)
        periodEnd.value = new Date(dateMax.value)
        
        let allData = []
        
        // Si un bassin spécifique est sélectionné
        if (selectedBassin.value) {
          console.log(`Filtrage par bassin ID: ${selectedBassin.value}`)
          
          // Récupérer les poissons de ce bassin
          const poissonsDuBassin = poissonsList.value.filter(poisson => 
            poisson.piscineActuelle && poisson.piscineActuelle.idPiscine === parseInt(selectedBassin.value)
          )
          
          console.log(`Poissons trouvés dans le bassin: ${poissonsDuBassin.length}`)
          
          // Récupérer l'historique pour chaque poisson du bassin
          for (const poisson of poissonsDuBassin) {
            try {
              const data = await nourrissageService.getHistoriquePoisson(poisson.idPoisson)
              if (Array.isArray(data)) {
                // Filtrer par date côté client
                const filteredData = data.filter(item => {
                  const itemDate = new Date(item.dateNourrissageFisakafoanana)
                  
                  if (dateMin.value && itemDate < new Date(dateMin.value)) {
                    return false
                  }
                  if (dateMax.value) {
                    const maxDate = new Date(dateMax.value)
                    maxDate.setHours(23, 59, 59, 999)
                    if (itemDate > maxDate) {
                      return false
                    }
                  }
                  return true
                })
                
                allData.push(...filteredData)
              }
            } catch (error) {
              console.warn(`Pas d'historique pour le poisson ${poisson.idPoisson}:`, error)
            }
          }
        } 
        // Si un poisson spécifique est sélectionné
        else if (selectedPoisson.value) {
          console.log(`Chargement historique pour poisson ID: ${selectedPoisson.value}`)
          const data = await nourrissageService.getHistoriquePoisson(selectedPoisson.value)
          if (Array.isArray(data)) {
            // Filtrer par date côté client
            allData = data.filter(item => {
              const itemDate = new Date(item.dateNourrissageFisakafoanana)
              
              if (dateMin.value && itemDate < new Date(dateMin.value)) {
                return false
              }
              if (dateMax.value) {
                const maxDate = new Date(dateMax.value)
                maxDate.setHours(23, 59, 59, 999)
                if (itemDate > maxDate) {
                  return false
                }
              }
              return true
            })
          }
        } 
        // Sinon, tout l'historique
        else {
          console.log('Chargement de tout l\'historique')
          for (const poisson of poissonsList.value) {
            try {
              const data = await nourrissageService.getHistoriquePoisson(poisson.idPoisson)
              if (Array.isArray(data)) {
                // Filtrer par date côté client
                const filteredData = data.filter(item => {
                  const itemDate = new Date(item.dateNourrissageFisakafoanana)
                  
                  if (dateMin.value && itemDate < new Date(dateMin.value)) {
                    return false
                  }
                  if (dateMax.value) {
                    const maxDate = new Date(dateMax.value)
                    maxDate.setHours(23, 59, 59, 999)
                    if (itemDate > maxDate) {
                      return false
                    }
                  }
                  return true
                })
                
                allData.push(...filteredData)
              }
            } catch (error) {
              console.warn(`Pas d'historique pour le poisson ${poisson.idPoisson}:`, error)
            }
          }
        }
        
        historique.value = allData
        console.log(`Historique chargé: ${historique.value.length} entrées`)
        
        // Calculer les statistiques
        calculateStats()
        
        // Initialiser les graphiques si on est en mode graphique
        if (viewMode.value === 'graph') {
          await nextTick()
          initCharts()
        }
        
      } catch (error) {
        console.error('Erreur chargement historique:', error)
        historique.value = []
      }
    }

    // Réinitialiser tous les filtres
    const resetFilters = () => {
      dateRange.value = '30'
      selectedPoisson.value = ''
      selectedBassin.value = ''
      statutFilter.value = ''
      searchQuery.value = ''
      currentPage.value = 1
      
      initDefaultDates()
      loadHistorique()
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

    // Filtrer et trier les données (filtrage côté client pour la recherche)
    const filteredHistorique = computed(() => {
      let filtered = [...historique.value]
      
      // Filtre de recherche textuelle
      if (searchQuery.value.trim()) {
        const query = searchQuery.value.toLowerCase().trim()
        filtered = filtered.filter(item => {
          const poissonName = item.poisson?.nomPoisson?.toLowerCase() || ''
          const raceName = item.poisson?.racePoisson?.nomRacePoisson?.toLowerCase() || ''
          const bassinName = item.poisson?.piscineActuelle?.nomPiscine?.toLowerCase() || ''
          return poissonName.includes(query) || raceName.includes(query) || bassinName.includes(query)
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
          case 'bassin':
            aValue = a.poisson?.piscineActuelle?.nomPiscine || ''
            bValue = b.poisson?.piscineActuelle?.nomPiscine || ''
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

    // Gestion de la recherche
    const onSearchInput = () => {
      currentPage.value = 1
    }

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
      
      const taux = ((item.nouveauPoidsFisakafoanana - item.ancienPoidsFisakafoanana) / item.ancienPoidsFisakafoanana) * 100
      return Math.round(taux * 100) / 100
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
          await nextTick()
          createEvolutionChart(historiquePoisson)
        }
      } catch (error) {
        console.error('Erreur chargement évolution:', error)
      }
    }

    // Initialiser les graphiques
    const initCharts = () => {
      console.log('Initialisation des graphiques...')
      
      if (historique.value.length === 0) {
        console.log('Aucune donnée pour les graphiques')
        return
      }
      
      destroyCharts()
      createGainChart()
      createSatisfactionChart()
      createNourritureChart()
    }

    const destroyCharts = () => {
      console.log('Destruction des graphiques existants...')
      
      const charts = [gainChartInstance, satisfactionChartInstance, nourritureChartInstance, poissonEvolutionChartInstance]
      charts.forEach(chart => {
        if (chart && typeof chart.destroy === 'function') {
          try {
            chart.destroy()
          } catch (e) {
            console.warn('Erreur lors de la destruction d\'un graphique:', e)
          }
        }
      })
      
      gainChartInstance = null
      satisfactionChartInstance = null
      nourritureChartInstance = null
      poissonEvolutionChartInstance = null
    }

    const createGainChart = () => {
      const canvas = document.getElementById('gainChart')
      if (!canvas) {
        console.error('Canvas gainChart non trouvé')
        return
      }
      
      const ctx = canvas.getContext('2d')
      if (!ctx) {
        console.error('Contexte canvas non disponible')
        return
      }
      
      try {
        // Grouper par date
        const gainsByDate = {}
        historique.value.forEach(item => {
          const date = item.dateNourrissageFisakafoanana
          if (!date) return
          
          const dateKey = formatDate(date) // Utiliser la date formatée comme clé
          if (!gainsByDate[dateKey]) {
            gainsByDate[dateKey] = { total: 0, count: 0 }
          }
          gainsByDate[dateKey].total += item.gainPoidsFisakafoanana || 0
          gainsByDate[dateKey].count++
        })
        
        const dates = Object.keys(gainsByDate).sort()
        const avgGains = dates.map(date => {
          const data = gainsByDate[date]
          return data.count > 0 ? data.total / data.count : 0
        })
        
        console.log('Données graphique gain:', { dates, avgGains })
        
        gainChartInstance = new Chart(ctx, {
          type: 'line',
          data: {
            labels: dates,
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
            maintainAspectRatio: false,
            plugins: {
              legend: { position: 'top' }
            },
            scales: {
              y: { 
                beginAtZero: true, 
                title: { display: true, text: 'Grammes' },
                ticks: {
                  callback: function(value) {
                    return value + 'g'
                  }
                }
              },
              x: { 
                title: { display: true, text: 'Date' },
                ticks: {
                  maxRotation: 45,
                  minRotation: 45
                }
              }
            }
          }
        })
        
        console.log('Graphique gain créé avec succès')
      } catch (error) {
        console.error('Erreur création graphique gain:', error)
      }
    }

    const createSatisfactionChart = () => {
      const canvas = document.getElementById('satisfactionChart')
      if (!canvas) {
        console.error('Canvas satisfactionChart non trouvé')
        return
      }
      
      const ctx = canvas.getContext('2d')
      if (!ctx) {
        console.error('Contexte canvas non disponible')
        return
      }
      
      try {
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
        
        console.log('Données graphique satisfaction:', categories)
        
        satisfactionChartInstance = new Chart(ctx, {
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
              ],
              borderWidth: 1
            }]
          },
          options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
              legend: { 
                position: 'right',
                labels: {
                  padding: 20
                }
              },
              tooltip: {
                callbacks: {
                  label: function(context) {
                    const label = context.label || ''
                    const value = context.raw || 0
                    const total = context.dataset.data.reduce((a, b) => a + b, 0)
                    const percentage = Math.round((value / total) * 100)
                    return `${label}: ${value} (${percentage}%)`
                  }
                }
              }
            }
          }
        })
        
        console.log('Graphique satisfaction créé avec succès')
      } catch (error) {
        console.error('Erreur création graphique satisfaction:', error)
      }
    }

    const createNourritureChart = () => {
      const canvas = document.getElementById('nourritureChart')
      if (!canvas) {
        console.error('Canvas nourritureChart non trouvé')
        return
      }
      
      const ctx = canvas.getContext('2d')
      if (!ctx) {
        console.error('Contexte canvas non disponible')
        return
      }
      
      try {
        // Grouper par poisson
        const nourritureByPoisson = {}
        historique.value.forEach(item => {
          const poissonName = item.poisson?.nomPoisson || 'Inconnu'
          if (!nourritureByPoisson[poissonName]) {
            nourritureByPoisson[poissonName] = 0
          }
          nourritureByPoisson[poissonName] += item.quantiteNourritureFisakafoanana || 0
        })
        
        // Prendre les 10 premiers ou tous si moins de 10
        const sorted = Object.entries(nourritureByPoisson)
          .sort(([, a], [, b]) => b - a)
          .slice(0, 10)
        
        console.log('Données graphique nourriture:', sorted)
        
        nourritureChartInstance = new Chart(ctx, {
          type: 'bar',
          data: {
            labels: sorted.map(([name]) => name),
            datasets: [{
              label: 'Nourriture consommée (g)',
              data: sorted.map(([, value]) => value),
              backgroundColor: '#667eea',
              borderWidth: 1
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
                title: { display: true, text: 'Grammes' },
                ticks: {
                  callback: function(value) {
                    return value + 'g'
                  }
                }
              }
            }
          }
        })
        
        console.log('Graphique nourriture créé avec succès')
      } catch (error) {
        console.error('Erreur création graphique nourriture:', error)
      }
    }

    const createEvolutionChart = (historiquePoisson) => {
      const canvas = document.getElementById('poissonEvolutionChart')
      if (!canvas) {
        console.error('Canvas poissonEvolutionChart non trouvé')
        return
      }
      
      const ctx = canvas.getContext('2d')
      if (!ctx) {
        console.error('Contexte canvas non disponible')
        return
      }
      
      try {
        // Trier par date
        const sortedHistorique = [...historiquePoisson].sort((a, b) => 
          new Date(a.dateNourrissageFisakafoanana) - new Date(b.dateNourrissageFisakafoanana)
        )
        
        const dates = sortedHistorique.map(item => formatDate(item.dateNourrissageFisakafoanana))
        const poids = sortedHistorique.map(item => item.nouveauPoidsFisakafoanana)
        const gains = sortedHistorique.map(item => item.gainPoidsFisakafoanana)
        
        console.log('Données graphique évolution:', { dates, poids, gains })
        
        poissonEvolutionChartInstance = new Chart(ctx, {
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
                yAxisID: 'y',
                fill: true
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
            maintainAspectRatio: false,
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
        
        console.log('Graphique évolution créé avec succès')
      } catch (error) {
        console.error('Erreur création graphique évolution:', error)
      }
    }

    // Basculer entre les vues
    const toggleView = async () => {
      viewMode.value = viewMode.value === 'table' ? 'graph' : 'table'
      if (viewMode.value === 'graph') {
        await nextTick()
        initCharts()
      }
    }

    // Exporter les données
    const exportData = () => {
      try {
        const dataToExport = historique.value.map(item => ({
          'Date': formatDate(item.dateNourrissageFisakafoanana),
          'Heure': formatTime(item.heureNourrissageFisakafoanana),
          'Poisson': item.poisson?.nomPoisson || 'N/A',
          'Race': item.poisson?.racePoisson?.nomRacePoisson || 'Non défini',
          'Bassin': item.poisson?.piscineActuelle?.nomPiscine || 'Non affecté',
          'ID Bassin': item.poisson?.piscineActuelle?.idPiscine || '',
          'Poids avant (g)': formatPoids(item.ancienPoidsFisakafoanana),
          'Poids après (g)': formatPoids(item.nouveauPoidsFisakafoanana),
          'Gain (g)': formatPoids(item.gainPoidsFisakafoanana),
          'Nourriture (g)': formatPoids(item.quantiteNourritureFisakafoanana),
          'Protéines (g)': formatPoids(item.proteinesRecuesFisakafoanana),
          'Glucides (g)': formatPoids(item.glucidesRecusFisakafoanana),
          'Satisfaction (%)': formatNumber(item.tauxSatisfactionFisakafoanana),
          'Besoins satisfaits': item.besoinsSatisfaitsFisakafoanana ? 'Oui' : 'Non',
          'Date création': formatDate(item.dateCreationFisakafoanana)
        }))
        
        const ws = XLSX.utils.json_to_sheet(dataToExport)
        const wb = XLSX.utils.book_new()
        XLSX.utils.book_append_sheet(wb, ws, 'Historique Nourrissage')
        
        const date = new Date().toISOString().split('T')[0]
        XLSX.writeFile(wb, `historique_nourrissage_${date}.xlsx`)
      } catch (error) {
        console.error('Erreur export Excel:', error)
        alert('Erreur lors de l\'export Excel')
      }
    }

    // Imprimer les détails
    const printDetails = () => {
      window.print()
    }

    // Confirmer la suppression
    const confirmDelete = (item) => {
      if (confirm(`Supprimer l'entrée du ${formatDate(item.dateNourrissageFisakafoanana)} pour ${item.poisson?.nomPoisson} ?`)) {
        console.log('Suppression de:', item.idFisakafoanana)
        alert('Fonctionnalité de suppression à implémenter avec l\'API')
      }
    }

    // Watchers
    watch(viewMode, (newVal) => {
      if (newVal === 'graph') {
        nextTick(() => {
          initCharts()
        })
      }
    })

    watch([dateRange, selectedPoisson, selectedBassin, statutFilter], () => {
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
      bassinsList,
      filteredHistorique,
      stats,
      selectedItem,
      selectedPoissonEvolution,
      
      // Filtres et états
      dateRange,
      dateMin,
      dateMax,
      selectedPoisson,
      selectedBassin,
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
      
      // Méthodes utilitaires
      getNombrePoissons,
      
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
      confirmDelete,
      onDateRangeChange,
      onSearchInput,
      resetFilters
    }
  }
}
</script>

<style scoped>
@import '../assets/styles/historique.css';
</style>



