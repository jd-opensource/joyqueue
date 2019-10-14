<template>
  <div>
    <d-tree :data="treeData" @node-click="handleNodeClick">
    <span class="custom-tree-icon" slot-scope="{ node, data }">
      <icon :name="data.children ? 'folder' : 'file'"></icon>
      <span>{{ node.label }}</span>
    </span>
    </d-tree>
  </div>
</template>

<script>

import apiRequest from '../../utils/apiRequest.js'

export default {
  name: 'brokerStoreTreeViewMonitor',
  props: {
    brokerId: {
      type: Number
    }
  },
  data () {
    return {
      urls: {
        treeView: '/monitor/broker/storeTreeView',
        deleteStoreGarbageFile:'/manage/broker/garbageFile'
      },
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
      },
      treeData:[{
          label: '目录 1',
          children: [{
            label: '目录 1-1',
            children: [{
              label: '文件 1'
            }]
          }]
        },
        {
          label: '目录 2',
          children: [{
            label: '目录 2-1',
            children: [{
              label: '文件 2'
            }]
          }, {
            label: '目录 2-2',
            children: [{
              label: '文件 3'
            }]
          }]
        }]

    }
  },
  methods: {
    getList () {
      apiRequest.get(this.urls.treeView + '/' + this.brokerId, '', {}).then(data => {
        if (data.code == 200) {
          this.treeData = data.data
        }
      })
    },
    handleNodeClick(data) {
      console.log(data);
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
