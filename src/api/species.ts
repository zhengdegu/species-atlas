import { request } from './request'
import type { Species, PageParams, PageResponse, SpeciesType } from '@/types/species'

// API 路径映射
const API_PATHS: Record<SpeciesType, string> = {
  '底栖动物': '/system/benthicAnimalDataSummary',
  '水生植物': '/system/aquaticPlantDataSummary',
  '浮游植物': '/system/phytoplanktonDataSummary',
  '鱼类': '/system/fishDataSummary',
  '浮游动物': '/system/zooplanktonDataSummary'
}

// 获取物种列表
export function getSpeciesList(type: SpeciesType, params: PageParams): Promise<PageResponse<Species>> {
  const basePath = API_PATHS[type]
  return request({
    url: `${basePath}/list`,
    method: 'get',
    params
  })
}

// 获取物种详情
export function getSpeciesDetail(type: SpeciesType, id: string | number): Promise<{ data: Species }> {
  const basePath = API_PATHS[type]
  return request({
    url: `${basePath}/${id}`,
    method: 'get'
  })
}

// 获取图谱记录总数
export function getDataSummaryCount(): Promise<{ data: number }> {
  return request({
    url: '/system/dataSummary/count',
    method: 'get'
  })
}

// 获取各类别数量统计
export async function getSpeciesStats(): Promise<{
  total: number
  byType: Record<SpeciesType, number>
}> {
  const types: SpeciesType[] = ['底栖动物', '水生植物', '浮游植物', '鱼类', '浮游动物']
  
  const results = await Promise.all(
    types.map(type => 
      getSpeciesList(type, { pageNum: 1, pageSize: 1 })
        .then(res => ({ type, total: res.total }))
        .catch(() => ({ type, total: 0 }))
    )
  )
  
  const byType = results.reduce((acc, { type, total }) => {
    acc[type] = total
    return acc
  }, {} as Record<SpeciesType, number>)
  
  const total = Object.values(byType).reduce((sum, count) => sum + count, 0)
  
  return { total, byType }
}

// 搜索物种（跨类别）
export async function searchSpecies(
  keyword: string,
  pageSize: number = 20
): Promise<{ type: SpeciesType; species: Species }[]> {
  const types: SpeciesType[] = ['底栖动物', '水生植物', '浮游植物', '鱼类', '浮游动物']
  
  const results = await Promise.all(
    types.map(type =>
      getSpeciesList(type, {
        pageNum: 1,
        pageSize,
        chineseName: keyword
      }).then(res => 
        res.rows.map(species => ({ type, species }))
      ).catch(() => [])
    )
  )
  
  return results.flat()
}
