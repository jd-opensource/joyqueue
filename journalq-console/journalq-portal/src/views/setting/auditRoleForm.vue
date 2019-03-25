<template>
  <div>
    <d-form ref="form" :model="formData" :rules="rules" label-width="100px" style="height: 370px; overflow-y:auto; width: 100%; padding-right: 50px">
      <d-form-item label="英文名:" :error="error.code" prop="code">
        <d-input v-model="formData.code" :placeholder="$store.getters.placeholder.code"/>
      </d-form-item>
      <d-form-item label="中文名:" prop="name">
        <d-input v-model="formData.name" :placeholder="$store.getters.placeholder.name"/>
      </d-form-item>
      <d-form-item label="定值方式:" prop="userType">
        <d-radio-group v-model="valueSetting" name="radiogroup" @on-change="handleValueSettingChange">
          <d-radio label="SQL">设置查询SQL</d-radio>
          <d-radio label="USER">指定审批人</d-radio>
        </d-radio-group>
      </d-form-item>
      <div v-if="valueSetting=='SQL'">
        <d-form-item label="查询SQL:" :error="error.sql" prop="sql" >
          <d-input type="textarea" rows="3" v-model="formData.sql"
                       placeholder="请输入审批人查询条件SQL，完整SQL为：SELECT u.* FROM user u WHERE + 查询条件SQL。查询条件SQL支持变量，格式:#{v},变量范围仅限apply表中存储的字段，包括payload中可解析的字段。" />
        </d-form-item>
      </div>
      <div v-else-if="valueSetting=='USER'">
        <d-form-item label="指定审批人:" :error="error.userCodes" prop="userCodes" >
          <d-input type="textarea" rows="3" v-model="formData.userCodes"
                       placeholder="请输入指定审批人Erp(格式：英文小写)，支持多个审批人，以英文逗号格式"/>
        </d-form-item>
      </div>
      <d-form-item label="备注:" prop="description" >
        <d-input type="textarea" rows="2" v-model="formData.description" placeholder="请输入备注" />
      </d-form-item>
    </d-form>
  </div>
</template>

<script>
import form from '../../mixins/form.js'
// import {getCodeRule} from '../../utils/common.js';
import {getNameRule} from '../../utils/common.js'

export default {
  name: 'audit-role-form',
  mixins: [ form ],
  props: {
    type: 0, // add or edit form
    data: {
      type: Object,
      default: function () {
        return {
          id: 0,
          code: '',
          name: '',
          sql: '',
          userCodes: '',
          description: '',
          status: 1
        }
      }
    }
  },
  data () {
    var sqlValidator = (rule, value, callback) => {
      if (this.valueSetting === 'USER') {
        this.formData.sql = ''
        return callback()
      } else {
        if (this.formData.sql === undefined || this.formData.sql.trim() === '') {
          return callback(new Error('请输入查询SQL条件'))
        } else {
          // set value
          this.formData.sql = this.formData.sql.trim()
          this.formData.userCodes = ''
          return callback()
        }
      }
    }
    var userCodesValidator = (rule, value, callback) => {
      if (this.valueSetting === 'SQL') {
        this.formData.userCode = ''
        return callback()
      } else if (this.formData.code === 'ADMIN') {
        this.formData.sql = ''
        this.formData.userCode = '0'
        return callback()
      } else {
        // set sql
        this.formData.sql = ''
        // validate userCodes
        if (!this.formData.userCodes || !this.formData.userCodes.trim()) {
          return callback(new Error('请输入指定审批人'))
        } else {
          this.formData.userCodes = this.formData.userCodes.trim().replace('，', ',').toLowerCase()
          return callback()
        }
      }
    }
    return {
      valueSetting: 'SQL',
      formData: this.data || {},
      urls: {
        // getUserByIds: '/user/getByIds/',
        // getUserByCode: '/user/getByCode/'
      },
      rules: {
        code: [
          {required: true, message: '请输入英文名', trigger: 'change'},
          {pattern: /^[a-zA-Z]{3,50}$/, message: '英文名格式不匹配', trigger: 'change'}
        ],
        name: getNameRule(),
        sql: [{validator: sqlValidator, trigger: 'blur'}],
        userCodes: [{validator: userCodesValidator, trigger: 'blur'}],
        description: [{ max: 512, message: '长度在 512 个字符以内', trigger: 'blur' }]
      },
      error: {
        code: '',
        sql: '',
        userCodes: ''
      }
    }
  },
  methods: {
    handleValueSettingChange (data) {
      if (data === 'SQL') {
        this.formData.userCodes = ''
      } else if (data === 'USER') {
        this.formData.sql = ''
      }
    }
  },
  mounted () {
    if (this.formData.userCodes !== undefined && this.formData.userCodes !== '') {
      this.valueSetting = 'USER'
    } else {
      this.valueSetting = 'SQL'
    }
  }
}
</script>

<style scoped>

</style>
