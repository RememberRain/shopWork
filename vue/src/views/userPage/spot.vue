<template>
  <div class="market-page-wrapper">
    <div class="market-container">

      <!-- 1. 顶部全景搜索栏（立体聚合感重构） -->
      <div class="search-section">
        <el-input v-model="query.name"
                  placeholder="搜索您心仪的特卖商品..."
                  clearable
                  size="large"
                  @clear="handleQuery"
                  @keyup.enter="handleQuery"
                  class="custom-search"
        >
          <template #prefix>
            <el-icon class="search-icon"><Search/></el-icon>
          </template>
          <template #append>
            <el-button @click="handleQuery" class="search-btn">搜 索</el-button>
          </template>
        </el-input>
      </div>

      <!-- 2. 商品货架网格区（引入自适应弹性响应式断点） -->
      <div class="goods-shelf">
        <el-row :gutter="24">
          <el-col
              :xs="24"
              :sm="12"
              :md="8"
              :lg="6"
              v-for="spot in spotList"
              :key="spot.spotId"
              class="shelf-col"
          >
            <el-card :body-style="{ padding: '0px' }"
                     class="product-card"
                     shadow="never"
                     @click="goToSpotDetail(spot.spotId)"
            >
              <!-- 图片视口，加入悬浮缩放动效 -->
              <div class="card-image-wrapper">
                <img :src="baseUrl + spot.image" class="product-img" alt="商品图片">
                <div class="img-mask"></div>
              </div>

              <!-- 文本叙述区：严格控制行高、字阶防崩塌 -->
              <div class="card-info">
                <h3 class="product-name" :title="spot.name">{{ spot.name }}</h3>
                <p class="product-desc" :title="spot.description">{{ spot.description }}</p>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </div>

      <!-- 3. 底部分页（靠右优雅对齐，拉开安全间距） -->
      <div class="pagination-section" v-show="total > 0">
        <pagination :total="total"
                    v-model:page="query.pageNum"
                    v-model:limit="query.pageSize"
                    @pagination="getList"
                    :pageSizes="[8,16,32]"
        />
      </div>

    </div>
  </div>
</template>

<script setup>

import {onMounted, ref} from "vue";
import {Search} from "@element-plus/icons-vue";
import {selectSpotList} from "@/api/homestay/spot.js";
import Pagination from "@/components/Pagination/index.vue";
import {useRouter} from "vue-router";


//基础api地址
const baseUrl = import.meta.env.VITE_APP_BASE_API

//查询参数
const query = ref({
  pageNum: 1,
  pageSize: 8,
  name: null,
})

//
const router = useRouter()

//列表数据
const spotList = ref([])

const getList = () => {
  selectSpotList(query.value).then(res => {
    spotList.value = res.rows
    total.value = res.total
  })
}
//数据总数
const total = ref(0)

//跳转到商品详情
const goToSpotDetail = (spotId) => {
  router.push('/user/spotDetail/' + spotId)
}

//搜索按钮
const handleQuery = () => {
  query.value.pageNum = 1
  getList()
}


onMounted(() => {
  getList()
})
</script>

<style scoped>
/* 外部全局底色及字体平滑 */
.market-page-wrapper {
  background-color: #f8fafc; /* 现代低饱和度浅灰蓝底 */
  min-height: 100vh;
  padding: 30px 0 60px 0;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
}

/* 核心视口容器 */
.market-container {
  width: 100%;
  max-width: 1300px;
  margin: 0 auto;
  padding: 0 24px;
  box-sizing: border-box;
}

/* ================= 1. 顶部搜索区域 ================= */
.search-section {
  margin-bottom: 28px;
  display: flex;
  justify-content: flex-start;
}
.custom-search {
  width: 100%;
  max-width: 420px;
}
/* 输入框外壳现代感圆角与阴影 */
.custom-search :deep(.el-input__wrapper) {
  border-radius: 10px 0 0 10px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.03) !important;
  border: 1px solid #e2e8f0;
  border-right: none;
  padding-left: 14px;
}
.custom-search :deep(.el-input__wrapper.is-focus) {
  border-color: #409eff;
}
/* 搜索按钮合并区美化 */
.custom-search :deep(.el-input-group__append) {
  background-color: #409eff;
  border: 1px solid #409eff;
  border-radius: 0 10px 10px 0;
  color: #ffffff;
  font-weight: 500;
  padding: 0 22px;
}
.search-icon {
  color: #94a3b8;
  font-size: 15px;
}

/* ================= 2. 商品卡片货架 ================= */
.goods-shelf {
  min-height: 400px;
}
.shelf-col {
  margin-bottom: 24px; /* 严格控制卡片纵向间距 */
}

.product-card {
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  background: #ffffff;
  cursor: pointer;
  overflow: hidden;
  /* 运用贝塞尔曲线，让悬浮过渡更丝滑 */
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

/* 鼠标悬停升空效果 */
.product-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 12px 24px rgba(148, 163, 184, 0.12) !important;
  border-color: #cbd5e1;
}

/* 图片裁剪容器 */
.card-image-wrapper {
  position: relative;
  width: 100%;
  height: 190px; /* 微调黄金高度比例 */
  overflow: hidden;
  background-color: #f1f5f9;
}
.product-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.5s ease;
}
.product-card:hover .product-img {
  transform: scale(1.04); /* 触碰时图片优雅放大 */
}
/* 图片浅色遮罩，提升质感 */
.img-mask {
  position: absolute;
  top: 0; left: 0; width: 100%; height: 100%;
  background: rgba(0, 0, 0, 0.01);
  pointer-events: none;
}

/* ================= 3. 卡片文本排版区 ================= */
.card-info {
  padding: 18px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

/* 商品名称：单行截断，防止长名字换行把卡片顶高 */
.product-name {
  font-size: 16px;
  font-weight: 600;
  color: #1e293b;
  margin: 0;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  line-height: 1.2;
}

/* 商品简介：严格锁定双行高度，超出自动 ellipsis */
.product-desc {
  font-size: 13px;
  color: #64748b;
  margin: 0;
  line-height: 1.5;
  height: 38px; /* 严格固定双行高度，保证整行卡片底边绝对对齐 */
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
  text-align: justify;
}

/* ================= 4. 底部分页重构 ================= */
.pagination-section {
  margin-top: 36px;
  display: flex;
  justify-content: flex-end; /* 修改为右对齐，更符合现代复杂电商平台的动线 */
}
</style>
