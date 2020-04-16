<template>
  <div>
    <div style="border:10px solid #f7f7f7;width:600px;" v-if="detail.startupInfo">
      <h4>Broker信息:</h4>
      <span>version:</span><span>{{detail.startupInfo.version}}</span>&nbsp&nbsp
      <span>startupTime:</span><span v-if="detail.startupInfo.startupTime">{{ detail.startupInfo.startupTime }}</span> &nbsp&nbsp
    </div>
    <div style="border:10px solid #f7f7f7;width:600px;" v-if="detail.store">
      <h4>存储监控:</h4>
      <span>started:</span><span v-if="detail.store.started">Running</span><span v-else>Stop</span>&nbsp&nbsp
      <span>totalSpace:</span><span>{{detail.store.totalSpace}}</span>&nbsp&nbsp
      <span>freeSpace:</span><span>{{detail.store.freeSpace}}</span>
    </div>
    <div style="border:10px solid #f7f7f7;width:600px;" v-if="detail.bufferPoolMonitorInfo">
      <h4>内存:</h4>
      <div v-for="item in detail.bufferPoolMonitorInfo.plMonitorInfos">
        <span>bufferSize:</span><span>{{item.bufferSize}}</span>&nbsp&nbsp
        <span>cached:</span><span>{{item.cached}}</span>&nbsp&nbsp
        <span>used:</span><span>{{item.usedPreLoad}}</span>&nbsp&nbsp
        <span>totalSize:</span><span>{{item.totalSize}}</span>
      </div>
      <br>
      <span>preload:</span><span>{{detail.bufferPoolMonitorInfo.plUsed}}</span>&nbsp&nbsp
      <span>used:</span><span>{{detail.bufferPoolMonitorInfo.used}}</span>&nbsp&nbsp
      <span>direct:</span><span>{{detail.bufferPoolMonitorInfo.directUsed}}</span>&nbsp&nbsp
      <span>mmp:</span><span>{{detail.bufferPoolMonitorInfo.mmpUsed}}</span>&nbsp&nbsp
      <span>maxMemorySize:</span><span>{{detail.bufferPoolMonitorInfo.maxMemorySize}}</span>
    </div>
    <div style="border:10px solid #f7f7f7;width:600px;" v-if="detail.connection">
      <h4>连接数:</h4>
      <span>total:</span><span>{{detail.connection.total}}</span>&nbsp&nbsp
      <span>consumer:</span><span>{{detail.connection.consumer}}</span>&nbsp&nbsp
      <span>producer:</span><span>{{detail.connection.producer}}</span>
    </div>
    <div style="border:10px solid #f7f7f7;width:600px;" v-if="detail.election">
      <h4>选举:</h4>
      <span>started:</span><span v-if="detail.election.started" >Running</span><span v-else >Stop</span>
    </div>
    <div style="border:10px solid #f7f7f7;width:600px;" v-if="detail.nameServer">
      <h4>nsr:</h4>
      <span>started:</span><span v-if="detail.nameServer.started" >Running</span><span v-else>Stop</span>
    </div>
    <div style="border:10px solid #f7f7f7;width:600px;" v-if="archiveMonitorData">
      <h4>归档监控:</h4>
      <span>待归档消费记录数:</span><span>{{archiveMonitorData.consumeBacklog}}</span>&nbsp&nbsp
      <span>待归档生产记录数:</span><span>{{archiveMonitorData.produceBacklog}}</span>&nbsp&nbsp
    </div>
  </div>
</template>

<script>

import apiRequest from '../../utils/apiRequest.js'
import {timeStampToString} from '../../utils/dateTimeUtils'

export default {
  name: 'brokerServerMonitor',
  props: {
  },
  data () {
    return {
      archiveMonitorData: {},
      urls: {
        findbroker: '/monitor/broker',
        archiveMonitorUrl: '/monitor/archive',
      },
      brokerId: this.$route.params.brokerId || this.$route.query.brokerId,
      brokerIp: this.$route.params.brokerIp || this.$route.query.brokerIp,
      brokerPort: this.$route.params.brokerPort || this.$route.query.brokerPort,
      detail: {
        store: {

        },
        connection: {

        },
        nameServer: {

        },
        election: {

        },
        bufferPoolMonitorInfo: {
        }
      }

    }
  },
  methods: {
    getList0 () {
      apiRequest.get(this.urls.findbroker + '/' + this.brokerId, '', {}).then(data => {
        if (data.code === 200) {
          this.detail = data.data
          if (this.detail.startupInfo) {
            this.detail.startupInfo.startupTime = timeStampToString(this.detail.startupInfo.startupTime)
          }
        }
      })
    },
    archiveMonitor () {
      let broker = {
        ip: this.brokerIp,
        port: this.brokerPort
      }
      apiRequest.postBase(this.urls.archiveMonitorUrl, {}, broker, false).then((data) => {
        data.data = data.data || {}
        this.archiveMonitorData = data.data
      })
    },
    getList() {
      this.getList0()
      this.archiveMonitor()
    }
  },
  mounted () {
    this.getList()
  }
}
</script>

<style scoped>
  .demo-split{
    height: 200px;
    border: 1px solid #dcdee2;
  }
  .demo-split-pane{
    display: flex;
    align-items: center;
    justify-content: center;
    height: 100%;
  }
  .demo-split-pane.no-padding{
    height: 200px;
    padding: 0;
  }
</style>
