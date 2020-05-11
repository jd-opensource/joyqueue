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
  </d-form>
</template>

<script>
import form from '../../mixins/form.js'
import {getCodeRule3} from '../../utils/common.js'

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
          group: ''
        }
      }
    }
  },
  methods: {
    getFormData () {
      return this.formData
    }
  },
  data () {
    return {
      formData: this.data,
      password: 'false',
      rules: {
        key: getCodeRule3(),
        value: [{required: true, message: '请填写值', trigger: 'change'}],
        group: getCodeRule3()
      }
    }
  }
}
</script>

<style scoped>

</style>
