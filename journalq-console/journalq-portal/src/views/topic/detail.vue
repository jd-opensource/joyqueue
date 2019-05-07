<template>
  <detail-slot ref="detail">
    <template slot="tabs">
      <d-tab-pane label="生产者" name="producer" icon="user-plus">
        <producer ref="producer" :search="search"/>
      </d-tab-pane>
      <d-tab-pane label="消费者" name="consumer" icon="user-minus">
        <consumer ref="consumer" :search="search"/>
      </d-tab-pane>
      <d-tab-pane label="分组信息" name="partitionGroup" icon="folder" v-if="$store.getters.isAdmin">
        <partitionGroup ref="partitionGroup" @on-partition-group-change="queryTopicDetail"
                        :showBrokerChart="false" :showHostChart="false"/>
      </d-tab-pane>
      <d-tab-pane label="Broker" v-if="$store.getters.isAdmin" name="broker" icon="cpu">
        <broker ref="broker" :topicId="this.$route.query.id"/>
      </d-tab-pane>
      <d-tab-pane label="重试" name="retry" icon="zap">
        <retry ref="retry" :search="retrySearch"/>
      </d-tab-pane>
      <d-tab-pane label="归档" name="archive" icon="package">
        <archive ref="archive" :search="archiveSearch"/>
      </d-tab-pane>
    </template>
  </detail-slot>
</template>

<script>
import detailSlot from './slot/detailSlot.vue'
import Producer from './producer.vue'
import Consumer from './consumer.vue'
import PartitionGroup from './partitionGroup.vue'
import retry from '../tool/retry.vue'
import broker from './broker.vue'
import archive from '../tool/archive.vue'

export default {
  name: 'applicationDetail',
  components: {
    detailSlot,
    Producer,
    Consumer,
    PartitionGroup,
    retry,
    broker,
    archive
  },
  computed: {
    search () {
      return {
        topic: {
          id: this.$route.query.id,
          code: this.$route.query.code,
          namespace: {
            id: this.$route.query.namespaceId,
            code: this.$route.query.namespaceCode
          }
        }
      }
    },
    retrySearch () {
      return {
        topic: this.$route.query.id,
        app: this.$route.query.app,
        status: 1,
        beginTime: '',
        endTime: ''
      }
    },
    archiveSearch () {
      return {
        topic: this.$route.query.id,
        businessId: '',
        beginTime: '',
        endTime: '',
        sendTime: '',
        messageId: '',
        count: 10
      }
    }
  },
  methods: {
    queryTopicDetail () {
      this.$refs.detail.getDetail(this.$route.query.id)
    }
  },
  mounted () {
    // init topic
    // this.topic.id = this.$route.query.id
    // this.topic.code = this.$route.query.code
    // this.topic.namespace.id = this.$route.query.namespaceId
    // this.topic.namespace.code = this.$route.query.namespaceCode
    // add archive refs into detail tab refs
    this.$refs.detail.$refs['producer'] = this.$refs.producer
    this.$refs.detail.$refs['consumer'] = this.$refs.consumer
    this.$refs.detail.$refs['partitionGroup'] = this.$refs.partitionGroup
    this.$refs.detail.$refs['broker'] = this.$refs.broker
    this.$refs.detail.$refs['retry'] = this.$refs.retry
    this.$refs.detail.$refs['archive'] = this.$refs.archive
  }
}
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>

</style>
