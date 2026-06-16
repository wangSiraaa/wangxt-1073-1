<template>
  <div class="page-container">
    <div class="page-header">
      <div class="page-title">秤具管理</div>
      <div>
        <el-button @click="checkExpired"><el-icon><Refresh /></el-icon>检查过期</el-button>
        <el-button type="primary" @click="handleAdd"><el-icon><Plus /></el-icon>登记秤具</el-button>
      </div>
    </div>

    <el-table :data="list" v-loading="loading" border stripe>
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="scaleCode" label="秤具编号" width="130" />
      <el-table-column prop="scaleModel" label="型号" width="120" />
      <el-table-column prop="manufacturer" label="生产厂家" width="120" />
      <el-table-column prop="stallId" label="摊位ID" width="80" />
      <el-table-column prop="maxCapacity" label="量程" width="90" />
      <el-table-column prop="accuracy" label="精度" width="80" />
      <el-table-column prop="calibrationExpireDate" label="校准有效期" width="120" />
      <el-table-column label="铅封状态" width="100">
        <template #default="scope">
          <el-tag :type="sealTagType(scope.row.sealStatus)" size="small">
            {{ sealText(scope.row.sealStatus) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="120">
        <template #default="scope">
          <el-tag :type="scaleStatusTagType(scope.row.status)" size="small">
            {{ scaleStatusText(scope.row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="借用/替代" width="110">
        <template #default="scope">
          <el-tag v-if="scope.row.status === 'BORROWED'" type="warning" size="small">已借出</el-tag>
          <el-tag v-else-if="scope.row.status === 'NEEDS_RECTIFICATION' && scope.row.stallId" type="info" size="small">待替代</el-tag>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="营业资格" width="90">
        <template #default="scope">
          <el-tag :type="scope.row.businessQualified ? 'success' : 'danger'" size="small">
            {{ scope.row.businessQualified ? '有效' : '无效' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="310" fixed="right">
        <template #default="scope">
          <el-button size="small" link type="primary" @click="handleEdit(scope.row)">编辑</el-button>
          <el-button size="small" link type="warning" @click="handleBind(scope.row)">绑定摊位</el-button>
          <el-button v-if="scope.row.stallId" size="small" link type="info" @click="handleViewUsage(scope.row)">摊位秤况</el-button>
          <el-button size="small" link :type="scope.row.businessQualified ? 'warning' : 'success'"
                     @click="handleToggleQualified(scope.row)">
            {{ scope.row.businessQualified ? '取消资格' : '恢复' }}
          </el-button>
          <el-button size="small" link type="danger" @click="handleDelete(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑秤具' : '登记秤具'" width="560px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="110px">
        <el-form-item label="秤具编号" prop="scaleCode">
          <el-input v-model="form.scaleCode" />
        </el-form-item>
        <el-form-item label="型号">
          <el-input v-model="form.scaleModel" />
        </el-form-item>
        <el-form-item label="生产厂家">
          <el-input v-model="form.manufacturer" />
        </el-form-item>
        <el-form-item label="出厂编号">
          <el-input v-model="form.factoryNumber" />
        </el-form-item>
        <el-form-item label="量程">
          <el-input v-model="form.maxCapacity" placeholder="例如: 30kg" />
        </el-form-item>
        <el-form-item label="精度">
          <el-input v-model="form.accuracy" placeholder="例如: 5g" />
        </el-form-item>
        <el-form-item label="摊位">
          <el-input v-model.number="form.stallId" type="number" placeholder="摊位ID" />
        </el-form-item>
        <el-form-item label="校准有效期">
          <el-date-picker v-model="form.calibrationExpireDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
        </el-form-item>
        <el-form-item label="铅封状态">
          <el-select v-model="form.sealStatus" style="width: 100%">
            <el-option label="完好" value="INTACT" />
            <el-option label="损坏" value="DAMAGED" />
            <el-option label="缺失" value="MISSING" />
          </el-select>
        </el-form-item>
        <el-form-item label="铅封号">
          <el-input v-model="form.sealNumber" />
        </el-form-item>
        <el-form-item label="营业资格">
          <el-switch v-model="form.businessQualified" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="bindDialogVisible" title="绑定摊位" width="400px">
      <el-form :model="bindForm" ref="bindFormRef" label-width="80px">
        <el-form-item label="摊位ID" prop="stallId">
          <el-input v-model.number="bindForm.stallId" type="number" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="bindDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitBind">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="scaleUsageVisible" :title="'摊位 ' + scaleUsageInfo.stallCode + ' 秤具使用状态'" width="900px">
      <el-alert
        :title="scaleUsageInfo.canOperate ? '该摊位当前可以营业' : '该摊位当前无法营业'"
        :type="scaleUsageInfo.canOperate ? 'success' : 'error'"
        :closable="false"
        style="margin-bottom: 16px"
      />
      <el-table :data="scaleUsageInfo.scales || []" border stripe size="small">
        <el-table-column prop="scaleCode" label="秤具编号" width="120" />
        <el-table-column label="类型" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.original ? '' : 'warning'" size="small">
              {{ scope.row.original ? '原配秤' : '备用秤' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="在用" width="70">
          <template #default="scope">
            <el-tag :type="scope.row.currentlyInUse ? 'success' : 'info'" size="small">
              {{ scope.row.currentlyInUse ? '是' : '否' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="活跃投诉" width="90">
          <template #default="scope">
            <el-badge :value="scope.row.activeComplaintCount || 0" :type="scope.row.activeComplaintCount > 0 ? 'danger' : 'info'" />
          </template>
        </el-table-column>
        <el-table-column label="关联信息" show-overflow-tooltip>
          <template #default="scope">
            <div v-if="scope.row.borrowedScaleCode" style="color: #67C23A">
              替代秤: {{ scope.row.borrowedScaleCode }}
              <span v-if="scope.row.borrowContext">({{ borrowContextLabel(scope.row.borrowContext) }})</span>
            </div>
            <div v-if="scope.row.originalScaleCode" style="color: #E6A23C">
              原秤: {{ scope.row.originalScaleCode }} ({{ scaleStatusLabel(scope.row.originalScaleStatus) }})
            </div>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getScales, createScale, updateScale, deleteScale,
  bindScaleStall, updateScaleQualified, checkExpiredScales
} from '../api/scale'
import { getStallScaleUsage } from '../api/stallScaleUsage'

const loading = ref(false)
const list = ref([])
const dialogVisible = ref(false)
const bindDialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref(null)
const bindFormRef = ref(null)
const currentId = ref(null)
const form = reactive({
  id: null, scaleCode: '', scaleModel: '', manufacturer: '', factoryNumber: '',
  maxCapacity: '', accuracy: '', stallId: null, calibrationExpireDate: '',
  sealStatus: 'INTACT', sealNumber: '', businessQualified: true
})
const bindForm = reactive({ stallId: null })
const rules = { scaleCode: [{ required: true, message: '请输入秤具编号', trigger: 'blur' }] }

const loadList = async () => {
  loading.value = true
  try { list.value = await getScales() || [] } finally { loading.value = false }
}

const checkExpired = async () => {
  await checkExpiredScales()
  ElMessage.success('已检查并更新过期状态')
  loadList()
}

const handleAdd = () => {
  isEdit.value = false
  Object.assign(form, { id: null, scaleCode: '', scaleModel: '', manufacturer: '', factoryNumber: '', maxCapacity: '', accuracy: '', stallId: null, calibrationExpireDate: '', sealStatus: 'INTACT', sealNumber: '', businessQualified: true })
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  Object.assign(form, row)
  dialogVisible.value = true
}

const handleSubmit = async () => {
  await formRef.value.validate()
  try {
    if (isEdit.value) { await updateScale(form.id, form); ElMessage.success('更新成功') }
    else { await createScale(form); ElMessage.success('登记成功') }
    dialogVisible.value = false; loadList()
  } catch (e) { console.error(e) }
}

const handleBind = (row) => {
  currentId.value = row.id
  bindForm.stallId = row.stallId
  bindDialogVisible.value = true
}

const submitBind = async () => {
  try {
    await bindScaleStall(currentId.value, bindForm.stallId)
    ElMessage.success('绑定成功')
    bindDialogVisible.value = false
    loadList()
  } catch (e) { console.error(e) }
}

const handleToggleQualified = async (row) => {
  try {
    await ElMessageBox.confirm(`确定要${row.businessQualified ? '取消' : '恢复'}该秤具的营业资格吗？`, '提示', { type: 'warning' })
    await updateScaleQualified(row.id, !row.businessQualified)
    ElMessage.success('操作成功'); loadList()
  } catch (e) { if (e !== 'cancel') console.error(e) }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`确定要删除秤具 ${row.scaleCode} 吗？`, '提示', { type: 'warning' })
    await deleteScale(row.id); ElMessage.success('删除成功'); loadList()
  } catch (e) { if (e !== 'cancel') console.error(e) }
}

const scaleStatusTagType = (s) => ({
  NORMAL: 'success', NEEDS_RECTIFICATION: 'warning', DISABLED: 'danger',
  CALIBRATION_EXPIRED: 'danger', BORROWED: 'info'
}[s] || 'info')
const scaleStatusText = (s) => ({
  NORMAL: '正常', NEEDS_RECTIFICATION: '限期整改', DISABLED: '停用',
  CALIBRATION_EXPIRED: '校准过期', BORROWED: '已借出'
}[s] || s)
const sealTagType = (s) => ({ INTACT: 'success', DAMAGED: 'warning', MISSING: 'danger' }[s] || 'info')
const sealText = (s) => ({ INTACT: '完好', DAMAGED: '损坏', MISSING: '缺失' }[s] || s)

const scaleUsageVisible = ref(false)
const scaleUsageInfo = ref({})

const handleViewUsage = async (row) => {
  if (!row.stallId) { ElMessage.warning('该秤具未绑定摊位'); return }
  try {
    scaleUsageInfo.value = await getStallScaleUsage(row.stallId) || {}
    scaleUsageVisible.value = true
  } catch (e) { console.error(e) }
}

const borrowContextLabel = (c) => ({
  RECTIFICATION_REPLACEMENT: '整改替代', CALIBRATION_REPLACEMENT: '校准替代',
  REINSPECTION_REPLACEMENT: '复检替代', TEMPORARY: '临时借用'
}[c] || '临时借用')

const scaleStatusLabel = (s) => ({
  NORMAL: '正常', NEEDS_RECTIFICATION: '限期整改', DISABLED: '停用',
  CALIBRATION_EXPIRED: '校准过期', BORROWED: '已借出'
}[s] || s || '')

onMounted(loadList)
</script>
