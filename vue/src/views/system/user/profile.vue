<template>
  <div class="profile-container">
    <div class="bg-glow"></div>

    <el-card class="profile-card" shadow="always">
      <div class="profile-header">
        <div class="avatar-wrapper">
          <el-upload
              class="avatar-uploader"
              :action="uploadUrl"
              :headers="headers"
              :show-file-list="false"
              :on-success="handleAvatarSuccess"
              :on-error="handleAvatarError"
              :before-upload="beforeUpload"
          >
            <div class="avatar-mask">
              <el-icon><Camera /></el-icon>
              <span>更换头像</span>
            </div>
            <img v-if="userStore.avatar" :src="userStore.avatar" class="avatar" alt="用户头像"/>
            <div v-else class="avatar-placeholder">
              <el-icon><User /></el-icon>
            </div>
          </el-upload>
        </div>
        <h2 class="welcome-title">欢迎回来，{{ userStore.name }}</h2>
        <p class="welcome-sub">个性化设置与账户安全管理中心</p>
      </div>

      <div class="info-list">
        <div class="info-item">
          <div class="info-left">
            <el-icon class="info-icon name-icon"><User /></el-icon>
            <span class="info-label">用户名称</span>
          </div>
          <div class="info-value">{{ userStore.name || '未设置' }}</div>
        </div>

        <div class="info-item">
          <div class="info-left">
            <el-icon class="info-icon phone-icon"><Cellphone /></el-icon>
            <span class="info-label">绑定手机</span>
          </div>
          <div class="info-value">{{ maskPhone(state.user.phonenumber) || '暂无手机号' }}</div>
        </div>

        <div class="info-item">
          <div class="info-left">
            <el-icon class="info-icon sex-icon" v-if="state.user.sex === 0"><Male /></el-icon>
            <el-icon class="info-icon sex-icon" v-else><Female /></el-icon>
            <span class="info-label">用户性别</span>
          </div>
          <div class="info-value">
            <el-tag :type="state.user.sex === 0 ? 'primary' : 'danger'" effect="light" round>
              {{ state.user.sex === 0 ? '男生' : '女生' }}
            </el-tag>
          </div>
        </div>
      </div>

      <div class="action-box">
        <el-button type="primary" class="gradient-btn" @click="editUserInfo">
          <el-icon><Edit /></el-icon>修改基本资料
        </el-button>
        <el-button type="info" plain class="safe-btn" @click="editPassword">
          <el-icon><Lock /></el-icon>安全修改密码
        </el-button>
      </div>
    </el-card>

    <vxe-modal title="修改基本资料" v-model="userInfoOpen" width="460px" showFooter show-maximize resize remember>
      <div class="dialog-inner-form">
        <el-form ref="userRef" :model="form" :rules="rules" label-position="top">
          <el-form-item label="用户名" prop="userName">
            <el-input v-model="form.userName" placeholder="请输入新的用户名">
              <template #prefix><el-icon><User /></el-icon></template>
            </el-input>
          </el-form-item>
          <el-form-item label="手机号码" prop="phonenumber">
            <el-input v-model="form.phonenumber" placeholder="请输入绑定的11位手机号" maxlength="11">
              <template #prefix><el-icon><Cellphone /></el-icon></template>
            </el-input>
          </el-form-item>
          <el-form-item label="性别" prop="sex">
            <el-radio-group v-model="form.sex" class="gender-radio-group">
              <el-radio-button :value="0">
                <el-icon><Male /></el-icon> 男生
              </el-radio-button>
              <el-radio-button :value="1">
                <el-icon><Female /></el-icon> 女生
              </el-radio-button>
            </el-radio-group>
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="userInfoOpen = false">取 消</el-button>
          <el-button type="primary" @click="submitUserInfo">保 存</el-button>
        </div>
      </template>
    </vxe-modal>

    <vxe-modal title="安全修改密码" v-model="pwdOpen" width="460px" showFooter show-maximize resize remember>
      <div class="dialog-inner-form">
        <el-form ref="pwdRef" :model="pwdForm" :rules="pwdRules" label-position="top">
          <el-form-item label="身份验证手机号" prop="phonenumber">
            <el-input v-model="pwdForm.phonenumber" placeholder="请输入该账户对应的手机号码" maxlength="11">
              <template #prefix><el-icon><Cellphone /></el-icon></template>
            </el-input>
          </el-form-item>
          <el-form-item label="旧密码" prop="oldPassword">
            <el-input v-model="pwdForm.oldPassword" type="password" show-password placeholder="确认当前的旧密码">
              <template #prefix><el-icon><Unlock /></el-icon></template>
            </el-input>
          </el-form-item>
          <el-form-item label="新密码" prop="newPassword">
            <el-input v-model="pwdForm.newPassword" type="password" show-password placeholder="设置新的强登录密码">
              <template #prefix><el-icon><Lock /></el-icon></template>
            </el-input>
          </el-form-item>
          <el-form-item label="确认密码" prop="confirmPassword">
            <el-input v-model="pwdForm.confirmPassword" type="password" show-password placeholder="请再次输入新密码">
              <template #prefix><el-icon><CircleCheck /></el-icon></template>
            </el-input>
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="pwdOpen = false">取 消</el-button>
          <el-button type="primary" @click="submitPwd">提 交</el-button>
        </div>
      </template>
    </vxe-modal>
  </div>
</template>

<script setup>
import useUserStore from "@/stores/modules/userStore.js";
import { onMounted, reactive, ref, watch } from "vue";
import { getInfo } from "@/api/login.js";
import { getToken } from "@/utils/auth.js";
import { ElMessage } from "element-plus";
import { VxeModal } from 'vxe-pc-ui';
import { updateProfile, updatePwd } from "@/api/system/user.js";
//
import { User, Cellphone, Male, Female, Edit, Lock, Unlock, Camera, CircleCheck } from "@element-plus/icons-vue";

const pwdOpen = ref(false)
const pwdRef = ref()
const pwdForm = ref({
  phonenumber: '', //
  oldPassword: '',
  newPassword: '',
  confirmPassword: '',
})

// 自定义手机号前端校验规则
const checkPhone = (rule, value, callback) => {
  const phoneReg = /^1[3-9]\d{9}$/
  if (!value) {
    return callback(new Error('手机号码不能为空'))
  }
  if (!phoneReg.test(value)) {
    callback(new Error('请输入有效的11位手机号码'))
  } else {
    callback()
  }
}


const editPassword = () => {
  // 打开时清空历史输入
  pwdForm.value = { phonenumber: '', oldPassword: '', newPassword: '', confirmPassword: '' }
  pwdOpen.value = true
}

const pwdRules = ref({
  phonenumber: [{ required: true, validator: checkPhone, trigger: 'blur' }], // 💡 加强校验
  oldPassword: [{ required: true, message: '请输入旧密码', trigger: 'blur' }],
  newPassword: [{ required: true, message: '请输入新密码', trigger: 'blur' }],
  confirmPassword: [{ required: true, message: '请确认新密码', trigger: 'blur' }],
})

const maskPhone = (phone) => {
  if (!phone) return '';
  // 使用正则匹配前3位和后4位，中间替换为 ****
  return String(phone).replace(/(\d{3})\d{4}(\d{4})/, '$1****$2');
}

const submitPwd = () => {
  //  pwdForm.value.phonenumber 是用户输入框的完整值
  //  state.user.phonenumber 是接口返回的完整原始值
  if (pwdForm.value.phonenumber !== state.user.phonenumber) {
    ElMessage.error('输入的手机号码不正确，请填写账户绑定的完整手机号！')
    return
  }

  // 密码校验逻辑
  if (pwdForm.value.newPassword !== pwdForm.value.confirmPassword) {
    ElMessage.error('两次输入的密码不一致')
    return
  }

  pwdRef.value.validate(valid => {
    if (valid) {
      updatePwd(pwdForm.value).then(res => {
        ElMessage.success('密码修改成功')
        pwdOpen.value = false
      })
    }
  })
}

const userRef = ref()
const userInfoOpen = ref(false)
const form = ref({})

const rules = ref({
  userName: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  phonenumber: [{ required: true, validator: checkPhone, trigger: 'blur' }] // 💡 资料修改同样增加手机规范拦截
})

const submitUserInfo = () => {
  userRef.value.validate(valid => {
    if (valid) {
      updateProfile(form.value).then(res => {
        userInfoOpen.value = false
        ElMessage.success('基础资料更新成功')
        getUser()
        userStore.name = form.value.userName
      })
    }
  })
}

const uploadUrl = import.meta.env.VITE_APP_BASE_API + "/system/user/profile/avatar"
const headers = { Authorization: "Bearer " + getToken() }
const userStore = useUserStore()
const state = reactive({
  user: {}
})

const editUserInfo = () => {
  form.value = {
    userName: state.user.userName || userStore.name,
    phonenumber: state.user.phonenumber, // 💡 同步当前手机号到编辑框
    sex: state.user.sex
  }
  userInfoOpen.value = true
}

const beforeUpload = (file) => {
  const isJpg = file.type === 'image/jpeg' || file.type === 'image/png'
  if (!isJpg) {
    ElMessage.error('上传头像图片只能是 JPG 或者 PNG 格式!')
  }
  return isJpg
}

const handleAvatarSuccess = (res) => {
  if (res.code === 200) {
    userStore.avatar = import.meta.env.VITE_APP_BASE_API + res.imgUrl
    ElMessage.success('头像上传成功')
  } else {
    ElMessage.error(res.msg || '修改失败')
  }
}

const handleAvatarError = () => {
  ElMessage.error('服务交互异常，头像上传失败')
}

const getUser = () => {
  getInfo().then(res => {
    state.user = res.data
  })
}

onMounted(() => {
  getUser()
})

watch(() => state.user,
    user => {
      if (user) {
        form.value = { userName: user.userName, phonenumber: user.phonenumber, sex: user.sex }
      }
    },
    { immediate: true }
)
</script>

<style scoped>
/* 容器级美化 */
.profile-container {
  position: relative;
  min-height: calc(100vh - 120px);
  padding: 40px 20px;
  display: flex;
  justify-content: center;
  align-items: flex-start;
  background-color: #f6f8fa;
  overflow: hidden;
}

/* 霓虹背景发光微动效 */
.bg-glow {
  position: absolute;
  width: 350px;
  height: 350px;
  background: radial-gradient(circle, rgba(64, 158, 255, 0.15) 0%, rgba(255, 255, 255, 0) 70%);
  top: -50px;
  left: calc(50% - 175px);
  z-index: 0;
  pointer-events: none;
}

/* 主卡片现代化改造 */
.profile-card {
  position: relative;
  width: 100%;
  max-width: 460px;
  border-radius: 16px;
  border: 1px solid rgba(230, 235, 245, 0.8);
  background: rgba(255, 255, 255, 0.95);
  box-shadow: 0 12px 32px 4px rgba(64, 158, 255, 0.04), 0 8px 20px -8px rgba(0, 0, 0, 0.08) !important;
  z-index: 1;
  transition: transform 0.3s ease;
}

.profile-card:hover {
  transform: translateY(-2px);
}

/* 头像及首部区域 */
.profile-header {
  text-align: center;
  padding: 20px 0 10px 0;
}

.avatar-wrapper {
  position: relative;
  width: 124px;
  height: 124px;
  margin: 0 auto 16px auto;
  border-radius: 50%;
  padding: 4px;
  background: linear-gradient(135deg, #409eff, #a0cfff);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
}

.avatar-uploader {
  width: 116px;
  height: 116px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  border-radius: 50%;
  background: #fff;
}

.avatar-uploader .avatar {
  width: 116px;
  height: 116px;
  object-fit: cover;
  border-radius: 50%;
  transition: all 0.3s ease;
}

/* 头像悬浮遮罩 */
.avatar-mask {
  position: absolute;
  top: 0;
  left: 0;
  width: 116px;
  height: 116px;
  background: rgba(0, 0, 0, 0.5);
  border-radius: 50%;
  opacity: 0;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  color: #fff;
  font-size: 12px;
  z-index: 2;
  transition: opacity 0.3s ease;
}

.avatar-mask .el-icon {
  font-size: 20px;
  margin-bottom: 4px;
}

.avatar-uploader:hover .avatar-mask {
  opacity: 1;
}

.avatar-placeholder {
  width: 116px;
  height: 116px;
  background: #f0f2f5;
  display: flex;
  justify-content: center;
  align-items: center;
  color: #909399;
  font-size: 40px;
}

.welcome-title {
  margin: 0 0 6px 0;
  font-size: 20px;
  font-weight: 600;
  color: #2c3e50;
}

.welcome-sub {
  margin: 0;
  font-size: 13px;
  color: #909399;
}

/* 信息卡条条目化 */
.info-list {
  margin: 30px 0;
  border-top: 1px solid #f0f2f5;
}

.info-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 8px;
  border-bottom: 1px solid #f0f2f5;
  transition: background 0.2s;
}

.info-item:hover {
  background-color: #fafafa;
}

.info-left {
  display: flex;
  align-items: center;
}

.info-icon {
  font-size: 16px;
  margin-right: 12px;
  padding: 6px;
  border-radius: 8px;
}

.name-icon {
  color: #409eff;
  background-color: rgba(64, 158, 255, 0.1);
}

/* 💡 新增：手机图标背景色装饰 */
.phone-icon {
  color: #e6a23c;
  background-color: rgba(230, 162, 60, 0.1);
}

.sex-icon {
  color: #67c23a;
  background-color: rgba(103, 194, 58, 0.1);
}

.info-label {
  font-size: 14px;
  color: #606266;
  font-weight: 500;
}

.info-value {
  font-size: 14px;
  color: #303133;
  font-weight: 600;
}

/* 按钮及动作区 */
.action-box {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-top: 10px;
}

.action-box .el-button {
  width: 100%;
  height: 40px;
  margin-left: 0 !important;
  border-radius: 8px;
  font-weight: 500;
  font-size: 14px;
}

.gradient-btn {
  background: linear-gradient(135deg, #409eff 0%, #2081f5 100%) !important;
  border: none !important;
  box-shadow: 0 4px 12px rgba(32, 129, 245, 0.25);
}

.gradient-btn:hover {
  opacity: 0.9;
}

.safe-btn {
  border-color: #dcdfe6 !important;
}

/* 弹出层表单美化 */
.dialog-inner-form {
  padding: 10px 20px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  padding-right: 10px;
}

.gender-radio-group {
  width: 100%;
}

:deep(.gender-radio-group .el-radio-button) {
  width: 50%;
}

:deep(.gender-radio-group .el-radio-button__inner) {
  width: 100%;
  display: inline-flex;
  justify-content: center;
  align-items: center;
  gap: 6px;
  height: 38px;
}
</style>
