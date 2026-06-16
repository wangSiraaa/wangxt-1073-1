<template>
  <div class="page-container">
    <div class="page-header">
      <div class="page-title">校准管理</div>
      <el-button type="primary" @click="handleAdd"><el-icon><Plus /></el-icon>上传校准结果</el-button>
    </div>

    <el-table :data="list" v-loading="loading" border stripe>
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="scaleId" label="秤具ID" width="80" />
      <el-table-column prop="calibrationDate" label="校准日期" width="120" />
      <el-table-column prop="metrologistName" label="计量员" width="100" />
      <el-table-column label="校准结果" width="120">
        <template #default="scope">
          <el-tag :type="resultTagType(scope.row.result)" size="small">
            {{ resultText(scope.row.result) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="nextCalibrationDate" label="下次校准日期" width="130" />
      <el-table-column prop="errorValue" label="误差值" width="100" />
      <el-table-column prop="temperature" label="温度" width="80" />
      <el-table-column prop="humidity" label="湿度" width="80" />
      <el-table-column prop="rectificationDeadline" label="整改期限" width="120" />
      <el-table-column prop="remark" label="备注" />
      <el-table-column prop="createdAt" label="上传时间" width="170" />
    </el-table>

    <el-dialog v-model="dialogVisible" title="上传校准结果" width="560px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="110px">
        <el-form-item label="秤具ID" prop="scaleId">
          <el-input v-model.number="form.scaleId" type="number" />
        </el-form-item>
        <el-form-item label="校准日期" prop="calibrationDate">
          <el-date-picker v-model="form.calibrationDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
        </el-form-item>
        <el-form-item label="计量员ID">
          <el-input v-model.number="form.metrologistId" type="number" />
        </el-form-item>
        <el-form-item label="计量员姓名">
          <el-input v-model="form.metrologistName" />
        </el-form-item>
        <el-form-item label="校准结果" prop="result">
          <el-select v-model="form.result" style="width: 100%">
            <el-option label="通过" value="PASS" />
            <el-option label="限期整改" value="RECTIFICATION_REQUIRED" />
            <el-option label="不合格(停用)" value="DISQUALIFIED" />
          </el-select>
        </el-form-item>
        <el-form-item label="下次校准日期">
          <el-date-picker v-model="form.nextCalibrationDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
        </el-form-item>
        <el-form-item label="误差值">
          <el-input v-model="form.errorValue" placeholder="例如: +2g" />
        </el-form-item>
        <el-form-item label="温度">
          <el-input v-model="form.temperature" placeholder="例如: 20°C" />
        </el-form-item>
        <el-form-item label="湿度">
          <el-input v-model="form.humidity" placeholder="例如: 50%RH" />
        </el-form-item>
        <el-form-item label="整改期限" v-if="form.result === 'RECTIFICATION_REQUIRED'">
          <el-date-picker v-model="form.rectificationDeadline" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getCalibrations, createCalibration } from '../api/calibration'

const loading = ref(false)
const list = ref([])
const dialogVisible = ref(false)
const formRef = ref(null)
const form = reactive({
  scaleId: null, calibrationDate: '', metrologistId: null, metrologistName: '',
  result: 'PASS', nextCalibrationDate: '', errorValue: '', temperature: '',
  humidity: '', rectificationDeadline: '', remark: ''
})
const rules = {
  scaleId: [{ required: true, message: '请输入秤具ID', trigger: 'blur' }],
  calibrationDate: [{ required: true, message: '请选择校准日期', trigger: 'change' }],
  result: [{ required: true, message: '请选择校准结果', trigger: 'change' }]
}

const loadList = async () => {
  loading.value = true
  try { list.value = await getCalibrations() || [] } finally { loading.value = false }
}

const handleAdd = () => {
  Object.assign(form, {
    scaleId: null, calibrationDate: '', metrologistId: null, metrologistName: '',
    result: 'PASS', nextCalibrationDate: '', errorValue: '', temperature: '',
    humidity: '', rectificationDeadline: '', remark: ''
  })
  dialogVisible.value = true
}

const handleSubmit = async () => {
  await formRef.value.validate()
  try {
    await createCalibration(form)
    ElMessage.success('上传成功')
    dialogVisible.value = false
    loadList()
  } catch (e) { console.error(e) }
}

const resultTagType = (r) => ({ PASS: 'success', RECTIFICATION_REQUIRED: 'warning', DISQUALIFIED: 'danger' }[r] || 'info')
const resultText = (r) => ({ PASS: '通过', RECTIFICATION_REQUIRED: '限期整改', DISQUALIFIED: '不合格' }[r] || r)

onMounted(loadList)
</script>
