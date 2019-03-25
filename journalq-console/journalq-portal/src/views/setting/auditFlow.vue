<template>
  <div>
    <div class="ml20 mt30">
      <d-input v-model="searchData.keyword" placeholder="请输入代码/名称" class="left mr10" style="width: 20%">
        <icon name="search" size="14" color="#CACACA" slot="suffix" @click="getList"></icon>
      </d-input>
      <d-button type="primary" @click="openDialog('addDialog', 'addForm')">新建流程角色<icon name="plus-circle" style="margin-left: 5px;"></icon></d-button>
    </div>
    <my-table :data="tableData" :showPin="showTablePin" :page="page" @on-size-change="handleSizeChange" @on-current-change="handleCurrentChange"
              @on-selection-change="handleSelectionChange" @on-edit="edit" @on-del="del">
    </my-table>

    <!--新建-->
    <my-dialog :dialog="addDialog" @on-dialog-confirm="addConfirm()" @on-dialog-cancel="dialogCancel('addDialog')">
      <audit-flow-form ref="addForm" :type="$store.getters.addFormType"/>
    </my-dialog>

    <!--编辑-->
    <my-dialog :dialog="editDialog" @on-dialog-confirm="editConfirm()" @on-dialog-cancel="dialogCancel('editDialog')">
      <audit-flow-form ref="editForm" :data="editData" :type="$store.getters.editFormType"/>
    </my-dialog>

    <!--编辑节点-->
    <!--<my-dialog :dialog="editDialog" @on-dialog-confirm="editConfirm()" @on-dialog-cancel="dialogCancel('editDialog')">-->
      <!--<audit-role-form ref="editForm" :data="editData" :type="$store.getters.editFormType"/>-->
    <!--</my-dialog>-->
  </div>
</template>

<script>
import myTable from '../../components/common/myTable.vue'
import myDialog from '../../components/common/myDialog.vue'
import auditFlowForm from './auditFlowForm.vue'
import crud from '../../mixins/crud.js'

export default {
  name: 'audit-flow',
  components: {
    myTable,
    myDialog,
    auditFlowForm
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
            title: '申请类型',
            key: 'type'
          },
          {
            title: '是否用户确认',
            key: 'applicantConfirm',
            formatter (row) {
              return row ? '是' : '否'
            }
          },
          {
            title: '是否手动执行',
            key: 'manualExecute',
            formatter (row) {
              return row ? '是' : '否'
            }
          },
          {
            title: '手动执行类',
            key: 'executeClass'
          },
          {
            title: '手动执行方法',
            key: 'executeMethod'
          },
          {
            title: '描述',
            key: 'description'
          }
          // {
          //   title:'状态',
          //   key: 'status',
          //   render:(h, params) => {
          //     let label;
          //     switch(params.item.status){
          //       case 0:
          //         label = '禁用'
          //         break;
          //       case 1:
          //         label = '启用'
          //         break;
          //     }
          //     return h('label', {}, label)
          //   }
          // }
        ],
        // 表格操作，如果需要根据特定值隐藏显示， 设置bindKey对应的属性名和bindVal对应的属性值
        btns: [
          // {
          //   txt: '编辑节点',
          //   method: 'on-edit-nodes'
          // },
          {
            txt: '编辑',
            method: 'on-edit'
          },
          // {
          //   txt: '禁用',
          //   method: 'on-disable',
          //   bindKey: 'status',
          //   bindVal: 1
          // },
          // {
          //   txt: '启用',
          //   method: 'on-enable',
          //   bindKey: 'status',
          //   bindVal: 0
          // },
          {
            txt: '删除',
            method: 'on-del'
          }
        ]
      },
      addDialog: {
        visible: false,
        title: '新建审批角色',
        showFooter: true,
        scrollable: true,
        width: 560
      },
      addData: {},
      editDialog: {
        visible: false,
        title: '编辑审批角色',
        showFooter: true,
        width: 560
      },
      editData: {}
    }
  },
  methods: {
    openDialog (dialogName) {
      this[dialogName].visible = true
    },
    beforeEditData (item) {
      // editData 排序
      this.editData = item
      if (this.editData.auditNodes !== undefined) {
        this.editData.auditNodes.sort((node1, node2) => {
          var value1 = node1.nodeNo
          var value2 = node2.nodeNo
          return value1 - value2
        })
      }
      return this.editData
    }
    // 启用/禁用
    // state(item, val){
    //   let editData = item;
    //   editData.status = val;//启用1，禁用0
    //   apiRequest.put(this.urlOrigin.state + "/" + editData.id, {}, editData).then((data) => {
    //     if(data.code != 200) {
    //       this.$Dialog.success({
    //         content:'更新失败'
    //       });
    //     }else{
    //       this.$Dialog.success({
    //         content: '更新成功'
    //       });
    //     }
    //     this.getList();
    //   })
    // },
    // //启用
    // enable(item) {
    //   this.state(item, 1);
    // },
    // //禁用
    // disable(item) {
    //   this.state(item, 0);
    // },
  },
  mounted () {
    this.getList()
  }
}
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>
</style>
