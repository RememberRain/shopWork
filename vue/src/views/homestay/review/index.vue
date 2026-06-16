<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryRef" :inline="true" v-show="showSearch" class="search-form-card">
      <el-form-item label="商品名称" prop="title">
        <el-input
            v-model="queryParams.title"
            placeholder="请输入商品名称"
            clearable
            @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="评价用户" prop="userName">
        <el-input
            v-model="queryParams.userName"
            placeholder="请输入评价用户"
            clearable
            @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <div class="table-container">
      <el-table @row-click="clickRow" ref="tableRef" highlight-current-row
                border v-loading="loading" :data="reviewList" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column label="序号" align="center" type="index" :index="indexMethod" width="70" />
        <el-table-column label="商品名称" align="left" prop="title" min-width="150" show-overflow-tooltip />
        <el-table-column label="评分/顾客" align="center" width="160">
          <template #default="scope">
            <div class="customer-info-cell">
              <el-rate v-model="scope.row.score" disabled />
              <span class="user-badge"><el-icon><User /></el-icon> {{ scope.row.userName }}</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="评价及回复详情" align="left" min-width="320">
          <template #default="scope">
            <div class="review-details-flow">
              <div class="user-comment">
                <span class="comment-label">初评：</span>
                <span class="comment-text">{{ scope.row.content }}</span>
              </div>

              <div v-if="scope.row.replyContent" class="merchant-reply-bubble">
                <div class="reply-header">
                  <span class="reply-badge">商家回信</span>
                  <span class="reply-time">{{ scope.row.replyTime }}</span>
                </div>
                <p class="reply-body">{{ scope.row.replyContent }}</p>
              </div>
              <div v-else class="no-reply-placeholder">
                <el-icon><ChatLineRound /></el-icon> 暂无商家回复信息
              </div>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="操作" align="center" width="160" class-name="small-padding fixed-width">
          <template #default="scope">
            <el-button link type="primary" icon="ChatDotRound" @click="handleReply(scope.row)">
              {{ scope.row.replyContent ? '修改回复' : '回复' }}
            </el-button>
            <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <pagination
        v-show="total>0"
        :total="total"
        v-model:page="queryParams.pageNum"
        v-model:limit="queryParams.pageSize"
        @pagination="getList"
    />

    <vxe-modal :title="title" v-model="open" width="560px" show-maximize showFooter resize>
      <div class="reply-modal-body">

        <div class="original-review-envelope">
          <div class="envelope-row">
            <span class="env-title">{{ form.title }}</span>
            <el-rate v-model="form.score" disabled />
          </div>
          <div class="envelope-content">
            <strong>顾客评价：</strong>"{{ form.content }}"
          </div>
        </div>

        <el-form ref="reviewRef" :model="form" :rules="replyRules" label-position="top">
          <el-form-item label="输入回复内容" prop="replyContent">
            <el-input
                v-model="form.replyContent"
                type="textarea"
                :rows="5"
                placeholder="请输入您对顾客的感谢或解释，文明用语，真诚沟通..."
                maxlength="300"
                show-word-limit
            />
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button type="primary" @click="submitReplyForm" :loading="submitLoading">回 复</el-button>
          <el-button @click="cancel">取 消</el-button>
        </div>
      </template>
    </vxe-modal>
  </div>
</template>

<script setup>
import { listReview, getReview, delReview, updateReview } from "@/api/homestay/review"
import { ElMessage, ElMessageBox ,} from "element-plus"
import { onMounted, reactive, ref, toRefs } from "vue"
import { VxeModal } from "vxe-pc-ui"
import {ChatLineRound, User} from "@element-plus/icons-vue";

const queryRef = ref()
const reviewRef = ref()
const reviewList = ref([])
const open = ref(false)
const loading = ref(true)
const submitLoading = ref(false)
const showSearch = ref(true)
const ids = ref([])
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
    userName: null
  },
  // 回复专属校验规则
  replyRules: {
    replyContent: [
      { required: true, message: "回复内容不能为空", trigger: "blur" },
      { min: 5, message: "回复内容不能少于5个字符", trigger: "blur" }
    ]
  }
})

const { queryParams, form, replyRules } = toRefs(data)

// 点击行勾选单选框
const clickRow = (row) => {
  selectedRow.value = row
  const table = tableRef.value
  table.clearSelection()
  table.toggleRowSelection(row, true)
}

/** 动态序号计算 */
const indexMethod = (index) => {
  let pageNum = queryParams.value.pageNum - 1
  return pageNum > 0 ? (index + 1) + (pageNum * queryParams.value.pageSize) : (index + 1)
}

/** 调取后台评价列表 */
const getList = () => {
  loading.value = true
  listReview(queryParams.value).then(response => {
    reviewList.value = response.rows
    total.value = response.total
    loading.value = false
  })
}

// 取消及弹窗抹平
const cancel = () => {
  open.value = false
  submitLoading.value = false
  reset()
}

// 浅重置
const reset = () => {
  form.value = {
    reviewId: null,
    homestayId: null,
    score: null,
    content: null,
    replyContent: null,
    replyTime: null
  }
  if (reviewRef.value) {
    reviewRef.value.resetFields()
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
  ids.value = selection.map(item => item.reviewId)
}

/** 唤醒商家回复视图 */
const handleReply = (row) => {
  reset()
  // 深拷贝数据，防止修改时直接污染表格行元数据
  form.value = JSON.parse(JSON.stringify(row))
  title.value = row.replyContent ? "修订商家回复" : "回复顾客评价"
  open.value = true
}

/** 提交回复业务核心 */
const submitReplyForm = () => {
  reviewRef.value.validate(valid => {
    if (valid) {
      submitLoading.value = true
      // 组装回复字段
      const payload = {
        reviewId: form.value.reviewId,
        replyContent: form.value.replyContent
      }
      // 调用既有 edit 统一更新接口完成更新
      updateReview(payload).then(() => {
        ElMessage.success("回复成功")
        open.value = false
        submitLoading.value = false
        getList()
      }).catch(() => {
        submitLoading.value = false
      })
    }
  })
}

/** 删除操作 */
const handleDelete = (row) => {
  const _reviewIds = row.reviewId || ids.value
  ElMessageBox.confirm('确定要删除该条评价数据吗？此操作不可逆。', '系统安全提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'size'
  }).then(() => {
    return delReview(_reviewIds)
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
/* 优雅中台样式沉淀 */
.search-form-card {
  background-color: #fafafa;
  padding: 18px 18px 4px 18px;
  border-radius: 8px;
  border: 1px solid #eef2f6;
  margin-bottom: 16px;
}
.table-container {
  margin-top: 10px;
}

.customer-info-cell {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
}
:deep(.el-rate) {
  height: auto;
}
.user-badge {
  font-size: 12px;
  color: #64748b;
  background-color: #f1f5f9;
  padding: 2px 8px;
  border-radius: 12px;
}

/* 复合评论与回复时间轴流 */
.review-details-flow {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 4px 0;
}
.user-comment {
  font-size: 14px;
  color: #334155;
  line-height: 1.4;
}
.comment-label {
  font-weight: 600;
  color: #0f172a;
}

/* 商家回复高级现代质感气泡 */
.merchant-reply-bubble {
  background-color: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 6px;
  padding: 10px 14px;
  position: relative;
}
.reply-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 4px;
}
.reply-badge {
  font-size: 11px;
  background-color: #e0f2fe;
  color: #0369a1;
  font-weight: 600;
  padding: 1px 6px;
  border-radius: 4px;
}
.reply-time {
  font-size: 11px;
  color: #94a3b8;
}
.reply-body {
  font-size: 13px;
  color: #475569;
  margin: 0;
  line-height: 1.4;
  text-align: justify;
}
.no-reply-placeholder {
  font-size: 12px;
  color: #94a3b8;
  font-style: italic;
  display: flex;
  align-items: center;
  gap: 4px;
  padding-left: 2px;
}

/* 弹出层信封样式 */
.reply-modal-body {
  padding: 8px 4px;
}
.original-review-envelope {
  background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  padding: 14px;
  margin-bottom: 20px;
}
.envelope-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}
.env-title {
  font-weight: 700;
  color: #1e293b;
  font-size: 15px;
}
.envelope-content {
  font-size: 13.5px;
  color: #475569;
  line-height: 1.5;
  background-color: #ffffff;
  padding: 8px 12px;
  border-radius: 6px;
  border: 1px solid #eef2f6;
}
</style>
