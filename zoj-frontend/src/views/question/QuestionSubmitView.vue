<template>
  <div class="question-submit-view">
    <h1>题目提交记录</h1>
    <a-card>
      <!-- 搜索表单 -->
      <a-form :model="searchForm" layout="inline" @submit="handleSearch">
        <a-form-item field="questionId" label="题目ID">
          <a-input v-model="searchForm.questionId" placeholder="请输入题目ID" allow-clear />
        </a-form-item>

        <a-form-item field="userId" label="用户ID">
          <a-input v-model="searchForm.userId" placeholder="请输入用户ID" allow-clear />
        </a-form-item>

        <a-form-item field="language" label="编程语言">
          <a-select
            v-model="searchForm.language"
            :style="{ width: '150px' }"
            placeholder="请选择编程语言"
            allow-clear
          >
            <a-option value="java">Java</a-option>
            <a-option value="cpp">C++</a-option>
            <a-option value="javascript">JavaScript</a-option>
            <a-option value="python">Python</a-option>
          </a-select>
        </a-form-item>

        <a-form-item>
          <a-button type="primary" html-type="submit">搜索</a-button>
          <a-button style="margin-left: 16px" @click="resetSearch">重置</a-button>
        </a-form-item>
      </a-form>
    </a-card>

    <!-- 提交记录表格 -->
    <a-card style="margin-top: 20px">
      <a-table
        :data="submitList"
        :pagination="tablePagination"
        :loading="loading"
        @page-change="handlePageChange"
        @page-size-change="handlePageSizeChange"
      >
        <template #columns>
          <a-table-column title="ID" data-index="id" :width="100" />
          <a-table-column title="题目ID" data-index="questionId" :width="100" />
          <a-table-column title="编程语言" data-index="language" :width="120" />
          <a-table-column title="用户ID" data-index="userId" :width="120" />
          <a-table-column title="判题信息" :width="200">
            <template #cell="{ record }">
              <div class="judge-info">
                <div>状态: {{ record.status ? '已完成' : '等待中' }}</div>
                <div v-if="record.judgeInfo">信息: {{ record.judgeInfo.message }}</div>
              </div>
            </template>
          </a-table-column>
          <a-table-column title="创建时间" :width="200">
            <template #cell="{ record }">
              {{ formatTime(record.createTime) }}
            </template>
          </a-table-column>
          <a-table-column title="操作" :width="100" fixed="right">
            <template #cell="{ record }">
              <a-button type="text" size="small" @click="viewSubmitDetail(record)"> 查看 </a-button>
            </template>
          </a-table-column>
        </template>
      </a-table>
    </a-card>

    <!-- 判题详情弹窗 -->
    <a-modal
      v-model:visible="detailModalVisible"
      :title="currentSubmitRecord ? '提交详情 - ' + currentSubmitRecord.id : '提交详情'"
      :width="800"
      @close="closeDetailModal"
    >
      <a-spin :loading="detailLoading">
        <a-descriptions
          v-if="currentSubmitRecord"
          :column="1"
          size="medium"
          :title="`题目: ${currentSubmitRecord.questionVO?.title || '未知题目'}`"
        >
          <a-descriptions-item label="提交ID">
            {{ currentSubmitRecord.id }}
          </a-descriptions-item>
          <a-descriptions-item label="编程语言">
            {{ currentSubmitRecord.language }}
          </a-descriptions-item>
          <a-descriptions-item label="提交时间">
            {{ formatTime(currentSubmitRecord.createTime) }}
          </a-descriptions-item>
          <a-descriptions-item label="判题状态">
            <a-tag :color="getJudgeStatusColor(getJudgeStatus(currentSubmitRecord))">
              {{ getJudgeStatusText(currentSubmitRecord) }}
            </a-tag>
          </a-descriptions-item>
          <a-descriptions-item label="时间消耗">
            {{ getJudgeTime(currentSubmitRecord) }} ms
          </a-descriptions-item>
          <a-descriptions-item label="内存消耗">
            {{ getJudgeMemory(currentSubmitRecord) }} KB
          </a-descriptions-item>
          <a-descriptions-item label="题目时间限制">
            {{ currentSubmitRecord.questionVO?.judgeConfig?.timeLimit || 'N/A' }} ms
          </a-descriptions-item>
          <a-descriptions-item label="题目内存限制">
            {{ currentSubmitRecord.questionVO?.judgeConfig?.memoryLimit || 'N/A' }} KB
          </a-descriptions-item>
        </a-descriptions>

        <a-card v-if="currentSubmitRecord" title="代码" style="margin-top: 20px">
          <pre
            style="
              max-height: 300px;
              overflow: auto;
              background-color: #f5f5f5;
              padding: 10px;
              border-radius: 4px;
            "
            >{{ currentSubmitRecord.code }}</pre
          >
        </a-card>
      </a-spin>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { Message } from '@arco-design/web-vue'
import {
  listQuestionSubmitVOByPage,
  getQuestionSubmitVOById,
} from '@/api/questionSubmit/questionSubmitApi'

// 使用路由
const router = useRouter()

// 搜索表单数据
const searchForm = reactive({
  questionId: '',
  userId: '',
  language: '',
})

// 提交记录列表
const submitList = ref<any[]>([])

// 加载状态
const loading = ref(false)

// 详情弹窗可见性
const detailModalVisible = ref(false)

// 详情加载状态
const detailLoading = ref(false)

// 当前查看的提交记录
const currentSubmitRecord = ref<any>(null)

// 分页配置
const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showTotal: true,
  showJumper: true,
  showPageSize: true,
})

// 计算属性，确保total是数字类型
const tablePagination = computed(() => {
  return {
    ...pagination,
    total: Number(pagination.total),
  }
})

// 格式化时间
const formatTime = (time: string | Date | undefined) => {
  if (!time) return ''
  const date = new Date(time)
  return date.toLocaleString()
}

// 获取判题状态（处理不同的数据结构）
const getJudgeStatus = (record: any) => {
  console.log('判题信息原始数据:', record.judgeInfo)

  // 如果judgeInfo是字符串，尝试解析
  if (typeof record.judgeInfo === 'string') {
    try {
      const parsed = JSON.parse(record.judgeInfo)
      return parsed.status || record.status || 0
    } catch (e) {
      return record.status || 0
    }
  }

  // 如果judgeInfo是对象
  if (typeof record.judgeInfo === 'object' && record.judgeInfo !== null) {
    return record.judgeInfo.status || record.status || 0
  }

  // 直接使用status字段
  return record.status || 0
}

// 获取判题状态文本
const getJudgeStatusText = (record: any) => {
  const status = getJudgeStatus(record)
  const statusMap = {
    0: '等待中',
    1: '判题中',
    2: '成功',
    3: '失败',
  }
  return statusMap[status] || '未知状态'
}

// 获取判题时间
const getJudgeTime = (record: any) => {
  if (typeof record.judgeInfo === 'string') {
    try {
      const parsed = JSON.parse(record.judgeInfo)
      return parsed.time || 'N/A'
    } catch (e) {
      return 'N/A'
    }
  }

  if (typeof record.judgeInfo === 'object' && record.judgeInfo !== null) {
    return record.judgeInfo.time || 'N/A'
  }

  return 'N/A'
}

// 获取判题内存
const getJudgeMemory = (record: any) => {
  if (typeof record.judgeInfo === 'string') {
    try {
      const parsed = JSON.parse(record.judgeInfo)
      return parsed.memory || 'N/A'
    } catch (e) {
      return 'N/A'
    }
  }

  if (typeof record.judgeInfo === 'object' && record.judgeInfo !== null) {
    return record.judgeInfo.memory || 'N/A'
  }

  return 'N/A'
}

// 获取判题状态对应的颜色
const getJudgeStatusColor = (status: number | undefined) => {
  switch (status) {
    case 0: // 等待中
      return 'gray'
    case 1: // 判题中
      return 'blue'
    case 2: // 成功
      return 'green'
    case 3: // 失败
      return 'red'
    default:
      return 'gray'
  }
}

// 查看提交详情
const viewSubmitDetail = async (record: any) => {
  console.log('提交记录详情:', record)
  console.log('代码字段:', record.code)
  console.log('判题信息:', record.judgeInfo)

  detailLoading.value = true
  try {
    // 重新请求详情接口获取完整信息
    const res = await getQuestionSubmitVOById(record.id)
    if (res.code === 0) {
      currentSubmitRecord.value = res.data
      console.log('详情接口返回的数据:', res.data)
      console.log('详情接口返回的代码:', res.data.code)
    } else {
      Message.error(res.message || '获取提交详情失败')
      return
    }
  } catch (err) {
    Message.error('获取提交详情失败，请稍后重试')
    console.error('获取提交详情失败:', err)
    return
  } finally {
    detailLoading.value = false
  }

  detailModalVisible.value = true
}

// 关闭详情弹窗
const closeDetailModal = () => {
  detailModalVisible.value = false
  currentSubmitRecord.value = null
}

// 重置搜索
const resetSearch = () => {
  searchForm.questionId = ''
  searchForm.userId = ''
  searchForm.language = ''
  pagination.current = 1
  fetchSubmitList()
}

// 处理搜索
const handleSearch = () => {
  pagination.current = 1
  fetchSubmitList()
}

// 处理页码变化
const handlePageChange = (current: number) => {
  pagination.current = current
  fetchSubmitList()
}

// 处理页面大小变化
const handlePageSizeChange = (pageSize: number) => {
  pagination.pageSize = pageSize
  pagination.current = 1
  fetchSubmitList()
}

// 获取提交记录列表
const fetchSubmitList = async () => {
  loading.value = true
  try {
    const res = await listQuestionSubmitVOByPage({
      questionId: searchForm.questionId ? Number(searchForm.questionId) : undefined,
      userId: searchForm.userId ? Number(searchForm.userId) : undefined,
      language: searchForm.language || undefined,
      current: pagination.current,
      pageSize: pagination.pageSize,
    })

    if (res.code === 0) {
      submitList.value = res.data.records
      pagination.total = res.data.total
    } else {
      Message.error(res.message || '获取提交记录失败')
    }
  } catch (err) {
    Message.error('获取提交记录失败，请稍后重试')
    console.error('获取提交记录失败:', err)
  } finally {
    loading.value = false
  }
}

// 页面加载时获取数据
onMounted(() => {
  fetchSubmitList()
})
</script>

<style scoped>
.question-submit-view {
  padding: 20px;
  background-color: #ffffff;
  color: #000000;
  border: none;
  min-height: calc(100vh - 64px);
}

.question-submit-view h1 {
  margin-bottom: 20px;
  text-align: center;
}

.judge-info {
  font-size: 12px;
}
</style>
