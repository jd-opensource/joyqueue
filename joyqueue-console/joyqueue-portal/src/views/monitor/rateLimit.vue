<template>
  <div>
    <div>
      <d-input v-model.number="tps" placeholder="请输入">
        <i slot="prepend" style="width: 30px;">&nbsp;&nbsp;&nbsp;tps:</i>
        <i slot="append" style="width: 30px">次/s</i>
      </d-input>
    </div>
    <br>
    <div>
      <d-input v-model.number="traffic" placeholder="请输入">
        <i slot="prepend" style="width: 30px">traffic:</i>
        <i slot="append" style="width: 30px">byte/s</i>
      </d-input>
    </div>
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
      traffic: this.limitTraffic,
      tps: this.limitTps
    }
  },
  methods: {
  },
  mounted () {
  }
}
</script>

<style scoped>

</style>
