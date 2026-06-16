<template>
  <div class="auth-page-container">

    <!-- 1. 未提交状态 / 进行中的多步表单 -->
    <el-card class="auth-card" shadow="never" v-if="authStatus === '未提交'">
      <!-- 步骤条：优化间距与字阶 -->
      <el-steps :active="currentStep" finish-status="success" align-center class="auth-steps">
        <el-step title="企业基本信息" />
        <el-step title="核心证件上传" />
        <el-step title="提交核对审核" />
      </el-steps>

      <div class="step-stage-wrapper">
        <!-- 第一步: 基本信息 -->
        <div v-if="currentStep === 0" class="step-content">
          <el-form ref="formRef"
                   :model="form"
                   :rules="rules"
                   label-position="top"
                   class="custom-classic-form">
            <el-row :gutter="24">
              <el-col :span="12">
                <el-form-item label="真实姓名" prop="realName">
                  <el-input v-model="form.realName" placeholder="请输入商家真实姓名" size="large" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="身份证号" prop="idCard">
                  <el-input v-model="form.idCard" placeholder="请输入18位居民身份证号" size="large" />
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="24">
              <el-col :span="12">
                <el-form-item label="联系电话" prop="phone">
                  <el-input v-model="form.phone" placeholder="请输入主要联系电话" size="large" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="电子邮箱" prop="email">
                  <el-input v-model="form.email" placeholder="请输入可接收审核结果的邮箱" size="large" />
                </el-form-item>
              </el-col>
            </el-row>
          </el-form>
        </div>

        <!-- 第二步: 证件上传 -->
        <div v-if="currentStep === 1" class="step-content">
          <el-form ref="formRef"
                   :model="form"
                   :rules="rules"
                   label-position="top"
                   class="custom-classic-form">
            <el-row :gutter="24" class="upload-grid-row">
              <el-col :xs="24" :sm="12" :md="8">
                <el-form-item label="身份证正面 (国徽面)" prop="idCardFront">
                  <div class="uploader-box-wrapper">
                    <image-upload v-model="form.idCardFront"/>
                  </div>
                </el-form-item>
              </el-col>
              <el-col :xs="24" :sm="12" :md="8">
                <el-form-item label="身份证反面 (人像面)" prop="idCardBack">
                  <div class="uploader-box-wrapper">
                    <image-upload v-model="form.idCardBack"/>
                  </div>
                </el-form-item>
              </el-col>
              <el-col :xs="24" :sm="12" :md="8">
                <el-form-item label="营业执照电子版" prop="license">
                  <div class="uploader-box-wrapper">
                    <image-upload v-model="form.license"/>
                  </div>
                </el-form-item>
              </el-col>
            </el-row>
          </el-form>
        </div>

        <!-- 第三步: 提交审核 (预览核对区升级) -->
        <div v-if="currentStep === 2" class="step-content">
          <div class="review-dashboard">
            <div class="review-notice">
              <el-icon class="notice-icon"><InfoFilled/></el-icon>
              <span>请仔细核对以下信息，提交后信息将被锁定直至审核流程结束。</span>
            </div>

            <!-- 数据明细网格 -->
            <div class="review-block">
              <h3 class="review-block-title">基本信息明细</h3>
              <div class="info-data-grid">
                <div class="grid-item"><span class="grid-label">真实姓名</span><span class="grid-value">{{ form.realName || '--' }}</span></div>
                <div class="grid-item"><span class="grid-label">身份证号</span><span class="grid-value font-mono">{{ form.idCard || '--' }}</span></div>
                <div class="grid-item"><span class="grid-label">联系电话</span><span class="grid-value font-mono">{{ form.phone || '--' }}</span></div>
                <div class="grid-item"><span class="grid-label">邮箱地址</span><span class="grid-value">{{ form.email || '--' }}</span></div>
              </div>
            </div>

            <!-- 证件缩略图墙 -->
            <div class="review-block">
              <h3 class="review-block-title">已上传证件凭证</h3>
              <el-row :gutter="20" class="image-preview-row">
                <el-col :xs="24" :sm="8">
                  <div class="preview-img-card">
                    <img :src="baseUrl + form.idCardFront" alt="身份证正面" class="actual-preview-img">
                    <div class="img-title-tag">身份证正面</div>
                  </div>
                </el-col>
                <el-col :xs="24" :sm="8">
                  <div class="preview-img-card">
                    <img :src="baseUrl + form.idCardBack" alt="身份证反面" class="actual-preview-img">
                    <div class="img-title-tag">身份证反面</div>
                  </div>
                </el-col>
                <el-col :xs="24" :sm="8">
                  <div class="preview-img-card">
                    <img :src="baseUrl + form.license" alt="营业执照" class="actual-preview-img">
                    <div class="img-title-tag">营业执照</div>
                  </div>
                </el-col>
              </el-row>
            </div>
          </div>
        </div>
      </div>

      <!-- 操作动作栏 -->
      <div class="form-actions-bar">
        <el-button
            v-if="currentStep > 0"
            @click="prevStep"
            :disabled="submitting"
            size="large"
            class="action-btn-secondary"
        >
          返回上一步
        </el-button>

        <el-button
            v-if="currentStep < 2"
            type="primary"
            @click="nextStep"
            :disabled="submitting"
            size="large"
            class="action-btn-primary"
        >
          下一步
        </el-button>

        <el-button
            v-if="currentStep === 2"
            type="success"
            @click="submitAuth"
            :loading="submitting"
            :disabled="submitting"
            size="large"
            class="action-btn-submit"
        >
          确认无误，提交审核
        </el-button>
      </div>
    </el-card>

    <!-- 2. 待审核状态面板 -->
    <el-card class="auth-card status-result-card" shadow="never" v-if="authStatus === '待审核'">
      <div class="result-state-content">
        <div class="state-icon-wrapper p-bg">
          <el-icon class="state-main-icon color-pending"><Clock/></el-icon>
        </div>
        <h2 class="state-title">认证申请正在全力审核中</h2>
        <p class="state-description">
          运营团队将在 1~3 个工作日内完成对您提交的资质、身份证信息的真实性核验，审核结果将通过邮件及系统通知第一时间告知您。
        </p>
        <div class="state-timeline-hint">
          <span>预计最晚反馈时间：系统收录后 48 小时内</span>
        </div>
      </div>
    </el-card>

    <!-- 3. 认证未通过状态面板 -->
    <el-card class="auth-card status-result-card" shadow="never" v-if="authStatus === '认证未通过'">
      <div class="result-state-content">
        <div class="state-icon-wrapper r-bg">
          <el-icon class="state-main-icon color-reject"><CircleClose/></el-icon>
        </div>
        <h2 class="state-title text-danger">很抱歉，您的商家认证未通过</h2>
        <p class="state-description">
          经系统及人工核查，您提交的资质文件或身份证照片可能存在以下问题：凭证边框不完整、字迹模糊、或执照经营状态异常。
        </p>
        <div class="state-action-area">
          <el-button type="primary" size="large" @click="resubmitAuth" class="resubmit-btn">
            重新修正并提交认证
          </el-button>
        </div>
      </div>
    </el-card>

  </div>
</template>



<script setup>
import {onMounted, ref} from "vue";
import {addMerchant, deleteMerchantByUserId, selectUserAuthStatus} from "@/api/homestay/merchant.js";
import ImageUpload from "@/components/ImageUpload/index.vue";
import {ElMessage} from "element-plus";
import {CircleClose, Loading} from "@element-plus/icons-vue";

//基础api地址
const baseUrl = import.meta.env.VITE_APP_BASE_API

//是否能够点击
const submitting = ref(false)

//表单校验
const rules = ref({
  realName: [
    { required: true, message: "真实姓名不能为空", trigger: "blur" }
  ],
  idCard: [
    { required: true, message: "身份证号不能为空", trigger: "blur" }
  ],
  phone: [
    { required: true, message: "联系电话不能为空", trigger: "blur" }
  ],
  email: [
    { required: true, message: "邮箱不能为空", trigger: "blur" }
  ],
  idCardFront: [
    { required: true, message: "身份证正面不能为空", trigger: "blur" }
  ],
  idCardBack: [
    { required: true, message: "身份证反面不能为空", trigger: "blur" }
  ],
  license: [
    { required: true, message: "营业执照不能为空", trigger: "blur" }
  ],
})

//表单参数
const form = ref({
  realName: null,
  idCard: null,
  phone: null,
  email: null,
  idCardFront: null,
  idCardBack: null,
  license: null,
})

//表单实例
const formRef = ref()

//当前的步骤
const currentStep = ref(0)

//认证状态
const authStatus = ref('')

//下一步按钮
const nextStep = () => {
  if (currentStep.value === 0) {
    currentStep.value++
  } else if (currentStep.value === 1) {
    //验证证件上传
    if (!form.value.idCardFront) {
      ElMessage.error('请上传身份证正面图片')
      return;
    }
    if (!form.value.idCardBack) {
      ElMessage.error('请上传身份证反面图片')
      return;
    }
    currentStep.value++
  }
}

//上一步按钮
const prevStep = () => {
  if (currentStep.value > 0) {
    currentStep.value--
  }
}

//查询认证审核状态
const getAuthStatus = () => {
  //查询当前用户的审核状态, 如果没有提交认证, 则为未提交
  selectUserAuthStatus().then(res => {
    authStatus.value = res.msg
  })
}

//提交认证
const submitAuth = () => {
  addMerchant(form.value).then(res => {
    submitting.value = true
    ElMessage.success('商家认证审核已提交, 我们将在2个工作日内通过电话联系您!')
    getAuthStatus()
  })
}

//重新提交认证
const resubmitAuth = () => {
  //先删除之前提交的信息
  deleteMerchantByUserId().then(res => {
    form.value = {
      realName: null,
      idCard: null,
      phone: null,
      email: null,
      idCardFront: null,
      idCardBack: null,
      license: null,
    }
    getAuthStatus()
    ElMessage.info('请重新填写认证信息')
  })

}

onMounted(() => {
  getAuthStatus()
})

</script>


<style scoped>
/* 全局页面底色与排版抗锯齿 */
.auth-page-container {
  max-width: 960px; /* 紧凑型中台黄金宽度 */
  margin: 40px auto;
  padding: 0 20px;
  box-sizing: border-box;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
}

/* 核心通用大卡片 */
.auth-card {
  border: 1px solid #e2e8f0;
  border-radius: 16px;
  background: #ffffff;
  box-shadow: 0 10px 25px rgba(148, 163, 184, 0.05) !important;
  padding: 12px;
}

/* ================= 步骤条与布局容器 ================= */
.auth-steps {
  margin: 20px 0 40px 0;
}
/* 深度美化 Element 内部步骤条线粗与圆圈 */
:deep(.el-step__title) {
  font-size: 14px;
  font-weight: 500;
}
:deep(.el-step__head.is-success) {
  color: #10b981;
  border-color: #10b981;
}

.step-stage-wrapper {
  background-color: #ffffff;
  min-height: 260px;
  padding: 0 16px;
}

/* 表单上置标签美化 */
.custom-classic-form :deep(.el-form-item__label) {
  font-size: 14px;
  font-weight: 600;
  color: #334155;
  margin-bottom: 6px;
}
.custom-classic-form :deep(.el-input__wrapper) {
  border-radius: 8px;
  box-shadow: 0 0 0 1px #cbd5e1 inset;
}
.custom-classic-form :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px #3b82f6 inset, 0 0 0 3px rgba(59, 130, 246, 0.15) !important;
}

/* 证件上传网格限制 */
.upload-grid-row {
  row-gap: 20px;
}
.uploader-box-wrapper {
  background-color: #f8fafc;
  border: 1px dashed #cbd5e1;
  border-radius: 12px;
  padding: 16px;
  display: flex;
  justify-content: center;
  align-items: center;
  transition: border-color 0.2s;
}
.uploader-box-wrapper:hover {
  border-color: #3b82f6;
}

/* ================= 第三步：高级核对确认面板 ================= */
.review-dashboard {
  display: flex;
  flex-direction: column;
  gap: 28px;
}
.review-notice {
  display: flex;
  align-items: center;
  gap: 8px;
  background-color: #f0fdf4;
  border: 1px solid #bbf7d0;
  color: #166534;
  padding: 12px 16px;
  border-radius: 8px;
  font-size: 13px;
}
.notice-icon {
  font-size: 16px;
  color: #22c55e;
}
.review-block {
  display: flex;
  flex-direction: column;
  gap: 14px;
}
.review-block-title {
  font-size: 15px;
  font-weight: 700;
  color: #1e293b;
  margin: 0;
  padding-left: 8px;
  border-left: 3px solid #3b82f6;
}
/* 确认单双列网格 */
.info-data-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 1px;
  background-color: #e2e8f0;
  border-radius: 8px;
  overflow: hidden;
  border: 1px solid #e2e8f0;
}
.grid-item {
  display: flex;
  background-color: #ffffff;
  font-size: 14px;
  height: 44px;
  align-items: center;
}
.grid-label {
  width: 120px;
  background-color: #f8fafc;
  color: #64748b;
  font-weight: 500;
  height: 100%;
  display: flex;
  align-items: center;
  padding-left: 16px;
  border-right: 1px solid #e2e8f0;
  flex-shrink: 0;
}
.grid-value {
  color: #334155;
  padding-left: 16px;
  font-weight: 500;
}
.font-mono {
  font-family: Monaco, Consolas, monospace;
}

/* 凭证缩略图控制 */
.image-preview-row {
  row-gap: 16px;
}
.preview-img-card {
  position: relative;
  border-radius: 10px;
  overflow: hidden;
  height: 160px;
  border: 1px solid #e2e8f0;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.02);
  background-color: #f1f5f9;
}
.actual-preview-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.img-title-tag {
  position: absolute;
  bottom: 0; left: 0; width: 100%;
  background: rgba(15, 23, 42, 0.65);
  backdrop-filter: blur(4px);
  color: #ffffff;
  font-size: 12px;
  text-align: center;
  padding: 6px 0;
  font-weight: 500;
}

/* ================= 底部动作按钮栏 ================= */
.form-actions-bar {
  margin-top: 40px;
  padding-top: 20px;
  border-top: 1px solid #f1f5f9;
  display: flex;
  justify-content: center;
  gap: 12px;
}
.action-btn-secondary {
  border-radius: 8px;
  padding: 10px 24px;
  color: #475569;
}
.action-btn-primary {
  border-radius: 8px;
  padding: 10px 28px;
  background-color: #3b82f6;
  border: none;
}
.action-btn-submit {
  border-radius: 8px;
  padding: 10px 28px;
  background-color: #10b981;
  border: none;
}

/* ================= 状态结果页整体样式重塑 ================= */
.status-result-card {
  padding: 48px 24px;
}
.result-state-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  max-width: 520px;
  margin: 0 auto;
}
/* 状态大圆圈容器 */
.state-icon-wrapper {
  width: 72px;
  height: 72px;
  border-radius: 50%;
  display: flex;
  justify-content: center;
  align-items: center;
  margin-bottom: 24px;
}
.p-bg { background-color: #eff6ff; border: 1px solid #dbeafe; }
.r-bg { background-color: #fef2f2; border: 1px solid #fee2e2; }

.state-main-icon {
  font-size: 36px;
}
.color-pending { color: #2563eb; }
.color-reject { color: #dc2626; }

.state-title {
  font-size: 22px;
  font-weight: 700;
  color: #0f172a;
  margin: 0 0 12px 0;
}
.text-danger {
  color: #e11d48;
}
.state-description {
  font-size: 14px;
  color: #64748b;
  line-height: 1.6;
  margin: 0 0 24px 0;
}
.state-timeline-hint {
  font-size: 13px;
  color: #94a3b8;
  background-color: #f8fafc;
  padding: 6px 16px;
  border-radius: 20px;
  border: 1px solid #f1f5f9;
}
.state-action-area {
  margin-top: 8px;
}
.resubmit-btn {
  border-radius: 8px;
  padding: 12px 28px;
  font-weight: 600;
}
</style>

