<template>
  <div class="app-container">
    <!-- 顶部查询区域（用卡片包裹提升层次感） -->
    <el-card class="query-card" shadow="never">
      <el-form :model="query" ref="queryRef" label-width="70px" inline>
        <el-form-item label="用户名称" prop="userName" class="mb0">
          <el-input
              v-model="query.userName"
              placeholder="请输入用户名称"
              clearable
              @keyup.enter="handleQuery"
              style="width: 220px"
          />
        </el-form-item>
        <el-form-item class="mb0">
          <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
          <el-button icon="Refresh" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 主体内容区域（用卡片包裹，与顶部产生间距） -->
    <el-card class="table-card" shadow="never">
      <!-- 顶部操作工具栏 -->
      <div class="toolbar-container">
        <div class="toolbar-left">
          <el-button type="primary" icon="Plus" @click="handleInsert">新增用户</el-button>
          <el-button :disabled="single" type="success" plain icon="Edit" @click="handleUpdate">修改</el-button>
          <el-button :disabled="multple" type="danger" plain icon="Delete" @click="handleDelete">删除</el-button>
        </div>
        <div class="toolbar-right">
          <!-- 这里可以留空，或者放刷新/显隐列的小图标 -->
        </div>
      </div>

      <!-- 数据表格（去掉生硬的纯黑直角边框，加入斑马纹和现代过渡） -->
      <el-table
          :data="userList"
          style="width: 100%"
          stripe
          v-loading="loading"
          @selection-change="handleSelectionChange"
          class="custom-table"
      >
        <el-table-column type="selection" width="50" align="center"/>
        <el-table-column prop="userId" label="用户编号" width="120" align="center" class-name="text-secondary"/>
        <el-table-column prop="userName" label="用户名" align="left" min-width="120">
          <template #default="scope">
            <span class="font-bold">{{ scope.row.userName }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="phonenumber" label="手机号" align="center" width="140"/>


        <!-- 性别优化：使用 Tag 标签代替纯文字 -->
        <el-table-column prop="sex" label="性别" align="center" width="100">
          <template #default="scope">
            <el-tag v-if="scope.row.sex === 0" type="primary" size="small" effect="light">男</el-tag>
            <el-tag v-else-if="scope.row.sex === 1" type="danger" size="small" effect="light">女</el-tag>
            <el-tag v-else type="info" size="small" effect="light">未设置</el-tag>
          </template>
        </el-table-column>

        <!-- 头像优化：处理成现代的圆形或微圆角，加阴影 -->
        <el-table-column label="用户头像" align="center" prop="avatar" width="100">
          <template #default="scope">
            <div class="avatar-wrapper">
              <image-preview
                  :src="scope.row.avatar ? scope.row.avatar : defaultAvatar"
                  alt="avatar"
                  :width="40"
                  :height="40"
                  class="round-avatar"
              />
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="roleName" label="对应角色" align="center" min-width="120">
          <template #default="scope">
            <el-tag type="warning" plain size="small">{{ scope.row.roleName }}</el-tag>
          </template>
        </el-table-column>

        <!-- 操作列优化：适当的间距和样式区分 -->
        <el-table-column label="操作" align="center" width="240" fixed="right">
          <template #default="scope">
            <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)">修改</el-button>
            <el-button link type="warning" icon="RefreshRight" @click="handleResetPwd(scope.row)">重置密码</el-button>
            <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页组件区域（右对齐，加顶部内边距） -->
      <div class="pagination-container">
        <pagination
            :total="total"
            v-model:page="query.pageNum"
            v-model:limit="query.pageSize"
            @pagination="getList"
        />
      </div>
    </el-card>

    <!-- 添加或修改用户对话框 -->
    <vxe-modal :title="title" v-model="open" width="500px" showFooter show-maximize resize>
      <el-form ref="userRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="用户名" prop="userName">
          <el-input v-model="form.userName" placeholder="请输入用户名"/>
        </el-form-item>
        <el-form-item label="手机号" prop="phonenumber">
          <el-input v-model="form.phonenumber" placeholder="请输入手机号" maxlength="11"/>
        </el-form-item>
        <el-form-item label="性别" prop="sex">
          <el-radio-group v-model="form.sex">
            <el-radio :value="0">男</el-radio>
            <el-radio :value="1">女</el-radio>
          </el-radio-group>
        </el-form-item>
        <!-- 仅在新增时显示密码框，若修改时也需要修改密码可去掉 v-if -->
        <el-form-item v-if="!form.userId" label="密码" prop="password">
          <el-input v-model="form.password" placeholder="请输入密码" show-password/>
        </el-form-item>
        <el-form-item label="对应角色" prop="roleId">
          <el-select v-model="form.roleId" placeholder="请选择角色">
            <el-option
                v-for="role in roleList"
                :key="role.roleId"
                :label="role.roleName"
                :value="role.roleId"
            />
          </el-select>
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
import {onMounted, ref} from "vue";
import {deleteUserByUserIds, insertUser, selectUserByUserId, selectUserList, updateUser} from "@/api/system/user.js";

// 默认头像
import defaultAvatar from '@/assets/images/profile.jpg'
import Pagination from "@/components/Pagination/index.vue";
import {VxeModal} from "vxe-pc-ui";
import {ElMessage, ElMessageBox} from "element-plus";
import {selectAllRole} from "@/api/system/role.js";

// 表单实例
const userRef = ref()

// 对话框title
const title = ref('')

// 对话框是否打开
const open = ref(false)

// 表单参数（增加了 phonenumber）
const form = ref({
  userId: null,
  userName: null,
  phonenumber: null,
  sex: null,
  password: null,
  roleId: null
})

// 表单校验
const rules = ref({
  userName: [
    {required: true, message: '请输入用户名', trigger: 'blur'}
  ],
  password: [
    {required: true, message: '请输入密码', trigger: 'blur'}
  ],
  roleId: [
    {required: true, message: '请选择角色', trigger: 'change'}
  ],
  // 手机号基础验证（选填，如果填了必须符合手机号格式）
  phonenumber: [
    {pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur'}
  ]
})

// 新增按钮
const handleInsert = () => {
  form.value = {
    userId: null,
    userName: null,
    phonenumber: null,
    sex: null,
    password: null,
    roleId: null
  }
  open.value = true
  title.value = '新增用户'
}

// 修改按钮
const handleUpdate = (row) => {
  const userId = row.userId || ids.value[0] // 如果是用顶部批量修改按钮，取选中的第一个
  selectUserByUserId(userId).then(res => {
    form.value = res.data
    open.value = true
    title.value = '修改用户'
  })
}

// 删除按钮
const handleDelete = (row) => {
  const userIds = row.userId || ids.value
  ElMessageBox.confirm(
      '是否确认删除用户?',
      '系统提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
  )
      .then(() => {
        // 调用删除api
        deleteUserByUserIds(userIds).then(res => {
          ElMessage.success('删除成功')
          getList()
        })
      })
}

// 新增：重置密码逻辑
const handleResetPwd = (row) => {
  ElMessageBox.confirm(
      `是否确认重置用户 "${row.userName}" 的账号密码？`,
      '安全提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
  ).then(() => {
    // 构建重置密码的数据，传入对应的 userId 和固定的新密码
    const resetData = {
      userId: row.userId,
      password: '123456'
    }
    // 调用你原有的修改用户信息接口来更新密码
    updateUser(resetData).then(res => {
      ElMessage.success(`用户 "${row.userName}" 的密码已成功重置为 123456`)
      getList()
    })
  }).catch(() => {
    // 点击取消不做任何操作
  })
}

// 保存按钮
const submitForm = () => {
  userRef.value.validate(valid => {
    if (valid) {
      if (form.value.userId != null) {
        // 调用修改api
        updateUser(form.value).then(res => {
          ElMessage.success('修改成功')
          open.value = false
          getList()
        })
      } else {
        // 调用新增api
        insertUser(form.value).then(res => {
          ElMessage.success('新增成功')
          open.value = false
          getList()
        })
      }
    }
  })
}

// 后端路径
const baseUrl = import.meta.env.VITE_APP_BASE_API

// 顶部查询表单实例
const queryRef = ref()

// 查询参数
const query = ref({
  pageNum: 1,
  pageSize: 10,
  userName: null,
})

// 用户列表数据
const userList = ref([])

// 当前是否未选中单行
const single = ref(true)

// 当前是否未选中多行
const multple = ref(true)

// 数据总数
const total = ref(0)

// 查询数据
const getList = () => {
  selectUserList(query.value).then(res => {
    userList.value = res.rows
    total.value = res.total
  })
}

// 已勾选的id数组
const ids = ref([])

// 多选时的触发方法
const handleSelectionChange = (selection) => {
  ids.value = selection.map(item => item.userId)
  single.value = selection.length !== 1
  multple.value = !selection.length
}

// 搜索按钮
const handleQuery = () => {
  query.value.pageNum = 1
  getList()
}

// 重置按钮
const resetQuery = () => {
  queryRef.value.resetFields()
  handleQuery()
}

// 角色列表数据
const roleList = ref([])

onMounted(() => {
  getList()

  // 查询所有角色列表
  selectAllRole().then(res => {
    roleList.value = res.data
  })
})
</script>

<style scoped>
/* 页面基础容器 */
.app-container {
  padding: 20px;
  background-color: #f5f7fa; /* 淡淡的灰色背景，让白色的卡片更有层次感 */
  min-height: calc(100vh - 50px);
  display: flex;
  flex-direction: column;
  gap: 16px; /* 模块之间的标准间距 */
}

/* 卡片样式定制 */
.query-card, .table-card {
  border: 1px solid #e4e7ed;
  border-radius: 8px; /* 现代感圆角 */
  background: #ffffff;
}

/* 消除表单单项底部的默认间距（当在一行展示时） */
.mb0 {
  margin-bottom: 0 !important;
}

/* 工具栏布局 */
.toolbar-container {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}
.toolbar-left {
  display: flex;
  gap: 8px;
}

/* 表格现代感微调 */
.custom-table {
  --el-table-border-color: #f0f0f0;
  border-radius: 4px;
  overflow: hidden;
}
.custom-table :deep(.el-table__header-th) {
  background-color: #fafafa !important; /* 表头浅灰背景，看起来更稳重 */
  color: #333333;
  font-weight: 600;
}

/* 辅助字色与加粗 */
.text-secondary {
  color: #909399;
}
.font-bold {
  font-weight: 500;
  color: #303133;
}

/* 头像外层与圆形化 */
.avatar-wrapper {
  display: flex;
  justify-content: center;
  align-items: center;
}
.round-avatar {
  border-radius: 50% !important; /* 变成圆形头像 */
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05); /* 微弱阴影 */
  border: 1px solid #e4e7ed;
  overflow: hidden;
}

/* 分页容器靠右对齐 */
.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

/* 优化操作按钮在悬浮和连续排列时的视觉 */
.el-button + .el-button {
  margin-left: 12px;
}
</style>
