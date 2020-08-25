<template>
  <div>
    <consumer-base ref="consumerBase" :keywordTip="keywordTip" :showPagination="false" :keywordName="keywordName"
                   :colData="colData" :operates="operates" :btns="btns"
                   :subscribeDialogColData="subscribeDialog.colData" :showSummaryChart="true"
                   :search="search" :subscribeUrls="subscribeDialog.urls" @on-detail="handleDetail"/>
  </div>
</template>

<script>
import Vue from 'vue'
import consumerBase from '../monitor/consumerBase.vue'
import { getAppCode, openOrCloseBtnRender, clientTypeSelectRender, clientTypeSelectRender2,
  clientTypeBtnRender, topicTypeBtnRender, baseBtnRender, subscribeGroupAutoCompleteRender, getTopicCodeByCode } from '../../utils/common.js'

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
      btns: [
        {
          txt: '消费详情',
          method: 'on-detail'
        },
        {
          txt: '配置',
          method: 'on-config'
        },
        {
          txt: '取消订阅',
          method: 'on-cancel-subscribe',
          isAdmin: 1
        }
      ],
      operates: [
        {
          txt: '消费者策略详情',
          method: 'on-consumer-policy',
          isAdmin: true
        },
        {
          txt: '消息预览',
          method: 'on-msg-preview'
        },
        {
          txt: '消息查询',
          method: 'on-msg-detail'
        },
        {
          txt: '限流',
          method: 'on-rateLimit'
        }
      ],
      colData: [
        {
          title: '应用',
          key: 'app.code',
          width: '8%',
          formatter (row) {
            return getAppCode(row.app, row.subscribeGroup)
          }
        },
        {
          title: '主题',
          key: 'topic.code',
          width: '11%',
          render: (h, params) => {
            const topic = params.item.topic
            const namespace = params.item.namespace
            const topicId = getTopicCodeByCode(topic.code, namespace.code)
            return h('label', {
              style: {
                color: '#3366FF'
              },
              on: {
                click: () => {
                  this.$router.push({name: `/${this.$i18n.locale}/topic/detail`,
                    query: { id: topicId, topic: topic.code, namespace: topic.namespace.code || namespace.code, tab: 'consumer' }})
                },
                mousemove: (event) => {
                  event.target.style.cursor = 'pointer'
                }
              }
            }, topic.code)
          }
        },
        // {
        //   title: '命名空间',
        //   key: 'namespace.code'
        // },
        // {
        //   title:'负责人',
        //   key: 'owner.code'
        // },
        {
          title: '连接数',
          key: 'connections',
          width: '8%',
          render: (h, params) => {
            let html = []
            let spin = h('d-spin', {
              attrs: {
                size: 'small'
              },
              style: {
                display: (params.item.connections !== undefined) ? 'none' : 'inline-block'
              }
            })
            html.push(spin)
            let connections = params.item.connections
            if (connections === undefined) {
              return h('div', {}, html)
            } else if (connections === 'unknown') {
              return h('icon', {
                style: {
                  color: 'red'
                },
                props: {
                  name: 'x-circle'
                }
              })
            } else {
              const formatNumFilter = Vue.filter('formatNum')
              let textSpan = h('label', {
                style: {
                  position: 'relative',
                  display: (params.item.connections === undefined) ? 'none' : 'inline-block'
                }
              }, formatNumFilter(connections))
              html.push(textSpan)
              return h('div', {}, html)
            }
          }
        },
        {
          title: '积压数',
          key: 'pending.count',
          width: '10%',
          render: (h, params) => {
            let html = []
            let spin = h('d-spin', {
              attrs: {
                size: 'small'
              },
              style: {
                display: params.item.pending !== undefined ? 'none' : 'inline-block'
              }
            })
            html.push(spin)
            let pending = params.item.pending
            if (pending === undefined) {
              return h('div', {}, html)
            } else if (pending === 'unknown') {
              return h('icon', {
                style: {
                  color: 'red'
                },
                props: {
                  name: 'x-circle'
                }
              })
            } else {
              const formatNumFilter = Vue.filter('formatNum')
              let textSpan = h('label', {
                style: {
                  position: 'relative',
                  display: params.item.pending === undefined ? 'none' : 'inline-block'
                }
              }, formatNumFilter(pending.count))
              html.push(textSpan)
              return h('div', {}, html)
            }
          }
        },
        {
          title: '出队数',
          key: 'deQuence.count',
          width: '13%',
          render: (h, params) => {
            let html = []
            let spin = h('d-spin', {
              attrs: {
                size: 'small'
              },
              style: {
                display: params.item.deQuence !== undefined ? 'none' : 'inline-block'
              }
            })
            html.push(spin)
            const deQuence = params.item.deQuence
            if (!deQuence) {
              let label = h('label', '')
              html.push(label)
              return h('div', {}, html)
            } else if (deQuence === 'unknown') {
              return h('icon', {
                style: {
                  color: 'red'
                },
                props: {
                  name: 'x-circle'
                }
              })
            } else {
              const formatNumFilter = Vue.filter('formatNum')
              let textSpan = h('label', {
                style: {
                  position: 'relative',
                  display: params.item.deQuence.count === undefined ? 'none' : 'inline-block'
                }
              }, formatNumFilter(deQuence.count))
              html.push(textSpan)
              return h('div', {}, html)
            }
          }
        },
        {
          title: '重试数',
          key: 'retry.count',
          width: '10%',
          render: (h, params) => {
            let html = []
            let spin = h('d-spin', {
              attrs: {
                size: 'small'
              },
              style: {
                display: params.item.retry !== undefined ? 'none' : 'inline-block'
              }
            })
            html.push(spin)
            const retry = params.item.retry
            if (!retry) {
              let label = h('label', '')
              html.push(label)
              return h('div', {}, html)
            } else if (retry === 'unknown') {
              return h('icon', {
                style: {
                  color: 'red'
                },
                props: {
                  name: 'x-circle'
                }
              })
            } else {
              const formatNumFilter = Vue.filter('formatNum')
              let textSpan = h('label', {
                style: {
                  cursor: 'pointer',
                  color: '#3366FF',
                  position: 'relative',
                  display: params.item.retry.count === undefined ? 'none' : 'inline-block'
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
                  },
                  mousemove: (event) => {
                    event.target.style.cursor = 'pointer'
                  }
                }
              }, formatNumFilter(retry.count))
              html.push(textSpan)
              return h('div', {}, html)
            }
          }
        },
        {
          title: '消息类型',
          key: 'topicType',
          width: '5%',
          render: (h, params) => {
            return topicTypeBtnRender(h, params.item.topicType)
          }
        },
        {
          title: '归档',
          key: 'config.archive',
          width: '4%',
          render: (h, params) => {
            return openOrCloseBtnRender(h, params.item.config === undefined ? undefined : params.item.config.archive)
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
          title: '消费状态',
          key: 'config.paused',
          width: '5%',
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
          width: '5%',
          render: (h, params) => {
            if (this.$store.getters.isAdmin) {
              return clientTypeSelectRender2(h, params, 'consumer')
            } else {
              return clientTypeBtnRender(h, params.item.clientType)
            }
          }
        },
        {
          title: '禁止IP消费',
          key: 'config.blackList',
          width: '4%',
          render: (h, params) => {
            const value = params.item.config ? params.item.config.blackList : ''
            return h('d-tooltip', {
              props: {
                content: value
              }
            }, [h('div', {
              attrs: {
                style: 'width: 50px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis;'
              }
            }, value)]
            )
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
          search: `/topic/unsubscribed/search`,
          del: `/consumer/delete/`
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
