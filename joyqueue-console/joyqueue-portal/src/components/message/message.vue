<template>
  <div
    :class="[prefixCls + '__wrapper']"
    :style="{
      top: top ? `${top}px` : 'auto'
    }">
    <transition name="move-up" @after-leave="doDestory">
      <div
        :class="classes"
        v-show="visible">
        <Icon :class="[prefixCls + '__icon']" :name="iconName"></Icon>
        <span :class="[prefixCls + '__content']">{{ message }}</span>
      </div>
    </transition>
  </div>
</template>

<script>
import Config from '../../config'
import Icon from '../icon'

const prefixCls = `${Config.clsPrefix}message`

export default {
  name: `${Config.namePrefix}Message`,
  components: { Icon },
  data () {
    return {
      prefixCls: prefixCls,
      message: '',
      duration: 3000,
      type: 'info',
      icon: '',
      visible: false,
      timer: null,
      closed: false,
      onClose: null,
      top: null
    }
  },
  computed: {
    classes () {
      return [
        `${prefixCls}`,
        {
          [`${prefixCls}--${this.type}`]: !!this.type
        }
      ]
    },
    iconName () {
      const nameArr = {
        'success': 'check-circle',
        'error': 'x-circle',
        'warning': 'alert-circle',
        'info': 'info',
        'loading': 'loader'
      }

      return this.icon || nameArr[this.type]
    }
  },
  watch: {
    closed (val) {
      if (val) {
        this.visible = false
      }
    }
  },
  methods: {
    doDestory () {
      this.$destroy(true)
      this.$el.parentNode.removeChild(this.$el)
    },
    close () {
      this.closed = true
      if (typeof this.onClose === 'function') {
        this.onClose(this)
      }
    },
    startTimer () {
      if (this.duration) {
        this.timer = setTimeout(() => {
          !this.closed && this.close()
        }, this.duration)
      }
    },
    clearTimer () {
      this.timer && clearTimeout(this.timer)
    }
  },
  mounted () {
    this.startTimer()
  }
}
</script>
