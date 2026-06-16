<template>
  <div class="page-container">
    <div class="page-header">
      <div class="page-title">审计日志</div>
    </div>

    <el-table :data="list" v-loading="loading" border stripe>
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="operationType" label="操作类型" width="160" />
      <el-table-column prop="module" label="模块" width="120" />
      <el-table-column prop="entityId" label="关联ID" width="90" />
      <el-table-column prop="operatorName" label="操作人" width="100" />
      <el-table-column prop="operatorRole" label="角色" width="120" />
      <el-table-column prop="detail" label="详情" show-overflow-tooltip />
      <el-table-column prop="ipAddress" label="IP地址" width="130" />
      <el-table-column prop="createdAt" label="操作时间" width="170" />
    </el-table>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getAuditLogs } from '../api/auditLog'

const loading = ref(false)
const list = ref([])

const loadList = async () => {
  loading.value = true
  try { list.value = await getAuditLogs() || [] } finally { loading.value = false }
}

onMounted(loadList)
</script>
