<template>
  <div style="margin:20px;" class="clearfix">
    <d-breadcrumb class="mb20" separator=">">
      <d-breadcrumb-item :to="{ name: `/${$i18n.locale}/topic` }">主题中心</d-breadcrumb-item>
      <d-breadcrumb-item>{{topic.id}}</d-breadcrumb-item>
    </d-breadcrumb>
    <slot :detail="detail"></slot>
    <d-tabs @on-change="handleTabChange" :value="tab">
      <slot name="tabs"></slot>
    </d-tabs>
  </div>
</template>

<script>
import apiRequest from '../../../utils/apiRequest.js'
import crud from '../../../mixins/crud.js'

export default {
  name: 'topicDetailSlot',
  mixins: [ crud ],
  data () {
    return {
      urls: {
        getById: '/topic/getById'
      },
      topic: {
        id: '',
        code: ''
      },
      namespace: {
        code: ''
      },
      tab: '',
      detail: {
        id: '',
        code: '',
        name: '',
        type: '',
        partitions: '',
        archive: false,
        labels: '',
        dataCenters: [],
        description: ''
      }
    }
  },
  watch: {
    '$route' (to, from) {
      this.tab = to.query.tab || 'producer'
    }
  },
  methods: {
    // 组件自身的方法写这里
    getDetail () {
      let data = {
        id: this.topic.id
      }
      apiRequest.post(this.urls.getById, {}, data).then((data) => {
        this.detail = data.data || {}
      })
    },
    gotoList () {
      this.$router.push({name: `/${this.$i18n.locale}/topic`})
    },
    handleTabChange (data) {
      let name = data.name
      if (name === 'producer' || name === 'consumer') {
        this.$refs[name].search.topic.code = this.topic.code
        this.$refs[name].search.topic.namespace.code = this.namespace.code

        this.$router.push({
          name: `/${this.$i18n.locale}/topic/detail`,
          query: {
            id: this.topic.id,
            topic: this.topic.code,
            namespace: this.namespace.code,
            tab: name,
            producerDetailVisible: '0',
            consumerDetailVisible: '0'
          }
        })

        this.$refs[name].getList()
      } else if (name === 'producerDetail') {
        let subTab = this.$route.query.subTab || 'partition'

        this.$refs[name].search.app.code = this.$route.query.app
        this.$refs[name].search.topic.code = this.topic.code
        this.$refs[name].search.namespace.code = this.namespace.code
        this.$refs[name].search.clientType = this.$route.query.clientType
        this.$refs[name].subTab = subTab

        this.$router.push({
          name: `/${this.$i18n.locale}/topic/detail`,
          query: {
            id: this.topic.id,
            app: this.$route.query.app,
            topic: this.topic.code,
            namespace: this.namespace.code,
            clientType: this.$route.query.clientType,
            tab: name,
            subTab: this.$route.query.subTab || 'partition',
            producerDetailVisible: '1',
            consumerDetailVisible: this.$route.query.consumerDetailVisible || '0'
          }
        })

        this.$refs[name].$refs[subTab].getList()
      } else if (name === 'consumerDetail') {
        let subTab = this.$route.query.subTab || 'partition'

        this.$refs[name].search.app.code = this.$route.query.app || ''
        this.$refs[name].search.topic.code = this.topic.code
        this.$refs[name].search.namespace.code = this.namespace.code
        this.$refs[name].search.subscribeGroup = this.$route.query.subscribeGroup || ''
        this.$refs[name].search.clientType = this.$route.query.clientType
        this.$refs[name].subTab = subTab

        this.$router.push({
          name: `/${this.$i18n.locale}/topic/detail`,
          query: {
            id: this.topic.id,
            app: this.$route.query.app || '',
            topic: this.topic.code,
            namespace: this.namespace.code,
            subscribeGroup: this.$route.query.subscribeGroup || '',
            clientType: this.$route.query.clientType,
            subTab: this.$route.query.subTab || 'partition',
            tab: name,
            producerDetailVisible: this.$route.query.producerDetailVisible || '0',
            consumerDetailVisible: '1'
          }
        })

        this.$refs[name].$refs[subTab].getList()
      } else if (name === 'retry') {
        this.$refs[name].search.app = this.$route.query.app || ''
        this.$refs[name].search.topic = this.topic.id

        this.$router.push({
          name: `/${this.$i18n.locale}/topic/detail`,
          query: {
            id: this.topic.id,
            app: this.$route.query.app || '',
            topic: this.topic.code,
            namespace: this.namespace.code,
            subscribeGroup: this.$route.query.subscribeGroup,
            tab: name,
            producerDetailVisible: '0',
            consumerDetailVisible: '0'
          }
        })

        this.$refs[name].getList()
      } else {
        this.$router.push({
          name: `/${this.$i18n.locale}/topic/detail`,
          query: {
            id: this.topic.id,
            topic: this.topic.code,
            namespace: this.namespace.code,
            tab: name,
            producerDetailVisible: '0',
            consumerDetailVisible: '0'
          }
        })

        this.$refs[name].getList()
      }
    }
  },
  mounted () {
    this.topic.id = this.$route.query.id
    this.topic.code = this.$route.query.topic
    this.namespace.code = this.$route.query.namespace
    this.tab = this.$route.query.tab || 'producer'
    this.getDetail()
  }

}
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>
  .layout-header, .layout-footer {
    text-align: center;
    line-height: 60px;
  }

  .layout-sider {
    text-align: center;
  }

  .detail {
    border: 1px solid #cdcdcd;
    box-sizing: border-box;
  }
  .detail .title {
    height: 40px;
    color: #000;
    line-height: 40px;
    text-align: center;
    font-weight: 700;
  }
  .detail .row {
    box-sizing: border-box;
    width: 100%;
    padding: 10px;
  }
</style>
