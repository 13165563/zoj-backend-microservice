<template>
  <div class="manage-question-view">
    <h1>管理题目</h1>
    <a-card>
      <template #extra>
        <a-button type="primary" @click="goToAddQuestion">
          <template #icon>
            <icon-plus />
          </template>
          <template #default>创建题目</template>
        </a-button>
      </template>
      
      <!-- 搜索表单 -->
      <a-form 
        :model="searchForm" 
        layout="inline"
        @submit="handleSearch"
      >
        <a-form-item field="title" label="题目标题">
          <a-input 
            v-model="searchForm.title" 
            placeholder="请输入题目标题" 
            allow-clear
          />
        </a-form-item>
        
        <a-form-item field="userId" label="用户ID">
          <a-input-number 
            v-model="searchForm.userId" 
            placeholder="请输入用户ID" 
            mode="button"
            :min="1"
          />
        </a-form-item>
        
        <a-form-item>
          <a-button type="primary" html-type="submit">搜索</a-button>
          <a-button style="margin-left: 16px" @click="resetSearch">重置</a-button>
        </a-form-item>
      </a-form>
    </a-card>
    
    <!-- 题目表格 -->
    <a-card style="margin-top: 20px">
      <a-table 
        :data="questionList"
        :pagination="tablePagination"
        :loading="loading"
        @page-change="handlePageChange"
        @page-size-change="handlePageSizeChange"
      >
        <template #columns>
          <a-table-column title="ID" data-index="id" :width="100" />
          <a-table-column title="题目标题" data-index="title" ellipsis tooltip />
          <a-table-column title="内容" :width="200">
            <template #cell="{ record }">
              <div class="content-preview">{{ getContentPreview(record.content) }}</div>
            </template>
          </a-table-column>
          <a-table-column title="标签" :width="200">
            <template #cell="{ record }">
              <a-space wrap>
                <a-tag 
                  v-for="(tag, index) in record.tags" 
                  :key="index" 
                  color="arcoblue"
                >
                  {{ tag }}
                </a-tag>
              </a-space>
            </template>
          </a-table-column>
          <a-table-column title="答案" :width="150">
            <template #cell="{ record }">
              <div class="answer-preview">{{ getAnswerPreview(record.answer) }}</div>
            </template>
          </a-table-column>
          <a-table-column title="提交数" data-index="submitNum" :width="100" />
          <a-table-column title="通过数" data-index="acceptedNum" :width="100" />
          <a-table-column title="判题配置" :width="150">
            <template #cell="{ record }">
              <div class="config-preview">{{ getJudgeConfigPreview(record.judgeConfig) }}</div>
            </template>
          </a-table-column>
          <a-table-column title="判题用例" :width="150">
            <template #cell="{ record }">
              <div class="case-preview">{{ getJudgeCasePreview(record.judgeCase) }}</div>
            </template>
          </a-table-column>
          <a-table-column title="用户ID" data-index="userId" :width="120" />
          <a-table-column title="创建时间" :width="200">
            <template #cell="{ record }">
              {{ formatTime(record.createTime) }}
            </template>
          </a-table-column>
          <a-table-column title="操作" :width="200" fixed="right">
            <template #cell="{ record }">
              <a-space>
                <a-button 
                  type="primary" 
                  size="small" 
                  @click="goToUpdateQuestion(record)"
                >
                  修改
                </a-button>
                <a-button 
                  type="outline" 
                  status="danger" 
                  size="small" 
                  @click="confirmDeleteQuestion(record.id)"
                >
                  删除
                </a-button>
              </a-space>
            </template>
          </a-table-column>
        </template>
      </a-table>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { Message, Modal } from '@arco-design/web-vue'
import { listQuestionByPage, deleteQuestion } from '@/api/question/questionApi'

// 使用路由
const router = useRouter()

// 搜索表单数据
const searchForm = reactive({
  title: '',
  userId: undefined as number | undefined
})

// 题目列表
const questionList = ref<any[]>([])

// 加载状态
const loading = ref(false)

// 分页配置
const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showTotal: true,
  showJumper: true,
  showPageSize: true
})

// 计算属性，确保total是数字类型
const tablePagination = computed(() => {
  return {
    ...pagination,
    total: Number(pagination.total)
  }
})

// 获取内容预览
const getContentPreview = (content: string) => {
  if (!content) return '无内容'
  // 提取前50个字符作为预览
  return content.length > 50 ? content.substring(0, 50) + '...' : content
}

// 获取答案预览
const getAnswerPreview = (answer: string) => {
  if (!answer) return '无答案'
  // 提取前30个字符作为预览
  return answer.length > 30 ? answer.substring(0, 30) + '...' : answer
}

// 获取判题配置预览
const getJudgeConfigPreview = (config: string) => {
  if (!config) return '无配置'
  try {
    const parsed = JSON.parse(config)
    return `时间: ${parsed.timeLimit || '无'}ms, 内存: ${parsed.memoryLimit || '无'}KB`
  } catch (e) {
    return '配置错误'
  }
}

// 获取判题用例预览
const getJudgeCasePreview = (cases: string) => {
  if (!cases) return '无用例'
  try {
    const parsed = JSON.parse(cases)
    if (Array.isArray(parsed)) {
      return `共 ${parsed.length} 个用例`
    }
    return '格式错误'
  } catch (e) {
    return '用例错误'
  }
}

// 格式化时间
const formatTime = (time: string | Date | undefined) => {
  if (!time) return ''
  const date = new Date(time)
  return date.toLocaleString()
}

// 跳转到创建题目页面
const goToAddQuestion = () => {
  router.push('/add/question')
}

// 跳转到更新题目页面
const goToUpdateQuestion = (record: any) => {
  router.push({
    path: '/update/question',
    query: { id: record.id }
  })
}

// 删除题目
const handleDeleteQuestion = async (id: string | number) => {
  try {
    const res: any = await deleteQuestion(id)
    if (res.code === 0) {
      Message.success('删除成功')
      // 重新加载数据
      await fetchQuestionList()
    } else {
      Message.error(res.message || '删除失败')
    }
  } catch (err) {
    Message.error('删除失败，请稍后重试')
    console.error('删除失败:', err)
  }
}

// 删除题目确认
const confirmDeleteQuestion = (id: string | number) => {
  Modal.warning({
    title: '确认删除',
    content: '确认删除该题目吗？此操作不可恢复',
    okText: '确认',
    cancelText: '取消',
    onOk: () => handleDeleteQuestion(id)
  })
}

// 重置搜索
const resetSearch = () => {
  searchForm.title = ''
  searchForm.userId = undefined
  pagination.current = 1
  fetchQuestionList()
}

// 处理搜索
const handleSearch = () => {
  pagination.current = 1
  fetchQuestionList()
}

// 处理页码变化
const handlePageChange = (current: number) => {
  pagination.current = current
  fetchQuestionList()
}

// 处理页面大小变化
const handlePageSizeChange = (pageSize: number) => {
  pagination.pageSize = pageSize
  pagination.current = 1
  fetchQuestionList()
}

// 获取题目列表
const fetchQuestionList = async () => {
  loading.value = true
  try {
    const res = await listQuestionByPage({
      title: searchForm.title || undefined,
      userId: searchForm.userId,
      current: pagination.current,
      pageSize: pagination.pageSize
    })
    
    if (res.code === 0) {
      // 处理标签数据
      questionList.value = res.data.records.map((question: any) => {
        // 如果标签是字符串，则尝试解析为数组
        if (typeof question.tags === 'string') {
          try {
            const parsedTags = JSON.parse(question.tags);
            if (Array.isArray(parsedTags)) {
              question.tags = parsedTags;
            }
          } catch (e) {
            // 解析失败则保持原样
            question.tags = [];
          }
        }
        return question;
      });
      pagination.total = Number(res.data.total)
    } else {
      Message.error(res.message || '获取题目列表失败')
    }
  } catch (err) {
    Message.error('获取题目列表失败，请稍后重试')
    console.error('获取题目列表失败:', err)
  } finally {
    loading.value = false
  }
}

// 页面加载时获取数据
onMounted(() => {
  fetchQuestionList()
})
</script>

<style scoped>
.manage-question-view {
  padding: 20px;
  background-color: #ffffff;
  color: #000000;
  border: none;
  min-height: calc(100vh - 64px);
}

.manage-question-view h1 {
  margin-bottom: 20px;
  text-align: center;
}

.content-preview,
.answer-preview,
.config-preview,
.case-preview {
  max-width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>