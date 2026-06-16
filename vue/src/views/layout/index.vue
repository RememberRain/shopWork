<template>
  <div class="layout-provider">

    <el-header class="global-navbar">
      <div class="brand-monogram">
        <img :src="logo" alt="System Logo" class="brand-logo">
        <h1 class="brand-title">后台管理系统</h1>
      </div>

      <div class="navbar-right-hub">
        <el-breadcrumb separator="/" class="custom-breadcrumb">
          <el-breadcrumb-item v-for="(item, index) in breadItems" :key="index">
            <span class="crumb-link">{{ item.meta.title }}</span>
          </el-breadcrumb-item>
        </el-breadcrumb>

        <div class="user-action-dock">
          <span class="welcome-text">您好，<strong class="user-name">{{ userStore.name }}</strong></span>

          <el-button link type="danger" @click="logout" class="logout-link-btn">
            <SvgIcon icon-class="logout" class="logout-icon"/>
            <span>退出登录</span>
          </el-button>

          <el-dropdown trigger="click" class="avatar-dropdown-trigger">
            <div class="avatar-wrapper">
              <img :src="userStore.avatar" alt="User Avatar" class="user-avatar">
              <el-icon class="arrow-icon"><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu class="custom-dropdown-menu">
                <router-link to="/user/profile" class="no-underline">
                  <el-dropdown-item icon="User">个人中心</el-dropdown-item>
                </router-link>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>
    </el-header>

    <div class="layout-workspace">

      <el-aside class="global-sidebar">
        <SideBar />
      </el-aside>

      <main class="global-app-main">
        <div class="app-main-inner">
          <AppMain />
        </div>
      </main>

    </div>

  </div>
</template>

<script setup>
import logo from '@/assets/logo/logo.png'
import {onMounted, ref, watch} from "vue";
import {useRoute} from "vue-router";
import useUserStore from "@/stores/modules/userStore.js";
import {ElMessage, ElMessageBox} from "element-plus";
import {ArrowDown} from "@element-plus/icons-vue";
import SvgIcon from "@/components/SvgIcon/index.vue";
import AppMain from "@/views/layout/components/AppMain.vue";
import SideBar from './components/Sidebar'

//用户状态信息
const userStore = useUserStore()

//面包屑数组
const breadItems = ref([])

const route = useRoute()

//退出登录
const logout = () => {
  ElMessageBox.confirm(
      '确定退出系统吗?',
      '系统提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
  )
      .then(() => {
        userStore.logOut().then(() => {
          //退出成功之后跳转到登录页
          location.href = '/login'
        })
      })
}

//判断当前路由是否是首页
const isDashboard = (route) => {
  const path = route && route.path
  if (!path) return false
  return path === '/' || path === '/index'
}

//获取面包屑数据
const getBread = () => {
  //1.从当前的路由匹配数组中过滤出需要显示的面包屑
  const matched = route.matched.filter(item => {
    return item.meta && item.meta.title
  })

  //2.如果不是首页, 就在最前面添加首页项
  if (matched.length === 0 || !isDashboard(matched[0])) {
    const homeRoute = {
      path: '/',
      meta: {title: '首页'},
      name: 'Index'
    }
    matched.unshift(homeRoute)
  }

  //3.更新面包屑数组
  breadItems.value = matched
}

//组件挂载完成后执行
onMounted(() => {
  //初始化面包屑
  getBread()
})

//监听路由变化
watch(() => route.path, () => {
  getBread()
})
</script>

<style scoped>
/* ================= 全局现代后台骨架布局 ================= */
.layout-provider {
  display: flex;
  flex-direction: column;
  width: 100vw;
  height: 100vh;
  overflow: hidden;
  background-color: #f8fafc; /* 国际范微蓝低饱和灰底色 */
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
}

/* ================= 1. 顶部导航栏重塑 ================= */
.global-navbar {
  height: 54px !important; /* 稍微加高 4px 更有呼吸感 */
  background-color: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(8px); /* 现代毛玻璃质感 */
  -webkit-backdrop-filter: blur(8px);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  box-shadow: 0 1px 3px rgba(15, 23, 42, 0.04);
  border-bottom: 1px solid #f1f5f9;
  z-index: 1000;
  position: relative;
}

/* 左侧 Logo 组 */
.brand-monogram {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-shrink: 0;
}
.brand-logo {
  width: 36px;
  height: 36px;
  object-fit: contain;
  filter: drop-shadow(0 2px 4px rgba(59, 130, 246, 0.15));
}
.brand-title {
  font-size: 18px; /* 修正：原 28px 在常规屏幕下过大，容易挤压菜单，18~20px 是标准B端黄金字阶 */
  font-weight: 700;
  color: #0f172a;
  margin: 0;
  letter-spacing: -0.3px;
}

/* 右侧综合面板 */
.navbar-right-hub {
  flex: 1;
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-left: 32px;
}

/* 面包屑美化 */
.custom-breadcrumb :deep(.el-breadcrumb__inner) {
  color: #64748b;
  font-weight: 400;
}
.custom-breadcrumb :deep(.el-breadcrumb__item:last-child .el-breadcrumb__inner) {
  color: #0f172a;
  font-weight: 600;
}

/* 用户状态区 */
.user-action-dock {
  display: flex;
  align-items: center;
  gap: 20px;
}
.welcome-text {
  font-size: 13.5px;
  color: #475569;
}
.user-name {
  color: #1e293b;
  font-weight: 600;
}

/* 退出登录按钮改版 */
.logout-link-btn {
  font-size: 13.5px;
  color: #64748b !important;
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 4px 12px;
  border-radius: 6px;
  transition: all 0.2s;
}
.logout-link-btn:hover {
  color: #ef4444 !important;
  background-color: #fef2f2;
}
.logout-icon {
  font-size: 14px;
}

/* 头像下拉触发器 */
.avatar-dropdown-trigger {
  cursor: pointer;
}
.avatar-wrapper {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 2px;
  border-radius: 20px;
  transition: background-color 0.2s;
}
.avatar-wrapper:hover {
  background-color: #f1f5f9;
}
.user-avatar {
  width: 34px;
  height: 34px;
  border-radius: 50%;
  border: 1px solid #e2e8f0;
  object-fit: cover;
}
.arrow-icon {
  font-size: 12px;
  color: #94a3b8;
  margin-right: 4px;
}

/* ================= 2. 下部工作空间（核心修正点） ================= */
.layout-workspace {
  flex: 1;
  display: flex;
  position: relative;
  height: calc(100vh - 54px); /* 精确切出下半部分高度 */
}

/* 侧边菜单栏容器精细化 */
.global-sidebar {
  width: 240px !important;
  height: 100%;
  background-color: #ffffff;
  border-right: 1px solid #e2e8f0;
  flex-shrink: 0;
  box-sizing: border-box;
}

/* 主视图视图：自带原生独立平滑滚动条，永不击穿顶栏 */
.global-app-main {
  flex: 1;
  height: 100%;
  overflow-y: auto; /* 仅在主区域内部产生纵向滚动条 */
  overflow-x: hidden;
  box-sizing: border-box;
}
.app-main-inner {
  padding: 24px; /* 全局业务内衬边距 */
  min-height: 100%;
  box-sizing: border-box;
}

/* 下拉菜单边框细节微调 */
.custom-dropdown-menu :deep(.el-dropdown-menu__item) {
  padding: 8px 16px;
  font-size: 13px;
}
.no-underline {
  text-decoration: none;
}
</style>
