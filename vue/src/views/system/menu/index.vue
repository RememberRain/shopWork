<template>
  <div class="app-container">
    <!-- 顶部查询区域（阴影微调，增强层次感） -->
    <el-card class="query-card" shadow="never">
      <el-form :model="query" ref="queryRef" label-width="70px" inline>
        <el-form-item label="菜单名称" prop="menuName" class="mb0">
          <el-input
              v-model="query.menuName"
              placeholder="请输入菜单名称"
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
      <!-- 顶部操作按钮区域 -->
      <div class="toolbar-container">
        <el-button type="primary" icon="Plus" @click="handleInsert">新增菜单</el-button>
      </div>

      <!-- 树形数据表格优化 -->
      <el-table
          :data="menuList"
          style="width: 100%"
          row-key="menuId"
          :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
          v-loading="loading"
          class="custom-tree-table"
      >
        <!-- 核心改动：树形节点列必须靠左(left)对齐，否则展开箭头样式会错乱 -->
        <el-table-column prop="menuName" label="菜单名称" align="left" min-width="180" show-overflow-tooltip>
          <template #default="scope">
            <span class="menu-name-text">{{ scope.row.menuName }}</span>
          </template>
        </el-table-column>

        <!-- 图标列：加入微弱的背景框，让图标看起来像个独立的Badge -->
        <el-table-column prop="icon" label="图标" align="center" width="80">
          <template #default="scope">
            <div v-if="scope.row.icon" class="icon-box">
              <svg-icon :icon-class="scope.row.icon" class="menu-icon" />
            </div>
            <span v-else class="text-placeholder">-</span>
          </template>
        </el-table-column>

        <!-- 排序：使用数字标签，凸显序号 -->
        <el-table-column prop="menuSort" label="排序" align="center" width="80">
          <template #default="scope">
            <el-tag size="small" type="info" effect="plain" class="sort-tag">
              {{ scope.row.menuSort }}
            </el-tag>
          </template>
        </el-table-column>

        <!-- 组件路径：代码块风格包裹，更符合开发人员视觉 -->
        <el-table-column prop="component" label="组件路径" align="left" min-width="200" show-overflow-tooltip>
          <template #default="scope">
            <code v-if="scope.row.component" class="component-code">{{ scope.row.component }}</code>
            <span v-else class="text-placeholder">---</span>
          </template>
        </el-table-column>

        <!-- 操作列：按钮间距微调 -->
        <el-table-column label="操作" align="center" width="180" fixed="right">
          <template #default="scope">
            <el-button link type="primary" icon="Edit" @click="handleUpdate(scope.row)">修改</el-button>
            <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 添加或修改角色对话框 -->
    <vxe-modal :title="title" v-model="open" width="40%" showFooter show-maximize resize>
      <el-form ref="menuRef" :model="form" :rules="rules" label-width="100px">
        <el-row>
          <el-col :span="12">
            <el-form-item label="上级菜单">
              <el-tree-select v-model="form.parentId"
                              :data="menuOptions"
                              :props="{ value: 'menuId', label: 'menuName', children: 'children' }"
                              check-strictly
                              value-key="menuId"
                              placeholder="选择上级菜单"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="菜单类型" prop="menuType">
              <el-radio-group v-model="form.menuType">
                <el-radio value="M">目录</el-radio>
                <el-radio value="C">菜单</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="菜单图标" prop="icon">
              <el-popover
                  placement="bottom-start"
                  :width="550"
                  trigger="click"
              >
                <template #reference>
                  <el-input v-model="form.icon" placeholder="点击选择图标" @blur="">
                    <template #prefix>
                      <svg-icon v-if="form.icon" :icon-class="form.icon" style="height: 32px; width: 16px;"/>
                      <el-icon v-else>
                        <Search/>
                      </el-icon>
                    </template>
                  </el-input>
                </template>
                <icon-select ref="iconSelectRef" @selected="selectedIcon"/>
              </el-popover>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="显示排序" prop="menuSort">
              <el-input-number v-model="form.menuSort" :min="0"/>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="菜单名称" prop="menuName">
              <el-input v-model="form.menuName" placeholder="请输入菜单名称"/>
            </el-form-item>
          </el-col>
          <el-col :span="12" v-if="form.menuType === 'C'">
            <el-form-item label="路由地址" prop="path">
              <el-input v-model="form.path" placeholder="请输入路由地址"/>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12" v-if="form.menuType === 'C'">
            <el-form-item label="组件路径" prop="component">
              <el-input v-model="form.component" placeholder="请输入组件路径"/>
            </el-form-item>
          </el-col>
        </el-row>
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
import {deleteMenuByMenuId, insertMenu, selectMenuByMenuId, selectMenuList, updateMenu} from "@/api/system/menu.js";
import SvgIcon from "@/components/SvgIcon/index.vue";
import {VxeModal} from "vxe-pc-ui";
import {ElMessage, ElMessageBox, ElTreeSelect} from 'element-plus'
import {Search} from "@element-plus/icons-vue";
import IconSelect from "@/components/IconSelect/index.vue";

//图标选择组件实例
const iconSelectRef = ref()

//选择图标回调事件
const selectedIcon = (name) => {
  form.value.icon = name
}

//对话框title
const title = ref('')

//对话框是否打开
const open = ref(false)

//表单参数
const form = ref({
  menuId: null,
  parentId: null,
  menuName: null,
  icon: null,
  menuType: 'M',
  menuSort: null,
  path: null,
  component: null
})

//表单校验
const rules = ref({
  menuName: [
    {required: true, message: '请输入菜单名称', trigger: 'blur'}
  ],
  menuSort: [
    {required: true, message: '请输入菜单顺序', trigger: 'blur'}
  ],
  path: [
    {required: true, message: '请输入路由顺序', trigger: 'blur'}
  ],
  component: [
    {required: true, message: '请输入组件路径', trigger: 'blur'}
  ],
})

//对话框表单实例
const menuRef = ref()

//新增按钮
const handleInsert = () => {
  form.value = {
    menuId: null,
    parentId: 0,
    menuName: null,
    icon: null,
    menuType: 'M',
    menuSort: null,
    path: null,
    component: null
  }
  if (menuOptions.value.length === 0) {
    getTreeSelect()
  }
  open.value = true
  title.value = '新增菜单'
}

//修改按钮
const handleUpdate = (row) => {
  const menuId = row.menuId
  if (menuOptions.value.length === 0) {
    getTreeSelect()
  }

  selectMenuByMenuId(menuId).then(res => {
    form.value = res.data
    open.value = true
    title.value = '修改菜单'
  })
}

//删除按钮
const handleDelete = (row) => {
  const menuId = row.menuId
  ElMessageBox.confirm(
      '是否确认删除菜单?',
      '系统提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
  )
      .then(() => {
        //调用删除api
        deleteMenuByMenuId(menuId).then(res => {
          ElMessage.success('删除成功')
          getList()
        })
      })
}

//菜单下拉树数据
const menuOptions = ref([])

//查询菜单下拉树结构
const getTreeSelect = () => {
  selectMenuList().then(res => {
    const menu = {menuId: 0, menuName: '主层级', children: []}
    menu.children = buildTree(res.data, 0)
    menuOptions.value.push(menu)
  })
}

//菜单列表
const menuList = ref([])

//顶部查询表单实例
const queryRef = ref()

//查询参数
const query = ref({
  menuName: null
})

//保存按钮
const submitForm = () => {
  menuRef.value.validate(valid => {
    if (valid) {
      if (form.value.menuId != null) {
        //调用修改api
        updateMenu(form.value).then(res => {
          ElMessage.success('修改成功')
          open.value = false
          getList()
        })
      } else {
        //调用新增api
        insertMenu(form.value).then(res => {
          ElMessage.success('新增成功')
          open.value = false
          getList()
        })
      }
    }
  })
}

//搜索按钮
const handleQuery = () => {
  getList()
}

//重置按钮
const resetQuery = () => {
  queryRef.value.resetFields()
  handleQuery()
}

//查询数据
const getList = () => {
  selectMenuList(query.value).then(res => {
    if (query.value.menuName != null) {
      //如果有查询参数(根据菜单名称条件查询)
      menuList.value = res.data
    } else {
      menuList.value = buildTree(res.data, 0)
    }
  })
}

//构建树形结构数据
//之前的数据: [{menuId: 1, parentId: 0}, {menuId: 2, parentId: 1}]
//转换成嵌套的树形结构: [{menuId: 1, parentId: 0, children: [{menuId: 2, parentId: 1}]} ]
const buildTree = (data, parentId) => {
  //存放当前层级的所有菜单项
  const result = []
  //遍历所有菜单数据
  for (const item of data) {
    //如果当前菜单的父ID等于传入的parentId, 说明它是当前层级的菜单
    //比如: parentId = 0时, 就找出它的子菜单
    if (item.parentId === parentId) {
      //递归查找当前菜单的所有子菜单
      const children = buildTree(data, item.menuId);
      //如果找到了子菜单
      if (children.length > 0) {
        //将子菜单数组挂在到当前菜单的children数组属性下
        item.children = children
      }
      //将处理好的菜单项加入到结果数组中
      result.push(item)
    }
  }
  //返回当前层级的所有菜单
  return result
}

onMounted(() => {
  getList()
})
</script>

<style scoped>
/* 页面基础布局 */
.app-container {
  padding: 20px;
  background-color: #f5f7fa; /* 浅灰底色，衬托白色卡片 */
  min-height: calc(100vh - 50px);
  display: flex;
  flex-direction: column;
  gap: 16px;
}

/* 卡片样式容器 */
.query-card, .table-card {
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  background: #ffffff;
}

/* 消除行内表单的底部间距 */
.mb0 {
  margin-bottom: 0 !important;
}

/* 工具栏样式 */
.toolbar-container {
  margin-bottom: 16px;
  display: flex;
  justify-content: flex-start;
}

/* 树形表格定制 */
.custom-tree-table {
  --el-table-border-color: #f0f0f0;
}

/* 优化表带头，使其更稳重 */
.custom-tree-table :deep(.el-table__header-th) {
  background-color: #fafafa !important;
  color: #333333;
  font-weight: 600;
  height: 45px;
}

/* 树形菜单文字微调 */
.menu-name-text {
  font-weight: 500;
  color: #303133;
  margin-left: 4px;
}

/* 图标小方块效果 */
.icon-box {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  background-color: #f0f2f5;
  border-radius: 6px;
  color: #606266;
}
.menu-icon {
  font-size: 16px;
}

/* 排序标签圆角微调 */
.sort-tag {
  border-radius: 4px;
  font-weight: bold;
}

/* 组件路径的代码高亮块视觉 */
.component-code {
  font-family: Monaco, Menlo, Consolas, "Courier New", monospace;
  background-color: #f4f4f5;
  color: #909399;
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 13px;
  border: 1px solid #e9e9eb;
}

/* 空值占位符样式 */
.text-placeholder {
  color: #c0c4cc;
  font-size: 13px;
}

/* 操作链接按钮间距 */
.el-button + .el-button {
  margin-left: 16px;
}
</style>
