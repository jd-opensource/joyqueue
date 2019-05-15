<template>
  <detail-slot ref="detail">
    <template slot="tabs">
      <d-tab-pane label="生产者" name="producer" icon="user-plus">
        <producer ref="producer" :search="search" @on-detail="openProducerDetailTab"/>
      </d-tab-pane>
      <d-tab-pane label="消费者" name="consumer" icon="user-minus">
        <consumer ref="consumer" :search="search"/>
      </d-tab-pane>
      <d-tab-pane label="用户" name="myAppUsers" icon="users">
        <my-app-users ref="myAppUsers"/>
      </d-tab-pane>
      <d-tab-pane label="令牌" name="myAppToken" icon="feather">
        <my-app-token ref="myAppToken"/>
      </d-tab-pane>
      <d-tab-pane :label="producerDetail.name" name="producerDetail" closable icon="paperclip">
        <producer-detail ref="producerDetail"/>
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

export default {
  name: 'applicationDetail',
  components: {
    detailSlot,
    Producer,
    Consumer,
    MyAppUsers,
    MyAppToken,
    ProducerDetail
  },
  data () {
    return {
      appDetailType: this.$store.getters.appDetailType,
      producerDetail: {
        name: '生产详情',
        app: {
          id: 0,
          code: ''
        },
        topic: {
          id: '',
          code: ''
        },
        namespace: {
          id: '',
          code: ''
        }
      }
    }
  },
  methods: {
    openProducerDetailTab (item) {
      this.producerDetail.name = '生产详情 - ' + item.topic.code
      //Jump to producer detail router
      this.$router.push({
        name: `/${this.$i18n.locale}/application/detail`,
        query: {
          id: this.$route.query.id,
          code: this.$route.query.code,
          tab: 'producerDetail',
          app: item.app.code,
          topic: item.topic.code,
          namespace: item.namespace.code,
          subscribeGroup: '',
          subTab: 'partition'
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
    }
  },
  mounted () {
    // add refs into detail tab refs
    this.$refs.detail.$refs['producer'] = this.$refs.producer
    this.$refs.detail.$refs['consumer'] = this.$refs.consumer
    this.$refs.detail.$refs['myAppUsers'] = this.$refs.myAppUsers
    this.$refs.detail.$refs['myAppToken'] = this.$refs.myAppToken
    this.$refs.detail.$refs['producerDetail'] = this.$refs.producerDetail
  }
}

</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>

</style>
