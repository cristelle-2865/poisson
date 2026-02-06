<template>
  <div class="nouveau-poisson">
    <div class="page-header">
      <div class="header-left">
        <router-link to="/poissons" class="btn-back">
          ← Retour à la liste
        </router-link>
        <h1>Ajouter un nouveau poisson</h1>
      </div>
      <div class="header-right">
        <button @click="resetForm" class="btn-reset" :disabled="isSubmitting">
          🔄 Réinitialiser
        </button>
      </div>
    </div>

    <!-- Messages d'alerte -->
    <div v-if="errorMessage" class="alert error">
      <span class="alert-icon">⚠️</span>
      <span>{{ errorMessage }}</span>
      <button @click="errorMessage = ''" class="alert-close">×</button>
    </div>

    <div v-if="successMessage" class="alert success">
      <span class="alert-icon">✅</span>
      <span>{{ successMessage }}</span>
      <button @click="successMessage = ''" class="alert-close">×</button>
    </div>

    <div class="form-container">
      <form @submit.prevent="submitForm" class="poisson-form">
        
        <!-- Section informations de base -->
        <div class="form-section">
          <h2>📝 Informations de base</h2>
          <div class="form-grid">
            <div class="form-group">
              <label for="nom">Nom du poisson *</label>
              <input
                type="text"
                id="nom"
                v-model="form.nomPoisson"
                :class="{ 'error': errors.nomPoisson }"
                placeholder="Ex: Tilapia-001"
                required
              />
              <span v-if="errors.nomPoisson" class="error-message">{{ errors.nomPoisson }}</span>
            </div>

            <div class="form-group">
              <label for="race">Race *</label>
              <select
                id="race"
                v-model="form.idRacePoisson"
                :class="{ 'error': errors.idRacePoisson }"
                required
              >
                <option value="">Sélectionner une race</option>
                <option v-for="race in races" :key="race.idRacePoisson" :value="race.idRacePoisson">
                  {{ race.nomRacePoisson }}
                </option>
              </select>
              <span v-if="errors.idRacePoisson" class="error-message">{{ errors.idRacePoisson }}</span>
              <button type="button" @click="showAddRaceModal = true" class="btn-link">
                + Ajouter une nouvelle race
              </button>
            </div>

            <!-- NOUVEAU : Sélection du bassin -->
            <!-- Section bassin corrigée -->
            <div class="form-group">
              <label for="bassin">Bassin (optionnel)</label>
              <select
                id="bassin"
                v-model="form.idPiscineActuel"
                :class="{ 'error': errors.idPiscineActuel }"
              >
                <option value="">Sélectionner un bassin (optionnel)</option>
                <option v-for="bassin in bassins" :key="bassin.idPiscine" :value="bassin.idPiscine">
                  {{ bassin.nomPiscine }}
                  <template v-if="bassin.capaciteMaxPiscine">
                    (Capacité: {{ bassin.capaciteMaxPiscine }})
                  </template>
                </option>
              </select>
              <span v-if="errors.idPiscineActuel" class="error-message">{{ errors.idPiscineActuel }}</span>
              <div class="input-info">
                Vous pouvez assigner un bassin maintenant ou plus tard
              </div>
            </div>

            <div class="form-group">
              <label for="dateArrivee">Date d'arrivée *</label>
              <input
                type="date"
                id="dateArrivee"
                v-model="form.dateArriveePoisson"
                :class="{ 'error': errors.dateArriveePoisson }"
                required
              />
              <span v-if="errors.dateArriveePoisson" class="error-message">{{ errors.dateArriveePoisson }}</span>
            </div>
          </div>
        </div>

        <!-- Section poids -->
        <div class="form-section">
          <h2>⚖️ Informations sur le poids</h2>
          <div class="form-grid">
            <div class="form-group">
              <label for="poidsInitial">Poids initial (g) *</label>
              <input
                type="number"
                id="poidsInitial"
                v-model="form.poidsInitialPoisson"
                :class="{ 'error': errors.poidsInitialPoisson }"
                step="0.01"
                min="0"
                required
              />
              <span v-if="errors.poidsInitialPoisson" class="error-message">{{ errors.poidsInitialPoisson }}</span>
              <div class="input-info">
                Poids du poisson à son arrivée
              </div>
            </div>

            <div class="form-group">
              <label for="poidsMaximal">Poids maximal (g) *</label>
              <input
                type="number"
                id="poidsMaximal"
                v-model="form.poidsMaximalPoisson"
                :class="{ 'error': errors.poidsMaximalPoisson }"
                step="0.01"
                min="0"
                required
              />
              <span v-if="errors.poidsMaximalPoisson" class="error-message">{{ errors.poidsMaximalPoisson }}</span>
              <div class="input-info">
                Poids maximum que le poisson peut atteindre
              </div>
            </div>

            <div class="form-group">
              <label for="capaciteAugmentation">Capacité augmentation/jour (g) *</label>
              <input
                type="number"
                id="capaciteAugmentation"
                v-model="form.capaciteAugmentationPoisson"
                :class="{ 'error': errors.capaciteAugmentationPoisson }"
                step="0.01"
                min="0"
                required
              />
              <span v-if="errors.capaciteAugmentationPoisson" class="error-message">{{ errors.capaciteAugmentationPoisson }}</span>
              <div class="input-info">
                Gain de poids maximal par jour
              </div>
            </div>

            <div class="form-group">
              <div class="progress-indicator">
                <label>Progression initiale</label>
                <div class="progress-bar">
                  <div 
                    class="progress-fill" 
                    :style="{ width: progressionInitiale + '%' }"
                    :class="getProgressionClass(progressionInitiale)"
                  ></div>
                </div>
                <span class="progress-text">{{ progressionInitiale }}%</span>
              </div>
              <div class="input-info">
                {{ poidsRestant }} g restant pour atteindre le poids maximal
              </div>
            </div>
          </div>
        </div>

        <!-- Section financière -->
        <div class="form-section">
          <h2>💰 Informations financières</h2>
          <div class="form-grid">
            <div class="form-group">
              <label for="prixAchat">Prix d'achat (MGA) *</label>
              <input
                type="number"
                id="prixAchat"
                v-model="form.prixAchatPoisson"
                :class="{ 'error': errors.prixAchatPoisson }"
                step="0.01"
                min="0"
                required
              />
              <span v-if="errors.prixAchatPoisson" class="error-message">{{ errors.prixAchatPoisson }}</span>
              <div class="input-info">
                Coût d'achat du poisson
              </div>
            </div>

            <div class="form-group">
              <label for="prixVente">Prix de vente estimé (MGA) *</label>
              <input
                type="number"
                id="prixVente"
                v-model="form.prixVentePoisson"
                :class="{ 'error': errors.prixVentePoisson }"
                step="0.01"
                min="0"
                required
              />
              <span v-if="errors.prixVentePoisson" class="error-message">{{ errors.prixVentePoisson }}</span>
              <div class="input-info">
                Prix de vente lorsque le poisson atteint son poids maximal
              </div>
            </div>

            <div class="form-group">
              <div class="profit-preview">
                <label>Bénéfice potentiel</label>
                <div class="profit-value" :class="getProfitClass">
                  {{ formatCurrency(beneficePotentiel) }}
                </div>
                <div class="profit-details">
                  <span>Marge: {{ margePourcentage }}%</span>
                  <span>Retour sur investissement: {{ roiPourcentage }}%</span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Section statut -->
        <div class="form-section">
          <h2>📊 Statut initial</h2>
          <div class="form-grid">
            <div class="form-group">
              <label class="checkbox-label">
                <input
                  type="checkbox"
                  v-model="form.estRassasiePoisson"
                />
                <span class="checkmark"></span>
                <span>Poisson rassasié à l'arrivée</span>
              </label>
              <div class="input-info">
                Si décoché, le poisson sera marqué comme affamé
              </div>
            </div>

            <div class="form-group">
              <label class="checkbox-label">
                <input
                  type="checkbox"
                  v-model="form.estEnViePoisson"
                />
                <span class="checkmark"></span>
                <span>Poisson en vie</span>
              </label>
              <div class="input-info">
                Décochez si le poisson arrive déjà mort
              </div>
            </div>
          </div>
        </div>

        <!-- Résumé -->
        <div class="form-section summary">
          <h2>📋 Résumé</h2>
          <div class="summary-grid">
            <div class="summary-item">
              <span class="summary-label">Nom</span>
              <span class="summary-value">{{ form.nomPoisson || 'Non défini' }}</span>
            </div>
            <div class="summary-item">
              <span class="summary-label">Race</span>
              <span class="summary-value">{{ getRaceName() || 'Non sélectionnée' }}</span>
            </div>
            <div class="summary-item">
              <span class="summary-label">Bassin</span>
              <span class="summary-value">{{ getBassinName() || 'Aucun' }}</span>
            </div>
            <div class="summary-item">
              <span class="summary-label">Poids</span>
              <span class="summary-value">{{ form.poidsInitialPoisson || 0 }}g → {{ form.poidsMaximalPoisson || 0 }}g</span>
            </div>
            <div class="summary-item">
              <span class="summary-label">Progression</span>
              <span class="summary-value">{{ progressionInitiale }}%</span>
            </div>
            <div class="summary-item">
              <span class="summary-label">Investissement</span>
              <span class="summary-value">{{ formatCurrency(form.prixAchatPoisson) }}</span>
            </div>
            <div class="summary-item">
              <span class="summary-label">Bénéfice potentiel</span>
              <span class="summary-value" :class="getProfitClass">
                {{ formatCurrency(beneficePotentiel) }}
              </span>
            </div>
          </div>
        </div>

        <!-- Actions du formulaire -->
        <div class="form-actions">
          <router-link to="/poissons" class="btn-cancel">
            Annuler
          </router-link>
          <button 
            type="submit" 
            class="btn-submit"
            :disabled="isSubmitting || !isFormValid"
          >
            <span v-if="isSubmitting" class="spinner"></span>
            {{ isSubmitting ? 'Création en cours...' : 'Créer le poisson' }}
          </button>
        </div>
      </form>

      <!-- Aperçu -->
      <div class="preview-container">
        <div class="preview-card">
          <h3>Aperçu du poisson</h3>
          <div class="preview-content">
            <div class="preview-header">
              <div class="preview-name">{{ form.nomPoisson || 'Nouveau poisson' }}</div>
              <div class="preview-race">{{ getRaceName() || 'Race non définie' }}</div>
              <div class="preview-bassin" v-if="getBassinName()">
                🏊 {{ getBassinName() }}
              </div>
            </div>
            
            <div class="preview-stats">
              <div class="stat-item">
                <span class="stat-label">Poids actuel</span>
                <span class="stat-value">{{ form.poidsInitialPoisson || 0 }}g</span>
              </div>
              <div class="stat-item">
                <span class="stat-label">Poids max</span>
                <span class="stat-value">{{ form.poidsMaximalPoisson || 0 }}g</span>
              </div>
              <div class="stat-item">
                <span class="stat-label">Progression</span>
                <div class="progress-container">
                  <div class="progress-bar-small">
                    <div 
                      class="progress-fill-small"
                      :style="{ width: progressionInitiale + '%' }"
                    ></div>
                  </div>
                  <span class="progress-percent">{{ progressionInitiale }}%</span>
                </div>
              </div>
            </div>

            <div class="preview-financial">
              <div class="financial-item">
                <span class="financial-label">Coût d'achat</span>
                <span class="financial-value cost">{{ formatCurrency(form.prixAchatPoisson) }}</span>
              </div>
              <div class="financial-item">
                <span class="financial-label">Vente estimée</span>
                <span class="financial-value sale">{{ formatCurrency(form.prixVentePoisson) }}</span>
              </div>
              <div class="financial-item">
                <span class="financial-label">Bénéfice</span>
                <span class="financial-value profit" :class="getProfitClass">
                  {{ formatCurrency(beneficePotentiel) }}
                </span>
              </div>
            </div>

            <div class="preview-status">
              <div class="status-badges">
                <span class="status-badge" :class="getStatusClass('vie')">
                  {{ form.estEnViePoisson ? 'En vie' : 'Mort' }}
                </span>
                <span class="status-badge" :class="getStatusClass('nourri')">
                  {{ form.estRassasiePoisson ? 'Rassasié' : 'Affamé' }}
                </span>
                <span class="status-badge" :class="getStatusClass('vente')">
                  {{ estPretAVendre ? 'Prêt à vendre' : 'En croissance' }}
                </span>
              </div>
            </div>

            <div class="preview-dates">
              <div class="date-item">
                <span class="date-label">Date d'arrivée</span>
                <span class="date-value">{{ formatDatePreview(form.dateArriveePoisson) }}</span>
              </div>
              <div class="date-item" v-if="getBassinName()">
                <span class="date-label">Bassin assigné</span>
                <span class="date-value">{{ getBassinName() }}</span>
              </div>
            </div>
          </div>
        </div>

        <!-- Conseils -->
        <div class="tips-card">
          <h3>💡 Conseils</h3>
          <ul class="tips-list">
            <li>Nommez vos poissons de façon identifiable (ex: Tilapia-001, Carpe-A)</li>
            <li>Le poids initial doit être inférieur au poids maximal</li>
            <li>Le prix de vente doit être supérieur au prix d'achat pour faire un bénéfice</li>
            <li>La capacité d'augmentation dépend de la race (20g/jour pour Tilapia, 15g/jour pour Carpe)</li>
            <li>Un poisson est prêt à vendre quand il atteint 95% de son poids maximal</li>
            <li>Vous pouvez assigner un bassin maintenant ou plus tard depuis la liste des poissons</li>
          </ul>
        </div>
      </div>
    </div>

    <!-- Modale ajout race -->
    <div v-if="showAddRaceModal" class="modal-overlay">
      <div class="modal">
        <div class="modal-header">
          <h3>Ajouter une nouvelle race</h3>
          <button @click="showAddRaceModal = false" class="modal-close">×</button>
        </div>
        <div class="modal-body">
          <div class="form-group">
            <label for="newRaceName">Nom de la race *</label>
            <input
              type="text"
              id="newRaceName"
              v-model="newRaceData.nomRacePoisson"
              placeholder="Ex: Tilapia du Nil"
              required
              :class="{ 'error': raceErrors.nomRacePoisson }"
            />
            <span v-if="raceErrors.nomRacePoisson" class="error-message">{{ raceErrors.nomRacePoisson }}</span>
          </div>
          <div class="form-group">
            <label for="newRaceDescription">Description</label>
            <textarea
              id="newRaceDescription"
              v-model="newRaceData.descriptionRacePoisson"
              placeholder="Description de la race..."
              rows="3"
            ></textarea>
          </div>
        </div>
        <div class="modal-footer">
          <button @click="showAddRaceModal = false" class="btn-cancel">
            Annuler
          </button>
          <button @click="addNewRace" class="btn-submit" :disabled="isAddingRace">
            <span v-if="isAddingRace" class="spinner"></span>
            {{ isAddingRace ? 'Ajout en cours...' : 'Ajouter la race' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import poissonService from '../services/poissonService'

export default {
  name: 'NouveauPoisson',
  setup() {
    const router = useRouter()
    
    // État du formulaire
    const form = ref({
      nomPoisson: '',
      idRacePoisson: '',
      idPiscineActuel: '', // NOUVEAU: ID du bassin
      prixAchatPoisson: 0,
      prixVentePoisson: 0,
      poidsMaximalPoisson: 0,
      capaciteAugmentationPoisson: 20, // Valeur par défaut
      poidsInitialPoisson: 0,
      dateArriveePoisson: new Date().toISOString().split('T')[0],
      estRassasiePoisson: true,
      estEnViePoisson: true
    })

    // État de l'application
    const races = ref([])
    const bassins = ref([]) // NOUVEAU: Liste des bassins
    const errors = ref({})
    const isSubmitting = ref(false)
    const errorMessage = ref('')
    const successMessage = ref('')
    const showAddRaceModal = ref(false)
    const newRaceData = ref({
      nomRacePoisson: '',
      descriptionRacePoisson: ''
    })
    const raceErrors = ref({})
    const isAddingRace = ref(false)

    // Charger les données initiales
    const loadInitialData = async () => {
      try {
        // Charger les races
        const racesData = await poissonService.getRacesPoisson()
        races.value = racesData
        
        // Charger les bassins
        await loadBassins()
        
        // Si aucune race, proposer d'en créer une
        if (races.value.length === 0) {
          showAddRaceModal.value = true
        }
      } catch (error) {
        console.error('Erreur chargement données:', error)
        errorMessage.value = 'Impossible de charger les données initiales'
      }
    }

    // Charger les bassins
    const loadBassins = async () => {
      try {
        const bassinsData = await poissonService.getBassins()
        bassins.value = bassinsData
      } catch (error) {
        console.error('Erreur chargement bassins:', error)
        // Ne pas bloquer le formulaire si erreur de chargement des bassins
        bassins.value = []
      }
    }

    // Computed properties
    const progressionInitiale = computed(() => {
      if (!form.value.poidsInitialPoisson || !form.value.poidsMaximalPoisson) return 0
      const progression = (form.value.poidsInitialPoisson / form.value.poidsMaximalPoisson) * 100
      return Math.min(Math.round(progression * 100) / 100, 100)
    })

    const poidsRestant = computed(() => {
      if (!form.value.poidsMaximalPoisson) return 0
      const restant = form.value.poidsMaximalPoisson - (form.value.poidsInitialPoisson || 0)
      return Math.max(0, Math.round(restant * 100) / 100)
    })

    const beneficePotentiel = computed(() => {
      if (!form.value.prixVentePoisson || !form.value.prixAchatPoisson) return 0
      return form.value.prixVentePoisson - form.value.prixAchatPoisson
    })

    const margePourcentage = computed(() => {
      if (!form.value.prixAchatPoisson || form.value.prixAchatPoisson <= 0) return 0
      const marge = (beneficePotentiel.value / form.value.prixAchatPoisson) * 100
      return Math.round(marge * 100) / 100
    })

    const roiPourcentage = computed(() => {
      if (!form.value.prixAchatPoisson || form.value.prixAchatPoisson <= 0) return 0
      const roi = (beneficePotentiel.value / form.value.prixAchatPoisson) * 100
      return Math.round(roi)
    })

    const estPretAVendre = computed(() => {
      return progressionInitiale.value >= 95
    })

    const getProfitClass = computed(() => {
      if (beneficePotentiel.value > 0) return 'profit-positive'
      if (beneficePotentiel.value < 0) return 'profit-negative'
      return 'profit-neutral'
    })

    const isFormValid = computed(() => {
      return form.value.nomPoisson &&
             form.value.idRacePoisson &&
             form.value.poidsInitialPoisson > 0 &&
             form.value.poidsMaximalPoisson > 0 &&
             form.value.prixAchatPoisson > 0 &&
             form.value.prixVentePoisson > 0 &&
             form.value.dateArriveePoisson &&
             form.value.poidsInitialPoisson <= form.value.poidsMaximalPoisson &&
             form.value.prixVentePoisson > form.value.prixAchatPoisson
    })

    // Méthodes
    const getRaceName = () => {
      const race = races.value.find(r => r.idRacePoisson == form.value.idRacePoisson)
      return race ? race.nomRacePoisson : ''
    }

    const getBassinName = () => {
      const bassin = bassins.value.find(b => b.idPiscine == form.value.idPiscineActuel)
      return bassin ? bassin.nomPiscine : ''
    }

    const getProgressionClass = (progression) => {
      if (progression >= 95) return 'progress-high'
      if (progression >= 70) return 'progress-medium'
      if (progression >= 40) return 'progress-low'
      return 'progress-very-low'
    }

    const getStatusClass = (type) => {
      switch (type) {
        case 'vie':
          return form.value.estEnViePoisson ? 'status-alive' : 'status-dead'
        case 'nourri':
          return form.value.estRassasiePoisson ? 'status-fed' : 'status-hungry'
        case 'vente':
          return estPretAVendre.value ? 'status-ready' : 'status-growing'
        default:
          return ''
      }
    }

    const formatCurrency = (amount) => {
      if (!amount) return '0 MGA'
      return new Intl.NumberFormat('fr-MG', {
        style: 'currency',
        currency: 'MGA',
        minimumFractionDigits: 2
      }).format(amount)
    }

    const formatDatePreview = (dateString) => {
      if (!dateString) return 'Non définie'
      return new Date(dateString).toLocaleDateString('fr-FR', {
        weekday: 'long',
        year: 'numeric',
        month: 'long',
        day: 'numeric'
      })
    }

    const validateForm = () => {
      errors.value = {}

      // Nom
      if (!form.value.nomPoisson.trim()) {
        errors.value.nomPoisson = 'Le nom est requis'
      }

      // Race
      if (!form.value.idRacePoisson) {
        errors.value.idRacePoisson = 'La race est requise'
      }

      // Poids initial
      if (!form.value.poidsInitialPoisson || form.value.poidsInitialPoisson <= 0) {
        errors.value.poidsInitialPoisson = 'Le poids initial doit être positif'
      }

      // Poids maximal
      if (!form.value.poidsMaximalPoisson || form.value.poidsMaximalPoisson <= 0) {
        errors.value.poidsMaximalPoisson = 'Le poids maximal doit être positif'
      }

      // Vérifier que poids initial < poids maximal
      if (form.value.poidsInitialPoisson > form.value.poidsMaximalPoisson) {
        errors.value.poidsInitialPoisson = 'Le poids initial doit être inférieur au poids maximal'
      }

      // Prix achat
      if (!form.value.prixAchatPoisson || form.value.prixAchatPoisson <= 0) {
        errors.value.prixAchatPoisson = 'Le prix d\'achat doit être positif'
      }

      // Prix vente
      if (!form.value.prixVentePoisson || form.value.prixVentePoisson <= 0) {
        errors.value.prixVentePoisson = 'Le prix de vente doit être positif'
      }

      // Vérifier que prix vente > prix achat
      if (form.value.prixVentePoisson <= form.value.prixAchatPoisson) {
        errors.value.prixVentePoisson = 'Le prix de vente doit être supérieur au prix d\'achat'
      }

      // Date arrivée
      if (!form.value.dateArriveePoisson) {
        errors.value.dateArriveePoisson = 'La date d\'arrivée est requise'
      }

      // Capacité augmentation
      if (!form.value.capaciteAugmentationPoisson || form.value.capaciteAugmentationPoisson <= 0) {
        errors.value.capaciteAugmentationPoisson = 'La capacité d\'augmentation doit être positive'
      }

      return Object.keys(errors.value).length === 0
    }

    const validateRaceForm = () => {
      raceErrors.value = {}
      
      if (!newRaceData.value.nomRacePoisson.trim()) {
        raceErrors.value.nomRacePoisson = 'Le nom de la race est requis'
      }
      
      return Object.keys(raceErrors.value).length === 0
    }

    const submitForm = async () => {
      if (!validateForm()) {
        errorMessage.value = 'Veuillez corriger les erreurs dans le formulaire'
        return
      }

      isSubmitting.value = true
      errorMessage.value = ''

      try {
        // Préparer les données pour l'API
        const poissonData = {
          nomPoisson: form.value.nomPoisson,
          racePoisson: {
            idRacePoisson: form.value.idRacePoisson
          },
          prixAchatPoisson: parseFloat(form.value.prixAchatPoisson),
          prixVentePoisson: parseFloat(form.value.prixVentePoisson),
          poidsMaximalPoisson: parseFloat(form.value.poidsMaximalPoisson),
          capaciteAugmentationPoisson: parseFloat(form.value.capaciteAugmentationPoisson),
          poidsInitialPoisson: parseFloat(form.value.poidsInitialPoisson),
          poidsActuelPoisson: parseFloat(form.value.poidsInitialPoisson), // Même que poids initial
          dateArriveePoisson: form.value.dateArriveePoisson,
          estRassasiePoisson: form.value.estRassasiePoisson,
          estEnViePoisson: form.value.estEnViePoisson,
          estVenduPoisson: false
        }

        // Ajouter le bassin si sélectionné
        if (form.value.idPiscineActuel) {
          poissonData.piscineActuelle = {
            idPiscine: form.value.idPiscineActuel
          }
        }

        // Appeler l'API
        await poissonService.createPoisson(poissonData)
        
        successMessage.value = 'Poisson créé avec succès !'
        
        // Rediriger après un délai
        setTimeout(() => {
          router.push('/poissons')
        }, 2000)

      } catch (error) {
        console.error('Erreur création poisson:', error)
        errorMessage.value = error.response?.data?.message || 'Erreur lors de la création du poisson'
      } finally {
        isSubmitting.value = false
      }
    }

    const resetForm = () => {
      if (!confirm('Réinitialiser le formulaire ? Toutes les données seront perdues.')) return
      
      form.value = {
        nomPoisson: '',
        idRacePoisson: '',
        idPiscineActuel: '',
        prixAchatPoisson: 0,
        prixVentePoisson: 0,
        poidsMaximalPoisson: 0,
        capaciteAugmentationPoisson: 20,
        poidsInitialPoisson: 0,
        dateArriveePoisson: new Date().toISOString().split('T')[0],
        estRassasiePoisson: true,
        estEnViePoisson: true
      }
      
      errors.value = {}
      errorMessage.value = ''
      successMessage.value = ''
    }

    const addNewRace = async () => {
      if (!validateRaceForm()) {
        return
      }

      isAddingRace.value = true
      
      try {
        const raceData = {
          nomRacePoisson: newRaceData.value.nomRacePoisson,
          descriptionRacePoisson: newRaceData.value.descriptionRacePoisson,
          estActifRacePoisson: true
        }

        // Utiliser le service pour créer la race
        const response = await fetch('http://localhost:8080/races-poisson', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json'
          },
          body: JSON.stringify(raceData)
        })

        if (!response.ok) {
          throw new Error('Erreur lors de la création de la race')
        }

        const newRace = await response.json()
        
        // Ajouter la nouvelle race à la liste
        races.value.push(newRace)
        form.value.idRacePoisson = newRace.idRacePoisson
        
        // Réinitialiser le formulaire de race
        newRaceData.value = {
          nomRacePoisson: '',
          descriptionRacePoisson: ''
        }
        raceErrors.value = {}
        showAddRaceModal.value = false
        
        successMessage.value = 'Race ajoutée avec succès'
        
      } catch (error) {
        console.error('Erreur ajout race:', error)
        errorMessage.value = 'Erreur lors de l\'ajout de la race: ' + error.message
      } finally {
        isAddingRace.value = false
      }
    }

    // Initialisation
    onMounted(() => {
      loadInitialData()
    })

    return {
      // État
      form,
      races,
      bassins,
      errors,
      raceErrors,
      isSubmitting,
      isAddingRace,
      errorMessage,
      successMessage,
      showAddRaceModal,
      newRaceData,
      
      // Computed
      progressionInitiale,
      poidsRestant,
      beneficePotentiel,
      margePourcentage,
      roiPourcentage,
      estPretAVendre,
      getProfitClass,
      isFormValid,
      
      // Méthodes
      getRaceName,
      getBassinName,
      getProgressionClass,
      getStatusClass,
      formatCurrency,
      formatDatePreview,
      submitForm,
      resetForm,
      addNewRace
    }
  }
}
</script>

<style scoped>
/* Styles spécifiques pour le nouveau select bassin */
.form-group select option:disabled {
  color: #999;
  font-style: italic;
}

.preview-bassin {
  margin-top: 5px;
  padding: 5px 10px;
  background-color: #e3f2fd;
  border-radius: 4px;
  color: #1565c0;
  font-size: 14px;
  display: inline-flex;
  align-items: center;
  gap: 5px;
}

/* Style pour les options de bassin avec capacité */
select option[value=""] {
  font-style: italic;
  color: #666;
}

/* Surligner les bassins presque pleins */
.bassin-warning {
  color: #ff6b6b;
  font-weight: bold;
}

/* Améliorer l'affichage des options */
select option {
  padding: 8px;
}

/* Animation pour la modal race */
.modal {
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
</style>

