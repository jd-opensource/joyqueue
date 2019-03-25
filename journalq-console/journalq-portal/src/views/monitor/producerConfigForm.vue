<template>
  <d-form ref="form" :model="formData" :rules="rules" label-width="100px" style="height: 350px; overflow-y:auto; width: 100%; padding-right: 20px">
    <d-form-item label="就近发送：">
      <d-switch v-model="formData.nearBy" ></d-switch>
    </d-form-item>
    <d-form-item label="单线程发送：">
      <d-switch v-model="formData.single"></d-switch>
    </d-form-item>
    <d-form-item label="生产归档:">
      <d-switch v-model="formData.archive"></d-switch>
    </d-form-item>
    <!--<d-form-item label="生产权重：">-->
      <!--<d-input placeholder="请输入权重，格式 brokerId:weight，多个之间请用英文逗号隔开" v-model="formData.weight"/>-->
    <!--</d-form-item>-->
    <d-form-item label="限制IP生产：">
      <d-input type="textarea" rows="4" v-model="formData.blackList" placeholder="请输入要限制的IP，多个IP之间请用英文逗号隔开"/>
    </d-form-item>
  </d-form>
</template>

<script>
import form from '../../mixins/form.js'

export default {
  name: 'producer-config-form',
  mixins: [ form ],
  props: {
    type: 0, // add or edit form
    data: {
      type: Object,
      default: function () {
        return {
          nearBy: false,
          single: false,
          archive: false,
          weight: '',
          blackList: ''
        }
      }
    }
  },
  data () {
    return {
      formData: this.data,
      rules: {

      }
    }
  },
  methods: {
    getFormData () {
      return this.formData
    }
  }
}
</script>

<style scoped>

</style>
