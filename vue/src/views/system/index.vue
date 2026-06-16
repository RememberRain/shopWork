<template>
  <div class="dashboard-container">
    <!-- 统计卡片区域 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :span="4" v-for="item in statsData" :key="item.title">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon" :style="{ backgroundColor: item.color }">
              <component :is="item.icon"/>
            </div>
            <div class="stat-info">
              <div class="stat-number">{{ item.value }}</div>
              <div class="stat-title">{{ item.title }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表区域 -->
    <el-row :gutter="20" class="charts-row">
      <el-col :span="16">
        <el-card class="chart-card" shadow="never">
          <template #header>
            <div class="card-header"></div>
            <span>商家认证状态</span>
          </template>
          <div ref="merchantChartRef" style="height: 470px"/>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card class="chart-card" shadow="never">
          <template #header>
            <div class="card-header"></div>
            <span>订单状态分布</span>
          </template>
          <div ref="orderChartRef" style="height: 470px"/>
        </el-card>
      </el-col>
    </el-row>


  </div>
</template>

<script setup>
import {onMounted, ref} from "vue";
import {merchantAuth, orderStatus, selectCount} from "@/api/homestay/home.js";
import * as echarts from 'echarts'

const merchantChartRef = ref()
const orderChartRef = ref()

// 统计数据
const statsData = ref([
  {title: '用户总数', value: 0, icon: 'User', color: '#409EFF'},
  {title: '商品总数', value: 0, icon: 'HomeFilled', color: '#67C23A'},
  {title: '订单总数', value: 0, icon: 'Document', color: '#E6A23C'},
  {title: '评价总数', value: 0, icon: 'Star', color: '#F56C6C'},
  {title: '尾货商品', value: 0, icon: 'Location', color: '#909399'},
  {title: '商家总数', value: 0, icon: 'Shop', color: '#FF6B6B'}
])

//初始化商家认证状态图表
const initMerchantChart = () => {
  merchantAuth().then(res => {
    const data = res.data

    let merchantChart = echarts.init(merchantChartRef.value)

    const option = {
      tooltip: {
        trigger: 'axis',
        axisPointer: {
          type: 'shadow'
        }
      },
      grid: {
        left: '3%',
        right: '4%',
        bottom: '3%',
        containLabel: true
      },
      xAxis: [
        {
          type: 'category',
          data: ['待审核', '认证通过', '认证未通过'],
          axisTick: {
            alignWithLabel: true
          }
        }
      ],
      yAxis: [
        {
          type: 'value'
        }
      ],
      series: [
        {
          name: '商家数量',
          type: 'bar',
          barWidth: '60%',
          data: [
            {value: data['待审核'] || 0, itemStyle: {color: '#E6A23C'}},
            {value: data['认证通过'] || 0, itemStyle: {color: '#67C23A'}},
            {value: data['认证未通过'] || 0, itemStyle: {color: '#F56C6C'}}
          ]
        }
      ]
    }

    merchantChart.setOption(option)
  })

}

//初始化订单状态图表
const initOrderChart = () => {
  orderStatus().then(res => {
    const data = res.data

    let orderChart = echarts.init(orderChartRef.value)

    const option = {
      tooltip: {
        trigger: 'item'
      },
      legend: {
        top: '5%',
        left: 'center'
      },
      series: [
        {
          name: '订单状态',
          type: 'pie',
          radius: ['40%', '70%'],
          avoidLabelOverlap: false,
          itemStyle: {
            borderRadius: 10,
            borderColor: '#fff',
            borderWidth: 2
          },
          label: {
            show: false,
            position: 'center'
          },
          emphasis: {
            label: {
              show: true,
              fontSize: '18',
              fontWeight: 'bold'
            }
          },
          labelLine: {
            show: false
          },
          data: [
            {value: data['待支付'] || 0, name: '待支付', itemStyle: {color: '#F56C6C'}},
            {value: data['已支付'] || 0, name: '已支付', itemStyle: {color: '#409EFF'}},
            {value: data['已完成'] || 0, name: '已完成', itemStyle: {color: '#67C23A'}},
            {value: data['已取消'] || 0, name: '已取消', itemStyle: {color: '#909399'}}
          ]
        }
      ]
    }

    orderChart.setOption(option)

  })
}

onMounted(() => {
  selectCount().then(res => {
    statsData.value[0].value = res.data.userCount
    statsData.value[1].value = res.data.homestayCount
    statsData.value[2].value = res.data.orderCount
    statsData.value[3].value = res.data.reviewCount
    statsData.value[4].value = res.data.spotCount
    statsData.value[5].value = res.data.merchantCount
  })

  initMerchantChart()

  initOrderChart()
})

</script>

<style scoped>
.dashboard-container {
  padding: 20px;
  background-color: #f5f5f5;

}

.stats-row {
  margin-bottom: 20px;
}

.stat-card {
  border-radius: 8px;
  overflow: hidden;
  transition: all 0.3s ease;
}

.stat-content {
  display: flex;
  align-items: center;
  padding: 20px;
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 20px;
}

.stat-icon :deep(svg) {
  width: 28px;
  height: 28px;
  color: white;
}

.stat-info {
  flex: 1;
}

.stat-number {
  font-size: 28px;
  font-weight: bold;
  color: #333;
  margin-bottom: 5px;
}

.stat-title {
  font-size: 14px;
  color: #666;
}

.charts-row {
  margin-bottom: 20px;
}

.chart-card {
  border-radius: 8px;
}

.card-header {
  font-size: 18px;
  font-weight: bold;
  color: #333;
}
</style>
