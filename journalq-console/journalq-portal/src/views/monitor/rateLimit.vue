<template>
  <div>
    <d-input v-model.number="tps" placeholder="请输入">
      <i slot="prepend">tps:</i>
      <i slot="append">次/s</i>
    </d-input>
    <br/>
    <d-input v-model.number="traffic" placeholder="请输入">
      <i slot="prepend">traffic:</i>
      <i slot="append">b/s</i>
    </d-input>
  </div>
</template>

<script>
export default {
  name: 'rate-limit',
  props: {
    limitTraffic: {
      type: Number,
      default: 0
    },
    limitTps: {
      type: Number,
      default: 0
    }
  },
  data () {
    return {
      traffic:this.limitTraffic,
      tps:this.limitTps
    }
  },
  methods:{
  },
  mounted(){
  }
}
</script>

<style scoped>

</style>
