<template>
  <transition name="fade">
    <div :class="classes" :style="outerStyles" v-show="show">
      <div :class="innerClasses">
        <div :class="innerCircleLoadingPercent" v-if="showPercent">{{percent}} % </div>
        <div :class="innerCircleLoadingPercent" v-if="!showPercent">{{loadingText}}</div>
        <div :class="innerCircleLoading"></div>
      </div>
    </div>
  </transition>
</template>

<script>
import Config from '../../config'
import locale from '../../mixins/locale'
const prefixCls = `${Config.clsPrefix}loading`
const localePrefix = `${Config.localePrefix}.loading.`
export default {
  name: `${Config.namePrefix}Loading`,
  props: {
    width: {
      type: [Number, String],
      default: '100%'
    },
    height: {
      type: [Number, String],
      default: '100%'
    },
    top: {
      type: Number,
      default: 0
    },
    showPercent: {
      type: Boolean,
      default: false
    }
  },
  data () {
    return {
      percent: 0,
      show: false
    }
  },
  mixins: [ locale ],
  computed: {
    loadingText () {
      return this.t(localePrefix + 'loadingText')
    },
    classes () {
      return `${prefixCls}`
    },
    outerStyles () {
      return {
        width: this.width.toString().indexOf('%') !== -1 ? this.width : `${this.width}px`,
        height: this.height.toString().indexOf('%') !== -1 ? this.height : `${this.height}px`,
        top: `${this.top}px`
      }
    },
    innerClasses () {
      return [
        `${prefixCls}-inner`,
        `${prefixCls}-inner-circle`
      ]
    },
    innerCircleLoading () {
      return [
        `${prefixCls}-inner-circle-loading`
      ]
    },
    innerCircleLoadingPercent () {
      return [
        `${prefixCls}-inner-circle-loading-percent`
      ]
    }
  }
}
</script>
