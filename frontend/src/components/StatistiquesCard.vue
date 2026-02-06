<template>
  <div class="stat-card" :class="color">
    <div class="stat-icon">{{ icon }}</div>
    <div class="stat-content">
      <h3>{{ title }}</h3>
      <p class="stat-value">
        {{ formatValue }}
        <span v-if="suffix" class="stat-suffix">{{ suffix }}</span>
      </p>
      <div v-if="trend" class="stat-trend" :class="trendClass">
        {{ trend }}
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'StatistiquesCard',
  props: {
    title: {
      type: String,
      required: true
    },
    value: {
      type: [Number, String],
      default: 0
    },
    icon: {
      type: String,
      default: '📊'
    },
    color: {
      type: String,
      default: 'primary'
    },
    suffix: {
      type: String,
      default: ''
    },
    format: {
      type: Boolean,
      default: false
    },
    trend: {
      type: String,
      default: ''
    },
    trendType: {
      type: String,
      default: 'neutral' // positive, negative, neutral
    }
  },
  computed: {
    formatValue() {
      if (this.format && typeof this.value === 'number') {
        return new Intl.NumberFormat('fr-MG', {
          minimumFractionDigits: 0,
          maximumFractionDigits: 2
        }).format(this.value)
      }
      return this.value
    },
    trendClass() {
      return `trend-${this.trendType}`
    }
  }
}
</script>

<style scoped>
.stat-card {
  background: white;
  padding: 20px;
  border-radius: 12px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.05);
  display: flex;
  align-items: center;
  gap: 15px;
  transition: transform 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-5px);
}

.stat-card.primary {
  border-left: 4px solid #4299e1;
}

.stat-card.warning {
  border-left: 4px solid #ed8936;
}

.stat-card.success {
  border-left: 4px solid #48bb78;
}

.stat-card.info {
  border-left: 4px solid #38b2ac;
}

.stat-card.purple {
  border-left: 4px solid #9f7aea;
}

.stat-card.orange {
  border-left: 4px solid #ed8936;
}

.stat-icon {
  font-size: 32px;
  width: 60px;
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 10px;
  background: #f7fafc;
}

.stat-card.primary .stat-icon {
  background: #ebf8ff;
  color: #4299e1;
}

.stat-card.warning .stat-icon {
  background: #fffaf0;
  color: #ed8936;
}

.stat-card.success .stat-icon {
  background: #f0fff4;
  color: #48bb78;
}

.stat-content {
  flex: 1;
}

.stat-content h3 {
  margin: 0 0 8px 0;
  font-size: 14px;
  color: #718096;
  font-weight: 600;
}

.stat-value {
  margin: 0;
  font-size: 28px;
  font-weight: bold;
  color: #2d3748;
  line-height: 1;
}

.stat-suffix {
  font-size: 16px;
  color: #718096;
  margin-left: 4px;
}

.stat-trend {
  margin-top: 8px;
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 4px;
  display: inline-block;
}

.trend-positive {
  background: #c6f6d5;
  color: #22543d;
}

.trend-negative {
  background: #fed7d7;
  color: #742a2a;
}

.trend-neutral {
  background: #e2e8f0;
  color: #4a5568;
}
</style>

