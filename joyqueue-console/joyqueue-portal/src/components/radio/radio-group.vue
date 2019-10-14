<template>
  <div :class="classes" :name="name">
    <slot></slot>
  </div>
</template>

<script>
import Config from '../../config'
import {findComponentsDownward, oneOf} from '../../utils/assist.js'
import Emitter from '../../mixins/emitter'
const prefixCls = `${Config.clsPrefix}radio-group`
let seed = 0
const now = Date.now()
const getUuid = () => `lbRadioGroup_${now}_${seed++}`
export default {
  name: `${Config.namePrefix}RadioGroup`,
  componentName: `${Config.namePrefix}RadioGroup`,
  mixins: [ Emitter ],
  inject: {
    duiFormItem: {
      default: ''
    }
  },
  props: {
    name: {
      type: String,
      default: getUuid
    },
    value: {
      type: [String, Number, Boolean],
      default: ''
    },
    vertical: {
      type: Boolean,
      default: false
    },
    type: {
      validator (value) {
        return oneOf(value, ['button'])
      }
    },
    size: {
      validator (value) {
        return oneOf(value, ['small', 'large', 'default'])
      },
      default () {
        return 'default'
      }
    }
  },
  data () {
    return {
      currentValue: this.value,
      children: []
    }
  },
  computed: {
    classes () {
      return [
        `${prefixCls}`,
        {
          [`${prefixCls}-${this.radioGroupSize}`]: !!this.radioGroupSize,
          [`lb-radio-${this.radioGroupSize}`]: !!this.radioGroupSize,
          [`${prefixCls}-${this.type}`]: !!this.type,
          [`${prefixCls}-vertical`]: this.vertical
        }
      ]
    },
    _duiFormItemSize () {
      return (this.duiFormItem || {}).duiFormItemSize
    },
    radioGroupSize () {
      return this.size || this._duiFormItemSize || (this.$DUI || {}).size
    }
  },
  mounted () {
    this.updateValue()
  },
  methods: {
    updateValue () {
      this.childrens = findComponentsDownward(this, `${Config.namePrefix}Radio`)
      if (this.childrens) {
        this.childrens.forEach(child => {
          child.currentValue = this.currentValue === child.label
          child.group = true
        })
      }
    },
    change (data) {
      this.currentValue = data.value
      this.updateValue()
      this.$emit('input', data.value)
      this.$emit('on-change', data.value)
      this.dispatch(`${Config.namePrefix}FormItem`, 'on-form-change', data.value)
    }
  },
  watch: {
    value () {
      if (this.currentValue !== this.value) {
        this.currentValue = this.value
        this.$nextTick(() => {
          this.updateValue()
        })
      }
    }
  }
}
</script>
