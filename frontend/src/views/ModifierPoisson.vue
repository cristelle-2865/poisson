<template>
  <div class="modifier-poisson">
    <!-- En-tête -->
    <div class="page-header">
      <div class="header-left">
        <router-link to="/poissons" class="btn-back">
          ← Retour à la liste
        </router-link>
        <h1>✏️ Modifier le poisson</h1>
        <p class="subtitle">{{ poisson?.nomPoisson || 'Chargement...' }}</p>
      </div>
      <div class="header-right">
        <button @click="refreshPoisson" class="btn-refresh" :disabled="loading">
          🔄
        </button>
        <button @click="savePoisson" class="btn-save" :disabled="!isFormValid || loading">
          💾 {{ loading ? 'Sauvegarde...' : 'Sauvegarder' }}
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

    <!-- Contenu principal -->
    <div class="content-container" v-if="poisson">
      <div class="form-preview-container">
        <!-- Formulaire de modification -->
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

            <div class="form-group">
              <label for="dateVente">Date de vente</label>
              <input
                type="date"
                id="dateVente"
                v-model="form.dateVentePoisson"
                :disabled="!form.estVenduPoisson"
              />
              <div class="checkbox-group">
                <label class="checkbox-label">
                  <input
                    type="checkbox"
                    v-model="form.estVenduPoisson"
                    @change="handleVenduChange"
                  />
                  <span class="checkmark"></span>
                  <span>Poisson vendu</span>
                </label>
              </div>
            </div>
          </div>
        </div>

        <!-- Informations sur le poids -->
        <div class="form-section">
          <h2>⚖️ Informations sur le poids</h2>
          <div class="form-grid">
            <div class="form-group">
              <label for="poidsActuel">Poids actuel (g) *</label>
              <input
                type="number"
                id="poidsActuel"
                v-model="form.poidsActuelPoisson"
                :class="{ 'error': errors.poidsActuelPoisson }"
                step="0.01"
                min="0"
                required
                @input="calculateProgression"
              />
              <span v-if="errors.poidsActuelPoisson" class="error-message">{{ errors.poidsActuelPoisson }}</span>
              <div class="input-info">
                Dernière mise à jour: {{ formatDate(poisson.dateModificationPoisson) }}
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
                @input="calculateProgression"
              />
              <span v-if="errors.poidsMaximalPoisson" class="error-message">{{ errors.poidsMaximalPoisson }}</span>
            </div>

            <div class="form-group">
              <label for="capaciteAugmentation">Capacité augmentation/jour (g)</label>
              <input
                type="number"
                id="capaciteAugmentation"
                v-model="form.capaciteAugmentationPoisson"
                :class="{ 'error': errors.capaciteAugmentationPoisson }"
                step="0.01"
                min="0"
              />
              <span v-if="errors.capaciteAugmentationPoisson" class="error-message">{{ errors.capaciteAugmentationPoisson }}</span>
            </div>

            <div class="form-group">
              <div class="progress-indicator">
                <label>Progression actuelle</label>
                <div class="progress-bar">
                  <div 
                    class="progress-fill" 
                    :style="{ width: progression + '%' }"
                    :class="getProgressionClass(progression)"
                  ></div>
                </div>
                <div class="progress-info">
                  <span class="progress-value">{{ progression }}%</span>
                  <span class="progress-details">
                    {{ formatPoids(poidsRestant) }} g restant
                  </span>
                </div>
                <div class="progress-estimation" v-if="poidsRestant > 0 && form.capaciteAugmentationPoisson > 0">
                  <span>≈ {{ joursRestants }} jours restants</span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Informations financières -->
        <div class="form-section">
          <h2>💰 Informations financières</h2>
          <div class="form-grid">
            <div class="form-group">
              <label for="prixAchat">Prix d'achat (MGA)</label>
              <input
                type="number"
                id="prixAchat"
                v-model="form.prixAchatPoisson"
                :class="{ 'error': errors.prixAchatPoisson }"
                step="0.01"
                min="0"
              />
              <span v-if="errors.prixAchatPoisson" class="error-message">{{ errors.prixAchatPoisson }}</span>
            </div>

            <div class="form-group">
              <label for="prixVente">Prix de vente (MGA)</label>
              <input
                type="number"
                id="prixVente"
                v-model="form.prixVentePoisson"
                :class="{ 'error': errors.prixVentePoisson }"
                step="0.01"
                min="0"
              />
              <span v-if="errors.prixVentePoisson" class="error-message">{{ errors.prixVentePoisson }}</span>
            </div>

            <div class="form-group">
              <div class="profit-calculator">
                <label>Calcul du bénéfice</label>
                <div class="profit-details">
                  <div class="profit-item">
                    <span>Coût d'achat:</span>
                    <span>{{ formatCurrency(form.prixAchatPoisson) }}</span>
                  </div>
                  <div class="profit-item">
                    <span>Prix de vente:</span>
                    <span>{{ formatCurrency(form.prixVentePoisson) }}</span>
                  </div>
                  <div class="profit-item total">
                    <span>Bénéfice potentiel:</span>
                    <span :class="getProfitClass(beneficePotentiel)">
                      {{ formatCurrency(beneficePotentiel) }}
                    </span>
                  </div>
                  <div class="profit-item" v-if="form.prixAchatPoisson > 0">
                    <span>Marge bénéficiaire:</span>
                    <span :class="getMarginClass(margePourcentage)">
                      {{ margePourcentage }}%
                    </span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Statut et santé -->
        <div class="form-section">
          <h2>🏥 Statut et santé</h2>
          <div class="form-grid">
            <div class="status-group">
              <div class="checkbox-group">
                <label class="checkbox-label">
                  <input
                    type="checkbox"
                    v-model="form.estRassasiePoisson"
                  />
                  <span class="checkmark"></span>
                  <span>Poisson rassasié</span>
                </label>
                <div class="checkbox-info">
                  Si décoché, le poisson sera marqué comme affamé
                </div>
              </div>

              <div class="checkbox-group">
                <label class="checkbox-label">
                  <input
                    type="checkbox"
                    v-model="form.estEnViePoisson"
                    @change="handleEnVieChange"
                  />
                  <span class="checkmark"></span>
                  <span>Poisson en vie</span>
                </label>
                <div class="checkbox-info">
                  Décocher si le poisson est mort
                </div>
              </div>

              <div class="checkbox-group">
                <label class="checkbox-label">
                  <input
                    type="checkbox"
                    v-model="form.estPretAVendre"
                    :disabled="!form.estEnViePoisson || form.estVenduPoisson"
                  />
                  <span class="checkmark"></span>
                  <span>Prêt à vendre</span>
                </label>
                <div class="checkbox-info">
                  Auto-détecté à 95% du poids max
                </div>
              </div>
            </div>

            <div class="health-indicators">
              <h3>Indicateurs de santé</h3>
              <div class="indicators-grid">
                <div class="indicator" :class="getHealthIndicatorClass('appetit')">
                  <span class="indicator-icon">🍽️</span>
                  <span class="indicator-label">Appétit</span>
                  <span class="indicator-value">{{ form.estRassasiePoisson ? 'Bon' : 'Affamé' }}</span>
                </div>
                <div class="indicator" :class="getHealthIndicatorClass('croissance')">
                  <span class="indicator-icon">📈</span>
                  <span class="indicator-label">Croissance</span>
                  <span class="indicator-value">{{ progression >= 95 ? 'Optimale' : 'En cours' }}</span>
                </div>
                <div class="indicator" :class="getHealthIndicatorClass('sante')">
                  <span class="indicator-icon">❤️</span>
                  <span class="indicator-label">Santé</span>
                  <span class="indicator-value">{{ form.estEnViePoisson ? 'Bonne' : 'Mort' }}</span>
                </div>
                <div class="indicator" :class="getHealthIndicatorClass('vente')">
                  <span class="indicator-icon">💰</span>
                  <span class="indicator-label">Commercial</span>
                  <span class="indicator-value">{{ form.estPretAVendre ? 'Prêt' : 'En élevage' }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Section historique -->
        <div class="form-section">
          <h2>📜 Historique récent</h2>
          <div class="historique-content">
            <div v-if="historique.length > 0" class="historique-list">
              <div v-for="item in historique.slice(0, 3)" :key="item.id" class="historique-item">
                <div class="historique-date">{{ formatDate(item.date) }}</div>
                <div class="historique-description">{{ item.description }}</div>
                <div class="historique-gain" v-if="item.gain > 0">
                  +{{ formatPoids(item.gain) }}g
                </div>
              </div>
            </div>
            <div v-else class="no-historique">
              <p>Aucun historique de nourrissage</p>
            </div>
            <button @click="voirHistoriqueComplet" class="btn-historique">
              Voir l'historique complet
            </button>
          </div>
        </div>

        <!-- Actions supplémentaires -->
        <div class="form-section">
          <h2>🔧 Actions supplémentaires</h2>
          <div class="actions-grid">
            <button @click="adjustWeight" class="action-btn primary">
              <span class="action-icon">⚖️</span>
              <span>Ajuster le poids</span>
            </button>
            <button @click="feedFish" class="action-btn success" :disabled="!form.estEnViePoisson">
              <span class="action-icon">🍽️</span>
              <span>Nourrir le poisson</span>
            </button>
            <button @click="sellFish" class="action-btn revenue" :disabled="!form.estEnViePoisson || form.estVenduPoisson">
              <span class="action-icon">💰</span>
              <span>Vendre ce poisson</span>
            </button>
            <button @click="duplicateFish" class="action-btn info">
              <span class="action-icon">📋</span>
              <span>Dupliquer</span>
            </button>
            <button @click="showDeleteConfirm" class="action-btn danger">
              <span class="action-icon">🗑️</span>
              <span>Supprimer</span>
            </button>
          </div>
        </div>
      </div>

      <!-- Aperçu et statistiques -->
      <div class="preview-sidebar">
        <!-- Carte d'identité du poisson -->
        <div class="identity-card">
          <div class="card-header">
            <h3>🪪 Carte d'identité</h3>
            <div class="card-id">ID: {{ poisson.idPoisson }}</div>
          </div>
          <div class="card-body">
            <div class="identity-item">
              <span class="label">Nom:</span>
              <span class="value">{{ form.nomPoisson || 'Non défini' }}</span>
            </div>
            <div class="identity-item">
              <span class="label">Race:</span>
              <span class="value">{{ getRaceName() || 'Non définie' }}</span>
            </div>
            <div class="identity-item">
              <span class="label">Âge:</span>
              <span class="value">{{ calculateAge() }} jours</span>
            </div>
            <div class="identity-item">
              <span class="label">Arrivée:</span>
              <span class="value">{{ formatDate(form.dateArriveePoisson) }}</span>
            </div>
            <div v-if="form.dateVentePoisson" class="identity-item">
              <span class="label">Vendu le:</span>
              <span class="value">{{ formatDate(form.dateVentePoisson) }}</span>
            </div>
          </div>
        </div>

        <!-- Statistiques de croissance -->
        <div class="stats-card">
          <h3>📊 Statistiques</h3>
          <div class="stats-grid">
            <div class="stat-item">
              <span class="stat-label">Poids initial</span>
              <span class="stat-value">{{ formatPoids(poisson.poidsInitialPoisson) }}g</span>
            </div>
            <div class="stat-item">
              <span class="stat-label">Gain total</span>
              <span class="stat-value success">
                +{{ formatPoids(form.poidsActuelPoisson - poisson.poidsInitialPoisson) }}g
              </span>
            </div>
            <div class="stat-item">
              <span class="stat-label">Gain moyen/jour</span>
              <span class="stat-value">{{ formatPoids(averageDailyGain) }}g</span>
            </div>
            <div class="stat-item">
              <span class="stat-label">Taux croissance</span>
              <span class="stat-value" :class="getGrowthRateClass(growthRate)">
                {{ growthRate }}%
              </span>
            </div>
          </div>
        </div>

        <!-- État actuel -->
        <div class="status-card">
          <h3>🎯 État actuel</h3>
          <div class="status-badges">
            <span class="status-badge" :class="getStatusClass('vie')">
              {{ form.estEnViePoisson ? '🐟 En vie' : '☠️ Mort' }}
            </span>
            <span class="status-badge" :class="getStatusClass('nourri')">
              {{ form.estRassasiePoisson ? '✅ Rassasié' : '🍽️ Affamé' }}
            </span>
            <span class="status-badge" :class="getStatusClass('vente')">
              {{ form.estVenduPoisson ? '💰 Vendu' : form.estPretAVendre ? '🎯 Prêt à vendre' : '📈 En croissance' }}
            </span>
            <span class="status-badge" :class="getStatusClass('progression')">
              {{ progression >= 95 ? '🏆 Maximum' : progression >= 80 ? '⭐ Excellent' : '📊 Bon' }}
            </span>
          </div>
        </div>

        <!-- Progression graphique -->
        <div class="progress-card">
          <h3>📈 Progression</h3>
          <div class="progress-chart">
            <div class="chart-bar">
              <div class="bar-fill" :style="{ width: progression + '%' }"></div>
            </div>
            <div class="chart-labels">
              <span>0%</span>
              <span>50%</span>
              <span>100%</span>
            </div>
            <div class="chart-milestone" :style="{ left: '95%' }">
              <div class="milestone-marker"></div>
              <div class="milestone-label">Prêt à vendre</div>
            </div>
          </div>
        </div>

        <!-- Dernière modification -->
        <div class="last-update-card">
          <h3>⏱️ Dernières modifications</h3>
          <div class="update-info">
            <div class="update-item">
              <span class="label">Modifié le:</span>
              <span class="value">{{ formatDateTime(poisson.dateModificationPoisson) }}</span>
            </div>
            <div class="update-item">
              <span class="label">Créé le:</span>
              <span class="value">{{ formatDateTime(poisson.dateCreationPoisson) }}</span>
            </div>
            <div class="update-item">
              <span class="label">Dernier repas:</span>
              <span class="value">{{ getLastFeeding() }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Chargement -->
    <div v-else class="loading-container">
      <div class="spinner"></div>
      <p>Chargement du poisson...</p>
    </div>

    <!-- Modales -->
    
    <!-- Modale ajustement de poids -->
    <div v-if="showWeightModal" class="modal-overlay">
      <div class="modal">
        <div class="modal-header">
          <h3>⚖️ Ajuster le poids</h3>
          <button @click="showWeightModal = false" class="modal-close">×</button>
        </div>
        <div class="modal-body">
          <div class="form-group">
            <label>Nouveau poids (g)</label>
            <input
              type="number"
              v-model="newWeight"
              step="0.01"
              min="0"
              :max="form.poidsMaximalPoisson"
            />
            <div class="input-info">
              Poids actuel: {{ formatPoids(form.poidsActuelPoisson) }}g
            </div>
          </div>
          <div class="weight-difference">
            <span v-if="weightDifference > 0" class="positive">
              Gain: +{{ formatPoids(weightDifference) }}g
            </span>
            <span v-else-if="weightDifference < 0" class="negative">
              Perte: {{ formatPoids(weightDifference) }}g
            </span>
          </div>
          <div class="form-group">
            <label>Raison de l'ajustement</label>
            <textarea v-model="weightAdjustmentReason" rows="3" placeholder="Ex: Nourrissage, mesure, correction..."></textarea>
          </div>
        </div>
        <div class="modal-footer">
          <button @click="showWeightModal = false" class="btn-cancel">Annuler</button>
          <button @click="applyWeightAdjustment" class="btn-confirm">Appliquer</button>
        </div>
      </div>
    </div>

    <!-- Modale nourrissage -->
    <div v-if="showFeedModal" class="modal-overlay">
      <div class="modal">
        <div class="modal-header">
          <h3>🍽️ Nourrir le poisson</h3>
          <button @click="showFeedModal = false" class="modal-close">×</button>
        </div>
        <div class="modal-body">
          <div class="form-group">
            <label>Quantité de nourriture (g)</label>
            <input type="number" v-model="feedAmount" step="0.01" min="0">
          </div>
          <div class="form-group">
            <label>Type d'aliment</label>
            <select v-model="selectedFood">
              <option value="">Sélectionner un aliment</option>
              <option v-for="food in foodTypes" :key="food.id" :value="food.id">
                {{ food.name }} ({{ food.proteines }}g protéines/kg)
              </option>
            </select>
          </div>
          <div class="nutrition-info" v-if="selectedFood">
            <div class="info-item">
              <span>Protéines:</span>
              <span>{{ calculateProteins() }}g</span>
            </div>
            <div class="info-item">
              <span>Glucides:</span>
              <span>{{ calculateCarbs() }}g</span>
            </div>
            <div class="info-item">
              <span>Gain estimé:</span>
              <span class="estimated-gain">{{ formatPoids(estimatedGain) }}g</span>
            </div>
          </div>
        </div>
        <div class="modal-footer">
          <button @click="showFeedModal = false" class="btn-cancel">Annuler</button>
          <button @click="feedFishNow" class="btn-confirm">Nourrir</button>
        </div>
      </div>
    </div>

    <!-- Modale vente -->
    <div v-if="showSellModal" class="modal-overlay">
      <div class="modal">
        <div class="modal-header">
          <h3>💰 Vendre le poisson</h3>
          <button @click="showSellModal = false" class="modal-close">×</button>
        </div>
        <div class="modal-body">
          <div class="sale-info">
            <div class="info-item">
              <span>Poisson:</span>
              <span>{{ form.nomPoisson }}</span>
            </div>
            <div class="info-item">
              <span>Poids actuel:</span>
              <span>{{ formatPoids(form.poidsActuelPoisson) }}g</span>
            </div>
            <div class="info-item">
              <span>Progression:</span>
              <span>{{ progression }}%</span>
            </div>
          </div>
          <div class="form-group">
            <label>Prix de vente (MGA)</label>
            <input type="number" v-model="salePrice" :min="form.prixAchatPoisson">
            <div class="input-info">
              Prix suggéré: {{ formatCurrency(form.prixVentePoisson) }}
            </div>
          </div>
          <div class="profit-summary">
            <div class="profit-item">
              <span>Coût d'achat:</span>
              <span>{{ formatCurrency(form.prixAchatPoisson) }}</span>
            </div>
            <div class="profit-item">
              <span>Prix de vente:</span>
              <span>{{ formatCurrency(salePrice) }}</span>
            </div>
            <div class="profit-item total">
              <span>Bénéfice:</span>
              <span :class="getProfitClass(salePrice - form.prixAchatPoisson)">
                {{ formatCurrency(salePrice - form.prixAchatPoisson) }}
              </span>
            </div>
          </div>
        </div>
        <div class="modal-footer">
          <button @click="showSellModal = false" class="btn-cancel">Annuler</button>
          <button @click="confirmSell" class="btn-confirm">Confirmer la vente</button>
        </div>
      </div>
    </div>

    <!-- Modale suppression -->
    <div v-if="showDeleteModal" class="modal-overlay">
      <div class="modal">
        <div class="modal-header">
          <h3>🗑️ Supprimer le poisson</h3>
          <button @click="showDeleteModal = false" class="modal-close">×</button>
        </div>
        <div class="modal-body">
          <div class="warning-message">
            <div class="warning-icon">⚠️</div>
            <div class="warning-content">
              <h4>Attention !</h4>
              <p>Vous êtes sur le point de supprimer le poisson <strong>"{{ form.nomPoisson }}"</strong>.</p>
              <p>Cette action est <strong>irréversible</strong> et supprimera toutes les données associées.</p>
            </div>
          </div>
          <div class="confirmation-input">
            <label>
              Tapez <strong>"SUPPRIMER"</strong> pour confirmer:
            </label>
            <input
              type="text"
              v-model="deleteConfirmation"
              placeholder="SUPPRIMER"
            />
          </div>
        </div>
        <div class="modal-footer">
          <button @click="showDeleteModal = false" class="btn-cancel">Annuler</button>
          <button 
            @click="confirmDelete" 
            class="btn-danger"
            :disabled="deleteConfirmation !== 'SUPPRIMER'"
          >
            Supprimer définitivement
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import poissonService from '../services/poissonService'
import nourrissageService from '../services/nourrissageService'
import alimentService from '../services/alimentService'

export default {
  name: 'ModifierPoisson',
  setup() {
    const route = useRoute()
    const router = useRouter()
    
    // État principal
    const loading = ref(false)
    const poisson = ref(null)
    const races = ref([])
    const historique = ref([])
    
    // Formulaire
    const form = ref({
      nomPoisson: '',
      idRacePoisson: '',
      prixAchatPoisson: 0,
      prixVentePoisson: 0,
      poidsMaximalPoisson: 0,
      capaciteAugmentationPoisson: 0,
      poidsActuelPoisson: 0,
      dateArriveePoisson: '',
      dateVentePoisson: '',
      estRassasiePoisson: false,
      estVenduPoisson: false,
      estEnViePoisson: true,
      estPretAVendre: false
    })
    
    // Erreurs et messages
    const errors = ref({})
    const errorMessage = ref('')
    const successMessage = ref('')
    
    // Calculs
    const progression = ref(0)
    const poidsRestant = ref(0)
    const joursRestants = ref(0)
    const beneficePotentiel = ref(0)
    const margePourcentage = ref(0)
    const averageDailyGain = ref(0)
    const growthRate = ref(0)
    
    // Modales
    const showWeightModal = ref(false)
    const showFeedModal = ref(false)
    const showSellModal = ref(false)
    const showDeleteModal = ref(false)
    
    // Données modales
    const newWeight = ref(0)
    const weightAdjustmentReason = ref('')
    const feedAmount = ref(0)
    const selectedFood = ref('')
    const foodTypes = ref([])
    const salePrice = ref(0)
    const deleteConfirmation = ref('')

    // Computed properties
    const isFormValid = computed(() => {
      return form.value.nomPoisson &&
             form.value.idRacePoisson &&
             form.value.poidsActuelPoisson >= 0 &&
             form.value.poidsMaximalPoisson > 0 &&
             form.value.poidsActuelPoisson <= form.value.poidsMaximalPoisson &&
             form.value.dateArriveePoisson
    })

    const weightDifference = computed(() => {
      return newWeight.value - form.value.poidsActuelPoisson
    })

    const estimatedGain = computed(() => {
      if (!feedAmount.value || !selectedFood.value) return 0
      // Logique simplifiée de calcul du gain
      return feedAmount.value * 0.1 // 10% de conversion nourriture -> poids
    })

    // Charger le poisson
    const loadPoisson = async () => {
      loading.value = true
      try {
        const id = route.params.id
        const data = await poissonService.getPoissonById(id)
        poisson.value = data
        
        // Initialiser le formulaire
        form.value = {
          nomPoisson: data.nomPoisson || '',
          idRacePoisson: data.racePoisson?.idRacePoisson || '',
          prixAchatPoisson: data.prixAchatPoisson || 0,
          prixVentePoisson: data.prixVentePoisson || 0,
          poidsMaximalPoisson: data.poidsMaximalPoisson || 0,
          capaciteAugmentationPoisson: data.capaciteAugmentationPoisson || 20,
          poidsActuelPoisson: data.poidsActuelPoisson || 0,
          dateArriveePoisson: data.dateArriveePoisson ? 
            new Date(data.dateArriveePoisson).toISOString().split('T')[0] : '',
          dateVentePoisson: data.dateVentePoisson ? 
            new Date(data.dateVentePoisson).toISOString().split('T')[0] : '',
          estRassasiePoisson: data.estRassasiePoisson || false,
          estVenduPoisson: data.estVenduPoisson || false,
          estEnViePoisson: data.estEnViePoisson !== false,
          estPretAVendre: data.estPretAVendre || false
        }
        
        // Calculer les valeurs dérivées
        calculateProgression()
        calculateFinancials()
        calculateGrowthStats()
        
        // Charger les données supplémentaires
        await loadRaces()
        await loadHistorique()
        await loadFoodTypes()
        
      } catch (error) {
        console.error('Erreur chargement poisson:', error)
        errorMessage.value = 'Impossible de charger le poisson'
        router.push('/poissons')
      } finally {
        loading.value = false
      }
    }

    // Charger les races
    const loadRaces = async () => {
      try {
        const data = await poissonService.getRacesPoisson()
        races.value = data
      } catch (error) {
        console.error('Erreur chargement races:', error)
      }
    }

    // Charger l'historique
    const loadHistorique = async () => {
      try {
        const id = route.params.id
        const data = await nourrissageService.getHistoriquePoisson(id)
        historique.value = data.map(item => ({
          id: item.idFisakafoanana,
          date: item.dateNourrissageFisakafoanana,
          description: `Nourrissage: ${item.quantiteNourritureFisakafoanana}g`,
          gain: item.gainPoidsFisakafoanana
        }))
      } catch (error) {
        console.error('Erreur chargement historique:', error)
        historique.value = []
      }
    }

    // Charger les types d'aliments
    const loadFoodTypes = async () => {
      try {
        const data = await alimentService.getAliments()
        foodTypes.value = data.map(aliment => ({
          id: aliment.idAliment,
          name: aliment.nomAliment,
          proteines: aliment.proteinesParKgAliment,
          glucides: aliment.glucidesParKgAliment
        }))
      } catch (error) {
        console.error('Erreur chargement aliments:', error)
        foodTypes.value = []
      }
    }

    // Calculer la progression
    const calculateProgression = () => {
      if (!form.value.poidsActuelPoisson || !form.value.poidsMaximalPoisson) {
        progression.value = 0
        poidsRestant.value = 0
        return
      }
      
      progression.value = Math.min(
        Math.round((form.value.poidsActuelPoisson / form.value.poidsMaximalPoisson) * 100),
        100
      )
      
      poidsRestant.value = Math.max(0, form.value.poidsMaximalPoisson - form.value.poidsActuelPoisson)
      
      // Calculer les jours restants
      if (form.value.capaciteAugmentationPoisson > 0 && poidsRestant.value > 0) {
        joursRestants.value = Math.ceil(poidsRestant.value / form.value.capaciteAugmentationPoisson)
      } else {
        joursRestants.value = 0
      }
      
      // Mettre à jour le statut "prêt à vendre"
      form.value.estPretAVendre = progression.value >= 95
    }

    // Calculer les valeurs financières
    const calculateFinancials = () => {
      beneficePotentiel.value = form.value.prixVentePoisson - form.value.prixAchatPoisson
      
      if (form.value.prixAchatPoisson > 0) {
        margePourcentage.value = Math.round(
          (beneficePotentiel.value / form.value.prixAchatPoisson) * 100
        )
      } else {
        margePourcentage.value = 0
      }
    }

    // Calculer les statistiques de croissance
    const calculateGrowthStats = () => {
      if (!poisson.value || !poisson.value.poidsInitialPoisson) return
      
      const daysAlive = calculateAge()
      if (daysAlive > 0) {
        const totalGain = form.value.poidsActuelPoisson - poisson.value.poidsInitialPoisson
        averageDailyGain.value = Math.round((totalGain / daysAlive) * 100) / 100
        
        if (poisson.value.poidsInitialPoisson > 0) {
          growthRate.value = Math.round(
            (totalGain / poisson.value.poidsInitialPoisson) * 100
          )
        }
      }
    }

    // Calculer l'âge en jours
    const calculateAge = () => {
      if (!form.value.dateArriveePoisson) return 0
      
      const arrival = new Date(form.value.dateArriveePoisson)
      const today = new Date()
      const diffTime = Math.abs(today - arrival)
      return Math.ceil(diffTime / (1000 * 60 * 60 * 24))
    }

    // Formater les données
    const formatCurrency = (amount) => {
      if (!amount) return '0 MGA'
      return new Intl.NumberFormat('fr-MG', {
        style: 'currency',
        currency: 'MGA',
        minimumFractionDigits: 0,
        maximumFractionDigits: 0
      }).format(amount)
    }

    const formatPoids = (poids) => {
      if (!poids) return '0'
      return Math.round(poids * 100) / 100
    }

    const formatDate = (dateString) => {
      if (!dateString) return 'N/A'
      return new Date(dateString).toLocaleDateString('fr-FR')
    }

    const formatDateTime = (dateTimeString) => {
      if (!dateTimeString) return 'N/A'
      const date = new Date(dateTimeString)
      return date.toLocaleString('fr-FR', {
        day: '2-digit',
        month: '2-digit',
        year: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
      })
    }

    // Classes CSS dynamiques
    const getProgressionClass = (prog) => {
      if (prog >= 95) return 'progress-high'
      if (prog >= 80) return 'progress-good'
      if (prog >= 60) return 'progress-medium'
      if (prog >= 40) return 'progress-low'
      return 'progress-very-low'
    }

    const getProfitClass = (profit) => {
      if (profit > 0) return 'profit-positive'
      if (profit < 0) return 'profit-negative'
      return 'profit-neutral'
    }

    const getMarginClass = (margin) => {
      if (margin >= 50) return 'margin-excellent'
      if (margin >= 30) return 'margin-good'
      if (margin >= 10) return 'margin-average'
      return 'margin-low'
    }

    const getStatusClass = (type) => {
      switch (type) {
        case 'vie':
          return form.value.estEnViePoisson ? 'status-alive' : 'status-dead'
        case 'nourri':
          return form.value.estRassasiePoisson ? 'status-fed' : 'status-hungry'
        case 'vente':
          return form.value.estVenduPoisson ? 'status-sold' : 
                 form.value.estPretAVendre ? 'status-ready' : 'status-growing'
        case 'progression':
          if (progression.value >= 95) return 'status-max'
          if (progression.value >= 80) return 'status-excellent'
          return 'status-good'
        default:
          return ''
      }
    }

    const getHealthIndicatorClass = (type) => {
      switch (type) {
        case 'appetit':
          return form.value.estRassasiePoisson ? 'indicator-good' : 'indicator-warning'
        case 'croissance':
          return progression.value >= 95 ? 'indicator-excellent' : 
                 progression.value >= 80 ? 'indicator-good' : 'indicator-average'
        case 'sante':
          return form.value.estEnViePoisson ? 'indicator-good' : 'indicator-danger'
        case 'vente':
          return form.value.estVenduPoisson ? 'indicator-sold' :
                 form.value.estPretAVendre ? 'indicator-ready' : 'indicator-growing'
        default:
          return 'indicator-neutral'
      }
    }

    const getGrowthRateClass = (rate) => {
      if (rate >= 100) return 'growth-excellent'
      if (rate >= 50) return 'growth-good'
      if (rate >= 20) return 'growth-average'
      return 'growth-low'
    }

    const getRaceName = () => {
      const race = races.value.find(r => r.idRacePoisson == form.value.idRacePoisson)
      return race ? race.nomRacePoisson : ''
    }

    const getLastFeeding = () => {
      if (historique.value.length === 0) return 'Jamais'
      const last = historique.value[0]
      return `${formatDate(last.date)} (${formatPoids(last.gain)}g)`
    }

    // Gestion des changements
    const handleVenduChange = () => {
      if (form.value.estVenduPoisson) {
        form.value.dateVentePoisson = new Date().toISOString().split('T')[0]
        form.value.estPretAVendre = true
      } else {
        form.value.dateVentePoisson = ''
      }
    }

    const handleEnVieChange = () => {
      if (!form.value.estEnViePoisson) {
        form.value.estRassasiePoisson = false
        form.value.estPretAVendre = false
        form.value.estVenduPoisson = false
      }
    }

    // Actions
    const refreshPoisson = () => {
      loadPoisson()
    }

   const savePoisson = async () => {
      if (!validateForm()) {
          errorMessage.value = 'Veuillez corriger les erreurs dans le formulaire'
          return
      }

      loading.value = true
      errorMessage.value = ''
      successMessage.value = ''

      try {
          // Préparer les données pour l'API
          const poissonData = {
              nomPoisson: form.value.nomPoisson,
              racePoisson: {
                  idRacePoisson: form.value.idRacePoisson
              },
              prixAchatPoisson: parseFloat(form.value.prixAchatPoisson) || 0,
              prixVentePoisson: parseFloat(form.value.prixVentePoisson) || 0,
              poidsMaximalPoisson: parseFloat(form.value.poidsMaximalPoisson) || 0,
              capaciteAugmentationPoisson: parseFloat(form.value.capaciteAugmentationPoisson) || 0,
              poidsActuelPoisson: parseFloat(form.value.poidsActuelPoisson) || 0,
              dateArriveePoisson: form.value.dateArriveePoisson,
              dateVentePoisson: form.value.estVenduPoisson ? form.value.dateVentePoisson : null,
              estRassasiePoisson: form.value.estRassasiePoisson,
              estVenduPoisson: form.value.estVenduPoisson,
              estEnViePoisson: form.value.estEnViePoisson,
              estPretAVendre: form.value.estPretAVendre
          }

          console.log('📤 Données envoyées:', poissonData)
          
          // Appeler l'API
          const response = await poissonService.updatePoisson(route.params.id, poissonData)
          console.log('✅ Réponse:', response)
          
          successMessage.value = 'Poisson mis à jour avec succès !'
          
          // Recharger les données
          await loadPoisson()
          
          // Cacher le message après 3 secondes
          setTimeout(() => {
              successMessage.value = ''
          }, 3000)

      } catch (error) {
          console.error('❌ Erreur:', error)
          
          // Gestion d'erreur détaillée
          if (error.response) {
              console.error('Status:', error.response.status)
              console.error('Data:', error.response.data)
              
              if (error.response.data) {
                  const errorData = error.response.data
                  errorMessage.value = errorData.message || 
                                    errorData.error || 
                                    `Erreur serveur (${error.response.status})`
              } else {
                  errorMessage.value = `Erreur serveur (${error.response.status})`
              }
          } else if (error.request) {
              errorMessage.value = 'Pas de réponse du serveur. Vérifiez que le serveur Spring Boot est démarré.'
          } else {
              errorMessage.value = 'Erreur: ' + error.message
          }
      } finally {
          loading.value = false
      }
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

      // Poids actuel
      if (form.value.poidsActuelPoisson < 0) {
        errors.value.poidsActuelPoisson = 'Le poids actuel doit être positif'
      }

      // Poids maximal
      if (form.value.poidsMaximalPoisson <= 0) {
        errors.value.poidsMaximalPoisson = 'Le poids maximal doit être positif'
      }

      // Vérifier que poids actuel <= poids maximal
      if (form.value.poidsActuelPoisson > form.value.poidsMaximalPoisson) {
        errors.value.poidsActuelPoisson = 'Le poids actuel ne peut pas dépasser le poids maximal'
      }

      // Date arrivée
      if (!form.value.dateArriveePoisson) {
        errors.value.dateArriveePoisson = 'La date d\'arrivée est requise'
      }

      return Object.keys(errors.value).length === 0
    }

    // Actions modales
    const adjustWeight = () => {
      newWeight.value = form.value.poidsActuelPoisson
      showWeightModal.value = true
    }

    const applyWeightAdjustment = () => {
      form.value.poidsActuelPoisson = newWeight.value
      calculateProgression()
      showWeightModal.value = false
      
      // Enregistrer la raison dans un historique
      console.log('Ajustement poids:', weightAdjustmentReason.value)
    }

    const feedFish = () => {
      feedAmount.value = 0
      selectedFood.value = ''
      showFeedModal.value = true
    }

    const calculateProteins = () => {
      if (!selectedFood.value || !feedAmount.value) return 0
      const food = foodTypes.value.find(f => f.id == selectedFood.value)
      return food ? (feedAmount.value * food.proteines / 1000) : 0
    }

    const calculateCarbs = () => {
      if (!selectedFood.value || !feedAmount.value) return 0
      const food = foodTypes.value.find(f => f.id == selectedFood.value)
      return food ? (feedAmount.value * food.glucides / 1000) : 0
    }

    const feedFishNow = () => {
      if (!feedAmount.value || feedAmount.value <= 0) {
        alert('Veuillez entrer une quantité valide')
        return
      }

      // Simuler l'ajout de poids basé sur la nourriture
      const gain = estimatedGain.value
      form.value.poidsActuelPoisson += gain
      form.value.estRassasiePoisson = true
      
      calculateProgression()
      showFeedModal.value = false
      
      // Ici, vous appelleriez votre API pour enregistrer le nourrissage
      console.log('Nourrissage:', { amount: feedAmount.value, food: selectedFood.value, gain })
    }

    const sellFish = () => {
      salePrice.value = form.value.prixVentePoisson
      showSellModal.value = true
    }

    const confirmSell = () => {
      if (salePrice.value <= 0) {
        alert('Veuillez entrer un prix de vente valide')
        return
      }

      form.value.estVenduPoisson = true
      form.value.estPretAVendre = true
      form.value.dateVentePoisson = new Date().toISOString().split('T')[0]
      form.value.prixVentePoisson = salePrice.value
      
      showSellModal.value = false
      savePoisson()
    }

    const duplicateFish = () => {
      if (confirm(`Dupliquer le poisson "${form.value.nomPoisson}" ?`)) {
        // Créer une copie avec un nom modifié
        const newName = `${form.value.nomPoisson}-copie`
        
        // Ici, vous appelleriez votre API pour créer une copie
        console.log('Duplication:', newName)
        alert('Fonctionnalité de duplication à implémenter')
      }
    }

    const showDeleteConfirm = () => {
      deleteConfirmation.value = ''
      showDeleteModal.value = true
    }

    const confirmDelete = async () => {
      try {
        await poissonService.deletePoisson(route.params.id)
        alert('Poisson supprimé avec succès')
        router.push('/poissons')
      } catch (error) {
        console.error('Erreur suppression:', error)
        errorMessage.value = 'Erreur lors de la suppression'
        showDeleteModal.value = false
      }
    }

    const voirHistoriqueComplet = () => {
      router.push(`/historique?poisson=${route.params.id}`)
    }

    // Initialisation
    onMounted(() => {
      loadPoisson()
    })

    // Watchers pour les calculs automatiques
    watch(() => form.value.poidsActuelPoisson, () => {
      calculateProgression()
    })

    watch(() => form.value.poidsMaximalPoisson, () => {
      calculateProgression()
    })

    watch(() => [form.value.prixAchatPoisson, form.value.prixVentePoisson], () => {
      calculateFinancials()
    })

    return {
      // État
      loading,
      poisson,
      form,
      races,
      historique,
      errors,
      errorMessage,
      successMessage,
      
      // Calculs
      progression,
      poidsRestant,
      joursRestants,
      beneficePotentiel,
      margePourcentage,
      averageDailyGain,
      growthRate,
      isFormValid,
      
      // Modales
      showWeightModal,
      showFeedModal,
      showSellModal,
      showDeleteModal,
      
      // Données modales
      newWeight,
      weightDifference,
      weightAdjustmentReason,
      feedAmount,
      selectedFood,
      foodTypes,
      estimatedGain,
      salePrice,
      deleteConfirmation,
      
      // Méthodes
      formatCurrency,
      formatPoids,
      formatDate,
      formatDateTime,
      getProgressionClass,
      getProfitClass,
      getMarginClass,
      getStatusClass,
      getHealthIndicatorClass,
      getGrowthRateClass,
      getRaceName,
      getLastFeeding,
      calculateAge,
      handleVenduChange,
      handleEnVieChange,
      refreshPoisson,
      savePoisson,
      adjustWeight,
      applyWeightAdjustment,
      feedFish,
      calculateProteins,
      calculateCarbs,
      feedFishNow,
      sellFish,
      confirmSell,
      duplicateFish,
      showDeleteConfirm,
      confirmDelete,
      voirHistoriqueComplet
    }
  }
}
</script>

<style scoped>
@import '../assets/styles/modifier';
</style>
