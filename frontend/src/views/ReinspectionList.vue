<template>
  <div class="page-container">
    <div class="page-header">
      <div class="page-title">复检管理</div>
      <el-button type="primary" @click="handleAdd"><el-icon><Plus /></el-icon>创建复检</el-button>
    </div>

    <el-table :data="list" v-loading="loading" border stripe>
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="reinspectionNo" label="复检编号" width="170" />
      <el-table-column prop="complaintId" label="投诉ID" width="80" />
      <el-table-column prop="stallId" label="摊位ID" width="80" />
      <el-table-column prop="scaleId" label="秤具ID" width="80" />
      <el-table-column prop="metrologistName" label="计量员" width="100" />
      <el-table-column label="状态" width="100">
        <template #default="scope">
          <el-tag :type="statusTagType(scope.row.status)" size="small">{{ statusText(scope.row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="scheduledTime" label="预约时间" width="170" />
      <el-table-column prop="actualTime" label="实际时间" width="170" />
      <el-table-column label="标准重量" width="100">
        <template #default="scope">{{ scope.row.standardWeight }}kg</template>
      </el-table-column>
      <el-table-column label="测量重量" width="100">
        <template #default="scope">{{ scope.row.measuredWeight }}kg</template>
      </el-table-column>
      <el-table-column label="误差%" width="90">
        <template #default="scope">{{ scope.row.errorPercentage }}%</template>
      </el-table-column>
      <el-table-column label="操作" width="260" fixed="right">
        <template #default="scope">
          <el-button v-if="scope.row.status === 'PENDING'" size="small" link type="primary" @click="schedule(scope.row)">预约</el-button>
          <el-button v-if="scope.row.status === 'SCHEDULED'" size="small" link type="warning" @click="start(scope.row)">开始</el-button>
          <el-button v-if="scope.row.status === 'IN_PROGRESS'" size="small" link type="success" @click="complete(scope.row)">完成</el-button>
          <el-button v-if="scope.row.status !== 'PASSED' && scope.row.status !== 'FAILED' && scope.row.status !== 'CANCELLED'"
                     size="small" link type="info" @click="cancel(scope.row)">取消</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" title="创建复检" width="480px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="投诉ID">
          <el-input v-model.number="form.complaintId" type="number" />
        </el-form-item>
        <el-form-item label="摊位ID" prop="stallId">
          <el-input v-model.number="form.stallId" type="number" />
        </el-form-item>
        <el-form-item label="秤具ID" prop="scaleId">
          <el-input v-model.number="form.scaleId" type="number" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="scheduleDialogVisible" title="预约复检" width="520px">
      <el-form :model="scheduleForm" :rules="scheduleRules" ref="scheduleFormRef" label-width="100px">
        <el-form-item label="预约时间" prop="scheduledTime">
          <el-date-picker v-model="scheduleForm.scheduledTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" style="width: 100%" />
        </el-form-item>
        <el-form-item label="计量员ID">
          <el-input v-model.number="scheduleForm.metrologistId" type="number" />
        </el-form-item>
        <el-form-item label="计量员姓名">
          <el-input v-model="scheduleForm.metrologistName" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="scheduleDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitSchedule">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="completeDialogVisible" title="完成复检" width="560px">
      <el-form :model="completeForm" :rules="completeRules" ref="completeFormRef" label-width="110px">
        <el-form-item label="标准重量(kg)" prop="standardWeight">
          <el-input v-model.number="completeForm.standardWeight" type="number" />
        </el-form-item>
        <el-form-item label="测量重量(kg)" prop="measuredWeight">
          <el-input v-model.number="completeForm.measuredWeight" type="number" />
        </el-form-item>
        <el-form-item label="使用借秤">
          <el-switch v-model="completeForm.borrowedUsed" />
        </el-form-item>
        <el-form-item label="借秤ID" v-if="completeForm.borrowedUsed">
          <el-input v-model.number="completeForm.borrowedScaleId" type="number" />
        </el-form-item>
        <el-form-item label="结果备注" prop="resultRemark">
          <el-input v-model="completeForm.resultRemark" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="completeDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitComplete">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="cancelDialogVisible" title="取消复检" width="460px">
      <el-form :model="cancelForm" ref="cancelFormRef">
        <el-form-item label="取消原因" prop="reason">
          <el-input v-model="cancelForm.reason" type="textarea" :rows="4" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="cancelDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitCancel">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import {
  getReinspections, createReinspection, scheduleReinspection,
  startReinspection, completeReinspection, cancelReinspection
} from '../api/reinspection'

const loading = ref(false)
const list = ref([])
const dialogVisible = ref(false)
const scheduleDialogVisible = ref(false)
const completeDialogVisible = ref(false)
const cancelDialogVisible = ref(false)
const currentId = ref(null)
const formRef = ref(null)
const scheduleFormRef = ref(null)
const completeFormRef = ref(null)
const cancelFormRef = ref(null)
const form = reactive({ complaintId: null, stallId: null, scaleId: null })
const scheduleForm = reactive({ scheduledTime: '', metrologistId: null, metrologistName: '' })
const completeForm = reactive({ standardWeight: null, measuredWeight: null, borrowedUsed: false, borrowedScaleId: null, resultRemark: '' })
const cancelForm = reactive({ reason: '' })
const rules = {
  stallId: [{ required: true, message: '请输入摊位ID', trigger: 'blur' }],
  scaleId: [{ required: true, message: '请输入秤具ID', trigger: 'blur' }]
}
const scheduleRules = { scheduledTime: [{ required: true, message: '请选择预约时间', trigger: 'change' }] }
const completeRules = {
  standardWeight: [{ required: true, message: '请输入标准重量', trigger: 'blur' }],
  measuredWeight: [{ required: true, message: '请输入测量重量', trigger: 'blur' }],
  resultRemark: [{ required: true, message: '请填写结果备注', trigger: 'blur' }]
}

const loadList = async () => {
  loading.value = true
  try { list.value = await getReinspections() || [] } finally { loading.value = false }
}

const handleAdd = () => {
  Object.assign(form, { complaintId: null, stallId: null, scaleId: null })
  dialogVisible.value = true
}

const handleSubmit = async () => {
  await formRef.value.validate()
  try {
    await createReinspection(form)
    ElMessage.success('创建成功')
    dialogVisible.value = false
    loadList()
  } catch (e) { console.error(e) }
}

const schedule = (row) => {
  currentId.value = row.id
  Object.assign(scheduleForm, { scheduledTime: '', metrologistId: null, metrologistName: '' })
  scheduleDialogVisible.value = true
}

const submitSchedule = async () => {
  await scheduleFormRef.value.validate()
  try {
    await scheduleReinspection(currentId.value, scheduleForm)
    ElMessage.success('预约成功')
    scheduleDialogVisible.value = false
    loadList()
  } catch (e) { console.error(e) }
}

const start = async (row) => {
  await startReinspection(row.id)
  ElMessage.success('已开始复检')
  loadList()
}

const complete = (row) => {
  currentId.value = row.id
  Object.assign(completeForm, { standardWeight: null, measuredWeight: null, borrowedUsed: false, borrowedScaleId: null, resultRemark: '' })
  completeDialogVisible.value = true
}

const submitComplete = async () => {
  await completeFormRef.value.validate()
  try {
    await completeReinspection(currentId.value, completeForm)
    ElMessage.success('复检完成')
    completeDialogVisible.value = false
    loadList()
  } catch (e) { console.error(e) }
}

const cancel = (row) => {
  currentId.value = row.id
  cancelForm.reason = ''
  cancelDialogVisible.value = true
}

const submitCancel = async () => {
  try {
    await cancelReinspection(currentId.value, cancelForm.reason)
    ElMessage.success('已取消')
    cancelDialogVisible.value = false
    loadList()
  } catch (e) { console.error(e) }
}

const statusTagType = (s) => ({
  PENDING: 'warning', SCHEDULED: 'primary', IN_PROGRESS: 'info',
  PASSED: 'success', FAILED: 'danger', CANCELLED: 'info'
}[s] || 'info')
const statusText = (s) => ({
  PENDING: '待处理', SCHEDULED: '已预约', IN_PROGRESS: '进行中',
  PASSED: '通过', FAILED: '未通过', CANCELLED: '已取消'
}[s] || s)

onMounted(loadList)
</script>
