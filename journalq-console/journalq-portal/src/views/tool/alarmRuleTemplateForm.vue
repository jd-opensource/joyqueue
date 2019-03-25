<template>
  <d-form ref="form" :model="formData" :rules="rules" label-width="100px" style="height: 350px; overflow-y:auto; width: 100%; padding-right: 20px">
    <d-form-item label="指标名称" prop="metricCode">
      <d-input v-model="formData.metricCode"  style="width: 50%"></d-input>
    </d-form-item>
    <d-form-item label="报警级别:" prop="alarmLevel">
      <d-select v-model="formData.alarmLevel" style="width: 50%">
        <d-option value=2>严重</d-option>
        <d-option value=1>次要</d-option>
        <d-option value=0>警告</d-option>
      </d-select>
    </d-form-item>
    <d-form-item label="报警对象:" prop="alarmUsers">
      <d-select v-model="formData.alarmUsers" style="width: 50%">
        <d-option value="ALL">全部</d-option>
        <d-option value="ADMIN">管理员</d-option>
        <d-option value="USER">普通用户</d-option>
      </d-select>
    </d-form-item>
    <d-form-item label="报警方式:" prop="alarmModeArray">
      <d-checkbox-group v-model="formData.alarmModeArray" name="Checkboxgroup">
        <d-checkbox label="email">EMAIL</d-checkbox>
        <d-checkbox label="message">MESSAGE</d-checkbox>
        <d-checkbox label="phone">PHONE</d-checkbox>
        <d-checkbox label="timline">TIMLINE</d-checkbox>
      </d-checkbox-group>
    </d-form-item>
    <d-form-item label="主题">
      <d-input v-model="formData.topic"  style="width: 50%"></d-input>
    </d-form-item>
    <d-form-item label="应用">
      <d-input v-model="formData.app"  style="width: 50%"></d-input>
    </d-form-item>
    <d-form-item label="主机" >
      <d-input v-model="formData.hosts" style="width: 50%"></d-input>
    </d-form-item>
    <d-form-item label="阈值">
      <d-input v-model="formData.threshold" style="width: 50%"></d-input>
    </d-form-item>
    <d-form-item label="检测时间">
      <d-input v-model="formData.detectDuration"  style="width: 50%"></d-input>
    </d-form-item>
    <d-form-item label="检测次数">
      <d-input v-model="formData.thresholdCount"  style="width: 50%"></d-input>
    </d-form-item>
    <d-form-item label="报警间隔">
      <d-select v-model="formData.alarmInterval" value="0"  style="width: 50%">
        <d-option v-for="item in intervalList" :value="item.value" :key="item.value">{{ item.label }}</d-option>
      </d-select>
    </d-form-item>
  </d-form>
</template>

<script>
import form from '../../mixins/form.js'
import { deepCopy } from '../../utils/assist.js'

export default {
  name: 'alarm-rule-default-form',
  mixins: [ form ],
  props: {
    type: 0, // add or edit form
    data: {
      type: Object,
      default: function () {
        return {
          alarmLevel: 'ALL',
          userType: 'ALL',
          alarmMode: '',
          app: '',
          topic: '',
          hosts: '',
          broker: '',
          data: 0.0,
          alarmInterval: 0,
          detectDuration:0,
          detectTimes: 0,
          effectiveTime: '',
          expirationTime: '',
          noticeProducer: false,
          status: 1
        }
      }
    }
  },
  data () {
    return {
      formData: {},
      typeList: [
        {
          value: 'backlog',
          label: 'backlog'
        },
        // {
        //   value: 'connection',
        //   label: 'connection'
        // }
        //todo 待完善
      ],
      intervalList: [
        {
          value: 0,
          label: '10分钟'
        },
        {
          value: 30,
          label: '30分钟'
        },
        {
          value: 60,
          label: '1个小时'
        },
        {
          value: 120,
          label: '2个小时'
        },
        {
          value: 180,
          label: '3个小时'
        },
        {
          value: 240,
          label: '4个小时'
        },
        {
          value: 300,
          label: '5个小时'
        },
        {
          value: 360,
          label: '6个小时'
        },
        {
          value: 720,
          label: '12个小时'
        }
      ],
      rules: {
        alarmLevel: [
          { required: true, message: '请选择报警级别', trigger: 'change' }
        ],
        userType: [
          { required: true, message: '请选择报警对象', trigger: 'change' }
        ],
        alarmModeArray: [
          { type: 'array', required: true, message: '请至少选择一个报警方式', trigger: 'change' }
        ],
        app: [
          { required: true, min: 1, message: '请输入应用', trigger: 'change' }
        ],
        topic: [
          { required: true, min: 1, message: '请输入主题', trigger: 'change' }
        ],
        hosts: [
          { required: true, message: '请输入主机', trigger: 'blur' },
          { min: 5, max: 20, message: '长度在 5 到 20 个字符', trigger: 'blur' }
        ],
        broker: [
          { required: true, message: '请输入broker', trigger: 'blur' },
          { min: 1, max: 200, message: '长度200 个字符以内', trigger: 'blur' }
        ],
        threshold: [
          { type: 'number', required: true, message: '请输入阈值', trigger: 'blur' }
        ],
        alarmInterval: [
          { type: 'number', required: true, message: '请选择报警间隔', trigger: 'blur' }
        ],
        thresholdCount: [
          { type: 'number', required: true, message: '请选择检测次数', trigger: 'blur' }
        ]
      }
    }
  },
  methods: {
    getFormData () {
      if (this.formData.alarmModeArray === undefined || this.formData.alarmModeArray === []) {
        this.formData.alarmWays = ''
      } else {
        this.formData.alarmWays = this.formData.alarmModeArray.join(',')
      }
      return this.formData
    }
  },
  mounted () {
    let newData = deepCopy(this.data)
    if (newData.alarmWays === undefined || newData.alarmWays === '') {
      newData.alarmModeArray = []
    } else {
      newData.alarmModeArray = newData.alarmWays.split(',')
    }
    this.formData = newData
  }
}
</script>

<style scoped>

</style>
