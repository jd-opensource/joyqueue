<template>
  <div style="margin:20px;" class="clearfix">
    <d-breadcrumb class="mb20" separator=">">
      <d-breadcrumb-item :to="{ name: `/${$i18n.locale}/application` }">我的应用</d-breadcrumb-item>
      <d-breadcrumb-item>{{detail.code}}</d-breadcrumb-item>
    </d-breadcrumb>
    <!--<div  class="el-breadcrumb mb20">
      <span class="el-breadcrumb__item">
        <span role="link" class="el-breadcrumb__inner is-link" @click="gotoList">
          我的应用
        </span>
        <icon name="chevron-right" class="el-breadcrumb__separator"></icon>
      </span>
      <span class="el-breadcrumb__item" aria-current="page">
          <span role="link" class="el-breadcrumb__inner">{{detail.code}}</span>
      </span>
    </div>-->
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
    <d-tabs  @on-change="handleTabChange">
      <!--tabs由于没有单独的路由名称，所以在调用table时，需要在data中自定义urls-->
      <!--<d-tab-pane label="Broker" name="name1" icon="github">-->
        <!--<msg-service-monitor></msg-service-monitor>-->
      <!--</d-tab-pane>-->
      <d-tab-pane label="生产者" name="producer" icon="user-plus">
        <producer ref="producer" :search="search"/>
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
    </d-tabs>
  </div>
</template>

<script>
import apiRequest from '../../utils/apiRequest.js'
import MsgService from './broker.vue'
import Producer from './producer.vue'
import Consumer from './consumer.vue'
import MyAppUsers from './myAppUsers.vue'
import MyAppToken from './myAppToken.vue'
import crud from '../../mixins/crud.js'

export default {
  name: 'applicationDetail',
  components: {
    MsgService,
    Producer,
    Consumer,
    MyAppUsers,
    MyAppToken
  },
  mixins: [ crud ],
  data () {
    return {
      app: {
        id: 0,
        code: ''
      },
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
  computed: {
    search () {
      return {app: this.app}
    }
  },
  methods: {
    getDetail (id) {
      apiRequest.get(this.urlOrigin.detail + '/' + this.app.id, {}).then((data) => {
        this.detail = data.data || {}
      })
    },
    gotoList () {
      this.$router.push({name: `/${this.$i18n.locale}/application`})
    },
    queryAppDetail () {
      // 获取我的应用详情页
      this.getDetail(this.app.id)
    },
    handleTabChange (data) {
      let name = data.name
      this.$refs[name].getList()
    }
  },
  mounted () {
    this.app.id = this.$route.query.id
    this.app.code = this.$route.query.code
    this.queryAppDetail()
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
