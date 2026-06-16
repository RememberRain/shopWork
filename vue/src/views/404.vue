<template>
  <div class="error-container">
    <div class="error-content">
      <!-- 404 趣味视觉主体（采用渐变艺术字，替代枯燥的普通文本） -->
      <div class="error-code">404</div>

      <!-- 提示文案 -->
      <h1 class="error-title">抱歉，您访问的页面不存在</h1>
      <p class="error-desc">可能由于网址输入错误，或者该网页已被管理员移除了...</p>

      <!-- 操作按钮区域（增加了返回和首页的双向选择，体验更佳） -->
      <div class="error-actions">
        <el-button
            type="primary"
            size="large"
            icon="ArrowLeft"
            @click="router.go(-1)"
            class="action-btn"
        >
          返回上一页
        </el-button>
        <!-- 额外附赠：通常 404 会留一个回首页的选择，您可以根据实际情况决定是否保留 -->
        <el-button
            size="large"
            icon="HomeFilled"
            @click="router.push('/')"
            class="action-btn text-btn"
        >
          返回首页
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from "vue-router";

// 路由实例
const router = useRouter()
</script>

<style scoped>
/* 全屏居中容器 */
.error-container {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 100%;
  min-height: 80vh; /* 让内容在视口中自然垂直居中 */
  background-color: #ffffff;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;
}

/* 内容卡片区 */
.error-content {
  text-align: center;
  max-width: 500px;
  padding: 0 20px;
  /* 优雅的入场动画 */
  animation: fadeInUp 0.6s ease-out both;
}

/* 404 大数字（炫酷的渐变色） */
.error-code {
  font-size: 140px;
  font-weight: 800;
  line-height: 1;
  letter-spacing: -4px;
  background: linear-gradient(135deg, #409eff 0%, #a0cfff 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  margin-bottom: 20px;
  user-select: none;
}

/* 错误主标题 */
.error-title {
  font-size: 24px;
  color: #303133;
  font-weight: 600;
  margin: 0 0 12px 0;
}

/* 错误次级描述 */
.error-desc {
  font-size: 14px;
  color: #909399;
  line-height: 1.6;
  margin: 0 0 32px 0;
}

/* 按钮组容器 */
.error-actions {
  display: flex;
  justify-content: center;
  gap: 16px;
}

/* 按钮细节微调 */
.action-btn {
  padding: 12px 24px;
  font-size: 14px;
  font-weight: 500;
  border-radius: 8px; /* 现代感大圆角 */
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
}

/* 给主按钮加点投影，更具悬浮感 */
.action-btn.el-button--primary {
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
}
.action-btn.el-button--primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(64, 158, 255, 0.4);
}

/* 柔和的动画 */
@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>
