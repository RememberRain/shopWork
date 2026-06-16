<template>
  <div class="app-container">
    <!-- 顶部查询区域（引入卡片容器与栅格化微调） -->
    <el-card class="query-card" shadow="never" v-show="showSearch">
      <el-form :model="queryParams" ref="queryRef" :inline="true" label-width="68px" class="responsive-form">
        <el-form-item label="订单号" prop="orderId">
          <el-input
              v-model="queryParams.orderId"
              placeholder="请输入订单号"
              clearable
              @keyup.enter="handleQuery"
              style="width: 200px"
          />
        </el-form-item>
        <el-form-item label="商品名称" prop="title">
          <el-input
              v-model="queryParams.title"
              placeholder="请输入商品名称"
              clearable
              @keyup.enter="handleQuery"
              style="width: 200px"
          />
        </el-form-item>
        <el-form-item label="订单状态" prop="status">
          <el-select v-model="queryParams.status" placeholder="请选择订单状态" clearable style="width: 200px">
            <el-option label="待支付" value="待支付"/>
            <el-option label="未发货" value="未发货"/>
            <el-option label="已发货" value="已发货"/>
            <el-option label="已完成" value="已完成"/>
            <el-option label="已取消" value="已取消"/>
          </el-select>
        </el-form-item>
        <el-form-item label="下单用户" prop="userName">
          <el-input
              v-model="queryParams.userName"
              placeholder="请输入下单用户"
              clearable
              @keyup.enter="handleQuery"
              style="width: 200px"
          />
        </el-form-item>
        <el-form-item class="search-actions">
          <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
          <el-button icon="Refresh" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 主体数据区域 -->
    <el-card class="table-card" shadow="never">
      <el-table
          @row-click="clickRow"
          ref="tableRef"
          highlight-current-row
          stripe
          v-loading="loading"
          :data="orderList"
          @selection-change="handleSelectionChange"
          class="custom-order-table"
      >
        <el-table-column type="selection" width="50" align="center"/>
        <el-table-column label="序号" align="center" type="index" :index="indexMethod" width="60" class-name="text-secondary"/>

        <!-- 订单号：代码等宽字体，便于核对 -->
        <el-table-column label="订单号" align="center" prop="orderId" width="160">
          <template #default="scope">
            <span class="order-id-text">{{ scope.row.orderId }}</span>
          </template>
        </el-table-column>

        <!-- 商品主体：图文左对齐混合排版，更具电商感 -->
        <el-table-column label="商品信息" align="left" min-width="220">
          <template #default="scope">
            <div class="goods-info-wrapper">
              <div class="goods-image">
                <image-preview :src="scope.row.image" :width="44" :height="44" class="product-img"/>
              </div>
              <span class="goods-title" :title="scope.row.title">{{ scope.row.title }}</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="购买数量" align="center" prop="quantity" width="90">
          <template #default="scope">
            <span class="quantity-text">x{{ scope.row.quantity }}</span>
          </template>
        </el-table-column>

        <!-- 价格：突出数字，增加符合，右对齐（符合财税表格标准） -->
        <el-table-column label="订单总价" align="right" prop="totalPrice" width="110">
          <template #default="scope">
            <span class="price-text">¥{{ Number(scope.row.totalPrice).toFixed(2) }}</span>
          </template>
        </el-table-column>

        <!-- 配送信息：靠左更易读 -->
        <el-table-column label="收货地址" align="left" prop="shippingAddress" show-overflow-tooltip min-width="180">
          <template #default="scope">
            <span class="address-text">{{ scope.row.shippingAddress }}</span>
          </template>
        </el-table-column>

        <el-table-column label="联系电话" align="center" prop="contactPhone" width="120"/>

        <!-- 订单状态：改用带圆点的轻量级 Dot/Tag 风格 -->
        <el-table-column label="订单状态" align="center" prop="status" width="100">
          <template #default="scope">
            <el-tag v-if="scope.row.status === '待支付'" type="warning" size="small" effect="light" round>{{ scope.row.status }}</el-tag>
            <el-tag v-else-if="scope.row.status === '未发货'" type="danger" size="small" effect="light" round>{{ scope.row.status }}</el-tag>
            <el-tag v-else-if="scope.row.status === '已发货'" type="primary" size="small" effect="light" round>{{ scope.row.status }}</el-tag>
            <el-tag v-else-if="scope.row.status === '已完成'" type="success" size="small" effect="light" round>{{ scope.row.status }}</el-tag>
            <el-tag v-else type="info" size="small" effect="light" round>{{ scope.row.status }}</el-tag>
          </template>
        </el-table-column>

        <el-table-column label="下单用户" align="center" prop="userName" width="110"/>

        <!-- 操作列：统一高度与表现形式，杜绝参差不齐 -->
        <el-table-column label="操作" align="center" width="130" fixed="right" class-name="small-padding fixed-width">
          <template #default="scope">
            <el-button
                type="primary"
                size="small"
                @click="handleDeliver(scope.row)"
                v-if="scope.row.status === '未发货'"
                icon="Position"
            >
              去发货
            </el-button>
            <div v-else class="status-action-text" :class="'status-' + scope.row.status">
              <span v-if="scope.row.status === '待支付'">待付款</span>
              <span v-else-if="scope.row.status === '已发货'">待签收</span>
              <span v-else-if="scope.row.status === '已完成'">交易完成</span>
              <span v-else class="text-muted">已关闭</span>
            </div>
          </template>
        </el-table-column>
      </el-table>

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
    </el-card>

    <vxe-modal :title="title" v-model="open" width="500px" show-maximize showFooter resize>
      <el-form ref="orderRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="商品ID" prop="homestayId">
          <el-input v-model="form.homestayId" disabled/>
        </el-form-item>
        <el-form-item label="购买数量" prop="quantity">
          <el-input-number v-model="form.quantity" :min="1" style="width: 100%"/>
        </el-form-item>
        <el-form-item label="收货地址" prop="shippingAddress">
          <el-input v-model="form.shippingAddress" type="textarea" placeholder="请输入收货地址"/>
        </el-form-item>
        <el-form-item label="订单总价" prop="totalPrice">
          <el-input v-model="form.totalPrice" placeholder="请输入订单总价"/>
        </el-form-item>
        <el-form-item label="联系电话" prop="contactPhone">
          <el-input v-model="form.contactPhone" placeholder="请输入联系电话"/>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitForm">确 定</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </vxe-modal>
  </div>
</template>

<script setup>
import {listOrder, getOrder, delOrder, addOrder, updateOrder, deliverOrder} from "@/api/homestay/order"
import {ElMessage, ElMessageBox} from "element-plus";
import {VxeModal} from "vxe-pc-ui";
import {onMounted, reactive, ref, toRefs} from "vue";

const queryRef = ref()
const orderRef = ref()
const orderList = ref([])
const open = ref(false)
const loading = ref(true)
const showSearch = ref(true)
const ids = ref([])
const single = ref(true)
const multiple = ref(true)
const total = ref(0)
const title = ref("")
const selectedRow = ref(null)
const tableRef = ref()

const data = reactive({
  form: {},
  queryParams: {
    pageNum: 1,
    pageSize: 10,
    orderId: null,
    homestayId: null,
    status: null,
    userId: null,
    title: null,
    userName: null
  },
  rules: {
    homestayId: [
      {required: true, message: "商品ID不能为空", trigger: "blur"}
    ],
    quantity: [
      {required: true, message: "购买数量不能为空", trigger: "blur"}
    ],
    shippingAddress: [
      {required: true, message: "收货地址不能为空", trigger: "blur"}
    ],
    totalPrice: [
      {required: true, message: "订单总价不能为空", trigger: "blur"}
    ],
    contactPhone: [
      {required: true, message: "联系电话不能为空", trigger: "blur"}
    ]
  }
})

const {queryParams, form, rules} = toRefs(data)

// 完成订单
const handleComplete = (row) => {
  ElMessageBox.confirm('是否确认该订单已完成？', '系统提示', {
    confirmButtonText: '确认完成',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    const item = {
      orderId: row.orderId,
      status: '已完成'
    }
    updateOrder(item).then(res => {
      getList()
      ElMessage.success('操作成功!')
    })
  })
}

const clickRow = (row) => {
  selectedRow.value = row;
  const table = tableRef.value;
  table.clearSelection();
  table.toggleRowSelection(row, true);
}

const indexMethod = (index) => {
  let pageNum = queryParams.value.pageNum - 1;
  return pageNum > 0 ? (index + 1) + (pageNum * queryParams.value.pageSize) : (index + 1);
}

const getList = () => {
  loading.value = true
  listOrder(queryParams.value).then(response => {
    orderList.value = response.rows
    total.value = response.total
    loading.value = false
  })
}

const cancel = () => {
  open.value = false
  reset()
}

const reset = () => {
  form.value = {
    orderId: null,
    homestayId: null,
    quantity: 1, // 💡 默认数量 1
    shippingAddress: null, // 💡 清空收货地址
    totalPrice: null,
    contactPhone: null,
    status: null,
    userId: null,
    createTime: null
  }
  if (orderRef.value) {
    orderRef.value.resetFields()
  }
}

const handleQuery = () => {
  queryParams.value.pageNum = 1
  getList()
}

const resetQuery = () => {
  queryRef.value.resetFields()
  handleQuery()
}

const handleSelectionChange = (selection) => {
  ids.value = selection.map(item => item.orderId)
  single.value = selection.length !== 1
  multiple.value = !selection.length
}

const handleDeliver = (row) => {
  ElMessageBox.confirm('是否确认对该订单进行发货处理？', '发货提示', {
    confirmButtonText: '确认发货',
    cancelButtonText: '取消',
    type: 'success'
  }).then(() => {
    deliverOrder(row.orderId).then(res => {
      ElMessage.success('发货成功！')
      getList() // 刷新列表
    })
  })
}

const submitForm = () => {
  orderRef.value.validate(valid => {
    if (valid) {
      if (form.value.orderId != null) {
        updateOrder(form.value).then(response => {
          ElMessage.success("修改成功")
          open.value = false
          getList()
        })
      } else {
        addOrder(form.value).then(response => {
          ElMessage.success("新增成功")
          open.value = false
          getList()
        })
      }
    }
  })
}

onMounted(() => {
  getList()
})
</script>
<style scoped>
/* 基础页面容器，带浅灰现代底色 */
.app-container {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: calc(100vh - 50px);
  display: flex;
  flex-direction: column;
  gap: 16px;
}

/* 白卡片容器提升质感 */
.query-card, .table-card {
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  background: #ffffff;
}

/* 过滤表单间距压缩 */
.responsive-form {
  margin-bottom: -18px; /* 抵消form-item自带的bottom边距 */
}

/* 订单号样式 */
.order-id-text {
  font-family: Monaco, Consolas, monospace;
  color: #606266;
  font-size: 13px;
}

/* 商品图文联合列 */
.goods-info-wrapper {
  display: flex;
  align-items: center;
  gap: 12px;
}
.goods-image {
  flex-shrink: 0;
  width: 44px;
  height: 44px;
  border-radius: 6px;
  border: 1px solid #e4e7ed;
  overflow: hidden;
  background: #fafafa;
  display: flex;
  align-items: center;
  justify-content: center;
}
.goods-title {
  font-weight: 500;
  color: #303133;
  line-height: 1.4;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

/* 数量样式 */
.quantity-text {
  color: #909399;
  font-family: Arial, sans-serif;
}

/* 价格高亮：采用红色/橙色车间标准电商配色，加粗，右靠齐 */
.price-text {
  font-family: "Helvetica Neue", Helvetica, Arial, sans-serif;
  font-weight: 600;
  color: #ff4d4f;
  font-size: 14px;
}

/* 文本类列微调 */
.address-text {
  color: #606266;
  font-size: 13px;
}
.text-secondary {
  color: #909399;
}

/* 表头重塑 */
.custom-order-table {
  --el-table-border-color: #f0f0f0;
}
.custom-order-table :deep(.el-table__header-th) {
  background-color: #fafafa !important;
  color: #2c3e50;
  font-weight: 600;
  height: 46px;
}

/* 替代原先无秩序的操作状态栏文字 */
.status-action-text {
  font-size: 13px;
  font-weight: 500;
}
.status-待支付 { color: #e6a23c; }
.status-已发货 { color: #409eff; }
.status-已完成 { color: #67c23a; }
.text-muted { color: #c0c4cc; }

/* 分页靠右 */
.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
