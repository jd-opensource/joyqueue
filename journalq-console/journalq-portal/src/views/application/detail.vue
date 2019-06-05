<template>
  <detail-slot ref="detail">
    <template slot="tabs">
      <d-tab-pane label="生产者" name="producer" icon="user-plus">
        <producer ref="producer" :search="search" @on-detail="openProducerDetailTab"/>
      </d-tab-pane>
      <d-tab-pane label="消费者" name="consumer" icon="user-minus">
        <consumer ref="consumer" :search="search" @on-detail="openConsumerDetailTab"/>
      </d-tab-pane>
      <d-tab-pane label="用户" name="myAppUsers" icon="users">
        <my-app-users ref="myAppUsers"/>
      </d-tab-pane>
      <d-tab-pane label="令牌" name="myAppToken" icon="feather">
        <my-app-token ref="myAppToken"/>
      </d-tab-pane>
      <d-tab-pane :label="producerDetailName" name="producerDetail" closable icon="paperclip" :visible="producerDetailVisible">
        <producer-detail ref="producerDetail" :detailType="$store.getters.appDetailType"/>
      </d-tab-pane>
      <d-tab-pane :label="consumerDetailName" name="consumerDetail" closable icon="paperclip" :visible="consumerDetailVisible">
        <consumer-detail ref="consumerDetail" :detailType="$store.getters.appDetailType"/>
      </d-tab-pane>
    </template>
  </detail-slot>
</template>

<script>
import detailSlot from './slot/detailSlot.vue'
import Producer from './producer.vue'
import Consumer from './consumer.vue'
import MyAppUsers from './myAppUsers.vue'
import MyAppToken from './myAppToken.vue'
import ProducerDetail from '../monitor/detail/producerDetail.vue'
import ConsumerDetail from '../monitor/detail/consumerDetail.vue'
import {generateProducerDetailTabName, generateConsumerDetailTabName} from '../../utils/common.js'

export default {
  name: 'applicationDetail',
  components: {
    detailSlot,
    Producer,
    Consumer,
    MyAppUsers,
    MyAppToken,
    ProducerDetail,
    ConsumerDetail
  },
  // data () {
  //   return {
  //     appDetailType: this.$store.getters.appDetailType
  //   }
  // },
  methods: {
    openProducerDetailTab (item) {
      // Jump to producer detail router
      this.$router.push({
        name: `/${this.$i18n.locale}/application/detail`,
        query: {
          id: this.$route.query.id,
          code: this.$route.query.code,
          tab: 'producerDetail',
          app: item.app.code,
          topic: item.topic.code,
          namespace: item.namespace.code,
          clientType: item.clientType,
          subTab: 'partition',
          producerDetailVisible: '1',
          consumerDetailVisible: this.consumerDetailVisible ? '1' : '0'
        }
      })
    },
    openConsumerDetailTab (item) {
      // Jump to consumer detail router
      this.$router.push({
        name: `/${this.$i18n.locale}/application/detail`,
        query: {
          id: this.$route.query.id,
          code: this.$route.query.code,
          tab: 'consumerDetail',
          app: item.app.code,
          topic: item.topic.code,
          namespace: item.namespace.code,
          subscribeGroup: item.subscribeGroup,
          clientType: item.clientType,
          subTab: 'partition',
          producerDetailVisible: this.producerDetailVisible ? '1' : '0',
          consumerDetailVisible: '1'
        }
      })
    }
  },
  computed: {
    search () {
      return {
        app: {
          id: this.$route.query.id,
          code: this.$route.query.code
        }
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
    this.$refs.detail.$refs['myAppUsers'] = this.$refs.myAppUsers
    this.$refs.detail.$refs['myAppToken'] = this.$refs.myAppToken
    this.$refs.detail.$refs['producerDetail'] = this.$refs.producerDetail
    this.$refs.detail.$refs['consumerDetail'] = this.$refs.consumerDetail
  }
}

</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>

</style>
