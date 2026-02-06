<template>
  <div class="poissons-list">
    <div class="page-header">
      <h1>Gestion des Poissons</h1>
      <div class="header-actions">
        <button @click="refreshData" class="btn-refresh" title="Rafraîchir">
          🔄
        </button>
        <router-link to="/poissons/nouveau" class="btn-add">
          <span>+</span> Ajouter un poisson
        </router-link>
      </div>
    </div>

    <!-- Filtres et recherche -->
    <div class="filters-container">
      <div class="search-box">
        <input 
          type="text" 
          v-model="searchQuery" 
          placeholder="Rechercher un poisson..."
          @input="filterPoissons"
        />
        <span class="search-icon">🔍</span>
      </div>
      
      <div class="filter-group">
        <select v-model="filterStatus" @change="filterPoissons">
          <option value="">Tous les statuts</option>
          <option value="en-vie">En vie</option>
          <option value="affame">Affamé</option>
          <option value="rassasie">Rassasié</option>
          <option value="pret-a-vendre">Prêt à vendre</option>
          <option value="vendu">Vendu</option>
          <option value="mort">Mort</option>
        </select>
        
        <select v-model="filterRace" @change="filterPoissons">
          <option value="">Toutes les races</option>
          <option v-for="race in races" :key="race.idRacePoisson" :value="race.idRacePoisson">
            {{ race.nomRacePoisson }}
          </option>
        </select>
      </div>
    </div>

    <!-- Statistiques de filtre -->
    <div class="filter-stats">
      <span class="filter-count">{{ filteredPoissons.length }} poisson(s) trouvé(s)</span>
      <div class="quick-stats">
        <span class="stat en-vie">🐟 {{ stats.enVie }} en vie</span>
        <span class="stat pret">💰 {{ stats.pret }} prêts</span>
        <span class="stat vendu">✅ {{ stats.vendu }} vendus</span>
      </div>
    </div>

    <!-- Tableau des poissons -->
    <div class="table-container">
      <div class="table-responsive">
        <table class="poissons-table">
          <thead>
            <tr>
              <th @click="sortBy('nomPoisson')" class="sortable">
                Nom
                <span v-if="sortField === 'nomPoisson'" class="sort-icon">
                  {{ sortDirection === 'asc' ? '↑' : '↓' }}
                </span>
              </th>
              <th @click="sortBy('racePoisson.nomRacePoisson')" class="sortable">
                Race
                <span v-if="sortField === 'racePoisson.nomRacePoisson'" class="sort-icon">
                  {{ sortDirection === 'asc' ? '↑' : '↓' }}
                </span>
              </th>
              <th @click="sortBy('poidsActuelPoisson')" class="sortable">
                Poids actuel
                <span v-if="sortField === 'poidsActuelPoisson'" class="sort-icon">
                  {{ sortDirection === 'asc' ? '↑' : '↓' }}
                </span>
              </th>
              <th>Poids max</th>
              <th>Progression</th>
              <th @click="sortBy('dateArriveePoisson')" class="sortable">
                Date arrivée
                <span v-if="sortField === 'dateArriveePoisson'" class="sort-icon">
                  {{ sortDirection === 'asc' ? '↑' : '↓' }}
                </span>
              </th>
              <th>Statut</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="poisson in paginatedPoissons" :key="poisson.idPoisson" 
                :class="getRowClass(poisson)">
              <td>
                <div class="poisson-name">
                  <span class="name">{{ poisson.nomPoisson }}</span>
                  <span v-if="poisson.estPretAVendre" class="badge ready">💰</span>
                </div>
              </td>
              <td>{{ poisson.racePoisson?.nomRacePoisson || 'N/A' }}</td>
              <td>
                <div class="weight-display">
                  <span class="weight-value">{{ formatPoids(poisson.poidsActuelPoisson) }}g</span>
                  <div class="weight-bar">
                    <div class="weight-fill" :style="{ width: getProgression(poisson) + '%' }"></div>
                  </div>
                </div>
              </td>
              <td>{{ formatPoids(poisson.poidsMaximalPoisson) }}g</td>
              <td>
                <div class="progress-cell">
                  <div class="progress-container">
                    <div class="progress-bar" :style="{ width: getProgression(poisson) + '%' }">
                      <span class="progress-text">{{ getProgression(poisson) }}%</span>
                    </div>
                  </div>
                </div>
              </td>
              <td>
                {{ formatDate(poisson.dateArriveePoisson) }}
                <div class="date-info" v-if="poisson.dateVentePoisson">
                  Vendu le: {{ formatDate(poisson.dateVentePoisson) }}
                </div>
              </td>
              <td>
                <div class="status-container">
                  <span class="status-badge" :class="getStatusClass(poisson)">
                    {{ getStatusText(poisson) }}
                  </span>
                  <div class="status-icons">
                    <span v-if="poisson.estEnViePoisson" title="En vie">🐟</span>
                    <span v-if="!poisson.estRassasiePoisson && poisson.estEnViePoisson" title="Affamé">🍽️</span>
                    <span v-if="poisson.estVenduPoisson" title="Vendu">💰</span>
                  </div>
                </div>
              </td>
              <td>
                <div class="action-buttons">
                  <router-link :to="'/poissons/' + poisson.idPoisson" class="btn-action view" title="Voir détails">
                    👁️
                  </router-link>
                  
                  <button v-if="poisson.estEnViePoisson && !poisson.estVenduPoisson" 
                          @click="toggleNourri(poisson)" 
                          class="btn-action feed" 
                          :title="poisson.estRassasiePoisson ? 'Marquer affamé' : 'Marquer rassasié'">
                    {{ poisson.estRassasiePoisson ? '🍽️' : '✅' }}
                  </button>
                  
                  <button v-if="poisson.estPretAVendre && !poisson.estVenduPoisson" 
                          @click="vendrePoisson(poisson.idPoisson)" 
                          class="btn-action sell" 
                          title="Vendre">
                    💰
                  </button>
                  
                  <button @click="editPoisson(poisson.idPoisson)" class="btn-action edit" title="Modifier">
                    ✏️
                  </button>
                  
                  <button v-if="!poisson.estVenduPoisson" 
                          @click="deletePoisson(poisson.idPoisson, poisson.nomPoisson)" 
                          class="btn-action delete" 
                          title="Supprimer">
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
          <option value="100">100 par page</option>
        </select>
      </div>

      <!-- Aucun résultat -->
      <div v-if="filteredPoissons.length === 0" class="no-results">
        <div class="no-results-icon">🐟</div>
        <h3>Aucun poisson trouvé</h3>
        <p v-if="searchQuery || filterStatus || filterRace">
          Essayez de modifier vos critères de recherche
        </p>
        <router-link to="/poissons/nouveau" class="btn-add-empty">
          Ajouter votre premier poisson
        </router-link>
      </div>
    </div>

    <!-- Modale de suppression -->
    <div v-if="showDeleteModal" class="modal-overlay">
      <div class="modal">
        <h3>Confirmer la suppression</h3>
        <p>Voulez-vous vraiment supprimer le poisson "{{ poissonToDelete?.nomPoisson }}" ?</p>
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

    <!-- Export et actions batch -->
    <div class="batch-actions">
      <button @click="exportToCSV" class="btn-export">
        📊 Exporter en CSV
      </button>
      <button @click="markAllFed" class="btn-batch" v-if="hasHungryFish">
        ✅ Marquer tous comme rassasiés
      </button>
    </div>
  </div>
</template>

<script>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import poissonService from '../services/poissonService'

export default {
  name: 'PoissonsList',
  setup() {
    const router = useRouter()
    const poissons = ref([])
    const races = ref([])
    const filteredPoissons = ref([])
    const searchQuery = ref('')
    const filterStatus = ref('')
    const filterRace = ref('')
    const sortField = ref('dateArriveePoisson')
    const sortDirection = ref('desc')
    const currentPage = ref(1)
    const itemsPerPage = ref(25)
    const showDeleteModal = ref(false)
    const poissonToDelete = ref(null)
    const loading = ref(false)

    // Calculer les statistiques
    const stats = computed(() => {
      const enVie = poissons.value.filter(p => p.estEnViePoisson && !p.estVenduPoisson).length
      const pret = poissons.value.filter(p => p.estPretAVendre && !p.estVenduPoisson).length
      const vendu = poissons.value.filter(p => p.estVenduPoisson).length
      return { enVie, pret, vendu }
    })

    // Vérifier s'il y a des poissons affamés
    const hasHungryFish = computed(() => {
      return poissons.value.some(p => 
        p.estEnViePoisson && !p.estVenduPoisson && !p.estRassasiePoisson
      )
    })

    // Pagination
    const totalPages = computed(() => {
      return Math.ceil(filteredPoissons.value.length / itemsPerPage.value)
    })

    const paginatedPoissons = computed(() => {
      const start = (currentPage.value - 1) * itemsPerPage.value
      const end = start + itemsPerPage.value
      return filteredPoissons.value.slice(start, end)
    })

    // Charger les données
    const loadData = async () => {
      loading.value = true
      try {
        // Charger tous les poissons
        const poissonsData = await poissonService.getAllPoissons()
        poissons.value = poissonsData
        filteredPoissons.value = poissonsData

        // Charger les races
        const racesData = await poissonService.getRacesPoisson()
        races.value = racesData
      } catch (error) {
        console.error('Erreur chargement données:', error)
        alert('Impossible de charger les données')
      } finally {
        loading.value = false
      }
    }

    // Filtrer les poissons
    const filterPoissons = () => {
      let filtered = [...poissons.value]

      // Recherche par nom
      if (searchQuery.value) {
        const query = searchQuery.value.toLowerCase()
        filtered = filtered.filter(p => 
          p.nomPoisson.toLowerCase().includes(query) ||
          (p.racePoisson?.nomRacePoisson?.toLowerCase() || '').includes(query)
        )
      }

      // Filtre par statut
      if (filterStatus.value) {
        switch (filterStatus.value) {
          case 'en-vie':
            filtered = filtered.filter(p => p.estEnViePoisson && !p.estVenduPoisson)
            break
          case 'affame':
            filtered = filtered.filter(p => !p.estRassasiePoisson && p.estEnViePoisson && !p.estVenduPoisson)
            break
          case 'rassasie':
            filtered = filtered.filter(p => p.estRassasiePoisson && p.estEnViePoisson && !p.estVenduPoisson)
            break
          case 'pret-a-vendre':
            filtered = filtered.filter(p => p.estPretAVendre && !p.estVenduPoisson)
            break
          case 'vendu':
            filtered = filtered.filter(p => p.estVenduPoisson)
            break
          case 'mort':
            filtered = filtered.filter(p => !p.estEnViePoisson && !p.estVenduPoisson)
            break
        }
      }

      // Filtre par race
      if (filterRace.value) {
        filtered = filtered.filter(p => p.racePoisson?.idRacePoisson == filterRace.value)
      }

      // Trier les résultats
      filtered.sort((a, b) => {
        let aValue, bValue
        
        if (sortField.value.includes('.')) {
          const fields = sortField.value.split('.')
          aValue = fields.reduce((obj, field) => obj?.[field], a)
          bValue = fields.reduce((obj, field) => obj?.[field], b)
        } else {
          aValue = a[sortField.value]
          bValue = b[sortField.value]
        }

        if (typeof aValue === 'string') {
          return sortDirection.value === 'asc' 
            ? aValue.localeCompare(bValue)
            : bValue.localeCompare(aValue)
        } else {
          return sortDirection.value === 'asc'
            ? (aValue || 0) - (bValue || 0)
            : (bValue || 0) - (aValue || 0)
        }
      })

      filteredPoissons.value = filtered
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
      filterPoissons()
    }

    // Formater le poids
    const formatPoids = (poids) => {
      return poids ? Math.round(poids * 100) / 100 : 0
    }

    // Formater la date
    const formatDate = (dateString) => {
      if (!dateString) return 'N/A'
      return new Date(dateString).toLocaleDateString('fr-FR')
    }

    // Calculer la progression
    const getProgression = (poisson) => {
      if (!poisson.poidsActuelPoisson || !poisson.poidsMaximalPoisson) return 0
      return Math.round((poisson.poidsActuelPoisson / poisson.poidsMaximalPoisson) * 100)
    }

    // Classe CSS pour la ligne
    const getRowClass = (poisson) => {
      const classes = []
      if (!poisson.estEnViePoisson) classes.push('dead')
      if (poisson.estVenduPoisson) classes.push('sold')
      if (poisson.estPretAVendre) classes.push('ready-to-sell')
      if (!poisson.estRassasiePoisson && poisson.estEnViePoisson) classes.push('hungry')
      return classes.join(' ')
    }

    // Classe de statut
    const getStatusClass = (poisson) => {
      if (!poisson.estEnViePoisson) return 'dead'
      if (poisson.estVenduPoisson) return 'sold'
      if (poisson.estPretAVendre) return 'ready'
      if (poisson.estRassasiePoisson) return 'fed'
      return 'hungry'
    }

    // Texte de statut
    const getStatusText = (poisson) => {
      if (!poisson.estEnViePoisson) return 'Mort'
      if (poisson.estVenduPoisson) return 'Vendu'
      if (poisson.estPretAVendre) return 'Prêt à vendre'
      if (poisson.estRassasiePoisson) return 'Rassasié'
      return 'Affamé'
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

    // Rafraîchir les données
    const refreshData = () => {
      loadData()
    }

    // Basculer état nourri
    const toggleNourri = async (poisson) => {
      try {
        const updatedPoisson = {
          ...poisson,
          estRassasiePoisson: !poisson.estRassasiePoisson
        }
        await poissonService.updatePoisson(poisson.idPoisson, updatedPoisson)
        await loadData()
      } catch (error) {
        console.error('Erreur mise à jour:', error)
        alert('Erreur lors de la mise à jour')
      }
    }

    // Vendre un poisson
    const vendrePoisson = async (id) => {
      if (confirm('Vendre ce poisson ?')) {
        try {
          await poissonService.vendrePoisson(id)
          await loadData()
          alert('Poisson vendu avec succès !')
        } catch (error) {
          alert('Erreur lors de la vente')
        }
      }
    }

    // Éditer un poisson
    const editPoisson = (poissonId) => {
      console.log('ID du poisson à éditer:', poissonId)
      if (!poissonId) {
        alert('ID du poisson invalide')
        return
      }
      router.push(`/poissons/edit/${poissonId}`)
    }

    // Supprimer un poisson
    const deletePoisson = (id, nom) => {
      poissonToDelete.value = { id, nom }
      showDeleteModal.value = true
    }

    const confirmDelete = async () => {
      try {
        await poissonService.deletePoisson(poissonToDelete.value.id)
        await loadData()
        alert('Poisson supprimé avec succès')
      } catch (error) {
        alert('Erreur lors de la suppression')
      } finally {
        cancelDelete()
      }
    }

    const cancelDelete = () => {
      showDeleteModal.value = false
      poissonToDelete.value = null
    }

    // Exporter en CSV
    const exportToCSV = () => {
      const headers = ['Nom', 'Race', 'Poids actuel (g)', 'Poids max (g)', 'Progression (%)', 
                      'Date arrivée', 'Date vente', 'Statut', 'Prix achat', 'Prix vente']
      
      const csvData = filteredPoissons.value.map(p => [
        p.nomPoisson,
        p.racePoisson?.nomRacePoisson || '',
        formatPoids(p.poidsActuelPoisson),
        formatPoids(p.poidsMaximalPoisson),
        getProgression(p),
        formatDate(p.dateArriveePoisson),
        formatDate(p.dateVentePoisson),
        getStatusText(p),
        p.prixAchatPoisson,
        p.prixVentePoisson
      ])

      const csvContent = [
        headers.join(','),
        ...csvData.map(row => row.join(','))
      ].join('\n')

      const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' })
      const link = document.createElement('a')
      link.href = URL.createObjectURL(blob)
      link.download = `poissons_${new Date().toISOString().split('T')[0]}.csv`
      link.click()
    }

    // Marquer tous comme rassasiés
    const markAllFed = async () => {
      if (!confirm('Marquer tous les poissons affamés comme rassasiés ?')) return

      try {
        const hungryFish = poissons.value.filter(p => 
          p.estEnViePoisson && !p.estVenduPoisson && !p.estRassasiePoisson
        )

        for (const poisson of hungryFish) {
          await poissonService.updatePoisson(poisson.idPoisson, {
            ...poisson,
            estRassasiePoisson: true
          })
        }

        await loadData()
        alert(`${hungryFish.length} poissons marqués comme rassasiés`)
      } catch (error) {
        console.error('Erreur:', error)
        alert('Erreur lors de la mise à jour')
      }
    }

    // Initialisation
    onMounted(() => {
      loadData()
    })

    return {
      poissons,
      races,
      filteredPoissons,
      paginatedPoissons,
      searchQuery,
      filterStatus,
      filterRace,
      sortField,
      sortDirection,
      currentPage,
      itemsPerPage,
      totalPages,
      stats,
      hasHungryFish,
      showDeleteModal,
      poissonToDelete,
      loading,
      
      filterPoissons,
      sortBy,
      formatPoids,
      formatDate,
      getProgression,
      getRowClass,
      getStatusClass,
      getStatusText,
      prevPage,
      nextPage,
      resetPagination,
      refreshData,
      toggleNourri,
      vendrePoisson,
      editPoisson,
      deletePoisson,
      confirmDelete,
      cancelDelete,
      exportToCSV,
      markAllFed
    }
  }
}
</script>

<style scoped>
@import '../assets/styles/poissons-list.css';
</style>


