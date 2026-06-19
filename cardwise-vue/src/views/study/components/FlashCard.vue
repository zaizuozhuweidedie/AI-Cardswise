<template>
  <div class="perspective-container" @click="flip">
    <div class="flipper" :class="{ flipped: isFlipped }">
      <div class="front rounded-xl flex items-center justify-center p-8 cursor-pointer"
        :style="{ backgroundColor: color || '#6366f1' }">
        <p class="text-white text-xl text-center leading-relaxed">{{ front }}</p>
      </div>
      <div class="back rounded-xl flex flex-col items-center justify-center p-8 cursor-pointer bg-white border border-gray-200">
        <p class="text-gray-900 text-xl text-center leading-relaxed mb-8">{{ back }}</p>
        <div v-if="isFlipped" class="flex gap-3" @click.stop>
          <button @click="$emit('rate', 1)"
            class="px-5 py-2 rounded-lg text-white text-sm font-medium bg-red-500 hover:bg-red-600">
            Again
          </button>
          <button @click="$emit('rate', 2)"
            class="px-5 py-2 rounded-lg text-white text-sm font-medium bg-orange-500 hover:bg-orange-600">
            Hard
          </button>
          <button @click="$emit('rate', 3)"
            class="px-5 py-2 rounded-lg text-white text-sm font-medium bg-green-500 hover:bg-green-600">
            Good
          </button>
          <button @click="$emit('rate', 4)"
            class="px-5 py-2 rounded-lg text-white text-sm font-medium bg-blue-500 hover:bg-blue-600">
            Easy
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const props = defineProps({
  front: String,
  back: String,
  color: String
})

defineEmits(['rate'])

const isFlipped = ref(false)

function flip() {
  isFlipped.value = !isFlipped.value
}
</script>

<style scoped>
.perspective-container {
  perspective: 1000px;
  width: 100%;
  max-width: 480px;
  height: 320px;
  margin: 0 auto;
}
.flipper {
  position: relative;
  width: 100%;
  height: 100%;
  transition: transform 0.5s;
  transform-style: preserve-3d;
}
.flipper.flipped {
  transform: rotateY(180deg);
}
.front, .back {
  position: absolute;
  width: 100%;
  height: 100%;
  backface-visibility: hidden;
}
.back {
  transform: rotateY(180deg);
}
</style>
