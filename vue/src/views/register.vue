<template>
  <div class="register-wrapper">
    <div class="glow-bubble bubble-1"></div>
    <div class="glow-bubble bubble-2"></div>

    <div class="register-card">
      <div class="card-inner">

        <div class="register-header">
          <h3 class="main-title">创建新账户</h3>
          <p class="sub-title">请填写下方信息以完成您的账户注册</p>
          <div class="title-line"></div>
        </div>

        <el-form :model="registerForm" ref="registerRef" :rules="rules" class="custom-form">

          <el-form-item prop="userName">
            <el-input
                v-model="registerForm.userName"
                size="large"
                placeholder="请设置您的用户名"
                clearable
            >
              <template #prefix>
                <el-icon class="input-icon">
                  <User/>
                </el-icon>
              </template>
            </el-input>
          </el-form-item>

          <el-form-item prop="phonenumber">
            <el-input
                v-model="registerForm.phonenumber"
                size="large"
                placeholder="请输入您的手机号码"
                clearable
                maxlength="11"
            >
              <template #prefix>
                <el-icon class="input-icon">
                  <Cellphone/>
                </el-icon>
              </template>
            </el-input>
          </el-form-item>

          <el-form-item prop="password">
            <el-input
                v-model="registerForm.password"
                type="password"
                show-password
                size="large"
                placeholder="请设置登录密码"
            >
              <template #prefix>
                <el-icon class="input-icon">
                  <Lock/>
                </el-icon>
              </template>
            </el-input>
          </el-form-item>

          <el-form-item prop="confirmPassword">
            <el-input
                v-model="registerForm.confirmPassword"
                type="password"
                show-password
                size="large"
                placeholder="请再次确认登录密码"
                @keyup.enter="handleRegister"
            >
              <template #prefix>
                <el-icon class="input-icon">
                  <CircleCheck/>
                </el-icon>
              </template>
            </el-input>
          </el-form-item>

          <el-form-item class="submit-item">
            <el-button
                type="primary"
                class="register-btn"
                :loading="loading"
                @click="handleRegister"
            >
              <span v-if="!loading">立 即 注 册</span>
              <span v-else>正在为您创建账户...</span>
            </el-button>
          </el-form-item>

          <div class="login-guide">
            <span>已有账户?</span>
            <router-link to="/login" class="login-link">
              去登录
              <el-icon class="arrow-icon">
                <ArrowRight/>
              </el-icon>
            </router-link>
          </div>

        </el-form>

      </div>
    </div>
  </div>
</template>

<script setup>
import {ref} from "vue";
import {register} from "@/api/register.js";
import {ElMessage} from "element-plus";
import {useRouter} from "vue-router";
// 💡 引入 Element Plus 的精美图标（追加了 Cellphone 手机图标）
import {User, Cellphone, Lock, CircleCheck, ArrowRight} from "@element-plus/icons-vue";

const loading = ref(false)
const registerRef = ref()

const registerForm = ref({
  userName: '',
  phonenumber: '', // 💡 新增：手机号参数
  password: '',
  confirmPassword: ''
})

// 验证手机号格式的自定义规则
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

// 验证两次密码输入是否相同
const equalToPassword = (rule, value, callback) => {
  if (registerForm.value.password !== value) {
    callback(new Error('两次输入的密码不一致，请仔细核对'))
  } else {
    callback()
  }
}

// 表单校验
const rules = ref({
  userName: [
    {required: true, message: '用户名不能为空，请给您的账号起个名字', trigger: 'blur'}
  ],
  // 💡 新增：手机号码校验规则
  phonenumber: [
    {required: true, validator: checkPhone, trigger: 'blur'}
  ],
  password: [
    {required: true, message: '请设置您的登录密码', trigger: 'blur'}
  ],
  confirmPassword: [
    {required: true, message: '请再次输入您的密码以做确认', trigger: 'blur'},
    {validator: equalToPassword, trigger: 'blur'}
  ],
})

const router = useRouter()

const handleRegister = () => {
  registerRef.value.validate(valid => {
    if (valid) {
      loading.value = true
      register(registerForm.value)
          .then(res => {
            // 假设你的后端返回 { code: 200, msg: "注册成功" }
            if (res.code === 200) {
              ElMessage.success('恭喜你，账号「' + registerForm.value.userName + '」注册成功！')
              router.push('/login')
            } else {
              // 关键点：如果后端返回 code 不是 200，显示具体错误原因
              ElMessage.error(res.msg || '注册失败，请稍后再试')
            }
          })
          .catch(err => {
            // 这里处理网络异常或后端抛出的异常
            console.error(err)
            // 如果后端抛出的是自定义异常，这里可以尝试读取 error 信息
            ElMessage.error(err.message || '系统繁忙，请稍后再试')
          })
          .finally(() => {
            loading.value = false
          })
    }
  })
}


</script>

<style scoped>
/* 容器背景层 */
.register-wrapper {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100vh;
  width: 100vw;
  /* 基础壁纸 + 叠加一层柔和暗化渐变，凸显卡片 */
  background: linear-gradient(135deg, rgba(20, 30, 48, 0.4), rgba(36, 59, 85, 0.6)),
  url("@/assets/images/background.jpg") no-repeat center;
  background-size: cover;
  overflow: hidden;
}

/* --- 灵动微动效：背景彩色发光气泡（与登录保持同步） --- */
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

/* --- 核心：高级毛玻璃注册卡片 --- */
.register-card {
  position: relative;
  width: 100%;
  max-width: 440px;
  padding: 3px;
  border-radius: 24px;
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.25), rgba(255, 255, 255, 0.05));
  backdrop-filter: blur(20px); /* 核心毛玻璃 */
  box-shadow: 0 20px 50px rgba(0, 0, 0, 0.15),
  inset 0 0 0 1px rgba(255, 255, 255, 0.2);
  z-index: 10;
  transition: all 0.4s cubic-bezier(0.165, 0.84, 0.44, 1);
}

.register-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 30px 60px rgba(0, 0, 0, 0.25),
  inset 0 0 0 1px rgba(255, 255, 255, 0.35);
}

.card-inner {
  padding: 40px 40px;
  background: rgba(255, 255, 255, 0.55); /* 提升可读性的微白遮罩 */
  border-radius: 21px;
}

/* 头部设计 */
.register-header {
  text-align: center;
  margin-bottom: 30px;
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
  margin-bottom: 20px;
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

/* 按钮级美化：流光科技感渐变 */
.submit-item {
  margin-top: 25px;
}

.register-btn {
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

.register-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 6px 20px rgba(37, 117, 252, 0.5) !important;
  opacity: 0.95;
}

.register-btn:active {
  transform: translateY(1px);
}

/* 登录引导美化 */
.login-guide {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  color: #64748b;
  font-size: 13px;
  margin-top: 25px;
}

.login-link {
  color: #409eff;
  display: inline-flex;
  align-items: center;
  text-decoration: none;
  font-weight: 500;
  transition: all 0.2s;
}

.login-link .arrow-icon {
  font-size: 11px;
  margin-left: 2px;
  transition: transform 0.2s;
}

.login-link:hover {
  color: #2575fc;
}

.login-link:hover .arrow-icon {
  transform: translateX(3px);
}
</style>
