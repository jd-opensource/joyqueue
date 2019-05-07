<template>
  <div>
    <div class="ml20 mt30">
      <d-button type="primary" @click="groupNew" class="left mr10">
        扩容
        <icon name="plus-circle" style="margin-left: 5px;"/>
      </d-button>
      <slot name="extendBtns"></slot>
      <d-button type="primary" v-if="showBrokerChart" @click="goBrokerChart" class="left mr10">
        Broker监控
        <icon name="bar-chart" style="margin-left: 5px;"/>
      </d-button>
      <d-button type="primary" v-if="showHostChart" @click="goHostChart" class="left mr10">
        主机监控
        <icon name="bar-chart" style="margin-left: 5px;"/>
      </d-button>
    </div>
    <my-table :data="tableData" :showPin="showTablePin" :page="page" @on-size-change="handleSizeChange"
              @on-current-change="handleCurrentChange" @on-view-detail="goDetail" @on-scale="groupScale"
              @on-merge="groupMerge" @on-del="del" @on-addPartition="addPartition" @on-removePartition="removePartition" @on-position="goPosition">
    </my-table>

    <!--详情-->
    <my-dialog :dialog="groupDetailDialog" @on-dialog-confirm="groupDetailConfirm()" @on-dialog-cancel="groupDetailCancel()">
      <group-detail :data="groupDetailDialogData"></group-detail>
    </my-dialog>
    <!--添加节点-->
    <my-dialog :dialog="groupScaleDialog" @on-dialog-confirm="groupScaleConfirm()" @on-dialog-cancel="groupScaleCancel()" >
      <group-scale :data="groupScaleDialogData"  ></group-scale>
    </my-dialog>
    <!--移除节点-->
    <my-dialog :dialog="groupMergeDialog" @on-dialog-confirm="groupMergeConfirm()" @on-dialog-cancel="groupMergeCancel()"  >
      <group-merge :data="groupMergeDialogData"  ></group-merge>
    </my-dialog>
    <!--主从同步-->
    <my-dialog :dialog="positionDialog" @on-dialog-confirm="positionConfirm()" @on-dialog-cancel="positionCancel()">
      <group-position :data="positionDialogData"></group-position>
    </my-dialog>
    <!--增加分区数-->
    <my-dialog :dialog="addPartitionDialog" @on-dialog-confirm="addPartitionConfirm()" @on-dialog-cancel="addPartitionCancel()"  >
      <d-input v-model="addPartitionDialogData.partitionCount" placeholder="请输入增加分区数" style="width: 400px">
        <span slot="prepend">增加分区数</span>
      </d-input>
    </my-dialog>
    <!--减少分区数-->
    <my-dialog :dialog="removePartitionDialog" @on-dialog-confirm="removePartitionConfirm()" @on-dialog-cancel="removePartitionCancel()"  >
      <d-input v-model="removePartitionDialogData.partitionCount" placeholder="请输入减少分区数" style="width: 400px">
        <span slot="prepend">减少分区数</span>
      </d-input>
    </my-dialog>
    <!--扩容-->
    <my-dialog :dialog="groupNewDialog" @on-dialog-confirm="groupNewConfirm()" @on-dialog-cancel="groupNewCancel()">
      <group-new :data="groupNewDialogData" @on-dialog-confirm="groupNewConfirm()" @on-dialog-cancel="groupNewCancel()"  @on-partition-group-change="topicUpdate"></group-new>
    </my-dialog>
  </div>
</template>

<script>
import apiRequest from '../../utils/apiRequest.js'
import myTable from '../../components/common/myTable.vue'
import myDialog from '../../components/common/myDialog.vue'
import groupDetail from './groupDetail.vue'
import groupScale from './groupScale.vue'
import GroupPosition from './groupPosition.vue'
import groupMerge from './groupMerge.vue'
import groupNew from './groupNew.vue'
import crud from '../../mixins/crud.js'
import {getTopicCode} from '../../utils/common.js'

export default {
  name: 'partitionGroup',
  components: {
    GroupPosition,
    myTable,
    myDialog,
    groupDetail,
    groupScale,
    groupMerge,
    groupNew
  },
  mixins: [ crud ],
  props: {
    showHostChart: {
      type: Boolean,
      default: false
    },
    showBrokerChart: {
      type: Boolean,
      default: false
    }
  },
  data () {
    return {
      urls: {
        search: `/partitionGroup/search`,
        del: `/partitionGroup/delete`,
        getBroker: `/broker/get`,
        addPartition: `/partitionGroup/addPartition`,
        removePartition: `/partitionGroup/removePartition`,
        getMonitor: `/monitor`,
        getUrl: `/grafana/getRedirectUrl`
      },
      searchData: {
        topic: {
          id: this.$route.query.id,
          code: this.$route.query.code
        },
        namespace: {
          id: this.$route.query.namespaceId,
          code: this.$route.query.namespaceCode
        },
        keyword: ''
      },
      tableData: {
        rowData: [],
        colData: [
          {
            title: 'group',
            width: '2%',
            key: 'groupNo'
          },
          {
            title: 'partitions',
            width: '15%',
            key: 'partitions'
          },
          {
            title: '选举类型',
            width: '5%',
            key: 'electType',
            render: (h, params) => {
              let label
              switch (params.item.electType) {
                case 0:
                  label = 'raft'
                  break
                case 1:
                  label = 'fix'
                  break
              }
              return h('label', {}, label)
            }
          },
          {
            title: '副本数',
            width: '2%',
            key: 'replicas.size'
          },
          {
            title: '当前leader',
            width: '15%',
            key: 'ip',
            formatter (item) {
              return `${item.leader}:${item.ip}`
            }
          },
          {
            title: '推荐leader',
            width: '10%',
            key: 'recLeader'
          },
          {
            title: 'isr',
            key: 'isr'
          },
          {
            title: 'term',
            width: '2%',
            key: 'term'
          }
        ],
        btns: [
          {
            txt: '详情',
            method: 'on-view-detail'
          },
          {
            txt: '增加副本数',
            method: 'on-scale'
          },
          {
            txt: '减副本数',
            method: 'on-merge'
          },
          {
            txt: '删除',
            method: 'on-del'
          },
          {
            txt: '增加分区',
            method: 'on-addPartition'
          },
          {
            txt: '减少分区',
            method: 'on-removePartition'
          },
          {
            txt: '主从同步监控',
            method: 'on-position'
          }
        ]
      },
      groupDetailDialog: {
        visible: false,
        title: '详情',
        width: 800,
        showFooter: false
      },
      groupDetailDialogData: {},
      positionDialog: {
        visible: false,
        title: '主从同步监控',
        width: 800,
        showFooter: false
      },
      positionDialogData: {},
      groupScaleDialog: {
        visible: false,
        title: '增加副本',
        width: 800,
        showFooter: false
      },
      groupScaleDialogData: {},
      groupMergeDialog: {
        visible: false,
        title: '减少副本',
        width: 800,
        showFooter: false
      },
      groupMergeDialogData: {},
      addPartitionDialog: {
        visible: false,
        title: '增加分区数',
        width: 500,
        showFooter: true
      },
      addPartitionDialogData: {},
      removePartitionDialog: {
        visible: false,
        title: '减少分区数',
        width: 800,
        showFooter: true
      },
      removePartitionDialogData: {},
      groupNewDialog: {
        visible: false,
        title: '详情',
        width: 800,
        showFooter: false
      },
      groupNewDialogData: {
        topic: this.$route.query.topic,
        namespace: this.$route.query.namespace,
        electType: 0,
        replicaGroups: [],
        partitions: 0
      },
      newGroupData: {
      }
    }
  },
  computed: {
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
          topic: this.searchData.topic,
          namespace: this.searchData.namespace,
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
        for (var i = 0; i < this.tableData.rowData.length; i++) {
          this.getBroker(this.tableData.rowData, i)
        }
        this.showTablePin = false
      })
    },
    getBroker (rowData, i) {
      apiRequest.get(this.urlOrigin.getBroker + '/' + rowData[i].leader).then((data) => {
        this.tableData.rowData[i].ip = data.data.ip
        this.$set(this.tableData.rowData, i, this.tableData.rowData[i])
      })
    },
    goBrokerChart () {
      apiRequest.get(this.urls.getUrl + '/broker', {}, {}).then((data) => {
        let url = data.data || ''
        if (url.indexOf('?') < 0) {
          url += '?'
        } else if (!url.endsWith('?')) {
          url += '&'
        }
        url = url + 'var-topic=' + getTopicCode(this.searchData.topic, this.searchData.namespace)
        window.open(url)
      })
    },
    goHostChart () {
      apiRequest.get(this.urls.getUrl + '/host', {}, {}).then((data) => {
        let url = data.data || ''
        if (url.indexOf('?') < 0) {
          url += '?'
        } else if (!url.endsWith('?')) {
          url += '&'
        }
        url = url + 'var-topic=' + getTopicCode(this.searchData.topic, this.searchData.namespace)
        window.open(url)
      })
    },
    goDetail (item) {
      this.groupDetailDialogData = {groupNo: item.groupNo, topic: this.searchData.topic, namespace: this.searchData.namespace}
      this.groupDetailDialog.visible = true
    },
    groupDetailConfirm () {

    },
    groupDetailCancel () {
      this.groupDetailDialog.visible = false
    },
    goPosition (item) {
      this.positionDialogData = {groupNo: item.groupNo, topic: this.searchData.topic, namespace: this.searchData.namespace}
      this.positionDialog.visible = true
    },
    positionConfirm () {

    },
    positionCancel () {
      this.positionDialog.visible = false
    },
    groupScale (item) {
      this.groupScaleDialogData = {groupNo: item.groupNo, topic: {id: item.topic.id, code: item.topic.code}, namespace: {id: item.namespace.id, code: item.namespace.code}}
      this.groupScaleDialog.visible = true
    },
    groupScaleConfirm () {

    },
    groupScaleCancel () {
      this.groupScaleDialog.visible = false
    },
    groupNewConfirm () {

    },
    groupNewCancel () {
      this.groupNewDialog.visible = false
      this.getList()
    },
    groupNew () {
      this.groupNewDialogData = {topic: this.searchData.topic, namespace: this.searchData.namespace}
      this.groupNewDialog.visible = true
    },
    groupMerge (item) {
      this.groupMergeDialog.visible = true
      this.groupMergeDialogData = {groupNo: item.groupNo, topic: {id: item.topic.id, code: item.topic.code}, namespace: {id: item.namespace.id, code: item.namespace.code}}
    },
    groupMergeConfirm () {

    },
    groupMergeCancel () {
      this.groupMergeDialog.visible = false
      this.getList()
    },
    addPartition (item) {
      this.addPartitionDialog.visible = true
      this.addPartitionDialogData = item
    },
    addPartitionConfirm () {
      if (this.addPartitionDialogData.partitionsCount <= 0) {
        return
      }
      apiRequest.post(this.urls.addPartition, {}, this.addPartitionDialogData).then(() => {
        this.addPartitionDialog.visible = false
        this.getList()
      })
    },
    addPartitionCancel () {
      this.addPartitionDialog.visible = false
    },
    removePartition (item) {
      this.removePartitionDialog.visible = true
      this.removePartitionDialogData = item
    },
    removePartitionConfirm () {
      if (this.removePartitionDialogData.partitionsCount <= 0) {
        return
      }
      apiRequest.post(this.urls.removePartition, {}, this.removePartitionDialogData).then((data) => {
        this.removePartitionDialog.visible = false
        this.getList()
      })
    },
    removePartitionCancel () {
      this.removePartitionDialog.visible = false
    },
    del (item) {
      var data = item
      apiRequest.post(this.urls.del, {}, data).then(() => {
        this.getList()
      })
    },
    topicUpdate () {
      this.$emit('on-partition-group-change')
    },
    afterDel () {
      this.topicUpdate()
    }
  },
  mounted () {
    // this.getList();
  }
}
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>
  .label{text-align: right; line-height: 32px;}
  .val{}
</style>
