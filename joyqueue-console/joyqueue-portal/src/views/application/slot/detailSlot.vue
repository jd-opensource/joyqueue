<template>
  <div style="margin:20px;" class="clearfix">
    <d-breadcrumb class="mb20" separator=">">
      <d-breadcrumb-item :to="{ name: `/${$i18n.locale}/application` }">我的应用</d-breadcrumb-item>
      <d-breadcrumb-item>{{detail.code}}</d-breadcrumb-item>
    </d-breadcrumb>
    <slot :detail="detail"></slot>
      <!--tabs由于没有单独的路由名称，所以在调用table时，需要在data中自定义urls-->
    <d-tabs @on-change="handleTabChange" :value="tab">
      <slot name="tabs"></slot>
    </d-tabs>
  </div>
</template>

<script>
import apiUrl from '../../../utils/apiUrl.js'
import apiRequest from '../../../utils/apiRequest.js'
import crud from '../../../mixins/crud.js'

export default {
  name: 'applicationDetailSlot',
  mixins: [ crud ],
  data () {
    return {
      app: {
        id: 0,
        code: ''
      },
      tab: '',
      detail: {
        id: 0,
        code: '',
        name: '',
        aliasCode: '',
        department: '',
        source: '',
        system: '',
        owner: {},
        sign: ''
      }
    }
  },
  watch: {
    '$route' (to, from) {
      this.tab = to.query.tab || 'producer'
    }
  },
  methods: {
    getDetail () {
      if (this.app.id) {
        apiRequest.get(this.urlOrigin.detail + '/' + this.app.id, {}).then((data) => {
          this.detail = data.data || {}
        })
      } else if (this.app.code) {
        apiRequest.get(apiUrl['/application'].getByCode + '/' + this.app.code, {}).then((data) => {
          this.detail = data.data || {}
        })
      }
    },
    gotoList () {
      this.$router.push({name: `/${this.$i18n.locale}/application`})
    },
    handleTabChange (data) {
      let name = data.name
      if (name === 'producer' || name === 'consumer') {
        this.$refs[name].search.app.code = this.app.code

        this.$router.push({
          name: `/${this.$i18n.locale}/application/detail`,
          query: {
            id: this.app.id,
            app: this.app.code,
            tab: name,
            producerDetailVisible: '0',
            consumerDetailVisible: '0'
          }
        })

        this.$refs[name].getList()
      } else if (name === 'producerDetail') {
        let subTab = this.$route.query.subTab || 'partition'

        this.$refs[name].search.app.code = this.app.code
        this.$refs[name].search.topic.code = this.$route.query.topic || ''
        this.$refs[name].search.namespace.code = this.$route.query.namespace || ''
        this.$refs[name].search.clientType = this.$route.query.clientType
        this.$refs[name].subTab = subTab

        this.$router.push({
          name: `/${this.$i18n.locale}/application/detail`,
          query: {
            id: this.app.id,
            app: this.app.code,
            topic: this.$route.query.topic || '',
            namespace: this.$route.query.namespace || '',
            clientType: this.$route.query.clientType,
            subTab: this.$route.query.subTab || 'partition',
            tab: name,
            producerDetailVisible: '1',
            consumerDetailVisible: this.$route.query.consumerDetailVisible || '0'
          }
        })

        this.$refs[name].$refs[subTab].getList()
      } else if (name === 'consumerDetail') {
        let subTab = this.$route.query.subTab || 'partition'

        this.$refs[name].search.app.code = this.app.code
        this.$refs[name].search.topic.code = this.$route.query.topic || ''
        this.$refs[name].search.namespace.code = this.$route.query.namespace || ''
        this.$refs[name].search.subscribeGroup = this.$route.query.subscribeGroup || ''
        this.$refs[name].search.clientType = this.$route.query.clientType
        this.$refs[name].subTab = subTab

        this.$router.push({
          name: `/${this.$i18n.locale}/application/detail`,
          query: {
            id: this.app.id,
            app: this.app.code,
            topic: this.$route.query.topic || '',
            namespace: this.$route.query.namespace || '',
            subscribeGroup: this.$route.query.subscribeGroup || '',
            clientType: this.$route.query.clientType,
            subTab: this.$route.query.subTab || 'partition',
            tab: name,
            producerDetailVisible: this.$route.query.producerDetailVisible || '0',
            consumerDetailVisible: '1'
          }
        })

        this.$refs[name].$refs[subTab].getList()
      } else {
        this.$router.push({
          name: `/${this.$i18n.locale}/application/detail`,
          query: {
            id: this.app.id,
            app: this.app.code,
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
    this.app.id = this.$route.query.id
    this.app.code = this.$route.query.app
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
