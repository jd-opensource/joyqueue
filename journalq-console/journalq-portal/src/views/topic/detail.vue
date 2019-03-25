<template>
  <div style="margin:20px;" class="clearfix">
    <d-breadcrumb class="mb20" separator=">">
      <d-breadcrumb-item :to="{ name: `/${$i18n.locale}/topic` }">主题中心</d-breadcrumb-item>
      <d-breadcrumb-item>{{topic.code}}</d-breadcrumb-item>
    </d-breadcrumb>
    <!--<div  class="el-breadcrumb mb20">-->
      <!--<span class="el-breadcrumb__item">-->
        <!--<span role="link" class="el-breadcrumb__inner is-link" @click="gotoList">-->
          <!--主题中心-->
        <!--</span>-->
        <!--<icon name="chevron-right" class="el-breadcrumb__separator"></icon>-->
      <!--</span>-->
      <!--<span class="el-breadcrumb__item" aria-current="page">-->
          <!--<span role="link" class="el-breadcrumb__inner">{{topic.code}}</span>-->
      <!--</span>-->
    <!--</div>-->
    <div class="detail mb20">
      <div class="title">{{topic.code}}</div>
      <!--<grid-row :gutter="16">-->
        <!--<grid-col span="8">-->
          <!--<span>ID:</span>-->
          <!--<span>{{detail.id}}</span>-->
        <!--</grid-col>-->
        <!--&lt;!&ndash;<grid-col span="8">&ndash;&gt;-->
          <!--&lt;!&ndash;<span>中文名称:</span>&ndash;&gt;-->
          <!--&lt;!&ndash;<span>{{detail.name}}</span>&ndash;&gt;-->
        <!--&lt;!&ndash;</grid-col>&ndash;&gt;-->
        <!--<grid-col span="8">-->
          <!--<span>英文名称:</span>-->
          <!--<span>{{detail.code}}</span>-->
        <!--</grid-col>-->
        <!--<grid-col span="8">-->
          <!--<span>类型:</span>-->
          <!--<span>{{typeStr(detail.type)}}</span>-->
        <!--</grid-col>-->
      <!--</grid-row>-->
      <grid-row :gutter="16">
        <!--<grid-col span="8">-->
          <!--<span>队列数:</span>-->
          <!--<span>{{detail.partitions}}</span>-->
        <!--</grid-col>-->
        <!--<grid-col span="8">-->
          <!--<span>归档:</span>-->
          <!--<span>{{archiveStr(detail.archive)}}</span>-->
        <!--</grid-col>-->
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
    <d-tabs @on-change="handleTabChange">
      <d-tab-pane label="生产者" name="producer" icon="user-plus">
        <producer ref="producer" :search="search"/>
      </d-tab-pane>
      <d-tab-pane label="消费者" name="consumer" icon="user-minus">
        <consumer ref="consumer" :search="search"/>
      </d-tab-pane>
      <d-tab-pane label="分组信息" name="partitionGroup" icon="folder" v-if="$store.getters.isAdmin">
        <partitionGroup ref="partitionGroup" @on-partition-group-change="queryTopicDetail"></partitionGroup>
      </d-tab-pane>
    </d-tabs>
  </div>
</template>

<script>
import apiRequest from '../../utils/apiRequest.js'
import MsgServiceMonitor from './brokerMonitor.vue'
import Producer from './producer.vue'
import Consumer from './consumer.vue'
import PartitionGroup from './partitionGroup.vue'
import crud from '../../mixins/crud.js'

export default {
  name: 'applicationDetail',
  components: {
    MsgServiceMonitor,
    Producer,
    Consumer,
    PartitionGroup
  },
  mixins: [ crud ],
  data () {
    return {
      urls: {
        getById: '/topic/getById'
      },
      topic: {
        id: '0',
        code: ''
      },
      namespace: {
        id: '0',
        code: ''
      },
      detail: {
        id: '0',
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
  computed: {
    search () {
      return {
        topic: this.topic
      }
    }
  },
  methods: {
    // 组件自身的方法写这里
    getDetail (id) {
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
    queryTopicDetail () {
      // 获取命名空间详情页
      this.getDetail(this.topic.id)
    },
    handleTabChange (data) {
      let name = data.name
      this.$refs[name].getList()
    },
    typeStr (type) {
      let label
      switch (type) {
        case 0:
          label = 'topic'
          break
        case 1:
          label = 'broadcast'
          break
        case 2:
          label = 'seuqential'
          break
      }
      return label
    },
    archiveStr (archive) {
      return archive === false ? '已关闭' : '已开启'
    }
  },
  mounted () {
    this.topic.id = this.$route.query.id
    this.topic.code = this.$route.query.code
    this.namespace.id = this.$route.query.namespaceId
    this.namespace.code = this.$route.query.namespaceCode
    this.queryTopicDetail()
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
