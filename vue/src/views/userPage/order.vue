<template>
  <div class="order-page-container">
    <div class="order-inner-wrapper">

      <div class="order-tabs-header">
        <el-tabs v-model="activeTab" @tab-click="(tab) => changeTab(tab.props.name)" class="custom-nav-tabs">
          <el-tab-pane
              v-for="tab in orderTabs"
              :key="tab.key"
              :label="tab.label"
              :name="tab.key"
          />
        </el-tabs>
      </div>

      <div class="order-list-stage">
        <div v-if="orderList.length > 0" class="cards-stack">
          <div v-for="order in orderList" :key="order.orderId" class="classic-order-card">

            <div class="card-top-bar">
              <div class="meta-left">
                <span class="id-badge">订单号</span>
                <span class="id-text font-mono">{{ order.orderId }}</span>
                <el-divider direction="vertical" class="inner-split"/>
                <span class="time-text">{{ order.createTime }}</span>
              </div>
              <div class="meta-status">
                <el-tag v-if="order.status === '待支付'" type="warning" effect="light" round class="status-tag">待支付
                </el-tag>
                <el-tag v-if="order.status === '未发货'" type="danger" effect="light" round class="status-tag">待发货
                </el-tag>
                <el-tag v-if="order.status === '已发货'" type="primary" effect="light" round class="status-tag">已发货
                </el-tag>

                <el-tag v-if="order.status === '已完成' && order.commentStatus !== '1'" type="success" effect="light"
                        round class="status-tag">待评价
                </el-tag>
                <el-tag v-if="order.status === '已完成' && order.commentStatus === '1'" type="info" effect="plain" round
                        class="status-tag">已评论
                </el-tag>
                <el-tag v-if="order.status === '已取消'" type="info" effect="light" round class="status-tag">已取消
                </el-tag>
              </div>
            </div>

            <div class="card-middle-body" @click="router.push('/user/orderDetail/' + order.orderId)">
              <div class="product-snapshot">
                <img
                    :src="order.image ? baseUrl + order.image : 'https://images.unsplash.com/photo-1540555700478-4be289fbecef?w=120'"
                    alt="商品快照" class="snapshot-img">
                <div class="product-info-text">
                  <h4 class="goods-title">{{ order.title  || '特卖/预订商品快照' }}</h4>
                  <p class="goods-count-spec">数量：<span class="font-mono">×{{
                      order.count || order.quantity || 1
                    }}</span></p>
                </div>
              </div>

              <div class="price-billing-box">
                <span class="bill-label">实付款</span>
                <span class="bill-currency">¥</span>
                <span class="bill-amount">{{ order.totalPrice || order.price || '0.00' }}</span>
              </div>
            </div>

            <div class="card-bottom-actions">
              <div class="left-hints">
                <span v-if="order.status === '未发货'" class="pending-hint-text">
                  <el-icon class="loading-spin"><Loading/></el-icon> 商家正在为您加急配货中...
                </span>
                <span v-if="order.status === '已完成' && order.commentStatus === '1'" class="commented-hint-text">
                  <el-icon><CircleCheck/></el-icon> 您已评价过该商品，感谢您的反馈！
                </span>
              </div>

              <div class="right-buttons-group">
                <el-button v-if="order.status === '待支付'" type="danger" plain size="default"
                           @click="cancelOrder(order)" class="action-btn">
                  取消订单
                </el-button>
                <el-button v-if="order.status === '待支付'" type="primary" size="default" @click="payOrder(order)"
                           class="action-btn active-btn">
                  立即支付
                </el-button>

                <el-button v-if="order.status === '已发货'" type="warning" size="default" @click="confirmReceive(order)"
                           class="action-btn active-btn">
                  确认收货
                </el-button>

                <template v-if="order.status === '已完成'">
                  <el-button
                      v-if="order.commentStatus !== '1'"
                      type="primary"
                      plain
                      size="default"
                      @click="handleReview(order)"
                      class="action-btn"
                  >
                    商品评价
                  </el-button>
                  <el-button
                      v-else
                      type="info"
                      disabled
                      size="default"
                      class="action-btn"
                  >
                    已评价
                  </el-button>
                </template>
              </div>
            </div>

          </div>
        </div>

        <div v-else class="empty-wrapper">
          <el-empty description="暂无订单数据" :image-size="140"/>
        </div>
      </div>

      <div class="pagination-section" v-show="total > 0">
        <pagination
            :total="total"
            v-model:page="query.pageNum"
            v-model:limit="query.pageSize"
            @pagination="getList"
        />
      </div>

    </div>

    <vxe-modal title="商品评价" v-model="open" width="500px" show-maximize showFooter resize>
      <el-form ref="reviewRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="评分" prop="score">
          <el-rate v-model="form.score"/>
        </el-form-item>
        <el-form-item label="评价内容" prop="content">
          <el-input v-model="form.content" type="textarea" placeholder="请输入您的宝贵意见..."/>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="open = false">取 消</el-button>
        </div>
      </template>
    </vxe-modal>

  </div>
</template>


<script setup>
import {onMounted, ref} from "vue";
import {listOrder, payment, receiveOrder, updateOrder} from "@/api/homestay/order.js";
import {Loading, CircleCheck} from "@element-plus/icons-vue";
import {ElMessage, ElMessageBox} from "element-plus";
import {VxeModal} from "vxe-pc-ui";
import {addReview} from "@/api/homestay/review.js";

const open = ref(false)
const form = ref({})
const reviewRef = ref()
const currentOrder = ref(null)

const rules = ref({
  score: [
    {required: true, message: "评分不能为空", trigger: "blur"}
  ],
  content: [
    {required: true, message: "评价内容不能为空", trigger: "blur"}
  ]
})

const handleReview = (order) => {
  currentOrder.value = order
  form.value = {
    homestayId: order.homestayId,
    score: null,
    content: null
  }
  open.value = true
}

// 确定评价
const submitForm = () => {
  reviewRef.value.validate(valid => {
    if (valid) {
      // 1. 发送评价数据
      addReview(form.value).then(res => {

        const orderUpdater = {
          orderId: currentOrder.value.orderId,
          commentStatus: '1'
        }
        // 2. 更新订单状态
        updateOrder(orderUpdater).then(() => {
          ElMessage.success('评价成功，感谢您的真实反馈~')
          open.value = false

          const targetOrder = orderList.value.find(o => o.orderId === currentOrder.value.orderId);
          if (targetOrder) {
            targetOrder.commentStatus = '1';
          }

          // 3. 重新向后端同步最新的列表数据
          getList()
        }).catch(err => {
          console.error("同步更新订单评价状态失败:", err)
          open.value = false;
          getList();
        })
      })
    }
  })
}


const activeTab = ref('all')
const baseUrl = import.meta.env.VITE_APP_BASE_API

// 💡 修改点1：在状态列表中正式加入“已评论”分类项
const orderTabs = [
  {key: 'all', label: '全部订单'},
  {key: '待支付', label: '待支付'},
  {key: '未发货', label: '未发货'},
  {key: '已发货', label: '已发货'},
  {key: '已完成', label: '待评价'}, // 将原完成改名为待评价更符合常规电商认知
  {key: '已评论', label: '已评论'}, // 🌟 新增的分类标签
  {key: '已取消', label: '已取消'},
]

const query = ref({
  pageNum: 1,
  pageSize: 3,
  status: null,
  commentStatus: null // 🌟 显式声明评价状态过滤参数
})

const orderList = ref([])
const total = ref(0)

//
const getList = () => {
  // 重置条件，防止上一次的分类污染下一次查询
  query.value.status = null;
  query.value.commentStatus = null;

  if (activeTab.value === 'all') {
    // 全部订单：status 和 commentStatus 都传 null 查全集
  } else if (activeTab.value === '已评论') {
    // 核心映射：点击已评论时，实际上查的是【status='已完成' 并且 commentStatus='1'】
    query.value.status = '已完成';
    query.value.commentStatus = '1';
  } else if (activeTab.value === '已完成') {
    // 核心映射：点击待评价时，查的是【status='已完成' 并且 commentStatus 不等于 '1'】
    query.value.status = '已完成';
    query.value.commentStatus = '0'; // 假设0或空代表未评论，如果后端只要status，这里可根据实际修改
  } else {
    // 其余标准状态：待支付、未发货、已发货、已取消
    query.value.status = activeTab.value;
  }

  listOrder(query.value).then(res => {
    orderList.value = res.rows
    total.value = res.total
  })
}

//
const changeTab = (tabKey) => {
  activeTab.value = tabKey
  query.value.pageNum = 1; // 切换分类时，强制重置回第一页
  getList()
}

const payOrder = (order) => {
  ElMessageBox.confirm('是否确认支付该订单？', '系统提示', {
    confirmButtonText: '确认支付',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    payment(order.orderId).then(res => {
      getList()
      ElMessage.success('支付成功!')
    })
  })
}

const confirmReceive = (order) => {
  ElMessageBox.confirm('您是否已确认收到该商品？', '提示', {
    confirmButtonText: '确认收货',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    receiveOrder(order.orderId).then(res => {
      ElMessage.success('交易已完成，感谢您的支持！')
      getList()
    })
  })
}

const cancelOrder = (order) => {
  ElMessageBox.confirm('是否确认取消该订单？', '系统提示', {
    confirmButtonText: '确认取消',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    const item = {
      orderId: order.orderId,
      status: '已取消'
    }
    updateOrder(item).then(res => {
      getList()
      ElMessage.success('取消成功!')
    })
  })
}

onMounted(() => {
  getList()
})

</script>

<style scoped>
/* 原有精美样式完美保持不变 */
.order-page-container {
  background-color: #f8fafc;
  min-height: 100vh;
  padding: 24px 0 60px 0;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
}

.order-inner-wrapper {
  max-width: 1000px;
  margin: 0 auto;
  padding: 0 20px;
  box-sizing: border-box;
}

.order-tabs-header {
  background: #ffffff;
  border-radius: 12px;
  padding: 4px 20px;
  border: 1px solid #e2e8f0;
  box-shadow: 0 4px 12px rgba(148, 163, 184, 0.03);
  margin-bottom: 20px;
}

.custom-nav-tabs :deep(.el-tabs__nav-wrap::after) {
  display: none;
}

.custom-nav-tabs :deep(.el-tabs__item) {
  font-size: 15px;
  font-weight: 500;
  color: #64748b;
  height: 48px;
  line-height: 48px;
}

.custom-nav-tabs :deep(.el-tabs__item.is-active) {
  color: #3b82f6;
  font-weight: 600;
}

.cards-stack {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.classic-order-card {
  background: #ffffff;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  overflow: hidden;
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
}

.classic-order-card:hover {
  border-color: #cbd5e1;
  box-shadow: 0 6px 16px rgba(148, 163, 184, 0.08);
}

.card-top-bar {
  background-color: #f8fafc;
  border-bottom: 1px solid #e2e8f0;
  padding: 12px 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.meta-left {
  display: flex;
  align-items: center;
  font-size: 13px;
  color: #64748b;
}

.id-badge {
  background-color: #e2e8f0;
  color: #475569;
  font-size: 11px;
  font-weight: 600;
  padding: 2px 6px;
  border-radius: 4px;
  margin-right: 8px;
}

.id-text {
  color: #1e293b;
  font-weight: 600;
}

.inner-split {
  margin: 0 12px;
  border-left-color: #cbd5e1;
}

.time-text {
  color: #94a3b8;
}

.status-tag {
  font-weight: 500;
  padding: 0 12px;
}

.card-middle-body {
  padding: 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  cursor: pointer;
  border-bottom: 1px solid #f1f5f9;
}

.card-middle-body:hover .goods-title {
  color: #3b82f6;
}

.product-snapshot {
  display: flex;
  align-items: center;
  gap: 16px;
}

.snapshot-img {
  width: 76px;
  height: 76px;
  object-fit: cover;
  border-radius: 8px;
  border: 1px solid #e2e8f0;
  background-color: #f1f5f9;
}

.product-info-text {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.goods-title {
  font-size: 15px;
  font-weight: 600;
  color: #334155;
  margin: 0;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.goods-count-spec {
  font-size: 13px;
  color: #94a3b8;
  margin: 0;
}

.font-mono {
  font-family: Monaco, Consolas, monospace;
}

.price-billing-box {
  display: flex;
  align-items: baseline;
  text-align: right;
  padding-left: 20px;
}

.bill-label {
  font-size: 12px;
  color: #64748b;
  margin-right: 6px;
}

.bill-currency {
  font-size: 13px;
  font-weight: 700;
  color: #1e293b;
  margin-right: 1px;
}

.bill-amount {
  font-size: 20px;
  font-weight: 700;
  color: #1e293b;
  font-family: 'Helvetica Neue', Arial, sans-serif;
}

.card-bottom-actions {
  padding: 14px 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  background-color: #ffffff;
}

.pending-hint-text {
  color: #ef4444;
  font-size: 13px;
  font-weight: 500;
  display: flex;
  align-items: center;
  gap: 6px;
}

.commented-hint-text {
  color: #10b981;
  font-size: 13px;
  font-weight: 500;
  display: flex;
  align-items: center;
  gap: 6px;
}

.loading-spin {
  animation: rotate 2s linear infinite;
}

.right-buttons-group {
  display: flex;
  gap: 8px;
}

.action-btn {
  border-radius: 6px;
  padding: 8px 16px;
  font-weight: 500;
  font-size: 13px;
}

.active-btn {
  box-shadow: 0 4px 10px rgba(59, 130, 246, 0.15);
}

.pagination-section {
  margin-top: 32px;
  display: flex;
  justify-content: flex-end;
}

.empty-wrapper {
  background: #ffffff;
  border-radius: 12px;
  padding: 40px 0;
  border: 1px solid #e2e8f0;
}

@keyframes rotate {
  100% {
    transform: rotate(360deg);
  }
}
</style>
