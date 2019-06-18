<template>
  <detail-slot ref="detail">
    <template slot="tabs">
      <d-tab-pane label="生产者" name="producer" icon="user-plus">
        <producer ref="producer" :search="search" @on-detail="openProducerDetailTab"/>
      </d-tab-pane>
      <d-tab-pane label="消费者" name="consumer" icon="user-minus">
        <consumer ref="consumer" :search="search" @on-detail="openConsumerDetailTab"/>
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
      <d-tab-pane :label="producerDetailName" name="producerDetail" closable icon="paperclip" :visible="producerDetailVisible">
        <producer-detail ref="producerDetail" :detailType="$store.getters.topicDetailType"/>
      </d-tab-pane>
      <d-tab-pane :label="consumerDetailName" name="consumerDetail" closable icon="paperclip" :visible="consumerDetailVisible">
        <consumer-detail ref="consumerDetail" :detailType="$store.getters.topicDetailType"/>
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
import ProducerDetail from '../monitor/detail/producerDetail.vue'
import ConsumerDetail from '../monitor/detail/consumerDetail.vue'
import {generateProducerDetailTabName, generateConsumerDetailTabName} from '../../utils/common.js'

export default {
  name: 'applicationDetail',
  components: {
    detailSlot,
    Producer,
    Consumer,
    PartitionGroup,
    retry,
    broker,
    archive,
    ProducerDetail,
    ConsumerDetail
  },
  // data () {
  //   return {
  //     appCode: ''
  //   }
  // // },
  // watch: {
  //   '$route' (to, from) {
  //     console.log(44)
  //     this.appCode = to.query.app
  //     console.log(this.appCode)
  //     // this.$refs[this.tab].getList()
  //   }
  // },
  methods: {
    openProducerDetailTab (item) {
      // Jump to producer detail router
      this.$router.push({
        name: `/${this.$i18n.locale}/topic/detail`,
        query: {
          id: item.topic.id,
          app: item.app.code,
          topic: item.topic.code,
          namespace: item.namespace.code,
          clientType: item.clientType,
          subTab: 'partition',
          tab: 'producerDetail',
          producerDetailVisible: '1',
          consumerDetailVisible: this.consumerDetailVisible ? '1' : '0'
        }
      })
    },
    openConsumerDetailTab (item) {
      // Jump to consumer detail router
      this.$router.push({
        name: `/${this.$i18n.locale}/topic/detail`,
        query: {
          id: this.topic.id,
          app: item.app.code,
          topic: item.topic.code,
          namespace: item.namespace.code,
          subscribeGroup: item.subscribeGroup,
          clientType: item.clientType,
          subTab: 'partition',
          tab: 'consumerDetail',
          producerDetailVisible: this.producerDetailVisible ? '1' : '0',
          consumerDetailVisible: '1'
        }
      })
    },
    queryTopicDetail () {
      this.$refs.detail.getDetail(this.$route.query.id)
    }
  },
  computed: {
    search () {
      return {
        topic: {
          id: this.$route.query.id,
          code: this.$route.query.topic,
          namespace: {
            code: this.$route.query.namespace
          }
        }
      }
    },
    retrySearch () {
      console.log(77)
      return {
        topic: this.$route.query.id,
        app: this.$route.query.app,
        status: 1
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
    },
    producerDetailName () {
      if (!this.$route.query.app || !this.$route.query.topic) {
        return '生产详情'
      }
      return generateProducerDetailTabName(this.$route.query.app, this.$route.query.topic, this.$route.query.namespace || '')
    },
    consumerDetailName () {
      if (!this.$route.query.app || !this.$route.query.topic) {
        return '消费详情'
      }
      return generateConsumerDetailTabName(this.$route.query.app, this.$route.query.subscribeGroup || '',
        this.$route.query.topic, this.$route.query.namespace || '')
    },
    producerDetailVisible () {
      if (this.$route.query.producerDetailVisible && this.$route.query.producerDetailVisible === '1') {
        return true
      } else {
        return false
      }
    },
    consumerDetailVisible () {
      if (this.$route.query.consumerDetailVisible && this.$route.query.consumerDetailVisible === '1') {
        return true
      } else {
        return false
      }
    }
  },
  mounted () {
    // add refs into detail tab refs
    this.$refs.detail.$refs['producer'] = this.$refs.producer
    this.$refs.detail.$refs['consumer'] = this.$refs.consumer
    this.$refs.detail.$refs['partitionGroup'] = this.$refs.partitionGroup
    this.$refs.detail.$refs['broker'] = this.$refs.broker
    this.$refs.detail.$refs['retry'] = this.$refs.retry
    this.$refs.detail.$refs['archive'] = this.$refs.archive
    this.$refs.detail.$refs['producerDetail'] = this.$refs.producerDetail
    this.$refs.detail.$refs['consumerDetail'] = this.$refs.consumerDetail
  }
}
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>

</style>
