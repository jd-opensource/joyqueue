<template>
  <li :class="classes" @mouseenter="handleMouseenter" @mouseleave="handleMouseleave">
    <div :class="[prefixCls + '-submenu-title']" ref="reference" @click.stop="handleClick" :style="titleStyle">
      <slot name="title"></slot>
      <Icon name="chevron-down" :class="[prefixCls + '-submenu-title-icon']"></Icon>
    </div>
    <collapse-transition v-if="mode === 'vertical'">
      <ul :class="[prefixCls]" v-show="opened"><slot></slot></ul>
    </collapse-transition>
    <transition name="slide-up" v-else>
      <div
        v-show="opened"
        placement="bottom"
        ref="drop"
        :style="dropStyle"><ul :class="[prefixCls + '-drop-list']"><slot></slot></ul>
      </div>
    </transition>
  </li>
</template>
<script>
import Config from '../../config'
import mixin from './mixin'
import Emitter from '../../mixins/emitter'
import { getStyle, findComponentUpward, findComponentsDownward } from '../../utils/assist'
import Icon from '../icon'
import CollapseTransition from '../../transitions/collapse-transition.js'
const prefixCls = `${Config.clsPrefix}menu`
export default {
  name: `${Config.namePrefix}Submenu`,
  mixins: [ Emitter, mixin ],
  components: { Icon, CollapseTransition },
  props: {
    name: {
      type: [String, Number],
      required: true
    },
    disabled: {
      type: Boolean,
      default: false
    }
  },
  data () {
    return {
      prefixCls: prefixCls,
      active: false,
      opened: false,
      dropWidth: parseFloat(getStyle(this.$el, 'width'))
    }
  },
  computed: {
    classes () {
      return [
        `${prefixCls}-submenu`,
        {
          [`${prefixCls}-item-active`]: this.active && !this.hasParentSubmenu,
          [`${prefixCls}-opened`]: this.opened,
          [`${prefixCls}-submenu-disabled`]: this.disabled,
          [`${prefixCls}-submenu-has-parent-submenu`]: this.hasParentSubmenu,
          [`${prefixCls}-child-item-active`]: this.active
        }
      ]
    },
    accordion () {
      return this.menu.accordion
    },
    dropStyle () {
      let style = {position: 'absolute'}
      if (this.dropWidth) style.minWidth = `${this.dropWidth}px`
      return style
    },
    titleStyle () {
      return this.hasParentSubmenu && this.mode !== 'horizontal' ? {
        paddingLeft: 43 + (this.parentSubmenuNum - 1) * 24 + 'px'
      } : {}
    }
  },
  methods: {
    handleMouseenter () {
      if (this.disabled) return
      if (this.mode === 'vertical') return
      clearTimeout(this.timeout)
      this.timeout = setTimeout(() => {
        this.menu.updateOpenKeys(this.name)
        this.opened = true
      }, 250)
    },
    handleMouseleave () {
      if (this.disabled) return
      if (this.mode === 'vertical') return
      clearTimeout(this.timeout)
      this.timeout = setTimeout(() => {
        this.menu.updateOpenKeys(this.name)
        this.opened = false
      }, 150)
    },
    handleClick () {
      if (this.disabled) return
      if (this.mode === 'horizontal') return
      const opened = this.opened
      if (this.accordion) {
        this.$parent.$children.forEach(item => {
          if (item.$options.name === 'Submenu') item.opened = false
        })
      }
      this.opened = !opened
      this.menu.updateOpenKeys(this.name)
    }
  },
  watch: {
    mode (val) {
      if (val === 'horizontal') {
        this.$refs.drop.update()
      }
    },
    opened (val) {
      if (this.mode === 'vertical') return
      if (val) {
        // set drop a width to fixed when menu has fixed position
        this.dropWidth = parseFloat(getStyle(this.$el, 'width'))
      } else {
      }
    }
  },
  mounted () {
    this.$on('on-menu-item-select', (name) => {
      if (this.mode === 'horizontal') this.opened = false
      this.dispatch(`${Config.namePrefix}Menu`, 'on-menu-item-select', name)
      return true
    })
    this.$on('on-update-active-name', (status) => {
      if (findComponentUpward(this, `${Config.namePrefix}Submenu`)) this.dispatch(`${Config.namePrefix}Submenu`, 'on-update-active-name', status)
      if (findComponentsDownward(this, `${Config.namePrefix}Submenu`)) {
        findComponentsDownward(this, `${Config.namePrefix}Submenu`).forEach(item => {
          item.active = false
        })
      }
      this.active = status
    })
  }
}
</script>
