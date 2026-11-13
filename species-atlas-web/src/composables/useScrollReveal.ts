import { ref, onMounted, onUnmounted, type Ref } from 'vue'

interface ScrollRevealOptions {
  threshold?: number
  rootMargin?: string
  once?: boolean
}

export function useScrollReveal(
  elementRef: Ref<HTMLElement | undefined>,
  options: ScrollRevealOptions = {}
) {
  const {
    threshold = 0.1,
    rootMargin = '0px',
    once = true
  } = options

  const isVisible = ref(false)
  let observer: IntersectionObserver | null = null

  onMounted(() => {
    if (!elementRef.value) return

    observer = new IntersectionObserver(
      (entries) => {
        entries.forEach((entry) => {
          if (entry.isIntersecting) {
            isVisible.value = true
            if (once && observer) {
              observer.disconnect()
            }
          } else if (!once) {
            isVisible.value = false
          }
        })
      },
      {
        threshold,
        rootMargin
      }
    )

    observer.observe(elementRef.value)
  })

  onUnmounted(() => {
    if (observer) {
      observer.disconnect()
    }
  })

  return { isVisible }
}

// 多元素批量观察
export function useScrollRevealMultiple(
  containerRef: Ref<HTMLElement | undefined>,
  selector: string = '.reveal',
  options: ScrollRevealOptions = {}
) {
  const {
    threshold = 0.1,
    rootMargin = '0px 0px -50px 0px',
    once = true
  } = options

  const visibleItems = ref<Set<Element>>(new Set())
  let observer: IntersectionObserver | null = null

  onMounted(() => {
    if (!containerRef.value) return

    observer = new IntersectionObserver(
      (entries) => {
        entries.forEach((entry) => {
          if (entry.isIntersecting) {
            entry.target.classList.add('visible')
            visibleItems.value.add(entry.target)
            if (once && observer) {
              observer.unobserve(entry.target)
            }
          } else if (!once) {
            entry.target.classList.remove('visible')
            visibleItems.value.delete(entry.target)
          }
        })
      },
      {
        threshold,
        rootMargin
      }
    )

    const elements = containerRef.value.querySelectorAll(selector)
    elements.forEach((el) => observer?.observe(el))
  })

  onUnmounted(() => {
    if (observer) {
      observer.disconnect()
    }
  })

  return { visibleItems }
}
