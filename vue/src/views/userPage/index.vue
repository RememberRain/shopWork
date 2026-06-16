<template>
  <div>
    <div class="header-container">
      <div class="header">
        <!-- 左侧：Logo及标题 -->
        <div class="header-left">
          <img src="@/assets/logo/logo.png" alt="">
<!--          <div class="titles" @click="router.push('/user/home')">后台管理系统</div>-->
          <div class="titles" @click="router.push('/user/home')">后台管理系统</div>
        </div>

        <!-- 中间：导航栏 -->
        <div class="header-center">
          <div class="front-header-nav">
            <el-menu :default-active="route.path" mode="horizontal" :router="true">
              <el-menu-item index="/user/home"><el-icon><HomeFilled/></el-icon>首页</el-menu-item>
              <el-menu-item index="/user/homestay"><el-icon><School/></el-icon>所有商品</el-menu-item>
              <el-menu-item index="/user/spot"><el-icon><Guide/></el-icon>特色商品</el-menu-item>
              <el-menu-item index="/user/order"><el-icon><Document/></el-icon>我的订单</el-menu-item>
              <el-menu-item index="/user/review"><el-icon><ChatDotRound/></el-icon>我的评价</el-menu-item>
              <el-menu-item index="/user/balance"><el-icon><Money/></el-icon>账户充值</el-menu-item>
              <el-menu-item index="/user/self"><el-icon><UserFilled/></el-icon>个人中心</el-menu-item>
            </el-menu>
          </div>
        </div>
R
        <!-- 右侧：按钮与用户信息 -->
        <div class="header-right">
          <!-- 商家认证按钮 -->
          <el-button @click="router.push('/user/auth')">去认证成为商家</el-button>

          <!-- 未登录状态 -->
          <div v-if="!userStore.name" class="auth-buttons">
            <el-button @click="router.push('/login')">登录</el-button>
            <el-button @click="router.push('/register')">注册</el-button>
          </div>

          <!-- 已登录状态 -->
          <div v-else class="user-info">
            <!-- 消息提醒 -->
            <el-popover placement="bottom-end" :width="320" trigger="hover" popper-class="msg-popover">
              <template #reference>
                <div class="bell-wrapper-flex other-class-name">
                  <el-badge :value="msgList.length" :max="99" :hidden="msgList.length === 0" class="badge-item">
                    <el-icon class="bell-icon"><Bell/></el-icon>
                  </el-badge>
                  <span v-if="msgList.length > 0" class="bell-text-prompt">消息通知</span>
                </div>
              </template>

              <!-- 消息列表内容 -->
              <div class="msg-box">
                <div class="msg-title">
                  <span>消息通知 ({{ msgList.length }})</span>
                  <el-button v-if="msgList.length > 0" link type="primary" size="small" @click="handleMarkAllRead">
                    全部忽略
                  </el-button>
                </div>
                <el-scrollbar max-height="250px">
                  <div v-if="msgList.length === 0" class="empty-msg">
                    <el-icon class="empty-icon"><CircleCheck/></el-icon>
                    <p>暂无新的商家回复消息</p>
                  </div>
                  <div v-else v-for="item in msgList" :key="item.reviewId" class="msg-item" @click="goToReviewPage(item)">
                    <div class="msg-item-header">
                      <span class="shop-name">商家回复了你的评价</span>
                      <span class="msg-time">{{ item.replyTime || '刚刚' }}</span>
                    </div>
                    <p class="msg-content">回复内容：{{ item.replyContent }}</p>
                    <p class="my-origin-content">我的原评：{{ item.content }}</p>
                  </div>
                </el-scrollbar>
              </div>
            </el-popover>

            <!-- 用户头像下拉 -->
            <el-dropdown>
              <div class="header-dropdown">
                <img :src="userStore.avatar" alt="">
                <div class="user-name"><span>{{ userStore.name }}</span></div>
              </div>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item>
                    <el-button type="text" @click.native="logout" >退出</el-button>
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </div>
      </div>
    </div>

    <!-- 路由渲染区域 -->
    <router-view/>
  </div>
</template>

<script setup>
import {useRoute, useRouter} from 'vue-router'
import {onMounted, onUnmounted, ref} from "vue";
import {ElMessageBox, ElMessage} from "element-plus";
import {
  Document,
  Guide,
  HomeFilled,
  Money,
  School,
  UserFilled,
  Bell,
  ChatDotRound,
  CircleCheck
} from "@element-plus/icons-vue";
import useUserStore from "@/stores/modules/userStore.js";

import {getUnreadReplies, readAllReplies} from "@/api/homestay/review.js";

const userStore = useUserStore()
const route = useRoute()
const router = useRouter()

// 消息通知列表数据源
const msgList = ref([])
let msgTimer = null // 轮询定时器

// 获取未读消息列表
const fetchUnreadMessages = () => {
  if (!userStore.name) return // 用户未登录时不执行轮询

  getUnreadReplies().then(res => {
    // 假设后端返回未读且被商家回复的评论集合
    msgList.value = res.data || []
  }).catch(() => {
  })
}

// 点击单条悬浮消息，一键跳转到我的评价页面
const goToReviewPage = (item) => {
  // 1. 跳转到“我的评价”页面
  router.push('/user/review')
  // 2. 刷新一下消息，或者可以针对性调用单条已读 API
  setTimeout(() => {
    fetchUnreadMessages()
  }, 500)
}

// 一键全部忽略/已读
const handleMarkAllRead = () => {
  readAllReplies().then(() => {
    ElMessage.success('消息已全部标记为已读')
    msgList.value = []
  })
}

const logout = () => {
  ElMessageBox.confirm('确定注销并退出系统吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    userStore.logOut().then(() => {
      location.href = '/index'
    })
  })
}

onMounted(() => {
  // 页面加载时立即拉取一次未读消息
  fetchUnreadMessages()

  // 建立300秒安全轮询机制，当商家在后台回复时，前端用户能自动收到提示
  msgTimer = setInterval(() => {
    fetchUnreadMessages()
  }, 300000)
})

onUnmounted(() => {
  // 页面销毁时切断定时器，释放系统内存
  if (msgTimer) {
    clearInterval(msgTimer)
  }
})
</script>

<style scoped>
.front-header-nav .el-menu-item {
  font-size: 16px;
  font-weight: 500;
}
.bell-wrapper-flex {
  /* 布局核心 */
  display: flex;
  align-items: center;
  justify-content: center; /* 确保内容在容器内水平居中 */

  /* 间距与交互 */
  gap: 8px;
  padding: 0 10px;
  position: relative;

  /* 状态控制 */
  cursor: pointer;
  white-space: nowrap; /* 强制不换行 */
  color: #fff;
  transition: opacity 0.3s ease;
}

.bell-wrapper-flex:hover {
  opacity: 0.8;
}

/* 覆盖 Element Plus 默认的 Badge 定位 */
:deep(.bottom-badge) {
  position: relative; /* Badge 容器不再使用默认的绝对定位 */
}

/* 关键样式：将红点移到右下角 */
:deep(.bottom-badge .el-badge__content) {
  position: absolute;
  top: auto !important;     /* 取消顶部定位 */
  bottom: -5px !important;  /* 移动到底部 */
  right: -5px !important;   /* 移动到右侧 */
  transform: none;          /* 防止默认偏移影响 */
}

/* 确保图标大小合适，给红点留出空间 */
.bell-icon {
  font-size: 22px !important;
  position: relative;
}

.front-header-nav .el-menu--horizontal .el-menu-item:not(.is-disabled):hover {
  color: #409EFF;
  background-color: #ecf5ff;
}

.front-header-nav .el-menu--horizontal > .el-menu-item.is-active {
  color: #409EFF;
  border-bottom: 2px solid #409EFF;
}

.front-header-nav .el-menu-item .el-icon {
  margin-right: 5px;
  width: 1em;
  height: 1em;
  vertical-align: middle;
}

.header-container {
  position: sticky;
  top: 0;
  z-index: 1000;
}

.header {
  display: flex;
  height: 70px;
  line-height: 70px;
  /* 使用高级蓝色渐变 */
  background: linear-gradient(135deg, #3a7bd5, #00d2ff);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  align-items: center;
}

/* 导航菜单样式调整 */
.el-menu {
  background-color: transparent !important; /* 透明背景 */
  border-bottom: none !important;
}

.header-left {
  width: auto;
  min-width: 250px;
  display: flex;
  align-items: center;
  padding-right: 20px;
  padding-left: 30px;
  gap: 12px;
}

.header-left img, .header-dropdown img {
  width: 40px;
  height: 40px;
  border-radius: 50%
}

.titles {
  cursor: pointer;
  margin-left: 15px;
  transition: all 0.3s ease;
  font-size: 20px;
  letter-spacing: 1px;
  color: #ffffff;
  font-weight: 600;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
}

.titles:hover {
  text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.3);
}

.header-center {
  flex: 1;
  overflow: hidden; /* 防止溢出 */
}

.header-right {
  width: auto;
  min-width: 180px;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  padding-right: 30px;
  gap: 15px;
}

.auth-buttons {
  display: flex;
  align-items: center;
  gap: 10px; /* 按钮间距 */
}

.user-info {
  display: flex;
  align-items: center;
}

/* 用户下拉菜单 */
.header-dropdown {
  display: flex;
  align-items: center;
  cursor: pointer;
  transition: all 0.3s ease;
  gap: 8px;
  background: rgba(0, 0, 0, 0.1);
  padding: 4px 12px 4px 4px;
  border-radius: 25px;
}

.header-dropdown:focus {
  outline: none;
}

.user-name {
  margin-left: 0;
  font-size: 14px;
  color: #fff;
  display: flex;
  align-items: center;
}

.front-header-nav .el-menu-item.is-active {
  color: white !important;
}

.main-content {
  width: 80%;
  margin: 5px auto;
}

.el-menu.el-menu--horizontal {
  border: none !important;
  height: 59px;
  background-color: transparent !important;
  border-bottom: none !important;
}

.el-menu-item.is-active {
  color: #fff !important;
  background-color: rgba(255, 255, 255, 0.2) !important;
  border-bottom: 3px solid #fff !important;
}

.el-menu-item {

  font-weight: 500 !important;
  margin: 0 !important;

  height: 72px !important;
  line-height: 72px !important;
  color: rgba(255, 255, 255, 0.85) !important;
  font-size: 15px !important;
  padding: 0 24px !important;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1) !important;
}

.el-menu-item:hover {
  background-color: rgba(255, 255, 255, 0.15) !important;
  color: #fff !important;
}

.el-menu-item:not(.is-active):hover {
  color: #fff !important;
  background-color: rgba(255, 255, 255, 0.1) !important;
  transform: translateY(-2px); /* 悬停上浮效果 */
}

/* 按钮样式优化 */
.el-button {
  padding: 10px 20px !important;
  transition: all 0.3s ease !important;

  border-radius: 8px !important; /* 从圆角改为圆润矩形，更现代 */
  font-weight: 500 !important;
  border: none !important;
  background: rgba(255, 255, 255, 0.1) !important;
  color: midnightblue !important;
}

.el-button:hover {
  background: rgba(255, 255, 255, 0.2) !important;
}

:deep(.msg-popover) {
  border-radius: 12px !important;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12) !important;
}

</style>
