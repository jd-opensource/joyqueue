<template>
  <transition :name="prefixCls + 'notification-fade'">
    <div
      :class="[prefixCls, customClass, horizontalClass]"
      v-show="visible"
      :style="positionStyle"
      @mouseenter="clearTimer()"
      @mouseleave="startTimer()"
      @click="click"
      role="alert"
    >
      <icon v-if="type" :class="[prefixCls + '__icon',typeClass, iconClass]" size="24"  :name="iconName"></icon>
      <div :class="[prefixCls+'__group',{'is-with-icon': typeClass || iconClass }]">
        <h2 :class="[ prefixCls+'__title' ]" v-text="title"></h2>
        <div :class="[ prefixCls+'__content' ]" v-show="message">
          <slot>
            <p v-if="!prudentHTML">{{ message }}</p>
            <p v-else v-html="message"></p>
          </slot>
        </div>
        <div
          :class="[ prefixCls+'__closeBtn']"
          v-if="showClose"
          @click.stop="close">
          <icon name="x" size="20" color="#909399"></icon>
        </div>
      </div>
    </div>
  </transition>
</template>

<script type="text/babel">
import Config from '../../config'

const prefixCls = `${Config.clsPrefix}notification`

let typeMap = {
  success: 'success',
  info: 'info',
  warning: 'warning',
  error: 'error'
}

export default {
  name: `${Config.namePrefix}Notification`,
  data () {
    return {
      prefixCls: prefixCls,
      visible: false,
      title: '',
      message: '',
      duration: 4500,
      type: '',
      showClose: true,
      customClass: '',
      icon: '',
      iconClass: '',
      onClose: null,
      onClick: null,
      closed: false,
      verticalOffset: 0,
      timer: null,
      prudentHTML: false,
      position: 'top-right'
    }
  },

  computed: {
    typeClass () {
      return (this.type && typeMap[this.type]) ? `${Config.clsPrefix}icon-${typeMap[this.type]}` : ''
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
    },
    horizontalClass () {
      return this.position.indexOf('right') > -1 ? 'right' : 'left'
    },

    verticalProperty () {
      return /^top-/.test(this.position) ? 'top' : 'bottom'
    },

    positionStyle () {
      return {
        [this.verticalProperty]: `${this.verticalOffset}px`
      }
    }
  },

  watch: {
    closed (newVal) {
      if (newVal) {
        this.visible = false
        this.$el.addEventListener('transitionend', this.destroyElement)
      }
    }
  },

  methods: {
    destroyElement () {
      this.$el.removeEventListener('transitionend', this.destroyElement)
      this.$destroy(true)
      this.$el.parentNode.removeChild(this.$el)
    },

    click () {
      if (typeof this.onClick === 'function') {
        this.onClick()
      }
    },

    close () {
      this.closed = true
      if (typeof this.onClose === 'function') {
        this.onClose()
      }
    },

    clearTimer () {
      clearTimeout(this.timer)
    },

    startTimer () {
      if (this.duration > 0) {
        this.timer = setTimeout(() => {
          if (!this.closed) {
            this.close()
          }
        }, this.duration)
      }
    },
    keydown (e) {
      if (e.keyCode === 46 || e.keyCode === 8) {
        this.clearTimer() // detele 取消倒计时
      } else if (e.keyCode === 27) { // esc关闭消息
        if (!this.closed) {
          this.close()
        }
      } else {
        this.startTimer() // 恢复倒计时
      }
    }
  },
  mounted () {
    if (this.duration > 0) {
      this.timer = setTimeout(() => {
        if (!this.closed) {
          this.close()
        }
      }, this.duration)
    }
    document.addEventListener('keydown', this.keydown)
  },
  beforeDestroy () {
    document.removeEventListener('keydown', this.keydown)
  }
}
</script>
