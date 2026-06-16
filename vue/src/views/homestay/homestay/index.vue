<template>
  <div class="app-container">
    <!-- 顶部查询区域（引入白卡片，强化层次） -->
    <el-card class="query-card" shadow="never" v-show="showSearch">
      <el-form :model="queryParams" ref="queryRef" :inline="true" label-width="68px" class="responsive-form">
        <el-form-item label="商品名称" prop="title" class="mb0">
          <el-input
              v-model="queryParams.title"
              placeholder="请输入商品名称"
              clearable
              @keyup.enter="handleQuery"
              style="width: 240px"
          />
        </el-form-item>
        <el-form-item class="mb0">
          <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
          <el-button icon="Refresh" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 主体内容区域 -->
    <el-card class="table-card" shadow="never">
      <!-- 顶部操作工具栏 -->
      <div class="toolbar-container">
        <div class="toolbar-left">
          <el-button type="primary" icon="Plus" @click="handleAdd">新增商品</el-button>
          <el-button type="success" plain icon="Edit" :disabled="single" @click="handleUpdate">修改</el-button>
          <el-button type="danger" plain icon="Delete" :disabled="multiple" @click="handleDelete">删除</el-button>
        </div>
      </div>

      <!-- 数据表格 -->
      <el-table
          @row-click="clickRow"
          ref="tableRef"
          highlight-current-row
          stripe
          border
          v-loading="loading"
          :data="homestayList"
          @selection-change="handleSelectionChange"
          class="custom-product-table"
      >
        <!-- 选择与序号 -->
        <el-table-column type="selection" width="50" align="center" />
        <el-table-column label="序号" align="center" type="index" :index="indexMethod" width="60" class-name="text-secondary" />

        <!-- 商品ID：等宽字体 -->
        <el-table-column label="商品ID" align="center" prop="homestayId" width="100" show-overflow-tooltip>
          <template #default="scope">
            <span class="id-text">{{ scope.row.homestayId }}</span>
          </template>
        </el-table-column>

        <!-- 商品信息：图文并茂混合列（大大提升视觉质感） -->
        <el-table-column label="商品信息" align="left" min-width="220">
          <template #default="scope">
            <div class="product-info-wrapper">
              <div class="product-image">
                <image-preview :src="scope.row.image" :width="44" :height="44" class="round-img"/>
              </div>
              <div class="product-title-box">
                <span class="product-title" :title="scope.row.title">{{ scope.row.title }}</span>
                <span class="merchant-text">商家: {{ scope.row.userName || '未知' }}</span>
              </div>
            </div>
          </template>
        </el-table-column>

        <!-- 商品单价：右对齐，财务规范加粗 -->
        <el-table-column label="商品单价" align="right" prop="price" width="110">
          <template #default="scope">
            <span class="price-text">¥{{ Number(scope.row.price).toFixed(2) }}</span>
          </template>
        </el-table-column>

        <!-- 商品库存：智能化颜色提示，库存<=5时呈红色警告 -->
        <el-table-column label="商品库存" align="center" prop="stock" width="100">
          <template #default="scope">
            <span :class="['stock-text', scope.row.stock <= 5 ? 'stock-warning' : '']">
              {{ scope.row.stock }} <span class="unit">件</span>
            </span>
          </template>
        </el-table-column>

<!--        商品销量-->
        <el-table-column label="商品销量" align="center" prop="sales" width="100">
          <template #default="scope">
            <span :class="['sales-text', scope.row.sales <= 5 ? 'sales-warning' : '']">
              {{ scope.row.sales }} <span class="unit">件</span>
            </span>
          </template>
        </el-table-column>

        <!-- 标签列：精细化间距与随机偏色质感 -->
        <el-table-column label="标签" align="left" prop="tags" min-width="150">
          <template #default="scope">
            <div class="tags-wrapper">
              <el-tag
                  v-for="tag in scope.row.tags ? scope.row.tags.split(',') : []"
                  :key="tag"
                  size="small"
                  type="primary"
                  effect="plain"
                  class="custom-tag"
              >
                {{ tag }}
              </el-tag>
            </div>
          </template>
        </el-table-column>

        <!-- 发货地址与描述：开启防撑开气泡 -->
        <el-table-column label="发货地址" align="left" prop="address" min-width="140" show-overflow-tooltip />
        <el-table-column label="描述" align="left" prop="description" min-width="160" show-overflow-tooltip class-name="text-secondary" />

        <!-- 操作列：规整统一 -->
        <el-table-column label="操作" align="center" width="130" fixed="right" class-name="small-padding fixed-width">
          <template #default="scope">
            <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)">修改</el-button>
            <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)">删除</el-button>
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
      <el-form ref="homestayRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="商品名称" prop="title">
          <el-input v-model="form.title" placeholder="请输入商品名称" />
        </el-form-item>
        <el-form-item label="商品单价" prop="price">
          <el-input-number style="width: 100%;" :min="0" :precision="2" v-model="form.price" placeholder="请输入商品单价" />
        </el-form-item>
        <el-form-item label="商品库存" prop="stock"> <el-input-number style="width: 100%;" :min="0" :precision="0" v-model="form.stock" placeholder="请输入库存数量" />
        </el-form-item>
        <el-form-item label="发货地址" prop="address">
          <el-input v-model="form.address" placeholder="请输入地址" />
        </el-form-item>
        <el-form-item label="标签" prop="tags">
          <el-input-tag v-model="form.tags" placeholder="请输入标签 按回车键结束"/>
        </el-form-item>
        <el-form-item label="图片" prop="image">
          <image-upload v-model="form.image"/>
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="form.description" type="textarea" placeholder="请输入内容" />
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
import { listHomestay, getHomestay, delHomestay, addHomestay, updateHomestay } from "@/api/homestay/homestay"
import { ElMessage, ElMessageBox } from "element-plus";
import { VxeModal } from "vxe-pc-ui";
import { onMounted, reactive, ref, toRefs } from "vue";
import ImageUpload from "@/components/ImageUpload/index.vue";

const queryRef = ref()
const homestayRef = ref()
const homestayList = ref([])
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
    pageSize: 5,
    title: null,
    userId: null,
  },
  rules: {
    title: [{ required: true, message: "商品名称不能为空", trigger: "blur" }],
    price: [{ required: true, message: "商品单价不能为空", trigger: "blur" }],
    stock: [{ required: true, message: "库存不能为空", trigger: "blur" }],
    sales: [{ required: true, message: "销量不能为空", trigger: "blur" }],
    address: [{ required: true, message: "地址不能为空", trigger: "blur" }],
    tags: [{ required: true, message: "标签不能为空", trigger: "blur" }],
    image: [{ required: true, message: "图片不能为空", trigger: "blur" }],
    description: [{ required: true, message: "描述不能为空", trigger: "blur" }]
  }
})

const { queryParams, form, rules } = toRefs(data)

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
  listHomestay(queryParams.value).then(response => {
    homestayList.value = response.rows
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
    homestayId: null,
    title: null,
    price: null,
    stock: 0,
    sales: 0,
    address: null,
    tags: null,
    image: null,
    description: null,
    userId: null,
    createTime: null
  }
  if (homestayRef.value) {
    homestayRef.value.resetFields()
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
  ids.value = selection.map(item => item.homestayId)
  single.value = selection.length !== 1
  multiple.value = !selection.length
}

const handleAdd = () => {
  reset()
  open.value = true
  title.value = "添加商品"
}

const handleUpdate = (row) => {
  reset()
  const _homestayId = row.homestayId || ids.value
  getHomestay(_homestayId).then(response => {
    form.value = response.data
    if (form.value.tags && typeof form.value.tags === 'string') {
      form.value.tags = form.value.tags.split(',')
    } else if (!form.value.tags) {
      form.value.tags = []
    }
    open.value = true
    title.value = "修改商品"
  })
}

const submitForm = () => {
  if (Array.isArray(form.value.tags)) {
    form.value.tags = form.value.tags.join(',')
  }
  homestayRef.value.validate(valid => {
    if (valid) {
      if (form.value.homestayId != null) {
        updateHomestay(form.value).then(response => {
          ElMessage.success("修改成功")
          open.value = false
          getList()
        })
      } else {
        addHomestay(form.value).then(response => {
          ElMessage.success("新增成功")
          open.value = false
          getList()
        })
      }
    }
  })
}

const handleDelete = (row) => {
  const _homestayIds = row.homestayId || ids.value
  ElMessageBox.confirm('是否确认删除该项数据？', '系统提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(function() {
    return delHomestay(_homestayIds)
  }).then(() => {
    getList()
    ElMessage.success("删除成功")
  }).catch(() => {})
}

onMounted(() => {
  getList()
})
</script>

<style scoped>
/* 现代后台灰底铺垫 */
.app-container {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: calc(100vh - 50px);
  display: flex;
  flex-direction: column;
  gap: 16px;
}

/* 白卡片圆角容器 */
.query-card, .table-card {
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  background: #ffffff;
}

.mb0 {
  margin-bottom: 0 !important;
}

/* 工具栏样式 */
.toolbar-container {
  margin-bottom: 16px;
  display: flex;
  justify-content: flex-start;
}

/* 表头与表格高级微调 */
.custom-product-table {
  --el-table-border-color: #f0f0f0;
}
.custom-product-table :deep(.el-table__header-th) {
  background-color: #fafafa !important;
  color: #2c3e50;
  font-weight: 600;
  height: 46px;
}

/* ID 弱化展示 */
.id-text {
  font-family: Monaco, monospace;
  color: #909399;
  font-size: 13px;
}

/* 商品图文多行排版 */
.product-info-wrapper {
  display: flex;
  align-items: center;
  gap: 12px;
}
.product-image {
  flex-shrink: 0;
  width: 44px;
  height: 44px;
  border-radius: 6px;
  overflow: hidden;
  border: 1px solid #e4e7ed;
  background: #fafafa;
}
.product-title-box {
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.product-title {
  font-weight: 500;
  color: #303133;
  line-height: 1.3;
  font-size: 14px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
}
.merchant-text {
  font-size: 12px;
  color: #a8abb2;
}

/* 核心价格样式 */
.price-text {
  font-family: "Helvetica Neue", Arial, sans-serif;
  font-weight: 600;
  color: #ff4d4f; /* 电商标配红 */
  font-size: 14px;
}

/* 库存数字化表现 */
.stock-text {
  font-weight: 500;
  color: #303133;
}
.unit {
  font-size: 12px;
  color: #909399;
}
.stock-warning {
  color: #f56c6c !important;
  font-weight: bold;
}

/* 标签流式包裹 */
.tags-wrapper {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}
.custom-tag {
  border-radius: 4px;
}

/* 辅助次要文本 */
.text-secondary {
  color: #909399;
}

/* 操作列链接按钮对齐 */
.el-button + .el-button {
  margin-left: 16px;
}

/* 分页右对齐 */
.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
