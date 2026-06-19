import { defineStore } from 'pinia'
import api from '../api'

export const useStatsStore = defineStore('stats', {
  state: () => ({
    totalCards: 0,
    dueCards: 0,
    studiedToday: 0,
    masteredCards: 0,
    dailyActivity: []
  }),
  actions: {
    async fetchStats() {
      const { data } = await api.get('/stats')
      this.totalCards = data.totalCards
      this.dueCards = data.dueCards
      this.studiedToday = data.studiedToday
      this.masteredCards = data.masteredCards
      this.dailyActivity = data.dailyActivity
    }
  }
})
