<template>
  <div>
    <div class="ml20 mt30">
      <d-input v-model="searchData.keyword" placeholder="请输入代码/值" class="left mr10"
               style="width:300px" @on-enter="getList">
        <span slot="prepend">关键词</span>
        <icon name="search" size="14" color="#CACACA" slot="suffix" @click="getList"></icon>
      </d-input>
      <d-button type="primary" @click="openDialog('addDialog')">新建 Namespace
        <icon name="plus-circle" style="margin-left: 5px;">
      </icon></d-button>
    </div>
    <my-table :data="tableData" :showPin="showTablePin" :page="page" @on-size-change="handleSizeChange"
              @on-current-change="handleCurrentChange" @on-selection-change="handleSelectionChange" @on-edit="edit" @on-del="del">
    </my-table>

    <!--新建namespace-->
    <my-dialog :dialog="addDialog" @on-dialog-confirm="addConfirm()" @on-dialog-cancel="addCancel()">
        <grid-row class="mb10">
          <grid-col :span="3" class="label">代码:</grid-col>
          <grid-col :span="1"/>
          <grid-col :span="14" class="val">
            <d-input v-model="addData.code"></d-input>
          </grid-col>
        </grid-row>
    </my-dialog>
    <!--编辑namespace-->
    <my-dialog :dialog="editDialog" @on-dialog-confirm="editConfirm()" @on-dialog-cancel="editCancel()">
      <grid-row class="mb10">
        <grid-col :span="5" class="label">代码:</grid-col>
        <grid-col :span="1"/>
        <grid-col :span="14" class="val">
          <d-input v-model="editData.code" disabled></d-input>
        </grid-col>
      </grid-row>
    </my-dialog>
  </div>
</template>

<script>
import myTable from '../../components/common/myTable.vue'
import myDialog from '../../components/common/myDialog.vue'
import crud from '../../mixins/crud.js'
export default {
  name: 'namespace',
  components: {
    myTable,
    myDialog
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
