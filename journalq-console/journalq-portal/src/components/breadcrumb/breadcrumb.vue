<template>
  <div :class="prefixCls">
    <slot></slot>
  </div>
</template>

<script>
import Config from '../../config'

const prefixCls = `${Config.clsPrefix}breadcrumb`

export default {
  name: `${Config.namePrefix}Breadcrumb`,
  props: {
    separator: {
      type: String,
      default: '/'
    }
  },
  data () {
    return {
      prefixCls: prefixCls
    }
  }
}
</script>
