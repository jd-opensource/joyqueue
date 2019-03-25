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

export function getCodeRule () {
  return [
    {required: true, message: '请输入英文名', trigger: 'change'},
    {pattern: /^[a-zA-Z]+[a-zA-Z0-9_-]{1,120}[a-zA-Z0-9]+$/, message: '英文名格式不匹配', trigger: 'change'}
  ]
}

export function getCodeRule2 () {
  return [
    {required: true, message: '请输入英文名', trigger: 'change'},
    {pattern: /^[a-zA-Z][a-zA-Z0-9]{2,20}$/, message: '英文名格式不匹配', trigger: 'change'}
  ]
}

export function getCodeRule3 () {
  return [
    {required: true, message: '请输入英文名', trigger: 'change'},
    {pattern: /^[a-zA-Z]+[a-zA-Z0-9/._-]{1,39}[a-zA-Z0-9]+$/, message: '英文名格式不匹配', trigger: 'change'}
  ]
}

export function getNameRule () {
  return [
    { required: true, message: '请输入中文名', trigger: 'change' },
    {pattern: /^[\u4E00-\u9FA5a-zA-Z]+[\u4E00-\u9FA5a-zA-Z0-9/._-]{1,60}[\u4E00-\u9FA5a-zA-Z0-9]+$/, message: '中文名格式不匹配', trigger: 'change'}
  ]
}

export function getSubscribeGroupRule () {
  return [
    {required: true, message: '请输入英文名', trigger: 'change'},
    {pattern: /^[a-zA-Z]+[a-zA-Z0-9/_-]{1,20}[a-zA-Z0-9]+$/, message: '英文名格式不匹配', trigger: 'change'}
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

  return h('d-button', {
    props: {
      size: 'small',
      borderless: true,
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
      txt: 'jmq',
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
export function topicTypeOptions() {
  return [
    {
      value: 0,
      txt: "普通主题",
      color: "success"
    },
    {
      value: 1,
      txt: "广播主题",
      color: "warning"
    },
    {
      value: 2,
      txt: "顺序主题",
      color: "danger"
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
  return baseBtnRender(h, value, clientTypeOptions())
}
export function topicTypeBtnRender(h, value) {
  return baseBtnRender(h, value, topicTypeOptions());
}

export function subscribeGroupAutoCompleteRender (h, params, subscribeRef) {
  function querySearch (queryString, cb) {
    var restaurants = [
      { 'value': '三全鲜食（北新泾店）', 'address': '长宁区新渔路144号' },
      { 'value': 'Hot honey 首尔炸鸡（仙霞路）', 'address': '上海市长宁区淞虹路661号' },
      { 'value': '新旺角茶餐厅', 'address': '上海市普陀区真北路988号创邑金沙谷6号楼113' }
    ]
    var results = queryString ? restaurants.filter(restaurant => {
      return (restaurant.value.toLowerCase().indexOf(queryString.toLowerCase()) === 0)
    }) : restaurants
    // 调用 callback 返回建议列表的数据
    cb(results)
  }

  return h('d-autocomplete', {
    props: {
      value: params.item.code, // subscribeGroup,
      placeholder: '请输入订阅分组',
      // disabled: !params.item['subscribeGroupExist']
      fetchSuggestions: querySearch
    },
    on: {
      select: (item) => {
        params.item.code = item.value || ''
        subscribeRef.tableData.rowData[params.index] = params.item
      }
    }
  })
}
