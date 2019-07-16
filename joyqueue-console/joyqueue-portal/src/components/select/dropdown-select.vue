<template>
  <div :class="[prefixCls + '-dropdown', className]" :style="styles"><slot></slot></div>
</template>
<script>
import Config from '../../config'

const prefixCls = `${Config.clsPrefix}select`

export default {
  name: 'Drop',
  props: {
    className: {
      type: String
    },
    prevElRect: {
      type: Object
    },
    visible: {
      type: Boolean,
      default: false
    }
  },
  data () {
    return {
      prefixCls: prefixCls,
      width: 0,
      height: 0
    }
  },
  computed: {
    styles () {
      if (!this.prevElRect) { return {} }
      if (this.width === 0 || this.height === 0) { return {} }
      let mwidth = 0
      let left = 0
      let top = 0
      let boundingRect = this.prevElRect.boundingRect || {}
      if (this.prevElRect.boundingRect) {
        left = this.prevElRect.toDocLeft
        top = this.prevElRect.toDocTop + boundingRect.height
      }
      if (this.width > this.prevElRect.windowW - boundingRect.left) {
        left = Math.floor(left - (this.width - this.prevElRect.windowW + boundingRect.left))
      }
      if (this.height > this.prevElRect.windowH - boundingRect.top - boundingRect.height) {
        top = Math.floor(top - (this.height - this.prevElRect.windowH + boundingRect.top + boundingRect.height))
      }
      mwidth = Math.floor(boundingRect.width)
      return {
        left: left + 'px',
        top: top + 'px',
        minWidth: mwidth + 'px',
        'display': this.visible ? 'inline-block' : 'none'
      }
    }
  },
  methods: {
  },
  mounted () {
    this.$nextTick(() => {
      this.$el.style.display = 'inline-block'
      this.width = this.$el.offsetWidth
      this.height = this.$el.offsetHeight
      this.$el.style.display = 'none'
    })
  }
}
</script>
