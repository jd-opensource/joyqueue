<template>
  <div style="margin:20px;" class="clearfix">
    <d-breadcrumb class="mb20" separator=">">
      <d-breadcrumb-item :to="{ name: `/${$i18n.locale}/topic` }">主题中心</d-breadcrumb-item>
      <d-breadcrumb-item>{{topic.code}}</d-breadcrumb-item>
    </d-breadcrumb>
    <div class="detail mb20">
      <div class="title">{{topic.code}}</div>
      <grid-row :gutter="16">
        <grid-col span="8">
          <span>队列数:</span>
          <span>{{detail.partitions}}</span>
        </grid-col>
        <grid-col span="8">
          <span>标签:</span>
          <span>{{detail.labels}}</span>
        </grid-col>
        <grid-col span="8">
          <span>备注:</span>
          <span>{{detail.description}}</span>
        </grid-col>
      </grid-row>
    </div>
    <d-tabs @on-change="handleTabChange" :value="tab">
      <slot name="tabs"></slot>
    </d-tabs>
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
      urls: {
        getById: '/topic/getById'
      },
      topic: {
        id: '',
        code: '',
        namespace: {
          id: '',
          code: ''
        }
      },
      app: '',
      tab: '',
      detail: {
        id: '',
        code: '',
        name: '',
        type: '',
        partitions: '',
        archive: false,
        labels: '',
        description: ''
      }
    }
  },
  watch: {
    '$route' (to, from) {
      this.topic.id = to.query.id
      this.topic.code = to.query.code
      this.topic.namespace.id = to.query.namespaceId
      this.topic.namespace.code = to.query.namespaceCode
      this.app = to.query.app
      this.tab = to.query.tab || this.tab
      // this.$refs[this.tab].getList()
    }
  },
  methods: {
    // 组件自身的方法写这里
    getDetail () {
      var data = {
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
      this.$refs[name].getList()
      if (name === 'retry') {
        this.$router.push({name: `/${this.$i18n.locale}/topic/detail`,
          query: {
            id: this.topic.id,
            code: this.topic.code,
            namespaceId: this.topic.namespace.id,
            namespaceCode: this.topic.namespace.code,
            tab: name,
            app: this.$route.query.app || ''}})
      } else {
        this.$router.push({name: `/${this.$i18n.locale}/topic/detail`,
          query: {
            id: this.topic.id,
            code: this.topic.code,
            namespaceId: this.topic.namespace.id,
            namespaceCode: this.topic.namespace.code,
            tab: name}})
      }
    }
  },
  mounted () {
    this.topic.id = this.$route.query.id
    this.topic.code = this.$route.query.code
    this.topic.namespace.id = this.$route.query.namespaceId
    this.topic.namespace.code = this.$route.query.namespaceCode
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
