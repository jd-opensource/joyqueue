<template>
  <i :class="classes"
     :style="styles"
     @mouseover="handleMouseOver"
     @mousemove="handleMouseMove"
     @click="handleClick">
    <slot></slot>
  </i>
</template>
<script>
import Config from '../../config'
import { isColor } from '../../utils/assist';

const prefixCls = `${Config.clsPrefix}icon`;

export default {
  name: 'Icon',
  inheritAttrs: false,
  props: {
    name: String,
    size: [Number, String],
    color: {
      validator (value) {
        if (!value) return true;
        return isColor(value);
      }
    },
    custom: {
      type: Boolean,
      default: false
    }
  },
  computed: {
    classes () {
      if (this.custom) {
        return `icon-custom icon-custom-${this.name}`;
      }
      return `${prefixCls} ${prefixCls}-${this.name}`;
    },
    styles () {
      let style = {};

      if (this.size) {
        style['font-size'] = `${this.size}px`;
      }

      if (this.color) {
        style.color = this.color;
      }

      return style;
    }
  },
  methods: {
    handleClick (event) {
      this.$emit('click', event);
    },
    handleMouseOver (event) {
      this.$emit('mouseover', event);
    },
    handleMouseMove (event) {
      this.$emit('mousemove', event);
    }
  }
};
</script>
