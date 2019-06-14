<template>
  <div :class="[prefixCls + '__pane']" v-show="show">
    <slot></slot>
  </div>
</template>

<script>
import Config from '../../config'

const prefixCls = `${Config.clsPrefix}tabs`

export default {
  name: `${Config.namePrefix}TabPane`,
  props: {
    name: {
      type: String
    },
    label: {
      type: String
    },
    icon: {
      type: String
    },
    disabled: {
      type: Boolean,
      default: false
    },
    closable: {
      type: Boolean,
      default: true
    },
    visible: {
      type: Boolean,
      default: true
    }
  },
  data () {
    return {
      prefixCls: prefixCls,
      currentName: this.name,
      show: true
    }
  },
  computed: {
    isClosable () {
      return this.closable ? true : this.$parent.closable
    }
  },
  watch: {
    name (val) {
      this.currentName = val
      this.updateNav()
    },
    label () {
      this.updateNav()
    },
    icon () {
      this.updateNav()
    },
    disabled () {
      this.updateNav()
    },
    visible () {
      this.updateNav()
    }
  },
  methods: {
    updateNav () {
      this.$parent.updateNav()
    }
  },
  mounted () {
    this.updateNav()
  },
  destroyed () {
    this.updateNav()
  }
}
</script>
