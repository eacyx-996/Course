<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { pageUsers, updateUserStatus } from '@/api/user'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

const classOptions = ['软件2301', '软件2302', '软件2303']
const query = reactive({ pageNum: 1, pageSize: 10, role: '', className: '', keyword: '' })
const tableData = ref([])
const total = ref(0)
const loading = ref(false)

const loadData = async () => {
  loading.value = true
  try {
    const { data } = await pageUsers(query)
    tableData.value = data.list
    total.value = data.total
  } finally {
    loading.value = false
  }
}

onMounted(loadData)

const toggleStatus = async (row) => {
  const target = row.status === 1 ? 0 : 1
  await updateUserStatus(row.id, target)
  ElMessage.success('操作成功')
  loadData()
}
</script>

<template>
  <div>
    <div class="toolbar">
      <el-input v-model="query.keyword" placeholder="用户名/姓名" clearable
                style="width: 200px" @keyup.enter="loadData" />
      <el-select v-model="query.role" placeholder="全部角色" clearable style="width: 130px">
        <el-option label="全部" value="" />
        <el-option label="管理员" value="admin" />
        <el-option label="教师" value="teacher" />
        <el-option label="学生" value="student" />
      </el-select>
      <el-select v-model="query.className" placeholder="全部班级" clearable style="width: 140px">
        <el-option label="全部" value="" />
        <el-option v-for="c in classOptions" :key="c" :label="c" :value="c" />
      </el-select>
      <el-button type="primary" @click="loadData">查询</el-button>
    </div>

    <el-table v-loading="loading" :data="tableData" border>
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="username" label="学号/工号" width="130" />
      <el-table-column prop="realName" label="姓名" width="120" />
      <el-table-column prop="role" label="角色" width="100" />
      <el-table-column prop="className" label="班级" width="130" />
      <el-table-column prop="email" label="邮箱" show-overflow-tooltip />
      <el-table-column prop="createTime" label="注册时间" width="170" />
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'">
            {{ row.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column v-if="userStore.role === 'admin'" label="操作" width="110">
        <template #default="{ row }">
          <el-button link :type="row.status === 1 ? 'danger' : 'success'"
                     @click="toggleStatus(row)">
            {{ row.status === 1 ? '禁用' : '启用' }}
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination v-model:current-page="query.pageNum" v-model:page-size="query.pageSize"
                   :total="total" layout="total, prev, pager, next"
                   class="pager" @current-change="loadData" />
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
