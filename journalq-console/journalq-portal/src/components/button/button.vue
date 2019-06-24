<template>
  <button
    :type="nativeType"
    :class="classes"
    @click="handleClick"
    :disabled="buttonDisabled || loading"
    :autofocus="autofocus"
    :style="styles"
  >
    <Icon name="loader" :class="loadingClass" v-if="loading"></Icon>
    <Icon :name="icon" :class="iconClass" v-if="icon"></Icon>
    <span :class="textClass" v-if="showSlot"><slot></slot></span>
  </button>
</template>
<script>
import Config from '../../config'
import Icon from '../icon'
import { oneOf, isColor } from '../../utils/assist'

const prefixCls = `${Config.clsPrefix}btn`

export default {
  name: `${Config.namePrefix}Button`,
  components: { Icon },
  inject: {
    duiForm: {
      default: ''
    },
    duiFormItem: {
      default: ''
    }
  },
  props: {
    type: {
      default: 'default',
      validator (value) {
        return oneOf(value, ['primary', 'default', 'dashed', 'borderless', 'ghost'])
      }
    },
    color: {
      validator (value) {
        return oneOf(value, ['primary', 'success', 'warning', 'danger', 'info']) || isColor(value)
      }
    },
    size: {
      default: 'default',
      validator (value) {
        return oneOf(value, ['large', 'default', 'small'])
      }
    },
    nativeType: {
      default: 'button',
      validator (value) {
        return oneOf(value, ['button', 'submit', 'reset'])
      }
    },
    icon: {
      type: String,
      default: ''
    },
    disabled: {
      type: Boolean,
      default: false
    },
    autofocus: {
      type: Boolean,
      default: false
    },
    loading: {
      type: Boolean,
      default: false
    },
    round: {
      type: Boolean,
      default: false
    },
    circle: {
      type: Boolean,
      default: false
    },
    long: {
      type: Boolean,
      default: false
    },
    className: {
      type: String
    }
  },
  computed: {
    styles () {
      let styles = {}
      if (isColor(this.color)) {
        styles.borderColor = this.color
        styles.backgroundColor = this.color
      }
      return styles
    },
    classes () {
      return [
        `${prefixCls}`,
        {
          [`${prefixCls}--${this.type}`]: !!this.type,
          [`${prefixCls}--${this.type}--${this.color}`]: oneOf(this.color, ['primary', 'success', 'warning', 'danger', 'info']),
          [`${prefixCls}--${this.buttonSize}`]: !!this.buttonSize,
          [`${prefixCls}--${this.$parent.size}`]: !!this.$parent.size,
          [`${prefixCls}--round`]: this.round,
          [`${prefixCls}--circle`]: this.circle && !this.showSlot,
          [`${prefixCls}--long`]: !!this.long,
          [`${this.className}`]: !!this.className
        }
      ]
    },
    loadingClass () {
      return `${prefixCls}__loading`
    },
    iconClass () {
      return `${prefixCls}__icon`
    },
    textClass () {
      return `${prefixCls}__text`
    },
    showSlot () {
      return this.$slots.default
    },
    _duiFormItemSize () {
      return (this.duiFormItem || {}).duiFormItemSize
    },
    buttonSize () {
      return this.size || this._duiFormItemSize || (this.$DUI || {}).size
    },
    buttonDisabled () {
      return this.disabled || (this.duiForm || {}).disabled
    }
  },

  methods: {
    handleClick (event) {
      this.$emit('click', event)
    }
  }
}
</script>
