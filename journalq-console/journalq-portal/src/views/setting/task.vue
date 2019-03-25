<template>
  <div>
    <div class="ml20 mt30">
      <d-input v-model="searchData.keyword" placeholder="请输入任务类型" class="left mr10" style="width: 20%">
        <icon name="search" size="14" color="#CACACA" slot="suffix" @click="getList"></icon>
      </d-input>
      <d-button type="primary" @click="openDialog('addDialog')">新建任务<icon name="plus-circle" style="margin-left: 5px;"></icon></d-button>
      <!--<d-button type="primary" @click="startTasks">启用<icon name="check-circle" style="margin-left: 5px;"></icon></d-button>-->
    </div>
    <my-table :data="tableData" :showPin="showTablePin" :page="page" @on-size-change="handleSizeChange"
              @on-current-change="handleCurrentChange" @on-selection-change="handleSelectionChange"
               @on-del="del" @on-enable="enable" @on-edit="edit" @on-viewException="viewException">
    </my-table>
    <my-dialog :dialog="addDialog" @on-dialog-confirm="addConfirm()" @on-dialog-cancel="addCancel()">
        <task-from ref="addForm" :data="addData" :type="$store.getters.addFormType" />
    </my-dialog>
    <my-dialog :dialog="editDialog" @on-dialog-confirm="editConfirm()" @on-dialog-cancel="addCancel()">
      <task-from ref="editForm" :data='editData' :type="$store.getters.editFormType" />
    </my-dialog>
  </div>
</template>

<script>
import apiRequest from '../../utils/apiRequest.js'
import myTable from '../../components/common/myTable.vue'
import myDialog from '../../components/common/myDialog.vue'
import crud from '../../mixins/crud.js'
import taskFrom from './taskForm.vue'
export default {
  name: 'task',
  components: {
    myTable,
    myDialog,
    taskFrom
  },
  mixins: [ crud ],
  data () {
    return {
      searchData: {
        keyword: '',
        newOrFailed: 'true'
      },
      searchRules: {
      },
      deleteDialog: {
        visible: false
      },
      tableData: {
        rowData: [],
        colData: [
          {
            title: 'ID',
            key: 'id'
          },
          {
            title: '任务类型',
            key: 'type'
          },
          {
            title: '互斥类型',
            key: 'mutex'
          },
          // {
          //   title:'数据中心',
          //   key: 'dataCenter.code'
          // },
          {
            title: '关联主键',
            key: 'referId'
          }, {
            title: '优先级',
            key: 'priority'
          }, {
            title: '参数',
            key: 'url'
          }, {
            title: '表达式',
            key: 'cron'
          }, {
            title: '派发类型',
            key: 'dispatchType',
            formatter (row) {
              if (row.dispatchType === 0) {
                return '任意执行器'
              }
              if (row.dispatchType === 1) {
                return '原有执行器优先'
              }
              if (row.dispatchType === 2) {
                return '必须派发给原有的执行器'
              }
            }
          }, {
            title: '守护任务',
            key: 'daemons',
            formatter (row) {
              return row.daemons ? '是' : '否'
            }
          }, {
            title: '重试',
            key: 'retry',
            formatter (row) {
              return row.retry ? '是' : '否'
            }
          }, {
            title: '重试次数',
            key: 'retryCount'
          }, {
            title: '状态',
            key: 'status',
            formatter (row) {
              if (row.status === 1) {
                return '新建'
              }
              if (row.status === 0) {
                return '审核中'
              }
              if (row.status === -1) {
                return '删除'
              }
              if (row.status === 2) {
                row.isException = true
                return '失败需要重试'
              }
              if (row.status === 3) {
                return '已派发'
              }
              if (row.status === 4) {
                return '执行中'
              }
              if (row.status === 5) {
                return '成功'
              }
              if (row.status === 6) {
                row.isException = true
                return '失败不重试'
              }
            }
          }
        ],
        // 表格操作，如果需要根据特定值隐藏显示， 设置bindKey对应的属性名和bindVal对应的属性值
        btns: [
          {
            txt: '编辑',
            method: 'on-edit',
            bindKey: 'status',
            bindVal: 0
          },
          {
            txt: '启用',
            method: 'on-enable',
            bindKey: 'status',
            bindVal: 0
          },
          {
            txt: '查看异常',
            method: 'on-viewException',
            bindKey: 'isException',
            bindVal: 1
          },
          {
            txt: '删除',
            method: 'on-del'
          }
        ]
      },
      // multipleSelection: [],
      addDialog: {
        visible: false,
        title: '新建任务',
        showFooter: true
      },
      addData: {
        type: '',
        mutex: '',
        referId: 0,
        priority: 0,
        daemons: false,
        url: '',
        cron: '',
        dispatchType: 0,
        retry: true,
        status: 0
      },
      editDialog: {
        visible: false,
        title: '编辑任务',
        showFooter: true
      },
      editData: {},
      editSubmitData: {},
      statusEnum: [
        {'key': -1, 'value': '删除'},
        {'key': 0, 'value': '审核中'},
        {'key': 1, 'value': '新增'},
        {'key': 2, 'value': '失败需要重试'},
        {'key': 3, 'value': '已派发'},
        {'key': 4, 'value': '执行中'},
        {'key': 5, 'value': '成功'},
        {'key': 6, 'value': '失败不重试'}
      ]
    }
  },
  methods: {
    openDialog (dialog) {
      this[dialog].visible = true
      console.log(dialog)
      if (dialog === 'addDialog') {
        this.addData = {
          type: '',
          mutex: '',
          referId: 0,
          priority: 0,
          daemons: false,
          url: '',
          cron: '',
          dispatchType: 0,
          retry: true,
          status: 0
        }
      }
    },
    // submitAddForm(data){
    //   if (data == false){
    //     this.addData = false;
    //   } else {
    //     this.addData = Object.assign({}, data);
    //   }
    // },
    // beforeAdd(){
    //   this.$refs.addForm.submitForm();
    //   return this.addData;
    // },

    state (item, val) {
      let editData = Object.assign({}, item)
      editData.status = val// 启用1，禁用0
      apiRequest.put(this.urlOrigin.state + '/' + editData.id, {}, editData).then((data) => {
        this.$Dialog.success({
          content: '启用成功'
        })
        this.getList()
      })
    },
    // 启用
    enable (item) {
      this.state(item, 1)
    },
    viewException (item) {
      this.$Dialog.confirm({
        title: '异常详情',
        content: item.exception
      })
    }
    // beforeEditData(item){
    //   //editData need to be formatted, so copy item's properties
    //   this.editData = Object.assign({}, item);
    //   return this.editData;
    // },
    // submitEditForm(data){
    //   if (data==false) {
    //     this.editSubmitData = false;
    //   } else {
    //     this.editSubmitData = Object.assign({}, data);//false or formData
    //   }
    // },
    // beforeEdit(){
    //   this.$refs.editForm.submitForm();
    //   return this.editSubmitData;
    // }
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
