<template>
  <div>
    <d-tabs @on-change="menuChange">
      <d-tab-pane label="生产者详情" name="producer">
        <my-table :data="tableData" :showPin="showTablePin" style="height: 500px;overflow-y:auto" :page="page" @on-size-change="handleSizeChange" @on-current-change="handleCurrentChange" @on-selection-change="handleSelectionChange"
                  @on-edit="edit">
        </my-table>
      </d-tab-pane>
      <d-tab-pane label="消费者详情" name="consumer">
        <my-table :data="tableData" :showPin="showTablePin" style="height: 500px;overflow-y:auto" :page="page" @on-size-change="handleSizeChange" @on-current-change="handleCurrentChange" @on-selection-change="handleSelectionChange"
                  @on-edit="edit">
        </my-table>
      </d-tab-pane>
      <d-tab-pane label="连接信息" name="brokerConnectionMonitor">
        <broker-connection-monitor ref="brokerConnectionMonitor" style="height: 500px;overflow-y:auto" :broker-id="this.brokerId"></broker-connection-monitor>
      </d-tab-pane>
      <d-tab-pane label="分区组" name="brokerPartitionGroupMonitor">
        <broker-partition-group-monitor ref="brokerPartitionGroupMonitor" style="height: 500px;overflow-y:auto" :broker-id="this.brokerId"></broker-partition-group-monitor>
      </d-tab-pane>
      <d-tab-pane label="服务器监控" name="brokerServerMonitor">
        <broker-server-monitor ref="brokerServerMonitor" :brokerId="brokerId"> </broker-server-monitor>
      </d-tab-pane>
    </d-tabs>
  </div>
</template>

<script>
  import apiRequest from '../../utils/apiRequest.js'
  import myTable from '../../components/common/myTable.vue'
  import myDialog from '../../components/common/myDialog.vue'
  import crud from '../../mixins/crud.js'
  import ClientConnection from "../monitor/detail/clientConnection";
  import BrokerConnectionMonitor from "./brokerConnectionMonitor";
  import BrokerPartitionGroupMonitor from "./brokerPartitionGroupMonitor";
  import BrokerServerMonitor from "./brokerServerMonitor.vue";

  export default {
    name: "brokerMonitor",
    components: {
      BrokerPartitionGroupMonitor,
      BrokerConnectionMonitor,
      ClientConnection,
      myTable,
      myDialog,
      BrokerServerMonitor
    },
    props:{
      brokerId:{
        type:Number
      }
    },
    mixins: [ crud ],
    data() {
      return {
        theme1: 'light',
        urls: {
          search: '/monitor/broker/topic/search',
        },
        searchData: {
          brokerId: this.brokerId,
          type:1
        },
        searchRules: {
        },
        tableData: {
          rowData: [],
          colData: [
            {
              title: '队列',
              key: 'topic'
            },
            {
              title: '应用',
              key: 'brokerTopicMonitorRecordList',
              render: (h, params) => {
                var list=params.item.brokerTopicMonitorRecordList;
                var html=[];
                for(var i=0;i<list.length;i++){
                  var p=h('div',list[i].app);
                  html.push(p);
                }
                return h('div',{},html)
              }
            },
            {
              title: '连接数',
              key: 'brokerTopicMonitorRecordList',
              render: (h, params) => {
                var list=params.item.brokerTopicMonitorRecordList;
                var html=[];
                for(var i=0;i<list.length;i++){
                  var p=h('div',list[i].connections);
                  html.push(p);
                }
                return h('div',html)
              }
            },
            {
              title: '出/入对数',
              key: 'brokerTopicMonitorRecordList',
              render: (h, params) => {
                var list=params.item.brokerTopicMonitorRecordList;
                var html=[]
                for(var i=0;i<list.length;i++){
                  var p=h('div',list[i].count);
                  html.push(p);
                }
                return h('div',html)
              }
            },
            {
              title: '出/入队流量',
              key: 'brokerTopicMonitorRecordList',
              render: (h, params) => {
                var list=params.item.brokerTopicMonitorRecordList;
                var html=[];
                for(var i=0;i<list.length;i++){
                  var p=h('div',list[i].totalSize);
                  html.push(p);
                }
                return h('div',html)
              }
            }
          ],
          // 表格操作，如果需要根据特定值隐藏显示， 设置bindKey对应的属性名和bindVal对应的属性值
          // btns: [
          //   {
          //     txt: '编辑',
          //     method: 'on-edit'
          //   }
          // ]
        },
        multipleSelection: []
      }
    },
    methods: {
      menuChange(item){
        if (item.name=='consumer'){
          this.searchData.type=2;
          this.getList();
        } else if (item.name=='producer') {
          this.searchData.type=1;
          this.getList();
        } else {
          this.$refs[item.name].getList()
        }
      }
    },
    mounted () {
      this.getList()
    }
  }
</script>
