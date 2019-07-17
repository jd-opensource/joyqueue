<template>
  <d-form ref="form" :model="formData" :rules="rules" label-width="100px" style="height: 350px; width: 100%; padding-right: 20px">
    <d-form-item label="键:" placeholser="支持字母大小写、数字、. _ / -,首字母" prop="key" style="width: 60%">
      <d-input v-model="formData.key" v-if="type==$store.getters.editFormType" disabled></d-input>
      <d-input v-model="formData.key" v-else></d-input>
    </d-form-item>
    <d-form-item label="分组:" placeholser="支持字母大小写、数字、. _ / -,首字母" prop="group" style="width: 60%">
      <d-input v-model="formData.group" v-if="type==$store.getters.editFormType" disabled></d-input>
      <d-input v-model="formData.group" v-else></d-input>
    </d-form-item>
    <d-form-item label="值:" prop="value" style="width: 60%">
      <d-input v-model="formData.value"></d-input>
    </d-form-item>
    <d-form-item label="是否为密码:" prop="password">
      <d-radio-group v-model="password" name="radiogroup">
        <d-radio label="true">是</d-radio>
        <d-radio label="false">否</d-radio>
      </d-radio-group>
    </d-form-item>
  </d-form>
</template>

<script>
import form from '../../mixins/form.js'
import {getCodeRule3, getNameRule} from '../../utils/common.js'

export default {
  name: 'config-form',
  mixins: [ form ],
  props: {
    type: 0, // add or edit form
    data: {
      type: Object,
      default: function () {
        return {
          id: '',
          key: '',
          value: '',
          name: '',
          group: '',
          password: 0
        }
      }
    }
  },
  methods: {
    getFormData () {
      this.formData.password = (this.password === 'true' ? 1 : 0)
      return this.formData
    }
  },
  data () {
    return {
      formData: this.data,
      password: 'false',
      rules: {
        key: getCodeRule3(),
        name: getNameRule(),
        value: [{required: true, message: '请填写值', trigger: 'change'}]
      }
    }
  },
  mounted () {
    this.password = this.data.password === 1 ? 'true' : 'false'
  }
}
</script>

<style scoped>

</style>
