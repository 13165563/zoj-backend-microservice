<template>
  <div ref="editorContainer" class="monaco-editor-container"></div>
</template>

<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount, watch, nextTick, computed } from 'vue'

// 定义组件属性
const props = withDefaults(defineProps<{
  modelValue?: string
  value?: string
  language?: string
  theme?: string
  height?: string | number
  options?: Record<string, any>
}>(), {
  modelValue: '',
  value: '',
  language: 'javascript',
  theme: 'vs-light',
  height: '300px',
  options: () => ({})
})

// 定义事件
const emit = defineEmits<{
  (e: 'update:modelValue', value: string): void
  (e: 'change', value: string): void
}>()

// 编辑器引用
const editorContainer = ref<HTMLDivElement | null>(null)
let editor: any = null

// 计算当前值（优先使用modelValue，兼容旧的value）
const currentValue = computed(() => {
  return props.modelValue || props.value || ''
})

// 计算编辑器高度
const editorHeight = computed(() => {
  if (typeof props.height === 'number') {
    return `${props.height}px`
  }
  return props.height
})

// 初始化编辑器
const initEditor = async () => {
  if (!editorContainer.value) return

  try {
    // 动态导入 monaco-editor
    const monaco = await import('monaco-editor')
    
    // 销毁已存在的编辑器
    if (editor) {
      editor.dispose()
    }

    // 创建编辑器
    const editorOptions: any = {
      value: currentValue.value,
      language: props.language,
      theme: props.theme,
      ...props.options
    }
    
    editor = monaco.editor.create(editorContainer.value, editorOptions)

    // 监听内容变化
    editor.onDidChangeModelContent(() => {
      const value = editor.getValue()
      emit('update:modelValue', value)
      emit('change', value)
    })
  } catch (error: any) {
    console.error('Failed to initialize Monaco Editor:', error)
    // 降级到textarea
    if (editorContainer.value) {
      editorContainer.value.innerHTML = `
        <textarea 
          style="width:100%;height:100%;border:none;outline:none;padding:10px;font-family:monospace;font-size:14px;"
          placeholder="Monaco Editor initialization failed. Using textarea as fallback."
        >${currentValue.value}</textarea>
      `
    }
  }
}

// 监听value变化
watch(currentValue, (newValue) => {
  if (editor && newValue !== editor.getValue()) {
    editor.setValue(newValue || '')
  }
})

// 监听语言变化
watch(() => props.language, (newLanguage) => {
  if (editor) {
    const model = editor.getModel()
    if (model) {
      import('monaco-editor').then((monaco) => {
        monaco.editor.setModelLanguage(model, newLanguage)
      })
    }
  }
})

// 监听主题变化
watch(() => props.theme, (newTheme) => {
  if (editor) {
    import('monaco-editor').then((monaco) => {
      monaco.editor.setTheme(newTheme)
    })
  }
})

// 监听选项变化
watch(() => props.options, () => {
  if (editor) {
    editor.updateOptions(props.options)
  }
})

// 组件挂载时初始化编辑器
onMounted(() => {
  nextTick(() => {
    initEditor()
  })
})

// 组件卸载前销毁编辑器
onBeforeUnmount(() => {
  if (editor) {
    editor.dispose()
    editor = null
  }
})

// 定义暴露给父组件的方法
defineExpose({
  getEditor: () => editor
})
</script>

<style scoped>
.monaco-editor-container {
  height: v-bind(editorHeight);
  width: 100%;
  border: 1px solid #ddd;
  border-radius: 4px;
}
</style>