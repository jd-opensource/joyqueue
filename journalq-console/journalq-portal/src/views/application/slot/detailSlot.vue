<template>
  <div style="margin:20px;" class="clearfix">
    <d-breadcrumb class="mb20" separator=">">
      <d-breadcrumb-item :to="{ name: `/${$i18n.locale}/application` }">我的应用</d-breadcrumb-item>
      <d-breadcrumb-item>{{detail.code}}</d-breadcrumb-item>
    </d-breadcrumb>
    <div class="detail mb20">
      <div class="title">{{app.code}}</div>
      <grid-row :gutter="16">
        <grid-col span="8">
          <span>中文名称:</span>
          <span>{{detail.name}}</span>
        </grid-col>
        <grid-col span="8">
          <span>负责人:</span>
          <span>{{detail.owner != null ? detail.owner.code : '-'}}</span>
        </grid-col>
        <grid-col span="8">
          <span>签名:</span>
          <span>{{detail.sign}}</span>
        </grid-col>
      </grid-row>
    </div>
      <!--tabs由于没有单独的路由名称，所以在调用table时，需要在data中自定义urls-->
      <slot name="tabs"></slot>
  </div>
</template>

<script>
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
      },
      visible: {
      }
    }
  },
  watch: {
    '$route' (to, from) {
      this.app.id = to.query.id
      this.app.code = to.query.code
      this.tab = to.query.tab || this.tab
      // this.$refs[this.tab].getList()
    }
  },
  methods: {
    getDetail () {
      apiRequest.get(this.urlOrigin.detail + '/' + this.app.id, {}).then((data) => {
        this.detail = data.data || {}
      })
    },
    gotoList () {
      this.$router.push({name: `/${this.$i18n.locale}/application`})
    },
    handleTabChange (data) {
      let name = data.name
      if (name === 'producerDetail') {
        this.$router.push({
          name: `/${this.$i18n.locale}/application/detail`,
          query: {
            id: this.app.id,
            code: this.app.code,
            app: this.app.code,
            topic: this.$route.query.topic || '',
            namespace: this.$route.query.namespace || '',
            subscribeGroup: this.$route.query.subscribeGroup || '',
            subTab: this.$route.query.subTab || 'partitioin',
            tab: name
          }
        })
      } else {
        this.$router.push({
          name: `/${this.$i18n.locale}/application/detail`,
          query: {
            id: this.app.id,
            code: this.app.code,
            tab: name
          }
        })
      }
      this.$refs[name].getList()
    }
  },
  mounted () {
    this.app.id = this.$route.query.id
    this.app.code = this.$route.query.code
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
