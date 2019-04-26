/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import apiRequest from './apiRequest.js'
import apiUrl from './apiUrl.js'
import {t} from "../locale";

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
      txt: 'journalq',
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
  return baseBtnRender(h, value, clientTypeOptions())
}
export function topicTypeBtnRender (h, value) {
  return baseBtnRender(h, value, topicTypeOptions())
}

export function subscribeGroupAutoCompleteRender (h, params, subscribeRef) {
  function querySearch (queryString, callback) {
    apiRequest.get(apiUrl.common.findSubscribeGroup).then((data) => {
      if (data.data === undefined || data.data === []) {
        var emptyResult = [{'value': ''}]
        callback(emptyResult)
      }
      var subscribeGroups = data.data.map(sg => {
        return {'value': sg}
      })
      var results = queryString ? subscribeGroups.filter(sg => {
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

export function replaceChartUrl (url, namespaceCode, topicCode, appFullName) {
  if (!url || url === '') {
    return undefined
  }
  if (!namespaceCode) {
    namespaceCode = 'default'
  }
  return url.replace(/\[namespace\]/g, namespaceCode).replace(/\[topic\]/g, topicCode).replace(/\[app\]/g, appFullName)
}
