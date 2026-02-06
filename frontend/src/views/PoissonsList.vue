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

        <!-- Filtre par bassin -->
        <select v-model="filterBassin" @change="filterPoissons">
          <option value="">Tous les bassins</option>
          <option value="none">Sans bassin</option>
          <option v-for="bassin in bassins" :key="bassin.idPiscine" :value="bassin.idPiscine">
            {{ bassin.nomPiscine }}
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
        <span class="stat sans-bassin">🏊 {{ stats.sansBassin }} sans bassin</span>
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
              <th @click="sortBy('piscineActuelle.nomPiscine')" class="sortable">
                Bassin
                <span v-if="sortField === 'piscineActuelle.nomPiscine'" class="sort-icon">
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
                <div class="bassin-info">
                  <template v-if="poisson.piscineActuelle">
                    <span class="bassin-name">{{ poisson.piscineActuelle.nomPiscine }}</span>
                    <span class="bassin-type" :class="getBassinTypeClass(poisson.piscineActuelle)">
                      {{ poisson.piscineActuelle.typePiscine }}
                    </span>
                  </template>
                  <template v-else>
                    <span class="no-bassin">Aucun bassin</span>
                    <router-link 
                      :to="'/poissons/edit/' + poisson.idPoisson" 
                      class="btn-assign-bassin"
                      title="Assigner un bassin">
                      🏊‍♂️
                    </router-link>
                  </template>
                </div>
              </td>
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
        <p v-if="searchQuery || filterStatus || filterRace || filterBassin">
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
      <button @click="assignToBassin" class="btn-batch" v-if="hasUnassignedFish">
        🏊‍♂️ Assigner à un bassin
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
    const bassins = ref([])
    const filteredPoissons = ref([])
    const searchQuery = ref('')
    const filterStatus = ref('')
    const filterRace = ref('')
    const filterBassin = ref('')
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
      const sansBassin = poissons.value.filter(p => !p.piscineActuelle && p.estEnViePoisson && !p.estVenduPoisson).length
      return { enVie, pret, vendu, sansBassin }
    })

    // Vérifier s'il y a des poissons affamés
    const hasHungryFish = computed(() => {
      return poissons.value.some(p => 
        p.estEnViePoisson && !p.estVenduPoisson && !p.estRassasiePoisson
      )
    })

    // Vérifier s'il y a des poissons sans bassin
    const hasUnassignedFish = computed(() => {
      return poissons.value.some(p => 
        !p.piscineActuelle && p.estEnViePoisson && !p.estVenduPoisson
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

        // Charger les bassins
        const bassinsData = await loadBassins()
        bassins.value = bassinsData
      } catch (error) {
        console.error('Erreur chargement données:', error)
        alert('Impossible de charger les données')
      } finally {
        loading.value = false
      }
    }

    // Charger les bassins (méthode séparée pour réutiliser)
    const loadBassins = async () => {
      try {
        // Créez un service pour les bassins ou utilisez une API existante
        const response = await fetch('/api/piscines') // Adaptez l'URL
        if (response.ok) {
          return await response.json()
        }
        return []
      } catch (error) {
        console.error('Erreur chargement bassins:', error)
        return []
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
          (p.racePoisson?.nomRacePoisson?.toLowerCase() || '').includes(query) ||
          (p.piscineActuelle?.nomPiscine?.toLowerCase() || '').includes(query)
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

      // Filtre par bassin
      if (filterBassin.value) {
        if (filterBassin.value === 'none') {
          filtered = filtered.filter(p => !p.piscineActuelle)
        } else {
          filtered = filtered.filter(p => 
            p.piscineActuelle?.idPiscine == filterBassin.value
          )
        }
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

        // Gérer les valeurs null/undefined
        if (aValue === undefined || aValue === null) aValue = ''
        if (bValue === undefined || bValue === null) bValue = ''

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
      if (!poisson.piscineActuelle && poisson.estEnViePoisson && !poisson.estVenduPoisson) classes.push('unassigned')
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

    // Classe pour le type de bassin
    const getBassinTypeClass = (piscine) => {
      if (!piscine || !piscine.typePiscine) return ''
      const type = piscine.typePiscine.toLowerCase()
      if (type.includes('extérieur') || type.includes('exterieur')) return 'type-outdoor'
      if (type.includes('intérieur') || type.includes('interieur')) return 'type-indoor'
      if (type.includes('nursery') || type.includes('nurserie')) return 'type-nursery'
      if (type.includes('quarantaine')) return 'type-quarantine'
      if (type.includes('reproduction')) return 'type-breeding'
      return ''
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
      const headers = ['Nom', 'Race', 'Bassin', 'Type bassin', 'Poids actuel (g)', 
                      'Poids max (g)', 'Progression (%)', 'Date arrivée', 
                      'Date vente', 'Statut', 'Prix achat', 'Prix vente']
      
      const csvData = filteredPoissons.value.map(p => [
        p.nomPoisson,
        p.racePoisson?.nomRacePoisson || '',
        p.piscineActuelle?.nomPiscine || 'Aucun',
        p.piscineActuelle?.typePiscine || '',
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

    // Assigner à un bassin (action batch)
    const assignToBassin = () => {
      const unassignedFish = poissons.value.filter(p => 
        !p.piscineActuelle && p.estEnViePoisson && !p.estVenduPoisson
      )
      
      if (unassignedFish.length === 0) {
        alert('Aucun poisson sans bassin')
        return
      }
      
      // Rediriger vers une page d'assignation en masse
      // ou ouvrir une modal pour assigner un bassin commun
      router.push('/poissons/assigner-bassin')
    }

    // Initialisation
    onMounted(() => {
      loadData()
    })

    return {
      poissons,
      races,
      bassins,
      filteredPoissons,
      paginatedPoissons,
      searchQuery,
      filterStatus,
      filterRace,
      filterBassin,
      sortField,
      sortDirection,
      currentPage,
      itemsPerPage,
      totalPages,
      stats,
      hasHungryFish,
      hasUnassignedFish,
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
      getBassinTypeClass,
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
      markAllFed,
      assignToBassin
    }
  }
}
</script>

<style scoped>
.poissons-list {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: 100vh;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
  padding-bottom: 15px;
  border-bottom: 2px solid #e0e6ed;
}

.page-header h1 {
  color: #2c3e50;
  font-size: 28px;
  margin: 0;
}

.header-actions {
  display: flex;
  gap: 15px;
  align-items: center;
}

.btn-refresh {
  background: #3498db;
  color: white;
  border: none;
  border-radius: 8px;
  padding: 10px 15px;
  cursor: pointer;
  font-size: 16px;
  transition: background 0.3s;
}

.btn-refresh:hover {
  background: #2980b9;
}

.btn-add {
  background: #27ae60;
  color: white;
  text-decoration: none;
  border-radius: 8px;
  padding: 10px 20px;
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 500;
  transition: background 0.3s;
}

.btn-add:hover {
  background: #219653;
}

.btn-add span {
  font-size: 20px;
}

/* Filtres */
.filters-container {
  background: white;
  padding: 20px;
  border-radius: 10px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.1);
  margin-bottom: 20px;
}

.search-box {
  position: relative;
  margin-bottom: 15px;
}

.search-box input {
  width: 100%;
  padding: 12px 15px 12px 45px;
  border: 1px solid #ddd;
  border-radius: 8px;
  font-size: 16px;
  transition: border 0.3s;
}

.search-box input:focus {
  outline: none;
  border-color: #3498db;
  box-shadow: 0 0 0 2px rgba(52, 152, 219, 0.2);
}

.search-icon {
  position: absolute;
  left: 15px;
  top: 50%;
  transform: translateY(-50%);
  color: #7f8c8d;
}

.filter-group {
  display: flex;
  gap: 15px;
}

.filter-group select {
  flex: 1;
  padding: 10px 15px;
  border: 1px solid #ddd;
  border-radius: 8px;
  background: white;
  font-size: 14px;
  cursor: pointer;
}

/* Statistiques */
.filter-stats {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding: 0 10px;
}

.filter-count {
  font-weight: 600;
  color: #2c3e50;
}

.quick-stats {
  display: flex;
  gap: 20px;
}

.stat {
  display: flex;
  align-items: center;
  gap: 5px;
  padding: 5px 10px;
  border-radius: 6px;
  font-size: 14px;
}

.stat.en-vie {
  background: #e8f5e9;
  color: #2e7d32;
}

.stat.pret {
  background: #fff3e0;
  color: #ef6c00;
}

.stat.vendu {
  background: #e8eaf6;
  color: #3f51b5;
}

.stat.sans-bassin {
  background: #e1f5fe;
  color: #0288d1;
}

/* Tableau */
.table-container {
  background: white;
  border-radius: 10px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.1);
  overflow: hidden;
}

.table-responsive {
  overflow-x: auto;
}

.poissons-table {
  width: 100%;
  border-collapse: collapse;
}

.poissons-table th {
  background: #f8f9fa;
  padding: 15px;
  text-align: left;
  font-weight: 600;
  color: #2c3e50;
  border-bottom: 2px solid #e0e6ed;
  user-select: none;
}

.poissons-table th.sortable {
  cursor: pointer;
  transition: background 0.2s;
}

.poissons-table th.sortable:hover {
  background: #e9ecef;
}

.sort-icon {
  margin-left: 5px;
  font-size: 12px;
}

.poissons-table td {
  padding: 15px;
  border-bottom: 1px solid #e0e6ed;
  vertical-align: middle;
}

/* Lignes avec styles spéciaux */
.poissons-table tr.dead td {
  background-color: #f9f9f9;
  opacity: 0.6;
}

.poissons-table tr.sold td {
  background-color: #f3e5f5;
}

.poissons-table tr.ready-to-sell td {
  background-color: #fff8e1;
}

.poissons-table tr.hungry td {
  background-color: #ffebee;
}

.poissons-table tr.unassigned td {
  background-color: #e3f2fd;
}

/* Cellule nom */
.poisson-name {
  display: flex;
  align-items: center;
  gap: 8px;
}

.poisson-name .name {
  font-weight: 600;
  color: #2c3e50;
}

.badge.ready {
  background: #ffd700;
  color: #000;
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 12px;
}

/* Cellule bassin */
.bassin-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.bassin-name {
  font-weight: 600;
  color: #2c3e50;
}

.bassin-type {
  font-size: 0.8em;
  padding: 2px 8px;
  border-radius: 12px;
  display: inline-block;
  width: fit-content;
}

.type-outdoor {
  background-color: #e8f5e9;
  color: #2e7d32;
  border: 1px solid #a5d6a7;
}

.type-indoor {
  background-color: #e3f2fd;
  color: #1565c0;
  border: 1px solid #90caf9;
}

.type-nursery {
  background-color: #fff3e0;
  color: #ef6c00;
  border: 1px solid #ffcc80;
}

.type-quarantine {
  background-color: #ffebee;
  color: #c62828;
  border: 1px solid #ff8a80;
}

.type-breeding {
  background-color: #f3e5f5;
  color: #7b1fa2;
  border: 1px solid #ce93d8;
}

.no-bassin {
  color: #95a5a6;
  font-style: italic;
}

.btn-assign-bassin {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background-color: #f0f0f0;
  color: #3498db;
  text-decoration: none;
  margin-left: 8px;
  transition: all 0.2s;
}

.btn-assign-bassin:hover {
  background-color: #3498db;
  color: white;
  transform: scale(1.1);
}

/* Cellule poids */
.weight-display {
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.weight-value {
  font-weight: 600;
  color: #2c3e50;
}

.weight-bar {
  height: 6px;
  background: #e0e6ed;
  border-radius: 3px;
  overflow: hidden;
}

.weight-fill {
  height: 100%;
  background: #3498db;
  border-radius: 3px;
  transition: width 0.3s;
}

/* Cellule progression */
.progress-cell {
  min-width: 150px;
}

.progress-container {
  height: 24px;
  background: #e0e6ed;
  border-radius: 12px;
  overflow: hidden;
  position: relative;
}

.progress-bar {
  height: 100%;
  background: linear-gradient(90deg, #4CAF50, #8BC34A);
  border-radius: 12px;
  transition: width 0.3s;
  display: flex;
  align-items: center;
  justify-content: center;
}

.progress-text {
  color: white;
  font-size: 12px;
  font-weight: 600;
  text-shadow: 1px 1px 1px rgba(0,0,0,0.3);
}

/* Cellule statut */
.status-container {
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.status-badge {
  padding: 4px 10px;
  border-radius: 15px;
  font-size: 12px;
  font-weight: 600;
  text-align: center;
  display: inline-block;
}

.status-badge.dead {
  background: #e0e0e0;
  color: #757575;
}

.status-badge.sold {
  background: #e1bee7;
  color: #7b1fa2;
}

.status-badge.ready {
  background: #ffecb3;
  color: #ff8f00;
}

.status-badge.fed {
  background: #c8e6c9;
  color: #2e7d32;
}

.status-badge.hungry {
  background: #ffcdd2;
  color: #c62828;
}

.status-icons {
  display: flex;
  gap: 5px;
  font-size: 14px;
}

/* Actions */
.action-buttons {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.btn-action {
  width: 36px;
  height: 36px;
  border: none;
  border-radius: 8px;
  background: #f8f9fa;
  color: #2c3e50;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  transition: all 0.2s;
}

.btn-action:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(0,0,0,0.1);
}

.btn-action.view:hover {
  background: #3498db;
  color: white;
}

.btn-action.feed:hover {
  background: #27ae60;
  color: white;
}

.btn-action.sell:hover {
  background: #f39c12;
  color: white;
}

.btn-action.edit:hover {
  background: #9b59b6;
  color: white;
}

.btn-action.delete:hover {
  background: #e74c3c;
  color: white;
}

/* Pagination */
.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 15px;
  padding: 20px;
  background: #f8f9fa;
}

.page-btn {
  padding: 8px 15px;
  border: 1px solid #ddd;
  background: white;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s;
}

.page-btn:hover:not(:disabled) {
  background: #3498db;
  color: white;
  border-color: #3498db;
}

.page-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.page-info {
  font-weight: 500;
  color: #2c3e50;
}

.pagination select {
  padding: 8px 15px;
  border: 1px solid #ddd;
  border-radius: 6px;
  background: white;
  cursor: pointer;
}

/* Aucun résultat */
.no-results {
  text-align: center;
  padding: 60px 20px;
}

.no-results-icon {
  font-size: 60px;
  margin-bottom: 20px;
}

.no-results h3 {
  color: #2c3e50;
  margin-bottom: 10px;
}

.no-results p {
  color: #7f8c8d;
  margin-bottom: 20px;
}

.btn-add-empty {
  display: inline-block;
  background: #27ae60;
  color: white;
  text-decoration: none;
  padding: 12px 24px;
  border-radius: 8px;
  font-weight: 500;
  transition: background 0.3s;
}

.btn-add-empty:hover {
  background: #219653;
}

/* Modal suppression */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0,0,0,0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal {
  background: white;
  padding: 30px;
  border-radius: 10px;
  max-width: 400px;
  width: 90%;
  box-shadow: 0 4px 20px rgba(0,0,0,0.2);
}

.modal h3 {
  margin-top: 0;
  color: #2c3e50;
}

.warning-text {
  color: #e74c3c;
  font-weight: 600;
  margin: 15px 0;
}

.modal-actions {
  display: flex;
  gap: 10px;
  justify-content: flex-end;
  margin-top: 20px;
}

.btn-delete-confirm {
  background: #e74c3c;
  color: white;
  border: none;
  padding: 10px 20px;
  border-radius: 6px;
  cursor: pointer;
  font-weight: 500;
}

.btn-delete-confirm:hover {
  background: #c0392b;
}

.btn-cancel {
  background: #95a5a6;
  color: white;
  border: none;
  padding: 10px 20px;
  border-radius: 6px;
  cursor: pointer;
  font-weight: 500;
}

.btn-cancel:hover {
  background: #7f8c8d;
}

/* Actions batch */
.batch-actions {
  margin-top: 20px;
  display: flex;
  gap: 15px;
  justify-content: center;
}

.btn-export, .btn-batch {
  padding: 12px 24px;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-weight: 500;
  display: flex;
  align-items: center;
  gap: 8px;
  transition: all 0.3s;
}

.btn-export {
  background: #3498db;
  color: white;
}

.btn-export:hover {
  background: #2980b9;
  transform: translateY(-2px);
}

.btn-batch {
  background: #9b59b6;
  color: white;
}

.btn-batch:hover {
  background: #8e44ad;
  transform: translateY(-2px);
}

/* Responsive */
@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 15px;
  }
  
  .filter-group {
    flex-direction: column;
  }
  
  .quick-stats {
    flex-wrap: wrap;
    justify-content: center;
  }
  
  .action-buttons {
    justify-content: center;
  }
  
  .batch-actions {
    flex-direction: column;
  }
}
</style>


