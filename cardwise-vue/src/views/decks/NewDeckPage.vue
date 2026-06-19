<template>
  <div class="max-w-lg">
    <h1 class="text-2xl font-bold text-gray-900 mb-6">New Deck</h1>
    <form @submit.prevent="handleCreate" class="border border-gray-200 rounded-xl p-6 space-y-4">
      <div>
        <label class="block text-sm font-medium text-gray-700 mb-1">Name</label>
        <input v-model="name" required
          class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-gray-900" />
      </div>
      <div>
        <label class="block text-sm font-medium text-gray-700 mb-1">Description (optional)</label>
        <textarea v-model="description" rows="3"
          class="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-gray-900"></textarea>
      </div>
      <div>
        <label class="block text-sm font-medium text-gray-700 mb-1">Color</label>
        <input v-model="color" type="color"
          class="w-10 h-10 p-0 border border-gray-300 rounded cursor-pointer" />
        <span class="ml-2 text-sm text-gray-500">{{ color }}</span>
      </div>
      <div class="flex gap-3 pt-2">
        <button type="submit"
          class="px-4 py-2 bg-gray-900 text-white rounded-lg hover:bg-gray-800 text-sm font-medium">
          Create
        </button>
        <router-link to="/dashboard/decks"
          class="px-4 py-2 border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50 text-sm font-medium">
          Cancel
        </router-link>
      </div>
    </form>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useDecksStore } from '../../stores/decks'

const router = useRouter()
const decksStore = useDecksStore()

const name = ref('')
const description = ref('')
const color = ref('#6366f1')

async function handleCreate() {
  await decksStore.createDeck({ name: name.value, description: description.value, color: color.value })
  router.push('/dashboard/decks')
}
</script>
