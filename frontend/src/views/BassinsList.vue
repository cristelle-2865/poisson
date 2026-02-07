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
              <span class="info-value">{{ bassin.nombrePoissonsActuels || 0 }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">Places restantes</span>
              <span class="info-value">{{ bassin.capaciteRestante || 0 }}</span>
            </div>
          </div>

          <!-- Barre de progression d'occupation -->
          <div class="occupation-section">
            <div class="occupation-header">
              <span>Taux d'occupation</span>
              <span class="occupation-percent">{{ (bassin.tauxOccupation || 0).toFixed(1) }}%</span>
            </div>
            <div class="occupation-bar">
              <div 
                class="occupation-fill" 
                :style="{ width: (bassin.tauxOccupation || 0) + '%' }"
                :class="getOccupationClass(bassin.tauxOccupation || 0)"
              ></div>
            </div>
            <div class="occupation-text">
              {{ bassin.nombrePoissonsActuels || 0 }} / {{ bassin.capaciteMaxPiscine }}
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
          </div>
        </div>

        <!-- Pied de la carte -->
        <div class="bassin-footer">
          <button @click="viderBassin(bassin)" class="btn-action" :disabled="!bassin.nombrePoissonsActuels">
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
          
          <div v-if="bassinToDelete?.nombrePoissonsActuels > 0" class="warning-alert">
            ⚠️ <strong>Attention !</strong> Ce bassin contient {{ bassinToDelete?.nombrePoissonsActuels }} poisson(s).
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
            :disabled="bassinToDelete?.nombrePoissonsActuels > 0"
          >
            {{ bassinToDelete?.nombrePoissonsActuels > 0 ? 'Impossible (bassin non vide)' : 'Supprimer définitivement' }}
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
          <p>Cette action retirera <strong>{{ bassinToEmpty?.nombrePoissonsActuels }} poisson(s)</strong> du bassin.</p>
          
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
    
    // Charger les données
    const loadData = async () => {
      isLoading.value = true
      try {
        const bassinsData = await bassinService.getAll()
        bassins.value = Array.isArray(bassinsData) ? bassinsData : []
        
        // Calculer les statistiques pour chaque bassin
        bassins.value = bassins.value.map(bassin => {
          const nombrePoissonsActuels = bassin.poissons?.length || 0
          const capaciteRestante = Math.max(0, bassin.capaciteMaxPiscine - nombrePoissonsActuels)
          const tauxOccupation = bassin.capaciteMaxPiscine > 0 
            ? (nombrePoissonsActuels / bassin.capaciteMaxPiscine) * 100 
            : 0
            
          return {
            ...bassin,
            nombrePoissonsActuels,
            capaciteRestante,
            tauxOccupation: parseFloat(tauxOccupation.toFixed(1))
          }
        })
      } catch (error) {
        console.error('Erreur chargement bassins:', error)
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
            const taux = bassin.tauxOccupation || 0
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
          switch (sortBy.value) {
            case 'nomDesc':
              return b.nomPiscine?.localeCompare(a.nomPiscine || '') || 0
            case 'occupationDesc':
              return (b.tauxOccupation || 0) - (a.tauxOccupation || 0)
            case 'occupationAsc':
              return (a.tauxOccupation || 0) - (b.tauxOccupation || 0)
            case 'poissonsDesc':
              return (b.nombrePoissonsActuels || 0) - (a.nombrePoissonsActuels || 0)
            case 'poissonsAsc':
              return (a.nombrePoissonsActuels || 0) - (b.nombrePoissonsActuels || 0)
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
    const totalPoissons = computed(() => bassins.value.reduce((sum, b) => sum + (b.nombrePoissonsActuels || 0), 0))
    const tauxOccupationMoyen = computed(() => {
      if (bassins.value.length === 0) return 0
      const total = bassins.value.reduce((sum, b) => sum + (b.tauxOccupation || 0), 0)
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
      const date = new Date(dateString)
      return date.toLocaleDateString('fr-FR')
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
      if (!bassin.nombrePoissonsActuels) {
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
      
      // Méthodes
      loadData,
      refreshData,
      resetFilters,
      confirmDelete,
      deleteBassin,
      viderBassin,
      confirmEmpty,
      toggleBassinStatus,
      getOccupationClass,
      truncateDescription,
      formatDate
    }
  }
}
</script>

<style scoped>
.bassins-list {
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
  background: #e2e8f0;
  color: #4a5568;
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
  background: #cbd5e0;
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

/* Filtres */
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

/* Grille des bassins */
.bassins-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 25px;
  margin-bottom: 30px;
}

.bassin-card {
  background: white;
  border-radius: 12px;
  box-shadow: 0 4px 6px rgba(0,0,0,0.1);
  overflow: hidden;
  display: flex;
  flex-direction: column;
  transition: transform 0.3s, box-shadow 0.3s;
}

.bassin-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 25px rgba(0,0,0,0.15);
}

/* En-tête de la carte */
.bassin-header {
  padding: 20px;
  background: #f7fafc;
  border-bottom: 1px solid #e2e8f0;
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.bassin-title {
  flex: 1;
}

.bassin-title h3 {
  margin: 0 0 10px 0;
  color: #2d3748;
  font-size: 20px;
}

.bassin-status {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.status-badge {
  padding: 5px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
}

.status-active {
  background: #c6f6d5;
  color: #22543d;
}

.status-inactive {
  background: #fed7d7;
  color: #742a2a;
}

.bassin-actions {
  display: flex;
  gap: 8px;
}

.btn-icon {
  background: none;
  border: none;
  font-size: 18px;
  cursor: pointer;
  padding: 8px;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background 0.2s;
  text-decoration: none;
  color: #4a5568;
}

.btn-icon:hover {
  background: #e2e8f0;
}

.btn-delete:hover {
  background: #fed7d7;
  color: #f56565;
}

/* Corps de la carte */
.bassin-body {
  padding: 20px;
  flex: 1;
}

.bassin-info {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 15px;
  margin-bottom: 20px;
  background: #f7fafc;
  padding: 15px;
  border-radius: 8px;
}

.info-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
}

.info-label {
  font-size: 12px;
  color: #718096;
  margin-bottom: 4px;
}

.info-value {
  font-weight: bold;
  color: #2d3748;
  font-size: 18px;
}

/* Section occupation */
.occupation-section {
  margin-bottom: 20px;
}

.occupation-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.occupation-percent {
  font-weight: bold;
  color: #2d3748;
  font-size: 16px;
}

.occupation-bar {
  height: 10px;
  background: #e2e8f0;
  border-radius: 5px;
  overflow: hidden;
  margin-bottom: 5px;
}

.occupation-fill {
  height: 100%;
  border-radius: 5px;
  transition: width 0.3s;
}

.occupation-empty { background: #a0aec0; }
.occupation-low { background: #48bb78; }
.occupation-medium { background: #ed8936; }
.occupation-high { background: #f56565; }
.occupation-full { background: #742a2a; }

.occupation-text {
  text-align: center;
  font-size: 14px;
  color: #718096;
}

/* Caractéristiques techniques */
.tech-specs {
  margin-bottom: 20px;
}

.tech-specs h4 {
  margin: 0 0 10px 0;
  color: #4a5568;
  font-size: 16px;
}

.specs-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(100px, 1fr));
  gap: 15px;
  background: #f7fafc;
  padding: 15px;
  border-radius: 8px;
}

.spec-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
}

.spec-label {
  font-size: 12px;
  color: #718096;
  margin-bottom: 4px;
}

.spec-value {
  font-weight: 600;
  color: #2d3748;
  font-size: 14px;
}

/* Description */
.description-section {
  margin-bottom: 20px;
}

.description-section h4 {
  margin: 0 0 10px 0;
  color: #4a5568;
  font-size: 16px;
}

.description-text {
  margin: 0;
  color: #718096;
  font-size: 14px;
  line-height: 1.5;
  background: #f7fafc;
  padding: 15px;
  border-radius: 8px;
}

/* Dates */
.dates-section {
  background: #f7fafc;
  padding: 15px;
  border-radius: 8px;
}

.date-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.date-label {
  color: #718096;
  font-size: 14px;
}

.date-value {
  font-weight: 600;
  color: #2d3748;
  font-size: 14px;
}

/* Pied de la carte */
.bassin-footer {
  padding: 20px;
  background: #f7fafc;
  border-top: 1px solid #e2e8f0;
  display: flex;
  gap: 10px;
}

.btn-action {
  flex: 1;
  padding: 10px 15px;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-weight: 600;
  font-size: 14px;
  text-align: center;
  text-decoration: none;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.btn-action:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.btn-action:not(.btn-primary) {
  background: #e2e8f0;
  color: #4a5568;
}

.btn-action:not(.btn-primary):hover:not(:disabled) {
  background: #cbd5e0;
}

.btn-action.btn-primary {
  background: #4299e1;
  color: white;
}

.btn-action.btn-primary:hover:not(:disabled) {
  background: #3182ce;
}

/* Chargement */
.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px;
  background: white;
  border-radius: 12px;
}

.spinner {
  width: 50px;
  height: 50px;
  border: 4px solid #e2e8f0;
  border-top-color: #4299e1;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 20px;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

/* Aucun résultat */
.no-results {
  padding: 60px 20px;
  text-align: center;
  background: white;
  border-radius: 12px;
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

/* Alertes */
.warning-alert {
  background: #fffaf0;
  border-left: 4px solid #ed8936;
  padding: 15px;
  margin: 15px 0;
  border-radius: 4px;
  color: #744210;
}

.text-danger {
  color: #f56565;
  font-weight: 600;
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

.btn-danger:hover:not(:disabled) {
  background: #e53e3e;
}

.btn-danger:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.btn-warning {
  padding: 10px 20px;
  background: #ed8936;
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-weight: 600;
}

.btn-warning:hover {
  background: #dd6b20;
}

/* Responsive */
@media (max-width: 1024px) {
  .bassins-grid {
    grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
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
  
  .bassins-grid {
    grid-template-columns: 1fr;
  }
  
  .bassin-info {
    grid-template-columns: repeat(3, 1fr);
  }
  
  .specs-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .bassin-footer {
    flex-direction: column;
  }
}

@media (max-width: 480px) {
  .bassin-info {
    grid-template-columns: 1fr;
    gap: 10px;
  }
  
  .specs-grid {
    grid-template-columns: 1fr;
  }
}
</style>