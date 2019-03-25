<template>
  <div>
    <div class="ml20 mt30">
      <d-input v-model="searchData.keyword" placeholder="请输入编码" class="left mr10" style="width: 20%">
        <icon name="search" size="14" color="#CACACA" slot="suffix" @click="getList"></icon>
      </d-input>
      <d-button type="primary" @click="openDialog('addDialog')">新建标签<icon name="plus-circle" style="margin-left: 5px;"></icon></d-button>
    </div>
    <my-table :data="tableData" :showPin="showTablePin" :page="page" @on-size-change="handleSizeChange"
              @on-current-change="handleCurrentChange" @on-selection-change="handleSelectionChange" @on-edit="edit" @on-del="del">
    </my-table>

    <!--新建标签-->
    <my-dialog :dialog="addDialog" @on-dialog-confirm="addConfirm()" @on-dialog-cancel="dialogCancel('addDialog')">
      <label-form ref="addForm" :type="$store.getters.addFormType" />
    </my-dialog>
    <!--编辑标签-->
    <my-dialog :dialog="editDialog" @on-dialog-confirm="editConfirm()" @on-dialog-cancel="dialogCancel('editDialog')">
      <label-form ref="editForm" :data="editData"  :type="$store.getters.editFormType" />
    </my-dialog>
  </div>
</template>

<script>

import myTable from '../../components/common/myTable.vue'
import myDialog from '../../components/common/myDialog.vue'
import labelForm from './labelForm.vue'
import crud from '../../mixins/crud.js'

export default {
  name: 'label-config',
  components: {
    myTable,
    myDialog,
    labelForm
  },
  mixins: [ crud ],
  data () {
    return {
      searchData: {
        keyword: '',
        group: 'label'// 标签分组
      },
      tableData: {
        rowData: [],
        colData: [{
          title: 'ID',
          key: 'id'
        }, {
          title: '编码',
          key: 'key'
        }, {
          title: '标签',
          key: 'value'
        }],
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
        title: '新建标签',
        showFooter: true
      },
      addData: {},
      editDialog: {
        visible: false,
        title: '编辑标签',
        showFooter: true
      },
      editData: {}
    }
  },
  computed: {
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
