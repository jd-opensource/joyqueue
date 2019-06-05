<template>
  <div>
    <d-tabs @on-change="handleTabChange" :value="subTab" size="small">
      <d-tab-pane label="分组" name="partition" icon="pocket">
        <partition ref="partition" :colData="partitionColData" :doSearch="doSearch" :search="search"/>
      </d-tab-pane>
      <d-tab-pane label="客户端连接" name="clientConnection" icon="github">
        <client-connection ref="clientConnection" :search="search" :doSearch="doSearch"/>
      </d-tab-pane>
      <d-tab-pane label="Broker" name="broker" icon="file-text">
        <broker ref="broker" :search="search" :doSearch="doSearch"/>
      </d-tab-pane>
    </d-tabs>
  </div>
</template>

<script>
import partition from './partition.vue'
import clientConnection from './clientConnection.vue'
import broker from './broker.vue'
import partitionExpand from './partitionExpand'

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
                      title: 'ID',
                      key: 'partitionGroup'
                    },
                    // {
                    //   title: '主分片',
                    //   key: 'ip'
                    // },
                    {
                      title: '分区',
                      key: 'partition'
                    },
                    {
                      title: '入队数',
                      key: 'enQuence.count'
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
            key: 'partitions'
          },
          {
            title: '入队数',
            key: 'enQuence.count'
          }
        ]
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
      subTab: 'partition'
    }
  },
  watch: {
    '$route' (to, from) {
      // this.app.code = to.query.app
      // this.topic.code = to.query.topic
      // this.namespace.code = to.query.namespace
      // this.subscribeGroup =
      this.subTab = to.query.subTab || this.subTab
      // this.tab = to.query.tab || this.tab
      // this.$refs[this.tab].getList()
    }
  },
  methods: {
    handleTabChange (data) {
      if (data.index === 0 && data.name !== 'partition') {
        return
      }

      let name = data.name
      if (this.detailType === this.$store.getters.appDetailType) { // my application
        this.$router.push({
          name: `/${this.$i18n.locale}/application/detail`,
          query: {
            id: this.$route.query.id,
            code: this.$route.query.code,
            app: this.$route.query.code,
            topic: this.$route.query.topic || '',
            namespace: this.$route.query.namespace || '',
            clientType: this.$route.query.clientType,
            subTab: name,
            tab: this.$route.query.tab,
            producerDetailVisible: this.$route.query.producerDetailVisible || '0',
            consumerDetailVisible: this.$route.query.consumerDetailVisible || '0'
          }
        })
      } else { // topic center
        this.$router.push({
          name: `/${this.$i18n.locale}/topic/detail`,
          query: {
            id: this.$route.query.id,
            code: this.$route.query.code,
            app: this.$route.query.code,
            topic: this.$route.query.topic || '',
            namespace: this.$route.query.namespace || '',
            namespaceId: this.$route.query.namespaceId || '',
            namespaceCode: this.$route.query.namespaceCode || '',
            clientType: this.$route.query.clientType,
            subTab: name,
            tab: this.$route.query.tab,
            producerDetailVisible: this.$route.query.producerDetailVisible || '0',
            consumerDetailVisible: this.$route.query.consumerDetailVisible || '0'
          }
        })
      }
      this.$refs[name].getList()
    },
    getList () {
      if (this.$route.query.app && this.$route.query.topic) {
        this.$refs[this.subTab].getList()
      }
    }
  },
  computed: {
    search () {
      return {
        app: {
          code: this.$route.query.app
        },
        topic: {
          code: this.$route.query.topic
        },
        namespace: {
          code: this.$route.query.namespace
        },
        subscribeGroup: this.$route.query.subscribeGroup,
        type: this.$store.getters.producerType
      }
    }
  }
}
</script>

<style scoped>

</style>
