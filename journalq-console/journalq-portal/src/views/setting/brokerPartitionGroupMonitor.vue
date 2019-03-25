<template>
  <div>
      <my-table :data="tableData" :showPin="showTablePin" :page="page" @on-size-change="handleSizeChange" @on-current-change="handleCurrentChange" @on-selection-change="handleSelectionChange"
                @on-edit="edit">
      </my-table>
  </div>
</template>

<script>
  import apiRequest from '../../utils/apiRequest.js'
  import myTable from '../../components/common/myTable.vue'
  import myDialog from '../../components/common/myDialog.vue'
  import crud from '../../mixins/crud.js'
  import ClientConnection from "../monitor/clientConnection";

  export default {
    name: "brokerPartitionGroupMonitor",
    components: {
      ClientConnection,
      myTable,
      myDialog
    },
    props:{
      brokerId:{
        type:Number
      }
    },
    mixins: [ crud ],
    data() {
      return {
        urls: {
          search: '/monitor/broker/partition/search',
        },
        searchData: {
          brokerId: this.brokerId,
        },
        searchRules: {
        },
        tableData: {
          rowData: [],
          colData: [
            {
              title: '主题',
              key: 'topic'
            },
            {
              title: 'partitionGroup',
              key: 'partitionGroupMetricList',
              render: (h, params) => {
                var list=params.item.partitionGroupMetricList;
                var html=[];
                for(var i=0;i<list.length;i++){
                  var p=h('div',list[i].partitionGroup);
                  html.push(p);
                }
                return h('div',{},html)
              }
            },
            {
              title: 'partition',
              key: 'partitionGroupMetricList',
              render: (h, params) => {
                var list=params.item.partitionGroupMetricList;
                var html=[];
                for(var i=0;i<list.length;i++){
                  var p=h('div',list[i].partitions);
                  html.push(p);
                }
                return h('div',{},html)
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
    },
    mounted () {
      this.getList()
    }
  }
</script>
