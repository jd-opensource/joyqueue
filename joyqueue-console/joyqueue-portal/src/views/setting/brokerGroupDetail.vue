<template>
  <div style="margin:20px;" class="clearfix">
    <d-breadcrumb class="mb20" separator=">">
      <d-breadcrumb-item :to="{ name: `/${$i18n.locale}/setting/brokerGroup` }">Broker分组管理</d-breadcrumb-item>
      <d-breadcrumb-item>{{brokerGroup.name}}</d-breadcrumb-item>
    </d-breadcrumb>
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
      <broker ref="broker" :searchData="brokerSearchData" :urls="brokerUrls" :colData="brokerColData"
              :operates="brokerBtns"></broker>
      </d-tab-pane>
    </d-tabs>
  </div>
</template>

<script>
import crud from '../../mixins/crud.js'
import apiRequest from '../../utils/apiRequest.js'
import Broker from './broker.vue'

export default {
  name: 'brokerGroupDetail',
  components: {
    Broker
  },
  mixins: [ crud ],
  data () {
    return {
      brokerGroup: {
        id: 0,
        code: '',
        name: ''
      },
      brokerSearchData: {
        brokerGroupId: this.$route.query.id,
        keyword: '',
        groupVisible: false,
        group: this.$route.query.code
      },
      brokerUrls: {
        search: `/broker/search`,
        removeBroker: `/brokerGroup/updateBroker`,
        startInfo: '/monitor/start',
        findDetail: '/monitor/broker/findBrokerDetail/'
      },
      brokerBtns: [
        {
          txt: '移除',
          method: function (item) {
            let that = this.$parent
            that.$Dialog.confirm({
              title: '提示',
              content: '确定要移除吗？'
            }).then(() => {
              let editData = {
                id: item.id,
                group: {
                  id: -1
                }
              }
              apiRequest.put(that.urls.removeBroker + '/' + item.id, {}, editData).then((data) => {
                that.getList()
              })
            })
          }
        }
      ],
      brokerColData: [
        {
          title: 'ID',
          key: 'id',
          width: '10%'
        },
        {
          title: 'IP',
          key: 'ip',
          width: '10%'
        },
        {
          title: '端口',
          key: 'port',
          width: '10%'
        },
        {
          title: '数据中心',
          key: 'dataCenter.id',
          width: '10%'
        },
        {
          title: '启动时间',
          key: 'startupTime',
          width: '10%'
        },
        {
          title: '重试方式',
          key: 'retryType',
          width: '10%'
        },
        {
          title: '权限',
          key: 'permission',
          width: '10%'
        },
        {
          title: '描述',
          key: 'description',
          width: '10%'
        }
      ]
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
