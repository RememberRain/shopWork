<template>
  <div class="login-wrapper">
    <div class="glow-bubble bubble-1"></div>
    <div class="glow-bubble bubble-2"></div>

    <div class="login-card">
      <div class="card-inner">

        <div class="login-header">
          <h3 class="main-title">尾货在线销售与推荐系统</h3>
          <p class="sub-title">请在下方输入您的账户信息以开启旅程</p>
          <div class="title-line"></div>
        </div>

        <el-form :model="loginForm" ref="loginRef" :rules="rules" class="custom-form">

          <el-form-item prop="userName">
            <el-input
                v-model="loginForm.userName"
                size="large"
                placeholder="请输入用户名"
                clearable
            >
              <template #prefix>
                <el-icon class="input-icon">
                  <User/>
                </el-icon>
              </template>
            </el-input>
          </el-form-item>

          <el-form-item prop="password">
            <el-input
                v-model="loginForm.password"
                type="password"
                show-password
                size="large"
                placeholder="请输入密码"
                @keyup.enter="handleLogin"
            >
              <template #prefix>
                <el-icon class="input-icon">
                  <Lock/>
                </el-icon>
              </template>
            </el-input>
          </el-form-item>

          <div class="form-options">
            <el-checkbox v-model="rememberMe" label="记住我" size="small"/>
            <span class="forget-pwd" @click="handleForgetPwd">忘记密码？</span>
          </div>

          <el-form-item class="submit-item">
            <el-button
                type="primary"
                class="login-btn"
                :loading="loading"
                @click="handleLogin"
            >
              <span v-if="!loading">立 即 登 录</span>
              <span v-else>安全登录中...</span>
            </el-button>
          </el-form-item>

          <div class="register-guide">
            <span>还没有账户?</span>
            <router-link to="/register" class="reg-link">
              立即注册
              <el-icon class="arrow-icon">
                <ArrowRight/>
              </el-icon>
            </router-link>
          </div>

        </el-form>

      </div>
    </div>

    <el-dialog
        title="找回密码 / 凭证核实"
        v-model="forgetOpen"
        width="440px"
        append-to-body
        class="custom-dialog"
    >
      <div class="dialog-desc">为了您的账户安全，请核实绑定的用户名与手机号。</div>
      <el-form :model="forgetForm" ref="forgetRef" :rules="forgetRules" label-position="top">
        <el-form-item label="用户名" prop="userName">
          <el-input v-model="forgetForm.userName" placeholder="请输入您的账户用户名" prefix-icon="User"/>
        </el-form-item>
        <el-form-item label="绑定手机号" prop="phonenumber">
          <el-input v-model="forgetForm.phonenumber" placeholder="请输入该账户绑定的11位手机号" prefix-icon="Cellphone"
                    maxlength="11"/>
        </el-form-item>
        <el-form-item label="设置新密码" prop="newPassword">
          <el-input v-model="forgetForm.newPassword" type="password" show-password placeholder="请设置您的新密码"
                    prefix-icon="Lock"/>
        </el-form-item>
        <el-form-item label="确认新密码" prop="confirmPassword">
          <el-input v-model="forgetForm.confirmPassword" type="password" show-password placeholder="请再次输入新密码"
                    prefix-icon="Lock"/>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="forgetOpen = false">取 消</el-button>
          <el-button type="primary" :loading="forgetLoading" class="submit-gradient" @click="submitForgetPwd">
            重置并保存
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import {ref} from "vue";
import useUserStore from "@/stores/modules/userStore.js";
import {useRouter} from "vue-router";
import {ElMessage} from "element-plus";
import {resetPwdByPhone} from "@/api/system/user.js";
import {User, Lock, ArrowRight, Cellphone} from "@element-plus/icons-vue";

const loading = ref(false)
const loginRef = ref()
const rememberMe = ref(false)

const loginForm = ref({
  userName: '',
  password: ''
})

const rules = ref({
  userName: [{required: true, message: '请输入用户名', trigger: 'blur'}],
  password: [{required: true, message: '为了您的账户安全，请输入密码', trigger: 'blur'}],
})

const userStore = useUserStore()
const router = useRouter()

const handleLogin = () => {
  loginRef.value.validate(valid => {
    if (valid) {
      loading.value = true
      userStore.login(loginForm.value).then(res => {
        router.push('/')
      }).catch(() => {
        loading.value = false
      })
    }
  })
}



const forgetOpen = ref(false)
const forgetLoading = ref(false)
const forgetRef = ref()

const forgetForm = ref({
  userName: '',
  phonenumber: '',
  newPassword: '',
  confirmPassword: ''
})

// 自定义校验：二次密码一致性检测
const equalToPassword = (rule, value, callback) => {
  if (value !== forgetForm.value.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const forgetRules = ref({
  userName: [{required: true, message: '请输入用户名', trigger: 'blur'}],
  phonenumber: [
    {required: true, message: '请输入手机号码', trigger: 'blur'},
    {pattern: /^1[3-9]\d{9}$/, message: '请输入有效的11位手机号码', trigger: 'blur'}
  ],
  newPassword: [
    {required: true, message: '新密码不能为空', trigger: 'blur'},
    {min: 6, max: 20, message: '密码长度需在 6 到 20 个字符之间', trigger: 'blur'}
  ],
  confirmPassword: [
    {required: true, message: '请再次输入新密码', trigger: 'blur'},
    {validator: equalToPassword, trigger: 'blur'}
  ]
})

// 打开忘记密码弹窗并重置表单
const handleForgetPwd = () => {
  forgetOpen.value = true
  if (forgetRef.value) {
    forgetRef.value.resetFields()
  }
  forgetForm.value = {
    userName: '',
    phonenumber: '',
    newPassword: '',
    confirmPassword: ''
  }
}

// 提交凭证并核实保存密码
const submitForgetPwd = () => {
  forgetRef.value.validate(valid => {
    if (valid) {
      forgetLoading.value = true
      // 发送核实并修改请求
      resetPwdByPhone(forgetForm.value).then(res => {
        ElMessage.success('身份核实成功，新密码已保存，请使用新密码登录')
        forgetOpen.value = false
        forgetLoading.value = false
        // 顺便把用户名同步到登录框，方便用户直接登录
        loginForm.value.userName = forgetForm.value.userName
      }).catch(() => {
        forgetLoading.value = false
      })
    }
  })
}
</script>


<style scoped>
/* 容器背景层 */
.login-wrapper {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100vh;
  width: 100vw;
  /* 基础壁纸 + 叠加一层柔和暗化渐变，让毛玻璃卡片更突出 */
  background: linear-gradient(135deg, rgba(20, 30, 48, 0.4), rgba(36, 59, 85, 0.6)),
  url("@/assets/images/background.jpg") no-repeat center;
  background-size: cover;
  overflow: hidden;
}

/* --- 灵动微动效：背景彩色发光气泡 --- */
.glow-bubble {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
  z-index: 0;
  pointer-events: none;
  opacity: 0.45;
}

.bubble-1 {
  width: 400px;
  height: 400px;
  background: radial-gradient(circle, #409eff 0%, rgba(160, 207, 255, 0) 70%);
  top: -100px;
  left: calc(50% - 400px);
  animation: floatAround 12s infinite alternate ease-in-out;
}

.bubble-2 {
  width: 500px;
  height: 500px;
  background: radial-gradient(circle, #67c23a 0%, rgba(255, 255, 255, 0) 70%);
  bottom: -150px;
  right: calc(50% - 450px);
  animation: floatAround 16s infinite alternate-reverse ease-in-out;
}

@keyframes floatAround {
  0% {
    transform: translateY(0) scale(1);
  }
  100% {
    transform: translateY(40px) translateX(30px) scale(1.1);
  }
}

/* --- 核心：高级毛玻璃登录卡片 --- */
.login-card {
  position: relative;
  width: 100%;
  max-width: 440px;
  padding: 3px; /* 边缘极光彩条 */
  border-radius: 24px;
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.25), rgba(255, 255, 255, 0.05));
  backdrop-filter: blur(20px); /* 核心毛玻璃 */
  box-shadow: 0 20px 50px rgba(0, 0, 0, 0.15),
  inset 0 0 0 1px rgba(255, 255, 255, 0.2);
  z-index: 10;
  transition: all 0.4s cubic-bezier(0.165, 0.84, 0.44, 1);
}

.login-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 30px 60px rgba(0, 0, 0, 0.25),
  inset 0 0 0 1px rgba(255, 255, 255, 0.35);
}

.card-inner {
  padding: 45px 40px;
  background: rgba(255, 255, 255, 0.55); /* 表单背景微白色遮罩，确保文字清晰好读 */
  border-radius: 21px;
}

/* 头部设计 */
.login-header {
  text-align: center;
  margin-bottom: 35px;
}

.main-title {
  font-size: 28px;
  font-weight: 700;
  color: #1e293b;
  margin: 0 0 8px 0;
  letter-spacing: 2px;
}

.sub-title {
  color: #64748b;
  font-size: 13px;
  margin: 0;
}

.title-line {
  width: 40px;
  height: 4px;
  background: linear-gradient(90deg, #409eff, #a0cfff);
  margin: 14px auto 0 auto;
  border-radius: 2px;
}

/* 表单与输入框重塑 */
.custom-form :deep(.el-form-item) {
  margin-bottom: 22px;
}

/* 自定义输入框内部样式 */
.custom-form :deep(.el-input__wrapper) {
  background-color: rgba(255, 255, 255, 0.8) !important;
  border-radius: 12px !important;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.02) !important;
  border: 1px solid rgba(0, 0, 0, 0.06);
  padding: 4px 15px;
  transition: all 0.3s ease;
}

.custom-form :deep(.el-input__wrapper.is-focus) {
  background-color: #fff !important;
  border-color: #409eff !important;
  box-shadow: 0 4px 14px rgba(64, 158, 255, 0.2) !important;
}

.custom-form :deep(.el-input__wrapper .input-icon) {
  font-size: 17px;
  color: #94a3b8;
  transition: color 0.3s;
}

.custom-form :deep(.el-input__wrapper.is-focus .input-icon) {
  color: #409eff;
}

/* 表单配置区：记住我与忘记密码 */
.form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: -8px;
  margin-bottom: 24px;
  padding: 0 2px;
}

.forget-pwd {
  font-size: 13px;
  color: #64748b;
  cursor: pointer;
  transition: color 0.2s;
}

.forget-pwd:hover {
  color: #409eff;
}

/* 按钮级美化：流光科技感渐变 */
.submit-item {
  margin-top: 10px;
}

.login-btn {
  width: 100%;
  height: 46px !important;
  border-radius: 12px !important;
  font-size: 15px !important;
  font-weight: 600 !important;
  letter-spacing: 4px;
  border: none !important;
  background: linear-gradient(135deg, #409eff 0%, #2575fc 100%) !important;
  box-shadow: 0 4px 15px rgba(37, 117, 252, 0.35) !important;
  transition: all 0.3s ease !important;
}

.login-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 6px 20px rgba(37, 117, 252, 0.5) !important;
  opacity: 0.95;
}

.login-btn:active {
  transform: translateY(1px);
}

/* 注册引导美化 */
.register-guide {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  color: #64748b;
  font-size: 13px;
  margin-top: 25px;
}

.reg-link {
  color: #409eff;
  display: inline-flex;
  align-items: center;
  text-decoration: none;
  font-weight: 500;
  transition: all 0.2s;
}

.reg-link .arrow-icon {
  font-size: 11px;
  margin-left: 2px;
  transition: transform 0.2s;
}

.reg-link:hover {
  color: #2575fc;
}

.reg-link:hover .arrow-icon {
  transform: translateX(3px);
}
</style>
