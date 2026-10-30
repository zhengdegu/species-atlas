import { ref, watch, type Ref } from 'vue'

interface CountUpOptions {
  duration?: number
  startValue?: number
  easing?: (t: number) => number
  separator?: string
  decimal?: string
  decimals?: number
  prefix?: string
  suffix?: string
}

// 默认缓动函数
const easeOutExpo = (t: number): number => {
  return t === 1 ? 1 : 1 - Math.pow(2, -10 * t)
}

const easeOutCubic = (t: number): number => {
  return 1 - Math.pow(1 - t, 3)
}

const easeOutQuart = (t: number): number => {
  return 1 - Math.pow(1 - t, 4)
}

export function useCountUp(
  targetValue: Ref<number> | number,
  options: CountUpOptions = {}
) {
  const {
    duration = 2000,
    startValue = 0,
    easing = easeOutExpo,
    separator = ',',
    decimal = '.',
    decimals = 0,
    prefix = '',
    suffix = ''
  } = options

  const displayValue = ref(startValue)
  const formattedValue = ref(formatNumber(startValue))
  const isAnimating = ref(false)

  function formatNumber(num: number): string {
    const fixed = num.toFixed(decimals)
    const [int, dec] = fixed.split('.')

    // 添加千位分隔符
    const formattedInt = int.replace(/\B(?=(\d{3})+(?!\d))/g, separator)

    let result = formattedInt
    if (decimals > 0 && dec) {
      result += decimal + dec
    }

    return prefix + result + suffix
  }

  function animate(target: number) {
    const start = displayValue.value
    const startTime = performance.now()
    isAnimating.value = true

    function update(currentTime: number) {
      const elapsed = currentTime - startTime
      const progress = Math.min(elapsed / duration, 1)
      const easedProgress = easing(progress)

      displayValue.value = start + (target - start) * easedProgress
      formattedValue.value = formatNumber(displayValue.value)

      if (progress < 1) {
        requestAnimationFrame(update)
      } else {
        displayValue.value = target
        formattedValue.value = formatNumber(target)
        isAnimating.value = false
      }
    }

    requestAnimationFrame(update)
  }

  function start() {
    const target = typeof targetValue === 'number' ? targetValue : targetValue.value
    animate(target)
  }

  function reset() {
    displayValue.value = startValue
    formattedValue.value = formatNumber(startValue)
    isAnimating.value = false
  }

  // 监听目标值变化
  if (typeof targetValue !== 'number') {
    watch(targetValue, (newVal) => {
      if (newVal !== displayValue.value) {
        animate(newVal)
      }
    })
  }

  return {
    displayValue,
    formattedValue,
    isAnimating,
    start,
    reset
  }
}

// 导出缓动函数
export const easings = {
  easeOutExpo,
  easeOutCubic,
  easeOutQuart,
  linear: (t: number) => t,
  easeInOutQuad: (t: number) => t < 0.5 ? 2 * t * t : 1 - Math.pow(-2 * t + 2, 2) / 2
}
