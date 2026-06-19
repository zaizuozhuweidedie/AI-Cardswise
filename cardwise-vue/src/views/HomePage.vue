<template>
  <div class="min-h-screen bg-white">
    <div class="max-w-3xl mx-auto px-4 py-20 text-center">
      <div class="inline-flex items-center justify-center w-16 h-16 bg-gray-900 text-white text-2xl font-bold rounded-xl mb-8">
        C
      </div>
      <h1 class="text-4xl font-bold text-gray-900 mb-4">CardWise</h1>
      <p class="text-lg text-gray-500 mb-2">AI-Powered Flashcard Learning</p>
      <p class="text-gray-400 mb-10">Paste your study material, let AI create flashcards, and master them with smart spaced repetition.</p>
      <div class="flex justify-center gap-4">
        <router-link to="/auth/register"
          class="px-6 py-3 bg-gray-900 text-white rounded-lg hover:bg-gray-800 font-medium">
          Get Started
        </router-link>
        <router-link to="/auth/login"
          class="px-6 py-3 bg-white border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50 font-medium">
          Sign In
        </router-link>
      </div>
    </div>
  </div>
</template>
