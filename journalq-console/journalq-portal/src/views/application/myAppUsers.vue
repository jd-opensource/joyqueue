<template>
  <div>
    <div class="ml20 mt30">
      <d-input v-model="searchData.keyword" placeholder="请输入Erp或中文名" class="input" @on-enter="getList">
        <span slot="prepend">关键词</span>
        <icon name="search" size="14" color="#CACACA" slot="suffix" @click="getList"></icon>
      </d-input>
      <d-button-group>
        <d-button @click="openDialog('addDialog')" class="button">添加
          <icon name="plus-circle" style="margin-left: 3px;"></icon>
        </d-button>
        <d-button type="primary" @click="getList" class="button">刷新
          <icon name="refresh-cw" style="margin-left: 3px;"></icon>
        </d-button>
      </d-button-group>
    </div>
    <my-table :data="tableData" :showPin="showTablePin" :page="page" @on-size-change="handleSizeChange"
              @on-current-change="handleCurrentChange" @on-selection-change="handleSelectionChange">
    </my-table>

    <!--添加用户-->
    <my-dialog :dialog="addDialog" @on-dialog-confirm="addConfirm()" @on-dialog-cancel="addCancel()">
        <grid-row class="mb10">
          <grid-col :span="3" class="label">英文名:</grid-col>
          <grid-col :span="1"/>
          <grid-col :span="14" class="val">
            <d-input v-model="addData.user.code"></d-input>
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
  name: 'application',
  components: {
    myTable,
    myDialog
  },
  mixins: [ crud ],
  data () {
    return {
      urls: {
        search: `/application/${this.$route.query.id}/user/search`,
        add: `/application/${this.$route.query.id}/user/add`
      },
      searchData: {
        keyword: ''
      },
      tableData: {
        rowData: [],
        colData: [
          {
            title: 'erp',
            key: 'code'
          },
          {
            title: '中文名',
            key: 'name'
          },
          {
            title: '所属部门',
            key: 'orgName'
          },
          {
            title: '邮箱',
            key: 'email'
          },
          {
            title: '手机号',
            key: 'mobile'
          }
        ]
      },
      multipleSelection: [],
      addDialog: {
        visible: false,
        title: '添加用户',
        showFooter: true
      },
      addData: {
        user: {
          code: ''
        }
      }
    }
  },
  computed: {
  },
  methods: {
  },
  // watch: {
  //   '$route' (to, from) {
  //     if (to.query.tab === 'myAppUsers') {
  //       this.getList()
  //     }
  //   }
  // },
  mounted () {
    // this.getList();
  }
}
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>
.label{text-align: right; line-height: 32px;}
.val{}
</style>
