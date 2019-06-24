<template>
  <li :class="[prefixCls + '-item-group']">
    <div :class="[prefixCls + '-item-group-title']" :style="groupStyle">{{ title }}</div>
    <ul><slot></slot></ul>
  </li>
</template>
<script>
import Config from '../../config'
import mixin from './mixin'
const prefixCls = `${Config.clsPrefix}menu`
export default {
  name: `${Config.namePrefix}MenuGroup`,
  mixins: [ mixin ],
  props: {
    title: {
      type: String,
      default: ''
    }
  },
  data () {
    return {
      prefixCls: prefixCls
    }
  },
  computed: {
    groupStyle () {
      return this.hasParentSubmenu && this.mode !== 'horizontal' ? {
        paddingLeft: 43 + (this.parentSubmenuNum - 1) * 28 + 'px'
      } : {}
    }
  }
}
</script>
