<template>
  <div class="border border-gray-200 rounded-xl p-5">
    <p class="text-sm text-gray-500 mb-1">{{ label }}</p>
    <p class="text-3xl font-bold text-gray-900">{{ value }}</p>
  </div>
</template>

<script setup>
defineProps({
  label: String,
  value: [Number, String]
})
</script>
