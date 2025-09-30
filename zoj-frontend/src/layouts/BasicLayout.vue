<template>
  <a-layout class="basic-layout">
    <!-- 全局顶部导航栏 -->
    <GlobalHeader />
    
    <!-- 内容区域 -->
    <a-layout-content class="content-layout">
      <RouterView />
    </a-layout-content>
    
    <!-- 全局页脚 -->
    <a-layout-footer class="layout-footer">
      <Footer />
    </a-layout-footer>
  </a-layout>
</template>

<script setup lang="ts">
import { RouterView } from 'vue-router'
import GlobalHeader from '@/components/GlobalHeader.vue'
import Footer from '@/components/Footer.vue'
</script>

<style scoped>
.basic-layout {
  height: 100vh;
  width: 100vw;
  overflow: auto;
  background-color: #f5f5f5;
}

.content-layout {
  flex: 1;
  overflow: auto;
  padding: 20px 0;
}

.layout-footer {
  text-align: center;
  background-color: #ffffff;
  border-top: 1px solid #e8e8e8;
  padding: 16px;
}
</style>