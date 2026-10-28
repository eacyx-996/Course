<script setup>
import { nextTick, onBeforeUnmount, onMounted, ref } from 'vue'
import { pageTasks } from '@/api/task'
import { getStatistics } from '@/api/review'
import * as echarts from 'echarts'

const taskOptions = ref([])
const classOptions = ['软件2301', '软件2302', '软件2303']
const taskId = ref(null)
const className = ref('')
const stat = ref(null)
const pieRef = ref()
let chart = null

const loadTasks = async () => {
  const { data } = await pageTasks({ pageNum: 1, pageSize: 100 })
  taskOptions.value = data.list
}

const loadStat = async () => {
  if (!taskId.value) return
  const { data } = await getStatistics(taskId.value, className.value || undefined)
  stat.value = data
  await nextTick()
  renderPie()
}

const renderPie = () => {
  if (!pieRef.value || !stat.value) return
  if (!chart) {
    chart = echarts.init(pieRef.value)
  }
  chart.setOption({
    tooltip: { trigger: 'item' },
    legend: { bottom: 0 },
    series: [{
      name: '分数段分布',
      type: 'pie',
      radius: ['40%', '70%'],
      avoidLabelOverlap: true,
      itemStyle: {
        borderRadius: 8,
        borderColor: '#fff',
        borderWidth: 2
      },
      label: { show: true, formatter: '{b}: {c}人' },
      data: stat.value.scoreDistribution.map(d => ({ name: d.range, value: d.count }))
    }]
  }, true) // notMerge：切换班级/任务时全量替换，避免旧数据残留
  chart.resize()
}

const handleResize = () => chart && chart.resize()

onMounted(() => {
  loadTasks()
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  chart && chart.dispose()
})
</script>

<template>
  <div>
    <div class="toolbar">
      <el-select v-model="taskId" placeholder="选择任务" filterable style="width: 280px"
                 @change="loadStat">
        <el-option v-for="t in taskOptions" :key="t.id" :label="t.title" :value="t.id" />
      </el-select>
      <el-select v-model="className" placeholder="全部班级" clearable style="width: 160px"
                 @change="loadStat">
        <el-option label="全部" value="" />
        <el-option v-for="c in classOptions" :key="c" :label="c" :value="c" />
      </el-select>
    </div>

    <template v-if="stat">
      <el-row :gutter="16">
        <el-col :span="6">
          <el-card class="hover-card"><div class="num">{{ stat.totalStudents }}</div><div class="label">学生总数</div></el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="hover-card"><div class="num">{{ stat.submittedCount }}</div><div class="label">已提交</div></el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="hover-card"><div class="num">{{ (stat.submissionRate * 100).toFixed(0) }}%</div><div class="label">提交率</div></el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="hover-card"><div class="num">{{ stat.avgScore ?? '-' }}</div><div class="label">平均分</div></el-card>
        </el-col>
      </el-row>

      <el-row :gutter="16">
        <el-col :span="12">
          <el-card class="dist-card hover-card">
            <template #header>分数段分布</template>
            <div v-for="d in stat.scoreDistribution" :key="d.range" class="dist-row">
              <span class="range">{{ d.range }}</span>
              <el-progress :percentage="stat.reviewedCount ? Math.round(d.count / stat.reviewedCount * 100) : 0"
                           :stroke-width="18" style="flex: 1" />
              <span class="count">{{ d.count }} 人</span>
            </div>
          </el-card>
        </el-col>
        <el-col :span="12">
          <el-card class="dist-card hover-card">
            <template #header>分数段占比</template>
            <div ref="pieRef" class="pie"></div>
          </el-card>
        </el-col>
      </el-row>
    </template>

    <el-empty v-else description="请选择任务查看统计" />
  </div>
</template>

<style scoped>
.toolbar {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
}
.num {
  font-size: 28px;
  font-weight: 600;
  text-align: center;
  background: linear-gradient(90deg, #409eff, #36cfc9);
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
}
.label {
  text-align: center;
  color: #909399;
  margin-top: 8px;
}
.dist-card {
  margin-top: 16px;
}
.pie {
  height: 320px;
}
.dist-row {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}
.range {
  width: 70px;
}
.count {
  width: 60px;
  text-align: right;
}
</style>
