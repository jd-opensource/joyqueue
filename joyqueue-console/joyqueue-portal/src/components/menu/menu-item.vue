<template>
  <a
    v-if="to"
    :href="linkUrl"
    :target="target"
    :class="classes"
    @click.exact="handleClickItem($event, false)"
    @click.ctrl="handleClickItem($event, true)"
    @click.meta="handleClickItem($event, true)"
    :style="itemStyle">
    <icon v-if="icon" :name="icon" :color="color"></icon><slot></slot>
  </a>
  <li v-else :class="classes" @click.stop="handleClickItem" :style="itemStyle">
    <icon v-if="icon" :name="icon" :color="color"></icon><slot></slot>
  </li>
</template>
<script>
import Config from '../../config'
import Emitter from '../../mixins/emitter'
import { isColor, findComponentUpward } from '../../utils/assist'
import mixin from './mixin'
import mixinsLink from '../../mixins/link'
const prefixCls = `${Config.clsPrefix}menu`
export default {
  name: `${Config.namePrefix}MenuItem`,
  mixins: [ Emitter, mixin, mixinsLink ],
  props: {
    name: {
      type: [String, Number],
      required: true
    },
    icon: String,
    color: {
      validator (value) {
        if (!value) return true
        return isColor(value)
      }
    },
    disabled: {
      type: Boolean,
      default: false
    }
  },
  data () {
    return {
      active: false
    }
  },
  computed: {
    classes () {
      return [
        `${prefixCls}-item`,
        {
          [`${prefixCls}-item-active`]: this.active,
          [`${prefixCls}-item-selected`]: this.active,
          [`${prefixCls}-item-disabled`]: this.disabled
        }
      ]
    },
    itemStyle () {
      const selectedColor = this.icon && this.color && this.active ? {color: this.color} : {}
      return this.hasParentSubmenu && this.mode !== 'horizontal' ? {
        paddingLeft: 43 + (this.parentSubmenuNum - 1) * 24 + 'px',
        ...selectedColor
      } : selectedColor
    }
  },
  methods: {
    handleClickItem (event, newWindow = false) {
      if (this.disabled) return

      if (newWindow) {
        // 如果是 newWindow，直接新开窗口就行，无需发送状态
        this.handleCheckClick(event, newWindow)
      } else {
        let parent = findComponentUpward(this, `${Config.namePrefix}Submenu`)

        if (parent) {
          this.dispatch(`${Config.namePrefix}Submenu`, 'on-menu-item-select', this.name)
        } else {
          this.dispatch(`${Config.namePrefix}Menu`, 'on-menu-item-select', this.name)
        }
        this.handleCheckClick(event, newWindow)
      }
    }
  },
  mounted () {
    this.$on('on-update-active-name', (name) => {
      if (this.name === name) {
        this.active = true
        this.dispatch(`${Config.namePrefix}Submenu`, 'on-update-active-name', name)
      } else {
        this.active = false
      }
    })
  }
}
</script>
