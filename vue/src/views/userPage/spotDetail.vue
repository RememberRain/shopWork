<template>
  <div class="detail-wrapper">
    <div class="detail-container">

      <div class="action-bar">
        <el-button icon="ArrowLeft" @click="router.go(-1)" class="back-btn">
          返回上一页
        </el-button>
      </div>

      <div class="image-banner">
        <el-image
            :src="baseUrl + spot.image"
            :preview-src-list="[baseUrl + spot.image]"
            preview-teleported
            fit="cover"
            class="banner-img"
        />
        <div class="banner-shadow"></div>
      </div>

      <div class="info-header">
        <h1 class="product-title">{{ spot.name }}</h1>
        <div class="location-tag">
          <el-icon class="loc-icon"><Location /></el-icon>
          <span class="loc-label">发货地址：</span>
          <span class="loc-value">{{ spot.location }}</span>
        </div>
      </div>

      <div class="info-section time-section">
        <h3 class="section-title title-blue">限时抢购时间</h3>
        <div class="time-board">
          <el-icon class="clock-icon"><Clock /></el-icon>
          <span class="time-text">{{ spot.openingHours }} 截止！</span>
          <span class="time-text">数量有限！先到先得！</span>
        </div>
      </div>

      <div class="info-section stock-section">
        <h3 class="section-title title-green">当前抢购状态</h3>
        <div class="stock-board">
          <span class="stock-label">剩余数量：</span>
          <el-tag
              :type="spot.stock > 10 ? 'success' : spot.stock > 0 ? 'warning' : 'danger'"
              effect="dark"
              size="large"
          >
            {{ spot.stock > 0 ? `${spot.stock} 件` : '已售罄 (0件)' }}
          </el-tag>
        </div>
      </div>

      <div class="info-section">
        <h3 class="section-title title-orange">商品详细介绍</h3>
        <div class="description-card">
          <p class="description-text">
            {{ spot.description }}
          </p>
        </div>
      </div>

      <div class="action-section">
        <el-button
            :type="spot.stock > 0 ? 'danger' : 'info'"
            size="large"
            class="rush-buy-btn"
            :icon="spot.stock > 0 ? 'ShoppingCart' : 'CircleClose'"
            :disabled="!spot.stock || spot.stock <= 0"
            @click="handleRushBuy(spot.homestayId)"
        >
          {{ spot.stock > 0 ? '立即去抢购' : '已售罄，下次早点来哦' }}
        </el-button>
      </div>

    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from "vue";
import { useRoute, useRouter } from "vue-router";
import { ElMessage } from 'element-plus'
import { selectSpotBySpotId } from "@/api/homestay/spot.js";
import { Location, ArrowLeft, Clock, ShoppingCart, CircleClose } from "@element-plus/icons-vue";

const router = useRouter()
const route = useRoute()

// 基础api地址
const baseUrl = import.meta.env.VITE_APP_BASE_API

// 景点ID
const spotId = route.params.id

// 景点信息
const spot = ref({})

// 抢购跳转
const handleRushBuy = (homestayId) => {
  console.log("当前商品的 homestayId 为:", homestayId)

  // 核心改动：在 JS 层做二次拦截，防止恶意点击或逻辑漏洞
  if (!spot.value.stock || spot.value.stock <= 0) {
    ElMessage.error('该商品非常火爆，已经全部售空啦！')
    return
  }

  if (!homestayId) {
    ElMessage.warning('暂无对应的抢购商品信息')
    return
  }
  router.push({
    path: `/user/homestayDetail/${homestayId}`
  })
}

onMounted(() => {
  if (spotId) {
    selectSpotBySpotId(spotId).then(res => {
      spot.value = res.data
      //
      console.log("后端返回的完整详情数据：", res.data)
    }).catch(err => {
      console.error("获取详情失败，请检查接口或网络:", err)
    })
  } else {
    console.warn("路由参数中未获取到有效的 spotId")
  }
})
</script>

<style scoped>
/* 保持你原本的样式，这里补充新增的库存状态栏样式 */
.action-section {
  margin-top: 24px;
  display: flex;
  justify-content: center;
}
.rush-buy-btn {
  width: 100%;
  max-width: 320px;
}

/* 确保 el-image 撑满容器 */
.banner-img {
  width: 100%;
  height: 100%;
  display: block; /* 消除底部间隙 */
}

/* 覆盖 el-image 默认的样式，保持你原本的图片悬浮放大效果 */
:deep(.el-image__inner) {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.4s cubic-bezier(0.25, 1, 0.5, 1);
}

.image-banner:hover :deep(.el-image__inner) {
  transform: scale(1.015);
}

/* 状态展示美化 */
.stock-board {
  display: flex;
  align-items: center;
  background: #f8f9fa;
  padding: 12px 20px;
  border-radius: 8px;
  border-left: 4px solid #67c23a;
  font-size: 15px;
}
.stock-label {
  color: #606266;
  font-weight: bold;
}
.title-green {
  color: #67c23a;
  border-left: 4px solid #67c23a;
  padding-left: 8px;
  margin-bottom: 12px;
}

/* 全局大背景，营造干净舒适的底色 */
.detail-wrapper {
  background-color: #f8fafc;
  min-height: 100vh;
  padding: 24px 0 60px 0;
}

/* 核心内容容器 */
.detail-container {
  width: 1000px; /* 调整为更紧凑黄金的 1000px，阅读视觉更聚焦 */
  margin: 0 auto;
  background: #ffffff;
  border-radius: 16px;
  padding: 24px 32px 40px 32px;
  box-shadow: 0 4px 20px rgba(160, 174, 192, 0.08);
}

/* 返回按钮美化 */
.action-bar {
  margin-bottom: 20px;
}
.back-btn {
  border-radius: 8px;
  padding: 8px 16px;
  color: #64748b;
  border-color: #e2e8f0;
  transition: all 0.2s;
}
.back-btn:hover {
  color: #3b82f6;
  border-color: #bfdbfe;
  background-color: #eff6ff;
}

/* 主图外框及高级投影 */
.image-banner {
  position: relative;
  width: 100%;
  height: 480px;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.05);
}
.banner-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.4s cubic-bezier(0.25, 1, 0.5, 1);
}
.image-banner:hover .banner-img {
  transform: scale(1.015); /* 悬浮时极为克制的平滑放大 */
}
.banner-shadow {
  position: absolute;
  bottom: 0; left: 0; width: 100%; height: 80px;
  background: linear-gradient(to top, rgba(0,0,0,0.02), transparent);
  pointer-events: none;
}

/* 商品标题与发货地 */
.info-header {
  margin: 28px 0;
}
.product-title {
  font-size: 28px;
  font-weight: 700;
  color: #1e293b;
  margin: 0 0 12px 0;
  line-height: 1.3;
}
.location-tag {
  display: inline-flex;
  align-items: center;
  font-size: 14px;
  color: #64748b;
  background-color: #f1f5f9;
  padding: 6px 12px;
  border-radius: 20px; /* 胶囊型发货地标签 */
}
.loc-icon {
  color: #ef4444;
  margin-right: 4px;
}
.loc-label {
  font-weight: 500;
}
.loc-value {
  color: #334155;
}

/* 通用区块定义 */
.info-section {
  margin-bottom: 36px;
}
.section-title {
  font-size: 18px;
  font-weight: 600;
  color: #1e293b;
  margin: 0 0 16px 0;
  padding-left: 12px;
  position: relative;
}
/* 用伪元素替代生硬的左边框 */
.section-title::before {
  content: '';
  position: absolute;
  left: 0; top: 3px;
  width: 4px; height: 16px;
  border-radius: 2px;
}
.title-blue::before { background-color: #3b82f6; }
.title-orange::before { background-color: #f97316; }

/* 抢购时间看板样式 */
.time-board {
  display: flex;
  align-items: center;
  gap: 10px;
  background: linear-gradient(135deg, #eff6ff 0%, #dbeafe 100%); /* 渐变冷色时效底色 */
  border: 1px solid #bfdbfe;
  padding: 16px 20px;
  border-radius: 10px;
}
.clock-icon {
  font-size: 20px;
  color: #2563eb;
  animation: pulse 2s infinite; /* 赋予时钟微微呼吸的动态感 */
}
.time-text {
  font-size: 16px;
  font-weight: 600;
  color: #1d4ed8;
  font-family: Monaco, Consolas, monospace;
}

/* 商品介绍文本盒 */
.description-card {
  background-color: #fafafa;
  border: 1px solid #f1f5f9;
  border-radius: 10px;
  padding: 20px;
}
.description-text {
  font-size: 15px;
  line-height: 1.8;
  color: #475569;
  margin: 0;
  text-align: justify;
  white-space: pre-line; /* 保留后台录入的自然换行 */
}

/* 时间图标轻微呼吸动效 */
@keyframes pulse {
  0%, 100% { opacity: 1; transform: scale(1); }
  50% { opacity: 0.8; transform: scale(1.05); }
}

.action-section {
  margin-top: 24px;
  display: flex;
  justify-content: center;
}
.rush-buy-btn {
  width: 100%;
  max-width: 320px;
}
</style>
