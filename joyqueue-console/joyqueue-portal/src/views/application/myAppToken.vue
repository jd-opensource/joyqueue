<template>
  <div>
    <div class="ml20 mt30">
      <d-button-group>
        <d-button class="button" @click="openDialog('addDialog')">添加
          <icon name="plus-circle" style="margin-left: 3px;"></icon>
        </d-button>
        <d-button type="primary" @click="getList" class="button">刷新
          <icon name="refresh-cw" style="margin-left: 3px;"></icon>
        </d-button>
      </d-button-group>
    </div>
    <my-table :data="tableData" :showPin="showTablePin" :page="page" @on-size-change="handleSizeChange"
              @on-current-change="handleCurrentChange" @on-selection-change="handleSelectionChange"
              @on-edit="edit" @on-del="del">
    </my-table>

    <!--添加-->
    <my-dialog :dialog="addDialog" @on-dialog-confirm="addConfirm()" @on-dialog-cancel="addCancel()">
        <grid-row class="mb10">
          <grid-col :span="4" class="label">有效时间:</grid-col>
          <grid-col :span="1"></grid-col>
          <grid-col :span="16" class="val">
            <div class="block">
              <d-date-picker
                v-model="addData.timeList"
                value-format="timestamp"
                :picker-options="pickerOptions2"
                type="datetimerange"
                range-separator="至"
                start-placeholder="生效时间"
                end-placeholder="失效时间">
              </d-date-picker>
            </div>
          </grid-col>
        </grid-row>
    </my-dialog>
    <!--修改-->
    <my-dialog :dialog="editDialog" @on-dialog-confirm="editConfirm()" @on-dialog-cancel="editCancel()">
        <grid-row class="mb10">
          <grid-col :span="4" class="label">token:</grid-col>
          <grid-col :span="1"/>
          <grid-col :span="16" class="val"><d-input v-model="editData.token" oninput="value = value.trim()" class="change-line-400" placeholder="请输入" disabled/></grid-col>
        </grid-row>
        <grid-row class="mb10">
          <grid-col :span="4" class="label">有效时间:</grid-col>
          <grid-col :span="1"/>
          <grid-col :span="16" class="val">
            <d-date-picker
              v-model="editData.timeList"
              value-format="timestamp"
              :picker-options="pickerOptions2"
              type="datetimerange"
              range-separator="至"
              start-placeholder="生效时间"
              end-placeholder="失效时间">
            </d-date-picker>
          </grid-col>
        </grid-row>
    </my-dialog>
  </div>
</template>

<script>
import {timeStampToString} from '../../utils/dateTimeUtils'
import myTable from '../../components/common/myTable.vue'
import myDialog from '../../components/common/myDialog.vue'
import crud from '../../mixins/crud.js'

export default {
  name: 'application',
  components: {
    myTable,
    myDialog
  },
  mixins: [ crud ],
  props: {
    colData: {
      type: Array,
      default: function () {
        return [
          {
            title: '应用',
            key: 'application.code'
          },
          {
            title: 'Token',
            key: 'token'
          },
          {
            title: '生效时间',
            key: 'effectiveTime',
            formatter (item) {
              return timeStampToString(item.effectiveTime)
            }
          },
          {
            title: '失效时间',
            key: 'expirationTime',
            formatter (item) {
              return timeStampToString(item.expirationTime)
            }
          }
        ]
      }
    }
  },
  data () {
    return {
      urls: {
        search: `/application/${this.$route.query.id}/token/getByApp`,
        add: `/application/${this.$route.query.id}/token/add`,
        edit: `/application/${this.$route.query.id}/token/update`,
        del: `/application/${this.$route.query.id}/token/delete`
      },
      searchData: {
        keyword: ''
      },
      searchRules: {
      },
      tableData: {
        rowData: [],
        colData: this.colData,
        // 常用btn
        btns: [
          {
            txt: '修改',
            method: 'on-edit'
          },
          {
            txt: '删除',
            method: 'on-del'
          }
        ]
      },
      multipleSelection: [],
      addDialog: {
        visible: false,
        title: '添加令牌',
        width: 700,
        showFooter: true
      },
      addData: {
        timeList: [new Date().getTime(), new Date().getTime() + 3600 * 1000 * 24]
      },
      pickerOptions2: {
        shortcuts: [
          {
            text: '一百年',
            onClick (picker) {
              const end = new Date()
              const start = new Date()
              end.setTime(start.getTime() + 3600 * 1000 * 24 * 365 * 100)
              picker.$emit('pick', [start.getTime(), end.getTime()])
            }
          }, {
            text: '十年',
            onClick (picker) {
              const end = new Date()
              const start = new Date()
              end.setTime(start.getTime() + 3600 * 1000 * 24 * 365 * 10)
              picker.$emit('pick', [start.getTime(), end.getTime()])
            }
          }, {
            text: '五年',
            onClick (picker) {
              const end = new Date()
              const start = new Date()
              end.setTime(start.getTime() + 3600 * 1000 * 24 * 365 * 5)
              picker.$emit('pick', [start.getTime(), end.getTime()])
            }
          },
          {
            text: '三年',
            onClick (picker) {
              const end = new Date()
              const start = new Date()
              end.setTime(start.getTime() + 3600 * 1000 * 24 * 365 * 3)
              picker.$emit('pick', [start.getTime(), end.getTime()])
            }
          }, {
            text: '一年',
            onClick (picker) {
              const end = new Date()
              const start = new Date()
              end.setTime(start.getTime() + 3600 * 1000 * 24 * 365)
              picker.$emit('pick', [start.getTime(), end.getTime()])
            }
          }
        ]
      },
      editDialog: {
        visible: false,
        title: '修改令牌',
        width: 700,
        showFooter: true
      },
      editData: { // 每一个vue自定义需要修改的项
        id: '',
        token: '',
        timeList: []
      }
    }
  },
  computed: {
  },
  methods: {
    beforeAdd () {
      return new Promise((resolve, reject) => {
        if (this.addData.timeList == null || this.addData.timeList.length < 2) {
          reject(new Error('时间范围必须选择'))
        } else {
          resolve({
            effectiveTime: this.addData.timeList[0],
            expirationTime: this.addData.timeList[1]
          })
        }
      })
    },
    beforeEditData (item) {
      return {
        id: item.id,
        token: item.token,
        timeList: [item.effectiveTime, item.expirationTime]
      }
    },
    beforeEdit () {
      return new Promise((resolve, reject) => {
        resolve({
          id: this.editData.id,
          token: this.editData.token,
          effectiveTime: this.editData.timeList[0],
          expirationTime: this.editData.timeList[1]
        })
      })
    }
  },
  mounted () {
    let startTime = new Date(new Date().toLocaleDateString()).getTime()
    let endTime = new Date(new Date().toLocaleDateString()).getTime() + (100 * 365 + 24) * 24 * 60 * 60 * 1000 - 1
    this.addData.timeList = [startTime, endTime]
  }
}
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->

<style scoped>
.change-line-400 {
  display:block;
  word-break: break-all;
  word-wrap: break-word;
  width: 400px;
}
.label{text-align: right; line-height: 40px;}
.val{}
</style>
<!--<style>-->
<!--@import "element-ui/lib/theme-chalk/index.css";-->
<!--</style>-->
