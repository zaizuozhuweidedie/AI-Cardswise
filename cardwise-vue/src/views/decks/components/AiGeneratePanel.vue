<template>
  <div class="border border-gray-200 rounded-xl p-5">
    <h3 class="font-semibold text-gray-900 mb-3">AI Generate Cards</h3>
    <textarea v-model="source" placeholder="Paste your study material here..."
      rows="5"
      class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-gray-900 mb-3"></textarea>
    <button @click="handleGenerate" :disabled="loading || !source.trim()"
      class="px-4 py-2 bg-gray-900 text-white rounded-lg hover:bg-gray-800 disabled:opacity-50 text-sm font-medium">
      {{ loading ? 'Generating...' : 'Generate Cards' }}
    </button>

    <div v-if="generatedCards.length > 0" class="mt-4 space-y-3">
      <h4 class="text-sm font-medium text-gray-700">Preview ({{ generatedCards.length }} cards)</h4>
      <div v-for="(card, index) in generatedCards" :key="index"
        class="border border-gray-200 rounded-lg p-3">
        <div class="mb-2">
          <label class="text-xs text-gray-400">Front</label>
          <input v-model="card.front"
            class="w-full text-sm text-gray-900 border border-gray-200 rounded px-2 py-1 mt-1" />
        </div>
        <div>
          <label class="text-xs text-gray-400">Back</label>
          <input v-model="card.back"
            class="w-full text-sm text-gray-900 border border-gray-200 rounded px-2 py-1 mt-1" />
        </div>
      </div>
      <button @click="handleSaveAll"
        class="px-4 py-2 bg-green-600 text-white rounded-lg hover:bg-green-700 text-sm font-medium">
        Save All to Deck
      </button>
    </div>

    <p v-if="error" class="text-red-500 text-sm mt-3">{{ error }}</p>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useCardsStore } from '../../../stores/cards'

const props = defineProps({
  deckId: { type: String, required: true }
})
const emit = defineEmits(['saved'])

const cardsStore = useCardsStore()
const source = ref('')
const generatedCards = ref([])
const loading = ref(false)
const error = ref('')

async function handleGenerate() {
  error.value = ''
  try {
    generatedCards.value = await cardsStore.generateCards(source.value)
  } catch (e) {
    error.value = e.response?.data?.error || 'Generation failed'
  }
}

async function handleSaveAll() {
  for (const card of generatedCards.value) {
    await cardsStore.createCard(props.deckId, {
      front: card.front,
      back: card.back,
      tags: ''
    })
  }
  generatedCards.value = []
  source.value = ''
  emit('saved')
}
</script>
