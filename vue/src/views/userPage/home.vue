<template>
  <div class="homepage-wrapper">
    <div class="main-container">


      <div class="search-container">
        <el-input
            v-model="searchKeyword"
            placeholder="搜索您心仪的商品..."
            prefix-icon="Search"
            class="custom-search-input"
            @keyup.enter="handleSearch"
            clearable
            @clear="handleSearch"
        >
          <template #append>
            <el-button icon="Search" @click="handleSearch" />
          </template>
        </el-input>
      </div>


      <div class="top-section" v-if="!isSearching">

        <el-row :gutter="24">
          <el-col :lg="18" :md="18" :sm="24">
            <el-carousel :interval="5500" arrow="always" height="420px" class="custom-carousel">
              <el-carousel-item v-for="spot in spotList" :key="spot.spotId">
                <div class="carousel-item-box" @click="router.push('/user/spotDetail/' + spot.spotId)">
                  <img :src="baseUrl + spot.image" alt="特色商品" class="carousel-img">
                  <div class="carousel-overlay"></div>
                  <div class="carousel-content">
                    <h3 class="carousel-title">{{ spot.name }}</h3>
                    <p class="carousel-desc">{{ spot.description }}</p>
                  </div>
                </div>
              </el-carousel-item>
            </el-carousel>
          </el-col>

          <el-col :lg="6" :md="6" :sm="0" class="hidden-sm-and-down">
            <div class="rank-card">
              <h3 class="rank-title">销量排行榜 TOP9</h3>
              <div class="rank-list">
                <div v-for="(item, index) in rankList" :key="item.homestayId" class="rank-item"
                     @click="router.push('/user/homestayDetail/' + item.homestayId)">
                  <span :class="['rank-num', index < 3 ? 'top-three' : '']">{{ index + 1 }}</span>
                  <span class="rank-name">{{ item.title }}</span>
                  <span class="rank-sales">{{ item.sales }}人已购</span>
                </div>
              </div>
            </div>
          </el-col>
        </el-row>
      </div>

      <div class="section-block">

        <div class="search-result-header" v-if="isSearching">
          <el-button class="back-btn" link @click="handleTabChange('all')">
            <el-icon><ArrowLeft /></el-icon>
            <span class="btn-text">返回首页</span>
          </el-button>

          <div class="result-summary">
            <span class="label">为您找到以下结果：</span>
          </div>
        </div>

        <div class="tab-header" v-if="!isSearching">>
          <h2 class="section-main-title">发现好物</h2>
          <div class="tabs">
            <span v-for="tab in tabList" :key="tab.value"
                  :class="['tab-item', activeTab === tab.value ? 'active' : '']"
                  @click="handleTabChange(tab.value)">
              {{ tab.label }}
            </span>
          </div>
        </div>

        <el-row :gutter="24" v-loading="loading">
          <el-col :xs="24" :sm="12" :md="8" :lg="6" v-for="item in homestayList" :key="item.homestayId"
                  class="grid-col-item">
            <el-card :body-style="{padding: '0px'}" class="homestay-card" shadow="hover"
                     @click="router.push('/user/homestayDetail/' + item.homestayId)">
              <div class="card-image-box">
                <img :src="baseUrl + item.image" class="card-img" loading="lazy">
                <div class="price-tag">¥ {{ item.price }}</div>
              </div>
              <div class="card-content-box">
                <h3 class="card-title">{{ item.title }}</h3>
                <div class="location-row">
                  <el-icon class="loc-icon">
                    <Location/>
                  </el-icon>
                  <span class="address-text">{{ item.address }}</span>
                </div>
              </div>
            </el-card>
          </el-col>
        </el-row>

        <div class="load-more-box">
          <el-button @click="loadMore" :loading="loading" round plain>
            {{ hasMore ? '加载更多' : '没有更多了' }}
          </el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import {ref, onMounted} from "vue";
import {useRouter} from "vue-router";
import {Location, ArrowLeft } from "@element-plus/icons-vue";
import {selectSpotList} from "@/api/homestay/spot.js";
import {listHomestay, getTopSalesList} from "@/api/homestay/homestay.js";

const router = useRouter();
const baseUrl = import.meta.env.VITE_APP_BASE_API;

const spotList = ref([]);
const rankList = ref([]);
const homestayList = ref([]);
const loading = ref(false);
const hasMore = ref(true);
const activeTab = ref('all');
const searchKeyword = ref('');
const isSearching = ref(false); // 状态标记


const queryParams = ref({pageNum: 1, pageSize: 8, tags: '', title: '' });

const tabList = [
  {label: '全部推荐', value: 'all'},
  {label: '数码产品', value: '数码'},
  {label: '时尚穿搭', value: '穿搭'},
  {label: '舌尖美味', value: '食品'}
];

const handleSearch = () => {
  if (!searchKeyword.value) {
    handleTabChange('all');
    return;
  }
  queryParams.value.title = searchKeyword.value;
  queryParams.value.tags = '';     // 搜索时优先关键词

  isSearching.value = true;        // 明确：关键词搜索进入搜索模式
  fetchHomestayList(true);
};

const initData = () => {
  // 获取轮播图
  selectSpotList({pageNum: 1, pageSize: 3}).then(res => {
    spotList.value = res.rows;
  });

  // 获取排行榜 (TOP 10)
  getTopSalesList().then(res => {
    rankList.value = res.data || [];
  });

  // 获取初始商品列表
  fetchHomestayList(true);
};


const fetchHomestayList = (reset = false) => {
  if (loading.value) return;
  if (reset) {
    queryParams.value.pageNum = 1;
  }

  loading.value = true;
  // 如果输入了标题或者选择了非全部的标签，则标记为搜索模式
  isSearching.value = (queryParams.value.title !== '' || queryParams.value.tags !== '');

  listHomestay(queryParams.value)
      .then(res => {
        const rows = res.rows || [];

        // 1. 如果是重置，直接覆盖；否则计算去重后的新数据
        if (reset) {
          homestayList.value = rows;
          hasMore.value = rows.length >= queryParams.value.pageSize;
        } else {
          // 筛选出那些在现有列表中不存在的数据
          const newItems = rows.filter(item =>
              !homestayList.value.some(existing => existing.homestayId === item.homestayId)
          );

          // 说明后端返回了重复内容，此时强制视为“没有更多了”
          if (newItems.length === 0 && rows.length > 0) {
            hasMore.value = false; // 触发“没有更多了”
          } else {
            homestayList.value = [...homestayList.value, ...newItems];
            // 只有当有新数据且数量满足分页大小时，才允许继续加载
            hasMore.value = rows.length >= queryParams.value.pageSize;
          }
        }
      })
      .finally(() => {
        loading.value = false;
      });
}


const handleTabChange = (val) => {
  activeTab.value = val;
  searchKeyword.value = '';
  queryParams.value.title = '';
  queryParams.value.tags = (val === 'all' ? '' : val);
  isSearching.value = false;       // 明确：标签筛选不进入搜索模式
  fetchHomestayList(true);
};

const loadMore = () => {
  if (!hasMore.value) return;
  queryParams.value.pageNum++;
  fetchHomestayList(false);
};

onMounted(initData);


</script>

<style scoped>
/* 全局雅致浅灰蓝底色 */
.homepage-wrapper {
  background-color: #f8fafc;
  min-height: 100vh;
  padding: 10px 0 60px 0;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
}

.main-container {
  max-width: 1240px;
  margin: 0 auto;
  padding: 0 20px;
  box-sizing: border-box;
}

.section-block {
  margin-bottom: 48px;
}

.grid-section {
  margin-top: 12px;
}

/* ================= 样式重塑：高阶头部区域 ================= */
.section-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  margin-bottom: 20px;
}

.title-area {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.section-main-title {
  font-size: 24px;
  font-weight: 700;
  color: #1e293b;
  margin: 0;
  letter-spacing: -0.5px;
}

.section-sub-title {
  font-size: 13px;
  color: #94a3b8;
}

/* “更多”按钮精致化 */
.more-btn {
  border-color: #e2e8f0;
  color: #475569;
  font-weight: 500;
  padding: 8px 16px;
  transition: all 0.2s ease;
}

.more-btn:hover {
  color: #409eff;
  border-color: #c6e2ff;
  background-color: #ecf5ff;
}

.btn-icon {
  font-size: 12px;
  margin-left: 2px;
  transition: transform 0.2s ease;
}

.more-btn:hover .btn-icon {
  transform: translateX(2px); /* 悬浮时箭头微右移交互 */
}

/* ================= 样式重塑：大轮播区 ================= */
.custom-carousel {
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 10px 30px rgba(15, 23, 42, 0.06);
}

.carousel-item-box {
  position: relative;
  width: 100%;
  height: 100%;
  cursor: pointer;
}

.carousel-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.6s cubic-bezier(0.25, 1, 0.5, 1);
}

.carousel-item-box:hover .carousel-img {
  transform: scale(1.02); /* 悬浮时大图深呼吸感微放大 */
}

/* 关键防倾轧黑渐变层 */
.carousel-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(to top, rgba(0, 0, 0, 0.65) 0%, rgba(0, 0, 0, 0.2) 50%, rgba(0, 0, 0, 0) 100%);
  pointer-events: none;
}

.carousel-content {
  position: absolute;
  left: 40px;
  bottom: 40px;
  right: 40px;
  color: #ffffff;
  z-index: 2;
}

.carousel-title {
  font-size: 28px;
  font-weight: 700;
  margin: 0 0 12px 0;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.15);
}

.carousel-desc {
  font-size: 15px;
  line-height: 1.6;
  color: rgba(255, 255, 255, 0.88);
  margin: 0;
  max-width: 680px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
}

/* ================= 样式重塑：下属卡片网格 ================= */
.grid-col-item {
  margin-bottom: 24px;
}

.homestay-card {
  border: none !important; /* 移除硬边框，改用轻微阴影 */
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.05), 0 2px 4px -1px rgba(0, 0, 0, 0.03);
  transition: all 0.4s cubic-bezier(0.165, 0.84, 0.44, 1);
}

/* 卡片悬浮漂浮投影 */
.homestay-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 10px 10px -5px rgba(0, 0, 0, 0.04);
}

.card-image-box {
  position: relative;
  width: 100%;
  height: 180px;
  overflow: hidden;
  background-color: #f1f5f9;
}

.card-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.5s ease;
}

.homestay-card:hover .card-img {
  transform: scale(1.05);
}

/* 毛玻璃拟态高级价签 */
.price-tag {
  left: 10px;
  bottom: 10px;
  background: rgba(255, 255, 255, 0.9);
  color: #1e293b;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  padding: 4px 12px;
  border-radius: 20px; /* 圆角标签更具时尚感 */
  font-weight: 700;
}

.price-tag .currency {
  font-size: 11px;
  font-weight: 600;
  margin-right: 1px;
}

.price-tag .amount {
  font-size: 17px;
  font-weight: 700;
  font-family: 'Helvetica Neue', Arial, sans-serif;
}

.price-tag .unit {
  font-size: 11px;
  opacity: 0.85;
  margin-left: 1px;
}

/* 严格行高对齐防崩塌文本区 */
.card-content-box {
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

/* 标题限制双行高度锁定，确保多列完美卡线对齐 */
.card-title {
  font-size: 15px;
  font-weight: 600;
  color: #1e293b;
  margin: 0;
  line-height: 1.4;
  height: 42px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* 地址单行限宽 */
.location-row {
  display: flex;
  align-items: center;
  color: #64748b;
  font-size: 13px;
}

.loc-icon {
  flex-shrink: 0;
  color: #ef4444;
  margin-right: 4px;
}

.address-text {
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
  width: 100%;
}


/* 覆盖 Element Carousel 原生指示器小细节（让其变得更扁平高级） */
:deep(.el-carousel__indicator--horizontal button) {
  height: 4px;
  border-radius: 2px;
  background-color: rgba(255, 255, 255, 0.4);
}

:deep(.el-carousel__indicator--horizontal.is-active button) {
  background-color: #ffffff;
  width: 24px; /* 激活态拉长条，现代主流规范 */
}

/* 排行榜样式 */
.rank-card {
  background: #fff;
  padding: 20px;
  border-radius: 12px;
  height: 420px;
  border: 1px solid #e2e8f0;
}

.rank-item {
  padding: 14px 10px;
  border-radius: 8px;
  transition: background 0.3s;
}

.rank-num {
  width: 20px;
  font-weight: bold;
  color: #94a3b8;
}

.top-three {
  color: #f59e0b;
}

.rank-name {
  flex: 1;
  margin: 0 10px;
  font-size: 14px;
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}

/* 标签切换栏 */
.tab-header {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
}

.tab-item {
  margin-left: 30px;
  cursor: pointer;
  padding-bottom: 4px;
  color: #64748b;
}

.tab-item.active {
  color: #409eff;
  border-bottom: 2px solid #409eff;
  font-weight: 600;
}

.load-more-box {
  text-align: center;
  margin-top: 40px;
}

/* 排行榜样式 */
.rank-card {
  background: #fff;
  padding: 20px;
  border-radius: 12px;
  height: 420px;
  border: 1px solid #e2e8f0;
}

.rank-item {
  display: flex;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid #f1f5f9;
}

.rank-num {
  width: 20px;
  font-weight: bold;
  color: #94a3b8;
}

.top-three {
  color: #f59e0b;
}

.rank-name {
  flex: 1;
  margin: 0 10px;
  font-size: 14px;
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
}

/* 标签切换栏 */
.tab-header {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
}

.tab-item {
  margin-left: 30px;
  cursor: pointer;
  padding-bottom: 4px;
  color: #64748b;
}

.tab-item.active {
  color: #409eff;
  border-bottom: 2px solid #409eff;
  font-weight: 600;
}

.load-more-box {
  text-align: center;
  margin-top: 40px;
}

.load-more-box .el-button {
  padding: 12px 40px;
  font-size: 15px;
  border-width: 2px;
}

/* 提升排行榜视觉层级 */
.rank-title {
  font-size: 16px;
  margin-top: 0;
  margin-bottom: 15px;
  color: #334155;
  border-left: 4px solid #409eff;
  padding-left: 10px;
}

.rank-item:hover {
  background-color: #f1f5f9;
}

.top-three {
  color: #ef4444 !important; /* 前三名醒目的红色 */
  font-weight: 800;
}

.rank-sales {
  font-size: 12px;
  color: #94a3b8;
}

/* 标签栏优化 */
.tab-header {
  border-bottom: 1px solid #e2e8f0;
  padding-bottom: 10px;
  margin-bottom: 20px;
}


.search-container {
  display: flex;
  justify-content: center;
  margin: 20px 0;
}

.custom-search-input {
  width: 100%;
  max-width: 600px;
}

/* 赋予搜索框圆润、现代的阴影感 */
:deep(.custom-search-input .el-input__wrapper) {
  border-radius: 24px;
  padding: 0 20px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}


.search-result-header {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  margin-bottom: 24px;
  background-color: #f8fafc;
  border-radius: 8px;
  border: 1px solid #e2e8f0;
}

.back-btn {
  font-size: 14px;
  color: #475569;
  transition: all 0.2s;
}

.back-btn:hover {
  color: #3b82f6;
}

.result-summary {
  margin-left: 16px;
  padding-left: 16px;
  border-left: 2px solid #cbd5e1;
  color: #64748b;
  font-size: 14px;
}

.count {
  font-weight: 600;
  color: #0f172a;
  margin-left: 4px;
}
</style>
