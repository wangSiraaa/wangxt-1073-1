<template>
  <div class="page-container">
    <div class="page-header">
      <div class="page-title">暂停营业管理</div>
      <el-button type="primary" @click="handleAdd"><el-icon><Plus /></el-icon>暂停摊位</el-button>
    </div>

    <el-table :data="list" v-loading="loading" border stripe>
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="stallId" label="摊位ID" width="80" />
      <el-table-column label="暂停类型" width="140">
        <template #default="scope">
          <el-tag :type="typeTagType(scope.row.suspensionType)" size="small">{{ typeText(scope.row.suspensionType) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="relatedType" label="关联类型" width="100" />
      <el-table-column prop="relatedId" label="关联ID" width="80" />
      <el-table-column prop="reason" label="原因" show-overflow-tooltip />
      <el-table-column prop="operatorName" label="操作人" width="100" />
      <el-table-column prop="suspendedAt" label="暂停时间" width="170" />
      <el-table-column label="状态" width="80">
        <template #default="scope">
          <el-tag :type="scope.row.isActive ? 'danger' : 'success'" size="small">
            {{ scope.row.isActive ? '生效中' : '已恢复' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="reopenedByName" label="恢复人" width="90" />
      <el-table-column prop="reopenedAt" label="恢复时间" width="170" />
      <el-table-column label="操作" width="120" fixed="right">
        <template #default="scope">
          <el-button v-if="scope.row.isActive" size="small" link type="success" @click="handleReopen(scope.row)">整改复开</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" title="暂停摊位营业" width="520px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="摊位ID" prop="stallId">
          <el-input v-model.number="form.stallId" type="number" />
        </el-form-item>
        <el-form-item label="暂停类型" prop="suspensionType">
          <el-select v-model="form.suspensionType" style="width: 100%">
            <el-option label="校准过期" value="CALIBRATION_EXPIRED" />
            <el-option label="复检未通过" value="REINSPECTION_FAILED" />
            <el-option label="投诉成立" value="COMPLIANT_ESTABLISHED" />
            <el-option label="人工暂停" value="MANUAL" />
          </el-select>
        </el-form-item>
        <el-form-item label="关联类型">
          <el-input v-model="form.relatedType" placeholder="例如: SCALE / REINSPECTION" />
        </el-form-item>
        <el-form-item label="关联ID">
          <el-input v-model.number="form.relatedId" type="number" />
        </el-form-item>
        <el-form-item label="原因" prop="reason">
          <el-input v-model="form.reason" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="reopenDialogVisible" title="整改复开" width="460px">
      <el-form :model="reopenForm" ref="reopenFormRef">
        <el-form-item label="复开原因" prop="reopenReason">
          <el-input v-model="reopenForm.reopenReason" type="textarea" :rows="4" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="reopenDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitReopen">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getSuspensions, suspendStall, reopenStall } from '../api/suspension'

const loading = ref(false)
const list = ref([])
const dialogVisible = ref(false)
const reopenDialogVisible = ref(false)
const currentId = ref(null)
const formRef = ref(null)
const reopenFormRef = ref(null)
const form = reactive({ stallId: null, suspensionType: 'CALIBRATION_EXPIRED', relatedType: '', relatedId: null, reason: '' })
const reopenForm = reactive({ reopenReason: '' })
const rules = {
  stallId: [{ required: true, message: '请输入摊位ID', trigger: 'blur' }],
  suspensionType: [{ required: true, message: '请选择暂停类型', trigger: 'change' }],
  reason: [{ required: true, message: '请输入原因', trigger: 'blur' }]
}

const loadList = async () => {
  loading.value = true
  try { list.value = await getSuspensions() || [] } finally { loading.value = false }
}

const handleAdd = () => {
  Object.assign(form, { stallId: null, suspensionType: 'CALIBRATION_EXPIRED', relatedType: '', relatedId: null, reason: '' })
  dialogVisible.value = true
}

const handleSubmit = async () => {
  await formRef.value.validate()
  try {
    await suspendStall(form)
    ElMessage.success('暂停成功')
    dialogVisible.value = false
    loadList()
  } catch (e) { console.error(e) }
}

const handleReopen = (row) => {
  currentId.value = row.id
  reopenForm.reopenReason = ''
  reopenDialogVisible.value = true
}

const submitReopen = async () => {
  try {
    await reopenStall(currentId.value, reopenForm.reopenReason)
    ElMessage.success('复开成功')
    reopenDialogVisible.value = false
    loadList()
  } catch (e) { console.error(e) }
}

const typeTagType = (t) => ({
  CALIBRATION_EXPIRED: 'warning', REINSPECTION_FAILED: 'danger', COMPLIANT_ESTABLISHED: 'danger', MANUAL: 'info'
}[t] || 'info')
const typeText = (t) => ({
  CALIBRATION_EXPIRED: '校准过期', REINSPECTION_FAILED: '复检未通过', COMPLIANT_ESTABLISHED: '投诉成立', MANUAL: '人工暂停'
}[t] || t)

onMounted(loadList)
</script>
