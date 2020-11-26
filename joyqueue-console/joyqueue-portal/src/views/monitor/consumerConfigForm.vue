<template>
  <d-form
    ref="form"
    :model="formData"
    :rules="rules"
    label-width="120px"
  >
    <grid-row v-if="tips">
      {{tips}}
    </grid-row>
    <grid-row>
      <grid-col span="6">
        <d-form-item label="消费归档">
          <d-switch :disabled="archiveDisabled" v-model="formData.archive"></d-switch>
        </d-form-item>
      </grid-col>
      <grid-col span="6">
        <d-form-item label="重试服务">
          <d-switch v-model="formData.retry"></d-switch>
        </d-form-item>
      </grid-col>
      <grid-col span="6">
        <d-form-item label="暂停消费">
          <d-switch v-model="formData.paused"></d-switch>
        </d-form-item>
      </grid-col>
      <grid-col span="6">
        <d-form-item label="就近机房消费">
          <d-switch :disabled="nearbyDisabled" v-model="formData.nearBy"></d-switch>
        </d-form-item>
      </grid-col>
    </grid-row>
    <grid-row>
      <grid-col span="8">
        <d-form-item label="延迟消费" prop="delay">
          <d-input placeholder="请输入值,最大延迟1小时" oninput="value = value.trim()" v-model.number="formData.delay">
            <i slot="append">ms</i>
          </d-input>
        </d-form-item>
      </grid-col>
      <grid-col span="8">
        <d-form-item label="并行消费" prop="concurrent" :disabled="$store.getters.isAdmin">
          <d-input
            oninput="value = value.trim()"
            :disabled="concurrentDisabled"
            placeholder="请输入并行数,范围1-50,默认1"
            v-model.number="formData.concurrent"
          />
        </d-form-item>
      </grid-col>
      <grid-col span="8">
        <d-form-item label="应答超时" prop="ackTimeout">
          <d-input
            oninput="value = value.trim()"
            placeholder="请输入值，默认10s"
            v-model.number="formData.ackTimeout"
          >
            <i slot="append">ms</i>
          </d-input>
        </d-form-item>
      </grid-col>
    </grid-row>
    <grid-row>
      <grid-col span="8">
        <d-form-item label="批量大小" prop="batchSize">
          <d-input oninput="value = value.trim()"
            laceholder="请输入批量数，范围1-1000，默认10"
            v-model.number="formData.batchSize"
          />
        </d-form-item>
      </grid-col>
      <grid-col span="8">
        <d-form-item label="重试次数" prop="maxRetrys">
          <d-input placeholder="默认无限制" oninput="value = value.trim()" v-model.number="formData.maxRetrys"/>
        </d-form-item>
      </grid-col>
      <grid-col span="8">
        <d-form-item label="过期时间" prop="expireTime">
          <d-input placeholder="默认3天" oninput="value = value.trim()" v-model.number="formData.expireTime">
            <i slot="append">ms</i>
          </d-input>
        </d-form-item>
      </grid-col>
    </grid-row>
    <grid-row>
      <grid-col span="8">
        <d-form-item label="过滤规则:">
          <d-input type="textarea" rows="1" v-model="formData.filters"
                   placeholder="格式如下:key1:value1, key2:value2"/>
        </d-form-item>
      </grid-col>
    </grid-row>
    <d-form-item label="禁止消费IP" prop="blackList">
      <d-input
        type="textarea"
        rows="3"
        oninput="value = value.trim()"
        v-model="formData.blackList"
        placeholder="请输入要限制的IP，多个IP之间请用英文逗号隔开"
      />
    </d-form-item>
  </d-form>
</template>

<script>
import form from '../../mixins/form.js'
import { deepCopy } from '../../utils/assist.js'
import {ipValidator} from '../../utils/common'

export default {
  name: 'consumer-config-form',
  mixins: [form],
  props: {
    tips: {
      type: String,
      default: undefined
    },
    archiveDisabled: {
      type: Boolean,
      default: false
    },
    nearbyDisabled: {
      type: Boolean,
      default: false
    },
    concurrentDisabled: {
      type: Boolean,
      default: false
    },
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
      if (
        (rule.min && value < rule.min) || (rule.max && value > rule.max)
      ) {
        callback(new Error(rule.hint))
      } else {
        callback()
      }
    }
    let batchSizeValidator = (rule, value, callback) => {
      if (this.$store.getters.isAdmin) {
        if ((rule.min && value < rule.min) || (rule.adminMax && value > rule.adminMax)) {
          callback(new Error(rule.adminHint))
        } else {
          callback()
        }
      }
      numberValidator(rule, value, callback)
    }
    return {
      formData: {},
      rules: {
        ackTimeout: [
          // {pattern: /^[1-9]+[0-9]{1,39}$/, message: '时时间至少为1s', trigger: 'change'}
          {
            validator: numberValidator,
            type: 'number',
            trigger: 'change',
            min: 1000,
            hint: '超时时间至少为1s',
            required: false
          }
        ],
        delay: [
          {
            validator: numberValidator,
            type: 'number',
            trigger: 'change',
            min: 0,
            max: 3600000,
            hint: '延时必需大于等于0，且不超过3600s',
            required: false
          }
        ],
        batchSize: [
          {
            validator: batchSizeValidator,
            type: 'number',
            trigger: 'change',
            min: 1,
            max: 100,
            adminMax: 30000,
            hint: '批量大小范围为1~100',
            adminHint: '批量大小范围为1~30000',
            required: false
          }
        ],
        concurrent: [
          {
            validator: numberValidator,
            type: 'number',
            trigger: 'change',
            max: 50,
            min: 1,
            hint: '并行度1~50',
            required: false
          }
        ],
        maxRetrys: [
          {
            validator: numberValidator,
            type: 'number',
            trigger: 'change',
            max: 999,
            min: 0,
            hint: '重试次数0~999',
            required: false
          }
        ],
        expireTime: [
          {
            validator: numberValidator,
            type: 'number',
            trigger: 'change',
            max: 259200000,
            min: 0,
            hint: '过期时间0~259200000',
            required: false
          }
        ],
        blackList: ipValidator()
      }
    }
  },
  methods: {
    getFormData () {
      let data = deepCopy(this.formData)
      // if (!data.maxRetrysStr) {
      //   data.maxRetrys = 0
      // } else {
      //   data.maxRetrys = parseInt(data.maxRetrysStr)
      // }
      // if (!data.expireTimeStr) {
      //   data.expireTime = 0
      // } else {
      //   data.expireTime = parseInt(data.expireTimeStr)
      // }
      return data
    },
    validate (callback) {
      this.$refs['form'].validate(valid => {
        if (valid) {
          callback && callback()
        } else {
          this.$Message.error('校验失败')
          return false
        }
      })
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
<style scoped>
</style>
