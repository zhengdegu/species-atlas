import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { Species, SpeciesType, PageParams } from '@/types/species'
import { getSpeciesList, getSpeciesDetail, getSpeciesStats } from '@/api/species'

export const useSpeciesStore = defineStore('species', () => {
  // 当前选中的物种类型
  const currentType = ref<SpeciesType>('底栖动物')
  
  // 物种列表
  const speciesList = ref<Species[]>([])
  
  // 加载状态
  const loading = ref(false)
  
  // 分页信息
  const pagination = ref({
    pageNum: 1,
    pageSize: 20,
    total: 0
  })
  
  // 搜索关键词
  const searchKeyword = ref('')
  
  // 统计数据
  const stats = ref<{
    total: number
    byType: Record<SpeciesType, number>
  }>({
    total: 0,
    byType: {
      '底栖动物': 0,
      '水生植物': 0,
      '浮游植物': 0,
      '鱼类': 0,
      '浮游动物': 0
    }
  })
  
  // 当前选中的物种详情
  const selectedSpecies = ref<Species | null>(null)
  const detailVisible = ref(false)
  
  // 计算属性：当前类型的数量
  const currentTypeCount = computed(() => stats.value.byType[currentType.value] || 0)
  
  // 获取物种列表
  async function fetchSpeciesList(reset = false) {
    if (reset) {
      pagination.value.pageNum = 1
      speciesList.value = []
    }
    
    loading.value = true
    
    try {
      const params: PageParams = {
        pageNum: pagination.value.pageNum,
        pageSize: pagination.value.pageSize
      }
      
      if (searchKeyword.value) {
        params.chineseName = searchKeyword.value
      }
      
      const res = await getSpeciesList(currentType.value, params)
      
      if (reset) {
        speciesList.value = res.rows
      } else {
        speciesList.value = [...speciesList.value, ...res.rows]
      }
      
      pagination.value.total = res.total
    } catch (error) {
      console.error('Failed to fetch species list:', error)
    } finally {
      loading.value = false
    }
  }
  
  // 加载更多
  async function loadMore() {
    if (loading.value) return
    if (speciesList.value.length >= pagination.value.total) return
    
    pagination.value.pageNum++
    await fetchSpeciesList(false)
  }
  
  // 切换物种类型
  async function setType(type: SpeciesType) {
    if (currentType.value === type) return
    currentType.value = type
    searchKeyword.value = ''
    await fetchSpeciesList(true)
  }
  
  // 搜索
  async function search(keyword: string) {
    searchKeyword.value = keyword
    await fetchSpeciesList(true)
  }
  
  // 获取物种详情
  async function fetchSpeciesDetail(id: string | number) {
    try {
      const res = await getSpeciesDetail(currentType.value, id)
      selectedSpecies.value = res.data
      detailVisible.value = true
    } catch (error) {
      console.error('Failed to fetch species detail:', error)
    }
  }
  
  // 关闭详情
  function closeDetail() {
    detailVisible.value = false
    selectedSpecies.value = null
  }
  
  // 获取统计数据
  async function fetchStats() {
    try {
      const result = await getSpeciesStats()
      stats.value = result
    } catch (error) {
      console.error('Failed to fetch stats:', error)
    }
  }
  
  return {
    currentType,
    speciesList,
    loading,
    pagination,
    searchKeyword,
    stats,
    selectedSpecies,
    detailVisible,
    currentTypeCount,
    fetchSpeciesList,
    loadMore,
    setType,
    search,
    fetchSpeciesDetail,
    closeDetail,
    fetchStats
  }
})
