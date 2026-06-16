<template>
  <div class="page-container">
    <div class="page-header">
      <div class="page-title">投诉管理</div>
      <el-button type="primary" @click="handleAdd"><el-icon><Plus /></el-icon>受理投诉</el-button>
    </div>

    <el-table :data="list" v-loading="loading" border stripe>
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="complaintNo" label="投诉编号" width="170" />
      <el-table-column prop="complainantName" label="投诉人" width="100" />
      <el-table-column prop="complainantPhone" label="联系电话" width="130" />
      <el-table-column prop="stallId" label="摊位ID" width="80" />
      <el-table-column prop="scaleId" label="秤具ID" width="80" />
      <el-table-column prop="goodsName" label="商品名称" width="120" />
      <el-table-column prop="transactionTime" label="交易时间" width="170" />
      <el-table-column label="显示重量" width="100">
        <template #default="scope">{{ scope.row.displayWeight }}kg</template>
      </el-table-column>
      <el-table-column label="实际重量" width="100">
        <template #default="scope">{{ scope.row.actualWeight }}kg</template>
      </el-table-column>
      <el-table-column label="多收金额" width="100">
        <template #default="scope">¥{{ scope.row.overchargedAmount }}</template>
      </el-table-column>
      <el-table-column label="状态" width="110">
        <template #default="scope">
          <el-tag :type="statusTagType(scope.row.status)" size="small">{{ statusText(scope.row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="handlerName" label="处理人" width="90" />
      <el-table-column label="操作" width="240" fixed="right">
        <template #default="scope">
          <el-button v-if="scope.row.status === 'PENDING'" size="small" link type="primary" @click="investigate(scope.row)">调查</el-button>
          <el-button v-if="scope.row.status === 'UNDER_INVESTIGATION'" size="small" link type="success" @click="establish(scope.row)">投诉成立</el-button>
          <el-button v-if="scope.row.status === 'UNDER_INVESTIGATION'" size="small" link type="warning" @click="reject(scope.row)">驳回</el-button>
          <el-button v-if="scope.row.status === 'PENDING' || scope.row.status === 'UNDER_INVESTIGATION'"
                     size="small" link type="info" @click="cancel(scope.row)">撤销</el-button>
          <el-button v-if="scope.row.status !== 'CLOSED'" size="small" link type="danger" @click="close(scope.row)">关闭</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" title="受理投诉" width="640px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="投诉人" prop="complainantName">
          <el-input v-model="form.complainantName" />
        </el-form-item>
        <el-form-item label="联系电话">
          <el-input v-model="form.complainantPhone" />
        </el-form-item>
        <el-form-item label="摊位ID" prop="stallId">
          <el-input v-model.number="form.stallId" type="number" />
        </el-form-item>
        <el-form-item label="秤具ID" prop="scaleId">
          <el-input v-model.number="form.scaleId" type="number" />
        </el-form-item>
        <el-form-item label="交易时间" prop="transactionTime">
          <el-date-picker v-model="form.transactionTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" style="width: 100%" />
        </el-form-item>
        <el-form-item label="商品名称">
          <el-input v-model="form.goodsName" />
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="8">
            <el-form-item label="显示重量(kg)">
              <el-input v-model.number="form.displayWeight" type="number" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="实际重量(kg)">
              <el-input v-model.number="form.actualWeight" type="number" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="单价(元)">
              <el-input v-model.number="form.pricePerUnit" type="number" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="情况描述">
          <el-input v-model="form.description" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="reasonDialogVisible" :title="reasonDialogTitle" width="460px">
      <el-form :model="reasonForm" ref="reasonFormRef">
        <el-form-item :label="reasonDialogLabel" prop="value">
          <el-input v-model="reasonForm.value" type="textarea" :rows="4" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="reasonDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitReason">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import {
  getComplaints, createComplaint, investigateComplaint,
  establishComplaint, rejectComplaint, cancelComplaint, closeComplaint
} from '../api/complaint'

const loading = ref(false)
const list = ref([])
const dialogVisible = ref(false)
const reasonDialogVisible = ref(false)
const reasonDialogTitle = ref('')
const reasonDialogLabel = ref('')
const currentAction = ref(null)
const currentRow = ref(null)
const formRef = ref(null)
const reasonFormRef = ref(null)
const form = reactive({
  complainantName: '', complainantPhone: '', stallId: null, scaleId: null,
  transactionTime: '', goodsName: '', displayWeight: null, actualWeight: null,
  pricePerUnit: null, displayAmount: null, actualAmount: null, overchargedAmount: null, description: ''
})
const reasonForm = reactive({ value: '' })
const rules = {
  complainantName: [{ required: true, message: '请输入投诉人姓名', trigger: 'blur' }],
  stallId: [{ required: true, message: '请输入摊位ID', trigger: 'blur' }],
  scaleId: [{ required: true, message: '请输入秤具ID', trigger: 'blur' }],
  transactionTime: [{ required: true, message: '请选择交易时间', trigger: 'change' }]
}

const loadList = async () => {
  loading.value = true
  try { list.value = await getComplaints() || [] } finally { loading.value = false }
}

const handleAdd = () => {
  Object.assign(form, {
    complainantName: '', complainantPhone: '', stallId: null, scaleId: null,
    transactionTime: '', goodsName: '', displayWeight: null, actualWeight: null,
    pricePerUnit: null, displayAmount: null, actualAmount: null, overchargedAmount: null, description: ''
  })
  dialogVisible.value = true
}

const handleSubmit = async () => {
  await formRef.value.validate()
  if (form.displayWeight && form.pricePerUnit) {
    form.displayAmount = form.displayWeight * form.pricePerUnit
  }
  if (form.actualWeight && form.pricePerUnit) {
    form.actualAmount = form.actualWeight * form.pricePerUnit
  }
  if (form.displayAmount && form.actualAmount) {
    form.overchargedAmount = form.displayAmount - form.actualAmount
  }
  try {
    await createComplaint(form)
    ElMessage.success('受理成功')
    dialogVisible.value = false
    loadList()
  } catch (e) { console.error(e) }
}

const investigate = async (row) => {
  await investigateComplaint(row.id)
  ElMessage.success('已开始调查')
  loadList()
}

const openReasonDialog = (title, label, action, row) => {
  reasonDialogTitle.value = title
  reasonDialogLabel.value = label
  reasonForm.value = ''
  currentAction.value = action
  currentRow.value = row
  reasonDialogVisible.value = true
}

const establish = (row) => openReasonDialog('投诉成立', '处理结果', 'establish', row)
const reject = (row) => openReasonDialog('驳回投诉', '驳回理由', 'reject', row)
const cancel = (row) => openReasonDialog('撤销投诉', '撤销原因', 'cancel', row)

const submitReason = async () => {
  try {
    if (currentAction.value === 'establish') {
      await establishComplaint(currentRow.value.id, reasonForm.value)
    } else if (currentAction.value === 'reject') {
      await rejectComplaint(currentRow.value.id, reasonForm.value)
    } else if (currentAction.value === 'cancel') {
      await cancelComplaint(currentRow.value.id, reasonForm.value)
    }
    ElMessage.success('操作成功')
    reasonDialogVisible.value = false
    loadList()
  } catch (e) { console.error(e) }
}

const close = async (row) => {
  await closeComplaint(row.id)
  ElMessage.success('已关闭')
  loadList()
}

const statusTagType = (s) => ({
  PENDING: 'warning', UNDER_INVESTIGATION: 'primary', ESTABLISHED: 'danger',
  REJECTED: 'info', CANCELLED: 'info', CLOSED: 'success'
}[s] || 'info')
const statusText = (s) => ({
  PENDING: '待处理', UNDER_INVESTIGATION: '调查中', ESTABLISHED: '投诉成立',
  REJECTED: '已驳回', CANCELLED: '已撤销', CLOSED: '已关闭'
}[s] || s)

onMounted(loadList)
</script>
