<template>
  <div>
    <div class="ml20 mt10">
      <d-input v-model="keyword" oninput="value = value.trim()" :placeholder="keywordTip" class="left" style="width: 400px" @on-enter="getList">
        <span slot="prepend">{{keywordName}}</span>
        <icon name="search" size="14" color="#CACACA" slot="suffix" @click="getList"></icon>
      </d-input>
    </div>
    <my-table :data="tableData" :showPin="showTablePin" :page="page" @on-size-change="handleSizeChange"
              @on-current-change="handleCurrentChange" @on-selection-change="handleSelectionChange" @on-add="add">
    </my-table>
  </div>
</template>

<script>
import apiRequest from '../../utils/apiRequest.js'
import myTable from '../../components/common/myTable.vue'
import crud from '../../mixins/crud.js'
import apiUrl from '../../utils/apiUrl.js'

export default {
  name: 'subscribe',
  components: {
    myTable
  },
  props: {
    keywordTip: {
      type: String,
      default: '请输入代码'
    },
    keywordName: {
      type: String
    },
    operates: {
      type: Array
    },
    colData: {// 必输
      type: Array
    },
    btns: {
      type: Array,
      default: function () {
        return [
          {
            txt: '订 阅',
            method: 'on-add'
          }
        ]
      }
    },
    search: {// 查询条件，我的应用：app:{id:0,code:''}  ， 主题中心：topic:{id:0,code:'',namespace:{id:0,code:''}}
      type: Object
    },
    type: { // 1-生产者， 2-消费者
      type: Number,
      default: 1
    },
    addUrl: '',
    searchUrl: ''
  },
  mixins: [ crud ],
  data () {
    return {
      urls: {
        search: this.searchUrl,
        add: this.addUrl
      },
      keyword: '',
      tableData: {
        rowData: [],
        colData: this.colData,
        btns: this.btns,
        operates: this.operates
      },
      multipleSelection: [],
      subscribeGroupList: []
    }
  },
  methods: {
    // 查询
    getList () {
      this.showTablePin = true
      let data = {
        pagination: {
          page: this.page.page,
          size: this.page.size
        },
        query: {
          keyword: this.keyword,
          subscribeType: this.type
        }
      }

      for (let key in this.search) {
        if (this.search.hasOwnProperty(key)) {
          data.query[key] = this.search[key]
        }
      }
      // this.tableData.rowData = [] // 先清空数据
      apiRequest.post(this.urlOrigin.search, {}, data).then((data) => {
        data.data = data.data || []
        data.pagination = data.pagination || {
          totalRecord: data.data.length
        }
        this.page.total = data.pagination.totalRecord
        this.page.page = data.pagination.page
        this.page.size = data.pagination.size
        this.tableData.rowData = data.data.map(d => {
          d['subscribeGroup'] = ''
          return d
        })
        this.showTablePin = false
      })
    },
    // 订阅
    add (item) {
      let data = {}
      // Validate
      // Consumer must input clientType and subscribeGroup.
      // Producer must input clientType.
      if (item.clientType === undefined) {
        this.$Dialog.warning({
          content: '请先输入客户端类型！'
        })
        return
      }
      data.clientType = item.clientType
      // Subscribe group format verification
      if (this.type === this.$store.getters.consumerType) {
        if (item.subscribeGroup && !item.subscribeGroup.match(this.$store.getters.pattern.subscribeGroup)) {
          this.$Message.error('订阅分组格式不匹配！支持格式：' + this.$store.getters.placeholder.subscribeGroup)
          return
        }
        data.subscribeGroup = item.subscribeGroup || ''
      }
      for (let key in this.search) {
        if (this.search.hasOwnProperty(key)) {
          data[key] = this.search[key]
          if (key === 'app') {
            data.topic = {
              id: item.id,
              code: item.code
            }
            data.namespace = item.namespace
          } else if (key === 'topic') {
            data.app = {
              id: item.id,
              code: item.code
            }
            // data.namespace = this.search.topic.namespace
          }
        }
      }

      let _this = this
      this.$Dialog.confirm({
        title: '提示',
        content: '确定要订阅吗？'
      }).then(() => {
        apiRequest.post(_this.addUrl, null, data).then(() => {
          // 移除数据
          this.tableData.rowData = []
          // 刷新
          _this.getList()
          _this.$emit('on-refresh')
        })
      }).catch(() => {
      })
    },
    getSubscribeGroups () {
      apiRequest.get(apiUrl.common.findSubscribeGroup).then((data) => {
        this.subscribeGroupList = data.data || []
      })
    },
    querySubscribeGroup (queryString, callback) {
      let subscribeGroups = this.subscribeGroupList
      let results = queryString ? subscribeGroups.filter(subscribeGroup => {
        return subscribeGroup.toLowerCase().indexOf(queryString.toLowerCase().trim()) === 0
      }) : subscribeGroups
      // 调用 callback 返回建议列表的数据
      callback(results)
    }
  },
  mounted () {
    // this.getList()
  }
}
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>

</style>
