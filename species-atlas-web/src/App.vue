<script setup lang="ts">
import { ref, provide } from 'vue'
import Navbar from '@/components/Navbar.vue'
import HeroSection from '@/components/HeroSection.vue'
import CategoryCards from '@/components/CategoryCards.vue'
import SpeciesGrid from '@/components/SpeciesGrid.vue'
import StatsSection from '@/components/StatsSection.vue'
import RelationGraph from '@/components/RelationGraph.vue'
import Footer from '@/components/Footer.vue'
import LoadingScreen from '@/components/LoadingScreen.vue'
import SearchModal from '@/components/SearchModal.vue'
import LoginModal from '@/components/LoginModal.vue'
import { useSpeciesStore } from '@/stores/species'
import { useUserStore } from '@/stores/user'
import type { Species, SpeciesType } from '@/types/species'

const speciesStore = useSpeciesStore()
const userStore = useUserStore()

const isLoading = ref(true)
const searchVisible = ref(false)
const isAuthenticated = ref(false)
const loginModalRef = ref<InstanceType<typeof LoginModal>>()

// 提供清除登录状态的方法给子组件
provide('clearToken', () => {
  loginModalRef.value?.clearToken()
  isAuthenticated.value = false
})

provide('showTokenInput', () => {
  loginModalRef.value?.show()
})

// 提供用户信息
provide('userStore', userStore)

function onLoadingComplete() {
  isLoading.value = false
}

function onAuthenticated() {
  isAuthenticated.value = true
}

function openSearch() {
  searchVisible.value = true
}

function closeSearch() {
  searchVisible.value = false
}

function handleSearchSelect(species: Species, type: SpeciesType) {
  speciesStore.setType(type)
  speciesStore.fetchSpeciesDetail(species.id || species.serialNumber || species.speciesCode)
}
</script>

<template>
  <div class="app">
    <!-- 登录弹窗 -->
    <LoginModal ref="loginModalRef" @authenticated="onAuthenticated" />
    
    <!-- 加载屏幕 -->
    <LoadingScreen v-if="isLoading && isAuthenticated" @complete="onLoadingComplete" />
    
    <!-- 主内容 -->
    <template v-if="isAuthenticated && !isLoading">
      <Navbar @open-search="openSearch" />
      <main>
        <HeroSection />
        <CategoryCards />
        <StatsSection />
        <RelationGraph />
        <SpeciesGrid />
      </main>
      <Footer />
      
      <!-- 搜索弹窗 -->
      <SearchModal 
        :visible="searchVisible" 
        @close="closeSearch"
        @select="handleSearchSelect"
      />
    </template>
  </div>
</template>

<style>
.app {
  min-height: 100vh;
  background: var(--color-bg);
}

main {
  position: relative;
}
</style>
