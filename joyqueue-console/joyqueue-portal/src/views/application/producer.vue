<template>
  <div>
    <producer-base ref="producerBase" :keywordTip="keywordTip" :showPagination="false" :keywordName="keywordName"
                   :colData="colData" :btns="btns" :operates="operates" @on-enter="getList"
                   :subscribeDialogColData="subscribeDialog.colData" :showSummaryChart="true"
                   :search="search" :subscribeUrls="subscribeDialog.urls" @on-detail="handleDetail"/>
  </div>
</template>

<script>
import Vue from 'vue'
import producerBase from '../monitor/producerBase.vue'
import { clientTypeSelectRender2, openOrCloseBtnRender, clientTypeSelectRender, clientTypeBtnRender, getTopicCodeByCode } from '../../utils/common.js'

export default {
  name: 'producer',
  components: {
    producerBase
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
          txt: '生产详情',
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
          txt: '生产者策略详情',
          method: 'on-producer-policy',
          isAdmin: true
        },
        {
          txt: '设置生产权重',
          method: 'on-weight',
          isAdmin: true
        },
        {
          txt: '发送消息',
          method: 'on-send-message'
        },
        {
          txt: '限流',
          method: 'on-rateLimit'
        }
      ],
      colData: [
        {
          title: '主题',
          key: 'topic.code',
          width: '15%',
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
                    query: { id: topicId, topic: topic.code, namespace: topic.namespace.code || namespace.code, tab: 'producer' }})
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
        //   title: '负责人',
        //   key: 'owner.code'
        // },
        {
          title: '连接数',
          key: 'connections',
          width: '10%',
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
          title: '入队数',
          key: 'enQuence.count',
          width: '15%',
          render: (h, params) => {
            let html = []
            let spin = h('d-spin', {
              attrs: {
                size: 'small'
              },
              style: {
                display: params.item.enQuence !== undefined ? 'none' : 'inline-block'
              }
            })
            html.push(spin)
            let enQuence = params.item.enQuence
            if (enQuence === undefined) {
              return h('div', {}, html)
            } else if (enQuence === 'unknown') {
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
                  display: params.item.enQuence.count === undefined ? 'none' : 'inline-block'
                }
              }, formatNumFilter(enQuence.count))
              html.push(textSpan)
              return h('div', {}, html)
            }
          }
        },
        {
          title: '归档',
          key: 'config.archive',
          width: '8%',
          render: (h, params) => {
            return openOrCloseBtnRender(h, params.item.config === undefined ? undefined : params.item.config.archive)
          }
        },
        {
          title: '就近发送',
          key: 'config.nearBy',
          width: '8%',
          render: (h, params) => {
            return openOrCloseBtnRender(h, params.item.config === undefined ? undefined : params.item.config.nearBy)
          }
        },
        {
          title: '客户端类型',
          key: 'clientType',
          width: '8%',
          render: (h, params) => {
            if (this.$store.getters.isAdmin) {
              return clientTypeSelectRender2(h, params, 'producer')
            } else {
              return clientTypeBtnRender(h, params.item.clientType)
            }
          }
        },
        {
          title: '生产权重',
          key: 'config.weight',
          width: '8%'
        },
        {
          title: '限制IP发送',
          key: 'config.blackList',
          width: '8%',
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
        }
      ],
      // 订阅框
      subscribeDialog: {
        colData: [
          {
            title: '主题代码',
            key: 'code',
            width: '30%'
          },
          {
            title: '命名空间',
            key: 'namespace.code',
            width: '30%'
          },
          {
            title: '客户端类型',
            key: 'clientType',
            width: '30%',
            render: (h, params) => {
              return clientTypeSelectRender(h, params, this.$refs.producerBase.$refs.subscribe)
            }
          }
        ],
        urls: {
          add: `/producer/add`,
          search: `/topic/unsubscribed/search`
        }
      }
    }
  },
  methods: {
    getList () {
      this.$refs.producerBase.getList()
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
