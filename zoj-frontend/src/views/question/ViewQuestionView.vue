<template>
  <div class="view-question-view">
    <a-spin :loading="loading" style="width: 100%">
      <a-row :gutter="20" v-if="question">
        <!-- 左侧题目信息 -->
        <a-col :span="12">
          <a-card class="question-card">
            <template #title>
              <div class="question-title">
                {{ question.title }}
              </div>
            </template>
            <div class="question-content">
              <MdViewer :value="question.content" />
            </div>
            <div class="question-tags">
              <a-tag v-for="(tag, index) in question.tags" :key="index" color="arcoblue">
                {{ tag }}
              </a-tag>
            </div>

            <!-- 评论区域 -->
            <div class="comments-section">
              <h3>评论</h3>
              <div class="comment-form">
                <a-textarea
                  v-model="commentContent"
                  placeholder="请输入您的评论..."
                  :auto-size="{ minRows: 2, maxRows: 4 }"
                />
                <div class="comment-actions">
                  <a-button type="primary" @click="submitComment" :disabled="!commentContent"
                    >发表评论</a-button
                  >
                </div>
              </div>
              <div class="comments-list">
                <div class="comment-item" v-for="comment in comments" :key="comment.id">
                  <div class="comment-header">
                    <span class="comment-author">{{ comment.author }}</span>
                    <span class="comment-time">{{ formatTime(comment.createTime) }}</span>
                  </div>
                  <div class="comment-content">
                    {{ comment.content }}
                  </div>
                </div>
                <div class="no-comments" v-if="comments.length === 0">暂无评论</div>
              </div>
            </div>
          </a-card>
        </a-col>

        <!-- 右侧代码编辑器 -->
        <a-col :span="12">
          <a-card class="code-card">
            <template #title>
              <div class="code-title">代码编辑</div>
            </template>
            <div class="code-editor-container">
              <div class="language-selector">
                <a-select
                  v-model="selectedLanguage"
                  :style="{ width: '150px' }"
                  placeholder="选择编程语言"
                >
                  <a-option value="java">Java</a-option>
                  <a-option value="cpp">C++</a-option>
                  <a-option value="javascript">JavaScript</a-option>
                  <a-option value="python">Python</a-option>
                </a-select>
              </div>

              <div class="code-editor">
                <MonacoEditor
                  v-model="code"
                  :language="languageMap[selectedLanguage]"
                  :height="400"
                />
              </div>

              <div class="submit-button-container">
                <a-space>
                  <a-button @click="saveCodeManually" :disabled="!code.trim()"> 保存代码 </a-button>
                  <a-button type="primary" @click="submitCode" :loading="submitting">
                    提交代码
                  </a-button>
                </a-space>
              </div>
            </div>
          </a-card>
        </a-col>
      </a-row>

      <a-card v-else-if="!loading" class="question-not-found">
        <a-empty>
          <template #image>
            <icon-exclamation-circle-fill />
          </template>
          <div>题目不存在或已被删除</div>
          <a-button type="primary" @click="router.push('/questions')" style="margin-top: 16px">
            返回题目列表
          </a-button>
        </a-empty>
      </a-card>
    </a-spin>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Message } from '@arco-design/web-vue'
import { IconExclamationCircleFill } from '@arco-design/web-vue/es/icon'
import { getQuestionVOById } from '@/api/question/questionApi'
import { doQuestionSubmit } from '@/api/questionSubmit/questionSubmitApi'
import type { QuestionVO } from '@/api/question/type'
import MonacoEditor from '@/components/MonacoEditor.vue'
import MdEditor from '@/components/MdEditor.vue'
import MdViewer from '@/components/MdViewer.vue'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

// 数据相关
const question = ref<QuestionVO | null>(null)
const loading = ref<boolean>(false)
const submitting = ref<boolean>(false)

// 代码相关
const code = ref<string>('')
const selectedLanguage = ref<string>('java')
const languageMap = {
  java: 'java',
  cpp: 'cpp',
  javascript: 'javascript',
  python: 'python',
}

// 代码存储的key，基于题目ID和语言
const getStorageKey = (questionId: string, language: string) => {
  return `question_code_${questionId}_${language}`
}

// 保存代码到本地存储
const saveCodeToStorage = (questionId: string, language: string, codeContent: string) => {
  try {
    localStorage.setItem(getStorageKey(questionId, language), codeContent)
  } catch (error) {
    console.warn('保存代码到本地存储失败:', error)
  }
}

// 从本地存储加载代码
const loadCodeFromStorage = (questionId: string, language: string): string => {
  try {
    return localStorage.getItem(getStorageKey(questionId, language)) || ''
  } catch (error) {
    console.warn('从本地存储加载代码失败:', error)
    return ''
  }
}

// 评论相关
const commentContent = ref<string>('')
const comments = ref<Array<any>>([])

/**
 * 加载题目详情
 */
const loadQuestion = async () => {
  const questionId = route.params.id as string

  console.log('接收到的题目ID:', questionId)

  if (!questionId) {
    Message.error('题目不存在')
    router.push('/questions')
    return
  }

  loading.value = true
  try {
    // 直接使用字符串ID，不需要转换
    const res = await getQuestionVOById(questionId)
    console.log('获取题目详情响应:', res)

    if (res.code === 0) {
      question.value = res.data
      console.log('题目详情:', question.value)
      // 设置默认代码模板（会优先从本地存储加载）
      setDefaultCodeTemplate()
    } else {
      Message.error(res.message || '获取题目详情失败')
      router.push('/questions')
    }
  } catch (error: any) {
    Message.error(error.message || '获取题目详情失败')
    router.push('/questions')
  } finally {
    loading.value = false
  }
}

/**
 * 设置默认代码模板
 */
const setDefaultCodeTemplate = () => {
  if (!question.value?.id) return

  // 先尝试从本地存储加载代码
  const savedCode = loadCodeFromStorage(question.value.id.toString(), selectedLanguage.value)
  if (savedCode) {
    code.value = savedCode
    return
  }

  // 如果没有保存的代码，使用默认模板
  const templates: Record<string, string> = {
    java: `public class Main {
    public static void main(String[] args) {
        // 在这里编写你的代码
    }
}`,
    cpp: `#include <iostream>
using namespace std;

int main() {
    // 在这里编写你的代码
    return 0;
}`,
    javascript: `// 在这里编写你的代码
console.log("Hello World");`,
    python: `# 在这里编写你的代码
print("Hello World")`,
  }

  code.value = templates[selectedLanguage.value] || ''
}

/**
 * 手动保存代码
 */
const saveCodeManually = () => {
  if (!question.value?.id) {
    Message.warning('题目信息不存在')
    return
  }

  if (!code.value.trim()) {
    Message.warning('请输入代码')
    return
  }

  saveCodeToStorage(question.value.id.toString(), selectedLanguage.value, code.value)
  Message.success('代码已保存')
}

/**
 * 提交代码
 */
const submitCode = async () => {
  if (!question.value) {
    Message.error('题目信息不存在，请重新进入页面')
    return
  }

  if (!code.value.trim()) {
    Message.warning('请输入代码')
    return
  }

  // 检查用户是否已登录
  if (!userStore.isLoggedIn) {
    Message.warning('请先登录后再提交代码')
    // 跳转到登录页面，登录后返回当前页面
    router.push({
      path: '/user/login',
      query: { redirect: route.fullPath },
    })
    return
  }

  console.log('准备提交代码，题目ID:', question.value.id)

  submitting.value = true
  try {
    const res = await doQuestionSubmit({
      questionId: question.value.id!.toString(), // 确保以字符串形式传递，避免JavaScript大整数精度丢失
      language: selectedLanguage.value,
      code: code.value,
    })

    console.log('提交代码响应:', res)

    if (res.code === 0) {
      Message.success('代码提交成功')
      // 清除本地存储的代码
      if (question.value?.id) {
        localStorage.removeItem(getStorageKey(question.value.id.toString(), selectedLanguage.value))
      }
      // 跳转到提交状态页面
      router.push(`/question_submit`)
    } else {
      Message.error('提交失败: ' + res.message)
    }
  } catch (error: any) {
    Message.error('提交失败: ' + error.message)
  } finally {
    submitting.value = false
  }
}

/**
 * 提交评论
 */
const submitComment = () => {
  if (!commentContent.value.trim()) {
    Message.warning('请输入评论内容')
    return
  }

  // 这里应该是调用API提交评论的逻辑
  // 暂时只是在前端模拟添加评论
  const newComment = {
    id: Date.now(),
    author: '当前用户',
    content: commentContent.value,
    createTime: new Date(),
  }

  comments.value.unshift(newComment)
  commentContent.value = ''
  Message.success('评论发表成功')
}

/**
 * 格式化时间
 */
const formatTime = (time: any) => {
  if (!time) return ''
  const date = new Date(time)
  return date.toLocaleString()
}

// 监听语言变化，更新代码模板
watch(selectedLanguage, () => {
  setDefaultCodeTemplate()
})

// 监听代码变化，自动保存
watch(
  code,
  (newCode) => {
    if (question.value?.id && newCode) {
      saveCodeToStorage(question.value.id.toString(), selectedLanguage.value, newCode)
    }
  },
  { deep: true },
)

onMounted(() => {
  loadQuestion()
})
</script>

<style scoped>
.view-question-view {
  padding: 20px;
  max-width: 1400px;
  margin: 0 auto;
}

.question-title {
  font-size: 24px;
  font-weight: bold;
}

.question-content {
  margin: 16px 0;
}

.question-tags {
  margin: 16px 0;
}

.code-title {
  font-size: 18px;
  font-weight: bold;
}

.language-selector {
  margin-bottom: 16px;
}

.code-editor {
  margin-bottom: 16px;
}

.submit-button-container {
  text-align: center;
}

.comments-section {
  margin-top: 30px;
  padding-top: 20px;
  border-top: 1px solid #e5e5e5;
}

.comment-form {
  margin-bottom: 20px;
}

.comment-actions {
  margin-top: 10px;
  text-align: right;
}

.comment-item {
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
}

.comment-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
  font-size: 14px;
  color: #666;
}

.comment-author {
  font-weight: bold;
}

.comment-time {
  color: #999;
}

.comment-content {
  font-size: 14px;
  line-height: 1.5;
}

.no-comments {
  text-align: center;
  color: #999;
  padding: 20px 0;
}
</style>
