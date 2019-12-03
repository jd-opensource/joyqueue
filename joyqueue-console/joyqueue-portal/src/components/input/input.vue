<template>
  <div :class="wrapClasses">
    <template v-if="type !== 'textarea'">
      <div v-if="$slots.prepend" :class="[prefixCls + '-group-prepend']">
        <slot name="prepend"></slot>
      </div>
      <span v-if="$slots.prefix" :class="[prefixCls + '-prefix-span']">
        <slot name="prefix"></slot>
      </span>
      <input ref="input"
             v-bind="$attrs"
             :type="type"
             :class="inputClasses"
             :placeholder="placeholder"
             :value="currentValue"
             :disabled="inputDisabled"
             :maxlength="maxlength"
             :readonly="readonly"
             @keyup.enter="handleEnter"
             @keyup="handleKeyup"
             @keypress="handleKeypress"
             @keydown="handleKeydown"
             @focus="handleFocus"
             @blur="handleBlur"
             @input="handleInput"
             @change="handleChange">
      <div v-if="$slots.append" :class="[prefixCls + '-group-append']">
        <slot name="append"></slot>
      </div>
      <div :class="[prefixCls + '-suffix-span']"
           v-if="$slots.suffix || (validateState && needStatusIcon)">
        <slot name="suffix"></slot>
        <Icon v-if="validateState && needStatusIcon" :name="validateIcon" :class="[prefixCls + '__validateIcon']"></Icon>
      </div>
    </template>
    <textarea v-else
              ref="textarea"
              v-bind="$attrs"
              :class="textareaClasses"
              :style="textareaStyle"
              :value="currentValue"
              :placeholder="placeholder"
              :maxlength="maxlength"
              :readonly="readonly"
              :disabled="disabled"
              @keyup.enter="handleEnter"
              @keyup="handleKeyup"
              @keypress="handleKeypress"
              @keydown="handleKeydown"
              @focus="handleFocus"
              @blur="handleBlur"
              @input="handleInput">
    </textarea>
  </div>
</template>

<script type="text/javascript">
import Config from '../../config'
import Icon from '../icon'
import {oneOf, findComponentUpward} from '../../utils/assist'
import calcTextareaHeight from '../../utils/calcTextareaHeight'
import Emitter from '../../mixins/emitter'

const prefixCls = `${Config.clsPrefix}input`

export default {
  name: `${Config.namePrefix}Input`,
  components: {Icon},
  mixins: [Emitter],
  inheritAttrs: false,
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
      validator (value) {
        return oneOf(value, ['text', 'textarea', 'password', 'url', 'email', 'date'])
      },
      default: 'text'
    },
    value: {
      type: [String, Number],
      default: ''
    },
    size: {
      validator (value) {
        return oneOf(value, ['small', 'large', 'default'])
      },
      default () {
        return this.$DUI.size === '' ? 'default' : this.$DUI.size
      }
    },
    disabled: {
      type: Boolean,
      default: false
    },
    placeholder: {
      type: String
    },
    maxlength: {
      type: Number
    },
    autosize: {
      type: [Boolean, Object],
      default: false
    },
    state: {
      type: String
    },
    readonly: {
      type: Boolean,
      default: false
    },
    icon: {
      type: String
    }
  },
  data () {
    return {
      currentValue: this.value,
      prefixCls: prefixCls,
      textareaCalcStyle: {}
    }
  },
  computed: {
    wrapClasses () {
      return [
        `${prefixCls}`,
        {
          [`${prefixCls}-group`]: (this.$slots.prepend || this.$slots.append),
          [`${prefixCls}-suffix`]: this.$slots.suffix,
          [`${prefixCls}-prefix`]: this.$slots.prefix,
          [`${prefixCls}-append`]: this.$slots.append,
          [`${prefixCls}-prepend`]: this.$slots.prepend
        }
      ]
    },
    inputClasses () {
      return [
        `${prefixCls}-base`,
        {
          [`${prefixCls}-${this.inputSize}`]: !!this.inputSize,
          [`${prefixCls}-${this.state}`]: !!this.state,
          [`${prefixCls}-disabled`]: this.inputDisabled
        }
      ]
    },
    textareaClasses () {
      return [
        `${prefixCls}-base`,
        {
          [`${prefixCls}-disabled`]: this.inputDisabled
        }
      ]
    },
    textareaStyle () {
      return this.textareaCalcStyle
    },
    _duiFormItemSize () {
      return (this.duiFormItem || {}).duiFormItemSize
    },
    validateState () {
      return this.duiFormItem ? this.duiFormItem.validateState : ''
    },
    needStatusIcon () {
      return this.duiForm ? this.duiForm.statusIcon : false
    },
    validateIcon () {
      return {
        validating: 'loader',
        success: 'check-circle',
        error: 'x-circle'
      }[this.validateState]
    },
    inputSize () {
      return this.size || this._duiFormItemSize || (this.$DUI || {}).size
    },
    inputDisabled () {
      return this.disabled || (this.duiForm || {}).disabled
    }
  },
  methods: {
    handleEnter (event) {
      this.$emit('on-enter', event)
      if (this.search) this.$emit('on-search', this.currentValue)
    },
    handleKeydown (event) {
      this.$emit('on-keydown', event)
    },
    handleKeypress (event) {
      this.$emit('on-keypress', event)
    },
    handleKeyup (event) {
      this.$emit('on-keyup', event)
    },
    handleFocus (event) {
      this.$emit('on-focus', event)
    },
    handleBlur (event) {
      this.$emit('on-blur', event)
      if (!findComponentUpward(this, ['DatePicker', 'TimePicker', 'Cascader', 'Search'])) {
        this.dispatch(`${Config.namePrefix}FormItem`, `${Config.localePrefix}.form.blur`, this.currentValue)
      }
    },
    handleInput (event) {
      let value = event.target.value
      if (this.number) value = Number.isNaN(Number(value)) ? value : Number(value)
      this.$emit('input', value)
      this.setCurrentValue(value)
      this.$emit('on-change', event)
    },
    handleChange (event) {
      this.$emit('on-input-change', event)
    },
    setCurrentValue (value) {
      if (value === this.currentValue) return
      this.$nextTick(() => {
        this.resizeTextarea()
      })
      this.currentValue = value
      if (!findComponentUpward(this, ['DatePicker', 'TimePicker', 'Cascader', 'Search'])) {
        this.dispatch(`${Config.namePrefix}FormItem`, `${Config.localePrefix}.form.change`, value)
      }
    },
    resizeTextarea () {
      if (this.$isServer) return
      const { autosize, type } = this
      if (type !== 'textarea') return
      if (!autosize) {
        this.textareaCalcStyle = {
          minHeight: calcTextareaHeight(this.$refs.textarea).minHeight
        }
        return
      }
      const minRows = autosize.minRows
      const maxRows = autosize.maxRows

      this.textareaCalcStyle = calcTextareaHeight(this.$refs.textarea, minRows, maxRows)
    },
    focus () {
      if (this.type === 'textarea') {
        this.$refs.textarea.focus()
      } else {
        this.$refs.input.focus()
      }
    },
    blur () {
      if (this.type === 'textarea') {
        this.$refs.textarea.blur()
      } else {
        this.$refs.input.blur()
      }
    }
  },
  watch: {
    value (val) {
      this.setCurrentValue(val)
    }
  },
  mounted () {
    // console.log('$attrs:' + JSON.stringify(this.$attrs))
    this.resizeTextarea()
  }
}
</script>
