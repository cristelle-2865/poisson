<template>
  <div class="auth-layout">
    <div class="auth-container">
      <div class="auth-header">
        <router-link to="/" class="auth-logo">
          <span class="logo-icon">🐟</span>
          <span class="logo-text">Pisciculture</span>
        </router-link>
      </div>
      
      <div class="auth-content">
        <div class="auth-card">
          <router-view />
        </div>
      </div>
      
      <div class="auth-footer">
        <p>© 2024 Système Pisciculture. Tous droits réservés.</p>
        <p class="footer-links">
          <router-link to="/terms">Conditions d'utilisation</router-link>
          <span>|</span>
          <router-link to="/privacy">Politique de confidentialité</router-link>
          <span>|</span>
          <router-link to="/contact">Contact</router-link>
        </p>
      </div>
    </div>
    
    <div class="auth-background">
      <div class="background-pattern"></div>
      <div class="background-overlay"></div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'AuthLayout'
}
</script>

<style scoped>
.auth-layout {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  position: relative;
  overflow: hidden;
}

.auth-background {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 0;
}

.background-pattern {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-image: url("data:image/svg+xml,%3Csvg width='60' height='60' viewBox='0 0 60 60' xmlns='http://www.w3.org/2000/svg'%3E%3Cg fill='none' fill-rule='evenodd'%3E%3Cg fill='%23ffffff' fill-opacity='0.05'%3E%3Cpath d='M36 34v-4h-2v4h-4v2h4v4h2v-4h4v-2h-4zm0-30V0h-2v4h-4v2h4v4h2V6h4V4h-4zM6 34v-4H4v4H0v2h4v4h2v-4h4v-2H6zM6 4V0H4v4H0v2h4v4h2V6h4V4H6z'/%3E%3C/g%3E%3C/g%3E%3C/svg%3E");
  opacity: 0.3;
}

.background-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(to right, rgba(102, 126, 234, 0.8), rgba(118, 75, 162, 0.8));
}

.auth-container {
  position: relative;
  z-index: 1;
  width: 100%;
  max-width: 1200px;
  padding: 20px;
}

.auth-header {
  text-align: center;
  margin-bottom: 40px;
}

.auth-logo {
  display: inline-flex;
  align-items: center;
  gap: 15px;
  text-decoration: none;
  color: white;
  font-size: 32px;
  font-weight: bold;
}

.logo-icon {
  font-size: 48px;
}

.logo-text {
  text-shadow: 0 2px 4px rgba(0,0,0,0.2);
}

.auth-content {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 500px;
}

.auth-card {
  background: white;
  border-radius: 20px;
  box-shadow: 0 20px 40px rgba(0,0,0,0.1);
  width: 100%;
  max-width: 450px;
  overflow: hidden;
  animation: slideUp 0.5s ease;
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.auth-footer {
  text-align: center;
  color: white;
  margin-top: 40px;
  font-size: 14px;
}

.auth-footer p {
  margin: 10px 0;
}

.footer-links {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 15px;
  flex-wrap: wrap;
}

.footer-links a {
  color: rgba(255,255,255,0.8);
  text-decoration: none;
  transition: color 0.2s;
}

.footer-links a:hover {
  color: white;
  text-decoration: underline;
}

.footer-links span {
  color: rgba(255,255,255,0.5);
}

@media (max-width: 768px) {
  .auth-logo {
    font-size: 24px;
  }
  
  .logo-icon {
    font-size: 36px;
  }
  
  .auth-card {
    max-width: 95%;
  }
  
  .auth-footer {
    font-size: 12px;
  }
  
  .footer-links {
    gap: 10px;
  }
}

@media (max-width: 480px) {
  .auth-container {
    padding: 15px;
  }
  
  .auth-header {
    margin-bottom: 20px;
  }
  
  .auth-content {
    min-height: 400px;
  }
  
  .footer-links {
    flex-direction: column;
    gap: 5px;
  }
  
  .footer-links span {
    display: none;
  }
}
</style>

