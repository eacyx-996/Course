<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { pageTasks } from '@/api/task'
import { pageSubmissions } from '@/api/submission'
import { reviewSubmission } from '@/api/review'

// 先选任务,再看该任务的提交列表
const taskOptions = ref([])
const classOptions = ['软件2301', '软件2302', '软件2303']
const query = reactive({ taskId: null, status: null, className: '', pageNum: 1, pageSize: 10 })
const tableData = ref([])
const total = ref(0)
const loading = ref(false)

const loadTasks = async () => {
  const { data } = await pageTasks({ pageNum: 1, pageSize: 100 })
  taskOptions.value = data.list
}

const loadData = async () => {
  if (!query.taskId) return
  loading.value = true
  try {
    const { data } = await pageSubmissions(query)
    tableData.value = data.list
    total.value = data.total
  } finally {
    loading.value = false
  }
}

onMounted(loadTasks)

// ---- 批阅弹窗 ----
const dialogVisible = ref(false)
const reviewForm = reactive({ submissionId: null, score: null, comment: '' })

const openReview = (row) => {
  Object.assign(reviewForm, { submissionId: row.id ?? row.submissionId, score: row.score, comment: row.comment || '' })
  dialogVisible.value = true
}

const handleReview = async () => {
  await reviewSubmission(reviewForm)
  ElMessage.success('批阅成功')
  dialogVisible.value = false
  loadData()
}
</script>

<template>
  <div>
    <div class="toolbar">
      <el-select v-model="query.taskId" placeholder="选择任务" filterable style="width: 280px"
                 @change="loadData">
        <el-option v-for="t in taskOptions" :key="t.id" :label="t.title" :value="t.id" />
      </el-select>
      <el-select v-model="query.status" placeholder="全部状态" clearable style="width: 130px"
                 @change="loadData">
        <el-option label="全部" :value="null" />
        <el-option label="待批阅" :value="0" />
        <el-option label="已批阅" :value="1" />
      </el-select>
      <el-select v-model="query.className" placeholder="全部班级" clearable style="width: 140px"
                 @change="loadData">
        <el-option label="全部" value="" />
        <el-option v-for="c in classOptions" :key="c" :label="c" :value="c" />
      </el-select>
      <el-button type="primary" @click="loadData">查询</el-button>
    </div>

    <el-table v-loading="loading" :data="tableData" border>
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="studentName" label="学生" width="110" />
      <el-table-column prop="username" label="学号" width="110" />
      <el-table-column prop="content" label="作业内容" show-overflow-tooltip />
      <el-table-column prop="attachmentUrl" label="附件" width="120">
        <template #default="{ row }">
          <a v-if="row.attachmentUrl" :href="`/api${row.attachmentUrl}`" target="_blank">下载</a>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column prop="submitTime" label="提交时间" width="170" />
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'warning'">
            {{ row.status === 1 ? '已批阅' : '待批阅' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="100">
        <template #default="{ row }">
          <el-button link type="primary" @click="openReview(row)">批阅</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination v-model:current-page="query.pageNum" v-model:page-size="query.pageSize"
                   :total="total" layout="total, prev, pager, next"
                   class="pager" @current-change="loadData" />

    <el-dialog v-model="dialogVisible" title="批阅作业" width="480px">
      <el-form label-width="80px">
        <el-form-item label="评分">
          <el-input-number v-model="reviewForm.score" :min="0" :max="100" :precision="1" />
        </el-form-item>
        <el-form-item label="评语">
          <el-input v-model="reviewForm.comment" type="textarea" :rows="4"
                    maxlength="500" show-word-limit />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleReview">确定</el-button>
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
