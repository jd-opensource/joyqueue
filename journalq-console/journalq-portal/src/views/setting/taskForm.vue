<template>
  <d-form :model="formData" :rules="rules" ref="form" label-width="100px" style="height: 350px; overflow-y:auto; width: 100%; padding-right: 20px">
    <d-form-item label="类型:" prop="type" style="width: 60%">
      <d-input style="width: 249px" v-model="formData.type" placeholder="由字母和点组成，如Topic.Update"></d-input>
    </d-form-item>
    <d-form-item label="互斥类型:" style="width: 60%">
      <d-input style="width: 249px" v-model="formData.mutex" placeholder="互斥任务类型,支持后缀*，如Topic.*"></d-input>
    </d-form-item>
    <d-form-item label="关联主键:" prop="referId" style="width: 60%">
      <d-input style="width: 249px" v-model="formData.referId"></d-input>
    </d-form-item>
    <d-form-item label="优先级:" prop="priority" style="width: 60%">
      <d-input style="width: 249px" v-model="formData.priority"></d-input>
    </d-form-item>
    <d-form-item label="守护任务:" prop="daemons">
      <d-radio-group v-model="formData.daemons">
        <d-radio :label="true">是</d-radio>
        <d-radio :label="false">否</d-radio>
        <span v-if="formData.daemons" style="color:red;">守护任务默认重试</span>
      </d-radio-group>
    </d-form-item>
    <d-form-item label="参数:" prop="url" style="width: 60%">
      <d-input style="width: 249px" v-model="formData.url" placeholder="任务的参数，如?a=1&b=2"></d-input>
    </d-form-item>
    <d-form-item label="表达式:" prop="cron" style="width: 60%">
      <d-input style="width: 249px" v-model="formData.cron" placeholder="cron表达式，如 0 5 0/1 * * ?"></d-input>
    </d-form-item>
    <d-form-item label="派发类型:" prop="dispatchType">
      <d-radio-group v-model="formData.dispatchType">
        <d-radio :label="0">任意执行器</d-radio>
        <d-radio :label="1">原有执行器优先 </d-radio>
        <d-radio :label="2">必须原有执行器</d-radio>
      </d-radio-group>
    </d-form-item>
    <d-form-item label="重试:" prop="retry">
      <d-radio-group v-model="formData.retry">
        <d-radio :label="true">是</d-radio>
        <d-radio :label="false">否</d-radio>
      </d-radio-group>
    </d-form-item>
  </d-form>
</template>

<script>
import form from '../../mixins/form.js'

export default {
  name: 'task-form',
  mixins: [ form ],
  props: {
    type: 0, // add or edit form
    data: {
      type: Object,
      default: function () {
        return {
          type: '',
          mutex: '',
          referId: 0,
          priority: 0,
          daemons: false,
          url: '',
          cron: '',
          dispatchType: 0,
          retry: true,
          status: 1
        }
      }
    }
  },
  data () {
    return {
      formData: this.data,
      rules: {
        type: [{required: true, message: '请填写类型', trigger: 'change'}],
        referId: [{required: true, message: '请填写关联id', trigger: 'change'}],
        priority: [{required: true, message: '请填优先级', trigger: 'change'}],
        daemons: [{required: true, message: '请填写守护线程', trigger: 'change'}],
        // url: [{required: true, message: '请填写url', trigger: 'change'}],
        // cron: [{required: true, message: '请填写cron表达式', trigger: 'change'}],
        dispatchType: [{required: true, message: '请填写派发类型', trigger: 'change'}],
        retry: [{required: true, message: '请填写重试类型', trigger: 'change'}]
      }
    }
  }
}
</script>

<style scoped>

</style>
