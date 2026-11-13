import { request } from './request'
import type { Species, PageParams, PageResponse, SpeciesType } from '@/types/species'

// API 路径映射（新后端路径）
const API_PATHS: Record<SpeciesType, string> = {
  '底栖动物': '/species/benthic-animal',
  '水生植物': '/species/aquatic-plant',
  '浮游植物': '/species/phytoplankton',
  '鱼类': '/species/fish',
  '浮游动物': '/species/zooplankton'
}

// 获取物种列表
export function getSpeciesList(type: SpeciesType, params: PageParams): Promise<PageResponse<Species>> {
  const basePath = API_PATHS[type]
  return request<{ data: PageResponse<Species> }>({
    url: `${basePath}/list`,
    method: 'get',
    params
  }).then(res => res.data)
}

// 获取物种详情
export function getSpeciesDetail(type: SpeciesType, id: string | number): Promise<{ data: Species }> {
  const basePath = API_PATHS[type]
  return request({
    url: `${basePath}/${id}`,
    method: 'get'
  })
}

// 创建物种
export function createSpecies(type: SpeciesType, data: Partial<Species>): Promise<{ data: Species }> {
  const basePath = API_PATHS[type]
  return request({
    url: basePath,
    method: 'post',
    data
  })
}

// 更新物种
export function updateSpecies(type: SpeciesType, id: string | number, data: Partial<Species>): Promise<{ data: Species }> {
  const basePath = API_PATHS[type]
  return request({
    url: `${basePath}/${id}`,
    method: 'put',
    data
  })
}

// 删除物种
export function deleteSpecies(type: SpeciesType, id: string | number): Promise<void> {
  const basePath = API_PATHS[type]
  return request({
    url: `${basePath}/${id}`,
    method: 'delete'
  })
}

// 批量删除物种
export function batchDeleteSpecies(type: SpeciesType, ids: (string | number)[]): Promise<void> {
  const basePath = API_PATHS[type]
  return request({
    url: `${basePath}/batch`,
    method: 'delete',
    data: { ids }
  })
}

// 获取统计数据
export async function getSpeciesStats(): Promise<{
  total: number
  byType: Record<SpeciesType, number>
}> {
  const res = await request<{
    data: {
      total: number
      byType: Record<string, number>
    }
  }>({
    url: '/statistics/overview',
    method: 'get'
  })

  // 映射后端字段到前端中文 key
  const byType: Record<SpeciesType, number> = {
    '底栖动物': res.data.byType.benthicAnimal || 0,
    '水生植物': res.data.byType.aquaticPlant || 0,
    '浮游植物': res.data.byType.phytoplankton || 0,
    '鱼类': res.data.byType.fish || 0,
    '浮游动物': res.data.byType.zooplankton || 0
  }

  return {
    total: res.data.total,
    byType
  }
}

// 搜索物种（跨类别）
export async function searchSpecies(
  keyword: string,
  pageSize: number = 20
): Promise<{ type: SpeciesType; species: Species }[]> {
  const res = await request<{
    data: Array<{
      type: string
      typePath: string
      species: Species
    }>
  }>({
    url: '/species/search',
    method: 'get',
    params: { keyword, pageSize }
  })

  // 转换为前端格式
  return res.data.map(item => ({
    type: item.type as SpeciesType,
    species: item.species
  }))
}
