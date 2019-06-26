<template>
  <div
    :class="stepStatusClass">
    <!--:style="stepStyle">-->
    <div v-if="!isLastStep" :class="[prefixCls + '__line']"></div>
    <div :class="[prefixCls + '__head']">
      <div :class="stepIconClass">
        <div v-if="icon">
          <Icon :name="icon"></Icon>
        </div>
        <template v-else>
          <div v-if="['process', 'wait'].indexOf(finalStatus) > -1"
               :class="[prefixCls + '__order']">
            {{ index + 1 }}
          </div>
          <div v-if="finalStatus === 'finish'">
            <Icon name="check"></Icon>
          </div>
          <div v-if="finalStatus === 'error'">
            <Icon name="x"></Icon>
          </div>
        </template>
      </div>
    </div>
    <div :class="[prefixCls + '__main']">
      <div :class="[prefixCls + '__title']">{{ title }}</div>
      <div :class="[prefixCls + '__description']" v-if="description">{{ description }}</div>
    </div>
    <i class="triangle-right-bg"></i>
    <i class="triangle-right"></i>
  </div>
</template>

<script>
import Config from '../../config'
import Icon from '../icon/icon.vue'
import { oneOf } from '../../utils/assist'

const prefixCls = `${Config.clsPrefix}step`
const availableStatus = ['wait', 'process', 'finish', 'error']

export default {
  name: `${Config.namePrefix}Step`,
  components: { Icon },
  props: {
    title: String,
    icon: String,
    description: String,
    status: {
      type: String,
      validator (value) {
        return oneOf(value, availableStatus)
      }
    }
  },
  data () {
    return {
      index: -1,
      internalStatus: '',
      nextError: false,
      prefixCls: prefixCls
    }
  },
  computed: {
    stepStyle () {
      const style = {}
      if (this.$parent.direction !== 'vertical') {
        style.width = `${1 / this.stepsTotal * 100}%`
      }
      return style
    },
    stepsTotal () {
      return this.$parent.steps.length
    },
    finalStatus () {
      return this.status || this.internalStatus
    },
    isLastStep () {
      return this.index === this.stepsTotal - 1
    },
    stepStatusClass () {
      const className = {}

      if (oneOf(this.finalStatus, availableStatus)) {
        className[`${prefixCls}--${this.finalStatus}`] = true
      }

      if (this.nextError) {
        className[`${prefixCls}--next-error`] = true
      }

      return [`${prefixCls}`, className]
    },
    stepIconClass () {
      return [
        `${prefixCls}__label`,
        {
          [`${prefixCls}__icon`]: !!this.icon
        }
      ]
    }
  },
  beforeCreate () {
    this.$parent.steps.push(this)
  },
  beforeDestroy () {
    const steps = this.$parent.steps
    const index = steps.indexOf(this)

    if (index >= 0) {
      steps.splice(index, 1)
    }
  }
}
</script>
