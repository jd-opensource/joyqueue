<template>
  <d-form ref="form" :model="formData" :rules="rules" label-width="100px" style="height: 350px; overflow-y:auto; width: 100%; padding-right: 20px">
        <d-form-item label="编码:" prop="key" style="width: 60%">
          <d-input v-model="formData.code" ></d-input>
        </d-form-item>
        <d-form-item label="名称:" prop="name" style="width: 60%">
          <d-input v-model="formData.name"></d-input>
        </d-form-item>
        <d-form-item label="指标:" prop="metric" style="width: 60%">
          <d-input v-model="formData.metric"></d-input>
        </d-form-item>
        <d-form-item label="报警对象:" prop="userType">
          <d-radio-group v-model="formData.userType">
          <d-radio label="ALL">全部</d-radio>
          <d-radio label="ADMIN">管理员</d-radio>
          <d-radio label="USER">普通用户</d-radio>
        </d-radio-group>
        </d-form-item>
          <d-form-item label="接入方:" prop="endPoint">
          <d-radio-group v-model="formData.endPoint">
          <d-radio label="PRODUCER">生产者</d-radio>
          <d-radio label="CONSUMER">消费者</d-radio>
        </d-radio-group>
        </d-form-item>
          <d-form-item label="建议:" prop="suggestion" style="width: 60%">
          <d-input v-model="formData.suggestion"></d-input>
        </d-form-item>
        <d-form-item label="恢复建议:" prop="resumeSuggestion" style="width: 60%">
          <d-input v-model="formData.resumeSuggestion"></d-input>
        </d-form-item>
        <d-form-item label="链接:" prop="url" style="width: 60%">
          <d-input v-model="formData.url"></d-input>
        </d-form-item>
        <d-form-item label="备注:" prop="description" style="width: 60%">
          <d-input v-model="formData.description"></d-input>
        </d-form-item>
      </d-form>
  <!--<d-form ref="form" :model="formData" :rules="rules" label-width="100px" style="height: 350px; overflow-y:auto; width: 100%; padding-right: 20px">-->
      <!--<d-form-item label="代码:" prop="code">-->
        <!--<d-input v-model="data.code"/>-->
      <!--</d-form-item>-->
      <!--<d-form-item label="名称:" prop="name">-->
        <!--<d-input v-model="data.name"></d-input>-->
      <!--</d-form-item>-->
      <!--<d-form-item label="指标:" prop="metric">-->
        <!--<d-input v-model="data.metric"></d-input>-->
      <!--</d-form-item>-->
      <!--<d-form-item label="报警对象:" prop="userType">-->
        <!--<d-select v-model="data.userType" style="width: 52%">-->
          <!--<d-option value="ALL" >全部</d-option>-->
          <!--<d-option value="ADMIN" >管理员</d-option>-->
          <!--<d-option value="USER" >普通用户</d-option>-->
        <!--</d-select>-->
      <!--</d-form-item>-->
      <!--<d-form-item label="接入方:" prop="endpoint">-->
        <!--<d-select v-model="data.endpoint" style="width: 52%">-->
          <!--<d-option value="PRODUCER" >生产者</d-option>-->
          <!--<d-option value="CONSUMER" >消费者</d-option>-->
        <!--</d-select>-->
      <!--</d-form-item>-->
      <!--<d-form-item label="建议:" prop="suggestion">-->
        <!--<d-input v-model="data.suggestion"></d-input>-->
      <!--</d-form-item>-->
      <!--<d-form-item label="恢复建议:" prop="resumeSuggestion">-->
        <!--<d-input v-model="data.resumeSuggestion"></d-input>-->
      <!--</d-form-item>-->
      <!--<d-form-item label="链接:" prop="url">-->
        <!--<d-input v-model="data.url"></d-input>-->
      <!--</d-form-item>-->
      <!--<d-form-item label="备注:" prop="description">-->
        <!--<d-input v-model="data.description"></d-input>-->
      <!--</d-form-item>-->
  <!--</d-form>-->
</template>

<script>
import form from '../../mixins/form.js'

export default {
  name: 'alarm-type-form',
  mixins: [ form ],
  props: {
    type: 0, // add or edit form
    data: {
      type: Object,
      default: function () {
        return {
          code: '',
          name: '',
          metric: '',
          userType: '',
          endpoint: 'PRODUCER',
          suggestion: '',
          resumeSuggestion: '',
          url: '',
          description: ''
        }
      }
    }
  },
  data () {
    return {
      formData: this.data,
      rules: {
        code: [{required: true, message: '请选择代码', trigger: 'change'}],
        name: [{required: true, message: '请选择名称', trigger: 'change'}],
        userType: [{required: true, message: '请选择报警对象', trigger: 'change'}]
      }
    }
  }
}
</script>

<style scoped>

</style>
