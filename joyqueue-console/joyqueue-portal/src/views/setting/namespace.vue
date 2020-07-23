<template>
  <div>
    <div class="ml20 mt30">
      <d-input v-model="searchData.keyword" oninput="value = value.trim()" placeholder="请输入代码/值" class="left mr10"
               style="width:300px" @on-enter="getList">
        <span slot="prepend">关键词</span>
        <icon name="search" size="14" color="#CACACA" slot="suffix" @click="getList"></icon>
      </d-input>
      <d-button type="primary" @click="openDialog('addDialog')">新建命名空间
        <icon name="plus-circle" style="margin-left: 5px;">
      </icon></d-button>
    </div>
    <my-table :data="tableData" :showPin="showTablePin" :show-pagination="false" :page="page" @on-size-change="handleSizeChange"
              @on-current-change="handleCurrentChange" @on-selection-change="handleSelectionChange" @on-edit="edit" @on-del="del">
    </my-table>

    <d-button class="right load-btn" v-if="this.curIndex < this.cacheList.length-1 && this.cacheList.length!==0" type="primary" @click="getRestList">加载更多
      <icon name="refresh-cw" style="margin-left: 3px;"></icon>
    </d-button>

    <!--新建namespace-->
    <my-dialog :dialog="addDialog" @on-dialog-confirm="addConfirm()" @on-dialog-cancel="addCancel()">
        <grid-row class="mb10">
          <grid-col :span="3" class="label">代码:</grid-col>
          <grid-col :span="1"/>
          <grid-col :span="14" class="val">
            <d-input v-model="addData.code" oninput="value = value.trim()"></d-input>
          </grid-col>
        </grid-row>
    </my-dialog>
    <!--编辑namespace-->
    <my-dialog :dialog="editDialog" @on-dialog-confirm="editConfirm()" @on-dialog-cancel="editCancel()">
      <grid-row class="mb10">
        <grid-col :span="5" class="label">代码:</grid-col>
        <grid-col :span="1"/>
        <grid-col :span="14" class="val">
          <d-input v-model="editData.code" oninput="value = value.trim()" disabled></d-input>
        </grid-col>
      </grid-row>
    </my-dialog>
  </div>
</template>

<script>
import myTable from '../../components/common/myTable.vue'
import myDialog from '../../components/common/myDialog.vue'
import crud from '../../mixins/crud.js'
import ButtonGroup from '../../components/button/button-group'
import apiRequest from '../../utils/apiRequest'
export default {
  name: 'namespace',
  components: {
    myTable,
    myDialog,
    ButtonGroup
  },
  mixins: [ crud ],
  data () {
    return {
      searchData: {
        keyword: ''
      },
      curIndex: 0,
      cacheList: [],
      tableData: {
        rowData: [],
        colData: [
          {
            title: 'ID',
            key: 'id'
          }, {
            title: '代码',
            key: 'code'
          }
        ],
        // 表格操作，如果需要根据特定值隐藏显示， 设置bindKey对应的属性名和bindVal对应的属性值
        btns: [
          {
            txt: '编辑',
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
        title: '新建命名空间',
        showFooter: true
      },
      addData: {
        code: ''
      },
      editDialog: {
        visible: false,
        title: '编辑命名空间',
        showFooter: true
      },
      editData: {}
    }
  },
  methods: {
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
    openDialog (dialog) {
      this[dialog].visible = true
      this.addData.code = ''
    },
    beforeEdit () {
      return new Promise((resolve, reject) => {
        resolve({
          id: this.editData.id,
          code: this.editData.code
        })
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
