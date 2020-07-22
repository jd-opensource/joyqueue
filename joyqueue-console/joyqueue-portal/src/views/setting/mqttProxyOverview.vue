<template>
  <div style="margin-top: 50px" >
    <grid-row class="mb10"  >
      <grid-col :offset=4 :span="4">
        <d-input v-model="publishQuery.clientId" oninput="value = value.trim()" placeholder="客户id" class="left mr10" style="width: 90%">
        </d-input>
      </grid-col>
      <grid-col :span="4">
        <d-input v-model="publishQuery.app" oninput="value = value.trim()" placeholder="应用code" class="left mr10" style="width: 90%">
        </d-input>
      </grid-col>
      <grid-col :span="4">
        <d-input v-model="publishQuery.topic" oninput="value = value.trim()" placeholder="主题" class="left mr10" style="width: 90%">
        </d-input>
      </grid-col>
      <grid-col :span="4">
        <d-button type="primary" @click="getPublishMonitor" icon="search">发布消息数</d-button>
      </grid-col>
    </grid-row>
    <grid-row class="mb10" :offset=5>
      <grid-col :offset=5 :span="2" class="label" >汇总信息</grid-col>
    </grid-row>
    <div >
    <grid-row  class="mb10" justify="end">
      <grid-col :offset=5 :span="2" class="label">连接数:</grid-col>
      <grid-col :span="6" class="label" >
        <span >{{mqttMonitor.totalConnections}}</span>
      </grid-col>
      <grid-col :span="2" class="label">会话数:</grid-col>
      <grid-col :span="6" class="label">
        <span >{{mqttMonitor.totalSessions}}</span>
      </grid-col>
    </grid-row>
    </div>
  <div>
    <grid-row class="mb10" justify="end">
      <grid-col :offset=5 :span="2" class="label">发布消息数:</grid-col>
      <grid-col :span="6" class="label" >
        <span >{{mqttMonitor.totalPublished}}</span>
      </grid-col>
      <grid-col :span="2" class="label">消费消息数:</grid-col>
      <grid-col :span="6" class="label">
        <span >{{mqttMonitor.totalConsumed}}</span>
      </grid-col>
    </grid-row>
  </div>
  <div>
    <grid-row class="mb10" justify="end">
      <grid-col :offset=5 :span="2" class="label">ACK次数:</grid-col>
      <grid-col :span="6" class="label" >
        <span >{{mqttMonitor.totalAcknowledged}}</span>
      </grid-col>
      <grid-col :span="2" class="label">重提交次数:</grid-col>
      <grid-col :span="6" class="label">
        <span >{{mqttMonitor.totalRecommit}}</span>
      </grid-col>
    </grid-row>
  </div>
    <div>
      <grid-row class="mb10" justify="end">
        <grid-col :offset=5 :span="2" class="label">消费线程数:</grid-col>
        <grid-col :span="6" class="label" >
          <span >{{mqttMonitor.consumePool}}</span>
        </grid-col>
        <grid-col :span="2" class="label">分发线程数:</grid-col>
        <grid-col :span="6" class="label">
          <span >{{mqttMonitor.deliveryPool}}</span>
        </grid-col>
      </grid-row>
    </div>
  </div>
</template>

<script>
import apiRequest from '../../utils/apiRequest.js'

export default {
  name: 'mqtt-proxy-overview',
  props: {
    executorId: {
      type: Number,
      default: 0
    }
  },
  data () {
    return {
      urls: {
        state: `/monitor/mqtt/proxy/overview/executor/`,
        publish: '/monitor/mqtt/proxy/publish'
      },
      publishQuery: {
        clientId: '',
        topic: '',
        app: '',
        id: this.executorId
      },
      mqttMonitor: {
        totalConnections: 0,
        totalSessions: 0,
        totalPublished: 0,
        totalConsumed: 0,
        totalAcknowledged: 0,
        totalRecommit: 0,
        consumePool: 0,
        deliveryPool: 0
      }
    }
  },
  methods: {
    getMqttProxyMonitorOverview () {
      apiRequest.get(this.urls.state + this.executorId, {}).then((data) => {
        data.data = data.data || {}
        this.mqttMonitor = data.data
      })
    },
    getPublishMonitor () {
      apiRequest.postBase(this.urls.publish, {}, this.publishQuery).then((data) => {
        this.$Dialog.confirm({
          title: '发布数',
          content: data.data
        })
      })
    },
    getList () {
      this.getMqttProxyMonitorOverview()
    }
  },
  mounted () {
    // this.getMqttProxyMonitorOverview();
  }

}
</script>

<style scoped>

</style>
