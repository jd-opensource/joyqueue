<template>
  <div :class="classes">
    <slot></slot>
  </div>
</template>
<script>
import Config from '../../config'
import {oneOf} from '../../utils/assist'

const prefixCls = `${Config.clsPrefix}btn-group`

export default {
  name: `${Config.namePrefix}ButtonGroup`,
  props: {
    size: {
      validator (value) {
        return oneOf(value, ['small', 'large', 'default'])
      }
    },
    shape: {
      validator (value) {
        return oneOf(value, ['circle', 'circle-outline'])
      }
    },
    vertical: {
      type: Boolean,
      default: false
    }
  },
  computed: {
    classes () {
      return [
        `${prefixCls}`,
        {
          [`${prefixCls}-${this.size}`]: !!this.size,
          [`${prefixCls}-${this.shape}`]: !!this.shape,
          [`${prefixCls}-vertical`]: this.vertical
        }
      ]
    }
  }
}
</script>
