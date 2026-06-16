import { createRouter, createWebHashHistory } from 'vue-router'
import Layout from '../layouts/Layout.vue'

const routes = [
  {
    path: '/',
    component: Layout,
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('../views/Dashboard.vue'),
        meta: { title: '工作台' }
      },
      {
        path: 'stalls',
        name: 'Stalls',
        component: () => import('../views/StallList.vue'),
        meta: { title: '摊位管理' }
      },
      {
        path: 'scales',
        name: 'Scales',
        component: () => import('../views/ScaleList.vue'),
        meta: { title: '秤具管理' }
      },
      {
        path: 'calibrations',
        name: 'Calibrations',
        component: () => import('../views/CalibrationList.vue'),
        meta: { title: '校准管理' }
      },
      {
        path: 'complaints',
        name: 'Complaints',
        component: () => import('../views/ComplaintList.vue'),
        meta: { title: '投诉管理' }
      },
      {
        path: 'reinspections',
        name: 'Reinspections',
        component: () => import('../views/ReinspectionList.vue'),
        meta: { title: '复检管理' }
      },
      {
        path: 'suspensions',
        name: 'Suspensions',
        component: () => import('../views/SuspensionList.vue'),
        meta: { title: '暂停营业' }
      },
      {
        path: 'scale-borrows',
        name: 'ScaleBorrows',
        component: () => import('../views/ScaleBorrowList.vue'),
        meta: { title: '临时借秤' }
      },
      {
        path: 'audit-logs',
        name: 'AuditLogs',
        component: () => import('../views/AuditLogList.vue'),
        meta: { title: '审计日志' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHashHistory(),
  routes
})

export default router
