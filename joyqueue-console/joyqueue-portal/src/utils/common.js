import apiRequest from './apiRequest.js'
import apiUrl from './apiUrl.js'

export function getTopicCode (topic, namespace) {
  let topicCode = (topic || {}).code
  if (topicCode.indexOf('.') >= 0) {
    topicCode = topicCode.split('.')[1]
  }

  if (!namespace || !namespace.code || namespace.code.trim().length === 0) {
    return topicCode
  }

  return (namespace || {}).code + '.' + topicCode
}

export function getTopicCodeByCode (topicCode, namespaceCode) {
  if (topicCode.indexOf('.') >= 0) {
    topicCode = topicCode.split('.')[1]
  }

  if (!namespaceCode || namespaceCode.trim().length === 0) {
    return topicCode
  }

  return namespaceCode + '.' + topicCode
}

export function generateProducerDetailTabName (appCode, topicCode, namespaceCode) {
  return '生产详情-' + appCode + '@' + getTopicCodeByCode(topicCode, namespaceCode)
}

export function generateConsumerDetailTabName (appCode, subscribeGroup, topicCode, namespaceCode) {
  return '消费详情-' + getAppCodeByCode(appCode, subscribeGroup) + '@' + getTopicCodeByCode(topicCode, namespaceCode)
}

export function resolveTopicCode (topicCode) {
  if (topicCode === undefined) {
    return undefined
  } else if (topicCode.indexOf('.') < 0) {
    return topicCode
  } else {
    return topicCode.split('.')[1]
  }
}

export function resolveNamespaceCode (topicCode) {
  if (topicCode === undefined) {
    return undefined
  } else if (topicCode.indexOf('.') < 0) {
    return ''
  } else {
    return topicCode.split('.')[0]
  }
}

export function getAppCode (app, subscribeGroup) {
  if (!subscribeGroup || subscribeGroup.trim().length === 0) {
    return (app || {}).code
  }
  return (app || {}).code + '.' + subscribeGroup
}

export function getAppCodeByCode (appCode, subscribeGroup) {
  if (!subscribeGroup || subscribeGroup.trim().length === 0) {
    return appCode
  }
  return appCode + '.' + subscribeGroup
}

export function resolveAppCode (appCode) {
  if (appCode === undefined) {
    return undefined
  } else if (appCode.indexOf('.') < 0) {
    return appCode
  } else {
    return appCode.split('.')[0]
  }
}

export function getCodeRule () {
  return [
    {required: true, message: '请输入英文名', trigger: 'change'},
    {pattern: /^[a-zA-Z]+[a-zA-Z0-9_-]{1,120}[a-zA-Z0-9]+$/, message: '支持字母、数字、下划线(_)和横线(-)，以英文字母开头，不超过120个字符', trigger: 'change'}
  ]
}

export function ipValidator () {
  let ipPattern = '(\\d{1,2}|1\\d\\d|2[0-4]\\d|25[0-5])\\.(\\d{1,2}|1\\d\\d|2[0-4]\\d|25[0-5])\\.(\\d{1,2}|1\\d\\d|2[0-4]\\d|25[0-5])\\.(\\d{1,2}|1\\d\\d|2[0-4]\\d|25[0-5])'
  return [
    {pattern: '^' + ipPattern + '(,' + ipPattern + ')*$', message: '请输入正确格式的ip', required: false, trigger: 'blur'}
  ]
}

export function getCodeRule2 () {
  return [
    {required: true, message: '请输入英文名', trigger: 'change'},
    {pattern: /^[a-zA-Z][a-zA-Z0-9]{2,20}$/, message: '支持英文字母和数字，以英文字母开头，长度在2-20个字符之间', trigger: 'change'}
  ]
}

export function getCodeRule3 () {
  return [
    {required: true, message: '请输入英文名', trigger: 'change'},
    {pattern: /^[a-zA-Z]+[a-zA-Z0-9/._-]{1,100}[a-zA-Z0-9]+$/, message: '支持字母、数字、英文句号(.)、下划线(_)和横线(-)，以英文字母开头，不超过100个字符', trigger: 'change'}
  ]
}

export function getErpsRule () {
  return [
    {required: true, message: '支持字母、数字、减号(-)、下划线(_)和英文句号(.)，多个ERP用英文逗号(,)分隔，不能有空格', trigger: 'change'},
    {pattern: /^[\w-._]+(,[\w-._]+)*$/, message: '支持字母、数字、减号(-)、下划线(_)和英文句号(.)，多个ERP用英文逗号(,)分隔，不能有空格', trigger: 'change'}
  ]
}

export function getNameRule () {
  return [
    { required: true, message: '请输入中文名', trigger: 'change' },
    {pattern: /^[\u4E00-\u9FA5a-zA-Z]+[\u4E00-\u9FA5a-zA-Z0-9/._-]{1,60}[\u4E00-\u9FA5a-zA-Z0-9]+$/, message: '支持中文、字母、数字、减号(-)、下划线(_)和英文句号(.)，以中英文字符开头，不超过60个字符', trigger: 'change'}
  ]
}

export function getSubscribeGroupRule () {
  return [
    {required: true, message: '请输入订阅分组', trigger: 'change'},
    {pattern: /^[a-zA-Z]+[a-zA-Z0-9/_-]{1,20}[a-zA-Z0-9]+$/, message: '支持字母、数字、下划线(_)和横线(-)，以英文字母开头，不超过20个字符', trigger: 'change'}
  ]
}

export function baseBtnRender (h, value, valueTxtColorOptions) {
  if (value === undefined || value === '') {
    return h('label', '')
  }

  let txt = value
  let color = ''
  valueTxtColorOptions.forEach((option) => {
    if (value === option.value) {
      txt = option.txt
      color = option.color
    }
  })

  return h('d-tag', {
    props: {
      size: 'small',
      color: color
    }
  }, txt)
}

export function basePrimaryBtnRender (h, value, valueTxtColorOptions) {
  if (value === undefined || value === '') {
    return h('label', '')
  }

  let txt = value
  let color = ''
  valueTxtColorOptions.forEach((option) => {
    if (value === option.value) {
      txt = option.txt
      color = option.color
    }
  })

  return h('d-tag', {
    props: {
      size: 'small',
      color: color
    }
  }, txt)
}

export function openOrCloseOptions () {
  return [
    {
      value: true,
      txt: '开启',
      color: 'success'
    },
    {
      value: false,
      txt: '关闭',
      color: 'danger'
    }
  ]
}

export function openOrCloseBtnRender (h, value) {
  return baseBtnRender(h, value, openOrCloseOptions())
}

export function yesOrNoOptions () {
  return [
    {
      value: true,
      txt: '是',
      color: 'success'
    },
    {
      value: false,
      txt: '否',
      color: 'danger'
    }
  ]
}

export function yesOrNoBtnRender (h, value) {
  return baseBtnRender(h, value, yesOrNoOptions())
}

/**
 * 客户端类型下拉选项
 */
export function clientTypeOptions () {
  return [
    {
      value: 0,
      txt: 'joyqueue',
      color: 'success'
    },
    {
      value: 1,
      txt: 'kafka',
      color: 'warning'
    },
    {
      value: 2,
      txt: 'mqtt',
      color: 'danger'
    },
    {
      value: 10,
      txt: 'others',
      color: 'info'
    }
  ]
}
export function topicTypeOptions () {
  return [
    {
      value: 0,
      txt: '普通主题',
      color: 'success'
    },
    {
      value: 1,
      txt: '广播主题',
      color: 'warning'
    },
    {
      value: 2,
      txt: '顺序主题',
      color: 'danger'
    }
  ]
}

export function clientTypeSelectRender (h, params, subscribeRef) {
  return h('d-select', {
    props: {
    },
    on: {
      'on-change': (newValue) => {
        params.item['clientType'] = newValue
        subscribeRef.tableData.rowData[params.index] = params.item
      }
    }
  },
  clientTypeOptions().map((item) => {
    return h('d-option', {
      props: {
        value: item.value,
        label: item.txt
      }
    })
  }))
}

export function clientTypeBtnRender (h, value) {
  return basePrimaryBtnRender(h, value, clientTypeOptions())
}
export function topicTypeBtnRender (h, value) {
  return basePrimaryBtnRender(h, value, topicTypeOptions())
}

export function subscribeGroupAutoCompleteRender (h, params, subscribeRef) {
  function querySearch (queryString, callback) {
    apiRequest.get(apiUrl.common.findSubscribeGroup).then((data) => {
      if (data.data === undefined || data.data === []) {
        let emptyResult = [{'value': ''}]
        callback(emptyResult)
      }
      let subscribeGroups = data.data.map(sg => {
        return {'value': sg}
      })
      let results = queryString ? subscribeGroups.filter(sg => {
        return sg.value.toLowerCase().indexOf(queryString.toLowerCase().trim()) === 0
      }) : subscribeGroups
      // 调用 callback 返回建议列表的数据
      callback(results)
    })
  }

  return h('d-autocomplete', {
    props: {
      value: params.item.subscribeGroup,
      placeholder: '请输入订阅分组',
      fetchSuggestions: querySearch
    },
    on: {
      select: (item) => {
        params.item.subscribeGroup = item.value || ''
        subscribeRef.tableData.rowData[params.index] = params.item
      },
      input: (item) => {
        params.item.subscribeGroup = (item.value || item) || ''
        subscribeRef.tableData.rowData[params.index] = params.item
      }
    }
  })
}

export function subscribeGroupInputRender (h, params, subscribeRef) {
  return h('d-input', {
    props: {
      value: params.item.subscribeGroup,
      placeholder: '请输入订阅分组'
    },
    on: {
      input: (item) => {
        params.item.subscribeGroup = (item.value || item) || ''
        subscribeRef.tableData.rowData[params.index] = params.item
      }
    }
  })
}

export function replaceChartUrl (url, namespaceCode, topicCode, appFullName) {
  if (!url || url === '') {
    return undefined
  }
  if (!namespaceCode) {
    namespaceCode = 'default'
  }
  return url.replace(/\[namespace\]/g, namespaceCode).replace(/\[topic\]/g, topicCode).replace(/\[app\]/g, appFullName)
}
