<template>
    <div>
      <div class="ml20 mt30" v-if="!threadSelect&&inputable">
          <d-input v-model="searchData.clientId" oninput="value = value.trim()" placeholder="客户端ID" class="left mr10"
                   style="width:213px" @on-enter="getList">
            <icon name="search" size="14" color="#CACACA" slot="suffix" @click="getList"></icon>
          </d-input>
      </div>

           <div class="ml20 mt30" label="线程">
            <d-select  v-if="threadSelect&&inputable" v-model="searchData.threadId" :label-in-value="true" style="width:260px" placeholder="请选择线程" @on-change="onThreadChange">
              <d-option v-for="item in threadList" :value="item.threadId" :key="item.threadId">{{item}}</d-option>
            </d-select>

             <d-button type="primary" v-if="threadType=='consume'&&searchData.threadId>=0&&item.debug==false" @click="onThreadDebug(item)" class="left mr10" style="margin-right: 100px;">打开调试</d-button>
             <d-button type="primary" v-if="threadType=='consume'&&searchData.threadId>=0&&item.debug==true" @click="onThreadDebug(item)" class="left mr10" style="margin-right: 100px;">关闭调试</d-button>

           </div>
    <my-table :data="tableData" :showPin="showTablePin" style="height: 400px;overflow-y:auto" :page="page"
              @on-size-change="handleSizeChange"
              @on-current-change="handleCurrentChange" @on-close-connection="closeConnection" @on-client-debug="onClientDebug">
    </my-table>
    </div>
</template>

<script>
import MyTable from '../../components/common/myTable'
import crud from '../../mixins/crud.js'
import apiRequest from '../../utils/apiRequest.js'

export default {
  name: 'mqtt-base-monitor',
  components: {
    MyTable
  },
  mixins: [crud],
  props: {
    executorId: {
      type: Number,
      default: 0
    },
    colData: {
      type: Array
    },
    btns: {
      type: Array
    },
    search: {
      type: String,
      default: ''
    },
    close: {
      type: String
    },
    inputable: {
      type: Boolean,
      default: true
    },
    threads: {
      type: String,
      default: ''
    },
    threadDebug: '',
    clientDebug: '',
    threadsSelect: {
      type: Boolean,
      default: false
    },
    threadType: {
      type: String,
      default: ''
    },
    clientId: {
      type: String
    }
  },
  data () {
    return {
      urls: {
        search: this.search,
        threads: this.threads,
        close: this.close,
        threadDebug: this.threadDebug,
        clientDebug: this.clientDebug
      },
      searchData: {
        clientId: this.clientId, // client id
        id: this.executorId, // executor id
        threadId: -1,
        threadType: this.threadType
      },
      tableData: {
        rowData: [],
        colData: this.colData,
        btns: this.btns
      },
      threadSelect: this.threadsSelect,
      threadList: [
      ],
      item: {
        debug: false,
        threadId: -1,
        totalClients: 0
      },
      btnHint: {
        open: '打开调试',
        close: '关闭调试'
      },
      btn: ''
    }
  },
  methods: {
    /* load thread id list */
    loadThread () {
      let path = this.searchData.id + '/type/' + this.searchData.threadType
      apiRequest.getBase(this.urls.threads + path, {}, false).then((data) => {
        data.data = data.data || []
        this.threadList = data.data
      })
    },
    onThreadChange (item) {
      this.item = JSON.parse(item.label)
      this.getList()
    },
    onThreadDebug (item) {
      let threadStatus = !item.debug
      let _this = this
      apiRequest.put(this.urls.threadDebug + threadStatus, {}, this.searchData).then((data) => {
        if (data.code === this.$store.getters.successCode) {
          this.$Message.success('成功')
          _this.loadThread()
        } else {
          this.$Message.error('失败')
        }
      })
    },
    onClientDebug (item) {
      let clientStatus = !item.status
      this.searchData.clientId = item.clientId
      let _this = this
      apiRequest.put(this.urls.clientDebug + clientStatus, {}, this.searchData).then((data) => {
        if (data.code === this.$store.getters.successCode) {
          this.$Message.success('成功')
          _this.getList()
        } else {
          this.$Message.error('失败')
        }
      })
    },
    closeConnection (item) {
      let query = {}
      let data = {
        clientId: item.clientId, // client id
        id: this.executorId // executor id
      }
      query.query = data
      let _this = this
      apiRequest.put(this.urls.close, {}, query).then((data) => {
        this.$Message.success('成功')
        _this.getList()
      })
    }
  },
  mounted () {
    if (this.threadSelect) {
      this.loadThread()
    }
  }
}
</script>

<style scoped>

</style>
