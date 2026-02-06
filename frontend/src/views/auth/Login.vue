<template>
  <div class="login-page">
    <div class="login-header">
      <h1>Connexion</h1>
      <p>Accédez à votre compte pisciculture</p>
    </div>
    
    <form @submit.prevent="handleLogin" class="login-form">
      <div v-if="errorMessage" class="alert error">
        {{ errorMessage }}
      </div>
      
      <div class="form-group">
        <label for="email">Email</label>
        <input
          type="email"
          id="email"
          v-model="form.email"
          :class="{ 'error': errors.email }"
          placeholder="votre@email.com"
          required
          autocomplete="email"
        />
        <span v-if="errors.email" class="error-message">{{ errors.email }}</span>
      </div>
      
      <div class="form-group">
        <label for="password">Mot de passe</label>
        <div class="password-input">
          <input
            :type="showPassword ? 'text' : 'password'"
            id="password"
            v-model="form.password"
            :class="{ 'error': errors.password }"
            placeholder="Votre mot de passe"
            required
            autocomplete="current-password"
          />
          <button type="button" @click="togglePassword" class="password-toggle">
            {{ showPassword ? '👁️‍🗨️' : '👁️' }}
          </button>
        </div>
        <span v-if="errors.password" class="error-message">{{ errors.password }}</span>
      </div>
      
      <div class="form-options">
        <label class="checkbox-label">
          <input type="checkbox" v-model="form.remember">
          <span class="checkmark"></span>
          <span>Se souvenir de moi</span>
        </label>
        <router-link to="/auth/forgot-password" class="forgot-link">
          Mot de passe oublié ?
        </router-link>
      </div>
      
      <button type="submit" class="btn-login" :disabled="loading">
        <span v-if="loading" class="spinner"></span>
        {{ loading ? 'Connexion...' : 'Se connecter' }}
      </button>
      
      <div class="divider">
        <span>Ou</span>
      </div>
      
      <button type="button" @click="loginWithDemo" class="btn-demo">
        🚀 Accès démo (sans authentification)
      </button>
    </form>
    
    <div class="login-footer">
      <p>
        Nouveau sur Pisciculture ? 
        <router-link to="/auth/register" class="register-link">
          Créer un compte
        </router-link>
      </p>
    </div>
    
    <!-- Demo Info Modal -->
    <div v-if="showDemoModal" class="modal-overlay" @click.self="showDemoModal = false">
      <div class="modal">
        <div class="modal-header">
          <h3>Accès Démo</h3>
          <button @click="showDemoModal = false" class="modal-close">×</button>
        </div>
        <div class="modal-body">
          <div class="demo-warning">
            <div class="warning-icon">⚠️</div>
            <div class="warning-content">
              <h4>Mode démonstration activé</h4>
              <p>Vous accédez à une version de démonstration du système.</p>
              <ul>
                <li>Les données sont pré-remplies</li>
                <li>Les modifications ne sont pas persistantes</li>
                <li>Certaines fonctionnalités peuvent être limitées</li>
              </ul>
            </div>
          </div>
          <div class="demo-credentials">
            <h4>Identifiants de test :</h4>
            <div class="credential-item">
              <span>Email:</span>
              <code>demo@pisciculture.mg</code>
            </div>
            <div class="credential-item">
              <span>Mot de passe:</span>
              <code>demo123</code>
            </div>
          </div>
        </div>
        <div class="modal-footer">
          <button @click="showDemoModal = false" class="btn-cancel">Annuler</button>
          <button @click="confirmDemoLogin" class="btn-confirm">
            Continuer en mode démo
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref } from 'vue'
import { useRouter } from 'vue-router'

export default {
  name: 'Login',
  setup() {
    const router = useRouter()
    
    // État
    const form = ref({
      email: '',
      password: '',
      remember: false
    })
    
    const errors = ref({})
    const errorMessage = ref('')
    const loading = ref(false)
    const showPassword = ref(false)
    const showDemoModal = ref(false)
    
    // Méthodes
    const validateForm = () => {
      errors.value = {}
      
      if (!form.value.email) {
        errors.value.email = 'L\'email est requis'
      } else if (!/\S+@\S+\.\S+/.test(form.value.email)) {
        errors.value.email = 'Email invalide'
      }
      
      if (!form.value.password) {
        errors.value.password = 'Le mot de passe est requis'
      } else if (form.value.password.length < 6) {
        errors.value.password = 'Minimum 6 caractères'
      }
      
      return Object.keys(errors.value).length === 0
    }
    
    const handleLogin = async () => {
      if (!validateForm()) {
        errorMessage.value = 'Veuillez corriger les erreurs'
        return
      }
      
      loading.value = true
      errorMessage.value = ''
      
      try {
        // Simuler l'appel API
        await new Promise(resolve => setTimeout(resolve, 1000))
        
        // Dans un vrai projet, vous appelleriez votre API ici
        // const response = await authService.login(form.value)
        
        // Pour la démo, accepter n'importe quelle combinaison
        const token = 'demo-token-' + Date.now()
        
        // Stocker le token
        if (form.value.remember) {
          localStorage.setItem('token', token)
          localStorage.setItem('user', JSON.stringify({
            email: form.value.email,
            name: 'Utilisateur Démo'
          }))
        } else {
          sessionStorage.setItem('token', token)
          sessionStorage.setItem('user', JSON.stringify({
            email: form.value.email,
            name: 'Utilisateur Démo'
          }))
        }
        
        // Rediriger vers le dashboard
        router.push('/')
        
      } catch (error) {
        console.error('Login error:', error)
        errorMessage.value = 'Email ou mot de passe incorrect'
      } finally {
        loading.value = false
      }
    }
    
    const togglePassword = () => {
      showPassword.value = !showPassword.value
    }
    
    const loginWithDemo = () => {
      form.value.email = 'demo@pisciculture.mg'
      form.value.password = 'demo123'
      showDemoModal.value = true
    }
    
    const confirmDemoLogin = () => {
      showDemoModal.value = false
      handleLogin()
    }
    
    return {
      form,
      errors,
      errorMessage,
      loading,
      showPassword,
      showDemoModal,
      handleLogin,
      togglePassword,
      loginWithDemo,
      confirmDemoLogin
    }
  }
}
</script>

<style scoped>
.login-page {
  padding: 40px;
  width: 100%;
}

.login-header {
  text-align: center;
  margin-bottom: 40px;
}

.login-header h1 {
  margin: 0 0 10px 0;
  color: #2d3748;
  font-size: 32px;
}

.login-header p {
  margin: 0;
  color: #718096;
  font-size: 16px;
}

.login-form {
  max-width: 400px;
  margin: 0 auto;
}

.alert {
  padding: 15px;
  border-radius: 8px;
  margin-bottom: 20px;
  font-size: 14px;
}

.alert.error {
  background: #fed7d7;
  color: #742a2a;
  border: 1px solid #f56565;
}

.form-group {
  margin-bottom: 25px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  font-weight: 600;
  color: #4a5568;
  font-size: 14px;
}

.form-group input {
  width: 100%;
  padding: 14px;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  font-size: 16px;
  transition: all 0.2s;
  box-sizing: border-box;
}

.form-group input:focus {
  outline: none;
  border-color: #4299e1;
  box-shadow: 0 0 0 3px rgba(66, 153, 225, 0.1);
}

.form-group input.error {
  border-color: #f56565;
}

.error-message {
  display: block;
  color: #f56565;
  font-size: 12px;
  margin-top: 5px;
}

.password-input {
  position: relative;
}

.password-toggle {
  position: absolute;
  right: 10px;
  top: 50%;
  transform: translateY(-50%);
  background: none;
  border: none;
  cursor: pointer;
  font-size: 20px;
  color: #718096;
}

.form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
}

.checkbox-label {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  user-select: none;
  font-size: 14px;
  color: #4a5568;
}

.checkbox-label input {
  display: none;
}

.checkbox-label .checkmark {
  width: 18px;
  height: 18px;
  border: 2px solid #cbd5e0;
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
}

.checkbox-label input:checked + .checkmark {
  background: #4299e1;
  border-color: #4299e1;
}

.checkbox-label input:checked + .checkmark::after {
  content: '✓';
  color: white;
  font-size: 12px;
}

.forgot-link {
  color: #4299e1;
  text-decoration: none;
  font-size: 14px;
}

.forgot-link:hover {
  text-decoration: underline;
}

.btn-login {
  width: 100%;
  padding: 15px;
  background: #48bb78;
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.2s;
  margin-bottom: 20px;
}

.btn-login:hover:not(:disabled) {
  background: #38a169;
}

.btn-login:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.spinner {
  display: inline-block;
  width: 16px;
  height: 16px;
  border: 2px solid rgba(255,255,255,0.3);
  border-radius: 50%;
  border-top-color: white;
  animation: spin 1s ease-in-out infinite;
  margin-right: 8px;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.divider {
  display: flex;
  align-items: center;
  margin: 25px 0;
  color: #a0aec0;
}

.divider::before,
.divider::after {
  content: '';
  flex: 1;
  border-bottom: 1px solid #e2e8f0;
}

.divider span {
  padding: 0 15px;
  font-size: 14px;
}

.btn-demo {
  width: 100%;
  padding: 15px;
  background: #ed8936;
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.2s;
}

.btn-demo:hover {
  background: #dd6b20;
}

.login-footer {
  text-align: center;
  margin-top: 40px;
  padding-top: 30px;
  border-top: 1px solid #e2e8f0;
}

.login-footer p {
  color: #718096;
  font-size: 14px;
}

.register-link {
  color: #4299e1;
  text-decoration: none;
  font-weight: 600;
}

.register-link:hover {
  text-decoration: underline;
}

/* Modal styles */
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
  width: 100%;
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

.demo-warning {
  background: #fffaf0;
  border: 1px solid #feebc8;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 20px;
  display: flex;
  gap: 15px;
}

.warning-icon {
  font-size: 32px;
  color: #ed8936;
}

.warning-content h4 {
  margin: 0 0 10px 0;
  color: #dd6b20;
}

.warning-content p {
  margin: 0 0 10px 0;
  color: #744210;
}

.warning-content ul {
  margin: 10px 0 0 0;
  padding-left: 20px;
  color: #744210;
}

.warning-content li {
  margin-bottom: 5px;
}

.demo-credentials {
  background: #f7fafc;
  border-radius: 8px;
  padding: 20px;
}

.demo-credentials h4 {
  margin: 0 0 15px 0;
  color: #2d3748;
}

.credential-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
  padding: 8px 0;
  border-bottom: 1px solid #e2e8f0;
}

.credential-item:last-child {
  border-bottom: none;
  margin-bottom: 0;
}

.credential-item span {
  color: #4a5568;
  font-weight: 600;
}

.credential-item code {
  background: #e2e8f0;
  padding: 4px 8px;
  border-radius: 4px;
  font-family: monospace;
  color: #2d3748;
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

.btn-confirm {
  padding: 10px 20px;
  background: #48bb78;
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-weight: 600;
}

.btn-confirm:hover {
  background: #38a169;
}

@media (max-width: 768px) {
  .login-page {
    padding: 20px;
  }
  
  .login-header h1 {
    font-size: 28px;
  }
  
  .modal {
    margin: 10px;
  }
}

@media (max-width: 480px) {
  .login-form {
    max-width: 100%;
  }
  
  .form-options {
    flex-direction: column;
    align-items: flex-start;
    gap: 15px;
  }
  
  .forgot-link {
    align-self: flex-start;
  }
  
  .modal-body {
    padding: 15px;
  }
  
  .modal-footer {
    flex-direction: column;
  }
  
  .modal-footer button {
    width: 100%;
  }
}
</style>

