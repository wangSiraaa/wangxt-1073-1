<template>
  <div>
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card shadow="hover">
          <div style="display: flex; align-items: center; justify-content: space-between">
            <div>
              <div style="font-size: 14px; color: #909399">摊位总数</div>
              <div style="font-size: 28px; font-weight: 600; margin-top: 8px; color: #409EFF">{{ stats.stalls }}</div>
            </div>
            <el-icon style="font-size: 40px; color: #409EFF"><Shop /></el-icon>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div style="display: flex; align-items: center; justify-content: space-between">
            <div>
              <div style="font-size: 14px; color: #909399">秤具总数</div>
              <div style="font-size: 28px; font-weight: 600; margin-top: 8px; color: #67C23A">{{ stats.scales }}</div>
            </div>
            <el-icon style="font-size: 40px; color: #67C23A"><Scale /></el-icon>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div style="display: flex; align-items: center; justify-content: space-between">
            <div>
              <div style="font-size: 14px; color: #909399">待处理投诉</div>
              <div style="font-size: 28px; font-weight: 600; margin-top: 8px; color: #E6A23C">{{ stats.pendingComplaints }}</div>
            </div>
            <el-icon style="font-size: 40px; color: #E6A23C"><Bell /></el-icon>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div style="display: flex; align-items: center; justify-content: space-between">
            <div>
              <div style="font-size: 14px; color: #909399">整改提醒</div>
              <div style="font-size: 28px; font-weight: 600; margin-top: 8px; color: #F56C6C">{{ pendingReminders.length }}</div>
            </div>
            <el-icon style="font-size: 40px; color: #F56C6C"><AlarmClock /></el-icon>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <div style="display:flex;justify-content:space-between;align-items:center">
              <div style="font-weight: 600">待复检任务</div>
              <el-tag v-if="urgentCount > 0" type="danger" size="small">{{ urgentCount }}个紧急</el-tag>
            </div>
          </template>
          <el-table :data="pendingReinspections" size="small" style="width: 100%">
            <el-table-column prop="reinspectionNo" label="复检编号" width="150" />
            <el-table-column prop="stallId" label="摊位ID" width="80" />
            <el-table-column label="优先级" width="90">
              <template #default="scope">
                <el-tag :type="priorityTagType(scope.row.priority)" size="small">{{ priorityText(scope.row.priority) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="100">
              <template #default="scope">
                <el-tag :type="statusTagType(scope.row.status)" size="small">{{ statusText(scope.row.status) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="scheduledTime" label="预约时间" />
          </el-table>
          <el-empty v-if="pendingReinspections.length === 0" description="暂无待复检任务" :image-size="80" />
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <div style="font-weight: 600">整改提醒</div>
          </template>
          <el-table :data="pendingReminders" size="small" style="width: 100%">
            <el-table-column prop="scaleId" label="秤具ID" width="80" />
            <el-table-column prop="stallId" label="摊位ID" width="80" />
            <el-table-column label="提醒对象" width="90">
              <template #default="scope">
                <el-tag :type="scope.row.targetType === 'STALL_OWNER' ? 'primary' : 'warning'" size="small">
                  {{ scope.row.targetType === 'STALL_OWNER' ? '摊主' : '管理员' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="deadline" label="截止日期" width="110" />
            <el-table-column prop="reminderDate" label="提醒日期" width="110" />
            <el-table-column prop="content" label="内容" show-overflow-tooltip />
          </el-table>
          <el-empty v-if="pendingReminders.length === 0" description="暂无整改提醒" :image-size="80" />
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <div style="font-weight: 600">校准即将过期的秤具</div>
          </template>
          <el-table :data="expiringScales" size="small" style="width: 100%">
            <el-table-column prop="scaleCode" label="秤具编号" width="140" />
            <el-table-column prop="stallId" label="摊位ID" width="80" />
            <el-table-column prop="status" label="状态" width="120">
              <template #default="scope">
                <el-tag type="warning" size="small">{{ statusText(scope.row.status) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="calibrationExpireDate" label="过期日期" />
          </el-table>
          <el-empty v-if="expiringScales.length === 0" description="暂无即将过期的秤具" :image-size="80" />
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <div style="font-weight: 600">当前借用中的备用秤</div>
          </template>
          <el-table :data="activeBorrows" size="small" style="width: 100%">
            <el-table-column prop="scaleId" label="借用秤ID" width="90" />
            <el-table-column prop="borrowedToStallId" label="使用摊位" width="90" />
            <el-table-column label="原秤ID" width="90">
              <template #default="scope">
                {{ scope.row.originalScaleId || '-' }}
              </template>
            </el-table-column>
            <el-table-column label="借用原因" width="100">
              <template #default="scope">
                <el-tag :type="borrowContextTagType(scope.row.borrowContext)" size="small">
                  {{ borrowContextText(scope.row.borrowContext) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="expectedReturnAt" label="预计归还" />
          </el-table>
          <el-empty v-if="activeBorrows.length === 0" description="暂无借用中的备用秤" :image-size="80" />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { getStalls } from '../api/stall'
import { getScales } from '../api/scale'
import { getComplaints } from '../api/complaint'
import { getReinspections } from '../api/reinspection'
import { getSuspensions } from '../api/suspension'
import { getPendingReminders } from '../api/reminder'
import { getActiveBorrows } from '../api/scaleBorrow'

const stats = ref({ stalls: 0, scales: 0, pendingComplaints: 0, suspendedStalls: 0 })
const pendingReinspections = ref([])
const expiringScales = ref([])
const pendingReminders = ref([])
const activeBorrows = ref([])

const urgentCount = computed(() => {
  return pendingReinspections.value.filter(r => r.priority === 'URGENT' || r.priority === 'EMERGENCY').length
})

const loadData = async () => {
  try {
    const [stalls, scales, complaints, reinspections, suspensions, reminders, borrows] = await Promise.all([
      getStalls(), getScales(), getComplaints(), getReinspections(), getSuspensions(),
      getPendingReminders(), getActiveBorrows()
    ])
    stats.value.stalls = stalls?.length || 0
    stats.value.scales = scales?.length || 0
    stats.value.pendingComplaints = (complaints || []).filter(c => c.status === 'PENDING' || c.status === 'UNDER_INVESTIGATION').length
    stats.value.suspendedStalls = (suspensions || []).filter(s => s.isActive).length

    pendingReinspections.value = (reinspections || [])
      .filter(r => r.status === 'PENDING' || r.status === 'SCHEDULED' || r.status === 'IN_PROGRESS')
      .sort((a, b) => {
        const order = { EMERGENCY: 0, URGENT: 1, NORMAL: 2 }
        return (order[a.priority] ?? 2) - (order[b.priority] ?? 2)
      })
      .slice(0, 5)

    pendingReminders.value = (reminders || []).sort((a, b) => {
      if (a.reminderDate < b.reminderDate) return -1
      if (a.reminderDate > b.reminderDate) return 1
      return 0
    }).slice(0, 8)

    activeBorrows.value = borrows || []

    const today = new Date()
    const in30Days = new Date(today.getTime() + 30 * 24 * 60 * 60 * 1000)
    expiringScales.value = (scales || [])
      .filter(s => {
        if (!s.calibrationExpireDate) return false
        const d = new Date(s.calibrationExpireDate)
        return d >= today && d <= in30Days
      })
      .slice(0, 5)
  } catch (e) {
    console.error(e)
  }
}

const statusTagType = (status) => {
  const map = { PENDING: 'warning', SCHEDULED: 'primary', IN_PROGRESS: 'info', PASSED: 'success', FAILED: 'danger', CANCELLED: 'info' }
  return map[status] || 'info'
}

const statusText = (status) => {
  const map = {
    PENDING: '待处理', SCHEDULED: '已预约', IN_PROGRESS: '进行中',
    PASSED: '通过', FAILED: '未通过', CANCELLED: '已取消',
    NORMAL: '正常', NEEDS_RECTIFICATION: '限期整改', DISABLED: '停用',
    CALIBRATION_EXPIRED: '校准过期', BORROWED: '已借出'
  }
  return map[status] || status
}

const priorityTagType = (p) => ({ NORMAL: 'info', URGENT: 'warning', EMERGENCY: 'danger' }[p] || 'info')
const priorityText = (p) => ({ NORMAL: '普通', URGENT: '紧急', EMERGENCY: '特级' }[p] || p)

const borrowContextTagType = (c) => ({
  RECTIFICATION_REPLACEMENT: 'warning',
  CALIBRATION_REPLACEMENT: 'danger',
  REINSPECTION_REPLACEMENT: 'danger',
  TEMPORARY: 'info'
}[c] || 'info')
const borrowContextText = (c) => ({
  RECTIFICATION_REPLACEMENT: '整改替代',
  CALIBRATION_REPLACEMENT: '校准替代',
  REINSPECTION_REPLACEMENT: '复检替代',
  TEMPORARY: '临时借用'
}[c] || '临时借用')

onMounted(loadData)
</script>
