<template>
  <div class="add-question-container">
    <h2>{{ isUpdate ? '更新题目' : '创建题目' }}</h2>
    <a-form :model="form" layout="vertical" @submit="handleSubmit">
      <a-form-item field="title" label="题目标题" required>
        <a-input
          v-model="form.title"
          placeholder="请输入题目标题"
          allow-clear
        />
      </a-form-item>

      <a-form-item field="content" label="题目内容" required>
        <div class="editor-container">
          <MdEditor
            v-model="form.content"
            :height="600"
            mode="split"
          />
        </div>
      </a-form-item>

      <a-form-item field="tags" label="标签">
        <div class="tag-container">
          <a-space wrap>
            <a-tag
              v-for="tag in form.tags"
              :key="tag"
              closable
              @close="removeTag(tag)"
            >
              {{ tag }}
            </a-tag>
          </a-space>
          <a-input-group class="tag-input-group">
            <a-input
              v-model="tagInput"
              placeholder="输入标签后按回车添加"
              @keyup.enter="addTag"
            />
            <a-button type="primary" @click="addTag">添加</a-button>
          </a-input-group>
        </div>
      </a-form-item>

      <a-form-item field="answer" label="答案">
        <MonacoEditor
          :model-value="form.answer"
          @update:model-value="(value) => form.answer = value"
          language="javascript"
          :height="400"
        />
      </a-form-item>

      <a-form-item field="judgeCase" label="判题用例">
        <div class="editor-toggle">
          <a-radio-group v-model="judgeCaseMode" type="button">
            <a-radio value="form">表单模式</a-radio>
            <a-radio value="json">JSON模式</a-radio>
          </a-radio-group>
        </div>

        <!-- 表单模式 -->
        <div v-if="judgeCaseMode === 'form'" class="form-mode">
          <div
            v-for="(item, index) in form.judgeCases"
            :key="index"
            class="judge-case-item"
          >
            <a-card class="case-card">
              <a-form :model="item" layout="vertical">
                <a-row :gutter="16">
                  <a-col :span="11">
                    <a-form-item label="输入" field="input">
                      <a-textarea
                        v-model="item.input"
                        placeholder="输入内容"
                        :auto-size="{ minRows: 2 }"
                      />
                    </a-form-item>
                  </a-col>
                  <a-col :span="11">
                    <a-form-item label="输出" field="output">
                      <a-textarea
                        v-model="item.output"
                        placeholder="期望输出"
                        :auto-size="{ minRows: 2 }"
                      />
                    </a-form-item>
                  </a-col>
                  <a-col :span="2">
                    <a-button
                      type="outline"
                      status="danger"
                      @click="removeJudgeCase(index)"
                      >删除</a-button
                    >
                  </a-col>
                </a-row>
              </a-form>
            </a-card>
          </div>
          <a-button type="outline" @click="addJudgeCase">+ 添加用例</a-button>
        </div>

        <!-- JSON模式 -->
        <div v-else>
          <a-alert type="warning" style="margin-bottom: 10px">
            <template #icon><icon-exclamation-circle-fill /></template>
            请按照以下格式输入：
            <pre class="json-example">
[
  {
    "input": "1 2",
    "output": "3"
  },
  {
    "input": "2 3",
    "output": "5"
  }
]</pre
            >
          </a-alert>
          <a-textarea
            v-model="form.judgeCase"
            placeholder="请输入判题用例（JSON格式）"
            :auto-size="{ minRows: 6 }"
          />
        </div>
      </a-form-item>

      <a-form-item field="judgeConfig" label="判题配置">
        <div class="editor-toggle">
          <a-radio-group v-model="judgeConfigMode" type="button">
            <a-radio value="form">表单模式</a-radio>
            <a-radio value="json">JSON模式</a-radio>
          </a-radio-group>
        </div>

        <!-- 表单模式 -->
        <div v-if="judgeConfigMode === 'form'" class="form-mode">
          <a-card>
            <a-form :model="form.judgeConfigForm" layout="vertical">
              <a-row :gutter="16">
                <a-col :span="8">
                  <a-form-item label="时间限制(ms)" field="timeLimit">
                    <a-input-number
                      v-model="form.judgeConfigForm.timeLimit"
                      placeholder="时间限制"
                      mode="button"
                    />
                  </a-form-item>
                </a-col>
                <a-col :span="8">
                  <a-form-item label="内存限制(kb)" field="memoryLimit">
                    <a-input-number
                      v-model="form.judgeConfigForm.memoryLimit"
                      placeholder="内存限制"
                      mode="button"
                    />
                  </a-form-item>
                </a-col>
                <a-col :span="8">
                  <a-form-item label="堆栈限制(kb)" field="stackLimit">
                    <a-input-number
                      v-model="form.judgeConfigForm.stackLimit"
                      placeholder="堆栈限制"
                      mode="button"
                    />
                  </a-form-item>
                </a-col>
              </a-row>
            </a-form>
          </a-card>
        </div>

        <!-- JSON模式 -->
        <div v-else>
          <a-alert type="warning" style="margin-bottom: 10px">
            <template #icon><icon-exclamation-circle-fill /></template>
            请按照以下格式输入：
            <pre class="json-example">
{
  "timeLimit": 1000,
  "memoryLimit": 10000,
  "stackLimit": 10000
}</pre
            >
          </a-alert>
          <a-textarea
            v-model="form.judgeConfig"
            placeholder="请输入判题配置（JSON格式）"
            :auto-size="{ minRows: 4 }"
          />
        </div>
      </a-form-item>

      <a-form-item>
        <a-space>
          <a-button type="primary" html-type="submit" :loading="loading">
            {{ isUpdate ? '更新题目' : '创建题目' }}
          </a-button>
          <a-button @click="router.push('/manage/question/')">
            返回题目管理
          </a-button>
        </a-space>
      </a-form-item>
    </a-form>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, watch, onMounted, computed } from "vue";
import { useRoute, useRouter } from "vue-router";
import { Message } from "@arco-design/web-vue";
import { IconExclamationCircleFill } from "@arco-design/web-vue/es/icon";
import MdEditor from "@/components/MdEditor.vue";
import MonacoEditor from "@/components/MonacoEditor.vue";
import { addQuestion, updateQuestion, getQuestionById } from "@/api/question/questionApi";
import type { QuestionAddRequest } from "@/api/question/type";

const router = useRouter();
const route = useRoute();

// 判断是否为更新题目
const isUpdate = ref(!!route.query.id);

const tagInput = ref("");
const loading = ref(false);
const judgeCaseMode = ref("form");
const judgeConfigMode = ref("form");

const form = reactive({
  id: undefined as string | number | undefined,
  title: "",
  content: "",
  tags: [] as string[],
  answer: "",
  judgeCase: "",
  judgeConfig: "",
  // 表单模式数据结构
  judgeCases: [] as { input: string; output: string }[],
  judgeConfigForm: {
    timeLimit: 1000,
    memoryLimit: 10000,
    stackLimit: 10000,
  },
});

// 添加标签
const addTag = () => {
  if (tagInput.value && !form.tags.includes(tagInput.value)) {
    form.tags.push(tagInput.value);
    tagInput.value = "";
  }
};

// 删除标签
const removeTag = (tag: string) => {
  form.tags = form.tags.filter((t) => t !== tag);
};

// 添加判题用例
const addJudgeCase = () => {
  form.judgeCases.push({ input: "", output: "" });
};

// 删除判题用例
const removeJudgeCase = (index: number) => {
  form.judgeCases.splice(index, 1);
};

// 监听表单模式变化，自动转换数据
watch(judgeCaseMode, (newMode) => {
  if (newMode === "json") {
    // 从表单模式切换到JSON模式，将表单数据转换为JSON
    try {
      form.judgeCase = JSON.stringify(form.judgeCases, null, 2);
    } catch (e) {
      form.judgeCase = "[]";
    }
  } else {
    // 从JSON模式切换到表单模式，尝试解析JSON
    try {
      const parsed = JSON.parse(form.judgeCase);
      if (Array.isArray(parsed)) {
        form.judgeCases = parsed;
      } else {
        form.judgeCases = [];
      }
    } catch (e) {
      form.judgeCases = [];
    }
  }
});

watch(judgeConfigMode, (newMode) => {
  if (newMode === "json") {
    // 从表单模式切换到JSON模式，将表单数据转换为JSON
    try {
      form.judgeConfig = JSON.stringify(form.judgeConfigForm, null, 2);
    } catch (e) {
      form.judgeConfig = "{}";
    }
  } else {
    // 从JSON模式切换到表单模式，尝试解析JSON
    try {
      const parsed = JSON.parse(form.judgeConfig);
      if (parsed && typeof parsed === "object" && !Array.isArray(parsed)) {
        form.judgeConfigForm = {
          ...form.judgeConfigForm,
          ...parsed,
        };
      }
    } catch (e) {
      // 保留默认值
    }
  }
});

// 提交表单
const handleSubmit = async () => {
  if (!form.title || !form.content) {
    Message.error("请填写必要信息");
    return;
  }

  loading.value = true;

  try {
    // 准备 judgeCase 数据
    let judgeCaseData;
    if (judgeCaseMode.value === "form") {
      // 表单模式直接使用数组对象
      judgeCaseData = form.judgeCases;
    } else {
      // JSON模式需要解析字符串为对象
      try {
        judgeCaseData = JSON.parse(form.judgeCase);
      } catch (e) {
        throw new Error("判题用例格式错误，请检查JSON格式");
      }
    }

    // 准备 judgeConfig 数据
    let judgeConfigData;
    if (judgeConfigMode.value === "form") {
      // 表单模式直接使用对象
      judgeConfigData = form.judgeConfigForm;
    } else {
      // JSON模式需要解析字符串为对象
      try {
        judgeConfigData = JSON.parse(form.judgeConfig);
      } catch (e) {
        throw new Error("判题配置格式错误，请检查JSON格式");
      }
    }

    let res;
    if (isUpdate.value) {
      // 更新题目
      res = await updateQuestion({
        id: form.id,
        title: form.title,
        content: form.content,
        tags: form.tags,
        answer: form.answer,
        judgeCase: judgeCaseData,
        judgeConfig: judgeConfigData,
      });
    } else {
      // 创建题目
      res = await addQuestion({
        title: form.title,
        content: form.content,
        tags: form.tags,
        answer: form.answer,
        judgeCase: judgeCaseData,
        judgeConfig: judgeConfigData,
      });
    }

    if (res.code === 0) {
      Message.success(`${isUpdate.value ? '更新' : '创建'}题目成功`);
      router.push('/manage/question/');
    } else {
      Message.error(res.message || `${isUpdate.value ? '更新' : '创建'}题目失败`);
    }
  } catch (err: any) {
    Message.error(err.message || `${isUpdate.value ? '更新' : '创建'}题目失败`);
  } finally {
    loading.value = false;
  }
};

// 获取题目详情（更新时使用）
const fetchQuestionDetail = async (id: string | number) => {
  try {
    const res = await getQuestionById(id);
    if (res.code === 0) {
      const question = res.data;
      form.id = question.id;
      form.title = question.title || '';
      form.content = question.content || '';
      form.answer = question.answer || '';

      // 处理标签
      if (question.tags) {
        try {
          const tags = JSON.parse(question.tags);
          if (Array.isArray(tags)) {
            form.tags = tags;
          }
        } catch (e) {
          // 如果解析失败，保持默认值
          form.tags = [];
        }
      }

      // 处理判题用例
      if (question.judgeCase) {
        try {
          const judgeCases = JSON.parse(question.judgeCase);
          if (Array.isArray(judgeCases)) {
            form.judgeCases = judgeCases;
            form.judgeCase = question.judgeCase;
          }
        } catch (e) {
          // 如果解析失败，保持默认值
        }
      }

      // 处理判题配置
      if (question.judgeConfig) {
        try {
          const judgeConfig = JSON.parse(question.judgeConfig);
          if (judgeConfig && typeof judgeConfig === 'object') {
            form.judgeConfigForm = {
              ...form.judgeConfigForm,
              ...judgeConfig
            };
            form.judgeConfig = question.judgeConfig;
          }
        } catch (e) {
          // 如果解析失败，保持默认值
        }
      }
    } else {
      Message.error('获取题目详情失败');
    }
  } catch (err) {
    Message.error('获取题目详情失败，请稍后重试');
    console.error('获取题目详情失败:', err);
  }
};

// 初始化默认用例
onMounted(() => {
  // 如果是更新题目，获取题目详情
  if (isUpdate.value && route.query.id) {
    fetchQuestionDetail(route.query.id);
  } else {
    // 初始化时添加一个空的判题用例
    if (form.judgeCases.length === 0) {
      addJudgeCase();
    }

    // 确保初始值正确设置为JSON字符串（用于显示）
    if (!form.judgeCase) {
      form.judgeCase = JSON.stringify(form.judgeCases, null, 2);
    }

    if (!form.judgeConfig) {
      form.judgeConfig = JSON.stringify(form.judgeConfigForm, null, 2);
    }
  }
});
</script>

<style scoped>
.add-question-container {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.tag-container {
  width: 100%;
}

.tag-input-group {
  margin-top: 10px;
  display: flex;
}

.tag-input-group :deep(.arco-input-wrapper) {
  flex: 1;
  margin-right: 10px;
}

.json-example {
  background: #f5f5f5;
  padding: 8px;
  border-radius: 4px;
  margin: 8px 0;
  font-size: 12px;
  white-space: pre-wrap;
}

.editor-toggle {
  margin-bottom: 15px;
}

.form-mode {
  margin-top: 10px;
}

.judge-case-item {
  margin-bottom: 15px;
}

.case-card {
  background: #f9f9f9;
}
</style>
