<template>
  <div class="md-viewer" v-html="renderedContent"></div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { marked } from 'marked';
import hljs from 'highlight.js';
import 'highlight.js/styles/github.css';

// 定义组件属性
const props = withDefaults(defineProps<{
  value?: string;
}>(), {
  value: ''
});

// 配置marked
marked.setOptions({
  highlight: function (code, lang) {
    const language = hljs.getLanguage(lang) ? lang : 'plaintext';
    return hljs.highlight(code, { language }).value;
  },
  langPrefix: 'hljs language-',
});

// 渲染后的内容
const renderedContent = computed(() => {
  return marked(props.value || '');
});
</script>

<style scoped>
.md-viewer {
  width: 100%;
  min-height: 200px;
  border: none;
  border-radius: 0.5rem;
  overflow: hidden;
  background-color: #ffffff;
  padding: 16px;
}

.md-viewer img {
  max-width: 100%;
  height: auto;
}

.md-viewer pre {
  padding: 12px;
  border-radius: 4px;
  overflow: auto;
}

.md-viewer code {
  border-radius: 4px;
}

.md-viewer blockquote {
  margin: 0;
  padding: 0 1em;
  border-left: 0.25em solid #dfe2e5;
  color: #6a737d;
}

.md-viewer table {
  border-collapse: collapse;
  width: 100%;
  margin: 16px 0;
}

.md-viewer table th,
.md-viewer table td {
  border: 1px solid #dfe2e5;
  padding: 6px 13px;
}

.md-viewer table tr:nth-child(2n) {
  background-color: #f6f8fa;
}
</style>