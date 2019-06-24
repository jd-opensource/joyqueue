<template>
  <footer class="layout-footer" :style="{ height }">
    <slot></slot>
  </footer>
</template>

<script>
export default {
  name: 'layout-footer',
  props: {
    height: {
      type: String,
      default: '60px'
    }
  }
}
</script>
