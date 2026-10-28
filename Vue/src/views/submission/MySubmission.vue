<script setup>
import { onMounted, reactive, ref } from 'vue'
import { pageMySubmissions } from '@/api/submission'

const query = reactive({ pageNum: 1, pageSize: 10 })
const tableData = ref([])
const total = ref(0)
const loading = ref(false)

const loadData = async () => {
  loading.value = true
  try {
    const { data } = await pageMySubmissions(query)
    tableData.value = data.list
    total.value = data.total
  } finally {
    loading.value = false
  }
}

onMounted(loadData)
</script>

<template>
  <div>
    <el-table v-loading="loading" :data="tableData" border>
      <el-table-column prop="submissionId" label="ID" width="70" />
      <el-table-column prop="taskTitle" label="任务" show-overflow-tooltip />
      <el-table-column prop="submitTime" label="提交时间" width="170" />
      <el-table-column label="批阅状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'warning'">
            {{ row.status === 1 ? '已批阅' : '待批阅' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="score" label="分数" width="90">
        <template #default="{ row }">{{ row.score ?? '-' }}</template>
      </el-table-column>
      <el-table-column prop="comment" label="评语" show-overflow-tooltip>
        <template #default="{ row }">{{ row.comment || '-' }}</template>
      </el-table-column>
    </el-table>

    <el-pagination v-model:current-page="query.pageNum" v-model:page-size="query.pageSize"
                   :total="total" layout="total, prev, pager, next"
                   class="pager" @current-change="loadData" />
  </div>
</template>

<style scoped>
.pager {
  margin-top: 16px;
  justify-content: flex-end;
}
</style>
