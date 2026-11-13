import { ref, onMounted, onUnmounted, type Ref } from 'vue'

interface ParallaxOptions {
  speed?: number
  direction?: 'vertical' | 'horizontal'
  reverse?: boolean
}

export function useParallax(
  elementRef: Ref<HTMLElement | undefined>,
  options: ParallaxOptions = {}
) {
  const { speed = 0.5, direction = 'vertical', reverse = false } = options

  const offset = ref(0)
  const isInView = ref(false)

  function handleScroll() {
    if (!elementRef.value) return

    const rect = elementRef.value.getBoundingClientRect()
    const windowHeight = window.innerHeight

    // 检查元素是否在视口内
    if (rect.bottom >= 0 && rect.top <= windowHeight) {
      isInView.value = true

      // 计算视差偏移
      const scrollPosition = windowHeight - rect.top
      const rawOffset = scrollPosition * speed

      offset.value = reverse ? -rawOffset : rawOffset
    } else {
      isInView.value = false
    }
  }

  onMounted(() => {
    window.addEventListener('scroll', handleScroll, { passive: true })
    handleScroll() // 初始计算
  })

  onUnmounted(() => {
    window.removeEventListener('scroll', handleScroll)
  })

  const style = ref({})

  if (direction === 'vertical') {
    style.value = {
      transform: `translateY(${offset.value}px)`
    }
  } else {
    style.value = {
      transform: `translateX(${offset.value}px)`
    }
  }

  return {
    offset,
    isInView,
    style
  }
}

// 鼠标视差效果
export function useMouseParallax(
  containerRef: Ref<HTMLElement | undefined>,
  intensity: number = 0.02
) {
  const mouseX = ref(0)
  const mouseY = ref(0)

  function handleMouseMove(e: MouseEvent) {
    if (!containerRef.value) return

    const rect = containerRef.value.getBoundingClientRect()
    const centerX = rect.left + rect.width / 2
    const centerY = rect.top + rect.height / 2

    mouseX.value = (e.clientX - centerX) * intensity
    mouseY.value = (e.clientY - centerY) * intensity
  }

  function handleMouseLeave() {
    mouseX.value = 0
    mouseY.value = 0
  }

  onMounted(() => {
    if (!containerRef.value) return
    containerRef.value.addEventListener('mousemove', handleMouseMove)
    containerRef.value.addEventListener('mouseleave', handleMouseLeave)
  })

  onUnmounted(() => {
    if (!containerRef.value) return
    containerRef.value.removeEventListener('mousemove', handleMouseMove)
    containerRef.value.removeEventListener('mouseleave', handleMouseLeave)
  })

  return {
    mouseX,
    mouseY,
    style: {
      transform: `translate(${mouseX.value}px, ${mouseY.value}px)`
    }
  }
}
