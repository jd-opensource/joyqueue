<template>
  <li :class="classes" @click="handleClick"><slot></slot></li>
</template>
<script>
import Config from '../../config'
import { findComponentUpward } from '../../utils/assist'
const prefixCls = `${Config.clsPrefix}dropdown-menu-item`
export default {
  name: `${Config.namePrefix}DropdownItem`,
  props: {
    name: {
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
    divided: {
      type: Boolean,
      default: false
    },
    command: {}
  },
  computed: {
    classes () {
      return [
        `${prefixCls}`,
        {
          [`${prefixCls}-disabled`]: this.disabled,
          [`${prefixCls}-selected`]: this.selected,
          [`${prefixCls}-divided`]: this.divided
        }
      ]
    }
  },
  methods: {
    handleClick () {
      const $parent = findComponentUpward(this, `${Config.namePrefix}Dropdown`)
      const hasChildren = this.$parent && this.$parent.$options.name === `${Config.namePrefix}Dropdown`

      if (this.disabled) {
        this.$nextTick(() => {
          $parent.currentVisible = true
        })
      } else if (hasChildren) {
        this.$parent.$emit('on-haschild-click')
      } else {
        if ($parent && $parent.$options.name === `${Config.namePrefix}Dropdown`) {
          $parent.$emit('on-hover-click')
        }
      }
      if (!this.disabled) {
        $parent.$emit('on-click', this.command)
      }
    }
  }
}
</script>
