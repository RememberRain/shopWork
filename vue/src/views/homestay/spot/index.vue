<template>
  <div class="app-container">
    <!-- 顶部查询区域（引入轻量白卡片） -->
    <el-card class="query-card" shadow="never">
      <el-form :model="query" ref="queryRef" label-width="80px" inline class="responsive-form">
        <el-form-item label="尾货名称" prop="name" class="mb0">
          <el-input
              v-model="query.name"
              placeholder="请输入尾货商品名称"
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
      <!-- 顶部按钮工具栏 -->
      <div class="toolbar-container">
        <div class="toolbar-left">
          <el-button type="primary" icon="Plus" @click="handleInsert">新增尾货</el-button>
          <el-button :disabled="isSingle" type="success" plain icon="Edit" @click="handleUpdate">修改</el-button>
          <el-button :disabled="isMultple" type="danger" plain icon="Delete" @click="handleDelete">删除</el-button>
        </div>
      </div>

      <!-- 数据表格（重塑线条与表头） -->
      <el-table
          :data="spotList"
          style="width: 100%"
          stripe
          border
          @selection-change="handleSelectionChange"
          class="custom-spot-table"
      >
        <el-table-column type="selection" width="50" align="center"/>

        <!-- 综合热度排名：模拟现代电商榜单质感 -->
        <el-table-column label="热度排行" width="100" align="center">
          <template #default="scope">
            <div class="rank-wrapper">
              <span v-if="scope.$index === 0" class="rank-badge rank-1">1</span>
              <span v-else-if="scope.$index === 1" class="rank-badge rank-2">2</span>
              <span v-else-if="scope.$index === 2" class="rank-badge rank-3">3</span>
              <span v-else class="rank-number">{{ scope.$index + 1 }}</span>
            </div>
          </template>
        </el-table-column>

        <!-- 尾货名称与图片合二为一：节省空间且更具直观感 -->
        <el-table-column label="尾货商品" align="left" min-width="200">
          <template #default="scope">
            <div class="spot-info-wrapper">
              <div class="spot-image">
                <image-preview :src="scope.row.image" :width="44" :height="44" class="round-img"/>
              </div>
              <span class="spot-name" :title="scope.row.name">{{ scope.row.name }}</span>
            </div>
          </template>
        </el-table-column>

        <!-- 当前库存：突出紧迫感 -->
        <el-table-column prop="stock" label="关联当前库存" width="140" align="center">
          <template #default="scope">
            <div class="stock-badge-box">
              <span class="stock-highlight">{{ scope.row.stock || 0 }}</span>
              <span class="stock-unit"> 件</span>
            </div>
          </template>
        </el-table-column>

        <!-- 仓位 -->
        <el-table-column prop="location" label="发货仓位" width="130" align="center">
          <template #default="scope">
            <el-tag type="info" effect="plain" size="small" class="location-tag">
              <el-icon class="mr2"><Compass /></el-icon>{{ scope.row.location }}
            </el-tag>
          </template>
        </el-table-column>

        <!-- 抢购截止时间：加入时间图标，增强时效感知 -->
        <el-table-column prop="openingHours" label="抢购截止时间" width="160" align="center">
          <template #default="scope">
            <div class="time-wrapper">
              <el-icon class="time-icon"><Clock /></el-icon>
              <span class="time-text">{{ scope.row.openingHours }}</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="description" label="特卖介绍" align="left" show-overflow-tooltip class-name="text-secondary" min-width="180"/>

        <!-- 操作列 -->
        <el-table-column label="操作" align="center" width="140" fixed="right">
          <template #default="scope">
            <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)">修改</el-button>
            <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页区域右对齐 -->
      <div class="pagination-container">
        <pagination
            :total="total"
            v-model:page="query.pageNum"
            v-model:limit="query.pageSize"
            @pagination="getList"
        />
      </div>
    </el-card>

  <vxe-modal :title="title" v-model="open" width="500px" showFooter show-maximize resize>
    <el-form ref="spotRef" :model="form" :rules="rules" label-width="100px">
      <el-form-item label="关联主商品" prop="homestayId">
        <el-input v-model="form.homestayId" placeholder="请输入关联的主商品 ID"/>
      </el-form-item>
      <el-form-item label="尾货名称" prop="name">
        <el-input v-model="form.name" placeholder="请输入尾货商品名称"/>
      </el-form-item>
      <el-form-item label="发货仓位" prop="location">
        <el-input v-model="form.location" placeholder="请输入仓位物理地址"/>
      </el-form-item>
      <el-form-item label="抢购截止时间" prop="openingHours">
        <el-input v-model="form.openingHours" placeholder="例如：2026-12-31 或 每日18点前"/>
      </el-form-item>
      <el-form-item label="商品图片" prop="image">
        <image-upload v-model="form.image"/>
      </el-form-item>
      <el-form-item label="特卖介绍" prop="description">
        <el-input type="textarea" :rows="3" v-model="form.description" placeholder="请输入特卖详情描述"/>
      </el-form-item>
    </el-form>
    <template #footer>
      <div>
        <el-button type="primary" @click="submitForm">保存</el-button>
        <el-button @click="open = false">取消</el-button>
      </div>
    </template>
  </vxe-modal>
  </div>
</template>

<script setup>
import { nextTick, onMounted, ref } from "vue";
import { deleteSpotByIds, insertSpot, selectSpotBySpotId, selectSpotList, updateSpot } from "@/api/homestay/spot.js";
import Pagination from "@/components/Pagination/index.vue";
import { VxeModal } from "vxe-pc-ui";
import { ElMessage, ElMessageBox } from "element-plus";
import ImageUpload from "@/components/ImageUpload/index.vue";
import ImagePreview from "@/components/ImagePreview/index.vue";
import {Clock, Compass} from "@element-plus/icons-vue";

const title = ref('')
const open = ref(false)
const spotRef = ref()
const form = ref({})

const rules = ref({
  homestayId: [{ required: true, message: '请绑定关联商品ID', trigger: 'blur' }],
  name: [{ required: true, message: '请输入商品名称', trigger: 'blur' }],
  location: [{ required: true, message: '请输入发货仓位', trigger: 'blur' }],
  openingHours: [{ required: true, message: '请输入截止时间', trigger: 'blur' }],
  image: [{ required: true, message: '请上传商品图片', trigger: 'blur' }],
  description: [{ required: true, message: '请输入商品介绍', trigger: 'blur' }]
})

const handleInsert = () => {
  form.value = {
    spotId: null,
    homestayId: null,
    location: null,
    name: null,
    openingHours: null,
    image: null,
    description: null,
  }
  open.value = true
  title.value = '新增尾货商品'
  nextTick(() => {
    if (spotRef.value) {
      spotRef.value.resetFields()
    }
  })
}

const handleUpdate = (row) => {
  const spotId = row.spotId || ids.value
  selectSpotBySpotId(spotId).then(res => {
    form.value = res.data
    open.value = true
    title.value = '修改尾货商品'
  })
}

const handleDelete = (row) => {
  const spotIds = row.spotId || ids.value
  if (!spotIds || (Array.isArray(spotIds) && spotIds.length === 0)) {
    ElMessage.warning("请选择要删除的数据");
    return;
  }
  ElMessageBox.confirm('是否确认删除所选商品', '系统提示', {
    confirmButtonText: '确认',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(() => {
    deleteSpotByIds(spotIds).then(res => {
      ElMessage.success('删除成功！')
      getList()
    })
  })
}

const submitForm = () => {
  spotRef.value.validate(valid => {
    if (valid) {
      if (form.value.spotId != null) {
        updateSpot(form.value).then(res => {
          ElMessage.success('修改成功')
          open.value = false
          getList()
        })
      } else {
        insertSpot(form.value).then(res => {
          ElMessage.success('新增成功')
          open.value = false
          getList()
        })
      }
    }
  })
}

const queryRef = ref()
const handleQuery = () => {
  query.value.pageNum = 1
  getList()
}

const resetQuery = () => {
  queryRef.value.resetFields()
  handleQuery()
}

// 核心策略：如果后端由于某种原因没有处理库存倒序，前端在接收时手动兜底排序
const getList = () => {
  selectSpotList(query.value).then(res => {
    const sortedRows = (res.rows || []).sort((a, b) => {
      const stockA = a.stock || 0;
      const stockB = b.stock || 0;
      return stockB - stockA; // 库存多的在前
    });

    spotList.value = sortedRows
    total.value = res.total
  })
}

const isSingle = ref(true)
const isMultple = ref(true)
const ids = ref([])

const handleSelectionChange = (selection) => {
  ids.value = selection.map(item => item.spotId)
  isSingle.value = selection.length !== 1
  isMultple.value = !selection.length
}

const query = ref({
  pageNum: 1,
  name: null,
  pageSize: 5,
})

const total = ref(0)
const spotList = ref([])

onMounted(() => {
  getList()
})
</script>


<style scoped>
/* 页面背景及流式布局 */
.app-container {
  padding: 20px;
  background-color: #f5f7fa; /* 现代中后台标配灰底 */
  min-height: calc(100vh - 50px);
  display: flex;
  flex-direction: column;
  gap: 16px;
}

/* 白卡片定义 */
.query-card, .table-card {
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  background: #ffffff;
}

.mb0 {
  margin-bottom: 0 !important;
}
.responsive-form {
  margin-bottom: -18px;
}

/* 工具栏样式 */
.toolbar-container {
  margin-bottom: 16px;
  display: flex;
  justify-content: flex-start;
}

/* 表头与线条高质感改造 */
.custom-spot-table {
  --el-table-border-color: #f0f0f0;
}
.custom-spot-table :deep(.el-table__header-th) {
  background-color: #fafafa !important;
  color: #2c3e50;
  font-weight: 600;
  height: 46px;
}

/* 荣耀排行榜单样式（亮点） */
.rank-wrapper {
  display: flex;
  justify-content: center;
  align-items: center;
}
.rank-badge {
  display: inline-flex;
  justify-content: center;
  align-items: center;
  width: 24px;
  height: 24px;
  border-radius: 50%;
  color: #ffffff;
  font-weight: bold;
  font-size: 13px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}
.rank-1 { background: linear-gradient(135deg, #ff4d4f, #ff7875); } /* Top 1 爆款红 */
.rank-2 { background: linear-gradient(135deg, #ff9c6e, #ffbb96); } /* Top 2 橙色 */
.rank-3 { background: linear-gradient(135deg, #ffc069, #ffe7ba); color: #d46b08; } /* Top 3 黄色 */
.rank-number {
  display: inline-flex;
  justify-content: center;
  align-items: center;
  width: 22px;
  height: 22px;
  background-color: #f0f2f5;
  color: #909399;
  border-radius: 50%;
  font-size: 12px;
}

/* 商品图文双列混合 */
.spot-info-wrapper {
  display: flex;
  align-items: center;
  gap: 12px;
}
.spot-image {
  flex-shrink: 0;
  width: 44px;
  height: 44px;
  border-radius: 6px;
  border: 1px solid #e4e7ed;
  overflow: hidden;
  background: #fafafa;
}
.spot-name {
  font-weight: 500;
  color: #303133;
  line-height: 1.4;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

/* 库存紧迫高亮 */
.stock-badge-box {
  display: inline-block;
  background-color: #fff1f0;
  border: 1px solid #ffa39e;
  padding: 2px 10px;
  border-radius: 20px;
}
.stock-highlight {
  font-weight: bold;
  color: #f56c6c;
  font-family: "Helvetica Neue", sans-serif;
}
.stock-unit {
  font-size: 12px;
  color: #f56c6c;
}

/* 发货仓位标签式 */
.location-tag {
  border-radius: 4px;
  border: 1px solid #d9d9d9;
}
.mr2 {
  margin-right: 2px;
}

/* 截止时间高可读性 */
.time-wrapper {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  color: #606266;
}
.time-icon {
  color: #1890ff;
}
.time-text {
  font-family: Monaco, monospace;
  font-size: 13px;
}

/* 辅助次要文本 */
.text-secondary {
  color: #8c8c8c;
}

/* 操作列间距 */
.el-button + .el-button {
  margin-left: 16px;
}

/* 分页容器靠右 */
.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
