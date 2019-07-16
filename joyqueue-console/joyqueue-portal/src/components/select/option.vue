<template>
  <li
    :class="classes"
    @click.stop="select"
    @touchend.stop="select"
    @mousedown.prevent
    @touchstart.prevent
  ><slot>{{ showLabel }}</slot></li>
</template>
<script>
import Config from '../../config'
import Emitter from '../../mixins/emitter'
import { findComponentUpward } from '../../utils/assist'

const prefixCls = `${Config.clsPrefix}select-item`

export default {
  name: `${Config.namePrefix}Option`,
  componentName: 'select-item',
  mixins: [ Emitter ],
  props: {
    value: {
      type: [String, Number],
      required: true
    },
    label: {
      type: [String, Number]
    },
    disabled: {
      type: Boolean,
      default: false
    },
    selected: {
      type: Boolean,
      default: false
    },
    isFocused: {
      type: Boolean,
      default: false
    }
  },
  data () {
    return {
      searchLabel: '', // the slot value (textContent)
      autoComplete: false
    }
  },
  computed: {
    classes () {
      return [
        `${prefixCls}`,
        {
          [`${prefixCls}-disabled`]: this.disabled,
          [`${prefixCls}-selected`]: this.selected && !this.autoComplete,
          [`${prefixCls}-focus`]: this.isFocused
        }
      ]
    },
    showLabel () {
      return (this.label) ? this.label : this.value
    },
    optionLabel () {
      return this.label || (this.$el && this.$el.textContent)
    }
  },
  methods: {
    select () {
      if (this.disabled) return false

      this.dispatch(`${Config.namePrefix}Select`, 'on-select-selected', {
        value: this.value,
        label: this.optionLabel
      })
      this.$emit('on-select-selected', {
        value: this.value,
        label: this.optionLabel
      })
    }
  },
  mounted () {
    const Select = findComponentUpward(this, `${Config.namePrefix}Select`)
    if (Select) this.autoComplete = Select.autoComplete
  }
}
</script>
