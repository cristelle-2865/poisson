<template>
  <div class="bassins-list">
    <!-- Header -->
    <div class="page-header">
      <div class="header-left">
        <h1>🏊 Gestion des Bassins</h1>
        <p class="subtitle">Suivez vos bassins et leurs poissons</p>
      </div>
      <div class="header-right">
        <button @click="refreshData" class="btn-refresh" :disabled="loading">
          🔄
        </button>
        <router-link to="/bassins/nouveau" class="btn-add">
          <span>+</span> Ajouter un bassin
        </router-link>
      </div>
    </div>

    <!-- Statistiques -->
    <div class="stats-container">
      <div class="stat-card" @click="filterByStatus('actif')">
        <div class="stat-icon">🏊</div>
        <div class="stat-content">
          <h3>Bassins actifs</h3>
          <div class="stat-value">{{ stats.actifs }}</div>
        </div>
      </div>
      
      <div class="stat-card" @click="filterByStatus('plein')">
        <div class="stat-icon">🐟</div>
        <div class="stat-content">
          <h3>Poissons total</h3>
          <div class="stat-value">{{ stats.poissons }}</div>
        </div>
      </div>
      
      <div class="stat-card" @click="filterByStatus('critique')">
        <div class="stat-icon">⚠️</div>
        <div class="stat-content">
          <h3>Bassins critiques</h3>
          <div class="stat-value warning">{{ stats.critiques }}</div>
        </div>
      </div>
      
      <div class="stat-card" @click="filterByStatus('maintenance')">
        <div class="stat-icon">🔧</div>
        <div class="stat-content">
          <h3>En maintenance</h3>
          <div class="stat-value">{{ stats.maintenance }}</div>
        </div>
      </div>
    </div>

    <!-- Filtres -->
    <div class="filters-container">
      <div class="search-box">
        <input 
          type="text" 
          v-model="searchQuery" 
          placeholder="Rechercher un bassin..."
          @input="filterBassins"
        />
        <span class="search-icon">🔍</span>
      </div>
      
      <div class="filter-group">
        <select v-model="filterStatus" @change="filterBassins">
          <option value="">Tous les statuts</option>
          <option value="actif">Actif</option>
          <option value="maintenance">Maintenance</option>
          <option value="inactif">Inactif</option>
        </select>
        
        <select v-model="filterType" @change="filterBassins">
          <option value="">Tous les types</option>
          <option value="beton">Béton</option>
          <option value="terre">Terre</option>
          <option value="fibre">Fibre de verre</option>
          <option value="plastique">Plastique</option>
        </select>
      </div>
    </div>

    <!-- Liste des bassins -->
    <div class="bassins-grid">
      <div v-for="bassin in filteredBassins" :key="bassin.id" class="bassin-card" 
           :class="getBassinClass(bassin)">
        <div class="bassin-header">
          <div class="bassin-title">
            <h3>{{ bassin.nom }}</h3>
            <span class="bassin-id">#{{ bassin.id }}</span>
          </div>
          <div class="bassin-status">
            <span class="status-badge" :class="getStatusClass(bassin)">
              {{ getStatusText(bassin) }}
            </span>
          </div>
        </div>
        
        <div class="bassin-info">
          <div class="info-row">
            <span class="label">Type:</span>
            <span class="value">{{ bassin.type }}</span>
          </div>
          <div class="info-row">
            <span class="label">Volume:</span>
            <span class="value">{{ bassin.volume }} m³</span>
          </div>
          <div class="info-row">
            <span class="label">Capacité max:</span>
            <span class="value">{{ bassin.capaciteMax }} poissons</span>
          </div>
          <div class="info-row">
            <span class="label">Poissons actuels:</span>
            <span class="value" :class="getOccupancyClass(bassin)">
              {{ bassin.poissonsActuels }} / {{ bassin.capaciteMax }}
            </span>
          </div>
        </div>
        
        <div class="bassin-progress">
          <div class="progress-label">
            <span>Taux d'occupation</span>
            <span>{{ getOccupancyPercentage(bassin) }}%</span>
          </div>
          <div class="progress-bar">
            <div class="progress-fill" :style="{ width: getOccupancyPercentage(bassin) + '%' }"
                 :class="getProgressClass(bassin)"></div>
          </div>
        </div>
        
        <div class="bassin-actions">
          <router-link :to="'/bassins/' + bassin.id" class="btn-view">
            👁️ Voir
          </router-link>
          <button @click="editBassin(bassin.id)" class="btn-edit">
            ✏️ Modifier
          </button>
          <button @click="nourrirBassin(bassin.id)" class="btn-feed" 
                  :disabled="bassin.statut === 'maintenance'">
            🍽️ Nourrir
          </button>
          <button @click="showPoissons(bassin.id)" class="btn-fish">
            🐟 Poissons
          </button>
        </div>
      </div>
      
      <div v-if="filteredBassins.length === 0" class="no-bassins">
        <div class="no-results-icon">🏊</div>
        <h3>Aucun bassin trouvé</h3>
        <p v-if="searchQuery || filterStatus || filterType">
          Essayez de modifier vos critères de recherche
        </p>
        <router-link to="/bassins/nouveau" class="btn-add-empty">
          Ajouter votre premier bassin
        </router-link>
      </div>
    </div>

    <!-- Quick Actions -->
    <div class="quick-actions-bar">
      <h3>Actions rapides</h3>
      <div class="actions-grid">
        <button @click="nourrirTous" class="action-btn primary" :disabled="loading">
          🍽️ Nourrir tous les bassins
        </button>
        <button @click="genererRapport" class="action-btn info">
          📊 Générer rapport
        </button>
        <button @click="planifierMaintenance" class="action-btn warning">
          🔧 Planifier maintenance
        </button>
        <button @click="exporterDonnees" class="action-btn success">
          📤 Exporter données
        </button>
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
    
    // État
    const bassins = ref([])
    const filteredBassins = ref([])
    const searchQuery = ref('')
    const filterStatus = ref('')
    const filterType = ref('')
    const loading = ref(false)
    
    // Données simulées (en attendant l'API)
    const sampleBassins = [
      {
        id: 1,
        nom: 'Bassin Principal',
        type: 'beton',
        volume: 50,
        capaciteMax: 100,
        poissonsActuels: 85,
        statut: 'actif',
        temperature: 26,
        ph: 7.2,
        dateCreation: '2024-01-15'
      },
      {
        id: 2,
        nom: 'Bassin Reproduction',
        type: 'fibre',
        volume: 20,
        capaciteMax: 40,
        poissonsActuels: 35,
        statut: 'actif',
        temperature: 28,
        ph: 7.0,
        dateCreation: '2024-02-10'
      },
      {
        id: 3,
        nom: 'Bassin Croissance',
        type: 'plastique',
        volume: 30,
        capaciteMax: 60,
        poissonsActuels: 58,
        statut: 'critique',
        temperature: 25,
        ph: 6.8,
        dateCreation: '2024-01-20'
      },
      {
        id: 4,
        nom: 'Bassin Quarantaine',
        type: 'terre',
        volume: 10,
        capaciteMax: 20,
        poissonsActuels: 5,
        statut: 'maintenance',
        temperature: 24,
        ph: 7.5,
        dateCreation: '2024-03-01'
      },
      {
        id: 5,
        nom: 'Bassin Alevins',
        type: 'fibre',
        volume: 15,
        capaciteMax: 30,
        poissonsActuels: 25,
        statut: 'actif',
        temperature: 27,
        ph: 7.1,
        dateCreation: '2024-02-15'
      }
    ]
    
    // Computed
    const stats = computed(() => {
      const actifs = bassins.value.filter(b => b.statut === 'actif').length
      const critiques = bassins.value.filter(b => b.statut === 'critique').length
      const maintenance = bassins.value.filter(b => b.statut === 'maintenance').length
      const poissons = bassins.value.reduce((sum, b) => sum + b.poissonsActuels, 0)
      
      return { actifs, critiques, maintenance, poissons }
    })
    
    // Méthodes
    const loadBassins = async () => {
      loading.value = true
      try {
        // Simuler l'appel API
        await new Promise(resolve => setTimeout(resolve, 500))
        
        // Pour l'instant, utiliser les données simulées
        bassins.value = sampleBassins
        filteredBassins.value = sampleBassins
        
      } catch (error) {
        console.error('Erreur chargement bassins:', error)
        alert('Impossible de charger les bassins')
      } finally {
        loading.value = false
      }
    }
    
    const filterBassins = () => {
      let filtered = [...bassins.value]
      
      // Recherche par nom
      if (searchQuery.value) {
        const query = searchQuery.value.toLowerCase()
        filtered = filtered.filter(b => 
          b.nom.toLowerCase().includes(query)
        )
      }
      
      // Filtre par statut
      if (filterStatus.value) {
        filtered = filtered.filter(b => b.statut === filterStatus.value)
      }
      
      // Filtre par type
      if (filterType.value) {
        filtered = filtered.filter(b => b.type === filterType.value)
      }
      
      filteredBassins.value = filtered
    }
    
    const getBassinClass = (bassin) => {
      if (bassin.statut === 'critique') return 'bassin-critique'
      if (bassin.statut === 'maintenance') return 'bassin-maintenance'
      if (getOccupancyPercentage(bassin) >= 90) return 'bassin-plein'
      return ''
    }
    
    const getStatusClass = (bassin) => {
      return `status-${bassin.statut}`
    }
    
    const getStatusText = (bassin) => {
      const statusMap = {
        'actif': 'Actif',
        'critique': 'Critique',
        'maintenance': 'Maintenance',
        'inactif': 'Inactif'
      }
      return statusMap[bassin.statut] || bassin.statut
    }
    
    const getOccupancyPercentage = (bassin) => {
      return Math.round((bassin.poissonsActuels / bassin.capaciteMax) * 100)
    }
    
    const getOccupancyClass = (bassin) => {
      const percentage = getOccupancyPercentage(bassin)
      if (percentage >= 90) return 'occupancy-high'
      if (percentage >= 70) return 'occupancy-medium'
      return 'occupancy-low'
    }
    
    const getProgressClass = (bassin) => {
      const percentage = getOccupancyPercentage(bassin)
      if (percentage >= 90) return 'progress-high'
      if (percentage >= 70) return 'progress-medium'
      return 'progress-low'
    }
    
    const filterByStatus = (status) => {
      filterStatus.value = status
      filterBassins()
    }
    
    const editBassin = (id) => {
      router.push(`/bassins/${id}?edit=true`)
    }
    
    const nourrirBassin = (id) => {
      router.push(`/nourrissage?bassin=${id}`)
    }
    
    const showPoissons = (id) => {
      router.push(`/poissons?bassin=${id}`)
    }
    
    const nourrirTous = () => {
      router.push('/nourrissage')
    }
    
    const genererRapport = () => {
      alert('Fonctionnalité rapport à implémenter')
    }
    
    const planifierMaintenance = () => {
      alert('Fonctionnalité maintenance à implémenter')
    }
    
    const exporterDonnees = () => {
      const csvContent = [
        ['ID', 'Nom', 'Type', 'Volume', 'Capacité', 'Poissons', 'Statut', 'Taux occupation'],
        ...bassins.value.map(b => [
          b.id,
          b.nom,
          b.type,
          b.volume,
          b.capaciteMax,
          b.poissonsActuels,
          getStatusText(b),
          `${getOccupancyPercentage(b)}%`
        ])
      ].map(row => row.join(',')).join('\n')
      
      const blob = new Blob([csvContent], { type: 'text/csv' })
      const url = URL.createObjectURL(blob)
      const link = document.createElement('a')
      link.href = url
      link.download = `bassins_${new Date().toISOString().split('T')[0]}.csv`
      link.click()
    }
    
    const refreshData = () => {
      loadBassins()
    }
    
    // Initialisation
    onMounted(() => {
      loadBassins()
    })
    
    return {
      bassins,
      filteredBassins,
      searchQuery,
      filterStatus,
      filterType,
      loading,
      stats,
      filterBassins,
      getBassinClass,
      getStatusClass,
      getStatusText,
      getOccupancyPercentage,
      getOccupancyClass,
      getProgressClass,
      filterByStatus,
      editBassin,
      nourrirBassin,
      showPoissons,
      nourrirTous,
      genererRapport,
      planifierMaintenance,
      exporterDonnees,
      refreshData
    }
  }
}
</script>

<style scoped>
.bassins-list {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 30px;
  background: white;
  padding: 25px;
  border-radius: 12px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.05);
}

.header-left h1 {
  margin: 0 0 10px 0;
  color: #2d3748;
}

.subtitle {
  color: #718096;
  margin: 0;
}

.header-right {
  display: flex;
  gap: 15px;
}

.btn-refresh, .btn-add {
  padding: 10px 20px;
  border-radius: 8px;
  font-weight: 600;
  text-decoration: none;
  display: flex;
  align-items: center;
  gap: 8px;
}

.btn-refresh {
  background: #e2e8f0;
  color: #4a5568;
  border: none;
  cursor: pointer;
}

.btn-refresh:hover:not(:disabled) {
  background: #cbd5e0;
}

.btn-refresh:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.btn-add {
  background: #48bb78;
  color: white;
}

.btn-add:hover {
  background: #38a169;
}

.stats-container {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
  margin-bottom: 30px;
}

.stat-card {
  background: white;
  padding: 20px;
  border-radius: 12px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.05);
  display: flex;
  align-items: center;
  gap: 20px;
  cursor: pointer;
  transition: transform 0.2s;
}

.stat-card:hover {
  transform: translateY(-5px);
}

.stat-icon {
  font-size: 40px;
}

.stat-content h3 {
  margin: 0 0 5px 0;
  font-size: 14px;
  color: #718096;
}

.stat-value {
  font-size: 32px;
  font-weight: bold;
  color: #2d3748;
}

.stat-value.warning {
  color: #ed8936;
}

.filters-container {
  background: white;
  padding: 25px;
  border-radius: 12px;
  margin-bottom: 30px;
  display: flex;
  gap: 20px;
  align-items: center;
  flex-wrap: wrap;
}

.search-box {
  flex: 1;
  min-width: 300px;
  position: relative;
}

.search-box input {
  width: 100%;
  padding: 12px 15px;
  padding-left: 40px;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  font-size: 16px;
}

.search-icon {
  position: absolute;
  left: 12px;
  top: 50%;
  transform: translateY(-50%);
  color: #718096;
}

.filter-group {
  display: flex;
  gap: 15px;
  flex-wrap: wrap;
}

.filter-group select {
  padding: 12px 15px;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  min-width: 180px;
  cursor: pointer;
}

.bassins-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 25px;
  margin-bottom: 40px;
}

.bassin-card {
  background: white;
  border-radius: 12px;
  padding: 25px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.05);
  transition: all 0.3s;
  border: 2px solid transparent;
}

.bassin-card:hover {
  box-shadow: 0 10px 25px rgba(0,0,0,0.1);
  transform: translateY(-5px);
}

.bassin-card.bassin-critique {
  border-color: #f56565;
  background: linear-gradient(to right, white, #fff5f5);
}

.bassin-card.bassin-maintenance {
  border-color: #ed8936;
  background: linear-gradient(to right, white, #fffaf0);
}

.bassin-card.bassin-plein {
  border-color: #ecc94b;
  background: linear-gradient(to right, white, #fefcbf);
}

.bassin-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 20px;
}

.bassin-title h3 {
  margin: 0;
  color: #2d3748;
  font-size: 20px;
}

.bassin-id {
  color: #718096;
  font-size: 14px;
}

.status-badge {
  padding: 6px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
}

.status-actif {
  background: #c6f6d5;
  color: #22543d;
}

.status-critique {
  background: #fed7d7;
  color: #742a2a;
}

.status-maintenance {
  background: #feebc8;
  color: #744210;
}

.status-inactif {
  background: #e2e8f0;
  color: #4a5568;
}

.bassin-info {
  margin-bottom: 25px;
}

.info-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
  padding-bottom: 10px;
  border-bottom: 1px solid #f7fafc;
}

.info-row:last-child {
  border-bottom: none;
  margin-bottom: 0;
  padding-bottom: 0;
}

.label {
  color: #718096;
  font-size: 14px;
}

.value {
  font-weight: 600;
  color: #2d3748;
}

.occupancy-high { color: #f56565; }
.occupancy-medium { color: #ecc94b; }
.occupancy-low { color: #48bb78; }

.bassin-progress {
  margin-bottom: 25px;
}

.progress-label {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.progress-label span:first-child {
  color: #4a5568;
  font-size: 14px;
}

.progress-label span:last-child {
  font-weight: bold;
  color: #2d3748;
}

.progress-bar {
  height: 10px;
  background: #e2e8f0;
  border-radius: 5px;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  transition: width 0.3s;
}

.progress-high { background: #f56565; }
.progress-medium { background: #ecc94b; }
.progress-low { background: #48bb78; }

.bassin-actions {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.bassin-actions button,
.bassin-actions .btn-view {
  padding: 8px 15px;
  border-radius: 6px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  text-decoration: none;
  display: inline-flex;
  align-items: center;
  gap: 5px;
  transition: all 0.2s;
}

.btn-view {
  background: #4299e1;
  color: white;
  border: none;
}

.btn-view:hover {
  background: #3182ce;
}

.btn-edit {
  background: #e2e8f0;
  color: #4a5568;
  border: none;
}

.btn-edit:hover {
  background: #cbd5e0;
}

.btn-feed {
  background: #48bb78;
  color: white;
  border: none;
}

.btn-feed:hover:not(:disabled) {
  background: #38a169;
}

.btn-feed:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.btn-fish {
  background: #9f7aea;
  color: white;
  border: none;
}

.btn-fish:hover {
  background: #805ad5;
}

.no-bassins {
  grid-column: 1 / -1;
  text-align: center;
  padding: 60px 20px;
  background: white;
  border-radius: 12px;
  border: 2px dashed #e2e8f0;
}

.no-results-icon {
  font-size: 60px;
  margin-bottom: 20px;
}

.no-bassins h3 {
  margin: 0 0 10px 0;
  color: #2d3748;
}

.no-bassins p {
  color: #718096;
  margin-bottom: 30px;
}

.btn-add-empty {
  padding: 12px 24px;
  background: #48bb78;
  color: white;
  text-decoration: none;
  border-radius: 8px;
  font-weight: 600;
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

.btn-add-empty:hover {
  background: #38a169;
}

.quick-actions-bar {
  background: white;
  padding: 25px;
  border-radius: 12px;
  margin-top: 30px;
}

.quick-actions-bar h3 {
  margin: 0 0 20px 0;
  color: #2d3748;
}

.actions-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 15px;
}

.action-btn {
  padding: 15px;
  border: none;
  border-radius: 8px;
  font-weight: 600;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  transition: all 0.2s;
}

.action-btn.primary {
  background: #4299e1;
  color: white;
}

.action-btn.primary:hover:not(:disabled) {
  background: #3182ce;
}

.action-btn.info {
  background: #0bc5ea;
  color: white;
}

.action-btn.info:hover {
  background: #00a3c4;
}

.action-btn.warning {
  background: #ed8936;
  color: white;
}

.action-btn.warning:hover {
  background: #dd6b20;
}

.action-btn.success {
  background: #48bb78;
  color: white;
}

.action-btn.success:hover {
  background: #38a169;
}

.action-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    gap: 15px;
  }
  
  .header-right {
    width: 100%;
  }
  
  .search-box {
    min-width: 100%;
  }
  
  .filter-group {
    width: 100%;
  }
  
  .filter-group select {
    flex: 1;
    min-width: 0;
  }
  
  .bassins-grid {
    grid-template-columns: 1fr;
  }
  
  .actions-grid {
    grid-template-columns: 1fr;
  }
}
</style>

