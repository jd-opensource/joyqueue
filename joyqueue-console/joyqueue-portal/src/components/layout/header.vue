<template>
  <header class="layout-header" :style="{ height }">
    <slot></slot>
  </header>
</template>

<script>
export default {
  name: 'layout-header',
  props: {
    height: {
      type: String,
      default: '60px'
    }
  }
}
</script>
