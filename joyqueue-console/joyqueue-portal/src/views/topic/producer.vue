<template>
  <div>
    <producer-base ref="producerBase" :keywordTip="keywordTip" :keywordName="keywordName" :colData="colData"
                   :subscribeDialogColData="subscribeDialog.colData" @on-enter="getList" :btns="btns" :operates="operates"
                    :search="search" :subscribeUrls="subscribeDialog.urls" @on-detail="handleDetail"/>
  </div>
</template>

<script>
import Vue from 'vue'
import producerBase from '../monitor/producerBase.vue'
import {getAppCode, yesOrNoBtnRender, openOrCloseBtnRender, clientTypeSelectRender, clientTypeSelectRender2,
  clientTypeBtnRender} from '../../utils/common.js'

export default {
  name: 'producer',
  components: {
    producerBase
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
      width: 80,
      colData: [
        {
          title: '应用',
          key: 'app.code',
          width: '10%',
          formatter (row) {
            return getAppCode(row.app, row.subscribeGroup)
          },
          render: (h, params) => {
            const app = params.item.app
            if (params.item.canOperate) {
              return h('label', {
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
                        tab: 'producer'
                      }
                    })
                  },
                  mousemove: (event) => {
                    event.target.style.cursor = 'pointer'
                  }
                }
              }, app.code)
            } else {
              return h('label', app.code)
            }
          }
        },
        {
          title: '主题',
          key: 'topic.code',
          width: '14%'
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
          title: '入队数',
          key: 'enQuence.count',
          width: '14%',
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
          title: '单线程发送',
          key: 'config.single',
          width: '6%',
          render: (h, params) => {
            return yesOrNoBtnRender(h, params.item.config === undefined ? undefined : params.item.config.single)
          }
        },
        {
          title: '归档',
          key: 'config.archive',
          width: '6%',
          render: (h, params) => {
            return openOrCloseBtnRender(h, params.item.config === undefined ? undefined : params.item.config.archive)
          }
        },
        {
          title: '客户端类型',
          key: 'clientType',
          width: '6%',
          render: (h, params) => {
            if (this.$store.getters.isAdmin) {
              return clientTypeSelectRender2(h, params, 'producer')
            } else {
              return clientTypeBtnRender(h, params.item.clientType)
            }
          }
        },
        {
          title: '就近发送',
          key: 'config.nearBy',
          width: '6%',
          render: (h, params) => {
            return openOrCloseBtnRender(h, params.item.config === undefined ? undefined : params.item.config.nearBy)
          }
        },
        {
          title: '生产权重',
          key: 'config.weight',
          width: '6%'
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
      btns: [
        {
          txt: '生产详情',
          method: 'on-detail',
          bindKey: 'canOperate',
          bindVal: true
        },
        {
          txt: '配置',
          method: 'on-config',
          bindKey: 'canOperate',
          bindVal: true
        },
        {
          txt: '取消订阅',
          method: 'on-cancel-subscribe',
          bindKey: 'canOperate',
          bindVal: true,
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
          bindKey: 'canOperate',
          bindVal: true
        },
        {
          txt: '发送消息',
          method: 'on-send-message',
          bindKey: 'canOperate',
          bindVal: true
        },
        {
          txt: '限流',
          method: 'on-rateLimit',
          bindKey: 'canOperate',
          bindVal: true
        }
      ],
      // 订阅框
      subscribeDialog: {
        colData: [
          {
            title: 'ID',
            key: 'id'
          },
          {
            title: '应用代码',
            key: 'code'
          },
          {
            title: '客户端类型',
            key: 'clientType',
            render: (h, params) => {
              return clientTypeSelectRender(h, params, this.$refs.producerBase.$refs.subscribe)
            }
          }
        ],
        urls: {
          add: `/producer/add`,
          search: `/application/unsubscribed/search`
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
  }
}
</script>

<style scoped>

</style>
