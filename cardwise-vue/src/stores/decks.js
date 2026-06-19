import { defineStore } from 'pinia'
import api from '../api'

export const useDecksStore = defineStore('decks', {
  state: () => ({
    decks: [],
    currentDeck: null
  }),
  actions: {
    async fetchDecks() {
      const { data } = await api.get('/decks')
      this.decks = data
    },
    async createDeck(deckData) {
      const { data } = await api.post('/decks', deckData)
      this.decks.unshift(data)
      return data
    },
    async updateDeck(id, deckData) {
      const { data } = await api.put(`/decks/${id}`, deckData)
      const index = this.decks.findIndex(d => d.id === id)
      if (index !== -1) this.decks[index] = data
      if (this.currentDeck?.id === id) this.currentDeck = data
      return data
    },
    async deleteDeck(id) {
      await api.delete(`/decks/${id}`)
      this.decks = this.decks.filter(d => d.id !== id)
    },
    async fetchDeck(id) {
      if (this.decks.length === 0) await this.fetchDecks()
      this.currentDeck = this.decks.find(d => d.id === id) || null
      return this.currentDeck
    }
  }
})
