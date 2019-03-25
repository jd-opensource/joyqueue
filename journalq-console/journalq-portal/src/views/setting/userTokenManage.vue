<template>
  <div>
    <div class="ml20 mt30">
      <d-input v-model="searchData.keyword" placeholder="请输入erp" class="left mr10" style="width: 20%">
        <icon name="search" size="14" color="#CACACA" slot="suffix" @click="getList"></icon>
      </d-input>
    </div>

    <my-table :data="tableData" :showPin="showTablePin" :page="page" @on-size-change="handleSizeChange" @on-current-change="handleCurrentChange"
              @on-selection-change="handleSelectionChange" @on-edit="onEdit" @on-del="del">
    </my-table>

    <!--角色修改-->
    <my-dialog :dialog="editDialog" @on-dialog-confirm="editConfirm()" @on-dialog-cancel="editCancel()">
      <api-token ref="editForm" :data="editData"></api-token>
    </my-dialog>
  </div>
</template>

<script>

import myTable from '../../components/common/myTable.vue'
import myDialog from '../../components/common/myDialog.vue'
import crud from '../../mixins/crud.js'
import apiToken from '../workflow/apiToken.vue'
import {timeStampToString} from '../../utils/dateTimeUtils'

export default {
  name: 'userToken',
  components: {
    myTable,
    myDialog,
    apiToken
  },
  mixins: [ crud ],
  data () {
    return {
      searchData: {
        keyword: ''
      },
      urls: {
        search: '/user/userToken/search',
        del:'/user/userToken/delete'
      },
      searchRules: {
      },
      tableData: {
        rowData: [],
        colData: [
          {
            title: 'erp',
            key: 'code'
          },
          {
            title: '令牌',
            key: 'token'
          },
          {
            title: '过期时间',
            key: 'expireTime',
            formatter (item) {
              return timeStampToString(item.createTime)
            }
          }
        ],
        btns: [
          {
            txt: '编辑  ',
            method: 'on-edit'

          },
          {
            txt: '删除  ',
            method: 'on-del'

          }
        ]
      },
      multipleSelection: [],
      editDialog: {
        visible: false,
        title: '令牌编辑',
        showFooter: false
      },
      editData:{
        code:'',
        token:'',
        expireTime:'',
      }
    }
  },
  methods: {
    onEdit(item){
      this.editData=item;
      this.openDialog('editDialog');
    },
    openDialog (dialog) {
      this[dialog].visible = true;
      this.addData.code = '';
    },
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
