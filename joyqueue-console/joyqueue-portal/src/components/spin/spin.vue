<template>
    <transition name="fade">
        <div :class="classes" v-if="fullscreenVisible">
            <div :class="mainClasses">
                <div :class="dotClasses">
                  <svg class="circular" viewBox="25 25 50 50">
                    <circle class="path" cx="50" cy="50" r="20" fill="none"/>
                  </svg>
                </div>
                <div :class="textClasses"><slot></slot></div>
            </div>
        </div>
    </transition>
</template>
<script>
import Config from '../../config'
import { oneOf } from '../../utils/assist'
import ScrollbarMixins from '../../mixins/scrollbar'

const prefixCls = `${Config.clsPrefix}spin`

export default {
  name: `${Config.namePrefix}Spin`,
  mixins: [ ScrollbarMixins ],
  props: {
    size: {
      validator (value) {
        return oneOf(value, ['small', 'large'])
      }
    },
    fix: {
      type: Boolean,
      default: false
    },
    fullscreen: {
      type: Boolean,
      default: false
    }
  },
  data () {
    return {
      showText: false,
      visible: false
    }
  },
  computed: {
    classes () {
      return [
        `${prefixCls}`,
        {
          [`${prefixCls}-${this.size}`]: !!this.size,
          [`${prefixCls}-fix`]: this.fix,
          [`${prefixCls}-show-text`]: this.showText,
          [`${prefixCls}-fullscreen`]: this.fullscreen
        }
      ]
    },
    mainClasses () {
      return `${prefixCls}-main`
    },
    dotClasses () {
      return `${prefixCls}-dot`
    },
    textClasses () {
      return `${prefixCls}-text`
    },
    fullscreenVisible () {
      if (this.fullscreen) {
        return this.visible
      } else {
        return true
      }
    }
  },
  watch: {
    visible (val) {
      if (val) {
        this.addScrollEffect()
      } else {
        this.removeScrollEffect()
      }
    }
  },
  mounted () {
    this.showText = this.$slots.default !== undefined
  }
}
</script>
