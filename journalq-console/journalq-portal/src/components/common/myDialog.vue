<template>
  <d-dialog
      v-model="dialog.visible"
      :title="dialog.title || '新建'"
      :showFooter="dialog.showFooter"
      :width="dialog.width || 520"
      :scrollable="dialog.scrollable || false"
      @on-confirm="handleSave"
      @on-cancel="handleCancel"
      >
      <slot></slot>
  </d-dialog>
</template>

<script>
export default {
    name: 'myDialog',
    data() {
      return {
      };
    },
    props: {
      dialog: {
        default(){
          return {
            visible: false
          }
        }
      }
    },
    methods: {
      handleSave(){
        this.$emit('on-dialog-confirm');
      },
      handleCancel(){
        this.$emit('on-dialog-cancel');
      }
    }
  }
</script>
