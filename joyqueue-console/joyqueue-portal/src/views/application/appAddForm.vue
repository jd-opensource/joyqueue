<template>
  <d-form ref="form" :model="formData" :rules="rules" label-width="100px">
    <d-form-item label="应用名:" prop="code" :error="error.code" >
      <d-input placeholder="支持英文字母和数字，以英文字母开头，长度在2-20个字符之间" oninput="value = value.trim()" v-model="formData.code"/>
    </d-form-item>
   <d-form-item label="描述:" prop="description">
     <d-input placeholder="应用描述信息" oninput="value = value.trim()" v-model="formData.description"></d-input>
   </d-form-item>
  </d-form>
</template>

<script>
import form from '../../mixins/form.js'
import {getCodeRule2} from '../../utils/common.js'

export default {
  name: 'add-app-form',
  mixins: [ form ],
  props: {
    type: 0, // add or edit form
    data: {
      type: Object,
      default: function () {
        return {
          code: '',
          owner: {
            code: 'admin'
          }
        }
      }
    }
  },
  methods: {
    getFormData () {
      this.formData.name = this.formData.code
      this.formData.system = 0
      return this.formData
    }
  },
  data () {
    return {
      formData: this.data,
      rules: {
        code: getCodeRule2()
      },
      error: {
        code: ''
      }
    }
  }
}
</script>

<style scoped>

</style>
