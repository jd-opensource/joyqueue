<template>
  <div>
    <d-form ref="form" :model="formData" :rules="rules" label-width="100px"
                style="height: 370px; overflow-y:auto; width: 100%; padding-right: 50px">
      <d-form-item label="类型:" prop="type">
        <d-input v-model="formData.type" placeholder="请输入用户类型，仅支持英文大写和-"/>
      </d-form-item>
      <d-form-item label="用户确认:" prop="applicantConfirm">
        <d-switch v-model="formData.applicantConfirm"></d-switch>
      </d-form-item>
      <d-form-item label="手动执行:" prop="manualExecute" @change="handlerManualExecuteChange">
        <d-switch v-model="formData.manualExecute"></d-switch>
      </d-form-item>
      <div v-if="formData.manualExecute">
        <d-form-item label="手动执行类:" prop="executeClass">
          <d-input v-model="formData.executeClass" placeholder="请输入手动执行调用的service，英文大小写格式"/>
        </d-form-item>
        <d-form-item label="手动执行方法:" prop="executeMethod">
          <d-input v-model="formData.executeMethod" placeholder="请输入，默认为execute"/>
        </d-form-item>
      </div>
      <d-form-item label="备注:" prop="description" >
        <d-input type="textarea" rows="2" v-model="formData.description" placeholder="请输入备注" />
      </d-form-item>
      <d-form-item
        v-for="(auditNode, index) in formData.auditNodes"
        :label="'节点' + index"
        :key="auditNode.nodeNo"
        :error="error.auditNodes[index]"
        :required="true"
        :prop="'auditNodes.' + index"
        :rules="rules.auditNodes"
      >
        <d-input type="textarea" rows="2" v-model="auditNode.auditRoles"  @change="handleAuditRolesChange"
                     placeholder="请输入审批角色code,仅支持大小写字母，多个之间以英文逗号隔开"/>
        <span>授权管理员：</span><d-switch v-model="auditNode.containAdmin"/>
        <d-button @click.prevent="addAuditNode(auditNode)">添加</d-button>
        <d-button @click.prevent="removeAuditNode(auditNode)">删除</d-button>
      </d-form-item>
    </d-form>
  </div>
</template>

<script>
import form from '../../mixins/form.js'
import {deepCopy} from '../../utils/assist.js'

export default {
  name: 'audit-flow-form',
  mixins: [ form ],
  props: {
    type: 0, // add or edit form
    data: {
      type: Object,
      default: function () {
        return {
          id: 0,
          type: '',
          manualExecute: true,
          applicantConfirm: true,
          executeClass: '',
          executeMethod: '',
          description: '',
          status: 1,
          auditNodes: [{
            nodeNo: 0,
            auditRoles: 'ADMIN',
            containAdmin: true
          }]
        }
      }
    }
  },
  data () {
    var executeClassValidator = (rule, value, callback) => {
      if (this.formData.manualExecute) {
        if (!this.formData.executeClass || !this.formData.executeClass.trim()) {
          return callback(new Error('请输入手动执行类'))
        } else {
          this.formData.executeClass = this.formData.executeClass.trim()
          return callback()
        }
      }
      return callback()
    }
    var auditNodesValidator = (rule, value, callback) => {
      let auditRole = value.auditRoles
      // Validate whether auditRole
      if (!auditRole || !auditRole.trim()) {
        return callback(new Error('请输入审批角色Code，多个以英文逗号隔开'))
      }
      // Validate auditRole pattern
      let reg = /^[a-zA-Z]{3,50}(,[a-zA-Z]{3,50})*$/
      let result = auditRole.match(reg)
      if (result == null) {
        return callback(new Error('审批角色格式不匹配'))
      }
      // Format
      let roles = auditRole.trim().split(',')
      for (var i = 0; i < roles.length; i++) {
        if (roles[i].toUpperCase() === 'ADMIN') {
          roles.splice(i, 1)
        }
      }
      if (value.containAdmin) {
        roles.push('ADMIN')
      }
      value.auditRoles = roles.join(',')
      return callback()
    }
    return {
      formData: this.data || {},
      urls: {
        // getUserByIds: '/user/getByIds/',
        // getUserByCode: '/user/getByCode/'
      },
      rules: {
        type: [
          {required: true, message: '请输入类型', trigger: 'change'},
          {pattern: /^[A-Z]+[A-Z_]{1,60}[A-Z]+$/, message: '格式不匹配', trigger: 'change'}
        ],
        executeClass: [{validator: executeClassValidator, trigger: 'blur'}],
        description: [{ max: 512, message: '长度在 512 个字符以内', trigger: 'blur' }],
        auditNodes: [
          // {required: true, message: '审批人不能为空', trigger: 'blur'},
          // {pattern: /^[a-zA-Z]{3,50}(,[a-zA-Z]{3,50})*$/, message: '格式不匹配', trigger: 'change'},
          {validator: auditNodesValidator, trigger: 'blur'}
        ]
      },
      error: {
        auditNodes: []
      }
    }
  },
  methods: {
    handlerManualExecuteChange (data) {
      if (!data) {
        this.formData.executeClass = ''
        this.formData.executeMethod = ''
      }
    },
    addAuditNode () {
      this.formData.auditNodes.push({
        nodeNo: this.formData.auditNodes.length,
        auditRoles: 'ADMIN',
        containAdmin: true
      })
    },
    removeAuditNode () {
      var index = this.formData.auditNodes.nodeNo
      if (index !== -1) {
        this.formData.auditNodes.splice(index, 1)
      }
    },
    handleAuditRolesChange (data) {
      // todo
    },
    getFormData () {
      for (var i = 0; i < this.formData.auditNodes.length; i++) {
        var node = this.formData.auditNodes[i]
        node.nodeNo = i
      }
      return deepCopy(this.formData || {})
    }
  },
  mounted () {

  }
}
</script>

<style scoped>

</style>
