<template>
  <d-form ref="form" :model="formData" :rules="rules" label-width="120px">
    <grid-row>
      <grid-col span="12">
        <d-form-item label="就近机房发送">
          <d-switch v-model="formData.nearBy" :disabled="!nearbyConfigEnabled"></d-switch>
        </d-form-item>
      </grid-col>
      <grid-col span="12">
        <d-form-item label="生产归档" >
          <d-switch v-model="formData.archive" :disabled="!archiveConfigEnabled"></d-switch>
        </d-form-item>
      </grid-col>
    </grid-row>
    <d-form-item label="限制IP生产" prop="blackList">
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
    archiveConfigEnabled: {
      type: Boolean,
      default: false
    },
    nearbyConfigEnabled: {
      type: Boolean,
      default: false
    },
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
    let ipPattern = '(\\d{1,2}|1\\d\\d|2[0-4]\\d|25[0-5])\\.(\\d{1,2}|1\\d\\d|2[0-4]\\d|25[0-5])\\.(\\d{1,2}|1\\d\\d|2[0-4]\\d|25[0-5])\\.(\\d{1,2}|1\\d\\d|2[0-4]\\d|25[0-5])'
    return {
      formData: this.data,
      rules: {
        blackList: [
          {
            pattern: '^' + ipPattern + '(,' + ipPattern + ')*$',
            message: '请输入正确格式的ip',
            required: false,
            trigger: 'blur'}
        ]
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
