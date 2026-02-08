<!-- NourrissageNew.vue - CORRIGÉ -->
<template>
  <div class="nourrissage-page">
    <h1>Nourrissage Nouveau</h1>
    
    <!-- Vérification des données -->
    <div class="data-check">
      <div>Poissons affamés: {{ poissonsAffames.length }}</div>
      <div>Plats disponibles: {{ platsDisponibles.length }}</div>
      <div>Aliments disponibles: {{ alimentsDisponibles.length }}</div>
    </div>

    <!-- Mode sélection -->
    <div class="mode-select">
      <button @click="modeNourrissage = 'plat'" 
              :class="{ active: modeNourrissage === 'plat' }">
        Utiliser un plat
      </button>
      <button @click="modeNourrissage = 'aliment'" 
              :class="{ active: modeNourrissage === 'aliment' }">
        Utiliser un aliment
      </button>
    </div>

    <!-- Mode Plat -->
    <div v-if="modeNourrissage === 'plat' && platsDisponibles.length > 0" class="plat-section">
      <h3>Sélectionnez un plat:</h3>
      <div class="plat-list">
        <div v-for="plat in platsDisponibles" 
             :key="plat.idPlat"
             @click="selectPlat(plat)"
             :class="{ selected: selectedPlat?.idPlat === plat.idPlat }"
             class="plat-item">
          <div class="plat-name">{{ plat.nomPlat }}</div>
          <div class="plat-info">
            <span>Quantité: {{ plat.poidsTotalPlat }} kg</span>
            <span>Coût: {{ plat.coutTotalPlat }} MGA</span>
          </div>
          <div class="plat-nutrition">
            <span>Protéines: {{ plat.proteinesParKgPlat }} g/kg</span>
            <span>Glucides: {{ plat.glucidesParKgPlat }} g/kg</span>
          </div>
        </div>
      </div>

      <div v-if="selectedPlat" class="plat-details">
        <h4>Détails du plat sélectionné:</h4>
        <p><strong>Nom:</strong> {{ selectedPlat.nomPlat }}</p>
        <p><strong>Quantité totale:</strong> {{ selectedPlat.poidsTotalPlat }} kg</p>
        <p><strong>Protéines par kg:</strong> {{ selectedPlat.proteinesParKgPlat }} g</p>
        <p><strong>Glucides par kg:</strong> {{ selectedPlat.glucidesParKgPlat }} g</p>
        <p><strong>Coût total:</strong> {{ selectedPlat.coutTotalPlat }} MGA</p>
        
        <div v-if="selectedPlat.compositions && selectedPlat.compositions.length > 0">
          <h5>Composition:</h5>
          <ul>
            <li v-for="comp in selectedPlat.compositions" :key="comp.idCompositionPlat">
              {{ comp.aliment?.nomAliment }}: {{ comp.poidsAlimentComposition }} kg
            </li>
          </ul>
        </div>
      </div>
    </div>

    <!-- Mode Aliment -->
    <div v-else-if="modeNourrissage === 'aliment' && alimentsDisponibles.length > 0" class="aliment-section">
      <h3>Sélectionnez un aliment:</h3>
      <div class="aliment-list">
        <div v-for="aliment in alimentsDisponibles" 
             :key="aliment.idAliment"
             @click="selectAliment(aliment)"
             :class="{ selected: selectedAliment?.idAliment === aliment.idAliment }"
             class="aliment-item">
          <div class="aliment-name">{{ aliment.nomAliment }}</div>
          <div class="aliment-info">
            <span>Stock: {{ aliment.stockAliment }} kg</span>
            <span>Prix: {{ aliment.prixKgAliment }} MGA/kg</span>
          </div>
          <div class="aliment-nutrition">
            <span>Protéines: {{ aliment.proteinesParKgAliment }} g/kg</span>
            <span>Glucides: {{ aliment.glucidesParKgAliment }} g/kg</span>
          </div>
        </div>
      </div>

      <div v-if="selectedAliment" class="aliment-details">
        <h4>Quantité à utiliser:</h4>
        <div class="quantity-control">
          <label>Quantité (kg):</label>
          <input type="number" 
                 v-model="quantiteAliment" 
                 :max="selectedAliment.stockAliment"
                 min="0.1" 
                 step="0.1">
          <span>Max: {{ selectedAliment.stockAliment }} kg</span>
        </div>
      </div>
    </div>

    <!-- Liste des poissons -->
    <div v-if="poissonsAffames.length > 0" class="poissons-section">
      <h3>Poissons à nourrir ({{ poissonsAffames.length }})</h3>
      <div class="poissons-list">
        <div v-for="poisson in poissonsAffames" 
             :key="poisson.idPoisson"
             class="poisson-item">
          <div class="poisson-name">{{ poisson.nomPoisson }}</div>
          <div class="poisson-info">
            <span>Poids: {{ poisson.poidsActuelPoisson }} g</span>
            <span>Max: {{ poisson.poidsMaximalPoisson }} g</span>
            <span>Progression: {{ getProgression(poisson) }}%</span>
          </div>
        </div>
      </div>
    </div>

    <!-- Bouton de nourrissage -->
    <div v-if="canNourrir" class="action-section">
      <button @click="executerNourrissage" :disabled="loading" class="btn-nourrir">
        {{ loading ? 'Nourrissage en cours...' : 'Nourrir les poissons' }}
      </button>
    </div>

    <!-- Message d'erreur -->
    <div v-if="errorMessage" class="error-message">
      {{ errorMessage }}
    </div>
  </div>
</template>

<script>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import platService from '../services/PlatService' // Assurez-vous du bon chemin
import poissonService from '../services/poissonService' // Assurez-vous du bon chemin
import alimentService from '../services/alimentService' // Assurez-vous du bon chemin
import nourrissageService from '../services/nourrissageService' // Assurez-vous du bon chemin

export default {
  name: 'NourrissageNew',
  setup() {
    const router = useRouter()
    
    // Données
    const poissonsAffames = ref([])
    const platsDisponibles = ref([])
    const alimentsDisponibles = ref([])
    const loading = ref(false)
    const errorMessage = ref('')
    
    // Sélections
    const modeNourrissage = ref('plat')
    const selectedPlat = ref(null)
    const selectedAliment = ref(null)
    const quantiteAliment = ref(1.0)
    
    // Charger les données
    const chargerDonnees = async () => {
      try {
        errorMessage.value = ''
        
        // 1. Charger les poissons affamés
        console.log('Chargement des poissons affamés...')
        const poissonsData = await poissonService.getPoissonsAffames()
        poissonsAffames.value = poissonsData
        console.log('Poissons affamés chargés:', poissonsData.length)
        
        // 2. Charger les plats disponibles
        console.log('Chargement des plats disponibles...')
        try {
          const platsData = await platService.getPlatsDisponibles()
          platsDisponibles.value = platsData
          console.log('Plats disponibles chargés:', platsData.length)
        } catch (platError) {
          console.warn('Erreur chargement plats:', platError)
          platsDisponibles.value = []
        }
        
        // 3. Charger les aliments disponibles
        console.log('Chargement des aliments disponibles...')
        try {
          const alimentsData = await alimentService.getAlimentsDisponibles()
          alimentsDisponibles.value = alimentsData
          console.log('Aliments disponibles chargés:', alimentsData.length)
        } catch (alimentError) {
          console.warn('Erreur chargement aliments:', alimentError)
          alimentsDisponibles.value = []
        }
        
      } catch (error) {
        console.error('Erreur chargement données:', error)
        errorMessage.value = 'Impossible de charger les données: ' + error.message
      }
    }
    
    // Computed properties
    const canNourrir = computed(() => {
      if (poissonsAffames.value.length === 0) return false
      
      if (modeNourrissage.value === 'plat') {
        return selectedPlat.value !== null
      } else if (modeNourrissage.value === 'aliment') {
        return selectedAliment.value !== null && quantiteAliment.value > 0
      }
      
      return false
    })
    
    // Méthodes
    const selectPlat = (plat) => {
      selectedPlat.value = plat
      selectedAliment.value = null
    }
    
    const selectAliment = (aliment) => {
      selectedAliment.value = aliment
      selectedPlat.value = null
      quantiteAliment.value = Math.min(1.0, aliment.stockAliment || 1.0)
    }
    
    const getProgression = (poisson) => {
      if (!poisson.poidsActuelPoisson || !poisson.poidsMaximalPoisson) return 0
      return Math.round((poisson.poidsActuelPoisson / poisson.poidsMaximalPoisson) * 100)
    }
    
    // Exécuter le nourrissage
    const executerNourrissage = async () => {
      if (!canNourrir.value) return
      
      loading.value = true
      errorMessage.value = ''
      
      try {
        let result
        
        if (modeNourrissage.value === 'plat' && selectedPlat.value) {
          // Nourrir avec un plat
          console.log('Nourrissage avec plat:', selectedPlat.value.idPlat)
          result = await nourrissageService.nourrirAvecPlat(selectedPlat.value.idPlat)
          
        } else if (modeNourrissage.value === 'aliment' && selectedAliment.value) {
          // Nourrir avec un aliment
          console.log('Nourrissage avec aliment:', selectedAliment.value.idAliment)
          result = await nourrissageService.nourrirPoissons(
            quantiteAliment.value,
            selectedAliment.value.proteinesParKgAliment,
            selectedAliment.value.glucidesParKgAliment
          )
        }
        
        console.log('Résultat nourrissage:', result)
        alert(`Nourrissage réussi! ${result.poissonsNourris || 0} poissons nourris.`)
        
        // Recharger les données
        await chargerDonnees()
        
      } catch (error) {
        console.error('Erreur nourrissage:', error)
        errorMessage.value = 'Erreur lors du nourrissage: ' + error.message
      } finally {
        loading.value = false
      }
    }
    
    // Initialisation
    onMounted(() => {
      chargerDonnees()
    })
    
    return {
      // Données
      poissonsAffames,
      platsDisponibles,
      alimentsDisponibles,
      loading,
      errorMessage,
      
      // Sélections
      modeNourrissage,
      selectedPlat,
      selectedAliment,
      quantiteAliment,
      
      // Computed
      canNourrir,
      
      // Méthodes
      selectPlat,
      selectAliment,
      getProgression,
      executerNourrissage
    }
  }
}
</script>

<style scoped>
.nourrissage-page {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.data-check {
  display: flex;
  gap: 20px;
  margin-bottom: 20px;
  padding: 10px;
  background: #f0f0f0;
  border-radius: 5px;
}

.mode-select {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
}

.mode-select button {
  padding: 10px 20px;
  border: 2px solid #ccc;
  background: white;
  border-radius: 5px;
  cursor: pointer;
}

.mode-select button.active {
  background: #007bff;
  color: white;
  border-color: #0056b3;
}

.plat-section, .aliment-section, .poissons-section {
  margin-bottom: 30px;
  padding: 20px;
  border: 1px solid #ddd;
  border-radius: 8px;
}

.plat-list, .aliment-list, .poissons-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 15px;
  margin-top: 15px;
}

.plat-item, .aliment-item {
  padding: 15px;
  border: 1px solid #ddd;
  border-radius: 5px;
  cursor: pointer;
  transition: all 0.3s;
}

.plat-item:hover, .aliment-item:hover {
  background: #f5f5f5;
  border-color: #007bff;
}

.plat-item.selected, .aliment-item.selected {
  background: #e3f2fd;
  border-color: #007bff;
}

.plat-name, .aliment-name, .poisson-name {
  font-weight: bold;
  margin-bottom: 5px;
  color: #333;
}

.plat-info, .aliment-info, .poisson-info {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  font-size: 0.9em;
  color: #666;
}

.plat-nutrition, .aliment-nutrition {
  display: flex;
  gap: 10px;
  margin-top: 5px;
  font-size: 0.9em;
  color: #28a745;
}

.plat-details, .aliment-details {
  margin-top: 20px;
  padding: 15px;
  background: #f8f9fa;
  border-radius: 5px;
}

.quantity-control {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-top: 10px;
}

.quantity-control input {
  padding: 5px;
  width: 100px;
  border: 1px solid #ddd;
  border-radius: 3px;
}

.poisson-item {
  padding: 10px;
  border: 1px solid #eee;
  border-radius: 3px;
  background: #fafafa;
}

.action-section {
  margin-top: 30px;
  text-align: center;
}

.btn-nourrir {
  padding: 12px 30px;
  background: #28a745;
  color: white;
  border: none;
  border-radius: 5px;
  font-size: 1.1em;
  cursor: pointer;
}

.btn-nourrir:hover:not(:disabled) {
  background: #218838;
}

.btn-nourrir:disabled {
  background: #ccc;
  cursor: not-allowed;
}

.error-message {
  margin-top: 20px;
  padding: 10px;
  background: #f8d7da;
  color: #721c24;
  border: 1px solid #f5c6cb;
  border-radius: 5px;
}

ul {
  list-style-type: none;
  padding-left: 0;
}

li {
  padding: 5px 0;
  border-bottom: 1px dashed #ddd;
}
</style>

