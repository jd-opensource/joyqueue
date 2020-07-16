<template>
  <div>
    <!--    <div v-if="isConsumer">-->
    <!--      <grid-row class="mb10">-->
    <!--        Kafka消费者估算限流条数的方法：-->
    <!--      </grid-row>-->
    <!--      <grid-row class="mb30">-->
    <!--        填写消息条数 = 期望出队消息总条数（每秒）/ Broker数量 / 客户端数量 / max(max.partition.fetch.bytes / 单条消息大小, 服务端批量条数)-->
    <!--      </grid-row>-->
    <!--    </div>-->
    <br>
    <grid-row>
      <d-input v-model.number="tps" oninput="value = value.trim()" placeholder="请输入">
        <i slot="prepend" style="width: 50px;">消息条数:</i>
        <i slot="append" style="width: 85px">条 / Broker / S</i>
      </d-input>
    </grid-row>
    <br>
    <grid-row>
      <d-input v-model.number="traffic" oninput="value = value.trim()" placeholder="请输入">-ci
        <i slot="prepend" style="width: 50px">消息流量:</i>
        <i slot="append" style="width: 85px">Byte / Broker / S</i>
      </d-input>
    </grid-row>
    <br>
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
    },
    isConsumer: {
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
