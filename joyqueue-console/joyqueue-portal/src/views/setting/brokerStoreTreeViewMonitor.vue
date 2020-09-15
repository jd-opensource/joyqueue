<template>
  <div style="height:700px;width:500px;overflow:scroll">
      <d-tree :data="treeData" style="overflow:scroll" accordion @node-click="handleNodeClick">
      <span class="custom-tree-icon" slot-scope="{ node, data }">
        <icon :name="data.directory ? 'folder' : 'file'"></icon>
        <span>{{ data.name }}</span>
        <span>
              <d-button
                type="borderless"
                @click="() => removeDialog(node, data)">
                <icon name="trash-2" color="red" size="10"></icon>
              </d-button>
         </span>
      </span>
      </d-tree>
      <label style=" margin-top: 30px">共 {{total}} 条记录</label>

    <d-dialog
      v-model="modal"
      :modal="modal"
      title="删除文件"
      @on-confirm="remove"
      @on-cancel="cancel">

      <d-input v-model="query.path" style="margin-top: 10px" disabled oninput="value = value.trim()">
        <span slot="prepend" >
          <d-tooltip  content="仅支持删除已软删除的文件">
            <Icon name="help-circle" class="help-icon" size="16" color="#4F8EEB" />
          </d-tooltip>
          路径</span>
      </d-input>
      <d-radio-group v-model="query.retain" name="radiogroup" style="margin-top: 20px">
        <d-radio label="true" >保留目录</d-radio>
        <d-radio label="false">删除目录</d-radio>
      </d-radio-group>
    </d-dialog>
  </div>
</template>

<script>

import apiRequest from '../../utils/apiRequest.js'

export default {
  name: 'brokerStoreTreeViewMonitor',
  props: {
  },
  data () {
    return {
      brokerId: this.$route.params.brokerId || this.$route.query.brokerId,
      urls: {
        treeView: '/monitor/broker/storeTreeView',
        deleteStoreGarbageFile: '/manage/broker/garbageFile'
      },
      query: {
        path: '',
        recursive: true,
        retain: 'true'
      },
      modal: false,
      total: 0,
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
      treeData: []

    }
  },
  methods: {
    getList () {
      apiRequest.get(this.urls.treeView + '/' + this.brokerId + '/' + this.query.recursive, '', {}).then(data => {
        if (data.code == 200) {
          this.treeData = data.data.children
          this.total = this.treeData.length
        }
      })
    },
    handleNodeClick (data) {
      console.log(data)
    },
    cancel () {
      this.modal = false
      // this.$Message.info('点击了取消');
    },
    removeDialog (node, data) {
      this.modal = true
      this.query.path = data.path
    },
    remove () {
      apiRequest.delete(this.urls.deleteStoreGarbageFile + '?brokerId=' + this.brokerId + '&fileName=' + this.query.path + '&retain=' + this.query.retain).then(data => {
        if (data.code == 200 && data.data) {
          // let isSuccess = data.data;
          this.$Message.success('删除成功')
          this.modal = false
          this.getList()
        } else {
          this.$Dialog.error({
            content: '删除失败'
          })
        }
      }
      )
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
