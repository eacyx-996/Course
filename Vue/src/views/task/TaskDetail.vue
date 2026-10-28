<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getTaskDetail } from '@/api/task'
import { submitHomework } from '@/api/submission'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const userStore = useUserStore()
const taskId = route.params.id

const task = ref(null)
const loadDetail = async () => {
  const { data } = await getTaskDetail(taskId)
  task.value = data
}
onMounted(loadDetail)

// ---- 学生提交作业 ----
const submitForm = reactive({ content: '', file: null })
const submitting = ref(false)

const handleFileChange = (uploadFile) => {
  submitForm.file = uploadFile.raw
}

const handleSubmit = async () => {
  const formData = new FormData()
  formData.append('taskId', taskId)
  formData.append('content', submitForm.content)
  if (submitForm.file) {
    formData.append('file', submitForm.file)
  }
  submitting.value = true
  try {
    await submitHomework(formData)
    ElMessage.success('提交成功')
    submitForm.content = ''
    submitForm.file = null
    loadDetail()
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <div v-if="task">
    <el-card>
      <template #header>
        <div class="head">
          <span class="title">{{ task.title }}</span>
          <el-tag v-if="task.submitted" type="success">已提交</el-tag>
        </div>
      </template>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="知识点">{{ task.knowledgePoint || '-' }}</el-descriptions-item>
        <el-descriptions-item label="发布人">{{ task.publisherName }}</el-descriptions-item>
        <el-descriptions-item label="截止时间">{{ task.deadline }}</el-descriptions-item>
        <el-descriptions-item label="发布时间">{{ task.createTime }}</el-descriptions-item>
      </el-descriptions>
      <div class="content">{{ task.content }}</div>
    </el-card>

    <el-card v-if="userStore.role === 'student'" class="submit-card">
      <template #header>提交作业</template>
      <el-input v-model="submitForm.content" type="textarea" :rows="5"
                placeholder="作业文本内容" />
      <div class="upload-row">
        <el-upload :auto-upload="false" :limit="1" :on-change="handleFileChange">
          <el-button>选择附件</el-button>
          <template #tip>
            <span class="tip">≤10MB，支持 zip/rar/pdf/doc/docx/md</span>
          </template>
        </el-upload>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">
          提交作业
        </el-button>
      </div>
    </el-card>
  </div>
</template>

<style scoped>
.head {
  display: flex;
  align-items: center;
  gap: 12px;
}
.title {
  font-size: 18px;
  font-weight: 600;
}
.content {
  margin-top: 16px;
  line-height: 1.8;
  white-space: pre-wrap;
}
.submit-card {
  margin-top: 16px;
}
.upload-row {
  margin-top: 12px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.tip {
  margin-left: 8px;
  color: #909399;
  font-size: 12px;
}
</style>
