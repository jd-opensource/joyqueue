<template>
  <div>
    <consumer-base ref="consumerBase" :keywordTip="keywordTip" :keywordName="keywordName" :colData="colData"
                   :subscribeDialogColData="subscribeDialog.colData"
                   :search="search" :subscribeUrls="subscribeDialog.urls"  @on-detail="handleDetail"/>
  </div>
</template>

<script>
import Vue from 'vue'
import consumerBase from '../monitor/consumerBase.vue'
import {getAppCode, openOrCloseBtnRender, clientTypeSelectRender,
  clientTypeBtnRender, topicTypeBtnRender, baseBtnRender, subscribeGroupAutoCompleteRender} from '../../utils/common.js'

export default {
  name: 'consumer',
  components: {
    consumerBase
  },
  props: {
    search: {// 查询条件，格式：topic:{id:0,code:'',namespace:{id:0,code:''}}
      type: Object
    }
  },
  data () {
    return {
      keywordTip: '请输入应用',
      keywordName: '应用',
      isRetryEnable: false,
      colData: [
          {
            title: '应用',
            key: 'app.code',
            width: 80,
            render: (h, params) => {
                const app = params.item.app
                const appFullName = getAppCode(app, params.item.subscribeGroup)
                return h('d-button', {
                    props: {
                        type: 'borderless',
                        color: 'primary'
                    },
                    style: {
                        color: '#3366FF'
                    },
                    on: {
                        click: () => {
                            this.$router.push({
                                name: `/${this.$i18n.locale}/application/detail`,
                                query: {
                                    id: app.id,
                                    app: app.code,
                                    tab: 'consumer'
                                }
                            })
                        }
                    }
                }, appFullName)
            }
        },
        {
          title: '主题',
          key: 'topic.code',
          width: 100
        },
        {
          title: '命名空间',
          key: 'namespace.code'
        },
        {
          title: '连接数',
          key: 'connections',
          width: 100,
          render: (h, params) => {
            const connections = params.item.connections
            const formatNumFilter = Vue.filter('formatNum')
            return h('label', formatNumFilter(connections))
          }
        },
        {
          title: '积压数',
          key: 'pending.count',
          width: 150,
          render: (h, params) => {
              const pending = params.item.pending
              if (!pending) {
                return h('label', '')
              } else {
                const formatNumFilter = Vue.filter('formatNum')
                return h('label', formatNumFilter(pending.count))
              }
          }
        },
        {
          title: '出队数',
          key: 'deQuence.count',
          width: 150,
          render: (h, params) => {
            const deQuence = params.item.deQuence
            if (!deQuence) {
              return h('label', '')
            } else {
              const formatNumFilter = Vue.filter('formatNum')
              return h('label', formatNumFilter(deQuence.count))
            }
          }
        },
        {
          title: '重试数',
          key: 'retry.count',
          width: 100,
          render: (h, params) => {
            const retry = params.item.retry
            const formatNumFilter = Vue.filter('formatNum')
            return h('label', {
              style: {
                  cursor: 'pointer',
                  color: '#3366FF'
              },
              on: {
                click: () => {
                  this.$router.push({
                    name: `/${this.$i18n.locale}/topic/detail`,
                    query: {
                      id: params.item.topic.code,
                      app: getAppCode(params.item.app, params.item.subscribeGroup),
                      topic: params.item.topic.code,
                      namespace: params.item.namespace.code,
                      tab: 'retry'
                    }
                  })
                }
              }
            }, retry === undefined ? 0 : formatNumFilter(retry.count))
          }
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
          render: (h, params) => {
            const value = params.item.config ? params.item.config.blackList : ''
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
          key: 'paused',
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
            title: 'ID',
            key: 'id',
            width: '25%'
          },
          {
            title: '应用代码',
            key: 'code',
            width: '25%'
          },
          {
            title: '订阅分组',
            key: 'subscribeGroup',
            width: '25%',
            render: (h, params) => {
              return subscribeGroupAutoCompleteRender(h, params, this.$refs.consumerBase.$refs.subscribe)
            }
          },
          {
            title: '客户端类型',
            key: 'clientType',
            width: '25%',
            render: (h, params) => {
              return clientTypeSelectRender(h, params, this.$refs.consumerBase.$refs.subscribe)
            }
          }
        ],
        urls: {
          add: `/consumer/add`,
          search: `/application/unsubscribed/search`
        }
      },
      // 消费详情
      detailDialog: {
        partition: {
          colData: []
        }
      }
    }
  },
  methods: {
    getList () {
      this.$refs.consumerBase.getList()
    },
    handleDetail (item) {
      this.$emit('on-detail', item)
    }
  }
}
</script>

<style scoped>

</style>
