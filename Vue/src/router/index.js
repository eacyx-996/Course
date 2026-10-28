import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/login',
    component: () => import('@/views/Login.vue')
  },
  {
    path: '/register',
    component: () => import('@/views/Register.vue')
  },
  {
    path: '/',
    component: () => import('@/layout/Layout.vue'),
    redirect: '/task',
    children: [
      { path: 'task', component: () => import('@/views/task/TaskList.vue'), meta: { title: '实训任务' } },
      { path: 'task/:id', component: () => import('@/views/task/TaskDetail.vue'), meta: { title: '任务详情' } },
      { path: 'my-submission', component: () => import('@/views/submission/MySubmission.vue'), meta: { title: '我的作业', roles: ['student'] } },
      { path: 'review', component: () => import('@/views/review/SubmissionManage.vue'), meta: { title: '作业批阅', roles: ['admin', 'teacher'] } },
      { path: 'statistics', component: () => import('@/views/review/Statistics.vue'), meta: { title: '成绩统计', roles: ['admin', 'teacher'] } },
      { path: 'user', component: () => import('@/views/admin/UserManage.vue'), meta: { title: '用户管理', roles: ['admin', 'teacher'] } }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 登录与角色守卫
router.beforeEach((to) => {
  const token = localStorage.getItem('token')
  if (!token && !['/login', '/register'].includes(to.path)) {
    return '/login'
  }
  if (token && to.path === '/login') {
    return '/'
  }
  return true
})

export default router
