<template>
  <span :class="[prefixCls + '__item']">
    <a v-if="href || Object.keys(to).length" :class="[prefixCls + '__link']" @click="handleClick">
      <slot></slot>
    </a>
    <span v-else :class="[prefixCls + '__text']"><slot></slot></span>
    <span :class="[prefixCls + '__separator']" v-html="separator"></span>
  </span>
</template>

<script>
import Config from '../../config'

const prefixCls = `${Config.clsPrefix}breadcrumb`

export default {
  name: `${Config.namePrefix}BreadcrumbItem`,
  props: {
    href: String,
    to: {
      type: [Object, String],
      default () {
        return {}
      }
    },
    replace: {
      type: Boolean,
      default: false
    }
  },
  data () {
    return {
      prefixCls: prefixCls,
      separator: ''
    }
  },
  mounted () {
    this.separator = this.$parent.separator
  },
  methods: {
    handleClick () {
      const to = this.to
      const href = this.href

      if (href) {
        window.location.href = href
      } else {
        this.replace ? this.$router.replace(to) : this.$router.push(to)
      }
    }
  }
}
</script>

<style>

</style>
