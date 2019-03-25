// 申请表单公共方法：App、topic查询、选择、校验
// 所有申请表单表单请覆盖此方法

import apiRequest from '../utils/apiRequest.js'
import {transStringListToObject, deepCopy} from '../utils/assist.js'
import {getNameRule, getTopicCode, resolveTopicCode, resolveNamespaceCode} from '../utils/common.js'

export default {
  props: {
    formType: 0, // 0:add,1:edit
    data: {
      type: Object,
      default: function () {
        return {
          id: 0,
          app: {
            id: 0,
            code: ''
          },
          topic: {
            id: 0,
            code: '',
            namespace: {
              id: 0,
              code: ''
            }
          },
          subscribeType: 0
        }
      }
    }
  },
  data () {
    return {
      formData: this.data,
      timeout: null,
      urls: {
        findApp: `/application/search`,
        findTopic: `/topic/search`,
        add: '',
        edit: ''
      },
      rules: {
        'app.code': [
          {required: true, message: '请选择应用英文名', trigger: 'change'}
        ],
        'topic.code': [
          {required: true, message: '请选择主题英文名', trigger: 'change'}
        ],
        subscribeType: [
          {type: 'number', required: true, message: '请选择订阅类型', trigger: 'change'}
        ]
      },
      error: {
        appCode: '',
        topicCode: ''
      },
      appList: [],
      topicList: []
    }
  },
  methods: {
    queryApp (queryString, callback) {
      this.error.appCode = ''
      if (queryString === undefined || queryString == '') {
        callback(this.appList)
      } else {
        clearTimeout(this.timeout)
        this.getApps(queryString).then(data => {
          this.timeout = setTimeout(() => {
            callback(data)
          }, 500)
        })
      }
    },
    handleAppSelect (item) {
      this.formData.app.id = item.id
      this.formData.app.code = item.code
    },
    getApps (keyword) {
      return new Promise((resolve, reject) => {
        let obj = {
          pagination: {
            page: 1,
            size: 100
          },
          query: {
            subscribeType: this.subscribeType
          }
        }
        if (keyword !== undefined || keyword != '') {
          obj.query.keyword = keyword
        }
        apiRequest.post(this.urls.findApp, {}, obj).then((data) => {
          if (keyword === undefined || keyword.trim() === '') {
            this.appList = data.data || []
          }
          if (data.code == this.$store.getters.successCode) {
            resolve(data.data || [])
          } else {
            resolve([])
          }
        }).catch(error => {
          reject(error)
        })
      })
    },
    queryTopic (queryString, callback) {
      this.error.topicCode = ''
      if (queryString === undefined || queryString == '') {
        callback(this.topicList)
      } else {
        clearTimeout(this.timeout)
        this.getTopics(queryString).then(data => {
          this.timeout = setTimeout(() => {
            callback(data)
          }, 500 * Math.random())
        })
      }
    },
    handleTopicSelect (item) {
      this.formData.topic.id = item.id
      this.formData.topic.code = item.code
      this.formData.topic.namespace.id = item.namespace.id
      this.formData.topic.namespace.code = item.namespace.code
    },
    getTopics (keyword) {
      return new Promise((resolve, reject) => {
        let obj = {
          pagination: {
            page: 1,
            size: 100
          },
          query: {
            subscribeType: this.$store.getters.consumerType
          }
        }
        if (keyword !== undefined || keyword != '') {
          obj.query.keyword = keyword
        }
        apiRequest.post(this.urls.findTopic, {}, obj).then((data) => {
          if (keyword === undefined || keyword.trim() === '') {
            this.topicList = (data.data || []).map(topic => {
              topic.code = getTopicCode(topic, topic.namespace)
              return topic
            })
          }
          if (data.code == this.$store.getters.successCode) {
            resolve((data.data || []).map(topic => {
              topic.code = getTopicCode(topic, topic.namespace)
              return topic
            }))
          } else {
            resolve([])
          }
        }).catch(error => {
          reject(error)
        })
      })
    },
    beforeConfirm () {
      // Resolve topic code
      let copyData = deepCopy(this.formData || {})
      let topicCode = copyData.topic.code
      copyData.topic.code = resolveTopicCode(topicCode)
      copyData.topic.namespace.code = resolveNamespaceCode(topicCode)
      return copyData
    },
    confirm () {
      // Before
      let addData = this.beforeConfirm()
      // Validate
      this.$refs.form.validate((valid) => {
        if (valid) {
          // Add or edit
          console.log(this.formType ? this.urls.edit : this.urls.add)
          apiRequest.post(this.formType ? this.urls.edit : this.urls.add, {}, addData).then((data) => {
            if (data.code == this.$store.getters.successCode) {
              this.$emit('on-dialog-cancel')
            } else if (data.code == this.$store.getters.validationCode) { // invalid inputs
              let errors = (data.message || '').split('|')
              if (errors == undefined || errors.length < 2) {
                this.$Dialog.error({
                  content: '添加申请失败'
                })
              } else {
                let error = {}
                errors[0].split(',').forEach(field => {
                  error[field] = errors[1]
                })
                for (var key in error) {
                  if (error.hasOwnProperty(key)) {
                    this.error[key] = error[key]
                  }
                }
                this.$Message.success('验证不通过: ' + data.message)
              }
            }
          })
        } else {
          this.$Message.error('验证不通过，请重新填写！')
        }
      })
    }
  }
}
