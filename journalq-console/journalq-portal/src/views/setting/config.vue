<template>
  <div>
    <div class="ml20 mt30">
      <d-input v-model="searchData.keyword" placeholder="请输入分组/键" class="left mr10"
               style="width:300px" @on-enter="getList">
        <span slot="prepend">关键词</span>
        <icon name="search" size="14" color="#CACACA" slot="suffix" @click="getList"></icon>
      </d-input>
      <d-button type="primary" @click="openDialog('addDialog')">新建配置<icon name="plus-circle" style="margin-left: 5px;"></icon></d-button>
    </div>
    <my-table :data="tableData" :showPin="showTablePin" :page="page" @on-size-change="handleSizeChange" @on-current-change="handleCurrentChange"
              @on-selection-change="handleSelectionChange" @on-edit="edit" @on-del="del">
    </my-table>
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

export default {
  name: 'config',
  components: {
    myTable,
    myDialog,
    configForm
  },
  mixins: [ crud ],
  data () {
    return {
      searchData: {
        keyword: ''
      },
      tableData: {
        rowData: [],
        colData: [
          {
            title: 'ID',
            key: 'id'
          },
          {
            title: '键',
            key: 'key'
          },
          {
            title: '分组',
            key: 'group'
          }, {
            title: '值',
            key: 'value'
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
