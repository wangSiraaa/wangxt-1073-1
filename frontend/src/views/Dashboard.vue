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
              <div style="font-size: 14px; color: #909399">暂停营业摊位</div>
              <div style="font-size: 28px; font-weight: 600; margin-top: 8px; color: #F56C6C">{{ stats.suspendedStalls }}</div>
            </div>
            <el-icon style="font-size: 40px; color: #F56C6C"><VideoPause /></el-icon>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <div style="font-weight: 600">待复检任务</div>
          </template>
          <el-table :data="pendingReinspections" size="small" style="width: 100%">
            <el-table-column prop="reinspectionNo" label="复检编号" width="160" />
            <el-table-column prop="stallId" label="摊位ID" width="80" />
            <el-table-column prop="status" label="状态" width="120">
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
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getStalls } from '../api/stall'
import { getScales } from '../api/scale'
import { getComplaints } from '../api/complaint'
import { getReinspections } from '../api/reinspection'
import { getSuspensions } from '../api/suspension'

const stats = ref({ stalls: 0, scales: 0, pendingComplaints: 0, suspendedStalls: 0 })
const pendingReinspections = ref([])
const expiringScales = ref([])

const loadData = async () => {
  try {
    const [stalls, scales, complaints, reinspections, suspensions] = await Promise.all([
      getStalls(), getScales(), getComplaints(), getReinspections(), getSuspensions()
    ])
    stats.value.stalls = stalls?.length || 0
    stats.value.scales = scales?.length || 0
    stats.value.pendingComplaints = (complaints || []).filter(c => c.status === 'PENDING' || c.status === 'UNDER_INVESTIGATION').length
    stats.value.suspendedStalls = (suspensions || []).filter(s => s.isActive).length

    pendingReinspections.value = (reinspections || [])
      .filter(r => r.status === 'PENDING' || r.status === 'SCHEDULED' || r.status === 'IN_PROGRESS')
      .slice(0, 5)

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

onMounted(loadData)
</script>
