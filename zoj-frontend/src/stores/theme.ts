// src/stores/theme.ts
import { ref } from 'vue'
import { defineStore } from 'pinia'

export const useThemeStore = defineStore('theme', () => {
  // 固定主题颜色为白色
  const sidebarBgColor = ref('#ffffff')

  // 预定义的主题颜色选项（只保留白色）
  const themeOptions = [
    { name: '白色', color: '#ffffff' }
  ]

  // 设置侧边栏背景色（只允许设置为白色）
  function setSidebarBgColor() {
    sidebarBgColor.value = '#ffffff'
  }

  return {
    sidebarBgColor,
    themeOptions,
    setSidebarBgColor
  }
})
