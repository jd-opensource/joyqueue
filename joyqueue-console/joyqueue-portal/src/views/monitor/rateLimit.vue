<template>
  <d-form ref="form" :model="formData" :rules="rules">
    <grid-row>
      <grid-col>
        <d-form-item prop="limitTps">
          <d-input v-model.number="formData.limitTps" placeholder="请输入">
            <i slot="prepend" style="width: 30px;">&nbsp;&nbsp;&nbsp;tps:</i>
            <i slot="append" style="width: 30px">次/s</i>
          </d-input>
        </d-form-item>
      </grid-col>
    </grid-row>
    <grid-row>
      <grid-col>
        <d-form-item prop="limitTraffic">
          <d-input v-model.number="formData.limitTraffic" placeholder="请输入">
            <i slot="prepend" style="width: 30px">traffic:</i>
            <i slot="append" style="width: 30px">byte/s</i>
          </d-input>
        </d-form-item>
      </grid-col>
    </grid-row>
  </d-form>
</template>

<script>
import GridRow from "../../components/grid/row";
import GridCol from "../../components/grid/col";
export default {
  name: 'rate-limit',
  components: {GridCol, GridRow},
  props: {
    data: {
      type: Object,
      default: function () {
        return {
          limitTraffic: 0,
          limitTps: 0
        }
      }
    }
  },
  data () {
    const rateLimitValidator = (rule, value, callback) => {
      const pattern= /^\d*$/
      if (!pattern.test(value)) {
        callback(new Error('只允许输入数字'))
      } else if (value <0 || value > 10000000) {
        callback(new Error('数字范围必须在[0,1000_0000]之间'))
      }
      else{
        callback()
      }
    }
    return {
      formData: {},
      rules: {
        limitTraffic:[{validator: rateLimitValidator,trigger:"blur"}],
        limitTps: [{ validator: rateLimitValidator,trigger:"blur"}]
      }
    }
  },
  methods: {
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
  },
  mounted () {
    this.formData = this.data
  }
}
</script>

<style scoped>

</style>
