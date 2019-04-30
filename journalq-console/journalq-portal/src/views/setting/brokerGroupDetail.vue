<template>
  <div style="margin:20px;" class="clearfix">
    <d-breadcrumb class="mb20" separator=">">
      <d-breadcrumb-item :to="{ name: `/${$i18n.locale}/setting/brokerGroup` }">分组管理</d-breadcrumb-item>
      <d-breadcrumb-item>{{brokerGroup.name}}</d-breadcrumb-item>
    </d-breadcrumb>
    <!--<div  class="el-breadcrumb mb20">
      <span class="el-breadcrumb__item">
        <span role="link" class="el-breadcrumb__inner is-link" @click="gotoList">
          分组管理
        </span>
        <icon name="chevron-right" class="el-breadcrumb__separator"></icon>
      </span>
      <span class="el-breadcrumb__item" aria-current="page">
          <span role="link" class="el-breadcrumb__inner">{{brokerGroup.name}}</span>
      </span>
    </div>-->
    <div class="detail mb20">
      <div class="title">{{brokerGroup.name}}</div>
      <grid-row :gutter="16">
        <grid-col span="8">
          <span>ID:</span>
          <span>{{brokerGroup.id}}</span>
        </grid-col>
        <grid-col span="8">
          <span>编码:</span>
          <span>{{brokerGroup.code}}</span>
        </grid-col>
        <grid-col span="8">
          <span>名称:</span>
          <span>{{brokerGroup.name}}</span>
        </grid-col>
      </grid-row>
    </div>
    <d-tabs>
      <d-tab-pane label="Broker" name="name2" icon="file-text">
      <broker-tab></broker-tab>
      </d-tab-pane>
    </d-tabs>
  </div>
</template>

<script>
import crud from '../../mixins/crud.js'
import BrokerTab from './brokerTab.vue'

export default {
  name: 'brokerGroupDetail',
  components: {
    BrokerTab
  },
  mixins: [ crud ],
  data () {
    return {
      brokerGroup: {
        id: 0,
        code: '',
        name: ''
      }
    }
  },
  methods: {
    gotoList () {
      this.$router.push({name: `/setting/brokerGroup`})
    }
  },
  created () {
    this.brokerGroup.id = this.$route.query.id
    this.brokerGroup.code = this.$route.query.code
    this.brokerGroup.name = this.$route.query.name
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
