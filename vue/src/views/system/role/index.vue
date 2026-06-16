<template>
  <div class="app-container">
    <!-- 顶部查询区域（阴影微调，增强层次感） -->
    <el-card class="query-card" shadow="never">
      <el-form :model="query" ref="queryRef" label-width="70px" inline>
        <el-form-item label="角色名称" prop="roleName" class="mb0">
          <el-input
              v-model="query.roleName"
              placeholder="请输入角色名称"
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

    <!-- 主体内容区域（卡片包裹） -->
    <el-card class="table-card" shadow="never">
      <!-- 顶部操作工具栏 -->
      <div class="toolbar-container">
        <div class="toolbar-left">
          <el-button type="primary" icon="Plus" @click="handleInsert">新增角色</el-button>
          <el-button :disabled="single" type="success" plain icon="Edit" @click="handleUpdate">修改</el-button>
          <el-button :disabled="multiple" type="danger" plain icon="Delete" @click="handleDelete">删除</el-button>
        </div>
        <div class="toolbar-right">
          <!-- 这里可放置控制表格列显隐、刷新的小组件 -->
        </div>
      </div>

      <!-- 数据表格（去掉生硬的黑边框，改用轻量级斑马纹和现代悬浮过渡） -->
      <el-table
          :data="roleList"
          style="width: 100%"
          stripe
          v-loading="loading"
          @selection-change="handleSelectionChange"
          class="custom-table"
      >
        <!-- 复选框 -->
        <el-table-column type="selection" width="50" align="center"/>

        <!-- 角色编号：弱化字色，使其不喧宾夺主 -->
        <el-table-column prop="roleId" label="角色编号" width="150" align="center">
          <template #default="scope">
            <span class="id-text">{{ scope.row.roleId }}</span>
          </template>
        </el-table-column>

        <!-- 角色名称：靠左对齐，并加入一个精致的色彩前缀，让少列的表格不显空旷 -->
        <el-table-column prop="roleName" label="角色名称" align="left" min-width="200">
          <template #default="scope">
            <div class="role-name-wrapper">
              <span class="role-avatar-badge">{{ scope.row.roleName?.substring(0, 1) }}</span>
              <span class="role-name-text">{{ scope.row.roleName }}</span>
            </div>
          </template>
        </el-table-column>

        <!-- 操作列：按钮间距微调，固定在右侧 -->
        <el-table-column label="操作" align="center" width="180" fixed="right">
          <template #default="scope">
            <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)">修改</el-button>
            <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页组件区域（右对齐） -->
      <div class="pagination-container">
        <pagination
            :total="total"
            v-model:page="query.pageNum"
            v-model:limit="query.pageSize"
            @pagination="getList"
        />
      </div>
    </el-card>

    <!-- 添加或修改角色对话框 -->
    <vxe-modal :title="title" v-model="open" width="500px" showFooter show-maximize resize>
      <el-form ref="roleRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="角色名称" prop="roleName">
          <el-input v-model="form.roleName" placeholder="请输入角色名称"/>
        </el-form-item>
        <el-form-item label="角色顺序" prop="roleSort">
          <el-input v-model="form.roleSort" placeholder="请输入角色顺序"/>
        </el-form-item>
        <el-form-item label="菜单权限">
          <el-tree
              style="width: 100%"
              :data="menuOptions"
              show-checkbox
              default-expand-all
              ref="menuRef"
              node-key="id"
              :props="{ label: 'label', children: 'children' }"
          />
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
import {nextTick, onMounted, ref} from "vue";
import {deleteRoleByRoleIds, insertRole, selectRoleByRoleId, selectRoleList, updateRole} from "@/api/system/role.js";
import Pagination from "@/components/Pagination/index.vue";
import {VxeModal} from "vxe-pc-ui";
import {ElMessage, ElMessageBox} from "element-plus";
import {selectRoleMenusTree, selectRoleMenuTree} from "@/api/system/menu.js";

//菜单权限表单实例
const menuRef = ref()

//表单实例
const roleRef = ref()

//对话框title
const title = ref('')

//对话框是否打开
const open = ref(false)

//表单参数
const form = ref({
  roleId: null,
  roleName: null,
  roleSort: null,
  menuIds: []
})

//表单校验
const rules = ref({
  roleName: [
    {required: true, message: '请输入角色名称', trigger: 'blur'}
  ],
  roleSort: [
    {required: true, message: '请输入角色顺序', trigger: 'blur'}
  ],
})

//新增按钮
const handleInsert = () => {
  if (menuRef.value) {
    menuRef.value.setCheckedKeys([])
  }
  form.value = {
    roleId: null,
    roleName: null,
    roleSort: null,
    menuIds: []
  }
  selectRoleMenusTree().then(res => {
    menuOptions.value = res.data
    open.value = true
    title.value = '新增角色'
  })
}

//修改按钮
const handleUpdate = (row) => {
  if (menuRef.value) {
    menuRef.value.setCheckedKeys([])
  }
  form.value = {
    roleId: null,
    roleName: null,
    roleSort: null,
    menuIds: []
  }
  const roleId = row.roleId || ids.value

  //根据角色ID查询对应菜单树
  const roleMenu = getRoleMenuTreeSelect(roleId);
  selectRoleByRoleId(roleId).then(res => {
    form.value = res.data
    open.value = true
    title.value = '修改角色'

    //等待DOM更新完成后执行菜单选中操作
    nextTick(() => {
      //等待菜单树数据加载完成
      roleMenu.then((res) => {
        //获取该角色已选中的菜单key数组
        let checkedKeys = res.checkedKeys
        //遍历所有已选中的菜单key, 在菜单树组件中设置选中状态
        checkedKeys.forEach((v) => {
          //针对每个菜单ID, 再次使用nextTick确保菜单树组件完全准备好
          nextTick(() => {
            menuRef.value.setChecked(v, true, false)
          })
        })

      })
    })

  })
}

//菜单树数据
const menuOptions = ref([])

//根据角色ID查询对应菜单树
const getRoleMenuTreeSelect = (roleId) => {
  return selectRoleMenuTree(roleId).then(res => {
    //重置菜单选项前先清空数组, 防止重复数据
    menuOptions.value = []
    //然后再赋值新的菜单树数据
    menuOptions.value = res.menus
    return res
  })
}

//删除按钮
const handleDelete = (row) => {
  console.log(ids.value, '看看ID数组')
  const roleIds = row.roleId || ids.value
  ElMessageBox.confirm(
      '是否确认删除角色?',
      '系统提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
  )
      .then(() => {
        //调用删除api
        deleteRoleByRoleIds(roleIds).then(res => {
          ElMessage.success('删除成功')
          getList()
        })
      })
}

//保存按钮
const submitForm = () => {
  roleRef.value.validate(valid => {
    if (valid) {
      if (form.value.roleId != null) {
        form.value.menuIds = getMenuAllCheckedKeys()
        //调用修改api
        updateRole(form.value).then(res => {
          ElMessage.success('修改成功')
          open.value = false
          getList()
        })
      } else {
        //调用新增api
        form.value.menuIds = getMenuAllCheckedKeys()
        insertRole(form.value).then(res => {
          ElMessage.success('新增成功')
          open.value = false
          getList()
        })
      }
    }
  })
}

//获取菜单组件中所有被选中的节点
const getMenuAllCheckedKeys = () => {
  //全选中的菜单节点
  let checkedKeys = menuRef.value.getCheckedKeys()
  //半选中的菜单节点
  let halfCheckedKeys = menuRef.value.getHalfCheckedKeys()
  //将半选中的节点合并到已选中的节点列表中
  checkedKeys.unshift.apply(checkedKeys, halfCheckedKeys)
  //返回合并后的所有选中节点ID数组
  return checkedKeys
}

//已勾选的id数组
const ids = ref([])

//当前是否未选中单行
const single = ref(true)

//当前是否未选中多行
const multiple = ref(true)

//顶部查询表单实例
const queryRef = ref()

//多选时的触发方法
const handleSelectionChange = (selection) => {
  ids.value = selection.map(item => item.roleId)
  single.value = selection.length !== 1
  multiple.value = !selection.length
}

//查询参数
const query = ref({
  pageNum: 1,
  pageSize: 10,
  roleName: null,
})

//角色列表数据
const roleList = ref([])

//数据总数
const total = ref(0)

//搜索按钮
const handleQuery = () => {
  query.value.pageNum = 1
  getList()
}

//重置按钮
const resetQuery = () => {
  queryRef.value.resetFields()
  handleQuery()
}

//查询数据
const getList = () => {
  selectRoleList(query.value).then(res => {
    roleList.value = res.rows
    total.value = res.total
  })
}

onMounted(() => {
  getList()
})
</script>

<style scoped>
/* 页面基础容器 */
.app-container {
  padding: 20px;
  background-color: #f5f7fa; /* 淡淡的灰色背景，形成纸张堆叠感 */
  min-height: calc(100vh - 50px);
  display: flex;
  flex-direction: column;
  gap: 16px;
}

/* 卡片容器定制 */
.query-card, .table-card {
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  background: #ffffff;
}

/* 消除内边距溢出 */
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
}
.custom-table :deep(.el-table__header-th) {
  background-color: #fafafa !important; /* 表头浅灰，看起来更扎实 */
  color: #333333;
  font-weight: 600;
  height: 46px;
}

/* 编号文字弱化 */
.id-text {
  font-family: Monaco, monospace;
  color: #909399;
  font-size: 13px;
}

/* 角色列样式：虚拟头像组件，填补列少带来的空旷感 */
.role-name-wrapper {
  display: flex;
  align-items: center;
  gap: 10px;
}
.role-avatar-badge {
  display: inline-flex;
  justify-content: center;
  align-items: center;
  width: 24px;
  height: 24px;
  background-color: #ecf5ff;
  color: #409eff;
  font-size: 12px;
  font-weight: bold;
  border-radius: 4px;
  border: 1px solid #d9ecff;
}
.role-name-text {
  font-weight: 500;
  color: #303133;
}

/* 分页容器靠右对齐 */
.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

/* 操作链接按钮间距 */
.el-button + .el-button {
  margin-left: 16px;
}
</style>
