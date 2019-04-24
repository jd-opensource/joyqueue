<template>
  <div>
    <div class="ml20 mt30">
      <d-select v-model="searchData.type" placeholder="请选择类型" class="left mr5" style="width:213px">
        <span slot="prepend">主题类型</span>
        <d-option value="-1" >全部</d-option>
        <d-option value="0" >普通主题</d-option>
        <d-option value="1" >广播主题</d-option>
        <d-option value="2" >顺序主题</d-option>
      </d-select>
      <d-input v-model="searchData.keyword" placeholder="请输入英文名" class="left mr5" style="width:213px">
        <span slot="prepend">关键词</span>
      </d-input>
      <d-button type="primary" color="success" @click="getList">查询<icon name="search" style="margin-left: 5px;"></icon></d-button>
      <d-button v-if="$store.getters.isAdmin" type="primary" class="left ml10" @click="openDialog('addDialog')">添加主题<icon name="plus-circle" style="margin-left: 5px;"></icon></d-button>
    </div>
    <my-table :data="tableData" :showPin="showTablePin" :page="page" @on-size-change="handleSizeChange"
              @on-current-change="handleCurrentChange" @on-selection-change="handleSelectionChange" @on-view-detail="goDetail"
              @on-edit="edit" @on-edit-label="editLabel" @on-add-brokerGroup="addBrokerGroup" @on-del="del">
    </my-table>
    <!--添加-->
    <my-dialog :dialog="addDialog" class="add-dialog" @on-dialog-cancel="dialogCancel('addDialog')">
      <topic-form @on-dialog-cancel="dialogCancel('addDialog')" :type="$store.getters.addFormType"/>
    </my-dialog>
    <!--编辑-->
    <!--<my-dialog :dialog="editDialog" @on-dialog-confirm="editConfirm()" @on-dialog-cancel="dialogCancel('editDialog')">-->
      <!--<topic-form :data="editData" @on-dialog-cancel="dialogCancel('editDialog')"/>-->
    <!--</my-dialog>-->
    <!--编辑标签-->
    <my-dialog :dialog="editLabelDialog" @on-dialog-confirm="editLabelConfirm()" @on-dialog-cancel="editLabelCancel()">
        <grid-row class="mb10">
          <grid-col :span="4" class="label">业务分组:</grid-col>
          <grid-col :span="20" class="val">
            <d-select v-model="editLabelData.bs" class="left">
              <d-option value="sc">商城</d-option>
              <d-option value="jr">金融</d-option>
            </d-select>
          </grid-col>
        </grid-row>
    </my-dialog>
    <!--添加分组-->
    <my-dialog :dialog="addBrokerGroupDialog" @on-dialog-confirm="addBrokerGroupConfirm()" @on-dialog-cancel="addBrokerGroupCancel()">
      <add-brokerGroup :data="addBrokerGroupData" @on-choosed-brokerGroup="choosedBrokerGroup"></add-brokerGroup>
    </my-dialog>
  </div>
</template>

<script>
import apiRequest from '../../utils/apiRequest.js'
import apiUrl from '../../utils/apiUrl.js'
import myTable from '../../components/common/myTable.vue'
import myDialog from '../../components/common/myDialog.vue'
import topicForm from './topicForm.vue'
import crud from '../../mixins/crud.js'
import {baseBtnRender} from '../../utils/common.js'

export default {
  name: 'topic',
  components: {
    myTable,
    myDialog,
    topicForm
  },
  mixins: [ crud ],
  data () {
    return {
      searchData: {
        keyword: '',
        command: 1
      },
      tableData: {
        rowData: [],
        colData: [
          {
            title: 'ID',
            key: 'id'
          },
          {
            title: '英文名',
            key: 'code',
            render: (h, params) => {
              return h('label', {
                style: {
                  cursor: 'pointer',
                  color: '#3366FF'
                },
                on: {
                  click: () => {
                    this.$router.push({
                      name: `/${this.$i18n.locale}/topic/detail`,
                      query: {id: params.item.id, code: params.item.code, namespaceId: params.item.namespace.id, namespaceCode: params.item.namespace.code}})
                  }
                }
              }, params.item.code)
            }
          },
          // {
          //   title:'命名空间',
          //   key: 'namespace.code'
          // },
          {
            title: '类型',
            key: 'type',
            render: (h, params) => {
              return baseBtnRender(h, params.item.type, [
                {
                  value: 0,
                  txt: 'Normal',
                  color: 'success'
                },
                {
                  value: 1,
                  txt: 'Broadcast',
                  color: 'warning'
                },
                {
                  value: 2,
                  txt: 'Sequential',
                  color: 'danger'
                }
              ])
            }
          },
          {
            title: '队列数',
            key: 'partitions'
          },
          {
            title: '归档',
            key: 'archive',
            render: (h, params) => {
              let txt = !params.item.archive ? '已关闭' : '已开启'
              let color = !params.item.archive ? 'warning' : 'success'
              return h('DButton', {
                props: {
                  size: 'small',
                  borderless: true,
                  color: color
                }
              }, txt)
            }
          }
          // {
          //   title:'标签',
          //   key: 'labels'
          // },
          // {
          //   title:'备注',
          //   key: 'description'
          // },
        ],
        btns: [
          {
            txt: '详情',
            method: 'on-view-detail'
          },
          // {
          //   txt: '编辑',
          //   method: 'on-edit'
          // },
          // {
          //   txt: '编辑标签',
          //   method: 'on-edit-label'
          // },
          // {
          //   txt: '添加分组',
          //   method: 'on-add-brokerGroup'
          // },
          {
            txt: '删除',
            method: 'on-del',
            isAdmin: true
          }
        ]
      },
      addDialog: {
        visible: false,
        title: '添加主题',
        width: 700,
        showFooter: false
      },
      addData: {},
      editDialog: {
        visible: false,
        title: '编辑主题',
        width: 700,
        showFooter: true
      },
      editData: {},
      editLabelDialog: {
        visible: false,
        title: '编辑主题标签',
        width: 700,
        showFooter: true
      },
      editLabelData: {},
      addBrokerGroupDialog: {
        visible: false,
        title: '添加分组',
        width: 700,
        showFooter: true
      },
      addBrokerGroupData: {}
    }
  },
  computed: {
  },
  methods: {
    goDetail (item) {
      this.$router.push({name: `/${this.$i18n.locale}/topic/detail`,
        query: {id: item.id, code: item.code, namespaceId: item.namespace.id, namespaceCode: item.namespace.code, tab: 'producer'}})
    },
    editLabel (item) {
      this.openDialog('editLabelDialog')
      apiRequest.get(apiUrl['/topic'].editLabelData + '/' + item.id).then((data) => {

      })
      this.editLabelData = item
    },
    editLabelConfirm () {
      let data = {
        id: this.editLabelData.id,
        bs: this.editLabelData.bs,
        labels: [{'bs': this.editLabelData.bs}]
      }
      apiRequest.post(this.urlOrigin.editLabel, {}, data).then((data) => {
        this.editLabelDialog.visible = false
        this.$Dialog.success({
          content: '修改成功'
        })
        this.getList()
      })
    },
    editLabelCancel () {
      this.editLabelDialog.visible = false
    },
    addBrokerGroup (item) {
      this.openDialog('addBrokerGroupDialog')
      this.addBrokerGroupData = item
      this.addBrokerGroupData.brokerGroupsAdd = []
    },
    addBrokerGroupConfirm () {
      let addBrokerGroupData = this.addBrokerGroupData
      apiRequest.post(this.urlOrigin.addBrokerGroup, {}, addBrokerGroupData).then((data) => {
        this.addBrokerGroupDialog.visible = false
        this.$Dialog.success({
          content: '添加成功'
        })
        this.getList()
      })
    },
    addBrokerGroupCancel () {
      this.addBrokerGroupDialog.visible = false
    },
    choosedBrokerGroup (val) {
      this.addBrokerGroupData.brokerGroupsAdd = val
    },
    isAdmin (item) {
      return this.$store.getters.isAdmin
    },
    // 删除
    del (item, index) {
      let _this = this
      this.$Dialog.confirm({
        title: '提示',
        content: '删除时会自动删除与该主题关联的分片分组信息，确定要删除吗？'
      }).then(() => {
        apiRequest.post(_this.urlOrigin.del, {}, item.id).then((data) => {
          if (data.code !== this.$store.getters.successCode) {
            this.$Dialog.error({
              content: '删除失败'
            })
          } else {
            this.$Message.success('删除成功')
            if (typeof (_this.afterDel) === 'function') {
              _this.afterDel(item)
            }
            _this.getList()
          }
        })
      }).catch(() => {
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
.add-dialog{
  overflow: hidden;
}
.show-enter-active,.show-leave-active{
  transition:all 0.5s;
}
.show-enter{
  margin-right: 100px;
}
.show-leave-to{
  margin-left: -100px;
}
.show-enter-to{
  margin-right: 100px;
}
.show-leave{
  margin-left: 100px;
}
.hint{color: #f00;}
.star{color: #f00;}
</style>
