<template>
  <div class="bassins-list">
    <!-- En-tête de page -->
    <div class="page-header">
      <div class="header-left">
        <h1>🏊 Gestion des Bassins</h1>
        <p class="subtitle">Visualisez et gérez tous vos bassins en un seul endroit</p>
      </div>
      <div class="header-right">
        <router-link to="/bassins/nouveau" class="btn-primary">
          ➕ Ajouter un bassin
        </router-link>
        <button @click="refreshData" class="btn-secondary" :disabled="isLoading">
          🔄 Actualiser
        </button>
      </div>
    </div>

    <!-- Cartes de statistiques -->
    <div class="stats-cards">
      <div class="stat-card">
        <div class="stat-icon">🏊</div>
        <div class="stat-content">
          <h3>Total bassins</h3>
          <p class="stat-value">{{ totalBassins }}</p>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon">✅</div>
        <div class="stat-content">
          <h3>Bassins actifs</h3>
          <p class="stat-value">{{ bassinsActifs }}</p>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon">🐟</div>
        <div class="stat-content">
          <h3>Poissons total</h3>
          <p class="stat-value">{{ totalPoissons }}</p>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon">📊</div>
        <div class="stat-content">
          <h3>Taux occupation</h3>
          <p class="stat-value">{{ tauxOccupationMoyen }}%</p>
        </div>
      </div>
    </div>

    <!-- Filtres -->
    <div class="filters-section">
      <div class="filters-header">
        <h2>🔍 Filtres et Recherche</h2>
        <button @click="resetFilters" class="btn-link">
          🔄 Réinitialiser
        </button>
      </div>
      
      <div class="filters-grid">
        <div class="filter-group">
          <label for="search">Recherche</label>
          <input
            type="text"
            id="search"
            v-model="filters.search"
            placeholder="Nom du bassin..."
            class="search-input"
          />
        </div>

        <div class="filter-group">
          <label for="status">Statut</label>
          <select id="status" v-model="filters.status" class="filter-select">
            <option value="">Tous les statuts</option>
            <option value="actif">Actif</option>
            <option value="inactif">Inactif</option>
          </select>
        </div>

        <div class="filter-group">
          <label for="occupation">Occupation</label>
          <select id="occupation" v-model="filters.occupation" class="filter-select">
            <option value="">Toute occupation</option>
            <option value="vide">Vide (0%)</option>
            <option value="faible">Faible (1-33%)</option>
            <option value="moyenne">Moyenne (34-66%)</option>
            <option value="elevee">Élevée (67-99%)</option>
            <option value="plein">Plein (100%)</option>
          </select>
        </div>

        <div class="filter-group">
          <label for="sort">Trier par</label>
          <select id="sort" v-model="sortBy" class="filter-select">
            <option value="nom">Nom A-Z</option>
            <option value="nomDesc">Nom Z-A</option>
            <option value="occupationDesc">Occupation ▼</option>
            <option value="occupationAsc">Occupation ▲</option>
            <option value="poissonsDesc">Poissons ▼</option>
            <option value="poissonsAsc">Poissons ▲</option>
            <option value="dateDesc">Date récente</option>
            <option value="dateAsc">Date ancienne</option>
          </select>
        </div>
      </div>
    </div>

    <!-- Liste des bassins -->
    <div v-if="isLoading" class="loading-container">
      <div class="spinner"></div>
      <p>Chargement des bassins...</p>
    </div>

    <div v-else-if="filteredBassins.length > 0" class="bassins-grid">
      <div v-for="bassin in filteredBassins" :key="bassin.idPiscine" class="bassin-card">
        <!-- En-tête de la carte -->
        <div class="bassin-header">
          <div class="bassin-title">
            <h3>{{ bassin.nomPiscine }}</h3>
            <div class="bassin-status">
              <span class="status-badge" :class="bassin.estActivePiscine ? 'status-active' : 'status-inactive'">
                {{ bassin.estActivePiscine ? 'Actif' : 'Inactif' }}
              </span>
            </div>
          </div>
          <div class="bassin-actions">
            <button @click="toggleBassinStatus(bassin)" class="btn-icon" :title="bassin.estActivePiscine ? 'Désactiver' : 'Activer'">
              {{ bassin.estActivePiscine ? '⏸️' : '▶️' }}
            </button>
            <router-link 
              :to="{ name: 'DetailBassin', params: { id: bassin.idPiscine } }" 
              class="btn-icon"
              title="Voir détails"
            >
              👁️
            </router-link>
            <router-link 
              :to="{ path: `/bassins/edit/${bassin.idPiscine}` }" 
              class="btn-icon"
              title="Modifier"
            >
              ✏️
            </router-link>
            <button 
              @click="confirmDelete(bassin)" 
              class="btn-icon btn-delete"
              title="Supprimer"
            >
              🗑️
            </button>
          </div>
        </div>

        <!-- Corps de la carte -->
        <div class="bassin-body">
          <!-- Informations de base -->
          <div class="bassin-info">
            <div class="info-item">
              <span class="info-label">Capacité</span>
              <span class="info-value">{{ bassin.capaciteMaxPiscine }} poissons</span>
            </div>
            <div class="info-item">
              <span class="info-label">Poissons actuels</span>
              <span class="info-value">{{ getNombrePoissonsActuels(bassin) }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">Places restantes</span>
              <span class="info-value">{{ getCapaciteRestante(bassin) }}</span>
            </div>
          </div>

          <!-- Barre de progression d'occupation -->
          <div class="occupation-section">
            <div class="occupation-header">
              <span>Taux d'occupation</span>
              <span class="occupation-percent">{{ getTauxOccupation(bassin) }}%</span>
            </div>
            <div class="occupation-bar">
              <div 
                class="occupation-fill" 
                :style="{ width: getTauxOccupation(bassin) + '%' }"
                :class="getOccupationClass(getTauxOccupation(bassin))"
              ></div>
            </div>
            <div class="occupation-text">
              {{ getNombrePoissonsActuels(bassin) }} / {{ bassin.capaciteMaxPiscine }}
            </div>
          </div>

          <!-- Caractéristiques techniques -->
          <div v-if="bassin.volumePiscine || bassin.temperaturePiscine || bassin.phPiscine" class="tech-specs">
            <h4>Caractéristiques techniques</h4>
            <div class="specs-grid">
              <div v-if="bassin.volumePiscine" class="spec-item">
                <span class="spec-label">Volume</span>
                <span class="spec-value">{{ bassin.volumePiscine }} m³</span>
              </div>
              <div v-if="bassin.temperaturePiscine" class="spec-item">
                <span class="spec-label">Température</span>
                <span class="spec-value">{{ bassin.temperaturePiscine }}°C</span>
              </div>
              <div v-if="bassin.phPiscine" class="spec-item">
                <span class="spec-label">pH</span>
                <span class="spec-value">{{ bassin.phPiscine }}</span>
              </div>
            </div>
          </div>

          <!-- Description -->
          <div v-if="bassin.descriptionPiscine" class="description-section">
            <h4>Description</h4>
            <p class="description-text">{{ truncateDescription(bassin.descriptionPiscine) }}</p>
          </div>

          <!-- Dates -->
          <div class="dates-section">
            <div class="date-item">
              <span class="date-label">Créé le</span>
              <span class="date-value">{{ formatDate(bassin.dateCreationPiscine) }}</span>
            </div>
            <div v-if="bassin.dateModificationPiscine" class="date-item">
              <span class="date-label">Modifié le</span>
              <span class="date-value">{{ formatDate(bassin.dateModificationPiscine) }}</span>
            </div>
          </div>
        </div>

        <!-- Pied de la carte -->
        <div class="bassin-footer">
          <button @click="viderBassin(bassin)" class="btn-action" :disabled="!getNombrePoissonsActuels(bassin)">
            🚰 Vider le bassin
          </button>
          <router-link 
            :to="{ name: 'DetailBassin', params: { id: bassin.idPiscine } }" 
            class="btn-action btn-primary"
          >
            👁️ Voir détails
          </router-link>
        </div>
      </div>
    </div>

    <!-- Message si aucun bassin -->
    <div v-else class="no-results">
      <div class="no-results-icon">🏊</div>
      <h3>Aucun bassin trouvé</h3>
      <p v-if="filters.search || filters.status || filters.occupation">
        Essayez de modifier vos filtres ou créez un nouveau bassin.
      </p>
      <p v-else>
        Commencez par créer votre premier bassin !
      </p>
      <router-link to="/bassins/nouveau" class="btn-primary">
        ➕ Créer un bassin
      </router-link>
    </div>

    <!-- Modale de suppression -->
    <div v-if="showDeleteModal" class="modal-overlay">
      <div class="modal">
        <div class="modal-header">
          <h3>Confirmer la suppression</h3>
          <button @click="showDeleteModal = false" class="modal-close">×</button>
        </div>
        <div class="modal-body">
          <p>Êtes-vous sûr de vouloir supprimer le bassin <strong>{{ bassinToDelete?.nomPiscine }}</strong> ?</p>
          
          <div v-if="getNombrePoissonsActuels(bassinToDelete) > 0" class="warning-alert">
            ⚠️ <strong>Attention !</strong> Ce bassin contient {{ getNombrePoissonsActuels(bassinToDelete) }} poisson(s).
            Vous devez d'abord vider le bassin ou transférer les poissons.
          </div>
          
          <p class="text-danger">⚠️ Cette action est irréversible !</p>
        </div>
        <div class="modal-footer">
          <button @click="showDeleteModal = false" class="btn-cancel">
            Annuler
          </button>
          <button 
            @click="deleteBassin" 
            class="btn-danger"
            :disabled="getNombrePoissonsActuels(bassinToDelete) > 0"
          >
            {{ getNombrePoissonsActuels(bassinToDelete) > 0 ? 'Impossible (bassin non vide)' : 'Supprimer définitivement' }}
          </button>
        </div>
      </div>
    </div>

    <!-- Modale de vidage -->
    <div v-if="showEmptyModal" class="modal-overlay">
      <div class="modal">
        <div class="modal-header">
          <h3>Vider le bassin</h3>
          <button @click="showEmptyModal = false" class="modal-close">×</button>
        </div>
        <div class="modal-body">
          <p>Êtes-vous sûr de vouloir vider le bassin <strong>{{ bassinToEmpty?.nomPiscine }}</strong> ?</p>
          <p>Cette action retirera <strong>{{ getNombrePoissonsActuels(bassinToEmpty) }} poisson(s)</strong> du bassin.</p>
          
          <div class="form-group">
            <label for="raison">Raison du vidage</label>
            <select id="raison" v-model="emptyReason" class="filter-select">
              <option value="Transfert">Transfert vers autre bassin</option>
              <option value="Maintenance">Maintenance du bassin</option>
              <option value="Nettoyage">Nettoyage</option>
              <option value="Autre">Autre raison</option>
            </select>
            <input 
              v-if="emptyReason === 'Autre'" 
              v-model="customReason" 
              placeholder="Spécifiez la raison..."
              class="search-input"
              style="margin-top: 10px;"
            />
          </div>
        </div>
        <div class="modal-footer">
          <button @click="showEmptyModal = false" class="btn-cancel">
            Annuler
          </button>
          <button @click="confirmEmpty" class="btn-warning">
            Vider le bassin
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import bassinService from '../services/bassinService'

export default {
  name: 'BassinsList',
  
  setup() {
    const router = useRouter()
    
    // États
    const bassins = ref([])
    const isLoading = ref(false)
    const showDeleteModal = ref(false)
    const showEmptyModal = ref(false)
    const bassinToDelete = ref(null)
    const bassinToEmpty = ref(null)
    const emptyReason = ref('Transfert')
    const customReason = ref('')
    
    // Filtres
    const filters = ref({
      search: '',
      status: '',
      occupation: ''
    })
    
    // Tri
    const sortBy = ref('nom')
    
    // Méthodes utilitaires pour calculer les statistiques
    const getNombrePoissonsActuels = (bassin) => {
      if (!bassin) return 0
      if (bassin.nombrePoissonsActuels !== undefined) return bassin.nombrePoissonsActuels
      if (bassin.poissons && Array.isArray(bassin.poissons)) return bassin.poissons.length
      return 0
    }
    
    const getCapaciteRestante = (bassin) => {
      if (!bassin || !bassin.capaciteMaxPiscine) return 0
      const nombrePoissons = getNombrePoissonsActuels(bassin)
      return Math.max(0, bassin.capaciteMaxPiscine - nombrePoissons)
    }
    
    const getTauxOccupation = (bassin) => {
      if (!bassin || !bassin.capaciteMaxPiscine || bassin.capaciteMaxPiscine === 0) return 0
      const nombrePoissons = getNombrePoissonsActuels(bassin)
      const taux = (nombrePoissons / bassin.capaciteMaxPiscine) * 100
      return parseFloat(taux.toFixed(1))
    }
    
    // Charger les données
    const loadData = async () => {
      isLoading.value = true
      try {
        const bassinsData = await bassinService.getAllWithStats()
        bassins.value = Array.isArray(bassinsData) ? bassinsData : []
      } catch (error) {
        console.error('Erreur chargement bassins:', error)
        // Fallback: essayer avec getAll normal
        try {
          const bassinsData = await bassinService.getAll()
          bassins.value = Array.isArray(bassinsData) ? bassinsData : []
        } catch (fallbackError) {
          console.error('Erreur chargement fallback:', fallbackError)
          bassins.value = []
        }
      } finally {
        isLoading.value = false
      }
    }
    
    // Computed properties
    const filteredBassins = computed(() => {
      return bassins.value
        .filter(bassin => {
          // Filtre recherche
          if (filters.value.search) {
            const searchLower = filters.value.search.toLowerCase()
            if (!bassin.nomPiscine?.toLowerCase().includes(searchLower) &&
                !bassin.descriptionPiscine?.toLowerCase().includes(searchLower)) {
              return false
            }
          }
          
          // Filtre statut
          if (filters.value.status) {
            if (filters.value.status === 'actif' && !bassin.estActivePiscine) return false
            if (filters.value.status === 'inactif' && bassin.estActivePiscine) return false
          }
          
          // Filtre occupation
          if (filters.value.occupation) {
            const taux = getTauxOccupation(bassin)
            switch (filters.value.occupation) {
              case 'vide': if (taux > 0) return false; break
              case 'faible': if (taux <= 0 || taux > 33) return false; break
              case 'moyenne': if (taux <= 33 || taux > 66) return false; break
              case 'elevee': if (taux <= 66 || taux >= 100) return false; break
              case 'plein': if (taux < 100) return false; break
            }
          }
          
          return true
        })
        .sort((a, b) => {
          const tauxA = getTauxOccupation(a)
          const tauxB = getTauxOccupation(b)
          const poissonsA = getNombrePoissonsActuels(a)
          const poissonsB = getNombrePoissonsActuels(b)
          
          switch (sortBy.value) {
            case 'nomDesc':
              return b.nomPiscine?.localeCompare(a.nomPiscine || '') || 0
            case 'occupationDesc':
              return tauxB - tauxA
            case 'occupationAsc':
              return tauxA - tauxB
            case 'poissonsDesc':
              return poissonsB - poissonsA
            case 'poissonsAsc':
              return poissonsA - poissonsB
            case 'dateDesc':
              return new Date(b.dateCreationPiscine) - new Date(a.dateCreationPiscine)
            case 'dateAsc':
              return new Date(a.dateCreationPiscine) - new Date(b.dateCreationPiscine)
            default: // 'nom'
              return a.nomPiscine?.localeCompare(b.nomPiscine || '') || 0
          }
        })
    })
    
    const totalBassins = computed(() => bassins.value.length)
    const bassinsActifs = computed(() => bassins.value.filter(b => b.estActivePiscine).length)
    const totalPoissons = computed(() => bassins.value.reduce((sum, b) => sum + getNombrePoissonsActuels(b), 0))
    const tauxOccupationMoyen = computed(() => {
      if (bassins.value.length === 0) return 0
      const total = bassins.value.reduce((sum, b) => sum + getTauxOccupation(b), 0)
      return Math.round(total / bassins.value.length)
    })
    
    // Méthodes utilitaires
    const getOccupationClass = (taux) => {
      if (taux === 0) return 'occupation-empty'
      if (taux < 33) return 'occupation-low'
      if (taux < 66) return 'occupation-medium'
      if (taux < 100) return 'occupation-high'
      return 'occupation-full'
    }
    
    const truncateDescription = (text, maxLength = 100) => {
      if (!text) return ''
      if (text.length <= maxLength) return text
      return text.substring(0, maxLength) + '...'
    }
    
    const formatDate = (dateString) => {
      if (!dateString) return 'Date inconnue'
      try {
        const date = new Date(dateString)
        return date.toLocaleDateString('fr-FR')
      } catch {
        return 'Date invalide'
      }
    }
    
    // Méthodes de gestion
    const refreshData = () => {
      loadData()
    }
    
    const resetFilters = () => {
      filters.value = {
        search: '',
        status: '',
        occupation: ''
      }
    }
    
    const confirmDelete = (bassin) => {
      bassinToDelete.value = bassin
      showDeleteModal.value = true
    }
    
    const deleteBassin = async () => {
      if (!bassinToDelete.value) return
      
      try {
        await bassinService.delete(bassinToDelete.value.idPiscine)
        await loadData()
        showDeleteModal.value = false
      } catch (error) {
        console.error('Erreur suppression bassin:', error)
        alert(error.response?.data?.message || 'Erreur lors de la suppression du bassin')
      }
    }
    
    const viderBassin = (bassin) => {
      const nombrePoissons = getNombrePoissonsActuels(bassin)
      if (!nombrePoissons) {
        alert('Ce bassin est déjà vide !')
        return
      }
      
      bassinToEmpty.value = bassin
      emptyReason.value = 'Transfert'
      customReason.value = ''
      showEmptyModal.value = true
    }
    
    const confirmEmpty = async () => {
      if (!bassinToEmpty.value) return
      
      try {
        const raison = emptyReason.value === 'Autre' ? customReason.value : emptyReason.value
        await bassinService.viderBassin(bassinToEmpty.value.idPiscine, raison)
        await loadData()
        showEmptyModal.value = false
      } catch (error) {
        console.error('Erreur vidage bassin:', error)
        alert(error.response?.data?.message || 'Erreur lors du vidage du bassin')
      }
    }
    
    const toggleBassinStatus = async (bassin) => {
      try {
        await bassinService.toggleStatus(bassin.idPiscine)
        await loadData()
      } catch (error) {
        console.error('Erreur changement statut:', error)
        alert(error.response?.data?.message || 'Erreur lors du changement de statut')
      }
    }
    
    // Initialisation
    onMounted(() => {
      loadData()
    })
    
    return {
      // États
      bassins,
      isLoading,
      showDeleteModal,
      showEmptyModal,
      bassinToDelete,
      bassinToEmpty,
      emptyReason,
      customReason,
      
      // Filtres
      filters,
      sortBy,
      
      // Computed
      filteredBassins,
      totalBassins,
      bassinsActifs,
      totalPoissons,
      tauxOccupationMoyen,
      
      // Méthodes utilitaires
      getNombrePoissonsActuels,
      getCapaciteRestante,
      getTauxOccupation,
      getOccupationClass,
      
      // Méthodes
      loadData,
      refreshData,
      resetFilters,
      confirmDelete,
      deleteBassin,
      viderBassin,
      confirmEmpty,
      toggleBassinStatus,
      truncateDescription,
      formatDate
    }
  }
}
</script>


<style scoped>
@import '../assets/styles/bassin-list';
</style>

