<template>
  <div>
    <div class="ml20 mt30">
      <d-input v-model="searchData.keyword" placeholder="请输入类型/代码/名称" class="left mr10" style="width: 20%">
        <icon name="search" size="14" color="#CACACA" slot="suffix" @click="getList"></icon>
      </d-input>
      <d-button type="primary" @click="openDialog('addDialog')">新建报警类型<icon name="plus-circle" style="margin-left: 5px;"></icon></d-button>
    </div>
    <my-table :data="tableData" :showPin="showTablePin" :page="page" @on-size-change="handleSizeChange" @on-del="del"
              @on-current-change="handleCurrentChange" @on-selection-change="handleSelectionChange" @on-edit="edit">
    </my-table>

    <!--新建报警类型-->
    <my-dialog :dialog="addDialog" @on-dialog-confirm="addConfirm()" @on-dialog-cancel="dialogCancel('addDialog')">
      <alarm-type-form ref="addForm"  :type="$store.getters.addFormType"/>
    </my-dialog>
    <!--编辑报警类型-->
    <my-dialog :dialog="editDialog" @on-dialog-confirm="editConfirm()" @on-dialog-cancel="dialogCancel('editDialog')">
      <alarm-type-form ref="editForm" :data="editData" :type="$store.getters.editFormType" />
    </my-dialog>
  </div>
</template>

<script>
import myTable from '../../components/common/myTable.vue'
import myDialog from '../../components/common/myDialog.vue'
import alarmTypeForm from './alarmTypeForm.vue'
import crud from '../../mixins/crud.js'

export default {
  name: 'alarm-type',
  components: {
    myTable,
    myDialog,
    alarmTypeForm
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
          },
          {
            title: '代码',
            key: 'code'
          },
          {
            title: '名称',
            key: 'name'
          },
          {
            title: '指标',
            key: 'metric'
          },
          {
            title: '报警对象',
            key: 'userType',
            formatter (item) {
              if (item.userType === 'ALL') { return '全部' }
              if (item.userType === 'ADMIN') { return '管理员' }
              if (item.userType === 'USER') { return '普通用户' }
            }
          },
          {
            title: '接入方',
            key: 'endPoint',
            formatter (item) {
              if (item.endPoint === 'PRODUCER') { return '生产者' }
              if (item.endPoint === 'CONSUMER') { return '消费者' }
            }
          },
          {
            title: '建议',
            key: 'suggestion'
          },
          {
            title: '恢复建议',
            key: 'resumeSuggestion'
          },
          {
            title: '链接',
            key: 'url'
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
        title: '新建报警类型',
        showFooter: true,
        scrollable: true,
        width: 560
      },
      addData: {},
      editDialog: {
        visible: false,
        title: '编辑报警类型',
        showFooter: true
      },
      editData: {}
    }
  },
  methods: {
    openDialog (dialogName) {
      this[dialogName].visible = true
    }
    // submitAddForm(data){
    //   this.addSubmitData = data;//false or formData
    // },
    // beforeAdd(){
    //   this.$refs.addForm.submitForm();
    //   return this.addSubmitData;
    // },
    // beforeEditData(item){
    //   this.editData = item;
    //   return this.editData;
    // },
    // submitEditForm(data){
    //   this.editSubmitData = data;//false or formData
    // },
    // beforeEdit(item){
    //   this.$refs.editForm.submitForm();
    //   return this.editSubmitData;
    // }
  },
  mounted () {
    // todo 待完善后放开
    this.getList()
  }
}
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>
.label{text-align: right; line-height: 32px;}
.val{}
</style>
