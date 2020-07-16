<template>
  <div v-dialog-drag="draggable"
       v-dialog-resize="{ resizable, minWidth, minHeight }"
       v-transfer-dom
       :data-transfer="transfer">
    <transition :name="transitionNames[1]" v-if="modal">
      <div :class="maskClasses" v-show="visible" @click="handleMaskClick"></div>
    </transition>
    <div :class="wrapClasses" @click.self="handleWrapClick">
      <transition :name="transitionNames[0]" @after-leave="animationFinish">
        <div :class="classes" :style="mainStyles" v-show="visible">
          <div :class="[prefixCls + '__content']">
            <Icon v-if="isIconType" :name="iconName" :class="[prefixCls + '__icon']"></Icon>
            <a :class="[prefixCls + '__close']" v-if="closable" @click="close">
              <slot name="close">
                <Icon name="x"></Icon>
              </slot>
            </a>
            <div :class="[prefixCls + '__header']" v-if="showHeader && ($slots.header || this.title)">
              <slot name="header">
                <div :class="[prefixCls + '__title']">{{ title }}</div>
              </slot>
            </div>
            <div :class="[prefixCls + '__body']">
              <div v-if="visible">
                <slot>
                  <div v-html="content"></div>
                  <div :class="[prefixCls + '__input']" v-if="showInput">
                    <d-input v-model="inputValue" :placeholder="inputPlaceholder" oninput="value = value.trim()" @keyup.enter.native="handleAction('confirm', $event)" ref="input"></d-input>
                  </div>
                </slot>
              </div>
            </div>
            <div :class="[prefixCls + '__footer']" v-if="showFooter">
              <slot name="footer">
                <d-button v-show="showCancelButton" @click.native="handleAction('cancel', $event)">{{ localeCancelText }}</d-button>
                <d-button type="primary" v-show="showConfirmButton" :loading="buttonLoading" @click.native="handleAction('confirm', $event)">{{ localeOkText }}</d-button>
              </slot>
            </div>
            <div v-if="resizable" :class="[prefixCls + '__resize']"></div>
          </div>
        </div>
      </transition>
    </div>
  </div>
</template>
<script>
import Config from '../../config'
import Icon from '../icon'
import DButton from '../button/button.vue'
import DInput from '../input/input.vue'
import TransferDom from '../../directives/transfer-dom'
import DialogResize from '../../directives/dialog-resize'
import DialogDrag from '../../directives/dialog-drag'
import Locale from '../../mixins/locale'
import Emitter from '../../mixins/emitter'
import ScrollbarMixins from '../../mixins/scrollbar'
import { oneOf } from '../../utils/assist'

const prefixCls = `${Config.clsPrefix}dialog`
const localePrefix = `${Config.localePrefix}.dialog.`

export default {
  name: `${Config.namePrefix}Dialog`,
  mixins: [ Locale, Emitter, ScrollbarMixins ],
  components: { Icon, DButton, DInput },
  directives: { TransferDom, DialogDrag, DialogResize },
  props: {
    value: {
      type: Boolean,
      default: false
    },
    modal: {
      type: Boolean,
      default: true
    },
    closable: {
      type: Boolean,
      default: true
    },
    maskClosable: {
      type: Boolean,
      default: true
    },
    autoClosable: {
      type: Boolean,
      default: false
    },
    title: {
      type: String
    },
    content: {
      type: String
    },
    width: {
      type: [Number, String],
      default: 520
    },
    okText: {
      type: String
    },
    cancelText: {
      type: String
    },
    loading: {
      type: Boolean,
      default: false
    },
    styles: {
      type: Object
    },
    className: {
      type: String
    },
    // for instance
    type: {
      validator (value) {
        return oneOf(value, ['alert', 'confirm', 'prompt', 'success', 'error', 'warning', 'info'])
      }
    },
    showHeader: {
      type: Boolean,
      default: true
    },
    showFooter: {
      type: Boolean,
      default: true
    },
    showInput: {
      type: Boolean,
      default: false
    },
    scrollable: {
      type: Boolean,
      default: false
    },
    transitionNames: {
      type: Array,
      default () {
        return ['ease', 'fade']
      }
    },
    transfer: {
      type: Boolean,
      default: true
    },
    beforeClose: {
      type: Function
    },
    draggable: {
      type: Boolean,
      default: false
    },
    resizable: {
      type: Boolean,
      default: false
    },
    minWidth: {
      type: Number,
      default: 323
    },
    minHeight: {
      type: Number,
      default: 200
    }
  },
  data () {
    return {
      prefixCls: prefixCls,
      wrapShow: false,
      showCancelButton: true,
      showConfirmButton: true,
      action: '',
      buttonLoading: false,
      visible: this.value,
      inputValue: null,
      inputPlaceholder: '',
      callback: null
    }
  },
  computed: {
    isIconType () {
      return oneOf(this.type, ['success', 'error', 'warning', 'info'])
    },
    wrapClasses () {
      return [
        `${prefixCls}__wrapper`,
        {
          [`${prefixCls}--hidden`]: !this.wrapShow,
          [`${prefixCls}--confirm`]: !!this.isIconType,
          [`${prefixCls}--confirm-${this.type}`]: !!this.isIconType,
          [`${this.className}`]: !!this.className
        }
      ]
    },
    maskClasses () {
      return `${prefixCls}__mask`
    },
    classes () {
      return `${prefixCls}`
    },
    mainStyles () {
      let style = {}

      const width = parseInt(this.width)
      const styleWidth = {
        width: width <= 100 ? `${width}%` : `${width}px`
      }

      const customStyle = this.styles ? this.styles : {}

      Object.assign(style, styleWidth, customStyle)

      return style
    },
    iconName () {
      const nameArr = {
        'success': 'check-circle',
        'error': 'x-circle',
        'warning': 'alert-circle',
        'info': 'info'
      }
      return nameArr[this.type] || ''
    },
    localeOkText () {
      if (this.okText === undefined) {
        return this.t(localePrefix + 'okText')
      } else {
        return this.okText
      }
    },
    localeCancelText () {
      if (this.cancelText === undefined) {
        return this.t(localePrefix + 'cancelText')
      } else {
        return this.cancelText
      }
    }
  },
  watch: {
    value (val) {
      this.visible = val
    },
    visible (val) {
      if (val === false) {
        this.buttonLoading = false
        this.timer = setTimeout(() => {
          this.wrapShow = false
          this.removeScrollEffect()
        }, 300)
      } else {
        if (this.timer) clearTimeout(this.timer)
        this.wrapShow = true
        if (!this.scrollable) {
          this.addScrollEffect()
        }
      }
      this.broadcast('Table', 'on-visible-change', val)
      this.broadcast('Slider', 'on-visible-change', val) // #2852
      this.$emit('on-visible-change', val)
    },
    loading (val) {
      if (!val) {
        this.buttonLoading = false
      }
    },
    scrollable (val) {
      if (!val) {
        this.addScrollEffect()
      } else {
        this.removeScrollEffect()
      }
    },
    title (val) {
      if (this.$slots.header === undefined) {
        this.showHeader = !!val
      }
    }
  },
  methods: {
    doBeforeClose (evt, action) {
      this.beforeClose ? this.beforeClose(evt, action) : action()
    },
    confirm (result) {
      if (result === false) {
        return
      }

      if (this.loading) {
        this.buttonLoading = true
      } else {
        if (this.autoClosable) {
          this.visible = false
          this.$emit('input', false)
        }
      }
      this.$emit('on-confirm')

      if (this.action && this.callback) {
        this.callback(this.action, this)
      }
    },
    close (result) {
      if (result === false) {
        return
      }
      this.visible = false
      this.$emit('input', false)
      this.$emit('on-cancel')

      if (this.action && this.callback) {
        this.callback(this.action, this)
      }
    },
    handleMaskClick (evt) {
      if (this.maskClosable) {
        this.doBeforeClose(evt, this.close)
      }
    },
    handleWrapClick (event) {
      // use indexOf,do not use === ,because d-dialog__wrapper can have other custom className
      const className = event.target.getAttribute('class')
      if (className && className.indexOf(`${prefixCls}__wrapper`) > -1) {
        this.handleMaskClick()
      }
    },
    handleAction (action, evt) {
      this.action = action

      if (action === 'cancel') {
        this.doBeforeClose(evt, this.close)
      }

      if (action === 'confirm') {
        this.doBeforeClose(evt, this.confirm)
      }
    },
    handleKeyCode (e) {
      if (this.visible && this.closable) {
        if (e.keyCode === 27) {
          this.doBeforeClose(e, this.close)
        }
      }
    },
    animationFinish () {
      this.$emit('on-hidden')
    }
  },
  mounted () {
    if (this.visible) {
      this.wrapShow = true
    }

    // ESC close
    document.addEventListener('keydown', this.handleKeyCode)
  },
  beforeDestroy () {
    document.removeEventListener('keydown', this.handleKeyCode)
    this.removeScrollEffect()
  }
}
</script>
