<template>
  <div>
    <div class="ml20 mt30">
      <d-input v-model="searchData.topic" placeholder="队列名" class="left mr10" style="width: 15%">
        <span slot="prepend">队列名</span>
      </d-input>
      <d-input v-model="searchData.app" placeholder="应用代码" class="left mr10" style="width: 15%">
        <span slot="prepend">应用代码</span>
      </d-input>

      <d-select v-model="searchData.userType" placeholder="请选择报警对象" class="left" style="width:12%">
        <d-option :value="-1" >全部</d-option>
        <d-option :value="0" >管理员</d-option>
        <d-option :value="1" >普通用户</d-option>
      </d-select>
      <d-button type="primary" @click="getList">查询<icon name="search" style="margin-left: 5px;"></icon></d-button>
      <d-button type="primary" @click="openDialog('addDialog', 'addForm')">新建报警规则<icon name="plus-circle" style="margin-left: 5px;"></icon></d-button>
    </div>
    <my-table :data="tableData" :page="page" @on-size-change="handleSizeChange" @on-current-change="handleCurrentChange"
              @on-selection-change="handleSelectionChange" @on-edit="edit" @on-del="del">
    </my-table>

    <!--新建-->
    <my-dialog :dialog="addDialog" @on-dialog-confirm="addConfirm()" @on-dialog-cancel="dialogCancel('addDialog')">
      <alarm-rule-form ref="addForm" :type="$store.getters.addFormType"/>
    </my-dialog>
    <!--编辑-->
    <my-dialog :dialog="editDialog" @on-dialog-confirm="editConfirm()" @on-dialog-cancel="dialogCancel('editDialog')">
      <alarm-rule-form ref="editForm" :data="editData" :appList="appList" :topicList="topicList"
                       :type="$store.getters.editFormType"/>
    </my-dialog>
  </div>
</template>

<script>
import apiRequest from '../../utils/apiRequest.js'
import myTable from '../../components/common/myTable.vue'
import myDialog from '../../components/common/myDialog.vue'
import alarmRuleForm from './alarmRuleForm.vue'
import crud from '../../mixins/crud.js'

export default {
  name: 'alarm-rule',
  components: {
    myTable,
    myDialog,
    alarmRuleForm
  },
  mixins: [ crud ],
  data () {
    return {
      searchData: {
        keyword:  ''
      },
      searchRules:{
      },
      tableData:{
        rowData: [],
        colData: [
          {
            title:'类型',
            key: 'type'
          },
          {
            title:'级别',
            key: 'alarmLevel'
          },
          {
            title:'用户',
            key: 'userType'
          },
          {
            title:'报警方式',
            key: 'alarmMode'
          },
          {
            title:'主题',
            key: 'topic'
          },
          {
            title:'应用',
            key: 'app'
          },
          {
            title:'主机',
            key: 'host'
          },
          {
            title:'阈值',
            key: 'threshold'
          },
          {
            title:'检测时间',
            key: 'duration'
          },
          {
            title:'检测次数',
            key: 'detectTimes'
          },
          {
            title:'报警间隔',
            key: 'alarmInterval'
          }
        ],
        // 表格操作，如果需要根据特定值隐藏显示， 设置bindKey对应的属性名和bindVal对应的属性值
        btns: [
          // {
          //   txt: '禁用',
          //   method: 'on-disable',
          //   bindKey: 'status',
          //   bindVal: '1'
          // },
          // {
          //   txt: '启用',
          //   method: 'on-enable',
          //   bindKey: 'status',
          //   bindVal: '0'
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
      multipleSelection: [],
      appList: [
        {
          id: 1,
          code: 'cyy'
        },
        {
          id: 2,
          code: 'cyy2'
        }
        // todo 待完善,请查询
      ],
      topicList: [
        {
          id: 1,
          code: 'cyyt1'
        },
        {
          id: 2,
          code: 'cyyt2'
        }
        // todo 待完善,请查询
      ],
      addDialog: {
        visible: false,
        title: '新建报警策略',
        showFooter: true,
        scrollable: true,
        width: 560
      },
      addData: {},
      // addSubmitData: {},
      editDialog: {
        visible: false,
        title: '编辑报警策略',
        showFooter: true
      },
      editData: {}
      // editSubmitData: {}
    }
  },
  methods: {
    openDialog (dialogName) {
      this[dialogName].visible = true
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
