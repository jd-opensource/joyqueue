<template>
  <div>
    <div class="ml20 mt30">
      <d-select v-model="searchData.applyType" class="left" style="width:15%" placeholder="请选择 申请类型">
        <span slot="prepend">申请类型</span>
        <d-option value="ALL">全部</d-option>
        <d-option value="SUBSCRIBE_PRODUCER_APPLY">订阅生产者</d-option>
        <d-option value="SUBSCRIBE_CONSUMER_APPLY">订阅消费者</d-option>
        <d-option value="CANCEL_SUBSCRIBE_APPLY">取消订阅</d-option>
        <!--<d-option value="APP_USER_APPLY">申请成为APP联系人</d-option>-->
        <!--<d-option value="APP_OWNER_APPLY">申请成为APP负责人</d-option>-->
      </d-select>
      <d-select v-model.number="searchData.status" class="left" style="width:15%" placeholder="请选择 待审批状态">
        <span slot="prepend">待处理状态</span>
        <d-option :value="-2">全部</d-option>
        <d-option :value="2">待审批</d-option>
        <d-option :value="5">待确认</d-option>
        <d-option :value="7">待执行</d-option>
      </d-select>
      <d-input v-model="searchData.keyword" placeholder="请输入申请信息" class="left" style="width: 15%"/>
      <d-button type="primary" @click="getList">查询<icon name="search" style="margin-left: 5px;"></icon></d-button>
    </div>
    <my-table :data="tableData" :showPin="showTablePin" :page="page" @on-size-change="handleSizeChange"
              @on-current-change="handleCurrentChange" @on-selection-change="handleSelectionChange" @on-del="del"
              @on-approve="approve" @on-edit="edit" @on-reject="openRejectDialog" @on-confirm="confirm" @on-execute="execute">
    </my-table>

    <!--驳回弹出框-->
    <my-dialog :dialog="rejectDialog" @on-dialog-confirm="reject" @on-dialog-cancel="dialogCancel()">
      <d-input type="textarea" rows="3" v-model="rejectData.suggestion" placeholder="请输入审批意见"></d-input>
    </my-dialog>

    <!--Subscribe Producer Dialog-->
    <my-dialog :dialog="subscribeProducerEditDialog" visible="false" @on-dialog-confirm="addConfirm()"
               @on-dialog-cancel="dialogCancel('subscribeProducerEditDialog')">
      <subscribe-producer-apply-form :data=subscribeProducerEditData :formType=$store.getters.editFormType
                                     @on-dialog-cancel="dialogCancel('subscribeProducerEditDialog') "/>
    </my-dialog>
    <!--Subscribe Consumer Dialog-->
    <my-dialog :dialog="subscribeConsumerEditDialog" visible="false" @on-dialog-confirm="addConfirm()"
               @on-dialog-cancel="dialogCancel('subscribeConsumerEditDialog')">
      <subscribe-consumer-apply-form :data=subscribeConsumerEditData :formType=$store.getters.editFormType
                                     @on-dialog-cancel="dialogCancel('subscribeConsumerEditDialog')"/>
    </my-dialog>
    <!--Cancel Subscribe Dialog-->
    <my-dialog :dialog="cancelSubscribeEditDialog" visible="false" @on-dialog-confirm="addConfirm()"
               @on-dialog-cancel="dialogCancel('cancelSubscribeEditDialog')">
      <cancel-subscribe-apply-form :data=cancelSubscribeEditData :formType=$store.getters.editFormType
                                   @on-dialog-cancel="dialogCancel('cancelSubscribeEditDialog')"/>
    </my-dialog>

  </div>
</template>

<script>
import myTable from '../../components/common/myTable.vue'
import myDialog from '../../components/common/myDialog.vue'
import crud from '../../mixins/crud.js'
import apiRequest from '../../utils/apiRequest.js'
import {format} from '../../utils/dateTimeUtils.js'
import subscribeProducerApplyForm from './subscribeProducerApplyForm.vue'
import subscribeConsumerApplyForm from './subscribeConsumerApplyForm.vue'
import cancelSubscribeApplyForm from './cancelSubscribeApplyForm.vue'
import {deepCopy} from '../../utils/assist.js'

export default {
  name: 'application',
  components: {
    myTable,
    myDialog,
    subscribeProducerApplyForm,
    subscribeConsumerApplyForm,
    cancelSubscribeApplyForm
  },
  mixins: [ crud ],
  data () {
    return {
      searchData: {
        keyword: '',
        applyType: 'ALL',
        status: -2
      },
      rejectDialog: {
        visible: false,
        title: '同步应用',
        showFooter: true
      },
      rejectData: {},
      tableData: {
        rowData: [],
        colData: [
          {
            title: 'ID',
            key: 'id'
          },
          {
            title: '申请类型',
            key: 'apply.type',
            render: (h, params) => {
              let label
              switch (params.item.apply.type) {
                case 'SUBSCRIBE_PRODUCER_APPLY':
                  label = '订阅生产者'
                  break
                case 'SUBSCRIBE_CONSUMER_APPLY':
                  label = '订阅消费者'
                  break
                case 'CANCEL_SUBSCRIBE_APPLY':
                  label = '取消订阅'
                  break
                case 'APP_USER_APPLY':
                  label = '申请成为APP联系人'
                  break
                case 'APP_OWNER_APPLY':
                  label = '申请成为APP负责人'
                  break
              }
              return h('label', {}, label)
            }
          },
          {
            title: '申请ID',
            key: 'apply.id'
          },
          {
            title: '主题',
            key: 'apply.topic.code'
          },
          {
            title: '应用',
            key: 'apply.app.code'
          },
          {
            title: '申请描述',
            key: 'apply.description'
          },
          {
            title: '申请人',
            key: 'apply.createBy.code'
          },
          {
            title: '申请时间',
            key: 'apply.createTime',
            render: (h, params) => {
              return h('label', {}, format(params.item.createTime, ''))
            }
          },
          {
            title: '状态',
            key: 'status',
            render: (h, params) => {
              let label
              switch (params.item.status) {
                case -1:
                  label = '已删除'
                  break
                case 0:
                  label = '已取消'
                  break
                case 1:
                  label = '新建'
                  break
                case 2:
                  label = '待审批'
                  break
                case 3:
                  label = '已驳回'
                  break
                case 4:
                  label = '审批通过'
                  break
                case 5:
                  label = '待确认'
                  break
                case 6:
                  label = '已确认'
                  break
                case 7:
                  label = '待执行'
                  break
                case 8:
                  label = '已完成'
                  break
              }
              return h('label', {}, label)
            }
          }
        ],
        btns: [
          // {
          //   txt: '查看详情',
          //   method: 'on-detail'
          // },
          {
            txt: '编辑',
            method: 'on-edit',
            isAdmin: true
          },
          {
            txt: '批准',
            method: 'on-approve',
            bindKey: 'status',
            bindVal: 2
          },
          {
            txt: '驳回',
            method: 'on-reject',
            bindKey: 'status',
            bindVal: 2
          },
          {
            txt: '确认',
            method: 'on-confirm',
            bindKey: 'status',
            bindVal: 5
          },
          {
            txt: '执行',
            method: 'on-execute',
            bindKey: 'status',
            bindVal: 7
          },
          {
            txt: '删除',
            method: 'on-del'
          }
        ]
      },
      subscribeProducerEditDialog: {
        visible: false,
        title: '订阅生产者',
        width: 700,
        showFooter: false
      },
      subscribeProducerEditData: {},
      subscribeConsumerEditDialog: {
        visible: false,
        title: '订阅消费者',
        width: 700,
        showFooter: false
      },
      subscribeConsumerEditData: {},
      cancelSubscribeEditDialog: {
        visible: false,
        title: '取消订阅',
        width: 600,
        showFooter: false
      },
      cancelSubscribeEditData: {}
    }
  },
  computed: {
  },
  methods: {
    openRejectDialog (item) {
      this.rejectData = item
      this.rejectDialog.visible = true
    },
    dialogCancel (dialog) {
      this[dialog].visible = false
      this.getList()
    },
    getSearchVal () {
      let obj = {
        pagination: {
          page: this.page.page,
          size: this.page.size
        },
        query: {
          keyword: this.searchData.keyword,
          applyType: this.searchData.applyType === 'ALL' ? null : this.searchData.applyType,
          status: this.searchData.status === -2 ? null : this.searchData.status
        }
      }
      return obj
    },
    edit (item) {
      // Find apply data
      let applyData = {}
      apiRequest.get(this.urlOrigin.findApply + '/' + item.id, {}, {}).then((data) => {
        applyData = data.data || {}
        // Set edit data and show dialog
        if (item.apply.type === 'SUBSCRIBE_PRODUCER_APPLY') {
          this.subscribeProducerEditData = deepCopy(applyData)
          this.subscribeProducerEditDialog.visible = true
        } else if (item.apply.type === 'SUBSCRIBE_CONSUMER_APPLY') {
          this.subscribeConsumerEditData = deepCopy(applyData)
          // this.subscribeConsumerEditData.topic.subscribeGroupExist = applyData.subscribeGroup?true:false;
          this.subscribeConsumerEditDialog.visible = true
        } else if (item.apply.type === 'CANCEL_SUBSCRIBE_APPLY') {
          this.cancelSubscribeEditData = deepCopy(applyData)
          this.cancelSubscribeEditData.subscribeType = (applyData.subscribeType === 'PRODUCER'
            ? this.$store.getters.producerType : this.$store.getters.consumerType)
          this.cancelSubscribeEditDialog.visible = true
        }
      })
    },
    approve (item) {
      let data = Object.assign({}, item)
      this.update(this.urlOrigin.approve + '/' + data.id, {}, '审批成功', '审批失败')
    },
    reject (item) {
      apiRequest.put(this.urlOrigin.reject + '/' + this.rejectData.id, {}, this.rejectData.suggestion).then((data) => {
        this.rejectDialog.visible = false
        this.$Message.success('驳回成功')
        this.getList()
      })
    },
    confirm (item) {
      let data = Object.assign({}, item)
      this.update(this.urlOrigin.confirm + '/' + data.id, {}, '确认成功', '确认失败')
    },
    execute (item) {
      let data = Object.assign({}, item)
      this.update(this.urlOrigin.execute + '/' + data.id, {}, '执行成功', '执行失败')
    }
  },
  mounted () {
    this.getList()
  }
}
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>

</style>
