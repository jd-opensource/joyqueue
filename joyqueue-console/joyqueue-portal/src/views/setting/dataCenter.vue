<template>
  <div>
    <div class="ml20 mt30">
      <d-input v-model="searchData.code" oninput="value = value.trim()" placeholder="请输入编码" class="left mr10"
               style="width:300px" @on-enter="getList">
        <span slot="prepend">编码</span>
        <icon name="search" size="14" color="#CACACA" slot="suffix" @click="getList"></icon>
      </d-input>
      <d-button type="primary" @click="openDialog('addDialog')">新建数据中心<icon name="plus-circle" style="margin-left: 5px;"></icon></d-button>
    </div>
    <my-table :data="tableData" :showPin="showTablePin" :showPagination="false" :page="page" @on-size-change="handleSizeChange"
              @on-current-change="handleCurrentChange" @on-selection-change="handleSelectionChange"
              @on-edit="edit" @on-del="del">
    </my-table>

    <d-button class="right load-btn" v-if="this.curIndex < this.cacheList.length-1" type="primary" @click="getRestList">加载更多
      <icon name="refresh-cw" style="margin-left: 3px;"></icon>
    </d-button>

    <!--新建数据中心-->
    <my-dialog :dialog="addDialog" @on-dialog-confirm="submitForm('addData')" @on-dialog-cancel="addCancel()">
      <d-form :model="addData" :rules="rules" ref="addData" label-width="120px" class="demo-ruleForm">
        <d-form-item label="区域:" prop="region" style="width: 80%">
          <d-input v-model="addData.region" oninput="value = value.trim()"></d-input>
        </d-form-item>
        <d-form-item label="编码:" prop="code" style="width: 80%">
          <d-input v-model="addData.code" oninput="value = value.trim()"></d-input>
        </d-form-item>
        <d-form-item label="名称:" prop="name" style="width: 80%">
          <d-input v-model="addData.name" oninput="value = value.trim()"></d-input>
        </d-form-item>
        <d-form-item label="解析类型:" prop="matchType" style="width: 80%">
          <d-input v-model="addData.matchType" placeholder="如:IPRANGE" oninput="value = value.trim()"></d-input>
        </d-form-item>
        <d-form-item label="IP段:" prop="ips" style="width: 80%">
          <d-input v-model="addData.ips" placeholder="多个;分隔" oninput="value = value.trim()"></d-input>
        </d-form-item>
      </d-form>
    </my-dialog>

    <my-dialog :dialog="editDialog" @on-dialog-confirm="submitForm('editData')" @on-dialog-cancel="editCancel()">
      <d-form :model="editData" :rules="rules" ref="editData" label-width="120px" class="demo-ruleForm">
        <d-form-item label="区域:" prop="region" style="width: 60%">
          <d-input v-model="editData.region" disabled oninput="value = value.trim()"></d-input>
        </d-form-item>
        <d-form-item label="编码:" prop="code" style="width: 60%">
          <d-input v-model="editData.code" disabled oninput="value = value.trim()"></d-input>
        </d-form-item>
        <d-form-item label="名称:" prop="name" style="width: 60%">
          <d-input v-model="editData.name" oninput="value = value.trim()"></d-input>
        </d-form-item>
        <d-form-item label="解析类型:" prop="matchType" style="width: 80%">
          <d-input v-model="editData.matchType" oninput="value = value.trim()"></d-input>
        </d-form-item>
        <d-form-item label="IP段:" prop="ips" style="width: 60%">
          <d-input v-model="editData.ips" oninput="value = value.trim()"></d-input>
        </d-form-item>
      </d-form>
    </my-dialog>
    <!--添加子网-->
    <my-dialog :dialog="addNetDialog" @on-dialog-confirm="addNetConfirm()" @on-dialog-cancel="addNetCancel()">
      <grid-row class="mb10">
        <grid-col :span="8" class="label">编码:</grid-col>
        <grid-col :span="16" class="val">
          <d-input v-model="addNetData.code" oninput="value = value.trim()"></d-input>
        </grid-col>
      </grid-row>
      <grid-row class="mb10">
        <grid-col :span="8" class="label">地址:</grid-col>
        <grid-col :span="16" class="val">
          <d-input v-model="addNetData.cidr" oninput="value = value.trim()"></d-input>
        </grid-col>
      </grid-row>
    </my-dialog>
  </div>
</template>

<script>
import apiRequest from '../../utils/apiRequest.js'
import myTable from '../../components/common/myTable.vue'
import myDialog from '../../components/common/myDialog.vue'
import crud from '../../mixins/crud.js'
import ButtonGroup from '../../components/button/button-group'

export default {
  name: 'application',
  components: {
    myTable,
    myDialog,
    ButtonGroup
  },
  mixins: [ crud ],
  data () {
    return {
      // urls: {
      //   del: '/datacenter'
      // },
      curIndex: 0,
      cacheList: [],
      searchData: {
        code: ''
      },
      searchRules: {
      },
      tableData: {
        rowData: [],
        colData: [{
          title: 'ID',
          key: 'id'
        }, {
          title: '区域',
          key: 'region'
        }, {
          title: '编码',
          key: 'code'
        }, {
          title: '名称',
          key: 'name'
        }, {
          title: '解析类型',
          key: 'matchType'
        }, {
          title: 'IP',
          key: 'ips'
        }],
        // 表格操作，如果需要根据特定值隐藏显示， 设置bindKey对应的属性名和bindVal对应的属性值
        btns: [
          // {
          //   txt: '添加子网',
          //   method: 'on-addNet'
          // },
          {
            txt: '编辑',
            method: 'on-edit'
          },
          {
            txt: '删除',
            method: 'on-del'
          }
          // ,
          //   {
          //   txt: '详情',
          //   method: 'on-detail'
          // }
        ]
      },
      multipleSelection: [],
      addDialog: {
        visible: false,
        title: '新建数据中心',
        showFooter: true
      },
      addData: {
        code: '',
        name: '',
        matchType: '',
        ips: '',
        region: ''
      },
      editDialog: {
        visible: false,
        title: '编辑数据中心',
        showFooter: true
      },
      editData: {
        code: '',
        name: '',
        matchType: '',
        ips: '',
        region: ''
      },
      addNetDialog: {
        visible: false,
        title: '添加子网',
        showFooter: true
      },
      addNetData: {
        code: '',
        cidr: ''
      },
      rules: {
        code: [{required: true, message: '请填写编码', trigger: 'change'}],
        name: [{required: true, message: '请填写名称', trigger: 'change'}],
        matchType: [{required: true, message: '请填写名称', trigger: 'change'}],
        region: [{required: true, message: '请输入数据中心所在区域', trigger: 'change'}],
        ips: [{required: true, message: '请填写ip段', trigger: 'change'}]
      }
    }
  },
  computed: {
    addNetUrl () {
      return this.urlOrigin.addNet
    }
  },
  methods: {
    openDialog (dialog) {
      this[dialog].visible = true
      this.addData.code = ''
      this.addData.name = ''
      this.addData.region = ''
      this.addData.ips = ''
      this.addData.matchType = ''
    },
    beforeEdit () {
      return new Promise((resolve, reject) => {
        resolve({
          id: this.editData.id,
          code: this.editData.code,
          name: this.editData.name,
          matchType: this.editData.matchType,
          ips: this.editData.ips,
          region: this.editData.region
        })
      })
    },
    // beforeDel(item){ //没有保存del数据，需要传递item
    //   this.urls.del = this.urls.del + '/' + item.id;
    // },
    goDetail (item) {
      this.$router.push({name: `/${this.$i18n.locale}/setting/dataCenter/detail`, query: {id: item.id, name: item.name}})
    },
    openAddNetDialog () {
      this.addNetDialog.visible = true
    },
    addNetConfirm () {
      apiRequest.get(this.addNetUrl, this.addNetData).then((data) => {
        this.addNetDialog.visible = false
        this.$Dialog.success({
          content: '添加成功'
        })
        this.getList()
      })
    },
    addNetCancel (index, row) {
      this.addNetDialog.visible = false
    },
    submitForm (formName) {
      this.$refs[formName].validate((valid) => {
        if (valid) {
          if (formName === 'addData') {
            this.addConfirm()
          } else {
            if (formName === 'editData') {
              this.editConfirm()
            }
          }
          this.getList()
          // this.$Message.info('提交成功!');
        } else {
          this.$Message.error('参数异常')
          return false
        }
      })
    },
    resetForm (formName) {
      this.$refs[formName].resetFields()
    },
    getList () {
      this.tableData.rowData = []
      this.showTablePin = true
      let data = this.getSearchVal()
      apiRequest.post(this.urlOrigin.search, {}, data).then((data) => {
        if (data === '') {
          return
        }
        data.data = data.data || []
        data.pagination = data.pagination || {
          totalRecord: data.data.length
        }
        this.page.total = data.pagination.totalRecord
        this.page.page = data.pagination.page
        this.page.size = data.pagination.size
        if (data.data.length > this.page.size) {
          this.tableData.rowData = data.data.slice(0, this.page.size)
          this.curIndex = this.page.size - 1
        } else {
          this.tableData.rowData = data.data
          this.curIndex = data.data.length - 1
        }
        this.cacheList = data.data
        this.showTablePin = false
      })
    },
    // 滚动事件触发下拉加载
    getRestList () {
      if (this.curIndex < this.cacheList.length - 1) {
        for (let i = 0; i < this.page.size; i++) {
          if (this.curIndex < this.cacheList.length - 1) {
            this.curIndex += 1
            if (!this.tableData.rowData.includes(this.cacheList[this.curIndex])) {
              this.tableData.rowData.push(this.cacheList[this.curIndex])
            }
          } else {
            break
          }
        }
      }
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
  .load-btn { margin-right: 50px;margin-top: -30px;position: relative}
</style>
