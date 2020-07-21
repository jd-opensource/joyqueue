<template>
  <d-form ref="form" :model="formData" :rules="rules" label-width="100px" style="height: 350px; width: 100%; padding-right: 20px">
    <d-form-item label="键:" placeholser="支持字母大小写、数字、. _ / -,首字母" prop="key" style="width: 60%">
      <d-input v-model="formData.key" oninput="value = value.trim()" v-if="type==$store.getters.editFormType" disabled></d-input>
      <d-input v-model="formData.key" oninput="value = value.trim()" v-else></d-input>
    </d-form-item>
    <d-form-item label="分组:" placeholser="支持字母大小写、数字、. _ / -,首字母" prop="group" style="width: 60%">
      <d-autocomplete
        class="inline-input"
        v-model="formData.group"
        :fetch-suggestions="querySearch"
        placeholder="请输入内容"
        @select="handleSelect"
        v-if="type==$store.getters.editFormType" disabled
        style="width: 100%"
      ></d-autocomplete>
      <d-autocomplete
        class="inline-input"
        v-model="formData.group"
        :fetch-suggestions="querySearch"
        placeholder="请输入内容"
        @select="handleSelect"
        v-else
        style="width: 100%"
      ></d-autocomplete>
    </d-form-item>
    <d-form-item label="值:" prop="value" style="width: 60%">
      <d-input v-model="formData.value" oninput="value = value.trim()"></d-input>
    </d-form-item>
  </d-form>
</template>

<script>
import form from '../../mixins/form.js'
import {getCodeRule3} from '../../utils/common.js'

export default {
  data () {
    return {
      dataSources: [],
      state1: '',
      formData: this.data,
      password: 'false',
      rules: {
        key: getCodeRule3(),
        value: [{required: true, message: '请填写值', trigger: 'change'}]
      }
    }
  },
  name: 'config-form',
  mixins: [ form ],
  props: {
    type: 0, // add or edit form
    data: {
      type: Object,
      default: function () {
        return {
          id: '',
          key: '',
          value: '',
          group: ''
        }
      }
    }
  },
  methods: {
    querySearch (queryString, cb) {
      var dataSources = this.dataSources
      var results = queryString ? dataSources.filter(this.createFilter(queryString)) : dataSources
      // 调用 callback 返回建议列表的数据
      cb(results)
    },
    createFilter (queryString) {
      return (dataSource) => {
        return (dataSource.value.toLowerCase().indexOf(queryString.toLowerCase()) === 0)
      }
    },
    loadAll () {
      return [
        { 'value': 'all' },
        { 'value': 'override' }
      ]
    },
    handleSelect (item) {
      console.log(item)
    }
  },
  mounted () {
    this.dataSources = this.loadAll()
  }
}
</script>

<style scoped>

</style>
