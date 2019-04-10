// 表单公共方法：表单校验，表单重置
// 所有表单请覆盖此方法

import {deepCopy} from '../utils/assist.js'
import {getCodeRule, getNameRule} from '../utils/common.js'
import apiRequest from '../utils/apiRequest.js'

export default {
  props: {
    data: {
      type: Object,
      default: function () {
        return {}
      }
    },
    type: 0
  },
  data () {
    return {
      formData: this.data,
      rules: {
        code: getCodeRule(),
        name: getNameRule(),
        description: [{ max: 512, message: '长度在 512 个字符以内', trigger: 'blur' }]
      },
      error: {
        code: ''
      },
      urls: {
        add: ``,
        edit: ``
      },
      addData: {}
    }
  },
  methods: {
    getFormData () {
      return this.formData
    },
    // 用于对话框确认按钮，不需要传表单类型type
    submitForm () {
      return new Promise((resolve, reject) => {
        for (var i in this.error) {
          if (this.error.hasOwnProperty(i)) {
            this.error[i] = ''
          }
        }
        this.$refs.form.validate((valid) => {
          if (valid) {
            resolve(this.getFormData())
          } else {
            reject(new Error('验证不通过，请重新填写！'))
          }
        })
      })
    },
    resetForm () {
      this.$refs.form.resetFields()
    },
    setError (error) {
      for (var key in error) {
        if (error.hasOwnProperty(key)) {
          this.error[key] = error[key]
        }
      }
    },
    // 用于表单里面确认，需传type参数，区分新增和编辑
    beforeConfirm () {
      return deepCopy(this.formData || {})
    },
    confirm () {
      // Before
      let data = this.beforeConfirm()
      // Validate
      this.$refs.form.validate((valid) => {
        if (valid) {
          let url = ''
          if (this.type === undefined || this.type === this.$store.getters.addFormType) {
            url = this.urls.add
          } else if (this.type === this.$store.getters.editFormType) {
            url = this.urls.edit
          }
          apiRequest.post(url, {}, data).then((data) => {
            if (data.code === this.$store.getters.successCode) {
              this.$emit('on-dialog-cancel')
            } else if (data.code === this.$store.getters.validationCode) { // invalid inputs
              let errors = (data.message || '').split('|')
              if (errors === undefined || errors.length < 2) {
                this.$Dialog.error({
                  content: '添加申请失败'
                })
              } else {
                let error = {}
                errors[0].split(',').forEach(field => {
                  error[field] = errors[1]
                })
                this.setError(error)
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
