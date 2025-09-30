<template>
  <div id="userLayout">
    <a-layout style="min-height: 100vh; background: linear-gradient(to right, #fefefe, #fff);">
      <a-layout-content class="content">
        <div class="content-wrapper">
          <router-view />
        </div>
      </a-layout-content>
      <a-layout-footer class="footer">
        <a href="https://yupi.icu" target="_blank">
          编程导航知识星球 by 程序员鱼皮
        </a>
      </a-layout-footer>
    </a-layout>
  </div>
</template>

<script setup lang="ts">
// 用户布局组件
</script>

<style scoped>
#userLayout {
  min-height: 100vh;
  width: 100%;
}

#userLayout .content {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 20px 0;
  background: linear-gradient(to right, #fefefe, #fff);
}

#userLayout .content-wrapper {
  width: 100%;
  max-width: 1200px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 0 20px;
}

#userLayout .footer {
  background: #efefef;
  padding: 16px;
  text-align: center;
  border: none;
  color: #000000;
}

#userLayout .footer a {
  color: #000000;
  text-decoration: none;
}

#userLayout .footer a:hover {
  background-color: rgba(0, 0, 0, 0.1);
}
</style>