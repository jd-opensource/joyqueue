<template>
  <div :class="wrapClasses">
    <slot></slot>
  </div>
</template>

<script>
import Config from '../../config'
import { oneOf } from '../../utils/assist'

const prefixCls = `${Config.clsPrefix}steps`
const availableStatus = ['wait', 'process', 'finish', 'error']
const availableSize = ['default', 'small'] // , 'large'
const availableDirection = ['horizontal', 'vertical']

export default {
  name: `${Config.namePrefix}Steps`,
  props: {
    current: {
      type: Number,
      default: 0,
      validator: val => val >= 0
    },
    status: {
      type: String,
      default: 'process',
      validator (value) {
        return oneOf(value, availableStatus)
      }
    },
    size: {
      type: String,
      default: 'default',
      validator (value) {
        return oneOf(value, availableSize)
      }
    },
    direction: {
      type: String,
      default: 'horizontal',
      validator (value) {
        return oneOf(value, availableDirection)
      }
    }
  },
  data () {
    return {
      steps: []
    }
  },
  computed: {
    wrapClasses () {
      return [
        `${prefixCls}`,
        {
          [`${prefixCls}--${this.direction}`]: !!this.direction,
          [`${prefixCls}--${this.size}`]: !!this.size
        }
      ]
    }
  },
  methods: {
    updateStepsStatus () {
      const current = this.current
      const status = this.status

      this.steps.forEach((child, index) => {
        const prevChild = this.steps[index - 1]

        if (index === current) {
          if (status === 'error') {
            child.internalStatus = 'error'
            prevChild && (prevChild.nextError = true)
          } else {
            child.internalStatus = 'process'
          }
        } else if (index < current) {
          child.internalStatus = 'finish'
        } else {
          child.internalStatus = 'wait'
        }

        if (child.finalStatus !== 'error' && prevChild) {
          prevChild.nextError = false
        }
      })
    }
  },
  watch: {
    current () {
      this.updateStepsStatus()
    },
    steps (steps) {
      steps.forEach((step, index) => {
        step.index = index
      })
      this.updateStepsStatus()
    }
  },
  mounted () {
    this.updateStepsStatus()
  }
}
</script>
