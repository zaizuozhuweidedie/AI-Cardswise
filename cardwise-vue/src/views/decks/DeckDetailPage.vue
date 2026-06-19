<template>
  <div v-if="deck">
    <div class="flex items-center justify-between mb-6">
      <div class="flex items-center gap-3">
        <div class="w-10 h-10 rounded-lg flex items-center justify-center text-white font-bold text-sm"
          :style="{ backgroundColor: deck.color }">
          {{ deck.name.charAt(0).toUpperCase() }}
        </div>
        <div>
          <h1 class="text-2xl font-bold text-gray-900">{{ deck.name }}</h1>
          <p v-if="deck.description" class="text-sm text-gray-500">{{ deck.description }}</p>
        </div>
      </div>
      <div class="flex gap-2">
        <router-link :to="`/dashboard/decks/${deck.id}/study`"
          class="px-4 py-2 bg-gray-900 text-white rounded-lg hover:bg-gray-800 text-sm font-medium">
          Study
        </router-link>
      </div>
    </div>

    <div class="mb-8">
      <AiGeneratePanel :deck-id="deck.id" @saved="refreshCards" />
    </div>

    <div class="border border-gray-200 rounded-xl p-5 mb-8">
      <h3 class="font-semibold text-gray-900 mb-3">Add Card Manually</h3>
      <div class="grid grid-cols-2 gap-4 mb-3">
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">Front</label>
          <input v-model="newFront"
            class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-gray-900" />
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">Back</label>
          <input v-model="newBack"
            class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-gray-900" />
        </div>
      </div>
      <button @click="handleAddCard" :disabled="!newFront || !newBack"
        class="px-4 py-2 border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50 disabled:opacity-50 text-sm font-medium">
        Add Card
      </button>
    </div>

    <div class="border border-gray-200 rounded-xl p-5">
      <h3 class="font-semibold text-gray-900 mb-3">Cards ({{ cards.length }})</h3>
      <div v-if="cards.length === 0" class="text-gray-400 text-sm py-8 text-center">
        No cards yet. Generate with AI or add manually.
      </div>
      <div v-else class="space-y-2">
        <div v-for="card in cards" :key="card.id"
          class="flex items-center justify-between p-3 border border-gray-100 rounded-lg">
          <div class="flex-1 min-w-0">
            <p class="text-sm font-medium text-gray-900 truncate">{{ card.front }}</p>
            <p class="text-sm text-gray-500 truncate">{{ card.back }}</p>
          </div>
          <button @click="handleDeleteCard(card.id)"
            class="text-sm text-red-500 hover:text-red-700 ml-4">Delete</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { useDecksStore } from '../../stores/decks'
import { useCardsStore } from '../../stores/cards'
import AiGeneratePanel from './components/AiGeneratePanel.vue'

const route = useRoute()
const decksStore = useDecksStore()
const cardsStore = useCardsStore()

const deck = ref(null)
const cards = ref([])
const newFront = ref('')
const newBack = ref('')

onMounted(async () => {
  deck.value = await decksStore.fetchDeck(route.params.id)
  await refreshCards()
})

async function refreshCards() {
  await cardsStore.fetchCards(route.params.id)
  cards.value = cardsStore.cards
}

async function handleAddCard() {
  await cardsStore.createCard(route.params.id, {
    front: newFront.value,
    back: newBack.value,
    tags: ''
  })
  newFront.value = ''
  newBack.value = ''
  await refreshCards()
}

async function handleDeleteCard(id) {
  await cardsStore.deleteCard(id)
  await refreshCards()
}
</script>
