<template>
  <div>
  <grid-row>
    <grid-col span="12">
      <d-menu :theme="theme2">
        <d-menu-item :name="index" v-for="(item,index) in navs" :key="index" :to="item.name">
         <icon :name="item.icon"></icon>
          {{item.title}}
        </d-menu-item>
      </d-menu>
    </grid-col>
  </grid-row>
  </div>
</template>
<script>
export default {
  data () {
    return {
      theme2: 'light'
    }
  },
  props: {
    navs: {
      type: Array,
      required: true
    }
  }
}
</script>
