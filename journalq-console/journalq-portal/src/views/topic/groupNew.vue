<template>
  <div>
    <div style="margin-left: 20px;">
      <d-form :model="addData" inline label-width="520">
        <d-form-item label="队列数量：" required style="width: 32%">
          <d-input v-model="addData.partitions" placeholder="必填项"></d-input>
        </d-form-item>
        <d-form-item label="选举类型：" required style="width:20%">
          <d-select v-model="addData.electType" value="0" placeholder="请选择">
            <d-option :value="0">raft</d-option>
            <d-option :value="1">fix</d-option>
          </d-select>
        </d-form-item>
        <d-form-item label="推荐leader：" required style="width:30%">
          <d-select v-model="addData.recLeader" placeholder="必选项，请选择">
            <d-option v-for="item in addData.replicaGroups" :value="item.brokerId" :key="item.id">{{item.brokerId}}</d-option>
          </d-select>
        </d-form-item>
      </d-form>
    </div>
    <my-table :optional="true" :data="tableData" :showPin="showTablePin" :page="page" @on-size-change="handleSizeChange" style="height: 400px;overflow-y:auto"
              @on-current-change="handleCurrentChange" @on-selection-change="handleSelectionChange"/>
    <div class="d-dialog__footer">
      <d-button type="primary" @click.native="addNewPartitionGroup()">确定</d-button>
    </div>
  </div>
</template>

<script>
import apiRequest from '../../utils/apiRequest.js'
import myTable from '../../components/common/myTable.vue'
import crud from '../../mixins/crud.js'
export default {
  name: 'group-new',
  components: {
    myTable
  },
  props: {
    data: {
      type: Object,
      default () {
        return {}
      }
    }
  },
  mixins: [ crud ],
  data () {
    return {
      topic: {},
      namespace: {},
      addData: {
        topic: {},
        namespace: {},
        replicaGroups: [],
        partitions: 1,
        electType: 0,
        recLeader:'',
      },
      page: {
        page: 1,
        size: 10,
        total: 100
      },
      urls: {
        search: '/partitionGroupReplica/searchBrokerToAddNew',
        add: `/partitionGroup/add`
      },
      searchData: {
        keyword: ''
      },
      tableData: {
        rowData: [{}],
        colData: [
          {
            title: 'brokerId',
            key: 'id'
          },
          {
            title: '分组',
            key: 'group.code'
          },
          {
            title: 'IP',
            key: 'ip'
          },
          {
            title: '端口',
            key: 'port'
          }
        ]
      },
      multipleSelection: []
    }
  },
  methods: {
    handleSelectionChange (val) {
      var brokerIds = []
      for (var i = 0; i < val.length; i++) {
        brokerIds.push({brokerId: val[i].id})
      }
      this.multipleSelection = val
      this.addData.replicaGroups = brokerIds
      if (this.addData.replicaGroups.length == 0) {
        this.addData.recLeader = '';
      } else {
        this.addData.recLeader=this.addData.replicaGroups[0].brokerId;
      }
    },
    // 查询
    getList () {
      // 1. 查询数据库里的数据
      this.showTablePin = true
      let data = {
        pagination: {
          page: this.page.page,
          size: this.page.size
        },
        query: {
          topic: this.topic,
          namespace: this.namespace,
          keyword: this.searchData.keyword
        }
      }
      apiRequest.post(this.urlOrigin.search, {}, data).then((data) => {
        data.data = data.data || []
        data.pagination = data.pagination || {
          totalRecord: data.data.length
        }
        this.page.total = data.pagination.totalRecord
        this.page.page = data.pagination.page
        this.page.size = data.pagination.size
        this.tableData.rowData = data.data
        this.showTablePin = false
      })
    },
    addNewPartitionGroup () {
      let addData = this.addData
      if (addData.electType == null || addData.partitions <= 0) {
        this.$Dialog.success({
          content: '请输入队列数量和选举方式'
        })
        return
      }
      let _this = this
      apiRequest.post(this.urls.add, {}, addData).then((data) => {
        // this.addDialog.visible = false;
        this.$Dialog.success({
          content: '添加成功'
        })
        _this.$emit('on-partition-group-change')
        this.$emit('on-dialog-cancel')
      })
    }
  },
  mounted () {
    this.topic = this.data.topic
    this.namespace = this.data.namespace
    this.addData.topic = this.data.topic
    this.addData.namespace = this.data.namespace
    this.getList()
  }
}
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>
  .label{text-align: right; line-height: 32px;}
  .val{}
</style>
