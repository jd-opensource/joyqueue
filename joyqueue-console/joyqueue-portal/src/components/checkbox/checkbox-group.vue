<template>
  <div :class="classes" :name="name">
    <slot></slot>
  </div>
</template>

<script>
import Config from '../../config'
import {findComponentsDownward, oneOf} from '../../utils/assist.js'
import Emitter from '../../mixins/emitter'
const prefixCls = `${Config.clsPrefix}checkbox-group`
export default {
  name: `${Config.namePrefix}CheckboxGroup`,
  componentName: `${Config.namePrefix}CheckboxGroup`,
  mixins: [ Emitter ],
  inject: {
    duiFormItem: {
      default: ''
    }
  },
  props: {
    name: {
      type: String
    },
    value: {
      type: Array,
      default () {
        return []
      }
    },
    vertical: {
      type: Boolean,
      default: false
    },
    size: {
      validator (value) {
        return oneOf(value, ['small', 'large', 'normal'])
      },
      default () {
        return 'normal'
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
          [`${prefixCls}-${this.checkboxGroupSize}`]: !!this.checkboxGroupSize,
          [`lb-checkbox-${this.checkboxGroupSize}`]: !!this.checkboxGroupSize,
          [`${prefixCls}-vertical`]: this.vertical
        }
      ]
    },
    _duiFormItemSize () {
      return (this.duiFormItem || {}).duiFormItemSize
    },
    checkboxGroupSize () {
      return this.size || this._duiFormItemSize || (this.$DUI || {}).size
    }
  },
  mounted () {
    this.updateValue()
  },
  methods: {
    updateValue () {
      this.childrens = findComponentsDownward(this, `${Config.namePrefix}Checkbox`)
      if (this.childrens) {
        this.childrens.forEach(child => {
          const { value } = this
          this.childrens.forEach(child => {
            child.model = value
            child.currentValue = value.indexOf(child.label) >= 0
            child.group = true
          })
        })
      }
    },
    change (data) {
      this.currentValue = data
      this.$emit('input', data)
      this.$emit('on-change', data)
      this.dispatch(`${Config.namePrefix}FormItem`, 'on-form-change', data)
    }
  },
  watch: {
    value () {
      this.updateValue()
    }
  }
}
</script>
