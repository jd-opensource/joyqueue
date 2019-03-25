<template>
  <div>
    <div class="ml20 mt30">
      <!--<span style="text-align: left; width: 100px; size: 10px">类型</span>-->
      <d-select v-model="searchData.type" class="left" style="width:15%" placeholder="请选择 申请类型">
        <span slot="prepend">申请类型</span>
        <d-option value="ALL">全部</d-option>
        <d-option value="SUBSCRIBE_PRODUCER_APPLY">订阅生产者</d-option>
        <d-option value="SUBSCRIBE_CONSUMER_APPLY">订阅消费者</d-option>
        <d-option value="CANCEL_SUBSCRIBE_APPLY">取消订阅</d-option>
        <!--<d-option value="APP_USER_APPLY">申请成为APP联系人</d-option>-->
        <!--<d-option value="APP_OWNER_APPLY">申请成为APP负责人</d-option>-->
      </d-select>
      <d-button type="primary" @click="getList">查询<icon name="search" style="margin-left: 5px;"></icon></d-button>
      <d-dropdown style="margin-left: 5px;" @on-command="handleAddCommand">
        <d-button type="primary" >
          订阅申请
          <icon name="chevron-down" size="14"></icon>
        </d-button>
        <d-dropdown-menu slot="list">
          <d-dropdown-item command="subscribeProducerApply">订阅生产者</d-dropdown-item>
          <d-dropdown-item command="subscribeConsumerApply">订阅消费者</d-dropdown-item>
          <!--<d-dropdown-item command="cancelSubscribeApply">取消订阅</d-dropdown-item>-->
          <!--<d-dropdown-item command="appUserApply">申请成为APP联系人</d-dropdown-item>-->
          <!--<d-dropdown-item command="appOwnerApply">申请成为APP负责人</d-dropdown-item>-->
        </d-dropdown-menu>
      </d-dropdown>
      <d-button type="primary" @click="openDialog('cancelSubscribeApplyDialog')">取消订阅<icon name="delete" style="margin-left: 5px;"></icon></d-button>
    </div>
    <my-table :data="tableData" :showPin="showTablePin" :page="page" @on-size-change="handleSizeChange" @on-current-change="handleCurrentChange"
              @on-selection-change="handleSelectionChange"  @on-edit="edit" @on-del="del"/>
    <!--Subscribe Producer Dialog-->
    <my-dialog :dialog="subscribeProducerApplyDialog" visible="false" @on-dialog-confirm="addConfirm()" @on-dialog-cancel="dialogCancel('subscribeProducerApplyDialog')">
      <subscribe-producer-apply-form @on-dialog-cancel="dialogCancel('subscribeProducerApplyDialog') "/>
    </my-dialog>
    <!--Subscribe Consumer Dialog-->
    <my-dialog :dialog="subscribeConsumerApplyDialog" visible="false" @on-dialog-confirm="addConfirm()" @on-dialog-cancel="dialogCancel('subscribeConsumerApplyDialog')">
      <subscribe-consumer-apply-form @on-dialog-cancel="dialogCancel('subscribeConsumerApplyDialog')"/>
    </my-dialog>
    <!--Cancel Subscribe Dialog-->
    <my-dialog :dialog="cancelSubscribeApplyDialog" visible="false" @on-dialog-confirm="addConfirm()" @on-dialog-cancel="dialogCancel('subscribeConsumerApplyDialog')">
      <cancel-subscribe-apply-form @on-dialog-cancel="dialogCancel('cancelSubscribeApplyDialog')"/>
    </my-dialog>
  </div>
</template>

<script>
import myTable from '../../components/common/myTable.vue'
import myDialog from '../../components/common/myDialog.vue'
import subscribeProducerApplyForm from './subscribeProducerApplyForm.vue'
import subscribeConsumerApplyForm from './subscribeConsumerApplyForm.vue'
import cancelSubscribeApplyForm from './cancelSubscribeApplyForm.vue'
import crud from '../../mixins/crud.js'
import {format} from '../../utils/dateTimeUtils.js'

export default {
  name: 'myApply',
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
        type: 'ALL'
      },
      searchRules: {
      },
      tableData: {
        rowData: [],
        colData: [
          {
            title: 'ID',
            key: 'id'
          },
          {
            title: '申请类型',
            key: 'type',
            render: (h, params) => {
              let label
              switch (params.item.type) {
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
            title: '主题',
            key: 'topic.code'
          },
          {
            title: '应用',
            key: 'app.code'
          },
          // {
          //   title:'申请信息',
          //   key: 'payload'
          // },
          // {
          //   title:'申请描述',
          //   key: 'description'
          // },
          {
            title: '申请者',
            key: 'createBy.code'
          },
          {
            title: '申请时间',
            key: 'createTime',
            render: (h, params) => {
              return h('label', {}, format(params.item.createTime, ''))
            }
          },
          {
            title: '当前待审人',
            key: 'curAuditor'
          },
          {
            title: '驳回意见',
            key: 'suggestion'
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
                  label = '审批中'
                  break
                case 2:
                  label = '被驳回'
                  break
                case 3:
                  label = '审批通过，待确认'
                  break
                case 4:
                  label = '待执行'
                  break
                case 5:
                  label = '自动执行'
                  break
                case 6:
                  label = '已完成'
                  break
                case 7:
                  label = '执行失败'
                  break
              }
              return h('label', {}, label)
            }
          }
        ],
        btns: [
          {
            txt: '删除',
            method: 'on-del',
            bindKey: 'status',
            bindVal: 6,
            bindCond: '!='
          }
        ]
      },
      subscribeProducerApplyDialog: {
        visible: false,
        title: '订阅生产者',
        width: 700,
        showFooter: false
      },
      subscribeConsumerApplyDialog: {
        visible: false,
        title: '订阅消费者',
        width: 700,
        showFooter: false
      },
      cancelSubscribeApplyDialog: {
        visible: false,
        title: '取消订阅',
        width: 600,
        showFooter: false
      }
    }
  },
  computed: {
  },
  methods: {
    handleAddCommand (command) {
      this.openDialog(command + 'Dialog')
    },
    getSearchVal () {
      let obj = {
        pagination: {
          page: this.page.page,
          size: this.page.size
        },
        query: {
          keyword: this.searchData.keyword,
          type: this.searchData.type === 'ALL' ? '' : this.searchData.type
        }
      }
      return obj
    }
  },
  mounted () {
    console.log(this.$route.path)
    this.getList()
  }
}
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>

</style>
