<template>
  <div>
    <div style="height: 60px;">
      <h4>连接数</h4>
      <br>
      <span>总连接数:</span><span>{{detail.connection.total}}</span>
      <span>消费者:</span><span>{{detail.connection.consumer}}</span>
      <span style="width: 10px"></span>
      <span>生产者:</span><span>{{detail.connection.producer}}</span>
    </div>
    <div>
      <h4>存储监控</h4>
      <br>
      <span>状态:</span><span v-if="detail.store.started" style="background: #07bf0c">启动</span><span v-else style="background: #bf291e">未启动</span>
      <span>总磁盘大小:</span><span>{{detail.store.totalSpace | numFilter}}GB</span>
      <span>空闲磁盘大小:</span><span>{{detail.store.freeSpace | numFilter}}GB</span>
    </div>
    <div>
      <h4>选举</h4>
      <br>
      <span>状态:</span><span v-if="detail.election.started" style="background: #07bf0c">启动</span><span v-else style="background: #bf0f20">未启动</span>
    </div>
    <div>
      <h4>nameServer</h4>
      <br>
      <span>状态:</span><span v-if="detail.nameServer.started" style="background: #07bf0c">启动</span><span v-else style="background: #bf0f20">未启动</span>
    </div>
  </div>
</template>

<script>

  import apiRequest from '../../utils/apiRequest.js'
  export default {
    name:"brokerServerMonitor",
    props:{
      brokerId:{
        type:Number
      }
    },
    data () {
      return {
        urls:{
          findbroker: '/monitor/broker'
        },
        detail:{
          store:{

          },
          connection:{

          },
          nameServer:{

          },
          election:{

          }
        },

      }
    },
    methods: {
      getList(){
        apiRequest.get(this.urls.findbroker+"/" + this.brokerId,"",{}).then(data=>{
          if(data.code == 200) {
            this.detail = data.data;
            console.log(data.data)
        }
        })
      }
    },
    mounted () {
      this.getList();
    },
    filters: {
      numFilter(value) {
        // 截取当前数据到小数点后两位
        let value1 = value/1024/1024/1024;
        let realVal = parseFloat(value1).toFixed(2)
        return realVal

      }

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
