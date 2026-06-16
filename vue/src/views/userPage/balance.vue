<template>
  <div class="recharge-container">
    <div class="main-content">

      <!-- 1. 账户余额卡片（升级为轻奢渐变金融卡片质感） -->
      <div class="balance-card">
        <div class="balance-info">
          <span class="label">当前账户可用余额</span>
          <div class="amount-row">
            <span class="symbol">¥</span>
            <span class="num">{{ balance.toFixed(2) }}</span>
          </div>
        </div>
        <div class="card-decoration">
          <!-- 悬浮微光背景图标 -->
          <el-icon class="bg-wallet-icon"><Wallet/></el-icon>
        </div>
      </div>

      <!-- 2. 核心操作面板（独立白底圆角卡片） -->
      <div class="action-panel">

        <!-- 充值金额网格 -->
        <div class="section-title">充值金额</div>
        <div class="amount-grid">
          <div v-for="amount in selectAmount"
               :key="amount"
               class="amount-item"
               :class="{ active: selectedAmount === amount }"
               @click="selectPreset(amount)"
          >
            <span class="val">{{ amount }}<span class="unit">元</span></span>
            <!-- 选中时的精致小角标 -->
            <div class="corner-check" v-if="selectedAmount === amount">
              <el-icon><Check /></el-icon>
            </div>
          </div>

          <!-- 自定义金额输入框 -->
          <div class="amount-item custom-input" :class="{ active: isCustomAmount }">
            <el-input-number v-model="customVal"
                             :min="1"
                             :controls="false"
                             placeholder="自定义金额"
                             @focus="handleCustomFocus"
                             class="input-box"
            >
              <template #suffix>
                <span class="suffix-text">元</span>
              </template>
            </el-input-number>
          </div>
        </div>

        <!-- 支付方式区域 -->
        <div class="section-title mt-30">支付方式</div>
        <div class="payment-methods">
          <div v-for="method in paymentMethodsList"
               :key="method"
               class="pay-item"
               :class="{ active: paymentMethod === method }"
               @click="paymentMethod = method"
          >
            <div class="icon-box" :class="method.value">
              <svg-icon icon-class="微信" v-if="method.value === 'wechat'" class="pay-svg-icon"/>
              <svg-icon icon-class="支付宝" v-if="method.value === 'alipay'" class="pay-svg-icon"/>
              <svg-icon icon-class="银行卡" v-if="method.value === 'card'" class="pay-svg-icon"/>
            </div>

            <div class="pay-info">
              <div class="name">{{ method.name }}</div>
              <div class="desc">{{ method.description }}</div>
            </div>

            <!-- 原生的选择圆圈，升级为高阶勾选联动 -->
            <div class="radio-check-box">
              <div class="inner-circle" :class="{ checked: paymentMethod === method }">
                <el-icon v-if="paymentMethod === method"><Select/></el-icon>
              </div>
            </div>
          </div>
        </div>

        <!-- 3. 底部结算区域（强化对比与操作暗示） -->
        <div class="submit-area">
          <div class="total-text">
            实付金额：<span class="currency-symbol">¥</span><span class="price">{{ finalPayAmount }}</span>
          </div>
          <el-button type="primary"
                     size="large"
                     class="pay-btn"
                     @click="handleRecharge">
            确认订单并立即支付
          </el-button>
        </div>

      </div>

    </div>
  </div>
</template>


<script setup>

import {computed, onMounted, ref} from "vue";
import {recharge, selectMyBalance} from "@/api/system/user.js";
import {Select, Wallet} from "@element-plus/icons-vue";
import SvgIcon from "@/components/SvgIcon/index.vue";

//最后支付金额
const finalPayAmount = computed(() => {
  if (isCustomAmount.value){
    return customVal.value ? Number(customVal.value).toFixed(2) : '0.00'
  }
  return selectedAmount.value.toFixed(2)
})

//支付方式
const paymentMethodsList = ref([
  {
    value: 'wechat',
    name: '微信支付',
    description: '推荐使用，更方便'
  },
  {
    value: 'alipay',
    name: '支付宝支付',
    description: '数亿用户的选择'
  },
  {
    value: 'card',
    name: '银行卡支付',
    description: '支持储蓄卡/信用卡'
  },
])

//当前支付方式（默认微信支付）
const paymentMethod = ref('wechat')

//默认金额选择
const selectAmount = [100, 300, 500, 1000, 3000]

//当前选中的预设金额
const selectedAmount = ref(100)

//是否使用自定义金额
const isCustomAmount = ref(false)

//选择预设金额
const selectPreset = (val) => {
  isCustomAmount.value = false
  selectedAmount.value = val
  customVal.value = null
}

//自定义金额
const customVal = ref(0)

//账户余额
const balance = ref(0)

//自定义金额输入
const handleCustomFocus = () => {
  isCustomAmount.value = true
  selectedAmount.value = null
}

//充值按钮
const handleRecharge = () => {

  //
  recharge(finalPayAmount.value).then(res => {
    getMyBalance()
  })
}

//查询账户余额
const getMyBalance = () => {
  selectMyBalance().then(res => {
    balance.value = res.data
  })
}

onMounted(() => {
 getMyBalance()
})
</script>

<style scoped>
/* 全局页面容器底色 */
.recharge-container {
  padding: 24px;
  background-color: #f7f9fc; /* 现代低饱和度浅蓝灰，比死灰更好看 */
  min-height: calc(100vh - 40px);
  display: flex;
  justify-content: center;
}

.main-content {
  width: 100%;
  max-width: 800px; /* 限制收银台最大宽度，防止大屏平铺穿帮 */
  display: flex;
  flex-direction: column;
  gap: 20px;
}

/* ================= 账户余额卡片（极光微光质感） ================= */
.balance-card {
  position: relative;
  background: linear-gradient(135deg, #2f54eb 0%, #1890ff 100%); /* 渐变科技蓝 */
  padding: 30px;
  border-radius: 16px;
  color: #ffffff;
  box-shadow: 0 8px 24px rgba(24, 144, 255, 0.25);
  overflow: hidden;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
/* 卡片装饰微光 */
.balance-card::before {
  content: '';
  position: absolute;
  top: -20%; right: -10%;
  width: 200px; height: 200px;
  background: rgba(255, 255, 255, 0.06);
  border-radius: 50%;
  filter: blur(20px);
}
.balance-info .label {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.75);
  letter-spacing: 1px;
}
.balance-info .amount-row {
  display: flex;
  align-items: baseline;
  margin-top: 8px;
}
.balance-info .symbol {
  font-size: 24px;
  font-weight: 500;
  margin-right: 4px;
}
.balance-info .num {
  font-size: 38px;
  font-weight: 700;
  font-family: 'Helvetica Neue', Arial, sans-serif;
  letter-spacing: -0.5px;
}
.bg-wallet-icon {
  font-size: 72px;
  color: rgba(255, 255, 255, 0.12);
  transform: rotate(-15deg);
}

/* ================= 核心操作面板 ================= */
.action-panel {
  background: #ffffff;
  border-radius: 16px;
  padding: 30px;
  border: 1px solid #eef2f7;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.02);
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: #1f1f1f;
  margin-bottom: 16px;
  position: relative;
  padding-left: 10px;
}
/* 标题前缀个性装饰条 */
.section-title::before {
  content: '';
  position: absolute;
  left: 0; top: 3px;
  width: 4px; height: 14px;
  background-color: #1890ff;
  border-radius: 2px;
}
.mt-30 { margin-top: 32px; }

/* ================= 金额网格选择器 ================= */
.amount-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(160px, 1px)); /* 弹性响应式网格 */
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

@media (max-width: 768px) {
  .amount-grid { grid-template-columns: repeat(2, 1fr); }
}

.amount-item {
  position: relative;
  height: 64px;
  background: #f5f7fa;
  border: 2px solid transparent;
  border-radius: 10px;
  display: flex;
  justify-content: center;
  align-items: center;
  cursor: pointer;
  overflow: hidden;
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
}
.amount-item .val {
  font-size: 18px;
  font-weight: 600;
  color: #434343;
}
.amount-item .unit {
  font-size: 13px;
  font-weight: normal;
  margin-left: 2px;
}

/* 金额项悬浮与激活 */
.amount-item:hover {
  background: #e6f7ff;
  color: #1890ff;
}
.amount-item.active {
  background: #e6f7ff;
  border-color: #1890ff;
}
.amount-item.active .val {
  color: #1890ff;
}

/* 精致小角标 */
.corner-check {
  position: absolute;
  right: -2px; bottom: -2px;
  width: 18px; height: 18px;
  background: #1890ff;
  border-radius: 4px 0 0 0;
  display: flex;
  align-items: center;
  justify-content: center;
}
.corner-check :deep(.el-icon) {
  color: #ffffff;
  font-size: 11px;
  font-weight: bold;
}

/* 自定义输入框微调 */
.custom-input {
  padding: 0;
  background: #ffffff;
  border: 2px dashed #d9d9d9;
}
.custom-input:hover, .custom-input.active {
  border-style: solid;
  border-color: #1890ff;
}
.input-box {
  width: 100%;
  height: 100%;
}
.input-box :deep(.el-input__wrapper) {
  background: transparent !important;
  box-shadow: none !important;
  height: 100%;
  padding: 0 16px;
}
.input-box :deep(.el-input__inner) {
  font-size: 16px;
  font-weight: 600;
  color: #1890ff;
  text-align: center;
}
.suffix-text {
  color: #8c8c8c;
  font-size: 14px;
}

/* ================= 支付方式卡片 ================= */
.payment-methods {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.pay-item {
  display: flex;
  align-items: center;
  padding: 16px 20px;
  background: #ffffff;
  border: 1px solid #eef2f7;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.2s;
}
.pay-item:hover {
  background: #fafbfe;
  border-color: #d6e4ff;
}
.pay-item.active {
  background: #f4f8ff;
  border-color: #91d5ff;
}

/* 支付 Icon 容器优化 */
.icon-box {
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 16px;
}
.pay-svg-icon {
  width: 40px;
  height: 40px;
  transition: transform 0.2s;
}
.pay-item:hover .pay-svg-icon {
  transform: scale(1.05); /* 悬浮时Logo轻微放大交互 */
}

/* 文本信息 */
.pay-info {
  flex-grow: 1;
}
.pay-info .name {
  font-size: 15px;
  font-weight: 600;
  color: #262626;
}
.pay-info .desc {
  font-size: 12px;
  color: #8c8c8c;
  margin-top: 3px;
}

/* 勾选圆形选择器外观升级 */
.radio-check-box {
  display: flex;
  align-items: center;
  justify-content: center;
}
.inner-circle {
  width: 20px;
  height: 20px;
  border-radius: 50%;
  border: 2px solid #d9d9d9;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
  background: #ffffff;
}
.inner-circle.checked {
  border-color: #1890ff;
  background: #1890ff;
}
.inner-circle :deep(.el-icon) {
  color: #ffffff;
  font-size: 12px;
  font-weight: bold;
}

/* ================= 底部提交与实付区域 ================= */
.submit-area {
  margin-top: 40px;
  padding-top: 24px;
  border-top: 1px dashed #e8e8e8;
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 16px;
}

.total-text {
  font-size: 15px;
  color: #434343;
  font-weight: 500;
}
.currency-symbol {
  font-size: 18px;
  color: #ff4d4f;
  font-weight: 600;
  margin-left: 4px;
}
.total-text .price {
  font-size: 28px;
  font-weight: 700;
  color: #ff4d4f; /* 电商标准支付红 */
  font-family: 'Helvetica Neue', sans-serif;
}

.pay-btn {
  padding: 0 40px;
  height: 50px;
  font-size: 16px;
  font-weight: 600;
  border-radius: 25px; /* 高级感胶囊大圆角按钮 */
  box-shadow: 0 4px 14px rgba(24, 144, 255, 0.4);
  transition: all 0.25s;
}
.pay-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 6px 20px rgba(24, 144, 255, 0.5);
}
.pay-btn:active {
  transform: translateY(1px);
}
</style>
