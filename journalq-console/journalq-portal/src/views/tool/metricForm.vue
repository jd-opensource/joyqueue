<template>
  <d-form  ref="form" :model="formData" :rules="rules"  label-width="100px" style="height: 350px; overflow-y:auto; width: 100%; padding-right: 20px">
    <d-form-item label="代码:" prop="code" >
      <d-input  v-model="formData.code" placeholder="如app.connections" style="width: 60%" ></d-input>
    </d-form-item>
    <d-form-item label="别名:" prop="aliasCode" >
      <d-input  v-model="formData.aliasCode" placeholder="connection" style="width: 60%" ></d-input>
    </d-form-item>
    <d-form-item label="名称:" prop="name" >
      <d-input  v-model="formData.name" placeholder="如app连接数"  style="width: 60%"></d-input>
    </d-form-item>
    <d-form-item label="类型:" prop="type" >
      <d-select v-model="formData.type" placeholder="类型" value=0  style="width: 60%">
        <d-option value=1>原子</d-option>
        <d-option value=2>聚集</d-option>
      </d-select>
    </d-form-item>
    <d-form-item label="来源:" prop="source"  >
      <d-input style="width: 60%" v-model="formData.source" placeholder="如agent.collector"></d-input>
    </d-form-item>
    <d-form-item label="描述:"  >
      <d-input style="width: 60%" v-model="formData.description" placeholder="描述"></d-input>
    </d-form-item>


  </d-form>

</template>

<script>
import form from '../../mixins/form.js'
export default {
  name: 'metric-form',
  mixins: [ form ],
  props: {
    type: 0, // add or edit form
    data: {
      type: Object,
      default: function () {
        return {
          code: '',
          aliasCode:'',
          name: '',
          type: 0,
          source: '',
          aggregator: '',
          groupField: '',
          groupFieldArray: [],
          summaryLevel: ''
        }
      }
    }
  },
  methods: {
    getFormData () {
      if (this.formData.groupFieldArray === undefined || this.formData.groupFieldArray === []) {
        this.formData.groupField = ''
      } else {
        this.formData.groupField = this.formData.groupFieldArray.join(',')
      }
      return this.formData
    }
  },
  data () {
    return {
      formData: this.data,
      rules: {
        code: [{required: true, message: '请填写代码', trigger: 'change'}],
        aliasCode: [{required: true, message: '请填写别名', trigger: 'change'}],
        name: [{required: true, message: '请填写名称', trigger: 'change'}],
        type: [{required: true, message: '请选类型', trigger: 'change'}],
        source: [{required: true, message: '请填写来源代码', trigger: 'change'}],
      }
    }
  }
}
</script>

<style scoped>

</style>
