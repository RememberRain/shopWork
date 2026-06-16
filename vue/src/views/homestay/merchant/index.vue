<template>
  <div class="app-container">
    <!-- 顶部搜索区域 -->
    <el-card class="query-card" shadow="never" v-show="showSearch">
      <el-form :model="queryParams" ref="queryRef" :inline="true" label-width="110px" class="responsive-form">
        <el-form-item label="认证状态" prop="authStatus" class="mb0">
          <el-select style="width: 200px;" v-model="queryParams.authStatus" placeholder="请选择认证状态" clearable>
            <el-option label="待审核" value="待审核"/>
            <el-option label="认证通过" value="认证通过"/>
            <el-option label="认证未通过" value="认证未通过"/>
          </el-select>
        </el-form-item>
        <el-form-item label="商家的用户名" prop="userName" class="mb0">
          <el-input
              v-model="queryParams.userName"
              placeholder="请输入商家的用户名"
              clearable
              @keyup.enter="handleQuery"
              style="width: 220px;"
          />
        </el-form-item>
        <el-form-item class="mb0">
          <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
          <el-button icon="Refresh" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 主体表格与操作区域 -->
    <el-card class="table-card" shadow="never">
      <!-- 顶部操作工具栏 -->
      <div class="toolbar-container">
        <div class="toolbar-left">
          <el-button type="primary" icon="Plus" @click="handleAdd">新增</el-button>
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
          :data="merchantList"
          @selection-change="handleSelectionChange"
          class="custom-merchant-table"
      >
        <el-table-column type="selection" width="50" align="center"/>
        <el-table-column label="序号" align="center" type="index" :index="indexMethod" width="60" class-name="text-secondary"/>

        <!-- 基础信息 -->
        <el-table-column label="真实姓名" align="center" prop="realName" width="100">
          <template #default="scope">
            <span class="font-bold">{{ scope.row.realName }}</span>
          </template>
        </el-table-column>

        <!-- 身份证：使用现代等宽编码字体 -->
        <el-table-column label="身份证号" align="center" prop="idCard" width="180">
          <template #default="scope">
            <span class="code-text">{{ scope.row.idCard }}</span>
          </template>
        </el-table-column>

        <el-table-column label="联系电话" align="center" prop="phone" width="120">
          <template #default="scope">
            <span class="code-text">{{ scope.row.phone }}</span>
          </template>
        </el-table-column>

        <el-table-column label="邮箱" align="left" prop="email" min-width="160" show-overflow-tooltip/>

        <!-- 证件图片列：加入高质感圆角与外框盒子包裹 -->
        <el-table-column label="身份证正面" align="center" prop="idCardFront" width="110">
          <template #default="scope">
            <div class="image-box" v-if="scope.row.idCardFront">
              <image-preview :src="scope.row.idCardFront" :width="44" :height="44"/>
            </div>
            <span v-else class="text-placeholder">未上传</span>
          </template>
        </el-table-column>

        <el-table-column label="身份证反面" align="center" prop="idCardBack" width="110">
          <template #default="scope">
            <div class="image-box" v-if="scope.row.idCardBack">
              <image-preview :src="scope.row.idCardBack" :width="44" :height="44"/>
            </div>
            <span v-else class="text-placeholder">未上传</span>
          </template>
        </el-table-column>

        <el-table-column label="营业执照" align="center" prop="license" width="110">
          <template #default="scope">
            <div class="image-box" v-if="scope.row.license">
              <image-preview :src="scope.row.license" :width="44" :height="44"/>
            </div>
            <span v-else class="text-placeholder">未上传</span>
          </template>
        </el-table-column>

        <!-- 状态标签：圆角精细化 -->
        <el-table-column label="认证状态" align="center" prop="authStatus" width="110">
          <template #default="scope">
            <el-tag type="warning" v-if="scope.row.authStatus === '待审核'" size="small" effect="light" round>{{ scope.row.authStatus }}</el-tag>
            <el-tag type="success" v-if="scope.row.authStatus === '认证通过'" size="small" effect="light" round>{{ scope.row.authStatus }}</el-tag>
            <el-tag type="danger" v-if="scope.row.authStatus === '认证未通过'" size="small" effect="light" round>{{ scope.row.authStatus }}</el-tag>
          </template>
        </el-table-column>

        <el-table-column label="关联用户名" align="center" prop="userName" min-width="130" show-overflow-tooltip/>

        <!-- 操作列：去掉突兀的大色块按钮，改用规整的轻量 link/text 按钮，体验更轻盈 -->
        <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="200" fixed="right">
          <template #default="scope">
            <div v-if="scope.row.authStatus === '待审核'" class="action-buttons-wrapper">
              <el-button
                  link
                  type="success"
                  icon="Check"
                  @click="handlePassed(scope.row)">
                通过
              </el-button>
              <el-button
                  link
                  type="danger"
                  icon="Close"
                  @click="handleNotPassed(scope.row)">
                驳回
              </el-button>
            </div>
            <span v-else class="text-placeholder">---</span>
          </template>
        </el-table-column>
      </el-table>

      <!-- 右对齐分页组件 -->
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

    <!-- 添加或修改商家对话框 -->
    <vxe-modal :title="title" v-model="open" width="500px" show-maximize showFooter resize>
      <el-form ref="merchantRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="真实姓名" prop="realName">
          <el-input v-model="form.realName" placeholder="请输入真实姓名"/>
        </el-form-item>
        <el-form-item label="身份证号" prop="idCard">
          <el-input v-model="form.idCard" placeholder="请输入身份证号"/>
        </el-form-item>
        <el-form-item label="联系电话" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入联系电话"/>
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" placeholder="请输入邮箱"/>
        </el-form-item>
        <el-form-item label="身份证正面" prop="idCardFront">
          <image-upload v-model="form.idCardFront"/>
        </el-form-item>
        <el-form-item label="身份证反面" prop="idCardBack">
          <image-upload v-model="form.idCardBack"/>
        </el-form-item>
        <el-form-item label="营业执照" prop="license">
          <image-upload v-model="form.license"/>
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
import {listMerchant, getMerchant, delMerchant, addMerchant, updateMerchant, passed} from "@/api/homestay/merchant"
import {ElMessage, ElMessageBox} from "element-plus";

const baseURL = import.meta.env.VITE_APP_BASE_API
import {VxeModal} from "vxe-pc-ui";
import {onMounted, reactive, ref, toRefs} from "vue";
import ImageUpload from "@/components/ImageUpload/index.vue";

const queryRef = ref()
const merchantRef = ref()
const merchantList = ref([])
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
    authStatus: null,
    userId: null,
    userName: null
  },
  rules: {
    merchantId: [
      {required: true, message: "商家ID不能为空", trigger: "blur"}
    ],
    realName: [
      {required: true, message: "真实姓名不能为空", trigger: "blur"}
    ],
    idCard: [
      {required: true, message: "身份证号不能为空", trigger: "blur"}
    ],
    phone: [
      {required: true, message: "联系电话不能为空", trigger: "blur"}
    ],
    email: [
      {required: true, message: "邮箱不能为空", trigger: "blur"}
    ],
    idCardFront: [
      {required: true, message: "身份证正面不能为空", trigger: "blur"}
    ],
    idCardBack: [
      {required: true, message: "身份证反面不能为空", trigger: "blur"}
    ],
    license: [
      {required: true, message: "营业执照不能为空", trigger: "blur"}
    ],
    authStatus: [
      {required: true, message: "认证状态不能为空", trigger: "blur"}
    ],
    userId: [
      {required: true, message: "民宿商家的用户ID不能为空", trigger: "blur"}
    ],
    createTime: [
      {required: true, message: "创建时间不能为空", trigger: "blur"}
    ]
  }
})

const {queryParams, form, rules} = toRefs(data)

//认证未通过
const handleNotPassed = (row) => {
  ElMessageBox.confirm('是否确认不通过该申请？', '系统提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    const item = {
      merchantId: row.merchantId,
      authStatus: '认证未通过'
    }
    updateMerchant(item).then(res => {
      ElMessage.success( '执行成功~')
      getList()
    })
  })
}

//认证通过
const handlePassed = (row) => {
  ElMessageBox.confirm('是否确认通过该申请？', '系统提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    passed(row.merchantId).then(res => {
      ElMessage.success('执行成功~')
      getList()
    })
  })
}

//点击行 获取行
const clickRow = (row) => {
  selectedRow.value = row; // 更新选中的行
  const table = tableRef.value;
  // 清除所有已选中的行
  table.clearSelection();
  // 选中当前点击的行
  table.toggleRowSelection(row, true);
}

/** 自定义序号 */
const indexMethod = (index) => {
  let pageNum = queryParams.value.pageNum - 1;
  if ((pageNum !== -1 && pageNum !== 0)) {
    return (index + 1) + (pageNum * queryParams.value.pageSize);
  } else {
    return (index + 1)
  }
}

/** 查询商家列表 */
const getList = () => {
  loading.value = true
  listMerchant(queryParams.value).then(response => {
    merchantList.value = response.rows
    total.value = response.total
    loading.value = false
  })
}

// 取消按钮
const cancel = () => {
  open.value = false
  reset()
}

// 表单重置
const reset = () => {
  form.value = {
    merchantId: null,
    realName: null,
    idCard: null,
    phone: null,
    email: null,
    idCardFront: null,
    idCardBack: null,
    license: null,
    authStatus: null,
    userId: null,
    createTime: null
  }
  if (merchantRef.value) {
    merchantRef.value.resetFields()
  }
}

/** 搜索按钮操作 */
const handleQuery = () => {
  queryParams.value.pageNum = 1
  getList()
}

/** 重置按钮操作 */
const resetQuery = () => {
  queryRef.value.resetFields()
  handleQuery()
}

// 多选框选中数据
const handleSelectionChange = (selection) => {
  ids.value = selection.map(item => item.merchantId)
  single.value = selection.length !== 1
  multiple.value = !selection.length
}

/** 新增按钮操作 */
const handleAdd = () => {
  reset()
  open.value = true
  title.value = "添加商家"
}

/** 修改按钮操作 */
const handleUpdate = (row) => {
  reset()
  const _merchantId = row.merchantId || ids.value
  getMerchant(_merchantId).then(response => {
    form.value = response.data
    open.value = true
    title.value = "修改商家"
  })
}

/** 提交按钮 */
const submitForm = () => {
  merchantRef.value.validate(valid => {
    if (valid) {
      if (form.value.merchantId != null) {
        updateMerchant(form.value).then(response => {
          ElMessage.success("修改成功")
          open.value = false
          getList()
        })
      } else {
        addMerchant(form.value).then(response => {
          ElMessage.success("新增成功")
          open.value = false
          getList()
        })
      }
    }
  })
}

/** 删除按钮操作 */
const handleDelete = (row) => {
  const _merchantIds = row.merchantId || ids.value
  ElMessageBox.confirm('是否确认删除该项数据？', '系统提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(function () {
    return delMerchant(_merchantIds)
  }).then(() => {
    getList()
    ElMessage.success("删除成功")
  }).catch(() => {
  })
}

onMounted(() => {
  getList()
})
</script>


<style scoped>
/* 页面背景及间距整体重塑 */
.app-container {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: calc(100vh - 50px);
  display: flex;
  flex-direction: column;
  gap: 16px;
}

/* 卡片容器定义 */
.query-card, .table-card {
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  background: #ffffff;
}

.mb0 {
  margin-bottom: 0 !important;
}

/* 工具栏排列 */
.toolbar-container {
  margin-bottom: 16px;
  display: flex;
  justify-content: flex-start;
}

/* 表头与线条高级微调 */
.custom-merchant-table {
  --el-table-border-color: #f0f0f0;
}
.custom-merchant-table :deep(.el-table__header-th) {
  background-color: #fafafa !important;
  color: #2c3e50;
  font-weight: 600;
  height: 46px;
}

/* 核心文本样式统一 */
.font-bold {
  font-weight: 500;
  color: #303133;
}
.code-text {
  font-family: Monaco, Consolas, "Courier New", monospace;
  color: #606266;
  font-size: 13px;
}

/* 图片外层精细的“证件盒”样式 */
.image-box {
  display: inline-flex;
  padding: 2px;
  background-color: #ffffff;
  border: 1px solid #e4e7ed;
  border-radius: 6px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.02);
  overflow: hidden;
  transition: all 0.3s;
}
.image-box:hover {
  border-color: #409eff;
  transform: translateY(-1px);
}

/* 操作按钮包裹与间距微调 */
.action-buttons-wrapper {
  display: flex;
  justify-content: center;
  gap: 12px;
}

/* 辅助与缺省占位文本 */
.text-secondary {
  color: #909399;
}
.text-placeholder {
  color: #c0c4cc;
  font-size: 13px;
}

/* 分页容器靠右 */
.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
