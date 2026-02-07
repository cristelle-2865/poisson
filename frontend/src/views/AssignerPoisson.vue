<template>
  <div class="assigner-poisson">
    <div class="page-header">
      <h1>Assigner un poisson à un bassin</h1>
      <div class="header-actions">
        <button @click="router.back()" class="btn-back">
          ← Retour
        </button>
      </div>
    </div>

    <!-- Sélection du bassin -->
    <div class="selection-section">
      <h2>1. Sélectionnez un bassin</h2>
      
      <div class="bassin-selection">
        <div class="search-box">
          <input 
            type="text" 
            v-model="searchBassin" 
            placeholder="Rechercher un bassin..."
            @input="filterBassins"
          />
          <span class="search-icon">🔍</span>
        </div>

        <div class="bassins-grid">
          <div v-for="bassin in filteredBassins" :key="bassin.idPiscine" 
               class="bassin-card"
               :class="{ 
                 'selected': selectedBassin?.idPiscine === bassin.idPiscine,
                 'full': (bassin.nombrePoissonsActuels || 0) >= bassin.capaciteMaxPiscine,
                 'inactive': !bassin.estActivePiscine
               }"
               @click="selectBassin(bassin)">
            
            <div class="bassin-card-header">
              <h3>{{ bassin.nomPiscine }}</h3>
              <span :class="bassin.estActivePiscine ? 'status-active' : 'status-inactive'">
                {{ bassin.estActivePiscine ? 'Actif' : 'Inactif' }}
              </span>
            </div>

            <div class="bassin-info">
              <div class="info-row">
                <span class="info-label">Capacité:</span>
                <span class="info-value">{{ bassin.capaciteMaxPiscine }} poissons</span>
              </div>
              
              <div class="info-row">
                <span class="info-label">Occupation:</span>
                <span class="info-value">
                  {{ bassin.nombrePoissonsActuels || 0 }}/{{ bassin.capaciteMaxPiscine }}
                </span>
              </div>

              <div class="progress-container">
                <div class="progress-bar" :style="{ 
                  width: Math.min(100, ((bassin.nombrePoissonsActuels || 0) / bassin.capaciteMaxPiscine) * 100) + '%'
                }"></div>
              </div>

              <div v-if="bassin.descriptionPiscine" class="description">
                {{ bassin.descriptionPiscine }}
              </div>
            </div>

            <div v-if="(bassin.nombrePoissonsActuels || 0) >= bassin.capaciteMaxPiscine" 
                 class="full-warning">
              ⚠️ Capacité maximale atteinte
            </div>
          </div>
        </div>

        <div v-if="filteredBassins.length === 0" class="no-results">
          Aucun bassin disponible
        </div>
      </div>
    </div>

    <!-- Sélection du poisson -->
    <div class="selection-section">
      <h2>2. Sélectionnez un poisson</h2>
      
      <div class="filters-container">
        <div class="filter-group">
          <select v-model="filterRace" @change="filterPoissons">
            <option value="">Toutes les races</option>
            <option v-for="race in races" :key="race.idRacePoisson" :value="race.idRacePoisson">
              {{ race.nomRacePoisson }}
            </option>
          </select>
        </div>
      </div>

      <div class="poissons-grid">
        <div v-for="poisson in filteredPoissons" :key="poisson.idPoisson" 
             class="poisson-card"
             :class="{ 
               'selected': selectedPoisson?.idPoisson === poisson.idPoisson,
               'has-bassin': poisson.piscineActuelle
             }"
             @click="selectPoisson(poisson)">
          
          <div class="poisson-header">
            <h3>{{ poisson.nomPoisson }}</h3>
            <span class="poisson-status" :class="getStatusClass(poisson)">
              {{ getStatusText(poisson) }}
            </span>
          </div>

          <div class="poisson-info">
            <div class="info-row">
              <span class="info-label">Race:</span>
              <span class="info-value">{{ poisson.racePoisson?.nomRacePoisson || 'N/A' }}</span>
            </div>
            
            <div class="info-row">
              <span class="info-label">Poids:</span>
              <span class="info-value">{{ Math.round(poisson.poidsActuelPoisson) }}g</span>
            </div>

            <div class="info-row">
              <span class="info-label">Progression:</span>
              <span class="info-value">{{ getProgression(poisson) }}%</span>
            </div>

            <div v-if="poisson.piscineActuelle" class="current-bassin">
              <span class="info-label">Bassin actuel:</span>
              <span class="info-value bassin-current">
                {{ poisson.piscineActuelle.nomPiscine }}
              </span>
            </div>
            <div v-else class="no-bassin">
              <span class="info-label">Bassin:</span>
              <span class="info-value bassin-none">Non assigné</span>
            </div>
          </div>
        </div>
      </div>

      <div v-if="filteredPoissons.length === 0" class="no-results">
        Aucun poisson disponible pour l'assignation
      </div>
    </div>

    <!-- Confirmation -->
    <div class="confirmation-section" v-if="selectedBassin && selectedPoisson">
      <h2>3. Confirmation</h2>
      
      <div class="confirmation-details">
        <div class="confirmation-item">
          <h3>Poisson sélectionné:</h3>
          <p><strong>{{ selectedPoisson.nomPoisson }}</strong></p>
          <p>Race: {{ selectedPoisson.racePoisson?.nomRacePoisson }}</p>
          <p>Poids: {{ Math.round(selectedPoisson.poidsActuelPoisson) }}g</p>
          <p v-if="selectedPoisson.piscineActuelle">
            Actuellement dans: <strong>{{ selectedPoisson.piscineActuelle.nomPiscine }}</strong>
          </p>
        </div>

        <div class="confirmation-item">
          <h3>Bassin sélectionné:</h3>
          <p><strong>{{ selectedBassin.nomPiscine }}</strong></p>
          <p>Capacité: {{ selectedBassin.capaciteMaxPiscine }} poissons</p>
          <p>Occupation: {{ selectedBassin.nombrePoissonsActuels || 0 }}/{{ selectedBassin.capaciteMaxPiscine }}</p>
          <p v-if="selectedPoisson.piscineActuelle">
            Le poisson sera transféré de {{ selectedPoisson.piscineActuelle.nomPiscine }}
          </p>
        </div>
      </div>

      <div class="confirmation-actions">
        <button @click="assigner" class="btn-confirm" :disabled="loading">
          {{ loading ? 'Assignation en cours...' : 'Confirmer l\'assignation' }}
        </button>
        <button @click="resetSelection" class="btn-reset">
          Réinitialiser la sélection
        </button>
      </div>
    </div>

    <!-- Chargement -->
    <div v-if="loading" class="loading-overlay">
      <div class="loading-spinner"></div>
      <p>Chargement...</p>
    </div>
  </div>
</template>

<script>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import piscineService from '../services/piscineService'
import poissonService from '../services/poissonService'

export default {
  name: 'AssignerPoisson',
  setup() {
    const router = useRouter()
    const route = useRoute()
    
    const bassins = ref([])
    const filteredBassins = ref([])
    const searchBassin = ref('')
    const selectedBassin = ref(null)
    
    const poissons = ref([])
    const filteredPoissons = ref([])
    const races = ref([])
    const filterRace = ref('')
    const selectedPoisson = ref(null)
    
    const loading = ref(false)

    // Charger les bassins
    const loadBassins = async () => {
      try {
        const bassinsData = await piscineService.getActivePiscines()
        bassins.value = bassinsData
        filteredBassins.value = bassinsData
        
        // Si un bassin est spécifié dans l'URL, le sélectionner
        if (route.query.piscine) {
          const bassinId = parseInt(route.query.piscine)
          const bassin = bassins.value.find(b => b.idPiscine === bassinId)
          if (bassin) {
            selectBassin(bassin)
          }
        }
      } catch (error) {
        console.error('Erreur chargement des bassins:', error)
        alert('Impossible de charger les bassins')
      }
    }

    // Charger les poissons
    const loadPoissons = async () => {
      try {
        // Charger les poissons en vie et non vendus
        const poissonsData = await poissonService.getPoissonsEnVie()
        poissons.value = poissonsData.filter(p => !p.estVenduPoisson)
        filteredPoissons.value = poissons.value

        // Charger les races
        const racesData = await poissonService.getRacesPoisson()
        races.value = racesData
      } catch (error) {
        console.error('Erreur chargement des poissons:', error)
        alert('Impossible de charger les poissons')
      }
    }

    // Filtrer les bassins
    const filterBassins = () => {
      if (!searchBassin.value) {
        filteredBassins.value = bassins.value
        return
      }
      
      const query = searchBassin.value.toLowerCase()
      filteredBassins.value = bassins.value.filter(bassin =>
        bassin.nomPiscine.toLowerCase().includes(query) ||
        (bassin.descriptionPiscine || '').toLowerCase().includes(query)
      )
    }

    // Filtrer les poissons
    const filterPoissons = () => {
      let filtered = poissons.value

      if (filterRace.value) {
        filtered = filtered.filter(p => 
          p.racePoisson?.idRacePoisson == filterRace.value
        )
      }

      filteredPoissons.value = filtered
    }

    // Sélectionner un bassin
    const selectBassin = (bassin) => {
      if (!bassin.estActivePiscine) {
        alert('Ce bassin est inactif')
        return
      }
      
      if ((bassin.nombrePoissonsActuels || 0) >= bassin.capaciteMaxPiscine) {
        alert('Ce bassin a atteint sa capacité maximale')
        return
      }
      
      selectedBassin.value = bassin
    }

    // Sélectionner un poisson
    const selectPoisson = (poisson) => {
      if (poisson.estVenduPoisson || !poisson.estEnViePoisson) {
        alert('Ce poisson n\'est pas disponible')
        return
      }
      
      selectedPoisson.value = poisson
    }

    // Calculer la progression
    const getProgression = (poisson) => {
      if (!poisson.poidsActuelPoisson || !poisson.poidsMaximalPoisson) return 0
      return Math.round((poisson.poidsActuelPoisson / poisson.poidsMaximalPoisson) * 100)
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

    // Assigner le poisson
    const assigner = async () => {
      if (!selectedBassin.value || !selectedPoisson.value) {
        alert('Veuillez sélectionner un poisson et un bassin')
        return
      }

      if (confirm(`Assigner le poisson "${selectedPoisson.value.nomPoisson}" au bassin "${selectedBassin.value.nomPiscine}" ?`)) {
        loading.value = true
        
        try {
          await piscineService.affecterPoisson(
            selectedBassin.value.idPiscine,
            selectedPoisson.value.idPoisson
          )
          
          alert('Poisson assigné avec succès !')
          
          // Recharger les données
          await Promise.all([loadBassins(), loadPoissons()])
          
          // Réinitialiser la sélection
          selectedBassin.value = null
          selectedPoisson.value = null
          
        } catch (error) {
          console.error('Erreur lors de l\'assignation:', error)
          alert(error.message || 'Erreur lors de l\'assignation')
        } finally {
          loading.value = false
        }
      }
    }

    // Réinitialiser la sélection
    const resetSelection = () => {
      selectedBassin.value = null
      selectedPoisson.value = null
    }

    // Initialisation
    onMounted(async () => {
      loading.value = true
      try {
        await Promise.all([loadBassins(), loadPoissons()])
      } catch (error) {
        console.error('Erreur initialisation:', error)
      } finally {
        loading.value = false
      }
    })

    return {
      router,
      bassins,
      filteredBassins,
      searchBassin,
      selectedBassin,
      poissons,
      filteredPoissons,
      races,
      filterRace,
      selectedPoisson,
      loading,
      
      filterBassins,
      filterPoissons,
      selectBassin,
      selectPoisson,
      getProgression,
      getStatusClass,
      getStatusText,
      assigner,
      resetSelection
    }
  }
}
</script>

<style scoped>
.assigner-poisson {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
}

.page-header h1 {
  margin: 0;
  color: #2c3e50;
}

.btn-back {
  padding: 10px 15px;
  background: #95a5a6;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.btn-back:hover {
  background: #7f8c8d;
}

/* Sections */
.selection-section {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
  padding: 25px;
  margin-bottom: 30px;
}

.selection-section h2 {
  margin-top: 0;
  margin-bottom: 20px;
  color: #2c3e50;
  border-bottom: 2px solid #3498db;
  padding-bottom: 10px;
}

/* Recherche bassin */
.search-box {
  position: relative;
  margin-bottom: 20px;
}

.search-box input {
  width: 100%;
  padding: 12px 40px 12px 15px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 1em;
}

.search-icon {
  position: absolute;
  right: 15px;
  top: 50%;
  transform: translateY(-50%);
  color: #666;
}

/* Grille bassins */
.bassins-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 15px;
  margin-bottom: 20px;
}

.bassin-card {
  background: #f8f9fa;
  border: 2px solid #eee;
  border-radius: 8px;
  padding: 15px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.bassin-card:hover {
  border-color: #3498db;
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(0,0,0,0.1);
}

.bassin-card.selected {
  border-color: #2ecc71;
  background: #e8f5e9;
}

.bassin-card.full {
  border-color: #e74c3c;
  background: #fdedec;
  cursor: not-allowed;
}

.bassin-card.inactive {
  opacity: 0.6;
  cursor: not-allowed;
}

.bassin-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.bassin-card-header h3 {
  margin: 0;
  font-size: 1.1em;
  color: #2c3e50;
}

.status-active, .status-inactive {
  padding: 3px 8px;
  border-radius: 12px;
  font-size: 0.8em;
  font-weight: bold;
}

.status-active {
  background: #d4edda;
  color: #155724;
}

.status-inactive {
  background: #f8d7da;
  color: #721c24;
}

.bassin-info {
  font-size: 0.9em;
}

.info-row {
  display: flex;
  justify-content: space-between;
  margin-bottom: 6px;
}

.info-label {
  color: #666;
}

.info-value {
  font-weight: 500;
}

.progress-container {
  height: 6px;
  background: #eee;
  border-radius: 3px;
  margin: 10px 0;
  overflow: hidden;
}

.progress-bar {
  height: 100%;
  background: #3498db;
  transition: width 0.3s;
}

.description {
  margin-top: 10px;
  font-style: italic;
  color: #666;
  font-size: 0.85em;
}

.full-warning {
  margin-top: 10px;
  padding: 5px;
  background: #f8d7da;
  color: #721c24;
  border-radius: 4px;
  font-size: 0.85em;
  text-align: center;
}

/* Filtres poissons */
.filters-container {
  margin-bottom: 20px;
}

.filter-group select {
  padding: 8px;
  border: 1px solid #ddd;
  border-radius: 4px;
  background: white;
  min-width: 200px;
}

/* Grille poissons */
.poissons-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 15px;
}

.poisson-card {
  background: #f8f9fa;
  border: 2px solid #eee;
  border-radius: 8px;
  padding: 15px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.poisson-card:hover {
  border-color: #3498db;
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(0,0,0,0.1);
}

.poisson-card.selected {
  border-color: #2ecc71;
  background: #e8f5e9;
}

.poisson-card.has-bassin {
  border-color: #f39c12;
  background: #fff3cd;
}

.poisson-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.poisson-header h3 {
  margin: 0;
  font-size: 1.1em;
  color: #2c3e50;
}

.poisson-status {
  padding: 3px 8px;
  border-radius: 12px;
  font-size: 0.8em;
  font-weight: bold;
}

.poisson-status.dead { background: #f8d7da; color: #721c24; }
.poisson-status.sold { background: #d1ecf1; color: #0c5460; }
.poisson-status.ready { background: #d4edda; color: #155724; }
.poisson-status.fed { background: #fff3cd; color: #856404; }
.poisson-status.hungry { background: #f8d7da; color: #721c24; }

.poisson-info {
  font-size: 0.9em;
}

.current-bassin .bassin-current {
  color: #f39c12;
  font-weight: bold;
}

.no-bassin .bassin-none {
  color: #95a5a6;
  font-style: italic;
}

/* Confirmation */
.confirmation-section {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
  padding: 25px;
}

.confirmation-details {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 30px;
  margin-bottom: 30px;
}

.confirmation-item {
  background: #f8f9fa;
  padding: 20px;
  border-radius: 6px;
}

.confirmation-item h3 {
  margin-top: 0;
  color: #2c3e50;
  border-bottom: 1px solid #ddd;
  padding-bottom: 10px;
}

.confirmation-actions {
  display: flex;
  gap: 15px;
  justify-content: center;
}

.btn-confirm, .btn-reset {
  padding: 12px 30px;
  border: none;
  border-radius: 4px;
  font-size: 1em;
  cursor: pointer;
  transition: all 0.3s ease;
}

.btn-confirm {
  background: #2ecc71;
  color: white;
}

.btn-confirm:hover:not(:disabled) {
  background: #27ae60;
}

.btn-confirm:disabled {
  background: #95a5a6;
  cursor: not-allowed;
}

.btn-reset {
  background: #95a5a6;
  color: white;
}

.btn-reset:hover {
  background: #7f8c8d;
}

/* Aucun résultat */
.no-results {
  text-align: center;
  padding: 40px;
  color: #666;
  font-style: italic;
}

/* Chargement */
.loading-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(255,255,255,0.8);
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.loading-spinner {
  border: 4px solid #f3f3f3;
  border-top: 4px solid #3498db;
  border-radius: 50%;
  width: 40px;
  height: 40px;
  animation: spin 1s linear infinite;
  margin-bottom: 15px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}
</style>
