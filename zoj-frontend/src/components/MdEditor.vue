<template>
  <div class="md-editor-wrapper">
    <Editor
      :value="value"
      :plugins="plugins"
      :placeholder="placeholder"
      :mode="mode"
      :preview-theme="previewTheme"
      @change="handleChange"
      :style="{ height: editorHeight }"
      class="bytemd-editor-container"
      :editorConfig="{ readonly: mode === 'preview' }"
    />
  </div>
</template>

<script setup lang="ts">
import { Editor } from "@bytemd/vue-next";
import gfm from "@bytemd/plugin-gfm";
import highlight from "@bytemd/plugin-highlight";
import frontmatter from "@bytemd/plugin-frontmatter";
import mediumZoom from "@bytemd/plugin-medium-zoom";
import breaks from "@bytemd/plugin-breaks";
import { computed } from "vue";

// 定义插件列表
const plugins = [gfm(), highlight(), frontmatter(), mediumZoom(), breaks()];

// 定义组件属性
const props = withDefaults(defineProps<{
  value?: string; // 将modelValue改为value，并设为可选
  modelValue?: string; // 保留modelValue以保持向后兼容
  height?: number | string;
  placeholder?: string;
  mode?: 'edit' | 'preview' | 'split'; // 添加mode属性
  previewTheme?: string; // 添加previewTheme属性
}>(), {
  value: '',
  modelValue: '',
  height: 500,
  placeholder: '请输入内容...',
  mode: 'edit', // 默认为编辑模式
  previewTheme: 'default' // 默认预览主题
});

// 定义事件发射器
const emit = defineEmits<{
  (e: "update:modelValue", value: string): void;
  (e: "change", value: string): void; // 添加change事件以保持向后兼容
}>();

// 计算属性：双向绑定值（兼容value和modelValue）
const value = computed({
  get() {
    // 优先使用modelValue，如果没有则使用value
    return props.modelValue || props.value || '';
  },
  set(val: string) {
    emit("update:modelValue", val);
    emit("change", val); // 同时发射change事件
  },
});

// 处理内容变化
const handleChange = (v: string) => {
  // 在编辑模式和分屏模式下都触发更新
  if (props.mode === 'edit' || props.mode === 'split') {
    value.value = v; // 通过计算属性的setter触发事件
  }
};

// 计算编辑器高度
const editorHeight = computed(() => {
  return typeof props.height === 'number' ? `${props.height}px` : props.height;
});
</script>

<style>
/* 使用相对路径导入本地 CSS 文件 */
@import url('../../node_modules/bytemd/dist/index.css');

.md-editor-wrapper {
  width: 100%;
  height: 100%;
}

.bytemd-editor-container {
  --tw-border-opacity: 1;
  border-color: rgba(229, 231, 235, var(--tw-border-opacity));
  border-radius: 0.5rem;
  overflow: hidden;
  border: 1px solid #e5e7eb;
  height: v-bind(editorHeight);
}

.bytemd-editor-container .bytemd {
  height: 100%;
  border: none;
}

/* 修复工具栏右侧最后一个图标显示问题 */
.bytemd-editor-container .bytemd-toolbar-right .bytemd-toolbar-icon:last-child {
  display: none;
}

/* 根据模式控制工具栏显示 */
.bytemd-editor-container .bytemd-toolbar {
  background-color: #f9fafb;
  border-bottom: 1px solid #e5e7eb;
  padding: 0 10px;
  display: flex;
  align-items: center;
  flex-wrap: wrap;
}

/* 修复分屏模式下的布局 */
.bytemd-editor-container .bytemd-body {
  display: flex;
  height: calc(100% - 40px);
}

/* 修复预览区域样式 */
.bytemd-editor-container .bytemd .bytemd-preview {
  background-color: #ffffff;
  padding: 16px;
  flex: 1;
  overflow-y: auto;
}

/* 修复工具栏图标样式 */
.bytemd-editor-container .bytemd-toolbar-icon {
  border-radius: 0.25rem;
  width: 30px;
  height: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 2px;
  color: #374151;
}

.bytemd-editor-container .bytemd-toolbar-icon:hover {
  background-color: #e5e7eb;
}

/* 修复工具栏左右区域布局 */
.bytemd-editor-container .bytemd-toolbar-left {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
}

.bytemd-editor-container .bytemd-toolbar-right {
  display: flex;
  align-items: center;
  margin-left: auto;
}

/* 修复编辑器和预览区域样式 */
.bytemd-editor-container .bytemd-body {
  background-color: #ffffff;
  height: calc(100% - 40px);
}

.bytemd-editor-container .bytemd .bytemd-preview {
  background-color: #ffffff;
}

.bytemd-editor-container .CodeMirror {
  background-color: #ffffff;
  height: calc(100% - 40px);
}

/* 修复状态栏样式 */
.bytemd-editor-container .bytemd-status {
  border-top: 1px solid #e5e7eb;
  background-color: #f9fafb;
  padding: 4px 10px;
  font-size: 12px;
}

/* 全屏模式样式 */
.bytemd-fullscreen.bytemd {
  z-index: 1000;
  height: 100vh !important;
}

.bytemd-fullscreen .bytemd-body {
  height: calc(100vh - 80px);
}

.bytemd-fullscreen .CodeMirror {
  height: 100%;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .bytemd-editor-container .bytemd-toolbar {
    padding: 5px;
  }

  .bytemd-editor-container .bytemd-toolbar-icon {
    width: 26px;
    height: 26px;
    margin: 1px;
  }
}
</style>
