<template>
  <aside class="layout-sider" :style="{ width }">
    <slot></slot>
  </aside>
</template>

<script>
export default {
  name: 'layout-sider',
  props: {
    width: {
      type: String,
      default: '300px'
    }
  }
}
</script>
