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
          poidsActuelPoisson: parseFloat(form.value.poidsActuelPoisson),
          dateArriveePoisson: form.value.dateArriveePoisson,
          dateVentePoisson: form.value.estVenduPoisson ? form.value.dateVentePoisson : null,
          estRassasiePoisson: form.value.estRassasiePoisson,
          estVenduPoisson: form.value.estVenduPoisson,
          estEnViePoisson: form.value.estEnViePoisson,
          estPretAVendre: form.value.estPretAVendre
        }

        // Appeler l'API
        await poissonService.updatePoisson(route.params.id, poissonData)
        
        successMessage.value = 'Poisson mis à jour avec succès !'
        
        // Recharger les données
        await loadPoisson()
        
        // Rediriger après un délai
        setTimeout(() => {
          successMessage.value = ''
        }, 3000)

      } catch (error) {
        console.error('Erreur mise à jour poisson:', error)
        errorMessage.value = error.response?.data?.message || 'Erreur lors de la mise à jour'
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
/* Styles spécifiques à la page de modification */
.modifier-poisson {
  padding: 20px;
  background: #f7fafc;
  min-height: 100vh;
}

/* En-tête */
.page-header {
  background: white;
  padding: 25px;
  border-radius: 12px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.05);
  margin-bottom: 25px;
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.header-left {
  flex: 1;
}

.btn-back {
  color: #4a5568;
  text-decoration: none;
  font-weight: 600;
  margin-bottom: 10px;
  display: inline-block;
}

.page-header h1 {
  margin: 0 0 5px 0;
  color: #2d3748;
  font-size: 28px;
}

.subtitle {
  margin: 0;
  color: #718096;
  font-size: 16px;
}

.header-right {
  display: flex;
  gap: 15px;
  align-items: center;
}

.btn-refresh, .btn-save {
  padding: 10px 20px;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-weight: 600;
  transition: all 0.2s;
}

.btn-refresh {
  background: #e2e8f0;
  color: #4a5568;
}

.btn-refresh:hover:not(:disabled) {
  background: #cbd5e0;
}

.btn-refresh:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.btn-save {
  background: #48bb78;
  color: white;
}

.btn-save:hover:not(:disabled) {
  background: #38a169;
}

.btn-save:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* Alertes */
.alert {
  padding: 15px 20px;
  border-radius: 8px;
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  gap: 10px;
}

.alert.error {
  background: #fed7d7;
  color: #742a2a;
  border-left: 4px solid #f56565;
}

.alert.success {
  background: #c6f6d5;
  color: #22543d;
  border-left: 4px solid #48bb78;
}

.alert-icon {
  font-size: 20px;
}

.alert-close {
  margin-left: auto;
  background: none;
  border: none;
  font-size: 20px;
  cursor: pointer;
  color: inherit;
}

/* Contenu principal */
.content-container {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 30px;
}

.form-preview-container {
  display: flex;
  flex-direction: column;
  gap: 30px;
}

/* Sections du formulaire */
.form-section {
  background: white;
  padding: 30px;
  border-radius: 12px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.05);
}

.form-section h2 {
  margin: 0 0 25px 0;
  color: #2d3748;
  font-size: 20px;
  display: flex;
  align-items: center;
  gap: 10px;
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 25px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-group label {
  font-weight: 600;
  color: #4a5568;
  font-size: 14px;
}

.form-group input,
.form-group select,
.form-group textarea {
  padding: 12px 15px;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  font-size: 16px;
  transition: all 0.2s;
}

.form-group input:focus,
.form-group select:focus,
.form-group textarea:focus {
  outline: none;
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.form-group input.error,
.form-group select.error {
  border-color: #f56565;
}

.error-message {
  color: #f56565;
  font-size: 14px;
  margin-top: 5px;
}

.input-info {
  font-size: 12px;
  color: #718096;
  margin-top: 5px;
}

/* Indicateur de progression */
.progress-indicator {
  padding: 15px;
  background: #f7fafc;
  border-radius: 8px;
}

.progress-bar {
  height: 10px;
  background: #e2e8f0;
  border-radius: 5px;
  overflow: hidden;
  margin: 10px 0;
}

.progress-fill {
  height: 100%;
  transition: width 0.3s;
}

.progress-high { background: #48bb78; }
.progress-good { background: #68d391; }
.progress-medium { background: #ecc94b; }
.progress-low { background: #ed8936; }
.progress-very-low { background: #f56565; }

.progress-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 10px;
}

.progress-value {
  font-size: 18px;
  font-weight: bold;
  color: #2d3748;
}

.progress-details {
  font-size: 14px;
  color: #718096;
}

.progress-estimation {
  margin-top: 10px;
  font-size: 14px;
  color: #4299e1;
  font-weight: 600;
}

/* Calcul du bénéfice */
.profit-calculator {
  padding: 15px;
  background: #f7fafc;
  border-radius: 8px;
}

.profit-details {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-top: 10px;
}

.profit-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 5px 0;
}

.profit-item.total {
  border-top: 2px solid #e2e8f0;
  padding-top: 10px;
  margin-top: 5px;
  font-weight: bold;
}

.profit-positive { color: #48bb78; }
.profit-negative { color: #f56565; }
.profit-neutral { color: #4a5568; }

.margin-excellent { color: #48bb78; }
.margin-good { color: #68d391; }
.margin-average { color: #ecc94b; }
.margin-low { color: #ed8936; }

/* Groupes de statut */
.status-group {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.checkbox-group {
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.checkbox-label {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  user-select: none;
}

.checkbox-label input {
  display: none;
}

.checkmark {
  width: 20px;
  height: 20px;
  border: 2px solid #cbd5e0;
  border-radius: 4px;
  position: relative;
  transition: all 0.2s;
}

.checkbox-label input:checked + .checkmark {
  background: #4299e1;
  border-color: #4299e1;
}

.checkbox-label input:checked + .checkmark::after {
  content: '✓';
  position: absolute;
  color: white;
  font-size: 14px;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
}

.checkbox-info {
  font-size: 12px;
  color: #718096;
  margin-left: 30px;
}

/* Indicateurs de santé */
.health-indicators {
  padding: 20px;
  background: #f7fafc;
  border-radius: 8px;
}

.health-indicators h3 {
  margin: 0 0 15px 0;
  color: #2d3748;
  font-size: 16px;
}

.indicators-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 15px;
}

.indicator {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 15px;
  border-radius: 8px;
  background: white;
  box-shadow: 0 2px 5px rgba(0,0,0,0.05);
}

.indicator-icon {
  font-size: 24px;
  margin-bottom: 8px;
}

.indicator-label {
  font-size: 12px;
  color: #718096;
  margin-bottom: 5px;
}

.indicator-value {
  font-size: 14px;
  font-weight: 600;
}

.indicator-good { border-top: 3px solid #48bb78; }
.indicator-excellent { border-top: 3px solid #38a169; }
.indicator-average { border-top: 3px solid #ecc94b; }
.indicator-warning { border-top: 3px solid #ed8936; }
.indicator-danger { border-top: 3px solid #f56565; }
.indicator-ready { border-top: 3px solid #9f7aea; }
.indicator-growing { border-top: 3px solid #4299e1; }
.indicator-sold { border-top: 3px solid #a0aec0; }
.indicator-neutral { border-top: 3px solid #cbd5e0; }

/* Historique */
.historique-content {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.historique-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.historique-item {
  display: flex;
  align-items: center;
  gap: 15px;
  padding: 12px;
  background: #f7fafc;
  border-radius: 6px;
  border-left: 4px solid #48bb78;
}

.historique-date {
  min-width: 100px;
  font-size: 14px;
  color: #4a5568;
  font-weight: 600;
}

.historique-description {
  flex: 1;
  color: #2d3748;
}

.historique-gain {
  background: #c6f6d5;
  color: #22543d;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 600;
}

.no-historique {
  text-align: center;
  padding: 20px;
  color: #718096;
}

.btn-historique {
  padding: 10px;
  background: #4299e1;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-weight: 600;
  margin-top: 10px;
}

.btn-historique:hover {
  background: #3182ce;
}

/* Actions supplémentaires */
.actions-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
  gap: 15px;
}

.action-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  padding: 20px;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.action-btn:hover {
  transform: translateY(-3px);
}

.action-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
  transform: none;
}

.action-btn.primary {
  background: #4299e1;
  color: white;
}

.action-btn.success {
  background: #48bb78;
  color: white;
}

.action-btn.revenue {
  background: #9f7aea;
  color: white;
}

.action-btn.info {
  background: #38b2ac;
  color: white;
}

.action-btn.danger {
  background: #f56565;
  color: white;
}

.action-icon {
  font-size: 24px;
}

/* Barre latérale */
.preview-sidebar {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

/* Cartes de la barre latérale */
.identity-card,
.stats-card,
.status-card,
.progress-card,
.last-update-card {
  background: white;
  padding: 25px;
  border-radius: 12px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.05);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.card-header h3 {
  margin: 0;
  color: #2d3748;
  font-size: 18px;
}

.card-id {
  font-size: 12px;
  color: #718096;
  background: #f7fafc;
  padding: 4px 8px;
  border-radius: 4px;
}

.card-body {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.identity-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 8px;
  border-bottom: 1px solid #f7fafc;
}

.identity-item:last-child {
  border-bottom: none;
}

.identity-item .label {
  color: #718096;
  font-size: 14px;
}

.identity-item .value {
  font-weight: 600;
  color: #2d3748;
}

/* Statistiques */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 15px;
}

.stat-item {
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.stat-label {
  font-size: 12px;
  color: #718096;
}

.stat-value {
  font-size: 16px;
  font-weight: bold;
  color: #2d3748;
}

.stat-value.success {
  color: #48bb78;
}

.growth-excellent { color: #48bb78; }
.growth-good { color: #68d391; }
.growth-average { color: #ecc94b; }
.growth-low { color: #ed8936; }

/* Badges de statut */
.status-badges {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.status-badge {
  padding: 8px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
}

.status-alive { background: #c6f6d5; color: #22543d; }
.status-dead { background: #fed7d7; color: #742a2a; }
.status-fed { background: #bee3f8; color: #2c5282; }
.status-hungry { background: #feebc8; color: #744210; }
.status-sold { background: #e2e8f0; color: #4a5568; }
.status-ready { background: #d6bcfa; color: #553c9a; }
.status-growing { background: #c6f6d5; color: #22543d; }
.status-max { background: #9ae6b4; color: #22543d; }
.status-excellent { background: #68d391; color: #22543d; }
.status-good { background: #9ae6b4; color: #22543d; }

/* Graphique de progression */
.progress-chart {
  position: relative;
  margin-top: 20px;
}

.chart-bar {
  height: 20px;
  background: #e2e8f0;
  border-radius: 10px;
  overflow: hidden;
}

.bar-fill {
  height: 100%;
  background: linear-gradient(90deg, #48bb78, #38a169);
  transition: width 0.5s ease;
}

.chart-labels {
  display: flex;
  justify-content: space-between;
  margin-top: 8px;
  font-size: 12px;
  color: #718096;
}

.chart-milestone {
  position: absolute;
  top: -25px;
  transform: translateX(-50%);
  display: flex;
  flex-direction: column;
  align-items: center;
}

.milestone-marker {
  width: 2px;
  height: 20px;
  background: #f56565;
}

.milestone-label {
  font-size: 10px;
  color: #f56565;
  white-space: nowrap;
  margin-top: 4px;
}

/* Dernières modifications */
.update-info {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.update-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 8px;
  border-bottom: 1px solid #f7fafc;
}

.update-item:last-child {
  border-bottom: none;
}

.update-item .label {
  font-size: 12px;
  color: #718096;
}

.update-item .value {
  font-size: 14px;
  color: #2d3748;
  font-weight: 500;
}

/* Chargement */
.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 300px;
  gap: 20px;
}

.spinner {
  width: 40px;
  height: 40px;
  border: 4px solid #e2e8f0;
  border-top: 4px solid #4299e1;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
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
  padding: 20px;
}

.modal {
  background: white;
  border-radius: 12px;
  max-width: 500px;
  width: 100%;
  max-height: 90vh;
  overflow-y: auto;
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
}

.modal-close:hover {
  color: #f56565;
}

.modal-body {
  padding: 20px;
}

.weight-difference {
  margin: 15px 0;
  padding: 10px;
  border-radius: 6px;
  text-align: center;
  font-weight: 600;
}

.weight-difference .positive {
  color: #48bb78;
}

.weight-difference .negative {
  color: #f56565;
}

.nutrition-info {
  margin-top: 20px;
  padding: 15px;
  background: #f7fafc;
  border-radius: 8px;
}

.info-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.info-item:last-child {
  margin-bottom: 0;
}

.estimated-gain {
  color: #48bb78;
  font-weight: 600;
}

.sale-info {
  margin-bottom: 20px;
  padding: 15px;
  background: #f7fafc;
  border-radius: 8px;
}

.profit-summary {
  margin-top: 20px;
  padding: 15px;
  background: #f7fafc;
  border-radius: 8px;
}

.warning-message {
  display: flex;
  gap: 15px;
  padding: 20px;
  background: #fffaf0;
  border-radius: 8px;
  border-left: 4px solid #ed8936;
}

.warning-icon {
  font-size: 32px;
}

.warning-content h4 {
  margin: 0 0 10px 0;
  color: #744210;
}

.warning-content p {
  margin: 0 0 5px 0;
  color: #744210;
}

.confirmation-input {
  margin-top: 20px;
}

.confirmation-input label {
  display: block;
  margin-bottom: 10px;
  color: #4a5568;
  font-weight: 600;
}

.confirmation-input input {
  width: 100%;
  padding: 12px;
  border: 2px solid #e2e8f0;
  border-radius: 6px;
  font-size: 16px;
  text-transform: uppercase;
}

.confirmation-input input:focus {
  outline: none;
  border-color: #f56565;
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
  border-radius: 6px;
  cursor: pointer;
  font-weight: 600;
}

.btn-confirm {
  padding: 10px 20px;
  background: #48bb78;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-weight: 600;
}

.btn-danger {
  padding: 10px 20px;
  background: #f56565;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-weight: 600;
}

.btn-cancel:hover {
  background: #cbd5e0;
}

.btn-confirm:hover {
  background: #38a169;
}

.btn-danger:hover:not(:disabled) {
  background: #e53e3e;
}

.btn-danger:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* Responsive */
@media (max-width: 1024px) {
  .content-container {
    grid-template-columns: 1fr;
  }
  
  .preview-sidebar {
    order: -1;
  }
}

@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    gap: 15px;
  }
  
  .header-right {
    width: 100%;
    justify-content: flex-start;
  }
  
  .form-grid {
    grid-template-columns: 1fr;
  }
  
  .indicators-grid {
    grid-template-columns: 1fr;
  }
  
  .actions-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .stats-grid {
    grid-template-columns: 1fr;
  }
  
  .modal {
    width: 95%;
    margin: 10px;
  }
}
</style>

