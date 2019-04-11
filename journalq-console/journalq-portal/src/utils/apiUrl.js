// url统一存放
export default {
  userInfo: '/login/user', // 用户信息
  logout: '/logout', // 用户信息
  '/application': {
    search: '/application/search',
    sync: '/sync/application',
    del: '/application/delete'
  },
  '/application/detail': {
    detail: '/application/get'
  },
  '/topic': {
    search: '/topic/search',
    add: '/topic/addWithBrokerGroup',
    edit: '/topic/update',
    del: '/topic/delete',
    searchBrokerGroup: '/brokerGroup/findAll',
    editLabelData: '/topic/topicLabel',
    eidtLabel: '/topic/updateLabel.do',
    getUrl: `/grafana/getRedirectUrl`
  },
  '/topic/detail': {
    detail: '/topic/get'
  },
  '/setting/user': {
    search: '/user/search',
    add: '/sync/user',
    edit: '/user/update'
  },
  '/setting/dataCenter': {
    search: '/dataCenter/search',
    add: '/dataCenter/add',
    edit: '/dataCenter/update',
    del: '/dataCenter/delete',
    addNet: ''
  },
  '/setting/config': {
    search: '/config/search',
    add: '/config/add',
    edit: '/config/update',
    del: '/config/delete'
  },
  '/setting/brokerGroup': {
    search: '/brokerGroup/search',
    add: '/brokerGroup/add',
    del: '/brokerGroup/delete',
    getBroker: '/broker/search',
    detail: '/brokerGroup/get',
    edit: '/brokerGroup/update'
  },
  '/setting/broker': {
    search: '/broker/search',
    edit: '/broker/update',
    del: ''
  },
  '/setting/namespace': {
    search: '/namespace/search',
    add: '/namespace/add',
    edit: '/namespace/update',
    del: '/namespace/delete'
  },
  '/setting/metric': {
    search: '/metric/search',
    add: '/metric/add',
    del: '/metric/delete',
    edit: '/metric/update'
  },
  '/tool/retry': {
    search: '/retry/search',
    del: '/retry/delete',
    download: '/retry/download',
    recovery: '/retry/recovery'
  },
  '/tool/archive': {
    search: '/archive/search',
    add: '/archive/add',
    consume: '/archive/consume',
    download: '/archive/download',
    retry: '/archive/retry',
    export: '/archive/export',
    batchRetry: '/archive/batchRetry'
  },
  '/tool/operateHistory': {
    search: '/operLog/search'
  },
  common: {
    findSubscribeGroup: `/consumer/findAllSubscribeGroups`
  }

}
