<template>
  <div class="form">
    <d-form ref="form" :model="formData" :rules="rules" label-width="70px" inline label-position="left"
            style="height:460px; overflow-y:auto; width:100%">
      <div class="top">
        <d-form-item label="消费归档:" style="width:30%">
          <d-switch v-model="formData.archive"></d-switch>
        </d-form-item>
        <d-form-item label="重试服务:" style="width:30%">
          <d-switch v-model="formData.retry"></d-switch>
        </d-form-item>
        <d-form-item label="暂停消费:" style="width:30%">
          <d-switch v-model="formData.paused"></d-switch>
        </d-form-item>
      </div>
      <label class="gray_line"></label>
      <span>规则配置:</span>
      <div class="rule">
        <d-form-item label="就近机房消费:" style="width:100%" label-width="90px">
          <d-switch v-model="formData.nearBy"></d-switch>
        </d-form-item>
        <d-form-item label="延迟消费(ms):" prop="delay" style="width:46%" label-width="90px">
          <d-input placeholder="请输入值,最大延迟1小时" v-model.number="formData.delay" style="width:130%"/>
        </d-form-item>
        <d-form-item label="并行消费:" prop="concurrent" style="width:46%; padding-left:35px">
          <d-input placeholder="请输入并行数,范围1-50,默认1" v-model.number="formData.concurrent" style="width:130%"/>
        </d-form-item>
        <d-form-item label="禁止消费IP:" style="width:100%" label-width="90px" >
          <d-input type="textarea" rows="3" v-model="formData.blackList" style="width:330%"
                   placeholder="请输入要限制的IP，多个IP之间请用英文逗号隔开"/>
        </d-form-item>
        <label class="gray_line2"></label>
        <div class="bottom">
          <d-form-item label="应答超时(ms):" prop="ackTimeout" style="width:46%" label-width="90px">
            <d-input placeholder="请输入值，默认10s" v-model.number="formData.ackTimeout" style="width:130%"/>
          </d-form-item>
          <d-form-item label="批量大小:" prop="batchSize" style="width:46%; padding-left:35px">
            <d-input laceholder="请输入批量数，范围1-100，默认10" v-model.number="formData.batchSize" style="width:130%"/>
          </d-form-item>
          <d-form-item label="过滤规则:" style="width:100%" label-width="90px">
            <d-input type="textarea" rows="1" v-model="formData.filters" style="width:330%"
                     placeholder="格式如下:key1:value1, key2:value2"/>
          </d-form-item>
        </div>
        <label class="gray_line"></label>
          <div class="bottom">
              <d-form-item label="重试次数:" prop="maxRetrys" style="width:46%" label-width="90px">
                <d-input placeholder="默认无限制" v-model="formData.maxRetrysStr" style="width:130%"/>
              </d-form-item>
              <d-form-item label="过期时间:" prop="expireTime" label-width="115px">
                <d-input placeholder="单位ms，默认3天" v-model="formData.expireTimeStr" style="width:130%"/>
              </d-form-item>
          </div>
      </div>
    </d-form>
  </div>
</template>

<script>
import form from '../../mixins/form.js'
import {deepCopy} from '../../utils/assist.js'

export default {
  name: 'consumer-config-form',
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
      console.log(22)
      console.log(rule)
      if ((rule.min !== undefined && value < rule.min) || (rule.max !== undefined && value > rule.max)) {
        callback(new Error(rule.hint))
      } else {
        callback()
      }
    }
    return {
      formData: {},
      rules: {
        ackTimeout:
            [
              // {pattern: /^[1-9]+[0-9]{1,39}$/, message: '时时间至少为1s', trigger: 'change'}
              {validator: numberValidator, type: 'number', trigger: 'change', min: 1000, hint: '超时时间至少为1s', required: false}
            ],
        delay: [
          {validator: numberValidator, type: 'number', trigger: 'change', min: 0, max: 3600000, hint: '延时必需大于等于0，且不超过3600s', required: false}
        ],
        batchSize:
            [
              {validator: numberValidator, type: 'number', trigger: 'change', max: 100, min: 1, hint: '批量大小1~100', required: false}
            ],
        concurrent: [
          {validator: numberValidator, type: 'number', trigger: 'change', max: 50, min: 1, hint: '并行度1~50', required: false}
        ],
        maxRetrys:
          [
            {validator: numberValidator, type: 'number', trigger: 'change', max: 999, min: 0, hint: '重试次数0~999', required: false}
          ],
        expireTime:
          [
            {validator: numberValidator, type: 'number', trigger: 'change', max: 259200000, min: 0, hint: '过期时间', required: false}
          ]
      }
    }
  },
  methods: {
    getFormData () {
      let data = deepCopy(this.formData)
      if (!data.maxRetrysStr) {
        data.maxRetrys = 0
      } else {
        data.maxRetrys = parseInt(data.maxRetrysStr)
      }
      if (!data.expireTimeStr) {
        data.expireTime = 0
      } else {
        data.expireTime = parseInt(data.expireTimeStr)
      }
      return data
    }
  },
  mounted () {
    this.formData = this.data

    if (!this.data.maxRetrys) {
      this.formData.maxRetrysStr = ''
    } else {
      this.formData.maxRetrysStr = this.data.maxRetrys.toString()
    }

    if (!this.formData.maxRetrys) {
      this.formData.expireTimeStr = ''
    } else {
      this.formData.expireTimeStr = this.data.expireTime.toString()
    }
  }
}
</script>

<style lang="scss" scoped>
  .form {
    & .top {
      padding-left: 20px;
      padding-right:20px
    }

    & .gray_line {
      background: #e5e5e5;
      width: 100%;
      height: 1px;
      float: right;
      margin-bottom: 10px
    }

    & .rule {
      margin-top: 10px;
      padding-left: 20px;
      padding-right: 20px;

      & .gray_line2 {
        background: #e5e5e5;
        width: 100%;
        height: 1px;
        float: right;
        margin-bottom: 20px
      }

      & .bottom {
        margin-top:10px;
      }
    }
  }
</style>
