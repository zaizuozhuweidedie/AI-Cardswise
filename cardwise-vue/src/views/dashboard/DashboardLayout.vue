<template>
  <div class="min-h-screen bg-white flex">
    <aside class="w-64 border-r border-gray-200 flex flex-col">
      <div class="p-6">
        <router-link to="/dashboard" class="flex items-center gap-2">
          <div class="w-8 h-8 bg-gray-900 text-white text-sm font-bold rounded-lg flex items-center justify-center">C</div>
          <span class="text-lg font-bold text-gray-900">CardWise</span>
        </router-link>
      </div>
      <nav class="flex-1 px-4 space-y-1">
        <router-link to="/dashboard"
          class="flex items-center gap-3 px-3 py-2 rounded-lg text-gray-600 hover:bg-gray-100"
          :class="{ 'bg-gray-100 text-gray-900 font-medium': $route.path === '/dashboard' }">
          <span>Dashboard</span>
        </router-link>
        <router-link to="/dashboard/decks"
          class="flex items-center gap-3 px-3 py-2 rounded-lg text-gray-600 hover:bg-gray-100"
          :class="{ 'bg-gray-100 text-gray-900 font-medium': $route.path.startsWith('/dashboard/decks') }">
          <span>My Decks</span>
        </router-link>
        <router-link to="/dashboard/study"
          class="flex items-center gap-3 px-3 py-2 rounded-lg text-gray-600 hover:bg-gray-100"
          :class="{ 'bg-gray-100 text-gray-900 font-medium': $route.path === '/dashboard/study' }">
          <span>Study</span>
        </router-link>
      </nav>
    </aside>

    <div class="flex-1 flex flex-col">
      <header class="h-16 border-b border-gray-200 flex items-center justify-end px-6 gap-4">
        <span class="text-sm text-gray-500">{{ auth.userEmail }}</span>
        <button @click="handleLogout" class="text-sm text-gray-500 hover:text-gray-700">Sign out</button>
      </header>
      <main class="flex-1 p-6">
        <router-view />
      </main>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { useAuthStore } from '../../stores/auth'

const router = useRouter()
const auth = useAuthStore()

function handleLogout() {
  auth.logout()
  router.push('/')
}
</script>
