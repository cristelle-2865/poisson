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
          <h3>Aliments actifs</h3>
          <div class="stat-value">{{ alimentsActifsCount }}</div>
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
              <th @click="sortBy('prixKgAliment')" class="sortable">
                Prix/kg
                <span v-if="sortField === 'prixKgAliment'" class="sort-icon">
                  {{ sortDirection === 'asc' ? '↑' : '↓' }}
                </span>
              </th>
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
                <span class="nutrient-value">
                  {{ formatNutrient(aliment.proteinesParKgAliment) }} g/kg
                </span>
              </td>
              <td>
                <span class="nutrient-value">
                  {{ formatNutrient(aliment.glucidesParKgAliment) }} g/kg
                </span>
              </td>
              <td>
                <span class="price-value">
                  {{ formatPrice(aliment.prixKgAliment) }} MGA
                </span>
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
        <p v-if="searchQuery || filterStatus || filterStock">
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
                     step="0.1" min="0" required>
            </div>
            
            <div class="form-group">
              <label>Glucides (g/kg) *</label>
              <input type="number" v-model="currentAliment.glucidesParKgAliment" 
                     step="0.1" min="0" required>
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
          
          <div class="form-preview">
            <h4>Résumé nutritionnel</h4>
            <div class="preview-grid">
              <div class="preview-item">
                <span class="preview-label">Protéines totales:</span>
                <span class="preview-value">
                  {{ calculateProteinesTotales() }} g
                </span>
              </div>
              <div class="preview-item">
                <span class="preview-label">Glucides totales:</span>
                <span class="preview-value">
                  {{ calculateGlucidesTotales() }} g
                </span>
              </div>
              <div class="preview-item">
                <span class="preview-label">Valeur stock:</span>
                <span class="preview-value">
                  {{ calculateValeurPreview() }} MGA
                </span>
              </div>
            </div>
          </div>
          
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
      <button @click="reorderAllLowStock" class="btn-batch" v-if="stocksBasCount > 0">
        📋 Générer commande pour stocks bas
      </button>
    </div>
  </div>
</template>


<script>
import { ref, computed, onMounted } from 'vue'
import api from '../services/api'

export default {
  name: 'Aliments',
  setup() {
    // Données
    const aliments = ref([])
    const filteredAliments = ref([])
    const searchQuery = ref('')
    const filterStatus = ref('')
    const filterStock = ref('')
    const sortField = ref('nomAliment')
    const sortDirection = ref('asc')
    const currentPage = ref(1)
    const itemsPerPage = ref(25)
    const loading = ref(false)
    
    // Modales
    const showAlimentModal = ref(false)
    const showStockManagementModal = ref(false)
    const showDeleteModal = ref(false)
    const isEditing = ref(false)
    
    // Données courantes
    const currentAliment = ref({
      nomAliment: '',
      proteinesParKgAliment: 10,
      glucidesParKgAliment: 10,
      prixKgAliment: 2000,
      stockAliment: 0,
      seuilMinimumAliment: 10,
      estActifAliment: true
    })
    
    const alimentToDelete = ref(null)
    const stockAction = ref('add')
    const stockQuantity = ref(0)

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

    const alimentsActifsCount = computed(() => {
      return aliments.value.filter(aliment => aliment.estActifAliment).length
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

    const formatNutrient = (nutrient) => {
      if (!nutrient && nutrient !== 0) return '0.00'
      return parseFloat(nutrient).toFixed(2)
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
        prixKgAliment: 2000,
        stockAliment: 0,
        seuilMinimumAliment: 10,
        estActifAliment: true
      }
      showAlimentModal.value = true
    }

    // Modifier un aliment
    const editAliment = (aliment) => {
      isEditing.value = true
      currentAliment.value = { ...aliment }
      showAlimentModal.value = true
    }

    // Calculs pour le formulaire
    const calculateProteinesTotales = () => {
      const stock = parseFloat(currentAliment.value.stockAliment || 0)
      const proteines = parseFloat(currentAliment.value.proteinesParKgAliment || 0)
      return (stock * proteines).toFixed(1)
    }

    const calculateGlucidesTotales = () => {
      const stock = parseFloat(currentAliment.value.stockAliment || 0)
      const glucides = parseFloat(currentAliment.value.glucidesParKgAliment || 0)
      return (stock * glucides).toFixed(1)
    }

    const calculateValeurPreview = () => {
      const stock = parseFloat(currentAliment.value.stockAliment || 0)
      const prix = parseFloat(currentAliment.value.prixKgAliment || 0)
      return (stock * prix).toFixed(0).replace(/\B(?=(\d{3})+(?!\d))/g, ' ')
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
      const headers = ['Nom', 'Stock (kg)', 'Protéines (g/kg)', 'Glucides (g/kg)', 
                      'Prix/kg (MGA)', 'Seuil min (kg)', 'Statut', 'Valeur stock (MGA)']
      
      const csvData = filteredAliments.value.map(aliment => [
        aliment.nomAliment,
        formatStock(aliment.stockAliment),
        formatNutrient(aliment.proteinesParKgAliment),
        formatNutrient(aliment.glucidesParKgAliment),
        aliment.prixKgAliment,
        formatStock(aliment.seuilMinimumAliment),
        getStatusText(aliment),
        calculateValeurStock(aliment)
      ])

      const csvContent = [
        headers.join(','),
        ...csvData.map(row => row.join(','))
      ].join('\n')

      const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' })
      const link = document.createElement('a')
      link.href = URL.createObjectURL(blob)
      link.download = `aliments_${new Date().toISOString().split('T')[0]}.csv`
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
      currentPage,
      itemsPerPage,
      totalPages,
      loading,
      showAlimentModal,
      showStockManagementModal,
      showDeleteModal,
      isEditing,
      currentAliment,
      alimentToDelete,
      stockAction,
      stockQuantity,
      
      // Computed
      totalStock,
      stocksBasCount,
      valeurStock,
      alimentsActifsCount,
      
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
      getStockPercentage,
      isStockLow,
      calculateValeurStock,
      prevPage,
      nextPage,
      resetPagination,
      refreshData,
      showAddModal,
      editAliment,
      calculateProteinesTotales,
      calculateGlucidesTotales,
      calculateValeurPreview,
      saveAliment,
      closeModal,
      toggleActive,
      showStockModal,
      calculateNewStock,
      updateStock,
      closeStockModal,
      deleteAliment,
      confirmDelete,
      cancelDelete,
      exportToCSV,
      reorderAllLowStock
    }
  }
}
</script>

<style scoped>
@import '../assets/styles/aliment.css';
</style>

