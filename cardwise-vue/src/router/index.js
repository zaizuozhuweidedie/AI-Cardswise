import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    name: 'Home',
    component: () => import('../views/HomePage.vue')
  },
  {
    path: '/auth/login',
    name: 'Login',
    component: () => import('../views/auth/LoginPage.vue')
  },
  {
    path: '/auth/register',
    name: 'Register',
    component: () => import('../views/auth/RegisterPage.vue')
  },
  {
    path: '/dashboard',
    component: () => import('../views/dashboard/DashboardLayout.vue'),
    meta: { requiresAuth: true },
    children: [
      {
        path: '',
        name: 'Dashboard',
        component: () => import('../views/dashboard/IndexPage.vue')
      },
      {
        path: 'decks',
        name: 'Decks',
        component: () => import('../views/decks/DecksPage.vue')
      },
      {
        path: 'decks/new',
        name: 'NewDeck',
        component: () => import('../views/decks/NewDeckPage.vue')
      },
      {
        path: 'decks/:id',
        name: 'DeckDetail',
        component: () => import('../views/decks/DeckDetailPage.vue'),
        props: true
      },
      {
        path: 'decks/:id/study',
        name: 'DeckStudy',
        component: () => import('../views/study/StudyPage.vue'),
        props: true
      },
      {
        path: 'study',
        name: 'Study',
        component: () => import('../views/study/StudyPage.vue')
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  if (to.meta.requiresAuth && !token) {
    next('/auth/login')
  } else {
    next()
  }
})

export default router
