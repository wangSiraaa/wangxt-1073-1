<template>
  <div class="page-container">
    <div class="page-header">
      <div class="page-title">摊位管理</div>
      <el-button type="primary" @click="handleAdd"><el-icon><Plus /></el-icon>新增摊位</el-button>
    </div>

    <el-table :data="list" v-loading="loading" border stripe>
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="stallCode" label="摊位编号" width="120" />
      <el-table-column prop="stallName" label="摊位名称" width="140" />
      <el-table-column prop="location" label="位置" />
      <el-table-column prop="ownerName" label="摊主" width="100" />
      <el-table-column prop="ownerPhone" label="联系电话" width="130" />
      <el-table-column prop="businessLicense" label="营业执照号" width="160" />
      <el-table-column label="营业资格" width="100">
        <template #default="scope">
          <el-tag :type="scope.row.businessQualified ? 'success' : 'danger'" size="small">
            {{ scope.row.businessQualified ? '有效' : '无效' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="100">
        <template #default="scope">
          <el-tag :type="stallStatusTagType(scope.row.status)" size="small">
            {{ stallStatusText(scope.row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="创建时间" width="170" />
      <el-table-column label="操作" width="240" fixed="right">
        <template #default="scope">
          <el-button size="small" link type="primary" @click="handleEdit(scope.row)">编辑</el-button>
          <el-button size="small" link :type="scope.row.businessQualified ? 'warning' : 'success'"
                     @click="handleToggleQualified(scope.row)">
            {{ scope.row.businessQualified ? '取消资格' : '恢复资格' }}
          </el-button>
          <el-button size="small" link type="danger" @click="handleDelete(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑摊位' : '新增摊位'" width="520px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="摊位编号" prop="stallCode">
          <el-input v-model="form.stallCode" />
        </el-form-item>
        <el-form-item label="摊位名称" prop="stallName">
          <el-input v-model="form.stallName" />
        </el-form-item>
        <el-form-item label="位置">
          <el-input v-model="form.location" />
        </el-form-item>
        <el-form-item label="摊主" prop="ownerName">
          <el-input v-model="form.ownerName" />
        </el-form-item>
        <el-form-item label="联系电话">
          <el-input v-model="form.ownerPhone" />
        </el-form-item>
        <el-form-item label="营业执照号">
          <el-input v-model="form.businessLicense" />
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
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getStalls, createStall, updateStall, deleteStall, updateStallQualified } from '../api/stall'

const loading = ref(false)
const list = ref([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref(null)
const form = reactive({
  id: null, stallCode: '', stallName: '', location: '',
  ownerName: '', ownerPhone: '', businessLicense: '', businessQualified: true
})
const rules = {
  stallCode: [{ required: true, message: '请输入摊位编号', trigger: 'blur' }],
  stallName: [{ required: true, message: '请输入摊位名称', trigger: 'blur' }],
  ownerName: [{ required: true, message: '请输入摊主姓名', trigger: 'blur' }]
}

const loadList = async () => {
  loading.value = true
  try {
    list.value = await getStalls() || []
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  isEdit.value = false
  Object.assign(form, { id: null, stallCode: '', stallName: '', location: '', ownerName: '', ownerPhone: '', businessLicense: '', businessQualified: true })
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
    if (isEdit.value) {
      await updateStall(form.id, form)
      ElMessage.success('更新成功')
    } else {
      await createStall(form)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    loadList()
  } catch (e) {
    console.error(e)
  }
}

const handleToggleQualified = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要${row.businessQualified ? '取消' : '恢复'}该摊位的营业资格吗？`,
      '提示', { type: 'warning' }
    )
    await updateStallQualified(row.id, !row.businessQualified)
    ElMessage.success('操作成功')
    loadList()
  } catch (e) {
    if (e !== 'cancel') console.error(e)
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`确定要删除摊位 ${row.stallCode} 吗？`, '提示', { type: 'warning' })
    await deleteStall(row.id)
    ElMessage.success('删除成功')
    loadList()
  } catch (e) {
    if (e !== 'cancel') console.error(e)
  }
}

const stallStatusTagType = (s) => ({ OPEN: 'success', SUSPENDED: 'danger', CLOSED: 'info' }[s] || 'info')
const stallStatusText = (s) => ({ OPEN: '营业中', SUSPENDED: '已暂停', CLOSED: '已关闭' }[s] || s)

onMounted(loadList)
</script>
