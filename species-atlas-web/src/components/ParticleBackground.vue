<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import * as THREE from 'three'

const containerRef = ref<HTMLDivElement>()
let scene: THREE.Scene
let camera: THREE.PerspectiveCamera
let renderer: THREE.WebGLRenderer
let particles: THREE.Points
let animationId: number
let mouseX = 0
let mouseY = 0

function init() {
  if (!containerRef.value) return

  const width = window.innerWidth
  const height = window.innerHeight

  // 创建场景
  scene = new THREE.Scene()

  // 创建相机
  camera = new THREE.PerspectiveCamera(75, width / height, 0.1, 1000)
  camera.position.z = 50

  // 创建渲染器
  renderer = new THREE.WebGLRenderer({ 
    antialias: true, 
    alpha: true 
  })
  renderer.setSize(width, height)
  renderer.setPixelRatio(Math.min(window.devicePixelRatio, 2))
  containerRef.value.appendChild(renderer.domElement)

  // 创建粒子
  createParticles()

  // 添加事件监听
  window.addEventListener('resize', onResize)
  window.addEventListener('mousemove', onMouseMove)

  // 开始动画
  animate()
}

function createParticles() {
  const particleCount = 2000
  const positions = new Float32Array(particleCount * 3)
  const colors = new Float32Array(particleCount * 3)
  const sizes = new Float32Array(particleCount)

  const colorPalette = [
    new THREE.Color('#2997ff'), // 蓝色
    new THREE.Color('#5ac8fa'), // 浅蓝
    new THREE.Color('#bf5af2'), // 紫色
    new THREE.Color('#30d158'), // 绿色
    new THREE.Color('#ff6b9d')  // 粉色
  ]

  for (let i = 0; i < particleCount; i++) {
    // 位置 - 球形分布
    const radius = 50 + Math.random() * 50
    const theta = Math.random() * Math.PI * 2
    const phi = Math.acos(2 * Math.random() - 1)

    positions[i * 3] = radius * Math.sin(phi) * Math.cos(theta)
    positions[i * 3 + 1] = radius * Math.sin(phi) * Math.sin(theta)
    positions[i * 3 + 2] = radius * Math.cos(phi) - 50

    // 颜色
    const color = colorPalette[Math.floor(Math.random() * colorPalette.length)]
    colors[i * 3] = color.r
    colors[i * 3 + 1] = color.g
    colors[i * 3 + 2] = color.b

    // 大小
    sizes[i] = Math.random() * 2 + 0.5
  }

  const geometry = new THREE.BufferGeometry()
  geometry.setAttribute('position', new THREE.BufferAttribute(positions, 3))
  geometry.setAttribute('color', new THREE.BufferAttribute(colors, 3))
  geometry.setAttribute('size', new THREE.BufferAttribute(sizes, 1))

  // 自定义着色器材质
  const material = new THREE.ShaderMaterial({
    uniforms: {
      uTime: { value: 0 },
      uPixelRatio: { value: Math.min(window.devicePixelRatio, 2) }
    },
    vertexShader: `
      attribute float size;
      attribute vec3 color;
      varying vec3 vColor;
      uniform float uTime;
      uniform float uPixelRatio;
      
      void main() {
        vColor = color;
        
        vec3 pos = position;
        pos.x += sin(uTime * 0.5 + position.y * 0.05) * 2.0;
        pos.y += cos(uTime * 0.3 + position.x * 0.05) * 2.0;
        
        vec4 mvPosition = modelViewMatrix * vec4(pos, 1.0);
        gl_PointSize = size * uPixelRatio * (300.0 / -mvPosition.z);
        gl_Position = projectionMatrix * mvPosition;
      }
    `,
    fragmentShader: `
      varying vec3 vColor;
      
      void main() {
        float dist = distance(gl_PointCoord, vec2(0.5));
        if (dist > 0.5) discard;
        
        float alpha = 1.0 - smoothstep(0.0, 0.5, dist);
        gl_FragColor = vec4(vColor, alpha * 0.8);
      }
    `,
    transparent: true,
    blending: THREE.AdditiveBlending,
    depthWrite: false
  })

  particles = new THREE.Points(geometry, material)
  scene.add(particles)
}

function animate() {
  animationId = requestAnimationFrame(animate)

  const time = performance.now() * 0.001

  // 更新 shader uniform
  if (particles.material instanceof THREE.ShaderMaterial) {
    particles.material.uniforms.uTime.value = time
  }

  // 粒子旋转
  particles.rotation.y = time * 0.05
  particles.rotation.x = Math.sin(time * 0.1) * 0.1

  // 鼠标交互
  camera.position.x += (mouseX * 10 - camera.position.x) * 0.02
  camera.position.y += (-mouseY * 10 - camera.position.y) * 0.02
  camera.lookAt(scene.position)

  renderer.render(scene, camera)
}

function onResize() {
  if (!containerRef.value) return

  const width = window.innerWidth
  const height = window.innerHeight

  camera.aspect = width / height
  camera.updateProjectionMatrix()

  renderer.setSize(width, height)
}

function onMouseMove(event: MouseEvent) {
  mouseX = (event.clientX / window.innerWidth) * 2 - 1
  mouseY = (event.clientY / window.innerHeight) * 2 - 1
}

function cleanup() {
  if (animationId) {
    cancelAnimationFrame(animationId)
  }
  
  window.removeEventListener('resize', onResize)
  window.removeEventListener('mousemove', onMouseMove)
  
  if (renderer && containerRef.value) {
    containerRef.value.removeChild(renderer.domElement)
    renderer.dispose()
  }
  
  if (particles) {
    particles.geometry.dispose()
    if (particles.material instanceof THREE.Material) {
      particles.material.dispose()
    }
  }
}

onMounted(() => {
  init()
})

onUnmounted(() => {
  cleanup()
})
</script>

<template>
  <div ref="containerRef" class="particle-container"></div>
</template>

<style scoped>
.particle-container {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 0;
  pointer-events: none;
}

.particle-container :deep(canvas) {
  display: block;
}
</style>
