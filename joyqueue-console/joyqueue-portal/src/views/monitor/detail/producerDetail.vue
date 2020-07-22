<template>
  <div style="margin-left:-20px;">
    <d-tabs type="card" @on-change="handleTabChange" :value="subTab" size="small">
      <d-tab-pane label="分区组" name="partition" icon="pocket" :closable="false">
        <partition ref="partition" :search="search" :colData="partitionColData" :doSearch="doSearch"/>
      </d-tab-pane>
      <d-tab-pane label="客户端连接" name="clientConnection" icon="github" :closable="false">
        <client-connection :search="search" ref="clientConnection" :doSearch="doSearch"/>
      </d-tab-pane>
      <d-tab-pane label="Broker" name="broker" icon="file-text" :closable="false">
        <broker ref="broker" :search="search" :doSearch="doSearch"/>
      </d-tab-pane>
      <div slot="extra">
        <d-button type="primary" size="small" @click="getList" class="mr40">刷新</d-button>
      </div>
    </d-tabs>
  </div>
</template>

<script>
import partition from './partition.vue'
import clientConnection from './clientConnection.vue'
import broker from './broker.vue'
import partitionExpand from './partitionExpand'
import {bytesToSize, mergePartitionGroup} from '../../../utils/common'

export default {
  name: 'producer-detail',
  components: {
    broker,
    partition,
    clientConnection,
    partitionExpand
  },
  props: {
    detailType: {
      type: Number
    },
    partitionColData: { // 分片 列表表头
      type: Array,
      default: function () {
        return [
          {
            type: 'expand',
            width: 50,
            render: (h, params) => {
              return h(partitionExpand, {
                props: {
                  row: params.row,
                  colData: [
                    {
                      title: '分区',
                      key: 'partition'
                    },
                    {
                      title: '入队数',
                      key: 'enQuence.count'
                    },
                    {
                      title: 'TPS',
                      key: 'enQuence.tps'
                    },
                    {
                      title: '流量',
                      key: 'enQuence.traffic',
                      formatter (item) {
                        return bytesToSize(item.enQuence.traffic)
                      }
                    }
                  ],
                  subscribe: params.row.subscribe,
                  partitionGroup: params.row.groupNo
                }
              })
            }
          },
          {
            title: 'ID',
            key: 'groupNo'
          },
          {
            title: '主分片',
            key: 'ip'
          },
          {
            title: '分区',
            key: 'partitions',
            formatter (item) {
              return mergePartitionGroup(JSON.parse(item.partitions))
            }
          },
          {
            title: '入队数',
            key: 'enQuence.count'
          }
        ]
      }
    },
    search: {
      type: Object,
      default: function () {
        return {
          app: {
            code: ''
          },
          topic: {
            code: ''
          },
          namespace: {
            code: ''
          },
          subscribeGroup: '',
          type: this.$store.getters.producerType,
          clientType: -1
        }
      }
    }
  },
  data () {
    return {
      // type: this.$store.getters.producerType
      doSearch: true,
      // app: {
      //   code: this.$route.query.app
      // },
      // topic: {
      //   code: this.$route.query.topic
      // },
      // namespace: {
      //   code: this.$route.query.namespace
      // },
      // subscribeGroup: '',
      // topicCode: '',
      // namespaceCode: '',
      // appName: '',
      subTab: 'partition'

    }
  },
  watch: {
    '$route' (to, from) {
      if (to.query.tab === 'producerDetail') {
        this.subTab = to.query.subTab || this.subTab
      }
    }
  },
  methods: {
    handleTabChange (data) {
      // if (data.index === 0 && data.name !== 'partition') {
      //   return
      // }
      if (this.$route.query.producerDetailVisible === '1') {
        let name = data.name
        this.$refs[name].search.app.code = this.$route.query.app || ''
        this.$refs[name].search.topic.code = this.$route.query.topic || ''
        this.$refs[name].search.namespace.code = this.$route.query.namespace || ''
        this.$refs[name].search.clientType = this.$route.query.clientType

        let routeName = ''
        if (this.detailType === this.$store.getters.appDetailType) {
          routeName = `/${this.$i18n.locale}/application/detail`
        } else {
          routeName = `/${this.$i18n.locale}/topic/detail`
        }

        this.$router.push({
          name: routeName,
          query: {
            id: this.$route.query.id,
            app: this.$route.query.app || '',
            topic: this.$route.query.topic || '',
            namespace: this.$route.query.namespace || '',
            clientType: this.$route.query.clientType,
            subTab: name,
            tab: this.$route.query.tab,
            producerDetailVisible: this.$route.query.producerDetailVisible || '0',
            consumerDetailVisible: this.$route.query.consumerDetailVisible || '0'
          }
        })
        this.$refs[name].getList()
      }
    },
    getList () {
      this.$refs[this.subTab].getList()
    }
  },
  mounted () {
    // this.getList()
  }
}
</script>

<style scoped>

</style>
