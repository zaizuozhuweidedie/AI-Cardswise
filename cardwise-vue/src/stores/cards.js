import { defineStore } from 'pinia'
import api from '../api'

export const useCardsStore = defineStore('cards', {
  state: () => ({
    cards: [],
    dueCards: [],
    loading: false
  }),
  actions: {
    async fetchCards(deckId) {
      const { data } = await api.get(`/decks/${deckId}/cards`)
      this.cards = data
    },
    async fetchDueCards(deckId = null) {
      const params = deckId ? { deckId } : {}
      const { data } = await api.get('/cards/due', { params })
      this.dueCards = data
    },
    async createCard(deckId, cardData) {
      const { data } = await api.post(`/decks/${deckId}/cards`, cardData)
      this.cards.push(data)
      return data
    },
    async updateCard(id, cardData) {
      const { data } = await api.put(`/cards/${id}`, cardData)
      const index = this.cards.findIndex(c => c.id === id)
      if (index !== -1) this.cards[index] = data
      return data
    },
    async deleteCard(id) {
      await api.delete(`/cards/${id}`)
      this.cards = this.cards.filter(c => c.id !== id)
    },
    async reviewCard(id, quality) {
      const { data } = await api.post(`/cards/${id}/review`, { quality })
      return data
    },
    async generateCards(source, sourceType = 'text') {
      this.loading = true
      try {
        const { data } = await api.post('/ai/generate', { source, sourceType })
        return data.cards
      } finally {
        this.loading = false
      }
    }
  }
})
