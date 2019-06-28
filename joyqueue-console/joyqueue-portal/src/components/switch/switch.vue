<template>
  <span
    @click="toggle"
    @keydown.space="toggle">
    <span :class="switchClasses" :style="{width: width +'px'}">
      <span :class="innerClasses">
        <slot name="open" v-if="isChecked"></slot>
        <slot name="close" v-if="!isChecked"></slot>
      </span>
    </span>
  </span>
</template>

<script type="text/javascript">
import Config from '../../config'
import {oneOf} from '../../utils/assist'
import Emitter from '../../mixins/emitter'

const prefixCls = `${Config.clsPrefix}switch`
export default {
  name: `${Config.namePrefix}Switch`,
  mixins: [Emitter],
  props: {
    value: {
      type: Boolean,
      default: false
    },
    trueValue: {
      type: [String, Number, Boolean],
      default: true
    },
    falseValue: {
      type: [String, Number, Boolean],
      default: false
    },
    disabled: {
      type: Boolean,
      default: false
    },
    size: {
      validator (value) {
        return oneOf(value, ['large', 'small', 'default'])
      }
    },
    width: {
      type: String
    }
  },
  data () {
    return {
      isChecked: this.value
    }
  },
  computed: {
    switchClasses () {
      return [
        `${prefixCls}`,
        {
          [`${prefixCls}--disabled`]: this.disabled,
          [`${prefixCls}--checked`]: this.isChecked,
          [`${prefixCls}--${this.size}`]: !!this.size
        }
      ]
    },
    innerClasses () {
      return `${prefixCls}--inner`
    }
  },
  watch: {
    value (val) {
      if (val !== this.trueValue && val !== this.falseValue) {
        throw new Error('Value should be trueValue or falseValue.')
      }
      this.isChecked = val
    }
  },
  methods: {
    toggle (event) {
      event.preventDefault()
      if (this.disabled) return

      const checked = this.isChecked === this.trueValue ? this.falseValue : this.trueValue

      this.isChecked = checked
      this.$emit('input', checked)
      this.$emit('change', checked)
      this.dispatch(`${Config.namePrefix}FormItem`, 'on-form-change', checked)
    }
  }
}
</script>
