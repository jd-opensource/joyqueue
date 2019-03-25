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
  '/workflow/apply': {
    search: '/apply/search',
    del: '/apply/delete'
  },
  '/workflow/audit': {
    search: '/audit/search',
    approve: '/audit/approve',
    reject: '/audit/reject',
    confirm: '/audit/confirm',
    execute: '/audit/execute',
    del: '/audit/delete',
    findApply: '/audit/findApply'
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
  '/setting/hosts': {
    search: '/hosts/search',
    add: '/hosts/add',
    edit: '/hosts/update',
    del: '/hosts/delete',
    generateBroker: '/hosts/generateBroker',
    sync: '/hosts/syncBrokers'
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
  '/setting/task': {
    search: '/task/search',
    add: '/task/add',
    edit: '/task/update',
    del: '/task/delete',
    state: '/task/state'
  },
  // '/setting/chartSet': {
  //   search: '/chartSet/search',
  //   add: '/chartSet/add',
  //   del: '/chartSet/delete',
  //   edit:'/chartSet/update',
  // },
  '/setting/executor': {
    search: '/executor/search',
    add: '/executor/add',
    batchAdd: '/executor/batchAdd',
    del: '/executor/delete',
    state: '/executor/state',
    edit: '/executor/update',
    batchState: '/executor/batchState'
  },
  '/setting/label': {
    search: '/config/search',
    add: '/config/add',
    edit: '/config/update',
    del: '/config/delete'
  },
  '/setting/namespace': {
    search: '/namespace/search',
    add: '/namespace/add',
    edit: '/namespace/update',
    del: '/namespace/delete'
  },
  '/setting/alarmType': {
    search: '/alarmType/search',
    add: '/alarmType/add',
    edit: '/alarmType/update',
    del: '/alarmType/delete'
  },
  '/setting/alarm': {
    search: '/alarmRule/search',
    add: '/alarmRule/add',
    edit: '/alarmRule/update',
    del: '/alarmRule/delete',
    state: '/alarmRule/state'
  },
  '/setting/auditRole': {
    search: '/auditRole/search',
    add: '/auditRole/add',
    edit: '/auditRole/update',
    del: '/auditRole/delete',
    state: '/auditRole/state'
  },
  '/setting/auditFlow': {
    search: '/auditFlow/search',
    add: '/auditFlow/add',
    edit: '/auditFlow/update',
    del: '/auditFlow/delete',
    state: '/auditFlow/state'
  },

  '/tool/taskHistory': {
    search: '/task/search',
    del: '/task/delete'
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
    export:'/archive/export',
    batchRetry:'/archive/batchRetry'
  },
  '/tool/operateHistory': {
    search: '/operLog/search'
  },
  '/tool/alarmRule': {
    search: '/alarmRule/search',
    add: '/alarmRule/add',
    del: '/alarmRule/delete',
    edit: '/alarmRule/update'
  },
  '/tool/alarmRuleTemplate': {
    search: '/alarmRuleTemplate/search',
    add: '/alarmRuleTemplate/add',
    del: '/alarmRuleTemplate/delete',
    edit: '/alarmRuleTemplate/update'
  },
  '/tool/archiveTask':{
    search: '/archiveTask/search',
    download: '/archiveTask/download'
  },
  '/tool/metric': {
    search: '/metric/search',
    add: '/metric/add',
    del: '/metric/delete',
    edit:'/metric/update',
  }

}
