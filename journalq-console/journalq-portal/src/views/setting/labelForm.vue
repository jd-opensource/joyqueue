<template>
  <d-form ref="form" :model="formData" :rules="rules" label-width="100px" style="height: 200px; width: 100%; padding-right: 20px">
      <d-form-item label="编码:" prop="key" style="width: 60%">
        <d-input v-model="formData.key" ></d-input>
      </d-form-item>
      <d-form-item label="标签:" prop="value" style="width: 60%">
        <d-input v-model="formData.value"></d-input>
      </d-form-item>
    </d-form>
</template>

<script>
import form from '../../mixins/form.js'
import {getCodeRule2, getNameRule} from '../../utils/common.js'

export default {
  name: 'label-form',
  mixins: [ form ],
  props: {
    type: 0, // add or edit form
    data: {
      type: Object,
      default: function () {
        return {
          id: 0,
          key: '',
          value: '',
          name: '',
          group: ''
        }
      }
    }
  },
  methods: {
    getFormData () {
      this.formData.name = this.formData.value
      this.formData.group = 'label'
      return this.formData
    }
  },
  data () {
    return {
      formData: this.data,
      rules: {
        key: getCodeRule2(),
        value: getNameRule()
      }
    }
  }
}
</script>

<style scoped>

</style>
