<template>
  <!--
   router-view 组件:
   是我们Vue Router的核心组件, 用于渲染当前路由对应的组件
   -->
  <router-view/>
  <AiChatBox />

</template>

<script setup>

import AiChatBox from "@/views/userPage/AiChatBox.vue";
</script>

<style scoped>

</style>
