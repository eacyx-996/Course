<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref()
const loading = ref(false)

const form = reactive({
  username: '',
  password: ''
})

const rules = {
  username: [{ required: true, message: '请输入学号/工号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const handleLogin = async () => {
  await formRef.value.validate()
  loading.value = true
  try {
    await userStore.login(form)
    ElMessage.success('登录成功')
    router.push('/')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="login-page">
    <el-card class="login-card">
      <h2 class="title">课程知识点在线实训管理平台</h2>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="0">
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="学号/工号" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" placeholder="密码"
                    show-password @keyup.enter="handleLogin" />
        </el-form-item>
        <el-button type="primary" class="btn" :loading="loading" @click="handleLogin">
          登 录
        </el-button>
      </el-form>
      <div class="footer">
        <router-link to="/register">学生注册</router-link>
        <span class="tip">默认管理员：admin / admin123</span>
      </div>
    </el-card>
  </div>
</template>

<style scoped>
.login-page {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #1f2d3d 0%, #2b5876 50%, #4e4376 100%);
  position: relative;
  overflow: hidden;
}
/* 背景光斑装饰 */
.login-page::before,
.login-page::after {
  content: '';
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
  opacity: 0.35;
  animation: float 8s ease-in-out infinite;
}
.login-page::before {
  width: 420px;
  height: 420px;
  background: #409eff;
  top: -120px;
  left: -100px;
}
.login-page::after {
  width: 360px;
  height: 360px;
  background: #36cfc9;
  bottom: -120px;
  right: -80px;
  animation-delay: 2s;
}
@keyframes float {
  0%, 100% { transform: translate(0, 0); }
  50% { transform: translate(30px, -30px); }
}
.login-card {
  width: 400px;
  padding: 28px;
  border-radius: 16px;
  border: none;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  position: relative;
  z-index: 1;
  animation: card-in 0.5s ease-out;
}
@keyframes card-in {
  from { opacity: 0; transform: translateY(24px) scale(0.97); }
  to { opacity: 1; transform: translateY(0) scale(1); }
}
.title {
  text-align: center;
  margin-bottom: 28px;
  font-size: 19px;
  font-weight: 600;
  background: linear-gradient(90deg, #409eff, #36cfc9);
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
}
.btn {
  width: 100%;
  border-radius: 8px;
  letter-spacing: 8px;
  transition: all 0.3s ease;
}
.btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 6px 16px rgba(64, 158, 255, 0.4);
}
.footer {
  margin-top: 16px;
  display: flex;
  justify-content: space-between;
  font-size: 13px;
}
.footer a {
  color: #409eff;
  transition: color 0.2s;
}
.footer a:hover {
  color: #66b1ff;
}
.tip {
  color: #909399;
}
</style>
