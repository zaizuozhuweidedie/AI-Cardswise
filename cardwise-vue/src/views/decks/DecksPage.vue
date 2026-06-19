<template>
  <div>
    <div class="flex items-center justify-between mb-6">
      <h1 class="text-2xl font-bold text-gray-900">My Decks</h1>
      <router-link to="/dashboard/decks/new"
        class="px-4 py-2 bg-gray-900 text-white rounded-lg hover:bg-gray-800 text-sm font-medium">
        New Deck
      </router-link>
    </div>

    <div v-if="decksStore.decks.length === 0" class="text-center py-20 text-gray-400">
      <p class="mb-4">No decks yet. Create your first one!</p>
      <router-link to="/dashboard/decks/new"
        class="text-gray-900 font-medium hover:underline">
        Create a deck
      </router-link>
    </div>

    <div v-else class="grid grid-cols-3 gap-4">
      <div v-for="deck in decksStore.decks" :key="deck.id"
        class="border border-gray-200 rounded-xl p-5 hover:border-gray-300 cursor-pointer"
        @click="$router.push(`/dashboard/decks/${deck.id}`)">
        <div class="w-10 h-10 rounded-lg flex items-center justify-center text-white font-bold text-sm mb-3"
          :style="{ backgroundColor: deck.color || '#6366f1' }">
          {{ deck.name.charAt(0).toUpperCase() }}
        </div>
        <h3 class="font-semibold text-gray-900 mb-1">{{ deck.name }}</h3>
        <p v-if="deck.description" class="text-sm text-gray-500 truncate">{{ deck.description }}</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted } from 'vue'
import { useDecksStore } from '../../stores/decks'

const decksStore = useDecksStore()
onMounted(() => decksStore.fetchDecks())
</script>
