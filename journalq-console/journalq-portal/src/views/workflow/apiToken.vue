<template>
  <div>
    <div class="ml20 mt30">
      <d-form ref="form" :model="formData" :rules="rules" label-width="100px">
        <d-form-item label="用户名:">
          <grid-col :span="2" class="label">
            <span>{{formData.code}}</span>
          </grid-col>
        </d-form-item>
        <d-form-item label="令牌:" v-if="formData.token!=null">
          <span>{{formData.token}}</span>
        </d-form-item>
        <d-form-item label="有效期限:" v-if="formData.token!=null">
          <span>{{timestampToStr(formData.expireTime,false,false)}}</span>
        </d-form-item>
        <d-form-item label="有效期:">
          <d-select v-model="months" v-if="$store.getters.isAdmin" class="left" style="width:200px"
                    placeholder="请选择有效期">
            <d-option value="6" >六个月</d-option>
            <d-option value="12">十二个月</d-option>
            <d-option value="36">三年</d-option>
          </d-select>
        </d-form-item>
         <div class="ml20 mt30" style="margin-top: 20px;margin-left: 20px">
            <d-button   @click="invalidToken()" class="left mr10">过期</d-button>
            <d-button type="primary" @click="updateToken()" class="left mr10">更新</d-button>
         </div>
      </d-form>
    </div>
  </div>

</template>

<script>
import store from '../../store'
import crud from '../../mixins/crud.js'
import apiRequest from '../../utils/apiRequest.js'
import {timeStampToString} from '../../utils/dateTimeUtils'
export default {
  name: 'api-token',
  mixins: [crud],
  props: {
    type: 0, // add or edit form
    data: {
      type: Object,
      default: function () {
        return {
          code: store.getters.loginUserName,
          token: '',
          expireTime: null
        }
      }
    }
  },
  data () {
    return {
      formData: this.data,
      urls: {
        search: `/user/userToken`,
        update: `/user/updateToken`,
        invalid: `/user/invalidToken`
      },
      rules: {
      },
      months: '12'
    }
  },
  methods: {
    getList () {
      apiRequest.get(this.urls.search + '/' + this.formData.code, {}).then((data) => {
        data.data = data.data || {}
        this.formData = data.data
        // this.page.total = this.tableData.rowData.length
        // this.showTablePin = false
      })
    },
    addMonth (date, num) {
      num = parseInt(num)
      let sDate = date
      let sYear = sDate.getFullYear()
      let sMonth = sDate.getMonth() + 1
      let sDay = sDate.getDate()
      let eYear = sYear
      let eMonth = sMonth + num
      let eDay = sDay
      while (eMonth > 12) {
        eYear++
        eMonth -= 12
      }
      let eDate = new Date(eYear, eMonth - 1, eDay)
      while (eDate.getMonth() != eMonth - 1) {
        eDay--
        eDate = new Date(eYear, eMonth - 1, eDay)
      }
      return eDate
    },
    timestampToStr (timestamp) {
      return timeStampToString(timestamp)
    },
    invalidToken () {
      apiRequest.put(this.urls.invalid + '/' + this.formData.code, {}).then((data) => {
        data.data = data.data || {}
        if (data.data === 'success') {
          this.$Message.success('更新成功')
          this.getList()
        } else {
          this.$Message.error('更新失败')
        }
      })
    },
    updateToken () {
      let months = parseInt(this.months)
      let date = new Date()
      date = this.addMonth(date, months)// increment days
      let invalidTime = date.getTime()
      let data = {
        code: store.getters.loginUserName,
        expireTime: invalidTime
      }
      apiRequest.postBase(this.urls.update, {}, data, false).then((data) => {
        data.data = data.data || {}
        if (data.data === 'success') {
          this.$Message.success('更新成功')
          this.getList()
        } else {
          this.$Message.error('更新失败')
        }
      })
    }

  },
  mounted () {
    this.getList()
  }
}
</script>

<style scoped>

</style>
