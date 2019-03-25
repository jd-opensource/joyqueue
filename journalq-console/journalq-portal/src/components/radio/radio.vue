<template>
  <label :class="wrapClasses">
    <span :class="radioClasses">
      <span :class="innerClasses"></span>
      <input type="radio"
        :class="inputClasses"
        :disabled="radioDisabled"
        :name="groupName"
        :checked="currentValue"
        @change="change"
        @focus="onFocus"
        @blur="onBlur"
      />
    </span>
    <span><slot>{{ label }}</slot></span>
  </label>
</template>

<script type="text/javascript">
import Config from '../../config'
import {findComponentUpward, oneOf} from '../../utils/assist'
import Emitter from '../../mixins/emitter'
const prefixCls = `${Config.clsPrefix}radio`
export default {
  name: `${Config.namePrefix}Radio`,
  mixins: [ Emitter ],
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
    label: { // 如果没有slot内容，会选用传递的label值
      type: [String, Number, Boolean]
    },
    value: {
      type: [String, Number, Boolean],
      default: false
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
      currentValue: false,
      group: false,
      groupName: this.name,
      parent: findComponentUpward(this, `${Config.namePrefix}RadioGroup`),
      focusWrapper: false,
      focusInner: false
    }
  },
  computed: {
    wrapClasses () {
      return [
        `${prefixCls}-wrapper`,
        {
          [`${prefixCls}-group-item`]: this.isGroup,
          [`${prefixCls}-wrapper-checked`]: this.currentValue,
          [`${prefixCls}-wrapper-disabled`]: this.radioDisabled,
          [`${prefixCls}-${this.radioSize}`]: !!this.radioSize,
          [`${prefixCls}-focus`]: this.focusWrapper
        }
      ]
    },
    radioClasses () {
      return [
        `${prefixCls}`,
        {
          [`${prefixCls}-checked`]: this.currentValue,
          [`${prefixCls}-disabled`]: this.radioDisabled
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
        if (parent.$options.componentName !== `${Config.namePrefix}RadioGroup`) {
          parent = parent.$parent
        } else {
          /* eslint-disable */
          this._radioGroup = parent;
          return true;
        }
      }
      return false;
    },
    _duiFormItemSize() {
      return (this.duiFormItem || {}).duiFormItemSize;
    },
    radioSize() {
      const temRadioSize = this.size || this._duiFormItemSize || (this.$DUI || {}).size;
      return this.isGroup
        ? this._radioGroup.radioGroupSize || temRadioSize
        : temRadioSize;
    },
    radioDisabled() {
      return this.isGroup
        ? this._radioGroup.disabled || this.disabled || (this.duiForm || {}).disabled
        : this.disabled || (this.duiForm || {}).disabled;
    }
  },
  mounted () {
    if (this.parent) {
      this.group = true;
      if (this.name && this.name !== this.parent.name) {
        /* eslint-disable no-console */
        if (console.warn) {
          console.warn('[luban] Name does not match Radio Group name.');
        }
        /* eslint-enable no-console */
      }
    }
    if (this.group) {
      this.parent.updateValue();
    } else {
      this.updateValue();
    }
  },
  methods: {
    change (ev) {
      if (this.radioDisabled) {
        return false;
      }
      const checked = ev.target.checked;
      const value = checked ? this.label : null; // value不可以直接修改，需要修改传递过来的value
      if (this.group) {
        this.parent.change({
          value: this.label
        });
      } else {
        this.$emit('input', value);
        this.$emit('on-change', value);
        this.dispatch(`${Config.namePrefix}FormItem`, 'on-form-change', value);
      }
    },
    updateValue () {
      this.currentValue = this.value === this.label
    },
    onBlur () {
      this.focusWrapper = false;
      this.focusInner = false;
    },
    onFocus () {
      if (this.group && this.parent.type === 'button') {
        this.focusWrapper = true;
      } else {
        this.focusInner = true;
      }
    }
  },
  watch: {
    value (val) {
      this.updateValue();
    }
  }
}
</script>
