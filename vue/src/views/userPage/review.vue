<template>
  <div class="review-page-container">
    <div class="review-card">

      <div class="review-header">
        <div class="header-title">
          <el-icon class="title-icon">
            <ChatLineRound/>
          </el-icon>
          <h2>我的评价中心</h2>
        </div>
        <p class="header-desc">在这里查看您对商品的每一份反馈，以及商家的精选回复。</p>
      </div>

      <div class="review-list-wrapper">
        <el-table
            v-loading="loading"
            :data="reviewList"
            style="width: 100%"
            :row-class-name="tableRowClassName"
            empty-text="暂无评价记录"
        >
          <el-table-column label="评价商品" width="200">
            <template #default="scope">
              <span class="product-title-link" @click="goToProduct(scope.row)">
                {{ scope.row.title || '未知商品' }}
              </span>
            </template>
          </el-table-column>

          <el-table-column label="我的评分" width="200">
            <template #default="scope">
              <el-rate v-model="scope.row.score" disabled show-score score-template="{value} 分"/>
            </template>
          </el-table-column>

          <el-table-column label="评价内容" min-width="240">
            <template #default="scope">
              <div class="user-content-box">
                <p class="review-text">{{ scope.row.content }}</p>
                <span class="review-time">发表于：{{ scope.row.createTime }}</span>
              </div>
            </template>
          </el-table-column>

          <el-table-column label="商家回复反馈" min-width="280">
            <template #default="scope">
              <div v-if="scope.row.replyContent" class="reply-container">
                <div class="reply-badge">
                  <el-icon>
                    <Check/>
                  </el-icon>
                  商家已回
                </div>
                <p class="reply-text">{{ scope.row.replyContent }}</p>
                <span class="reply-time">回复于：{{ scope.row.replyTime }}</span>
              </div>

              <div v-else class="no-reply">
                <el-icon class="sand-icon">
                  <Compass/>
                </el-icon>
                <span>期待商家回复中...</span>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>

    </div>
    <!-- 右对齐分页 -->
    <div class="pagination-container">
      <pagination
          v-show="total>0"
          :total="total"
          v-model:page="queryParams.pageNum"
          v-model:limit="queryParams.pageSize"
          @pagination="getList"
      />
    </div>
  </div>
</template>

<script setup>
import {onMounted, ref, watch} from "vue";
import {useRoute, useRouter} from "vue-router";
import {ElMessage} from "element-plus";
import {ChatLineRound, Check, Compass} from "@element-plus/icons-vue";

// 导入拉取历史评价列表的专用 API 函数
import {getMyReviewList} from "@/api/homestay/review.js";

const route = useRoute();
const router = useRouter();

const loading = ref(false);
const reviewList = ref([]);

// 获取当前登录用户的所有历史评价记录

// 总记录数
const total = ref(0);

// 列表数据
const list = ref([]);
const queryParams = ref({
  pageNum: 1,
  pageSize: 7,
});



const getList = () => {
  loading.value = true;

  // 使用响应式的 queryParams 对象
  getMyReviewList(queryParams.value)
      .then((res) => {
        reviewList.value = res.rows || [];
        total.value = res.total || 0;
        handleHighlightRow();
      })
      .catch((error) => {
        console.error("加载失败", error);
      })
      .finally(() => {
        loading.value = false;
      });
};

// 2. 路由传参联动：如果带着消息 ID 降落，为那一行标记高亮类名
const tableRowClassName = ({row}) => {
  const targetReviewId = route.query.reviewId;
  if (targetReviewId && String(row.reviewId) === String(targetReviewId)) {
    return "highlight-message-row";
  }
  return "";
};

// 3. 气泡提示
const handleHighlightRow = () => {
  if (route.query.reviewId) {
    ElMessage({
      message: "已为您自动定位到该条商家回复信息",
      type: "success",
      duration: 4000,
    });
  }
};

// 4. 点击商品名称支持直接跳回对应的明细页面
const goToProduct = (row) => {
  if (row.homestayId) {
    router.push(`/user/homestayDetail/${row.homestayId}`);
  }
};

// 5. 监听路由参数变化（处理在当前页重复点击不同消息通知的刷新场景）
watch(() => route.query.reviewId, () => {
  getList();
});


onMounted(() => {
  getList();
});
</script>


<style scoped>
/* 容器精美卡片白底设计 */
.review-page-container {
  padding: 30px 10%;
  background-color: #f8f9fa;
  min-height: calc(100vh - 80px);
}

.review-card {
  background: #ffffff;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.04);
}

/* 顶部标题栏 */
.review-header {
  margin-bottom: 24px;
  border-bottom: 1px solid #f0f2f5;
  padding-bottom: 16px;
}

.header-title {
  display: flex;
  align-items: center;
  gap: 10px;
}

.title-icon {
  font-size: 24px;
  color: #409eff;
}

.header-title h2 {
  margin: 0;
  font-size: 20px;
  color: #303133;
}

.header-desc {
  margin: 6px 0 0 0;
  font-size: 13px;
  color: #909399;
}

/* 商品超链接样式 */
.product-title-link {
  color: #409eff;
  font-weight: 600;
  cursor: pointer;
  transition: color 0.2s;
}

.product-title-link:hover {
  color: #66b1ff;
  text-decoration: underline;
}

/* 评价时间 */
.review-time, .reply-time {
  font-size: 11px;
  color: #a8abb2;
  display: block;
  margin-top: 6px;
}

.review-text {
  font-size: 14px;
  color: #303133;
  line-height: 1.5;
  margin: 0;
}

/* 商家回复精美气泡框 */
.reply-container {
  background-color: #fdf6ec;
  border: 1px solid #fdf3e6;
  border-radius: 8px;
  padding: 12px;
}

.reply-badge {
  background-color: #e6a23c;
  color: white;
  font-size: 11px;
  padding: 2px 6px;
  border-radius: 4px;
  display: inline-flex;
  align-items: center;
  gap: 4px;
  margin-bottom: 6px;
  font-weight: bold;
}

.reply-text {
  font-size: 13px;
  color: #606266;
  margin: 0;
  line-height: 1.4;
}

/* 未回复呈现 */
.no-reply {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #c0c4cc;
  font-size: 13px;
}

.sand-icon {
  animation: rotating 2s linear infinite;
}

@keyframes rotating {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

/* 🚨 核心亮点样式：由通知中心跳转过来时触发的炫酷高亮呼吸灯特效 */
:deep(.highlight-message-row) {
  background-color: #f0f7ff !important;
  animation: rowGlow 1.5s ease-in-out infinite alternate;
}

@keyframes rowGlow {
  0% {
    box-shadow: inset 0 0 4px rgba(64, 158, 255, 0.2);
  }
  100% {
    box-shadow: inset 0 0 12px rgba(64, 158, 255, 0.5);
  }
}
</style>
