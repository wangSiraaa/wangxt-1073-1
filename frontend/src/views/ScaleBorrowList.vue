<template>
  <div class="page-container">
    <div class="page-header">
      <div class="page-title">临时借秤管理</div>
      <el-button type="primary" @click="handleAdd"><el-icon><Share /></el-icon>借出秤具</el-button>
    </div>

    <el-table :data="list" v-loading="loading" border stripe>
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="scaleId" label="秤具ID" width="80" />
      <el-table-column prop="borrowedToStallId" label="借到摊位ID" width="110" />
      <el-table-column prop="borrowerName" label="借用人" width="100" />
      <el-table-column prop="borrowReason" label="借用原因" show-overflow-tooltip />
      <el-table-column prop="borrowedAt" label="借出时间" width="170" />
      <el-table-column prop="expectedReturnAt" label="预计归还" width="170" />
      <el-table-column label="状态" width="90">
        <template #default="scope">
          <el-tag :type="scope.row.isReturned ? 'success' : 'warning'" size="small">
            {{ scope.row.isReturned ? '已归还' : '借用中' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="returnedAt" label="归还时间" width="170" />
      <el-table-column label="操作" width="120" fixed="right">
        <template #default="scope">
          <el-button v-if="!scope.row.isReturned" size="small" link type="success" @click="handleReturn(scope.row)">归还</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" title="借出秤具" width="520px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="110px">
        <el-form-item label="秤具ID" prop="scaleId">
          <el-input v-model.number="form.scaleId" type="number" />
        </el-form-item>
        <el-form-item label="借到摊位ID">
          <el-input v-model.number="form.toStallId" type="number" />
        </el-form-item>
        <el-form-item label="借用人ID">
          <el-input v-model.number="form.borrowerId" type="number" />
        </el-form-item>
        <el-form-item label="借用人姓名">
          <el-input v-model="form.borrowerName" />
        </el-form-item>
        <el-form-item label="借用原因" prop="borrowReason">
          <el-input v-model="form.borrowReason" />
        </el-form-item>
        <el-form-item label="预计归还时间">
          <el-date-picker v-model="form.expectedReturnAt" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="returnDialogVisible" title="归还秤具" width="460px">
      <el-form :model="returnForm" ref="returnFormRef">
        <el-form-item label="归还备注">
          <el-input v-model="returnForm.returnRemark" type="textarea" :rows="4" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="returnDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitReturn">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getScaleBorrows, borrowScale, returnScale } from '../api/scaleBorrow'

const loading = ref(false)
const list = ref([])
const dialogVisible = ref(false)
const returnDialogVisible = ref(false)
const currentId = ref(null)
const formRef = ref(null)
const returnFormRef = ref(null)
const form = reactive({ scaleId: null, toStallId: null, borrowerId: null, borrowerName: '', borrowReason: '', expectedReturnAt: '' })
const returnForm = reactive({ returnRemark: '' })
const rules = {
  scaleId: [{ required: true, message: '请输入秤具ID', trigger: 'blur' }],
  borrowReason: [{ required: true, message: '请输入借用原因', trigger: 'blur' }]
}

const loadList = async () => {
  loading.value = true
  try { list.value = await getScaleBorrows() || [] } finally { loading.value = false }
}

const handleAdd = () => {
  Object.assign(form, { scaleId: null, toStallId: null, borrowerId: null, borrowerName: '', borrowReason: '', expectedReturnAt: '' })
  dialogVisible.value = true
}

const handleSubmit = async () => {
  await formRef.value.validate()
  try {
    await borrowScale(form)
    ElMessage.success('借出成功')
    dialogVisible.value = false
    loadList()
  } catch (e) { console.error(e) }
}

const handleReturn = (row) => {
  currentId.value = row.id
  returnForm.returnRemark = ''
  returnDialogVisible.value = true
}

const submitReturn = async () => {
  try {
    await returnScale(currentId.value, returnForm.returnRemark)
    ElMessage.success('归还成功')
    returnDialogVisible.value = false
    loadList()
  } catch (e) { console.error(e) }
}

onMounted(loadList)
</script>
