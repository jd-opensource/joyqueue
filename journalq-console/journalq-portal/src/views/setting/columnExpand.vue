<template>
  <div>
    <grid-row class="expand-row">
      <grid-col span="8">
        <span class="expand-value">{{ col }}</span>
      </grid-col>
    </grid-row>
  </div>
</template>

<script>
export default {
  name: 'column-expand',
  props: {
    col: ''
  }
}
</script>

<style scoped>
  .expand-row{
    margin-bottom: 16px;
  }
</style>
