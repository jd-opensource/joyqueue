<template>
  <d-form ref="form" :model="formData" :rules="rules" label-width="120px">
    <d-form-item label="主题">
      <d-input v-model="formData.topic" disabled oninput="value = value.trim()"></d-input>
    </d-form-item>
    <d-form-item label="应用">
      <d-input v-model="formData.app" disabled oninput="value = value.trim()"></d-input>
    </d-form-item>
    <d-form-item label="消息内容" prop="message">
      <d-input type="textarea" rows="4" v-model="formData.message" placeholder="请输入要发送的消息，每行一条"/>
    </d-form-item>
  </d-form>
</template>

<script>
import form from '../../mixins/form.js'

export default {
  name: 'producer-sendMessage-form',
  mixins: [ form ],
  props: {
  },
  data () {
    return {
      formData: this.data,
      rules: {
        message: [
          {required: true, message: '请输入消息体', trigger: 'change'}
        ]
      }
    }
  },
  methods: {
    getFormData () {
      return this.formData;
    }
  },
  mounted () {
  }
}
</script>

<style scoped>

</style>
