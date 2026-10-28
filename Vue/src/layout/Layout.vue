<script setup>
import { computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

onMounted(() => {
  if (!userStore.userInfo) {
    userStore.fetchUserInfo().catch(() => {})
  }
})

const menus = computed(() => {
  const role = userStore.role
  const list = [{ path: '/task', title: '实训任务' }]
  if (role === 'student') {
    list.push({ path: '/my-submission', title: '我的作业' })
  }
  if (['admin', 'teacher'].includes(role)) {
    list.push({ path: '/review', title: '作业批阅' })
    list.push({ path: '/statistics', title: '成绩统计' })
    list.push({ path: '/user', title: '用户管理' })
  }
  return list
})

const handleLogout = () => {
  userStore.logout()
  router.push('/login')
}
</script>

<template>
  <el-container class="layout">
    <el-aside width="220px" class="aside">
      <div class="logo">实训管理平台</div>
      <el-menu :default-active="route.path" router>
        <el-menu-item v-for="m in menus" :key="m.path" :index="m.path">
          {{ m.title }}
        </el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="header">
        <span>{{ route.meta.title || '' }}</span>
        <el-dropdown @command="handleLogout">
          <span class="user">
            {{ userStore.userInfo?.realName || '用户' }}（{{ userStore.role }}）
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="logout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </el-header>
      <el-main>
        <router-view v-slot="{ Component }">
          <transition name="fade-slide" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<style scoped>
.layout {
  height: 100%;
}
.aside {
  border-right: 1px solid #e4e7ed;
  background: linear-gradient(180deg, #ffffff 0%, #f7f9fc 100%);
}
.aside :deep(.el-menu) {
  border-right: none;
  background: transparent;
}
.aside :deep(.el-menu-item) {
  margin: 4px 10px;
  border-radius: 8px;
  transition: all 0.25s ease;
}
.aside :deep(.el-menu-item:hover) {
  background: #ecf5ff;
}
.aside :deep(.el-menu-item.is-active) {
  background: #409eff;
  color: #fff;
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.35);
}
.logo {
  height: 60px;
  line-height: 60px;
  text-align: center;
  font-weight: 600;
  font-size: 16px;
  background: linear-gradient(90deg, #409eff, #36cfc9);
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
  letter-spacing: 1px;
}
.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid #e4e7ed;
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(8px);
}
.user {
  cursor: pointer;
  color: #606266;
  transition: color 0.2s;
}
.user:hover {
  color: #409eff;
}
</style>
