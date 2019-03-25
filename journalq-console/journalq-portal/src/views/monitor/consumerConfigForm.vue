<template>
  <d-form ref="form" :model="formData" :rules="rules" label-width="100px" style="height: 350px; overflow-y:auto; width: 100%; padding-right: 20px">
    <grid-row :gutter=4>
       <grid-col span="6">
         <d-form-item label="消费归档:">
           <d-switch v-model="formData.archive"></d-switch>
         </d-form-item>
       </grid-col>
       <grid-col span="6">
         <d-form-item label="暂停消费:">
           <d-switch v-model="formData.paused"></d-switch>
         </d-form-item>
       </grid-col>
    </grid-row>
    <d-form-item label="延迟消费(ms):" prop="delay" >
      <d-input style="width: 60%" placeholder="请输入值,最大延迟1小时" v-model.number="formData.delay"/>
    </d-form-item>
    <d-form-item label="应答超时(ms):" prop="ackTimeout">
      <d-input  style="width: 60%" placeholder="请输入值，默认10s" v-model.number="formData.ackTimeout"/>
    </d-form-item>

    <d-form-item label="批量大小:" prop="batchSize">
      <d-input style="width: 60%"  placeholder="请输入值，范围1-100，默认10" v-model.number="formData.batchSize"/>
    </d-form-item>
    <d-form-item label="并行消费:">
      <d-input style="width: 60%"  placeholder="请输入值,范围1-50,默认1" v-model.number="formData.concurrent"/>
    </d-form-item>
    <d-form-item label="限制IP消费:">
      <d-input style="width: 60%"  type="textarea" rows="3" v-model="formData.blackList" placeholder="请输入要限制的IP，多个IP之间请用英文逗号隔开"/>
    </d-form-item>
    <grid-row>
      <grid-col span="6">
        <d-form-item label="就近消费:">
          <d-switch v-model="formData.nearBy"></d-switch>
        </d-form-item>
      </grid-col>
      <grid-col span="6">
        <d-form-item label="重试服务:">
          <d-switch v-model="formData.retry"></d-switch>
        </d-form-item>
      </grid-col>
    </grid-row>
    <d-form-item label="过滤规则:">
      <d-input style="width: 60%"  type="textarea" rows="3" v-model="formData.filters" placeholder="格式如下:key1:value1,key2:value2"/>
    </d-form-item>
  </d-form>
</template>

<script>
import form from '../../mixins/form.js'
import Label from '../setting/label'

export default {
  name: 'consumer-config-form',
  components: {Label},
  mixins: [ form ],
  props: {
    type: 0, // add or edit form
    data: {
      type: Object,
      default: function () {
        return {
          nearBy: false,
          single: false,
          paused: false,
          archive: false,
          retry: true,
          ackTimeout: undefined,
          batchSize: undefined,
          maxRetrys: 0,
          maxRetryDelay: 300000,
          retryDelay: 0,
          useExponentialBackOff: 0,
          backOffMultiplier: 0,
          expireTime: 3.0,
          delay: undefined,
          concurrent: 1,
          blackList: ''
        }
      }
    }
  },
  data () {
    let numberValidator = (rule, value, callback) => {
      // console.log(rule);
      if ((rule.min !== undefined && value < rule.min) || (rule.max !== undefined && value > rule.max)) {
        callback(new Error(rule.hint))
      } else {
        callback()
      }
    }
    return {
      formData: this.data,
      rules: {
        ackTimeout:
            [
              {validator: numberValidator, type: 'number', trigger: 'change', min: 1000, hint: '超时时间至少为1s', required: false}
            ],
        delay:
            [
              {validator: numberValidator, type: 'number', trigger: 'change', min: 0, max: 3600000, hint: '延时必需大于等于0，且不超过3600s', required: false}
            ],
        batchSize:
            [
              {validator: numberValidator, type: 'number', trigger: 'change', max: 100, min: 1, hint: '批量大小1~100', required: false}
            ],
        concurrent:
          [
            {validator: numberValidator, type: 'number', trigger: 'change', max: 50, min: 1, hint: '并行度1~50', required: false}
          ]

      }
    }
  },
  methods: {
    getFormData () {
      // this.submitForm()
      return this.formData
    }

  }
}
</script>

<style scoped>

</style>
