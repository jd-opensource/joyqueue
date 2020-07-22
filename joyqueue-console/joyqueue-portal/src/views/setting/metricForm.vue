<template>
  <d-form  ref="form" :model="formData" :rules="rules"  label-width="120px" style="height: 350px; overflow-y:auto; width: 100%; padding-right: 20px">
    <d-form-item label="代码:" prop="code" :error="error.code" >
      <d-input  v-model="formData.code" oninput="value = value.trim()" :disabled="type" placeholder="如app_slice_pending" style="width: 60%" ></d-input>
    </d-form-item>
    <d-form-item label="简称:" prop="aliasCode" :error="error.aliasCode" >
      <d-input  v-model="formData.aliasCode" oninput="value = value.trim()" placeholder="如backlog" style="width: 60%" ></d-input>
    </d-form-item>
    <d-form-item label="名称:" prop="name" >
      <d-input  v-model="formData.name" oninput="value = value.trim()" placeholder="如积压"  style="width: 60%"></d-input>
    </d-form-item>
    <d-form-item label="类型:" prop="type" >
      <d-select v-model.number="formData.type" oninput="value = value.trim()" placeholder="类型" style="width: 60%">
        <d-option :value=1>原子</d-option>
        <d-option :value=2>聚集</d-option>
      </d-select>
    </d-form-item>
    <d-form-item label="来源:" prop="source"  >
      <d-input style="width: 60%" v-model="formData.source" oninput="value = value.trim()" placeholder="指标来源"></d-input>
    </d-form-item>
    <d-form-item label="提供方:" prop="provider"  >
      <d-input style="width: 60%" v-model="formData.provider" oninput="value = value.trim()" placeholder="指标提供方"></d-input>
    </d-form-item>
    <d-form-item label="用户是否开放权限:"  >
      <d-switch v-model="formData.userPermission"></d-switch>
    </d-form-item>
    <d-form-item label="种类" prop="category">
      <d-input v-model="formData.category" style="width: 60%" oninput="value = value.trim()" placeholder="指标所属者种类(producer,consumer,broker...)"/>
    </d-form-item>
    <d-form-item label="采集间隔" prop="collectInterval">
      <d-input v-model="formData.collectInterval" style="width: 60%" oninput="value=value.replace(/[^\d]/g, '')" placeholder="采集间隔"/>
    </d-form-item>
    <d-form-item label="描述:"  >
      <d-input style="width: 60%" v-model="formData.description" oninput="value = value.trim()" placeholder="描述"></d-input>
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
          aliasCode: '',
          name: '',
          type: 1,
          source: '',
          aggregator: '',
          groupField: '',
          groupFieldArray: [],
          summaryLevel: '',
          userPermission: false,
          collectInterval: 1,
          category: ''
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
        aliasCode: [{required: true, message: '请输入别名，尽量缩写', trigger: 'change'}],
        name: [{required: true, message: '请输入名称', trigger: 'change'}],
        type: [{required: true, message: '请选择类型', trigger: 'change'}],
        source: [{required: true, message: '请输入来源代码', trigger: 'change'}],
        provider: [{required: true, message: '请输入指标提供方', trigger: 'change'}]
      },
      error: {
        code: '',
        aliasCode: ''
      }
    }
  }
}
</script>

<style scoped>

</style>
