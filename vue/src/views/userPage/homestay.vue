<template>
  <div class="homestay-page-container">

    <!-- 1. 顶部全景搜索栏（增加微弱立体投影与聚合感） -->
    <div class="search-wrapper">
      <div class="search-box">
        <el-input v-model="query.title"
                  placeholder="开始寻找您喜爱的商品..."
                  clearable
                  size="large"
                  @clear="handleQuery"
                  @keyup.enter="handleQuery"
                  class="custom-search-input"
        >
          <template #prefix>
            <el-icon class="search-icon"><Search/></el-icon>
          </template>
          <template #append>
            <el-button type="primary" @click="handleQuery" class="search-btn">搜索</el-button>
          </template>
        </el-input>
      </div>
    </div>

    <!-- 2. 民宿瀑布流卡片区（引入全维度响应式弹性断点） -->
    <div class="list-section">
      <el-row :gutter="24">
        <el-col
            :xs="24"
            :sm="12"
            :md="8"
            :lg="6"
            v-for="homestay in homestayList"
            :key="homestay.homestayId"
            class="card-col-item"
        >
          <el-card
              :body-style="{ padding: '0px' }"
              class="homestay-card"
              shadow="never"
              @click="router.push('/user/homestayDetail/' + homestay.homestayId)"
          >
            <!-- 图片视口：固定黄金比例 + 悬浮滤镜 -->
            <div class="card-image-box">
              <img :src="baseUrl + homestay.image" alt="商品外观" class="homestay-img">
              <div class="image-overlay"></div>
              <!-- 价格标签精细化：融入玻璃拟态半透明底色 -->
              <div class="price-tag">
                <span class="currency">¥</span>
                <span class="address-text">单价:</span>
                <span class="amount">{{ homestay.price }}</span>

              </div>
            </div>

            <!-- 卡片文本区：严格控制字阶与行高 -->
            <div class="card-content">
              <!-- 标题防溢出：最多展示两行，超出自动 ellipsis -->
              <h3 class="card-title" :title="homestay.title">{{ homestay.title }}</h3>

              <!-- 地址防溢出：单行截断 -->
              <div class="address-row">
                <el-icon class="location-icon"><Location/></el-icon>
                <span class="address-text" :title="homestay.address">发货地址：{{ homestay.address }}</span>
              </div>

              <!-- 标签流 -->
              <div class="tags-wrapper" v-if="homestay.tags">
                <el-tag
                    v-for="tag in homestay.tags.split(',')"
                    :key="tag"
                    size="small"
                    effect="plain"
                    class="custom-tag"
                >
                  {{ tag }}
                </el-tag>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 3. 底部分页（右对齐并拉开安全呼吸间距） -->
    <div class="pagination-section" v-show="total > 0">
      <pagination
          :total="total"
          v-model:page="query.pageNum"
          v-model:limit="query.pageSize"
          @pagination="getList"
      />
    </div>

  </div>
</template>

<script setup>
import {onMounted, ref} from "vue";
import {Location, Search} from "@element-plus/icons-vue";
import {listHomestay} from "@/api/homestay/homestay.js";
import {useRouter} from "vue-router";

const router = useRouter()

//基础api地址
const baseUrl = import.meta.env.VITE_APP_BASE_API

//查询参数
const query = ref({
  pageNum: 1,
  pageSize: 8,
  title: null
})

//搜索按钮
const handleQuery = () => {
  query.value.pageNum = 1
  getList()
}

//民宿列表数据
const homestayList = ref([])

//数据总数
const total = ref(0)

//查询列表数据
const getList = () => {
  listHomestay(query.value).then(res => {
    homestayList.value = res.rows
    total.value = res.total
  })
}

onMounted(() => {
  getList()
})

</script>

<style scoped>
/* 全局雅致浅灰底色，衬托白卡片 */
.homestay-page-container {
  max-width: 1280px;
  margin: 0 auto;
  padding: 24px 20px 60px 20px;
  background-color: #f8fafc;
  min-height: 100vh;
}

/* ================= 1. 顶部搜索栏高级重塑 ================= */
.search-wrapper {
  display: flex;
  justify-content: center;
  margin-bottom: 32px;
}
.search-box {
  width: 100%;
  max-width: 600px;
}
.custom-search-input :deep(.el-input__wrapper) {
  border-radius: 12px 0 0 12px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.04) !important;
  padding-left: 16px;
  border: 1px solid #e2e8f0;
  border-right: none;
}
.custom-search-input :deep(.el-input-group__append) {
  background-color: #409eff;
  border-radius: 0 12px 12px 0;
  border: none;
  box-shadow: 0 4px 16px rgba(64, 158, 255, 0.2);
}
.custom-search-input :deep(.el-input-group__append button.el-button) {
  color: #ffffff;
  font-weight: 600;
  padding: 0 24px;
  height: 100%;
}
.search-icon {
  color: #94a3b8;
  font-size: 16px;
}

/* ================= 2. 民宿响应式网格与卡片 ================= */
.card-col-item {
  margin-bottom: 24px; /* 卡片纵向间距 */
}

.homestay-card {
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  background: #ffffff;
  cursor: pointer;
  overflow: hidden;
  transition: all 0.35s cubic-bezier(0.4, 0, 0.2, 1);
}

/* 卡片悬浮升空动效 */
.homestay-card:hover {
  transform: translateY(-6px);
  box-shadow: 0 12px 24px rgba(148, 163, 184, 0.15) !important;
  border-color: #cbd5e1;
}

/* 图片剪裁视口 */
.card-image-box {
  position: relative;
  width: 100%;
  height: 200px; /* 黄金固定截断高度 */
  overflow: hidden;
  background-color: #f1f5f9;
}
.homestay-img {
  width: 100%;
  height: 100%;
  object-fit: cover; /* 核心：防止后台宽窄图拉伸变形 */
  transition: transform 0.5s ease;
}
.homestay-card:hover .homestay-img {
  transform: scale(1.05); /* 悬浮图面优雅微拉近 */
}

/* 图片暗底渐变（为了衬托白色价格字） */
.image-overlay {
  position: absolute;
  top: 0; left: 0; width: 100%; height: 100%;
  background: linear-gradient(to top, rgba(0, 0, 0, 0.35) 0%, rgba(0, 0, 0, 0) 40%);
  pointer-events: none;
}

/* 玻璃拟态悬浮价格签 */
.price-tag {
  position: absolute;
  left: 12px;
  bottom: 12px;
  color: #ffffff;
  background: rgba(0, 0, 0, 0.55);
  backdrop-filter: blur(4px); /* 现代毛玻璃效果 */
  padding: 4px 10px;
  border-radius: 6px;
  display: flex;
  align-items: baseline;
  line-height: 1;
}
.price-tag .currency {
  font-size: 12px;
  font-weight: 600;
  margin-right: 2px;
}
.price-tag .amount {
  font-size: 18px;
  font-weight: 700;
  font-family: 'Helvetica Neue', Arial, sans-serif;
}
.price-tag .unit {
  font-size: 11px;
  opacity: 0.8;
  margin-left: 2px;
}

/* ================= 3. 卡片文本信息排版 ================= */
.card-content {
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

/* 标题双行安全截断 */
.card-title {
  font-size: 15px;
  font-weight: 600;
  color: #1e293b;
  margin: 0;
  line-height: 1.4;
  height: 42px; /* 严格双行高度锁定，确保卡片对齐 */
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* 地址单行截断线 */
.address-row {
  display: flex;
  align-items: center;
  color: #64748b;
  font-size: 13px;
}
.location-icon {
  flex-shrink: 0;
  color: #ef4444; /* 经典地标红 */
  margin-right: 4px;
}
.address-text {
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
  width: 100%;
}

/* 标签流限制 */
.tags-wrapper {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  height: 24px; /* 锁定单行高度，多余的自然隐藏，防止撑破卡片 */
  overflow: hidden;
  margin-top: 2px;
}
.custom-tag {
  border-color: #e2e8f0;
  color: #475569;
  background-color: #f8fafc;
  border-radius: 4px;
}

/* ================= 4. 分页器右对齐 ================= */
.pagination-section {
  margin-top: 36px;
  display: flex;
  justify-content: flex-end;
}
</style>
