<template>
  <el-dialog v-model="visible" :title="isEdit ? '编辑任务' : '创建任务'" width="640px" @close="onClose">
    <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
      <el-form-item label="任务标题" prop="title">
        <el-input v-model="form.title" placeholder="请输入任务标题" maxlength="100" show-word-limit />
      </el-form-item>
      <el-row :gutter="16">
        <el-col :span="12">
          <el-form-item label="状态" prop="status">
            <el-select v-model="form.status" style="width:100%">
              <el-option v-for="s in STATUS_OPTIONS" :key="s.value" :label="s.label" :value="s.value" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="优先级" prop="priority">
            <el-select v-model="form.priority" style="width:100%">
              <el-option v-for="p in PRIORITY_OPTIONS" :key="p.value" :label="p.label" :value="p.value" />
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>
      <el-row :gutter="16">
        <el-col :span="12">
          <el-form-item label="负责人">
            <el-select v-model="form.assigneeId" placeholder="选择负责人" filterable clearable style="width:100%">
              <el-option v-for="u in users" :key="u.id" :label="`${u.name}（${u.role === 'MENTOR' ? '导师' : '实习生'}）`" :value="u.id" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="截止日期">
            <el-date-picker v-model="form.dueDate" type="datetime" placeholder="选择截止时间" value-format="YYYY-MM-DD HH:mm:ss" style="width:100%" />
          </el-form-item>
        </el-col>
      </el-row>
      <el-form-item label="任务描述">
        <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入任务描述" />
      </el-form-item>
      <el-form-item label="标签">
        <el-input v-model="form.tags" placeholder="多个标签用逗号分隔，如 Java,Spring Boot（用于关联资讯）" />
        <div class="tag-hint">提示：填写技术标签后，任务详情页将自动关联获取相关行业资讯</div>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" :loading="saving" @click="handleSubmit">确定</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { taskApi, userApi } from '../api'
import { STATUS_OPTIONS, PRIORITY_OPTIONS } from '../utils/constants'

const props = defineProps({
  modelValue: Boolean,
  taskId: [Number, String, null]
})
const emit = defineEmits(['update:modelValue', 'success'])

const visible = ref(false)
const isEdit = ref(false)
const saving = ref(false)
const formRef = ref()
const users = ref([])

const form = reactive({
  title: '', description: '', status: 'TODO', priority: 'MEDIUM',
  assigneeId: null, dueDate: '', tags: ''
})

const rules = {
  title: [{ required: true, message: '请输入任务标题', trigger: 'blur' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }],
  priority: [{ required: true, message: '请选择优先级', trigger: 'change' }]
}

watch(() => props.modelValue, async (val) => {
  visible.value = val
  if (val) {
    users.value = await userApi.list()
    if (props.taskId) {
      isEdit.value = true
      const data = await taskApi.getById(props.taskId)
      Object.assign(form, {
        title: data.title, description: data.description, status: data.status,
        priority: data.priority, assigneeId: data.assigneeId,
        dueDate: data.dueDate, tags: data.tags
      })
    } else {
      isEdit.value = false
      Object.assign(form, { title: '', description: '', status: 'TODO', priority: 'MEDIUM', assigneeId: null, dueDate: '', tags: '' })
    }
  }
})
watch(visible, (val) => emit('update:modelValue', val))

async function handleSubmit() {
  await formRef.value.validate()
  saving.value = true
  try {
    if (isEdit.value) {
      await taskApi.update(props.taskId, form)
      ElMessage.success('修改成功')
    } else {
      await taskApi.create(form)
      ElMessage.success('创建成功')
    }
    visible.value = false
    emit('success')
  } finally {
    saving.value = false
  }
}

function onClose() {
  formRef.value?.resetFields()
}
</script>

<style scoped>
.tag-hint { font-size: 12px; color: #909399; margin-top: 4px; }
</style>
