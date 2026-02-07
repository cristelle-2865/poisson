<template>
  <div class="poissons-list">
    <!-- En-tête de page -->
    <div class="page-header">
      <div class="header-left">
        <h1>📋 Liste des Poissons</h1>
        <p class="subtitle">Gérez tous vos poissons dans un seul endroit</p>
      </div>
      <div class="header-right">
        <router-link to="/poissons/nouveau" class="btn-primary">
          ➕ Ajouter un poisson
        </router-link>
        <button @click="exportToCSV" class="btn-secondary" :disabled="filteredPoissons.length === 0">
          📥 Exporter CSV
        </button>
      </div>
    </div>

    <!-- Statistiques rapides -->
    <div class="stats-cards">
      <div class="stat-card">
        <div class="stat-icon">🐟</div>
        <div class="stat-content">
          <h3>Total poissons</h3>
          <p class="stat-value">{{ poissons.length }}</p>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon">🏊</div>
        <div class="stat-content">
          <h3>Avec bassin</h3>
          <p class="stat-value">{{ poissonsAvecBassin }}</p>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon">💰</div>
        <div class="stat-content">
          <h3>Prêts à vendre</h3>
          <p class="stat-value">{{ poissonsPretAVendre }}</p>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon">⚖️</div>
        <div class="stat-content">
          <h3>Poids moyen</h3>
          <p class="stat-value">{{ poidsMoyen }}g</p>
        </div>
      </div>
    </div>

    <!-- Filtres -->
    <div class="filters-section">
      <div class="filters-header">
        <h2>🔍 Filtres et Recherche</h2>
        <button @click="resetFilters" class="btn-link">
          🔄 Réinitialiser les filtres
        </button>
      </div>
      
      <div class="filters-grid">
        <!-- Recherche par nom -->
        <div class="filter-group">
          <label for="search">Recherche</label>
          <input
            type="text"
            id="search"
            v-model="filters.search"
            placeholder="Nom du poisson..."
            class="search-input"
          />
        </div>

        <!-- Filtre par race -->
        <div class="filter-group">
          <label for="race">Race</label>
          <select id="race" v-model="filters.race" class="filter-select">
            <option value="">Toutes les races</option>
            <option v-for="race in races" :key="race.idRacePoisson" :value="race.idRacePoisson">
              {{ race.nomRacePoisson }}
            </option>
          </select>
        </div>

        <!-- Filtre par bassin -->
        <div class="filter-group">
          <label for="bassin">Bassin</label>
          <select id="bassin" v-model="filters.bassin" class="filter-select">
            <option value="">Tous les bassins</option>
            <option v-for="bassin in bassins" :key="bassin.idPiscine" :value="bassin.idPiscine">
              {{ bassin.nomPiscine }}
            </option>
            <option value="sans-bassin">Sans bassin</option>
          </select>
        </div>

        <!-- Filtre par statut -->
        <div class="filter-group">
          <label for="status">Statut</label>
          <select id="status" v-model="filters.status" class="filter-select">
            <option value="">Tous les statuts</option>
            <option value="en-vie">En vie</option>
            <option value="pret-a-vendre">Prêt à vendre</option>
            <option value="affame">Affamé</option>
            <option value="vendu">Vendu</option>
            <option value="mort">Mort</option>
          </select>
        </div>

        <!-- Filtre par progression -->
        <div class="filter-group">
          <label for="progression">Progression</label>
          <select id="progression" v-model="filters.progression" class="filter-select">
            <option value="">Toute progression</option>
            <option value="0-25">0-25%</option>
            <option value="25-50">25-50%</option>
            <option value="50-75">50-75%</option>
            <option value="75-95">75-95%</option>
            <option value="95-100">95-100% (Prêt)</option>
          </select>
        </div>

        <!-- Tri -->
        <div class="filter-group">
          <label for="sort">Trier par</label>
          <select id="sort" v-model="sortBy" class="filter-select">
            <option value="dateArriveeDesc">Date d'arrivée ▼</option>
            <option value="dateArriveeAsc">Date d'arrivée ▲</option>
            <option value="nomAsc">Nom A-Z</option>
            <option value="nomDesc">Nom Z-A</option>
            <option value="poidsDesc">Poids ▼</option>
            <option value="poidsAsc">Poids ▲</option>
            <option value="progressionDesc">Progression ▼</option>
            <option value="progressionAsc">Progression ▲</option>
          </select>
        </div>
      </div>

      <!-- Info filtres actifs -->
      <div v-if="filtersActifs.length > 0" class="active-filters">
        <span class="active-filters-label">Filtres actifs:</span>
        <div class="filter-chips">
          <span v-for="filter in filtersActifs" :key="filter.label" class="filter-chip">
            {{ filter.label }}: {{ filter.value }}
            <button @click="removeFilter(filter.key)" class="filter-chip-remove">×</button>
          </span>
        </div>
      </div>
    </div>

    <!-- Tableau des poissons -->
    <div class="table-container">
      <div class="table-header">
        <div class="table-info">
          <span class="result-count">
            {{ filteredPoissons.length }} poisson{{ filteredPoissons.length !== 1 ? 's' : '' }} trouvé{{ filteredPoissons.length !== 1 ? 's' : '' }}
          </span>
        </div>
        <div class="table-actions">
          <button @click="refreshData" class="btn-icon" title="Actualiser">
            🔄
          </button>
        </div>
      </div>

      <div class="table-responsive">
        <table class="poissons-table">
          <thead>
            <tr>
              <th @click="sortByColumn('nomPoisson')" class="sortable">
                Nom
                <span v-if="sortColumn === 'nomPoisson'" class="sort-indicator">
                  {{ sortDirection === 'asc' ? '▲' : '▼' }}
                </span>
              </th>
              <th>Race</th>
              <th>Bassin</th>
              <th @click="sortByColumn('poidsActuelPoisson')" class="sortable">
                Poids actuel
                <span v-if="sortColumn === 'poidsActuelPoisson'" class="sort-indicator">
                  {{ sortDirection === 'asc' ? '▲' : '▼' }}
                </span>
              </th>
              <th>Poids max</th>
              <th @click="sortByColumn('progression')" class="sortable">
                Progression
                <span v-if="sortColumn === 'progression'" class="sort-indicator">
                  {{ sortDirection === 'asc' ? '▲' : '▼' }}
                </span>
              </th>
              <th>Date d'arrivée</th>
              <th>Statut</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="poisson in paginatedPoissons" :key="poisson.idPoisson" class="table-row">
              <td class="cell-name">
                <div class="name-container">
                  <span class="fish-icon">🐟</span>
                  <div class="name-details">
                    <strong>{{ poisson.nomPoisson }}</strong>
                    <small class="text-muted">ID: {{ poisson.idPoisson }}</small>
                  </div>
                </div>
              </td>
              <td>
                <span class="race-badge" :style="{ backgroundColor: getRaceColor(poisson.racePoisson?.idRacePoisson) }">
                  {{ poisson.racePoisson?.nomRacePoisson || 'Non défini' }}
                </span>
              </td>
              <td>
                <div class="bassin-info" v-if="poisson.piscineActuelle">
                  <span class="bassin-icon">🏊</span>
                  <div>
                    <strong>{{ poisson.piscineActuelle.nomPiscine }}</strong>
                    <small class="text-muted">
                      {{ poisson.piscineActuelle.poissons?.length || 0 }}/{{ poisson.piscineActuelle.capaciteMaxPiscine }}
                    </small>
                  </div>
                </div>
                <span v-else class="no-bassin">❌ Sans bassin</span>
              </td>
              <td>
                <div class="weight-info">
                  <strong>{{ formatWeight(poisson.poidsActuelPoisson) }}g</strong>
                  <small class="text-muted">
                    Gain: +{{ formatWeight(poisson.poidsActuelPoisson - (poisson.poidsInitialPoisson || 0)) }}g
                  </small>
                </div>
              </td>
              <td>
                <div class="weight-info">
                  <strong>{{ formatWeight(poisson.poidsMaximalPoisson) }}g</strong>
                  <small class="text-muted">
                    Reste: {{ formatWeight(poisson.poidsMaximalPoisson - poisson.poidsActuelPoisson) }}g
                  </small>
                </div>
              </td>
              <td>
                <div class="progression-cell">
                  <div class="progression-bar-container">
                    <div 
                      class="progression-bar" 
                      :style="{ width: getProgression(poisson) + '%' }"
                      :class="getProgressionClass(getProgression(poisson))"
                    ></div>
                  </div>
                  <span class="progression-text">{{ getProgression(poisson).toFixed(1) }}%</span>
                </div>
              </td>
              <td>
                <div class="date-cell">
                  <strong>{{ formatDate(poisson.dateArriveePoisson) }}</strong>
                  <small class="text-muted">
                    {{ getDaysSinceArrival(poisson.dateArriveePoisson) }} jour{{ getDaysSinceArrival(poisson.dateArriveePoisson) !== 1 ? 's' : '' }}
                  </small>
                </div>
              </td>
              <td>
                <div class="status-container">
                  <span class="status-badge" :class="getStatusClass(poisson)">
                    {{ getStatusText(poisson) }}
                  </span>
                  <div class="status-icons">
                    <span v-if="poisson.estRassasiePoisson" title="Rassasié" class="status-icon">🍽️</span>
                    <span v-else title="Affamé" class="status-icon">😋</span>
                    <span v-if="poisson.estEnViePoisson" title="En vie" class="status-icon">❤️</span>
                    <span v-else title="Mort" class="status-icon">💀</span>
                  </div>
                </div>
              </td>
              <td>
                <div class="action-buttons">
                  <router-link 
                    :to="{ name: 'DetailPoisson', params: { id: poisson.idPoisson } }" 
                    class="btn-action btn-view"
                    title="Voir détails"
                  >
                    👁️
                  </router-link>
                  <router-link 
                    :to="{ name: 'ModifierPoisson', params: { id: poisson.idPoisson } }" 
                    class="btn-action btn-edit"
                    title="Modifier"
                  >
                    ✏️
                  </router-link>
                  <button 
                    v-if="poisson.estPretAVendre && !poisson.estVenduPoisson && poisson.estEnViePoisson"
                    @click="vendrePoisson(poisson)"
                    class="btn-action btn-sell"
                    title="Vendre"
                  >
                    💰
                  </button>
                  <button 
                    @click="confirmDelete(poisson)"
                    class="btn-action btn-delete"
                    title="Supprimer"
                  >
                    🗑️
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- Message si aucun résultat -->
      <div v-if="filteredPoissons.length === 0" class="no-results">
        <div class="no-results-icon">🐟</div>
        <h3>Aucun poisson trouvé</h3>
        <p>Essayez de modifier vos filtres ou ajoutez un nouveau poisson.</p>
        <router-link to="/poissons/nouveau" class="btn-primary">
          ➕ Ajouter un poisson
        </router-link>
      </div>

      <!-- Pagination -->
      <div v-if="filteredPoissons.length > itemsPerPage" class="pagination">
        <button 
          @click="currentPage--" 
          :disabled="currentPage === 1"
          class="pagination-btn"
        >
          ← Précédent
        </button>
        
        <div class="pagination-pages">
          <span 
            v-for="page in pages" 
            :key="page"
            @click="currentPage = page"
            :class="{ active: currentPage === page }"
            class="page-number"
          >
            {{ page }}
          </span>
        </div>
        
        <button 
          @click="currentPage++" 
          :disabled="currentPage === totalPages"
          class="pagination-btn"
        >
          Suivant →
        </button>
        
        <div class="pagination-info">
          <span>Page {{ currentPage }} sur {{ totalPages }}</span>
          <select v-model="itemsPerPage" class="page-size-select">
            <option value="10">10 par page</option>
            <option value="25">25 par page</option>
            <option value="50">50 par page</option>
            <option value="100">100 par page</option>
          </select>
        </div>
      </div>
    </div>

    <!-- Modale de suppression -->
    <div v-if="showDeleteModal" class="modal-overlay">
      <div class="modal">
        <div class="modal-header">
          <h3>Confirmer la suppression</h3>
          <button @click="showDeleteModal = false" class="modal-close">×</button>
        </div>
        <div class="modal-body">
          <p>Êtes-vous sûr de vouloir supprimer le poisson <strong>{{ poissonToDelete?.nomPoisson }}</strong> ?</p>
          <p class="text-warning">⚠️ Cette action est irréversible !</p>
        </div>
        <div class="modal-footer">
          <button @click="showDeleteModal = false" class="btn-cancel">
            Annuler
          </button>
          <button @click="deletePoisson" class="btn-danger">
            Supprimer définitivement
          </button>
        </div>
      </div>
    </div>

    <!-- Modale de vente -->
    <div v-if="showSellModal" class="modal-overlay">
      <div class="modal">
        <div class="modal-header">
          <h3>Vendre le poisson</h3>
          <button @click="showSellModal = false" class="modal-close">×</button>
        </div>
        <div class="modal-body">
          <p>Confirmez-vous la vente du poisson <strong>{{ poissonToSell?.nomPoisson }}</strong> ?</p>
          <div class="sell-details">
            <div class="sell-info">
              <span>Prix d'achat:</span>
              <strong class="text-cost">{{ formatCurrency(poissonToSell?.prixAchatPoisson) }}</strong>
            </div>
            <div class="sell-info">
              <span>Prix de vente:</span>
              <strong class="text-profit">{{ formatCurrency(poissonToSell?.prixVentePoisson) }}</strong>
            </div>
            <div class="sell-info">
              <span>Bénéfice:</span>
              <strong class="text-success">
                {{ formatCurrency((poissonToSell?.prixVentePoisson || 0) - (poissonToSell?.prixAchatPoisson || 0)) }}
              </strong>
            </div>
            <div class="sell-info">
              <span>Poids actuel:</span>
              <strong>{{ poissonToSell?.poidsActuelPoisson }}g</strong>
            </div>
          </div>
        </div>
        <div class="modal-footer">
          <button @click="showSellModal = false" class="btn-cancel">
            Annuler
          </button>
          <button @click="confirmSell" class="btn-success">
            Confirmer la vente
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
import { saveAs } from 'file-saver'

export default {
  name: 'PoissonsList',
  
  setup() {
    const router = useRouter()
    
    // États
    const poissons = ref([])
    const races = ref([])
    const bassins = ref([])
    const isLoading = ref(false)
    const showDeleteModal = ref(false)
    const showSellModal = ref(false)
    const poissonToDelete = ref(null)
    const poissonToSell = ref(null)
    
    // Filtres
    const filters = ref({
      search: '',
      race: '',
      bassin: '',
      status: '',
      progression: ''
    })
    
    // Tri et pagination
    const sortBy = ref('dateArriveeDesc')
    const sortColumn = ref('dateArriveePoisson')
    const sortDirection = ref('desc')
    const currentPage = ref(1)
    const itemsPerPage = ref(10)
    
    // Charger les données
    const loadData = async () => {
      isLoading.value = true
      try {
        // Charger les poissons
        const poissonsData = await poissonService.getAllPoissons()
        poissons.value = Array.isArray(poissonsData) ? poissonsData : []
        
        // Charger les races
        const racesData = await poissonService.getRacesPoisson()
        races.value = Array.isArray(racesData) ? racesData : []
        
        // Charger les bassins
        const bassinsData = await poissonService.getBassins()
        bassins.value = Array.isArray(bassinsData) ? bassinsData : []
        
      } catch (error) {
        console.error('Erreur chargement données:', error)
      } finally {
        isLoading.value = false
      }
    }
    
    // Computed properties
    const filteredPoissons = computed(() => {
      return poissons.value.filter(poisson => {
        // Filtre recherche
        if (filters.value.search) {
          const searchLower = filters.value.search.toLowerCase()
          if (!poisson.nomPoisson?.toLowerCase().includes(searchLower)) {
            return false
          }
        }
        
        // Filtre race
        if (filters.value.race && poisson.racePoisson?.idRacePoisson != filters.value.race) {
          return false
        }
        
        // Filtre bassin
        if (filters.value.bassin) {
          if (filters.value.bassin === 'sans-bassin') {
            if (poisson.piscineActuelle) return false
          } else if (poisson.piscineActuelle?.idPiscine != filters.value.bassin) {
            return false
          }
        }
        
        // Filtre statut
        if (filters.value.status) {
          switch (filters.value.status) {
            case 'en-vie':
              if (!poisson.estEnViePoisson || poisson.estVenduPoisson) return false
              break
            case 'pret-a-vendre':
              if (!poisson.estPretAVendre?.() && !getProgression(poisson) >= 95) return false
              break
            case 'affame':
              if (poisson.estRassasiePoisson) return false
              break
            case 'vendu':
              if (!poisson.estVenduPoisson) return false
              break
            case 'mort':
              if (poisson.estEnViePoisson) return false
              break
          }
        }
        
        // Filtre progression
        if (filters.value.progression) {
          const progression = getProgression(poisson)
          const [min, max] = filters.value.progression.split('-').map(Number)
          if (progression < min || progression > max) return false
        }
        
        return true
      }).sort((a, b) => {
        // Tri
        let compareA, compareB
        
        switch (sortColumn.value) {
          case 'nomPoisson':
            compareA = a.nomPoisson?.toLowerCase() || ''
            compareB = b.nomPoisson?.toLowerCase() || ''
            break
          case 'poidsActuelPoisson':
            compareA = a.poidsActuelPoisson || 0
            compareB = b.poidsActuelPoisson || 0
            break
          case 'progression':
            compareA = getProgression(a)
            compareB = getProgression(b)
            break
          case 'dateArriveePoisson':
          default:
            compareA = new Date(a.dateArriveePoisson || 0)
            compareB = new Date(b.dateArriveePoisson || 0)
        }
        
        if (sortDirection.value === 'asc') {
          return compareA > compareB ? 1 : -1
        } else {
          return compareA < compareB ? 1 : -1
        }
      })
    })
    
    const paginatedPoissons = computed(() => {
      const start = (currentPage.value - 1) * itemsPerPage.value
      const end = start + itemsPerPage.value
      return filteredPoissons.value.slice(start, end)
    })
    
    const totalPages = computed(() => {
      return Math.ceil(filteredPoissons.value.length / itemsPerPage.value)
    })
    
    const pages = computed(() => {
      const pages = []
      const maxPages = 5
      let start = Math.max(1, currentPage.value - Math.floor(maxPages / 2))
      let end = Math.min(totalPages.value, start + maxPages - 1)
      
      if (end - start + 1 < maxPages) {
        start = Math.max(1, end - maxPages + 1)
      }
      
      for (let i = start; i <= end; i++) {
        pages.push(i)
      }
      return pages
    })
    
    const filtersActifs = computed(() => {
      const actifs = []
      if (filters.value.search) {
        actifs.push({ key: 'search', label: 'Recherche', value: filters.value.search })
      }
      if (filters.value.race) {
        const race = races.value.find(r => r.idRacePoisson == filters.value.race)
        actifs.push({ key: 'race', label: 'Race', value: race?.nomRacePoisson || filters.value.race })
      }
      if (filters.value.bassin) {
        if (filters.value.bassin === 'sans-bassin') {
          actifs.push({ key: 'bassin', label: 'Bassin', value: 'Sans bassin' })
        } else {
          const bassin = bassins.value.find(b => b.idPiscine == filters.value.bassin)
          actifs.push({ key: 'bassin', label: 'Bassin', value: bassin?.nomPiscine || filters.value.bassin })
        }
      }
      if (filters.value.status) {
        const statusLabels = {
          'en-vie': 'En vie',
          'pret-a-vendre': 'Prêt à vendre',
          'affame': 'Affamé',
          'vendu': 'Vendu',
          'mort': 'Mort'
        }
        actifs.push({ key: 'status', label: 'Statut', value: statusLabels[filters.value.status] })
      }
      if (filters.value.progression) {
        const progressionLabels = {
          '0-25': '0-25%',
          '25-50': '25-50%',
          '50-75': '50-75%',
          '75-95': '75-95%',
          '95-100': '95-100%'
        }
        actifs.push({ key: 'progression', label: 'Progression', value: progressionLabels[filters.value.progression] })
      }
      return actifs
    })
    
    const poissonsAvecBassin = computed(() => {
      return poissons.value.filter(p => p.piscineActuelle).length
    })
    
    const poissonsPretAVendre = computed(() => {
      return poissons.value.filter(p => 
        p.estEnViePoisson && 
        !p.estVenduPoisson && 
        getProgression(p) >= 95
      ).length
    })
    
    const poidsMoyen = computed(() => {
      if (poissons.value.length === 0) return 0
      const total = poissons.value
        .filter(p => p.estEnViePoisson && !p.estVenduPoisson)
        .reduce((sum, p) => sum + (p.poidsActuelPoisson || 0), 0)
      const count = poissons.value.filter(p => p.estEnViePoisson && !p.estVenduPoisson).length
      return count > 0 ? Math.round(total / count) : 0
    })
    
    // Méthodes utilitaires
    const getProgression = (poisson) => {
      if (!poisson.poidsMaximalPoisson || poisson.poidsMaximalPoisson <= 0) return 0
      const progression = (poisson.poidsActuelPoisson / poisson.poidsMaximalPoisson) * 100
      return Math.min(progression, 100)
    }
    
    const getProgressionClass = (progression) => {
      if (progression >= 95) return 'progress-high'
      if (progression >= 75) return 'progress-medium-high'
      if (progression >= 50) return 'progress-medium'
      if (progression >= 25) return 'progress-low'
      return 'progress-very-low'
    }
    
    const getStatusClass = (poisson) => {
      if (poisson.estVenduPoisson) return 'status-sold'
      if (!poisson.estEnViePoisson) return 'status-dead'
      if (getProgression(poisson) >= 95) return 'status-ready'
      if (!poisson.estRassasiePoisson) return 'status-hungry'
      return 'status-alive'
    }
    
    const getStatusText = (poisson) => {
      if (poisson.estVenduPoisson) return 'Vendu'
      if (!poisson.estEnViePoisson) return 'Mort'
      if (getProgression(poisson) >= 95) return 'Prêt à vendre'
      if (!poisson.estRassasiePoisson) return 'Affamé'
      return 'En vie'
    }
    
    const getRaceColor = (raceId) => {
      const colors = [
        '#4299e1', '#48bb78', '#ed8936', '#9f7aea',
        '#f56565', '#38b2ac', '#ecc94b', '#667eea'
      ]
      return colors[raceId % colors.length] + '20'
    }
    
    const formatWeight = (weight) => {
      if (!weight) return '0'
      return weight.toFixed(2)
    }
    
    const formatDate = (dateString) => {
      if (!dateString) return 'N/A'
      return new Date(dateString).toLocaleDateString('fr-FR')
    }
    
    const formatCurrency = (amount) => {
      if (!amount) return '0 MGA'
      return new Intl.NumberFormat('fr-MG', {
        style: 'currency',
        currency: 'MGA',
        minimumFractionDigits: 0,
        maximumFractionDigits: 0
      }).format(amount)
    }
    
    const getDaysSinceArrival = (dateString) => {
      if (!dateString) return 0
      const arrivalDate = new Date(dateString)
      const today = new Date()
      const diffTime = Math.abs(today - arrivalDate)
      return Math.ceil(diffTime / (1000 * 60 * 60 * 24))
    }
    
    // Méthodes de gestion
    const refreshData = () => {
      loadData()
    }
    
    const resetFilters = () => {
      filters.value = {
        search: '',
        race: '',
        bassin: '',
        status: '',
        progression: ''
      }
      currentPage.value = 1
    }
    
    const removeFilter = (filterKey) => {
      filters.value[filterKey] = ''
    }
    
    const sortByColumn = (column) => {
      if (sortColumn.value === column) {
        sortDirection.value = sortDirection.value === 'asc' ? 'desc' : 'asc'
      } else {
        sortColumn.value = column
        sortDirection.value = 'asc'
      }
    }
    
    const exportToCSV = () => {
      const headers = [
        'ID', 'Nom', 'Race', 'Bassin', 'Poids actuel (g)', 'Poids max (g)',
        'Progression (%)', 'Date arrivée', 'Statut', 'Prix achat', 'Prix vente',
        'Bénéfice potentiel', 'Capacité augmentation/jour', 'Rassasié', 'En vie'
      ]
      
      const csvData = filteredPoissons.value.map(poisson => [
        poisson.idPoisson,
        poisson.nomPoisson,
        poisson.racePoisson?.nomRacePoisson || '',
        poisson.piscineActuelle?.nomPiscine || 'Sans bassin',
        poisson.poidsActuelPoisson,
        poisson.poidsMaximalPoisson,
        getProgression(poisson).toFixed(2),
        poisson.dateArriveePoisson,
        getStatusText(poisson),
        poisson.prixAchatPoisson,
        poisson.prixVentePoisson,
        (poisson.prixVentePoisson - poisson.prixAchatPoisson),
        poisson.capaciteAugmentationPoisson,
        poisson.estRassasiePoisson ? 'Oui' : 'Non',
        poisson.estEnViePoisson ? 'Oui' : 'Non'
      ])
      
      const csvContent = [
        headers.join(','),
        ...csvData.map(row => row.map(cell => `"${cell}"`).join(','))
      ].join('\n')
      
      const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8' })
      const filename = `poissons_${new Date().toISOString().split('T')[0]}.csv`
      saveAs(blob, filename)
    }
    
    const confirmDelete = (poisson) => {
      poissonToDelete.value = poisson
      showDeleteModal.value = true
    }
    
    const deletePoisson = async () => {
      if (!poissonToDelete.value) return
      
      try {
        await poissonService.deletePoisson(poissonToDelete.value.idPoisson)
        await loadData()
        showDeleteModal.value = false
      } catch (error) {
        console.error('Erreur suppression poisson:', error)
        alert('Erreur lors de la suppression du poisson')
      }
    }
    
    const vendrePoisson = (poisson) => {
      poissonToSell.value = poisson
      showSellModal.value = true
    }
    
    const confirmSell = async () => {
      if (!poissonToSell.value) return
      
      try {
        await poissonService.vendrePoisson(poissonToSell.value.idPoisson)
        await loadData()
        showSellModal.value = false
      } catch (error) {
        console.error('Erreur vente poisson:', error)
        alert('Erreur lors de la vente du poisson')
      }
    }
    
    // Surveillance des changements
    watch(sortBy, (newValue) => {
      switch (newValue) {
        case 'dateArriveeDesc':
          sortColumn.value = 'dateArriveePoisson'
          sortDirection.value = 'desc'
          break
        case 'dateArriveeAsc':
          sortColumn.value = 'dateArriveePoisson'
          sortDirection.value = 'asc'
          break
        case 'nomAsc':
          sortColumn.value = 'nomPoisson'
          sortDirection.value = 'asc'
          break
        case 'nomDesc':
          sortColumn.value = 'nomPoisson'
          sortDirection.value = 'desc'
          break
        case 'poidsDesc':
          sortColumn.value = 'poidsActuelPoisson'
          sortDirection.value = 'desc'
          break
        case 'poidsAsc':
          sortColumn.value = 'poidsActuelPoisson'
          sortDirection.value = 'asc'
          break
        case 'progressionDesc':
          sortColumn.value = 'progression'
          sortDirection.value = 'desc'
          break
        case 'progressionAsc':
          sortColumn.value = 'progression'
          sortDirection.value = 'asc'
          break
      }
    })
    
    watch([filters, itemsPerPage], () => {
      currentPage.value = 1
    })
    
    // Initialisation
    onMounted(() => {
      loadData()
    })
    
    return {
      // États
      poissons,
      races,
      bassins,
      isLoading,
      showDeleteModal,
      showSellModal,
      poissonToDelete,
      poissonToSell,
      
      // Filtres
      filters,
      filtersActifs,
      sortBy,
      sortColumn,
      sortDirection,
      currentPage,
      itemsPerPage,
      
      // Computed
      filteredPoissons,
      paginatedPoissons,
      totalPages,
      pages,
      poissonsAvecBassin,
      poissonsPretAVendre,
      poidsMoyen,
      
      // Méthodes
      loadData,
      refreshData,
      resetFilters,
      removeFilter,
      sortByColumn,
      exportToCSV,
      confirmDelete,
      deletePoisson,
      vendrePoisson,
      confirmSell,
      getProgression,
      getProgressionClass,
      getStatusClass,
      getStatusText,
      getRaceColor,
      formatWeight,
      formatDate,
      formatCurrency,
      getDaysSinceArrival
    }
  }
}
</script>

<style scoped>
.poissons-list {
  padding: 20px;
  background: #f7fafc;
  min-height: 100vh;
}

/* En-tête */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
  background: white;
  padding: 25px;
  border-radius: 12px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.05);
}

.header-left h1 {
  margin: 0;
  color: #2d3748;
  font-size: 28px;
}

.subtitle {
  margin: 5px 0 0 0;
  color: #718096;
}

.header-right {
  display: flex;
  gap: 15px;
}

.btn-primary {
  padding: 12px 24px;
  background: #4299e1;
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-weight: 600;
  text-decoration: none;
  display: inline-flex;
  align-items: center;
  gap: 8px;
  transition: background 0.2s;
}

.btn-primary:hover {
  background: #3182ce;
}

.btn-secondary {
  padding: 12px 24px;
  background: #48bb78;
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-weight: 600;
  display: inline-flex;
  align-items: center;
  gap: 8px;
  transition: background 0.2s;
}

.btn-secondary:hover:not(:disabled) {
  background: #38a169;
}

.btn-secondary:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* Cartes de statistiques */
.stats-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
  margin-bottom: 30px;
}

.stat-card {
  background: white;
  padding: 20px;
  border-radius: 10px;
  box-shadow: 0 2px 5px rgba(0,0,0,0.1);
  display: flex;
  align-items: center;
  gap: 15px;
}

.stat-icon {
  font-size: 32px;
}

.stat-content h3 {
  margin: 0;
  color: #718096;
  font-size: 14px;
  font-weight: 600;
}

.stat-value {
  margin: 5px 0 0 0;
  font-size: 24px;
  font-weight: bold;
  color: #2d3748;
}

/* Section filtres */
.filters-section {
  background: white;
  padding: 25px;
  border-radius: 12px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.05);
  margin-bottom: 30px;
}

.filters-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.filters-header h2 {
  margin: 0;
  color: #2d3748;
  font-size: 20px;
  display: flex;
  align-items: center;
  gap: 10px;
}

.btn-link {
  background: none;
  border: none;
  color: #4299e1;
  cursor: pointer;
  font-weight: 600;
  display: inline-flex;
  align-items: center;
  gap: 5px;
}

.btn-link:hover {
  text-decoration: underline;
}

.filters-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
  margin-bottom: 20px;
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

.search-input,
.filter-select {
  padding: 10px 15px;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  font-size: 14px;
  width: 100%;
}

.search-input:focus,
.filter-select:focus {
  outline: none;
  border-color: #4299e1;
  box-shadow: 0 0 0 3px rgba(66, 153, 225, 0.1);
}

/* Filtres actifs */
.active-filters {
  padding-top: 20px;
  border-top: 1px solid #e2e8f0;
}

.active-filters-label {
  display: block;
  margin-bottom: 10px;
  color: #718096;
  font-size: 14px;
}

.filter-chips {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.filter-chip {
  background: #e2e8f0;
  padding: 5px 10px;
  border-radius: 20px;
  font-size: 14px;
  color: #4a5568;
  display: inline-flex;
  align-items: center;
  gap: 5px;
}

.filter-chip-remove {
  background: none;
  border: none;
  color: #718096;
  cursor: pointer;
  font-size: 16px;
  line-height: 1;
  padding: 0;
  width: 16px;
  height: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.filter-chip-remove:hover {
  color: #f56565;
}

/* Tableau */
.table-container {
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.05);
  overflow: hidden;
}

.table-header {
  padding: 20px;
  border-bottom: 1px solid #e2e8f0;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.table-info {
  color: #718096;
}

.result-count {
  font-weight: 600;
}

.btn-icon {
  background: none;
  border: none;
  font-size: 20px;
  cursor: pointer;
  padding: 5px;
  border-radius: 4px;
}

.btn-icon:hover {
  background: #f7fafc;
}

.table-responsive {
  overflow-x: auto;
}

.poissons-table {
  width: 100%;
  border-collapse: collapse;
}

.poissons-table thead {
  background: #f7fafc;
}

.poissons-table th {
  padding: 15px;
  text-align: left;
  font-weight: 600;
  color: #4a5568;
  border-bottom: 2px solid #e2e8f0;
  white-space: nowrap;
}

.sortable {
  cursor: pointer;
  user-select: none;
}

.sortable:hover {
  background: #edf2f7;
}

.sort-indicator {
  margin-left: 5px;
  font-size: 12px;
}

.poissons-table td {
  padding: 15px;
  border-bottom: 1px solid #e2e8f0;
  vertical-align: middle;
}

.table-row:hover {
  background: #f7fafc;
}

/* Styles des cellules */
.cell-name .name-container {
  display: flex;
  align-items: center;
  gap: 10px;
}

.fish-icon {
  font-size: 24px;
}

.name-details {
  display: flex;
  flex-direction: column;
}

.text-muted {
  color: #718096;
  font-size: 12px;
}

.race-badge {
  padding: 5px 10px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
  display: inline-block;
}

.bassin-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.bassin-icon {
  font-size: 20px;
}

.no-bassin {
  color: #f56565;
  font-style: italic;
}

.weight-info {
  display: flex;
  flex-direction: column;
}

.progression-cell {
  display: flex;
  align-items: center;
  gap: 10px;
}

.progression-bar-container {
  flex: 1;
  height: 8px;
  background: #e2e8f0;
  border-radius: 4px;
  overflow: hidden;
}

.progression-bar {
  height: 100%;
  border-radius: 4px;
  transition: width 0.3s;
}

.progress-very-low { background: #f56565; }
.progress-low { background: #ed8936; }
.progress-medium { background: #ecc94b; }
.progress-medium-high { background: #48bb78; }
.progress-high { background: #38b2ac; }

.progression-text {
  min-width: 45px;
  text-align: right;
  font-weight: 600;
  color: #2d3748;
}

.date-cell {
  display: flex;
  flex-direction: column;
}

.status-container {
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.status-badge {
  padding: 5px 10px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
  display: inline-block;
  width: fit-content;
}

.status-alive { background: #c6f6d5; color: #22543d; }
.status-hungry { background: #feebc8; color: #744210; }
.status-ready { background: #d6bcfa; color: #553c9a; }
.status-dead { background: #fed7d7; color: #742a2a; }
.status-sold { background: #bee3f8; color: #2c5282; }

.status-icons {
  display: flex;
  gap: 5px;
}

.status-icon {
  font-size: 14px;
}

/* Boutons d'action */
.action-buttons {
  display: flex;
  gap: 8px;
}

.btn-action {
  padding: 8px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
  text-decoration: none;
}

.btn-view {
  background: #bee3f8;
  color: #2c5282;
}

.btn-view:hover {
  background: #90cdf4;
}

.btn-edit {
  background: #c6f6d5;
  color: #22543d;
}

.btn-edit:hover {
  background: #9ae6b4;
}

.btn-sell {
  background: #d6bcfa;
  color: #553c9a;
}

.btn-sell:hover {
  background: #b794f4;
}

.btn-delete {
  background: #fed7d7;
  color: #742a2a;
}

.btn-delete:hover {
  background: #feb2b2;
}

/* Pas de résultats */
.no-results {
  padding: 60px 20px;
  text-align: center;
}

.no-results-icon {
  font-size: 60px;
  margin-bottom: 20px;
}

.no-results h3 {
  margin: 0 0 10px 0;
  color: #2d3748;
}

.no-results p {
  color: #718096;
  margin-bottom: 20px;
}

/* Pagination */
.pagination {
  padding: 20px;
  border-top: 1px solid #e2e8f0;
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 15px;
}

.pagination-btn {
  padding: 8px 16px;
  background: #e2e8f0;
  color: #4a5568;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-weight: 600;
}

.pagination-btn:hover:not(:disabled) {
  background: #cbd5e0;
}

.pagination-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.pagination-pages {
  display: flex;
  gap: 5px;
}

.page-number {
  padding: 8px 12px;
  border-radius: 6px;
  cursor: pointer;
  user-select: none;
  min-width: 40px;
  text-align: center;
}

.page-number:hover {
  background: #e2e8f0;
}

.page-number.active {
  background: #4299e1;
  color: white;
}

.pagination-info {
  display: flex;
  align-items: center;
  gap: 15px;
  color: #718096;
}

.page-size-select {
  padding: 5px 10px;
  border: 1px solid #e2e8f0;
  border-radius: 4px;
  background: white;
}

/* Modales */
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
}

.modal {
  background: white;
  border-radius: 12px;
  width: 90%;
  max-width: 500px;
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
  padding: 20px;
  border-bottom: 1px solid #e2e8f0;
}

.modal-header h3 {
  margin: 0;
  color: #2d3748;
}

.modal-close {
  background: none;
  border: none;
  font-size: 24px;
  cursor: pointer;
  color: #718096;
  line-height: 1;
}

.modal-close:hover {
  color: #f56565;
}

.modal-body {
  padding: 20px;
}

.text-warning {
  color: #ed8936;
  font-weight: 600;
}

.text-cost {
  color: #f56565;
}

.text-profit {
  color: #48bb78;
}

.text-success {
  color: #38a169;
}

.sell-details {
  background: #f7fafc;
  padding: 15px;
  border-radius: 8px;
  margin-top: 15px;
}

.sell-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.sell-info:last-child {
  margin-bottom: 0;
}

.modal-footer {
  padding: 20px;
  border-top: 1px solid #e2e8f0;
  display: flex;
  justify-content: flex-end;
  gap: 15px;
}

.btn-cancel {
  padding: 10px 20px;
  background: #e2e8f0;
  color: #4a5568;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-weight: 600;
}

.btn-cancel:hover {
  background: #cbd5e0;
}

.btn-danger {
  padding: 10px 20px;
  background: #f56565;
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-weight: 600;
}

.btn-danger:hover {
  background: #e53e3e;
}

.btn-success {
  padding: 10px 20px;
  background: #48bb78;
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-weight: 600;
}

.btn-success:hover {
  background: #38a169;
}

/* Responsive */
@media (max-width: 1024px) {
  .filters-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    align-items: stretch;
    gap: 15px;
  }
  
  .header-right {
    justify-content: flex-start;
  }
  
  .filters-grid {
    grid-template-columns: 1fr;
  }
  
  .pagination {
    flex-direction: column;
    gap: 15px;
  }
  
  .action-buttons {
    flex-wrap: wrap;
  }
  
  .poissons-table {
    font-size: 14px;
  }
  
  .poissons-table th,
  .poissons-table td {
    padding: 10px;
  }
}
</style>