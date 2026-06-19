<template>
  <div>
    <h1 class="text-2xl font-bold text-gray-900 mb-6">Dashboard</h1>

    <div class="grid grid-cols-4 gap-4 mb-8">
      <StatCard label="Total Cards" :value="stats.totalCards" />
      <StatCard label="Due for Review" :value="stats.dueCards" />
      <StatCard label="Studied Today" :value="stats.studiedToday" />
      <StatCard label="Mastered" :value="stats.masteredCards" />
    </div>

    <div class="border border-gray-200 rounded-xl p-6">
      <h2 class="text-lg font-semibold text-gray-900 mb-4">Cards Due for Review</h2>
      <div v-if="dueCards.length === 0" class="text-gray-400 text-sm py-8 text-center">
        No cards due for review. Great job!
      </div>
      <div v-else class="space-y-2">
        <div v-for="card in dueCards" :key="card.id"
          class="flex items-center justify-between p-3 border border-gray-100 rounded-lg">
          <p class="text-sm text-gray-700 truncate flex-1">{{ card.front }}</p>
          <router-link :to="`/dashboard/decks/${card.deckId}/study`"
            class="text-sm text-gray-900 font-medium hover:underline ml-4">
            Study
          </router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted } from 'vue'
import { useStatsStore } from '../../stores/stats'
import { useCardsStore } from '../../stores/cards'
import StatCard from './components/StatCard.vue'

const stats = useStatsStore()
const cardsStore = useCardsStore()

onMounted(async () => {
  await Promise.all([
    stats.fetchStats(),
    cardsStore.fetchDueCards()
  ])
})

const dueCards = cardsStore.dueCards
</script>
