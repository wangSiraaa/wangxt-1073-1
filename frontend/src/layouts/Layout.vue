<template>
  <el-container style="height: 100vh">
    <el-aside width="220px" style="background: #304156">
      <div style="padding: 16px; color: #fff; font-size: 16px; font-weight: 600; text-align: center; border-bottom: 1px solid #1f2d3d">
        <el-icon style="margin-right: 8px"><Scale /></el-icon>
        电子秤监管系统
      </div>
      <el-menu
        :default-active="activeMenu"
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409EFF"
        router>
        <el-menu-item index="/dashboard">
          <el-icon><Odometer /></el-icon>
          <span>工作台</span>
        </el-menu-item>
        <el-menu-item index="/stalls">
          <el-icon><Shop /></el-icon>
          <span>摊位管理</span>
        </el-menu-item>
        <el-menu-item index="/scales">
          <el-icon><Scale /></el-icon>
          <span>秤具管理</span>
        </el-menu-item>
        <el-menu-item index="/calibrations">
          <el-icon><Files /></el-icon>
          <span>校准管理</span>
        </el-menu-item>
        <el-menu-item index="/complaints">
          <el-icon><Bell /></el-icon>
          <span>投诉管理</span>
        </el-menu-item>
        <el-menu-item index="/reinspections">
          <el-icon><RefreshRight /></el-icon>
          <span>复检管理</span>
        </el-menu-item>
        <el-menu-item index="/suspensions">
          <el-icon><VideoPause /></el-icon>
          <span>暂停营业</span>
        </el-menu-item>
        <el-menu-item index="/scale-borrows">
          <el-icon><Share /></el-icon>
          <span>临时借秤</span>
        </el-menu-item>
        <el-menu-item index="/audit-logs">
          <el-icon><Document /></el-icon>
          <span>审计日志</span>
        </el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header style="background: #fff; border-bottom: 1px solid #e6e6e6; display: flex; align-items: center; justify-content: space-between">
        <div style="font-size: 16px; color: #303133; font-weight: 500">
          {{ pageTitle }}
        </div>
        <div style="color: #606266; font-size: 14px">
          <el-icon style="margin-right: 6px"><User /></el-icon>
          系统管理员
        </div>
      </el-header>
      <el-main style="background: #f0f2f5; padding: 20px">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const activeMenu = computed(() => route.path)
const pageTitle = computed(() => route.meta?.title || '')
</script>
