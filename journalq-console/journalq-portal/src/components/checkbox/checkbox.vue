<template>
  <label :class="wrapClasses">
    <span :class="checkboxClasses">
      <span :class="innerClasses"></span>
       <input
        v-if="group"
        type="checkbox"
        :class="inputClasses"
        :disabled="checkboxDisabled"
        :value="label"
        v-model="model"
        :name="name"
        @change="change"
        @focus="onFocus"
        @blur="onBlur">
      <input
        v-if="!group"
        type="checkbox"
        :class="inputClasses"
        :disabled="checkboxDisabled"
        :checked="currentValue"
        :name="name"
        @change="change"
        @focus="onFocus"
        @blur="onBlur">
    </span>
    <span><slot></slot></span>
  </label>
</template>
<script>
import Config from '../../config'
import {findComponentUpward, oneOf} from '../../utils/assist'
import Emitter from '../../mixins/emitter'

const prefixCls = `${Config.clsPrefix}checkbox`
export default {
  name: `${Config.namePrefix}Checkbox`,
  mixins: [Emitter],
  inject: {
    duiForm: {
      default: ''
    },
    duiFormItem: {
      default: ''
    }
  },
  props: {
    disabled: {
      type: Boolean,
      default: false
    },
    name: {
      type: String
    },
    label: {
      type: [String, Number, Boolean]
    },
    value: {
      type: [String, Number, Boolean],
      default: false
    },
    size: {
      validator (value) {
        return oneOf(value, ['small', 'large', 'normal'])
      },
      default () {
        return 'normal'
      }
    },
    indeterminate: { // 设置 indeterminate 状态，只负责样式控制
      type: Boolean,
      default: false
    }
  },
  data () {
    return {
      model: [],
      currentValue: this.value,
      group: false,
      groupName: this.name,
      parent: findComponentUpward(this, `${Config.namePrefix}CheckboxGroup`),
      focusInner: false
    }
  },
  computed: {
    wrapClasses () {
      return [
        `${prefixCls}-wrapper`,
        {
          [`${prefixCls}-group-item`]: this.group,
          [`${prefixCls}-wrapper-checked`]: this.currentValue,
          [`${prefixCls}-wrapper-disabled`]: this.checkboxDisabled,
          [`${prefixCls}-${this.checkboxSize}`]: !!this.checkboxSize
        }
      ]
    },
    checkboxClasses () {
      return [
        `${prefixCls}`,
        {
          [`${prefixCls}-checked`]: this.currentValue,
          [`${prefixCls}-disabled`]: this.checkboxDisabled,
          [`${prefixCls}-indeterminate`]: this.indeterminate
        }
      ]
    },
    innerClasses () {
      return [
        `${prefixCls}-inner`,
        {
          [`${prefixCls}-focus`]: this.focusInner
        }
      ]
    },
    inputClasses () {
      return `${prefixCls}-input`
    },
    isGroup () {
      let parent = this.$parent
      while (parent) {
        if (parent.$options.componentName !== `${Config.namePrefix}CheckboxGroup`) {
          parent = parent.$parent
        } else {
          /* eslint-disable */
          this._checkboxGroup = parent;
          return true;
        }
      }
      return false;
    },
    _duiFormItemSize() {
      return (this.duiFormItem || {}).duiFormItemSize;
    },
    checkboxSize() {
      const temCheckboxSize = this.size || this._duiFormItemSize || (this.$DUI || {}).size;
      return this.isGroup
        ? this._checkboxGroup.checkboxGroupSize || temCheckboxSize
        : temCheckboxSize;
    },
    checkboxDisabled() {
      return this.isGroup
        ? this._checkboxGroup.disabled || this.disabled || (this.duiForm || {}).disabled
        : this.disabled || (this.duiForm || {}).disabled;
    }
  },
  mounted () {
    if (this.parent) {
      this.group = true;
    }
    if (this.group) {
      this.parent.updateValue();
    } else {
      this.updateValue();
    }
  },
  methods: {
    change (event) {
      if (this.checkboxDisabled) {
        return false;
      }
      const checked = event.target.checked;
      const value = checked; // ? this.label : null;
      if (this.group) {
        this.parent.change(this.model);
      } else {
        this.$emit('input', value);
        this.$emit('on-change', value, event);
        this.dispatch(`${Config.namePrefix}FormItem`, 'on-form-change', value, event);
      }
    },
    updateValue () {
      this.currentValue = this.value;
    },
    onBlur () {
      this.focusInner = false;
    },
    onFocus () {
      this.focusInner = true;
    }
  },
  watch: {
    value (val) {
      this.updateValue();
    }
  }
}
</script>
