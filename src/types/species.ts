// 物种基础信息
export interface Species {
  id?: number
  serialNumber?: string
  speciesCode: string
  chineseName: string
  latinName?: string
  photoCode?: string
  photoPaths?: string[]
  taxonomicStatus: string
  geneName?: string
  dnaSequenceCode?: string
  dnaCodingProperty?: string
  morphologicalDescription?: string
  reference?: string
  geographicalDistribution?: string
  createTime?: string
  updateTime?: string
}

// 物种类型
export type SpeciesType = '底栖动物' | '水生植物' | '浮游植物' | '鱼类' | '浮游动物'

// 物种类型英文映射
export const speciesTypeMap: Record<SpeciesType, string> = {
  '底栖动物': 'benthic',
  '水生植物': 'aquatic',
  '浮游植物': 'phytoplankton',
  '鱼类': 'fish',
  '浮游动物': 'zooplankton'
}

// 物种类型配置
export interface SpeciesTypeConfig {
  name: SpeciesType
  nameEn: string
  icon: string
  color: string
  gradient: string
  description: string
}

export const speciesTypes: SpeciesTypeConfig[] = [
  {
    name: '底栖动物',
    nameEn: 'Benthos',
    icon: '🦐',
    color: '#ff6b6b',
    gradient: 'linear-gradient(135deg, #ff6b6b 0%, #ee5a24 100%)',
    description: '栖息于水体底部的动物群落'
  },
  {
    name: '水生植物',
    nameEn: 'Aquatic Plants',
    icon: '🌿',
    color: '#26de81',
    gradient: 'linear-gradient(135deg, #26de81 0%, #20bf6b 100%)',
    description: '生长于水中的植物群落'
  },
  {
    name: '浮游植物',
    nameEn: 'Phytoplankton',
    icon: '🧬',
    color: '#45aaf2',
    gradient: 'linear-gradient(135deg, #45aaf2 0%, #2d98da 100%)',
    description: '漂浮于水中的微小植物'
  },
  {
    name: '鱼类',
    nameEn: 'Fish',
    icon: '🐟',
    color: '#fed330',
    gradient: 'linear-gradient(135deg, #fed330 0%, #f7b731 100%)',
    description: '水域中的脊椎动物'
  },
  {
    name: '浮游动物',
    nameEn: 'Zooplankton',
    icon: '🔬',
    color: '#a55eea',
    gradient: 'linear-gradient(135deg, #a55eea 0%, #8854d0 100%)',
    description: '漂浮于水中的微小动物'
  }
]

// 分页参数
export interface PageParams {
  pageNum: number
  pageSize: number
  speciesCode?: string
  chineseName?: string
  geneName?: string
}

// 分页响应
export interface PageResponse<T> {
  rows: T[]
  total: number
  code?: number
  msg?: string
}

// 统计数据
export interface SpeciesStats {
  totalCount: number
  benthicCount: number
  aquaticCount: number
  phytoplanktonCount: number
  fishCount: number
  zooplanktonCount: number
}
