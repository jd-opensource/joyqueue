<template>
  <div>
    <div class="ml20 mt30">
      <d-input v-model="searchData.keyword" placeholder="请输入代码/名称" class="left mr10" style="width: 20%">
        <icon name="search" size="14" color="#CACACA" slot="suffix" @click="getList"></icon>
      </d-input>
      <d-button type="primary" @click="openDialog('addDialog', 'addForm')">新建审批角色<icon name="plus-circle" style="margin-left: 5px;"></icon></d-button>
    </div>
    <my-table :data="tableData" :showPin="showTablePin" :page="page" @on-size-change="handleSizeChange" @on-current-change="handleCurrentChange"
              @on-selection-change="handleSelectionChange" @on-edit="edit" @on-del="del">
    </my-table>

    <!--新建-->
    <my-dialog :dialog="addDialog" @on-dialog-confirm="addConfirm()" @on-dialog-cancel="dialogCancel('addDialog')">
      <audit-role-form ref="addForm" :type="$store.getters.addFormType"/>
    </my-dialog>

    <!--编辑-->
    <my-dialog :dialog="editDialog" @on-dialog-confirm="editConfirm()" @on-dialog-cancel="dialogCancel('editDialog')">
      <audit-role-form ref="editForm" :data="editData" :type="$store.getters.editFormType"/>
    </my-dialog>
  </div>
</template>

<script>
import myTable from '../../components/common/myTable.vue'
import myDialog from '../../components/common/myDialog.vue'
import auditRoleForm from './auditRoleForm.vue'
import crud from '../../mixins/crud.js'

export default {
  name: 'audit-role',
  components: {
    myTable,
    myDialog,
    auditRoleForm
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
            key: 'id',
            width: '7%'
          },
          {
            title: '代码',
            key: 'code',
            width: '10%'
          },
          {
            title: '名称',
            key: 'name',
            width: '10%'
          },
          {
            title: '查询SQL',
            key: 'sql',
            width: '40%'
          },
          {
            title: '指定审批人',
            key: 'userCodes',
            width: '20%'
          }
        ],
        // 表格操作，如果需要根据特定值隐藏显示， 设置bindKey对应的属性名和bindVal对应的属性值
        btns: [
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
    }
    // //启用/禁用
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
    // submitEditForm(data){
    //   if (data===false) {
    //     this.editSubmitData = false;
    //   } else {
    //     this.editSubmitData = Object.assign({}, data);//false or formData
    //   }
    // },
  //   beforeEdit(item){
  //     return new Promise((resolve, reject) => {
  //       this.$refs.editForm.submitForm().then(data=>{
  //         resolve(data);//false or formData
  //       }).catch(error => {
  //         reject(error)
  //       });
  //     })
  //   }
  },
  mounted () {
    this.getList()
  }
}
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>
</style>
