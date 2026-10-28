<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { createTask, deleteTask, pageTasks, updateTask } from '@/api/task'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const query = reactive({ pageNum: 1, pageSize: 10, keyword: '', status: null })
const tableData = ref([])
const total = ref(0)
const loading = ref(false)

const loadData = async () => {
  loading.value = true
  try {
    const { data } = await pageTasks(query)
    tableData.value = data.list
    total.value = data.total
  } finally {
    loading.value = false
  }
}

onMounted(loadData)

// ---- 发布/编辑弹窗 ----
const dialogVisible = ref(false)
const dialogTitle = ref('发布任务')
const formRef = ref()
const classOptions = ['软件2301', '软件2302', '软件2303']
const form = reactive({
  id: null,
  title: '',
  knowledgePoint: '',
  content: '',
  className: null,
  deadline: '',
  status: 1
})
// 编辑时记录原截止时间：未改动则允许保留（任务可能已截止）
const originalDeadline = ref('')

// 日期面板禁用今天之前的日期
const disabledDate = (time) => {
  return time.getTime() < new Date().setHours(0, 0, 0, 0)
}

// 截止时间校验：必填且必须晚于当前时间
const validateDeadline = (_rule, value, callback) => {
  if (!value) {
    return callback(new Error('请选择截止时间'))
  }
  if (form.id && value === originalDeadline.value) {
    return callback()
  }
  const picked = new Date(value.replace(/-/g, '/')).getTime()
  if (picked <= Date.now()) {
    return callback(new Error('截止时间必须晚于当前时间'))
  }
  callback()
}

const openCreate = () => {
  dialogTitle.value = '发布任务'
  Object.assign(form, { id: null, title: '', knowledgePoint: '', content: '', className: null, deadline: '', status: 1 })
  originalDeadline.value = ''
  dialogVisible.value = true
}

const openEdit = (row) => {
  dialogTitle.value = '编辑任务'
  Object.assign(form, row)
  originalDeadline.value = row.deadline
  dialogVisible.value = true
}

const handleSave = async () => {
  await formRef.value.validate()
  if (form.id) {
    await updateTask(form.id, form)
    ElMessage.success('修改成功')
  } else {
    await createTask(form)
    ElMessage.success('发布成功')
  }
  dialogVisible.value = false
  loadData()
}

const handleDelete = (row) => {
  ElMessageBox.confirm(`确认删除任务「${row.title}」？`, '提示', { type: 'warning' })
    .then(async () => {
      await deleteTask(row.id)
      ElMessage.success('删除成功')
      loadData()
    })
}

const statusMap = { 0: '草稿', 1: '进行中', 2: '已截止' }
</script>

<template>
  <div>
    <div class="toolbar">
      <el-input v-model="query.keyword" placeholder="按标题搜索" clearable
                style="width: 220px" @keyup.enter="loadData" />
      <el-select v-if="userStore.isTeacherOrAdmin" v-model="query.status"
                 placeholder="全部状态" clearable style="width: 120px">
        <el-option label="全部" :value="null" />
        <el-option label="草稿" :value="0" />
        <el-option label="进行中" :value="1" />
        <el-option label="已截止" :value="2" />
      </el-select>
      <el-button type="primary" @click="loadData">查询</el-button>
      <el-button v-if="userStore.isTeacherOrAdmin" type="success" @click="openCreate">
        发布任务
      </el-button>
    </div>

    <el-table v-loading="loading" :data="tableData" border>
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="title" label="任务标题" show-overflow-tooltip />
      <el-table-column prop="knowledgePoint" label="知识点" width="140" />
      <el-table-column prop="publisherName" label="发布人" width="100" />
      <el-table-column label="目标班级" width="110">
        <template #default="{ row }">
          <el-tag v-if="row.className" type="warning" effect="plain">{{ row.className }}</el-tag>
          <span v-else>全部班级</span>
        </template>
      </el-table-column>
      <el-table-column prop="deadline" label="截止时间" width="170" />
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'">
            {{ statusMap[row.status] ?? row.status }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200">
        <template #default="{ row }">
          <el-button link type="primary" @click="router.push(`/task/${row.id}`)">查看</el-button>
          <template v-if="userStore.isTeacherOrAdmin">
            <el-button link type="warning" @click="openEdit(row)">编辑</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination v-model:current-page="query.pageNum" v-model:page-size="query.pageSize"
                   :total="total" layout="total, prev, pager, next"
                   class="pager" @current-change="loadData" />

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px">
      <el-form ref="formRef" :model="form" label-width="90px">
        <el-form-item label="标题" prop="title" :rules="[{ required: true, message: '请输入标题' }]">
          <el-input v-model="form.title" maxlength="100" show-word-limit />
        </el-form-item>
        <el-form-item label="知识点">
          <el-input v-model="form.knowledgePoint" />
        </el-form-item>
        <el-form-item label="任务内容">
          <el-input v-model="form.content" type="textarea" :rows="5" />
        </el-form-item>
        <el-form-item label="目标班级">
          <el-select v-model="form.className" placeholder="不选 = 全部班级" clearable
                     filterable allow-create style="width: 100%">
            <el-option v-for="c in classOptions" :key="c" :label="c" :value="c" />
          </el-select>
        </el-form-item>
        <el-form-item label="截止时间" prop="deadline"
                      :rules="[{ validator: validateDeadline, trigger: 'change' }]">
          <el-date-picker v-model="form.deadline" type="datetime"
                          :disabled-date="disabledDate"
                          placeholder="请选择晚于当前的时间"
                          value-format="YYYY-MM-DD HH:mm:ss" style="width: 100%" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :value="0">保存草稿</el-radio>
            <el-radio :value="1">立即发布</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.toolbar {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
}
.pager {
  margin-top: 16px;
  justify-content: flex-end;
}
</style>
