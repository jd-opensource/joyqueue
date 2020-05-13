<template>
  <div>
    <div class="ml20 mt30">
      <d-input v-model="searchData.keyword" oninput="value = value.trim()" placeholder="请输入代码/名称/来源" class="left mr10"
               style="width:300px" @on-enter="getList">
        <span slot="prepend">关键词</span>
        <icon name="search" size="14" color="#CACACA" slot="suffix" @click="getList"></icon>
      </d-input>

      <d-button type="primary" class="left ml10" @click="openDialog('addDialog')">新建指标<icon name="plus-circle" style="margin-left: 5px;"></icon></d-button>
    </div>
    <my-table :data="tableData" :showPin="showTablePin" :page="page" @on-size-change="handleSizeChange"
              @on-current-change="handleCurrentChange" @on-selection-change="handleSelectionChange" @on-edit="edit" @on-del="del">
    </my-table>

    <!--新建-->
    <my-dialog :dialog="addDialog" @on-dialog-confirm="addConfirm()" @on-dialog-cancel="dialogCancel('addDialog')">
      <metric-form ref="addForm" :type="$store.getters.addFormType" />
    </my-dialog>
    <my-dialog :dialog="editDialog" @on-dialog-confirm="editConfirm()" @on-dialog-cancel="dialogCancel('editDialog')">
      <metric-form ref="editForm" :data="editData"  :type="$store.getters.editFormType" />
    </my-dialog>
  </div>
</template>

<script>
import myTable from '../../components/common/myTable.vue'
import myDialog from '../../components/common/myDialog.vue'
import crud from '../../mixins/crud.js'
import metricForm from './metricForm.vue'
import { deepCopy } from '../../utils/assist.js'

export default {
  name: 'metric',
  components: {
    myTable,
    myDialog,
    metricForm
  },
  mixins: [ crud ],
  data () {
    return {
      searchData: {
        keyword: ''
      },
      searchRules: {
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
          }, {
            title: '别名',
            key: 'aliasCode'
          }, {
            title: '名称',
            key: 'name'
          }, {
            title: '类型',
            key: 'type',
            formatter (item) {
              let label
              switch (item.type) {
                case 1:
                  label = '原子'
                  break
                case 2:
                  label = '聚集'
                  break
              }
              return label
            }
          },
          {
            title: '指标来源',
            key: 'source'
          },
          {
            title: '用户是否有权限',
            key: 'userPermission',
            formatter (row) {
              return row.userPermission ? '是' : '否'
            }
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
      addDialog: {
        visible: false,
        title: '新建指标',
        showFooter: true
      },
      addData: {},
      editData: {},
      // editSubmitData:{},
      editDialog: {
        visible: false,
        title: '编辑指标',
        showFooter: true
      }
    }
  },
  methods: {
    openDialog (dialog) {
      this[dialog].visible = true
    },
    beforeEditData (item) {
      // editData need to be formatted, so copy item's properties
      let newData = deepCopy(item)
      if (newData.groupField === undefined || newData.groupField === '') {
        newData.groupFieldArray = []
      } else {
        newData.groupFieldArray = newData.groupField.split(',')
      }
      this.editData = newData
      return this.editData
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
