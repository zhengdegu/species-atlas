<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch } from 'vue'
import * as THREE from 'three'
import { useSpeciesStore } from '@/stores/species'
import { speciesTypes } from '@/types/species'

const store = useSpeciesStore()
const containerRef = ref<HTMLDivElement>()
const visible = ref(false)
const activeNode = ref<string | null>(null)

let scene: THREE.Scene
let camera: THREE.PerspectiveCamera
let renderer: THREE.WebGLRenderer
let nodes: THREE.Mesh[] = []
let lines: THREE.Line[] = []
let animationId: number
let mouseX = 0
let mouseY = 0

interface NodeData {
  name: string
  color: string
  position: THREE.Vector3
  size: number
  type: 'center' | 'category'
}

const nodeDataList: NodeData[] = [
  {
    name: '物种图谱',
    color: '#ffffff',
    position: new THREE.Vector3(0, 0, 0),
    size: 2,
    type: 'center'
  },
  ...speciesTypes.map((type, index) => ({
    name: type.name,
    color: type.color,
    position: new THREE.Vector3(
      Math.cos((index / 5) * Math.PI * 2) * 8,
      Math.sin((index / 5) * Math.PI * 2) * 8,
      (Math.random() - 0.5) * 4
    ),
    size: 1.2,
    type: 'category' as const
  }))
]

function init() {
  if (!containerRef.value) return

  const width = containerRef.value.clientWidth
  const height = containerRef.value.clientHeight

  // 场景
  scene = new THREE.Scene()

  // 相机
  camera = new THREE.PerspectiveCamera(60, width / height, 0.1, 1000)
  camera.position.z = 25

  // 渲染器
  renderer = new THREE.WebGLRenderer({
    antialias: true,
    alpha: true
  })
  renderer.setSize(width, height)
  renderer.setPixelRatio(Math.min(window.devicePixelRatio, 2))
  containerRef.value.appendChild(renderer.domElement)

  // 创建节点
  createNodes()

  // 创建连线
  createLines()

  // 添加环境光
  const ambientLight = new THREE.AmbientLight(0xffffff, 0.5)
  scene.add(ambientLight)

  // 点光源
  const pointLight = new THREE.PointLight(0x2997ff, 1)
  pointLight.position.set(10, 10, 10)
  scene.add(pointLight)

  // 事件监听
  window.addEventListener('resize', onResize)
  containerRef.value.addEventListener('mousemove', onMouseMove)

  // 开始动画
  animate()
}

function createNodes() {
  nodeDataList.forEach((data) => {
    // 节点几何体
    const geometry = new THREE.SphereGeometry(data.size, 32, 32)

    // 节点材质
    const material = new THREE.MeshStandardMaterial({
      color: data.color,
      emissive: data.color,
      emissiveIntensity: 0.3,
      roughness: 0.4,
      metalness: 0.6
    })

    const mesh = new THREE.Mesh(geometry, material)
    mesh.position.copy(data.position)
    mesh.userData = { name: data.name, type: data.type }

    nodes.push(mesh)
    scene.add(mesh)

    // 添加光晕效果
    if (data.type === 'center') {
      const glowGeometry = new THREE.SphereGeometry(data.size * 1.5, 32, 32)
      const glowMaterial = new THREE.MeshBasicMaterial({
        color: data.color,
        transparent: true,
        opacity: 0.15
      })
      const glow = new THREE.Mesh(glowGeometry, glowMaterial)
      mesh.add(glow)
    }
  })
}

function createLines() {
  const centerNode = nodes[0]

  nodes.slice(1).forEach((node) => {
    const points = []
    points.push(centerNode.position.clone())
    points.push(node.position.clone())

    const geometry = new THREE.BufferGeometry().setFromPoints(points)
    const material = new THREE.LineBasicMaterial({
      color: 0x2997ff,
      transparent: true,
      opacity: 0.3
    })

    const line = new THREE.Line(geometry, material)
    lines.push(line)
    scene.add(line)
  })
}

function animate() {
  animationId = requestAnimationFrame(animate)

  const time = performance.now() * 0.001

  // 节点动画
  nodes.forEach((node, index) => {
    if (index === 0) {
      // 中心节点缓慢旋转
      node.rotation.y = time * 0.2
    } else {
      // 其他节点浮动效果
      const offset = index * 0.5
      node.position.y =
        nodeDataList[index].position.y + Math.sin(time + offset) * 0.5
      node.position.x =
        nodeDataList[index].position.x + Math.cos(time * 0.5 + offset) * 0.3
    }
  })

  // 更新连线
  lines.forEach((line, index) => {
    const positions = line.geometry.attributes.position.array as Float32Array
    const targetNode = nodes[index + 1]

    positions[3] = targetNode.position.x
    positions[4] = targetNode.position.y
    positions[5] = targetNode.position.z

    line.geometry.attributes.position.needsUpdate = true
  })

  // 相机跟随鼠标
  camera.position.x += (mouseX * 5 - camera.position.x) * 0.02
  camera.position.y += (-mouseY * 5 - camera.position.y) * 0.02
  camera.lookAt(scene.position)

  renderer.render(scene, camera)
}

function onResize() {
  if (!containerRef.value) return

  const width = containerRef.value.clientWidth
  const height = containerRef.value.clientHeight

  camera.aspect = width / height
  camera.updateProjectionMatrix()
  renderer.setSize(width, height)
}

function onMouseMove(event: MouseEvent) {
  if (!containerRef.value) return

  const rect = containerRef.value.getBoundingClientRect()
  mouseX = ((event.clientX - rect.left) / rect.width) * 2 - 1
  mouseY = ((event.clientY - rect.top) / rect.height) * 2 - 1
}

function cleanup() {
  if (animationId) {
    cancelAnimationFrame(animationId)
  }

  window.removeEventListener('resize', onResize)

  if (containerRef.value) {
    containerRef.value.removeEventListener('mousemove', onMouseMove)

    if (renderer && renderer.domElement) {
      containerRef.value.removeChild(renderer.domElement)
    }
  }

  nodes.forEach((node) => {
    node.geometry.dispose()
    if (node.material instanceof THREE.Material) {
      node.material.dispose()
    }
  })

  lines.forEach((line) => {
    line.geometry.dispose()
    if (line.material instanceof THREE.Material) {
      line.material.dispose()
    }
  })

  if (renderer) {
    renderer.dispose()
  }
}

function selectCategory(type: string) {
  store.setType(type as any)
  const section = document.getElementById('species-section')
  if (section) {
    section.scrollIntoView({ behavior: 'smooth' })
  }
}

onMounted(() => {
  const observer = new IntersectionObserver(
    (entries) => {
      entries.forEach((entry) => {
        if (entry.isIntersecting && !visible.value) {
          visible.value = true
          setTimeout(() => {
            init()
          }, 300)
        }
      })
    },
    { threshold: 0.2 }
  )

  if (containerRef.value) {
    observer.observe(containerRef.value)
  }
})

onUnmounted(() => {
  cleanup()
})
</script>

<template>
  <section class="graph-section">
    <div class="container">
      <div class="section-header" :class="{ visible }">
        <h2 class="section-title">
          物种关系图谱
          <span class="title-sub">Species Relation Graph</span>
        </h2>
        <p class="section-desc">
          探索五大物种类别之间的关联与分布
        </p>
      </div>

      <div class="graph-container" :class="{ visible }">
        <!-- 3D 图谱 -->
        <div ref="containerRef" class="graph-canvas"></div>

        <!-- 图例和交互 -->
        <div class="graph-legend">
          <div class="legend-title">点击探索</div>
          <div class="legend-items">
            <button
              v-for="type in speciesTypes"
              :key="type.name"
              class="legend-item"
              :class="{ active: store.currentType === type.name }"
              :style="{ '--item-color': type.color }"
              @click="selectCategory(type.name)"
              @mouseenter="activeNode = type.name"
              @mouseleave="activeNode = null"
            >
              <span class="item-dot"></span>
              <span class="item-name">{{ type.name }}</span>
              <span class="item-count">{{ store.stats.byType[type.name] || 0 }}</span>
            </button>
          </div>
        </div>

        <!-- 节点信息提示 -->
        <div v-if="activeNode" class="node-tooltip">
          <div class="tooltip-content">
            <span class="tooltip-name">{{ activeNode }}</span>
            <span class="tooltip-desc">点击查看详细物种列表</span>
          </div>
        </div>
      </div>
    </div>
  </section>
</template>

<style scoped>
.graph-section {
  position: relative;
  padding: 120px 0;
  background: linear-gradient(180deg, var(--color-bg) 0%, var(--color-bg-secondary) 100%);
  overflow: hidden;
}

.section-header {
  text-align: center;
  margin-bottom: 48px;
  opacity: 0;
  transform: translateY(40px);
  transition: all 0.8s ease-out;
}

.section-header.visible {
  opacity: 1;
  transform: translateY(0);
}

.section-title {
  font-family: var(--font-display);
  font-size: clamp(32px, 5vw, 48px);
  font-weight: 700;
  color: var(--color-text);
  letter-spacing: -0.02em;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  margin-bottom: 16px;
}

.title-sub {
  font-size: 14px;
  font-weight: 400;
  color: var(--color-text-muted);
  letter-spacing: 0.1em;
  text-transform: uppercase;
}

.section-desc {
  font-size: 18px;
  color: var(--color-text-secondary);
}

.graph-container {
  position: relative;
  height: 500px;
  border-radius: 24px;
  background: var(--color-bg-card);
  border: 1px solid var(--color-border);
  overflow: hidden;
  opacity: 0;
  transform: scale(0.95);
  transition: all 0.8s ease-out 0.2s;
}

.graph-container.visible {
  opacity: 1;
  transform: scale(1);
}

.graph-canvas {
  width: 100%;
  height: 100%;
}

.graph-legend {
  position: absolute;
  top: 24px;
  right: 24px;
  padding: 20px;
  background: rgba(0, 0, 0, 0.6);
  backdrop-filter: blur(20px);
  border: 1px solid var(--color-border);
  border-radius: 16px;
  z-index: 10;
}

.legend-title {
  font-size: 12px;
  font-weight: 600;
  color: var(--color-text-muted);
  text-transform: uppercase;
  letter-spacing: 0.05em;
  margin-bottom: 16px;
}

.legend-items {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 14px;
  background: transparent;
  border: 1px solid transparent;
  border-radius: 10px;
  cursor: pointer;
  transition: all var(--transition-fast);
}

.legend-item:hover {
  background: rgba(255, 255, 255, 0.05);
  border-color: var(--color-border);
}

.legend-item.active {
  background: rgba(255, 255, 255, 0.1);
  border-color: var(--item-color);
}

.item-dot {
  width: 10px;
  height: 10px;
  background: var(--item-color);
  border-radius: 50%;
  box-shadow: 0 0 10px var(--item-color);
}

.item-name {
  flex: 1;
  font-size: 14px;
  font-weight: 500;
  color: var(--color-text);
  text-align: left;
}

.item-count {
  font-family: var(--font-mono);
  font-size: 12px;
  color: var(--color-text-muted);
}

.node-tooltip {
  position: absolute;
  bottom: 24px;
  left: 50%;
  transform: translateX(-50%);
  padding: 12px 20px;
  background: rgba(0, 0, 0, 0.8);
  backdrop-filter: blur(10px);
  border: 1px solid var(--color-border);
  border-radius: 12px;
  z-index: 10;
  animation: tooltipIn 0.3s ease-out;
}

@keyframes tooltipIn {
  from {
    opacity: 0;
    transform: translateX(-50%) translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateX(-50%) translateY(0);
  }
}

.tooltip-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

.tooltip-name {
  font-size: 16px;
  font-weight: 600;
  color: var(--color-text);
}

.tooltip-desc {
  font-size: 12px;
  color: var(--color-text-muted);
}

/* 响应式 */
@media (max-width: 768px) {
  .graph-container {
    height: 400px;
  }

  .graph-legend {
    position: relative;
    top: auto;
    right: auto;
    margin-top: 24px;
    border-radius: 16px;
  }

  .legend-items {
    flex-direction: row;
    flex-wrap: wrap;
  }

  .legend-item {
    flex: 1 1 calc(50% - 4px);
    min-width: 120px;
  }
}
</style>
