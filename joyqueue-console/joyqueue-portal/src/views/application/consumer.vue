<template>
  <div>
    <consumer-base ref="consumerBase" :keywordTip="keywordTip" :keywordName="keywordName" :colData="colData"
                   :subscribeDialogColData="subscribeDialog.colData" :showSummaryChart="true"
                   :search="search" :subscribeUrls="subscribeDialog.urls" @on-detail="handleDetail"/>
  </div>
</template>

<script>
import consumerBase from '../monitor/consumerBase.vue'
import {getAppCode, openOrCloseBtnRender, clientTypeSelectRender,
  clientTypeBtnRender, topicTypeBtnRender, baseBtnRender, subscribeGroupAutoCompleteRender} from '../../utils/common.js'

export default {
  name: 'consumer',
  components: {
    consumerBase
  },
  props: {
    search: {// 查询条件，格式：app:{id:0,code:''}
      type: Object
    }
  },
  data () {
    return {
      keywordTip: '请输入主题',
      keywordName: '主题',
      colData: [
        {
          title: '应用',
          key: 'app.code',
          formatter (row) {
            return getAppCode(row.app, row.subscribeGroup)
          }
        },
        {
          title: '主题',
          key: 'topic.code'
        },
        {
          title: '命名空间',
          key: 'namespace.code'
        },
        // {
        //   title:'负责人',
        //   key: 'owner.code'
        // },
        {
          title: '连接数',
          key: 'connections'
        },
        {
          title: '积压数',
          key: 'pending.count'
        },
        {
          title: '出队数',
          key: 'deQuence.count'
        },
        {
          title: '重试数',
          key: 'retry.count'
        },
        {
          title: '消息类型',
          key: 'topicType',
          render: (h, params) => {
            return topicTypeBtnRender(h, params.item.topicType)
          }
        },
        {
          title: '禁止IP消费',
          key: 'config.blackList',
          // formatter (item) {
          //   return item.config === undefined ? '' : item.config.blackList
          // },
          render: (h, params) => {
            const value = params.item.config ? params.item.config.blackList : '' // '192.168.0.3,192.168.0.3,192.168.0.3,192.168.0.3,192.168.0.3,192.168.0.3,192.168.0.3'
            return h('d-tooltip', {
              props: {
                content: value
              }
            }, [h('div', {
              attrs: {
                style: 'width: 100px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis;'
              }
            }, value)]
            )
          }
        },
        {
          title: '过滤规则',
          key: 'config.filters',
          formatter (item) {
            return item.config === undefined ? '' : item.config.filters
          }
        },
        {
          title: '归档',
          key: 'config.archive',
          render: (h, params) => {
            return openOrCloseBtnRender(h, params.item.config === undefined ? undefined : params.item.config.archive)
          }
        },
        {
          title: '消费状态',
          key: 'config.paused',
          render: (h, params) => {
            let options = [
              {
                value: false,
                txt: '正常消费',
                color: 'success'
              },
              {
                value: true,
                txt: '暂停消费',
                color: 'danger'
              }
            ]
            return baseBtnRender(h, params.item.config === undefined ? undefined : params.item.config.paused, options)
          }
        },
        {
          title: '客户端类型',
          key: 'clientType',
          render: (h, params) => {
            return clientTypeBtnRender(h, params.item.clientType)
          }
        }
      ],
      // 订阅框
      subscribeDialog: {
        colData: [
          {
            title: '主题代码',
            key: 'code',
            width: '22%'
          },
          {
            title: '命名空间',
            key: 'namespace.code',
            width: '22%'
          },
          {
            title: '订阅分组',
            key: 'subscribeGroup',
            width: '22%',
            render: (h, params) => {
              return subscribeGroupAutoCompleteRender(h, params, this.$refs.consumerBase.$refs.subscribe)
            }
          },
          {
            title: '客户端类型',
            key: 'clientType',
            width: '22%',
            render: (h, params) => {
              return clientTypeSelectRender(h, params, this.$refs.consumerBase.$refs.subscribe)
            }
          }
        ],
        urls: {
          add: `/consumer/add`,
          search: `/topic/unsubscribed/search`
        }
      },
      // 消费详情
      detailDialog: {
        partition: {
          colData: []
        }
      }
      // ,
      // monitorUrls: {
      //   performance: this.$store.getters.urls.performance.consumer
      // }
    }
  },
  methods: {
    getList () {
      this.$refs.consumerBase.getList()
    },
    handleDetail (item) {
      this.$emit('on-detail', item)
    }
  },
  mounted () {
    // this.getList()
  }
}
</script>

<style scoped>

</style>
