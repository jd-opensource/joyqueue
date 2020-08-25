<template>
  <div>
    <div class="ml20 mt30">
      <d-input v-model="searchData.keyword" oninput="value = value.trim()" placeholder="请输入编码/名称" class="left mr10"
               style="width:300px" @on-enter="getList">
        <span slot="prepend">关键词</span>
        <icon name="search" size="14" color="#CACACA" slot="suffix" @click="getList"></icon>
      </d-input>
      <d-button type="primary" @click="openDialog('addDialog')">新建Broker分组<icon name="plus-circle" style="margin-left: 5px;"></icon></d-button>
    </div>
    <my-table :data="tableData" :showPin="showTablePin" :page="page" @on-size-change="handleSizeChange" @on-current-change="handleCurrentChange"
              @on-selection-change="handleSelectionChange" @on-detail="goDetail" @on-add-broker="addBroker" @on-del="del" @on-edit="edit">
    </my-table>

    <!--新建分组-->
    <my-dialog :dialog="addDialog" @on-dialog-confirm="submit()" @on-dialog-cancel="addCancel()">
      <grid-row class="mb10">
        <grid-col :span="5" class="label">编码:</grid-col>
        <grid-col :span="1"/>
        <grid-col :span="14" class="val">
          <d-input v-model="addData.code" oninput="value = value.trim()"></d-input>
        </grid-col>
      </grid-row>
      <grid-row class="mb10">
        <grid-col :span="5" class="label">名称:</grid-col>
        <grid-col :span="1"/>
        <grid-col :span="14" class="val">
          <d-input v-model="addData.name" oninput="value = value.trim()"></d-input>
        </grid-col>
      </grid-row>
      <grid-row class="mb10">
        <grid-col :span="5" class="label">存储最长时间:</grid-col>
        <grid-col :span="1"/>
        <grid-col :span="14" class="val">
          <d-input v-model.number="addData.storeMaxTime" oninput="value=value.replace(/[^\d]/g, '')"></d-input>
        </grid-col>
      </grid-row>
      <grid-row class="mb10">
        <grid-col :span="5" class="label">保留未消费数据:</grid-col>
        <grid-col :span="1"/>
        <grid-col :span="14" class="val">
          <d-radio-group v-model="addData.storeCleanKeepUnconsumed">
            <d-radio :label="true">是</d-radio>
            <d-radio :label="false">否</d-radio>
          </d-radio-group>
        </grid-col>
      </grid-row>
      <grid-row class="mb10">
        <grid-col :span="5" class="label">仅管理员:</grid-col>
        <grid-col :span="1"/>
        <grid-col :span="14" class="val">
          <d-radio-group v-model="addData.status">
            <d-radio :label="2">是</d-radio>
            <d-radio :label="1">否</d-radio>
          </d-radio-group>
        </grid-col>
      </grid-row>
    </my-dialog>
    <my-dialog :dialog="editDialog" @on-dialog-confirm="editSubmit()" @on-dialog-cancel="editCancel()">
      <grid-row class="mb10">
        <grid-col :span="5" class="label">编码:</grid-col>
        <grid-col :span="1"/>
        <grid-col :span="14" class="val">
          <d-input v-model="editData.code" oninput="value = value.trim()"></d-input>
        </grid-col>
      </grid-row>
      <grid-row class="mb10">
        <grid-col :span="5" class="label">名称:</grid-col>
        <grid-col :span="1"/>
        <grid-col :span="14" class="val">
          <d-input v-model="editData.name" oninput="value = value.trim()"></d-input>
        </grid-col>
      </grid-row>
      <grid-row class="mb10">
        <grid-col :span="5" class="label">存储最长时间:</grid-col>
        <grid-col :span="1"/>
        <grid-col :span="14" class="val">
          <d-input v-model.number="editData.storeMaxTime" oninput="value=value.replace(/[^\d]/g, '')"></d-input>
        </grid-col>
      </grid-row>
      <grid-row class="mb10">
        <grid-col :span="5" class="label">保留未消费数据:</grid-col>
        <grid-col :span="1"/>
        <grid-col :span="14" class="val">
          <d-radio-group v-model="editData.storeCleanKeepUnconsumed">
            <d-radio :label="true">是</d-radio>
            <d-radio :label="false">否</d-radio>
            <d-radio :label="''">置空</d-radio>
          </d-radio-group>
        </grid-col>
      </grid-row>
      <grid-row class="mb10">
        <grid-col :span="5" class="label">仅管理员:</grid-col>
        <grid-col :span="1"/>
        <grid-col :span="14" class="val">
          <d-radio-group v-model="editData.status">
            <d-radio :label="2">是</d-radio>
            <d-radio :label="1">否</d-radio>
          </d-radio-group>
        </grid-col>
      </grid-row>
    </my-dialog>
    <!--添加broker-->
    <my-dialog :dialog="addBrokerDialog" @on-dialog-cancel="addBrokerCancel()">
      <add-broker ref="broker" :urls="addBrokerUrls" :colData="addBrokerColData" :btns="addBrokerBtns"></add-broker>
    </my-dialog>
  </div>
</template>

<script>
import myTable from '../../components/common/myTable.vue'
import myDialog from '../../components/common/myDialog.vue'
import addBroker from '../topic/addBroker.vue'
import crud from '../../mixins/crud.js'
import apiRequest from '../../utils/apiRequest.js'
import {deepCopy} from '../../utils/assist'

export default {
  name: 'broker-group',
  components: {
    myTable,
    myDialog,
    addBroker
  },
  mixins: [ crud ],
  props: {
    addBrokerUrls: {
      type: Object,
      default () {
        return {
          search: '/broker/search',
          addBroker: '/brokerGroup/updateBroker'
        }
      }
    },
    addBrokerColData: {
      type: Array,
      default () {
        return [
          {
            title: 'ID',
            key: 'id'
          },
          {
            title: 'IP',
            key: 'ip'
          },
          {
            title: '端口',
            key: 'port'
          },
          {
            title: 'Broker分组',
            key: 'group.code'
          }
        ]
      }
    },
    addBrokerBtns: {
      type: Array,
      default () {
        return [
          {
            txt: '添加',
            method: function (item) {
              let that = this.$parent
              let parmas = {
                id: item.id,
                group: that.$parent.$parent.$parent.group
              }
              that.$Dialog.confirm({
                title: '提示',
                content: '确定要添加吗？'
              }).then(() => {
                apiRequest.put(that.urls.addBroker + '/' + item.id, null, parmas).then((data) => {
                  that.getList()
                })
              })
            },
            bindKey: 'group',
            bindVal: undefined
          }
        ]
      }
    }
  },
  data () {
    return {
      group: {
        id: 0,
        code: ''
      },
      searchData: {
        keyword: ''
      },
      tableData: {
        rowData: [],
        colData: [
          {
            title: 'ID',
            key: 'id',
            width: '20%'
          },
          {
            title: '编码',
            key: 'code',
            width: '20%'
          },
          {
            title: '名称',
            key: 'name',
            width: '20%'
          },
          {
            title: '仅管理员',
            key: 'status',
            width: '20%',
            formatter (row) {
              if (row.status === 2) {
                return '是'
              }
              if (row.status === 1) {
                return '否'
              }
            }
          }
        ],
        // 表格操作，如果需要根据特定值隐藏显示， 设置bindKey对应的属性名和bindVal对应的属性值
        btns: [
          {
            txt: '详情',
            method: 'on-detail'
          },
          {
            txt: '编辑',
            method: 'on-edit'
          },
          {
            txt: '删除',
            method: 'on-del'
          },
          // {
          //   txt: '编辑标签',
          //   method: 'on-edit-label'
          // },
          {
            txt: '添加Broker',
            method: 'on-add-broker'
          }
        ]
      },
      multipleSelection: [],
      addDialog: {
        visible: false,
        title: '新建分组',
        showFooter: true
      },
      addData: {
        code: '',
        name: '',
        storeCleanKeepUnconsumed: undefined,
        storeMaxTime: undefined,
        status: 1
      },
      editDialog: {
        visible: false,
        title: '编辑分组',
        showFooter: true
      },
      editData: {
        code: '',
        name: '',
        storeCleanKeepUnconsumed: undefined,
        storeMaxTime: undefined,
        status: 1
      },
      addBrokerDialog: {
        visible: false,
        width: 700,
        title: '添加Broker',
        showFooter: false
      },
      brokerData: {}
    }
  },
  computed: {
    curLang () {
      return this.$i18n.locale
    }
  },
  methods: {
    openDialog (dialog) {
      this[dialog].visible = true
      this.addData.code = ''
      this.addData.name = ''
    },
    goDetail (item) {
      this.$router.push({name: `/${this.curLang}/setting/brokerGroup/detail`,
        query: {id: item.id, code: item.code, name: item.name}})
    },
    addBroker (item, index) {
      this.group = {
        id: item.id,
        code: item.code
      }
      this.addBrokerDialog.visible = true
      this.$nextTick(() => {
        this.$refs.broker.getList(item.id)
      })
    },
    submit () {
      if (!this.addData.code) {
        this.$Message.error('编码不能为空')
        return false
      }

      if (!this.addData.name) {
        this.$Message.error('名称不能为空')
        return false
      }
      this.addData.policies = {}
      if (this.addData.storeMaxTime !== undefined && this.addData.storeMaxTime > 0) {
        this.addData.policies.storeMaxTime = this.addData.storeMaxTime
      }
      if (this.addData.storeCleanKeepUnconsumed !== undefined && this.addData.storeCleanKeepUnconsumed !== '') {
        this.addData.policies.storeCleanKeepUnconsumed = this.addData.storeCleanKeepUnconsumed
      }
      this.addConfirm()
    },
    beforeEditData (item) {
      if (item.policies !== undefined) {
        if (item.policies.storeMaxTime !== undefined) {
          item.storeMaxTime = item.policies.storeMaxTime
        }
        if (item.policies.storeCleanKeepUnconsumed !== undefined && this.addData.storeCleanKeepUnconsumed !== '') {
          let storeCleanKeepUnconsumed = item.policies.storeCleanKeepUnconsumed
          if (storeCleanKeepUnconsumed === true || storeCleanKeepUnconsumed === 'true') {
            item.storeCleanKeepUnconsumed = true
          } else if (storeCleanKeepUnconsumed === false || storeCleanKeepUnconsumed === 'false') {
            item.storeCleanKeepUnconsumed = false
          } else {
            item.storeCleanKeepUnconsumed = ''
          }
        }
      }
      return deepCopy(item)
    },
    editSubmit () {
      if (!this.editData.code) {
        this.$Message.error('编码不能为空')
        return false
      }

      if (!this.editData.name) {
        this.$Message.error('名称不能为空')
        return false
      }
      this.editData.policies = {}
      if (this.editData.storeMaxTime !== undefined && this.editData.storeMaxTime > 0) {
        this.editData.policies.storeMaxTime = this.editData.storeMaxTime
      }
      if (this.editData.storeCleanKeepUnconsumed !== undefined && this.addData.storeCleanKeepUnconsumed !== '') {
        this.editData.policies.storeCleanKeepUnconsumed = this.editData.storeCleanKeepUnconsumed
      }
      this.editConfirm()
    },
    addBrokerCancel (index, row) {
      this.addBrokerDialog.visible = false
    },
    // 删除
    del (item, index) {
      let _this = this
      this.$Dialog.confirm({
        title: '提示',
        content: '删除后Broker分组绑定的broker会自动解除绑定，确定还要删除吗？'
      }).then(() => {
        if (typeof (_this.beforeDel) === 'function') {
          _this.beforeDel(item)
        }
        apiRequest.delete(_this.urlOrigin.del + '/' + item.id).then((data) => {
          if (data.code !== this.$store.getters.successCode) {
            this.$Dialog.error({
              content: '删除失败'
            })
          } else {
            this.$Message.success('删除成功')
            if (typeof (_this.afterDel) === 'function') {
              _this.afterDel(item)
            }
            _this.getList()
          }
        })
      }).catch(() => {
      })
    },
    getList () {
      this.showTablePin = true
      let data = this.getSearchVal()
      apiRequest.post(this.urlOrigin.search, {}, data).then((data) => {
        if (data === '') {
          return
        }
        data.data = data.data || []
        if (typeof (this.sortData) === 'function') {
          data.data = this.sortData(data.data)
        }
        data.pagination = data.pagination || {
          totalRecord: data.data.length
        }
        this.page.total = data.pagination.totalRecord
        this.page.page = data.pagination.page
        this.page.size = data.pagination.size
        this.tableData.rowData = data.data
        for (let i in this.tableData.rowData) {
          if (this.tableData.rowData.hasOwnProperty(i)) {
            if (this.tableData.rowData[i].policies) {
              for (let key in this.tableData.rowData[i].policies) {
                if (this.tableData.rowData[i].policies.hasOwnProperty(key)) {
                  this.tableData.rowData[i][key] = this.tableData.rowData[i].policies[key]
                }
              }
              this.$set(this.tableData.rowData, i, this.tableData.rowData[i])
            }
          }
        }
        this.showTablePin = false
      })
    }
  },
  mounted () {
    this.getList()
  }
}
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>
  .label{text-align: right; line-height: 32px;}
  .val{}
</style>
