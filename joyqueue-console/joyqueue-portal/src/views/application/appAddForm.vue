<template>
  <d-form ref="form" :model="formData" :rules="rules" label-width="100px">
    <d-form-item label="英文名称：" prop="code" :error="error.code" >
      <d-input placeholder="仅支持英文字母大小写和数字，首字母" v-model="formData.code" style="width: 80%"/>
    </d-form-item>
<!--    <d-form-item label="应用负责人：" prop="owner.code">-->
<!--      <d-input placeholder="user code" v-model="formData.owner.code" style="width: 80%"></d-input>-->
<!--    </d-form-item>-->
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
