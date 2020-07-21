<template>
  <div>
    <div class="ml20 mt30">
      <d-input v-model="searchData.keyword" oninput="value = value.trim()" placeholder="请输入分组/键" class="left mr10"
               style="width:300px" @on-enter="getList">
        <span slot="prepend">关键词</span>
        <icon name="search" size="14" color="#CACACA" slot="suffix" @click="getList"></icon>
      </d-input>
      <d-button type="primary" @click="openDialog('addDialog')">新建配置<icon name="plus-circle" style="margin-left: 5px;"></icon></d-button>
    </div>
    <div class ="ml20 mt30">
      <d-button type="primary" color = "success" name="search" v-on:click = "getList">all</d-button>
      <d-button type="primary" color = "success" name="search" v-on:click = "getListByPrefix('store.max.store.time')">store.max.store.time</d-button>
      <d-button type="primary" color = "success" name="search" v-on:click = "getListByPrefix('store.clean.keep.unconsumed')">store.clean.keep.unconsumed</d-button>
      <d-button type="primary" color = "success" name="search" v-on:click = "getListByPrefix('retry.random.bound')">retry.random.bound</d-button>
    </div>
    <my-table :data="tableData" :showPin="showTablePin" :showPagination="false" :page="page" @on-size-change="handleSizeChange" @on-current-change="handleCurrentChange"
              @on-selection-change="handleSelectionChange" @on-edit="edit" @on-del="del">
    </my-table>

    <!--    <d-button class="right load-btn" v-if="this.curIndex < this.cacheList.length-1" type="primary" @click="getRestList">加载更多
          <icon name="refresh-cw" style="margin-left: 3px;"></icon>
        </d-button>-->

    <!--新增-->
    <my-dialog :dialog="addDialog" @on-dialog-confirm="addConfirm()" @on-dialog-cancel="dialogCancel('addDialog')" >
      <config-form ref="addForm" :type="$store.getters.addFormType" />
    </my-dialog>
    <!--编辑-->
    <my-dialog :dialog="editDialog" @on-dialog-confirm="editConfirm()" @on-dialog-cancel="dialogCancel('editDialog')">
      <config-form ref="editForm" :data="editData"  :type="$store.getters.editFormType" />
    </my-dialog>
  </div>
</template>

<script>

import myTable from '../../components/common/myTable.vue'
import myDialog from '../../components/common/myDialog.vue'
import configForm from './configForm.vue'
import crud from '../../mixins/crud.js'
import apiRequest from '../../utils/apiRequest'
import ButtonGroup from '../../components/button/button-group'

export default {
  name: 'config',
  components: {
    myTable,
    myDialog,
    configForm,
    ButtonGroup
  },
  mixins: [ crud ],
  data () {
    return {
      curIndex: 0,
      cacheList: [],
      searchData: {
        keyword: ''
      },
      searchKeyPrefix: '',
      tableData: {
        rowData: [],
        colData: [
          {
            title: 'ID',
            key: 'id',
            width: '30%'
          },
          {
            title: '键',
            key: 'key',
            width: '30%'
          },
          {
            title: '分组',
            key: 'group',
            width: '30%'
          }, {
            title: '值',
            key: 'value',
            width: '10%'
          }
        ],
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
      addDialog: {
        visible: false,
        title: '新建配置',
        showFooter: true
      },
      addData: {},
      editDialog: {
        visible: false,
        title: '编辑配置',
        showFooter: true
      },
      editData: {}
    }
  },
  methods: {
    getListByPrefix (value) {
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
        this.page.total = data.pagination.totalRecord // 总数
        this.page.page = data.pagination.page // 分页
        this.page.size = data.pagination.size // 分页大小
        let result = []
        for (let i = 0, j = 0; i < data.data.length; i++) {
          if (data.data[i]['key'].slice(0, value.length) == value) {
            result.push(data.data[i])
          }
        }
        this.tableData.rowData = result
        this.curIndex = data.data.length - 1
        /*        } */
        this.cacheList = data.data
        this.showTablePin = false
      })
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
        /*        if (data.data.length > this.page.size) {
                    this.tableData.rowData = data.data.slice(0, this.page.size)
                    this.curIndex = this.page.size - 1
                  } else { */
        this.tableData.rowData = data.data
        this.curIndex = data.data.length - 1
        /*        } */
        this.cacheList = data.data
        this.showTablePin = false
      })
    }
    /* getRestList () {
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
      } */
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
