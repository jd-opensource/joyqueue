<template>
  <d-dialog
      v-model="dialog.visible"
      :title="dialog.title || '新建'"
      :showFooter="dialog.showFooter"
      :width="dialog.width || 520"
      :height="dialog.height || 'auto'"
      :scrollable="dialog.scrollable || false"
      :styles="styles"
      @on-confirm="handleSave"
      @on-cancel="handleCancel"
      :maskClosable="dialog.maskClosable || false"
      :draggable="dialog.draggable || false"
      :resizable="dialog.resizable || true">
  <slot></slot>
  </d-dialog>
</template>

<script>
export default {
  name: 'myDialog',
  data () {
    return {
    }
  },
  props: {
    dialog: {
      type: Object,
      default () {
        return {
          visible: false
        }
      }
    },
    styles: {
      type: Object,
      default () {
        return {top: '40px'}
      }
    }
  },
  methods: {
    handleSave () {
      this.$emit('on-dialog-confirm')
    },
    handleCancel () {
      this.$emit('on-dialog-cancel')
    }
  }
}
</script>
