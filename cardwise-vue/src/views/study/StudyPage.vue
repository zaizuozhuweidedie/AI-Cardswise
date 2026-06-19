<template>
  <div>
    <h1 class="text-2xl font-bold text-gray-900 mb-6">
      {{ deckId ? 'Study: ' + (deck?.name || '') : 'Study' }}
    </h1>

    <div v-if="dueCards.length === 0" class="text-center py-20 text-gray-400">
      <p class="text-lg mb-2">All caught up!</p>
      <p class="text-sm">No cards due for review.</p>
    </div>

    <div v-else-if="currentIndex < dueCards.length" class="flex flex-col items-center">
      <p class="text-sm text-gray-400 mb-4">{{ currentIndex + 1 }} / {{ dueCards.length }}</p>
      <FlashCard
        :front="currentCard.front"
        :back="currentCard.back"
        :color="currentDeckColor"
        @rate="handleRate" />
    </div>

    <div v-else class="text-center py-10">
      <h2 class="text-xl font-bold text-gray-900 mb-4">Session Complete!</h2>
      <p class="text-gray-500 mb-2">Reviewed {{ dueCards.length }} cards</p>
      <div class="flex justify-center gap-6 mb-8 text-sm">
        <span class="text-red-500">Again: {{ ratingCounts[1] || 0 }}</span>
        <span class="text-orange-500">Hard: {{ ratingCounts[2] || 0 }}</span>
        <span class="text-green-500">Good: {{ ratingCounts[3] || 0 }}</span>
        <span class="text-blue-500">Easy: {{ ratingCounts[4] || 0 }}</span>
      </div>
      <router-link to="/dashboard"
        class="px-4 py-2 bg-gray-900 text-white rounded-lg hover:bg-gray-800 text-sm font-medium">
        Back to Dashboard
      </router-link>
    </div>

    <p v-if="error" class="text-red-500 text-sm mt-4 text-center">{{ error }}</p>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { useCardsStore } from '../../stores/cards'
import { useDecksStore } from '../../stores/decks'
import FlashCard from './components/FlashCard.vue'

const route = useRoute()
const cardsStore = useCardsStore()
const decksStore = useDecksStore()

const deckId = computed(() => route.params.id || null)
const deck = ref(null)
const dueCards = ref([])
const currentIndex = ref(0)
const ratingCounts = ref({})
const error = ref('')

const currentCard = computed(() => dueCards.value[currentIndex.value] || {})

const currentDeckColor = computed(() => {
  if (!currentCard.value.deckId) return '#6366f1'
  if (deck.value?.id === currentCard.value.deckId) return deck.value.color
  const d = decksStore.decks.find(d => d.id === currentCard.value.deckId)
  return d?.color || '#6366f1'
})

onMounted(async () => {
  try {
    await cardsStore.fetchDueCards(deckId.value)
    dueCards.value = [...cardsStore.dueCards]
    if (deckId.value) {
      deck.value = await decksStore.fetchDeck(deckId.value)
    }
  } catch (e) {
    error.value = 'Failed to load cards'
  }
})

async function handleRate(quality) {
  const card = currentCard.value
  if (!card?.id) return
  try {
    await cardsStore.reviewCard(card.id, quality)
    ratingCounts.value[quality] = (ratingCounts.value[quality] || 0) + 1
    currentIndex.value++
  } catch (e) {
    error.value = 'Failed to save review'
  }
}
</script>
