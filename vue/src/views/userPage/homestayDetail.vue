<template>
  <div class="detail-page-container">
    <!-- 顶部导航面包屑/返回区 -->
    <div class="back-button-area">
      <el-button icon="ArrowLeft" @click="router.go(-1)" class="custom-back-btn">
        返回上页
      </el-button>
    </div>

    <!-- 主图画廊区域 -->
    <el-row :gutter="20" class="top-layout">
      <el-col :md="18">
        <div class="gallery-section">
          <el-image
              v-if="homestay.image"
              :key="homestay.image"
              :src="baseUrl + homestay.image"
              :preview-src-list="[baseUrl + homestay.image]"
              preview-teleported
              class="gallery-img,product-img"
          />
        </div>
      </el-col>
      <el-col :md="6">
        <div class="side-recommend-box">
          <h3 class="block-title">同类推荐</h3>
          <ul class="recommend-list">
            <li v-for="item in recommendList" :key="item.homestayId"
                class="recommend-item"
                @click="handleRecommendClick(item.homestayId)">
              {{ item.title }}
            </li>
          </ul>
        </div>
      </el-col>
    </el-row>

    <!-- 核心两列布局 -->
    <el-row :gutter="40" class="content-row">
      <!-- 左侧：基础信息、简介、评价 -->
      <el-col :xs="24" :sm="24" :md="16" :lg="16">


        <div class="main-info">
          <h1 class="title">{{ homestay.title }}</h1>

          <div class="tags-row">
            <el-tag v-for="tag in homestay.tags" :key="tag" class="info-tag" effect="plain" round>
              {{ tag }}
            </el-tag>
          </div>

          <div class="location-row">
            <el-icon class="loc-icon">
              <Location/>
            </el-icon>
            <span class="address-text">{{ homestay.address }}</span>
          </div>
        </div>

        <!-- 商品简介 -->
        <div class="section-block">
          <h3 class="block-title">商品简介</h3>
          <p class="description-text">
            {{ homestay.description }}
          </p>
        </div>

        <el-divider class="custom-divider"/>

        <!-- 用户评价 -->
        <div class="section-block">
          <h3 class="block-title">用户评价 <span class="review-count">({{ reviewList.length }})</span></h3>

          <div v-if="reviewList.length > 0" class="reviews-list">
            <div v-for="review in reviewList" :key="review.reviewId" class="review-item">
              <div class="reviewer-header">
                <el-avatar :size="44" :src="review.avatar ? baseUrl + review.avatar : defaultAvatar"
                           class="avatar-shadow"/>
                <div class="reviewer-details">
                  <div class="reviewer-name">{{ review.userName }}</div>
                  <div class="review-date">{{ review.createTime }}</div>
                </div>
                <div class="review-rating">
                  <el-rate v-model="review.score"
                           disabled
                           show-score
                           score-template="{value} 分"
                  />
                </div>
              </div>
              <div class="review-content">
                {{ review.content }}
              </div>
            </div>
          </div>

          <div v-else class="empty-wrapper">
            <el-empty description="该商品暂无评价" :image-size="120"/>
          </div>
        </div>
      </el-col>

      <!-- 右侧：交易/购买面板（固定的卡片） -->
      <el-col :xs="24" :sm="24" :md="8" :lg="8">
        <el-card class="booking-card" shadow="never">
          <!-- 价格头部 -->
          <div class="price-header">
            <span class="currency">¥</span>
            <span class="price">{{ homestay.price }}</span>
            <span class="unit">/件</span>
          </div>

          <!-- 库存状态 -->
          <div class="stock-row">
            <span class="stock-label">保障状态</span>
            <el-tag :type="homestay.stock > 0 ? 'success' : 'danger'" effect="light" class="stock-tag">
              {{ homestay.stock > 0 ? `当前库存 ${homestay.stock} 件` : '已售罄' }}
            </el-tag>
          </div>

          <el-divider class="card-inner-divider"/>

          <!-- 购买表单区 -->
          <div class="booking-form">
            <div class="input-with-label">
              <label class="input-label">购买数量</label>
              <el-input-number
                  v-model="nights"
                  :min="1"
                  :max="homestay.stock || 1"
                  :disabled="homestay.stock <= 0"
                  controls-position="right"
                  class="full-width-input"
              />
            </div>

            <div class="input-with-label">
              <label class="input-label">联系电话</label>
              <el-input v-model="contactPhone"
                        placeholder="请输入您的手机号"
                        prefix-icon="Cellphone"
                        class="full-width-input"
              />
            </div>

            <div class="input-with-label">
              <label class="input-label">收货地址</label>
              <el-input
                  v-model="shippingAddress"
                  placeholder="请填写详细收货地址"
                  clearable
                  class="full-width-input"
              >
                <template #prefix>
                  <el-icon class="input-icon-highlight">
                    <HomeFilled/>
                  </el-icon>
                </template>
              </el-input>
            </div>
          </div>

          <!-- 精致结算明细 -->
          <div class="calc-bill-box">
            <div class="calc-row">
              <span class="bill-label">商品单价</span>
              <span class="bill-value">¥{{ homestay.price }} × {{ nights }}</span>
            </div>
            <div class="calc-row total">
              <span class="bill-label-total">应付总额</span>
              <span class="bill-value-total">¥{{ totalPrice }}</span>
            </div>
          </div>

          <!-- 提交按钮 -->
          <el-button
              type="primary"
              class="book-btn"
              size="large"
              :disabled="homestay.stock <= 0"
              :class="{ 'is-disabled-btn': homestay.stock <= 0 }"
              @click="handleBooking"
          >
            {{ homestay.stock > 0 ? '立即购买' : '商品已售罄' }}
          </el-button>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import {computed, onMounted, ref} from "vue";
import {useRoute, useRouter} from "vue-router";
import {getHomestay, getRecommendList} from "@/api/homestay/homestay.js";
import {Location, HomeFilled} from "@element-plus/icons-vue";
import {ElMessage} from "element-plus";
import {addOrder} from "@/api/homestay/order.js";
import {selectReviewListByHomestayId} from "@/api/homestay/review.js";

//默认头像
import defaultAvatar from '@/assets/images/profile.jpg'
import useUserStore from "@/stores/modules/userStore.js";

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const recommendList = ref([]);

const fetchData = (id) => {
  // 获取详情
  getHomestay(id).then(res => {
    homestay.value = res.data;
    homestay.value.tags = res.data.tags ? res.data.tags.split(",") : [];

    // 调用推荐接口 (假设后端接口地址正确)
    getRecommendList(id).then(r => {
      recommendList.value = r.data;
    });
  });

  // 获取评价 (保持原有逻辑)
  selectReviewListByHomestayId(id).then(res => {
    reviewList.value = res.data;
  });
};

// 2. 重新编写总价计算
const totalPrice = computed(() => {
  if (!homestay.value.price) return 0
  return nights.value * homestay.value.price
})

//预订按钮
// 3. 购买按钮
const handleBooking = () => {
  // 校验库存
  if (homestay.value.stock <= 0) {
    ElMessage.error('商品库存不足')
    return
  }
  // 校验电话
  if (!contactPhone.value || contactPhone.value.trim().length === 0) {
    ElMessage.warning('请输入联系电话')
    return
  }
  // 校验收货地址
  if (!shippingAddress.value || shippingAddress.value.trim().length === 0) {
    ElMessage.warning('请输入收货地址')
    return
  }
  // 4. 创建订单数据对象
  const orderData = {
    homestayId: homestay.value.homestayId,
    quantity: nights.value,          // 传给后端的数量
    totalPrice: totalPrice.value,
    contactPhone: contactPhone.value,
    shippingAddress: shippingAddress.value // 传给后端的地址
  }

  // 发送请求
  addOrder(orderData).then(res => {
    ElMessage.success('下单成功~')
    router.push('/user/order')
    console.log("当前读取的环境变量:", import.meta.env.VITE_APP_BASE_API);
  }).catch(err => {
    ElMessage.error('下单失败，请稍后再试')
  })
}

//购买数量默认为1
const nights = ref(1)

//收获地址
const shippingAddress = ref('')

//联系电话
const contactPhone = ref('')

//基础api地址
const baseUrl = import.meta.env.VITE_APP_BASE_API

//路由参数中的民宿ID
const homestayId = route.params.id

//民宿数据
const homestay = ref({})

//评价列表
const reviewList = ref([])

onMounted(() => {
  // 使用 route.params.id 获取
  const id = route.params.id;
  fetchData(id);
  // 自动渲染当前登录用户的手机号
  userStore.getInfo().then(res => {
    if (res.data && (res.data.phonenumber || res.data.phone)) {
      contactPhone.value = res.data.phonenumber || res.data.phone
    }
  })
})

// 5. 增加一个跳转并刷新的方法，防止点击推荐商品后页面不更新
const handleRecommendClick = (id) => {
  router.push('/user/homestayDetail/' + id);
  // 关键：跳转后立即刷新数据
  fetchData(id);
}

</script>

<style scoped>
.gallery-section {
  width: 100%;
  height: 480px; /* 根据实际需要调整这个高度 */
  background-color: #f5f5f5; /* 可选：设置背景色，如果图片有空白可以更好看 */
  display: flex;
  justify-content: center;
  align-items: center;
  overflow: hidden;
  border-radius: 8px; /* 保持圆角 */
}

/* 图片样式，核心部分 */
.product-img {
  width: 100%;
  height: 100%; /* 图片继承容器高度 */
  cursor: zoom-in; /* 鼠标移入显示放大镜手势，提示可点击预览 */
  transition: transform 0.3s ease;
}

.product-img:hover {
  transform: scale(1.01); /* 鼠标悬浮轻微放大，提升交互感 */
}

.gallery-img {
  width: 100%;
  height: 400px; /* 建议设置一个合适的高度 */
  cursor: zoom-in; /* 鼠标移入显示放大镜手势 */
  border-radius: 8px;
  transition: transform 0.3s ease;
}

.gallery-img:hover {
  transform: scale(1.02); /* 悬浮微动，提升交互感 */
}

/* 全局布局与底色优化 */
.detail-page-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px 20px 60px 20px;
  background-color: #ffffff;
  color: #303133;
}

/* 返回按钮 */
.back-button-area {
  margin-bottom: 20px;
}

.custom-back-btn {
  border-radius: 6px;
  padding: 8px 16px;
  color: #606266;
  border-color: #dcdfe6;
  transition: all 0.2s;
}

.custom-back-btn:hover {
  color: #409eff;
  border-color: #c6e2ff;
  background-color: #ecf5ff;
}

/* 主图画廊区（升级为现代大平铺圆角级） */
.gallery-section {
  position: relative;
  width: 100%;
  height: 400px;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.06);
}

.gallery-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.5s cubic-bezier(0.4, 0, 0.2, 1);
}

.gallery-section:hover .gallery-img {
  transform: scale(1.02); /* 悬浮优雅微放大 */
}


/* 主体两列排版 */
.content-row {
  margin-top: 32px;
}

/* 左侧基础图文排版 */
.main-info {
  margin-bottom: 28px;
}

.main-info .title {
  font-size: 28px;
  font-weight: 700;
  color: #1f1f1f;
  margin: 0 0 16px 0;
  line-height: 1.3;
}

.tags-row {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 16px;
}

.info-tag {
  border-color: #e8e8e8;
  color: #595959;
  background-color: #fafafa;
}

.location-row {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #8c8c8c;
  font-size: 14px;
}

.loc-icon {
  color: #ff4d4f; /* 坐标红 */
  font-size: 16px;
}

/* 模块小标题 */
.section-block {
  margin-bottom: 32px;
}

.block-title {
  font-size: 18px;
  font-weight: 600;
  color: #262626;
  margin: 0 0 16px 0;
  display: flex;
  align-items: center;
  gap: 8px;
}

.review-count {
  font-size: 14px;
  font-weight: normal;
  color: #8c8c8c;
}

.description-text {
  font-size: 15px;
  line-height: 1.7;
  color: #515a6e;
  text-align: justify;
  white-space: pre-line;
}

.custom-divider {
  margin: 32px 0;
  border-top-color: #f0f0f0;
}

/* 评价列表卡片化优化 */
.reviews-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.review-item {
  padding: 20px;
  background-color: #fcfcfd;
  border: 1px solid #f0f0f2;
  border-radius: 8px;
  transition: all 0.2s;
}

.review-item:hover {
  background-color: #ffffff;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.03);
  border-color: #e4e7ed;
}

.reviewer-header {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
}

.avatar-shadow {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.reviewer-details {
  flex-grow: 1;
  margin-left: 12px;
}

.reviewer-name {
  font-size: 14px;
  font-weight: 600;
  color: #262626;
}

.review-date {
  font-size: 12px;
  color: #bfbfbf;
  margin-top: 2px;
}

.review-content {
  font-size: 14px;
  color: #434343;
  line-height: 1.6;
  padding-left: 56px; /* 与头像对齐线保持一致 */
}

/* 右侧预订表单卡片 */
.booking-card {
  position: sticky;
  top: 24px; /* 滚动时固定悬浮效果 */
  border: 1px solid #eef2f7;
  border-radius: 12px;
  padding: 4px;
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.03) !important;
}

/* 价格区 */
.price-header {
  display: flex;
  align-items: baseline;
  color: #ff4d4f;
  margin-bottom: 12px;
}

.price-header .currency {
  font-size: 18px;
  font-weight: 600;
  margin-right: 2px;
}

.price-header .price {
  font-size: 32px;
  font-weight: 700;
  font-family: 'Helvetica Neue', Arial, sans-serif;
}

.price-header .unit {
  font-size: 14px;
  color: #8c8c8c;
  margin-left: 2px;
}

/* 库存 */
.stock-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 13px;
}

.stock-label {
  color: #8c8c8c;
}

.stock-tag {
  border-radius: 4px;
  font-weight: 500;
}

.card-inner-divider {
  margin: 16px 0;
  border-top-style: dashed;
}

/* 表单输入框元素 */
.booking-form {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.input-with-label {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.input-label {
  font-size: 13px;
  font-weight: 500;
  color: #5a5a5a;
}

.full-width-input {
  width: 100% !important;
}

.full-width-input :deep(.el-input__wrapper) {
  padding: 4px 12px;
}

.input-icon-highlight {
  color: #1890ff;
}

/* 账单盒明细 */
.calc-bill-box {
  margin-top: 20px;
  background-color: #fafafa;
  border-radius: 8px;
  padding: 12px 16px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.calc-row {
  display: flex;
  justify-content: space-between;
  font-size: 13px;
  color: #8c8c8c;
}

.calc-row.total {
  margin-top: 4px;
  padding-top: 8px;
  border-top: 1px dashed #e8e8e8;
  align-items: baseline;
}

.bill-value {
  color: #434343;
  font-family: monospace;
}

.bill-label-total {
  font-size: 14px;
  font-weight: 600;
  color: #262626;
}

.bill-value-total {
  font-size: 22px;
  font-weight: 700;
  color: #ff4d4f;
  font-family: 'Helvetica Neue', Arial, sans-serif;
}

/* 购买动作大按钮 */
.book-btn {
  width: 100%;
  height: 48px;
  font-size: 16px;
  font-weight: 600;
  border-radius: 8px;
  margin-top: 20px;
  background: linear-gradient(135deg, #ff7875 0%, #ff4d4f 100%);
  border: none;
  box-shadow: 0 4px 12px rgba(255, 77, 79, 0.3);
  transition: all 0.25s;
}

.book-btn:hover:not(.is-disabled) {
  transform: translateY(-1px);
  box-shadow: 0 6px 16px rgba(255, 77, 79, 0.4);
}

.is-disabled-btn {
  background: #f5f5f5 !important;
  color: #b7b7b7 !important;
  box-shadow: none !important;
}

/* 顶部布局微调 */
.top-layout {
  margin-bottom: 32px;
}

/* 右侧推荐框样式 */
.side-recommend-box {
  background: #f9f9f9;
  padding: 16px;
  border-radius: 12px;
  height: 400px; /* 与主图高度一致 */
  border: 1px solid #eee;
}

.recommend-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.recommend-item {
  padding: 12px 0;
  border-bottom: 1px solid #eee;
  cursor: pointer;
  font-size: 14px;
  color: #606266;
  transition: color 0.2s;
  /* 文本溢出处理 */
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.recommend-item:hover {
  color: #409eff;
}

/* 移除旧的冗余样式 */
.recommend-section, .recommend-card, .rec-img, .rec-info {
  display: none; /* 或者直接从样式表中删除这些代码块 */
}

.empty-wrapper {
  padding: 30px 0;
}

</style>
