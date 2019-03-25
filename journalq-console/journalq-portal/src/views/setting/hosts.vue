<template>
  <div>
    <div class="ml20 mt30">
      <d-input v-model="searchData.ip" placeholder="请输入IP" class="left mr10" style="width: 20%">
        <icon name="search" size="14" color="#CACACA" slot="suffix" @click="getList"></icon>
      </d-input>
      <d-button type="primary" @click="openDialog('addDialog')">新建主机<icon name="plus-circle" style="margin-left: 5px;"></icon></d-button>
      <!--<d-button type="primary" @click="generateBroker">生成Broker<icon name="plus-circle" style="margin-left: 5px;"></icon></d-button>-->
      <d-button type="primary" @click="syncHosts">同步主机<icon name="download" style="margin-left: 5px;"></icon></d-button>
    </div>
    <my-table :optional="true" :data="tableData" :showPin="showTablePin" :page="page" @on-size-change="handleSizeChange"
              @on-current-change="handleCurrentChange" @on-selection-change="handleSelectionChange"
              @on-select-all="handleSelectionAll" @on-edit="edit" @on-del="del">
    </my-table>

    <my-dialog :dialog="addDialog" @on-dialog-confirm="submitForm('addData')" @on-dialog-cancel="addCancel()">
      <d-form :model="addData" :rules="rules" ref="addData" label-width="120px" class="demo-ruleForm">
        <d-form-item label="ip:" prop="ip" style="width: 60%">
          <d-input v-model="addData.ip"></d-input>
        </d-form-item>
        <d-form-item label="品牌:" prop="vendor" style="width: 60%">
          <d-input v-model="addData.vendor"></d-input>
        </d-form-item>
        <d-form-item label="cpu核数:" prop="cpuCapacity" style="width: 60%">
          <d-input v-model="addData.cpuCapacity"></d-input>
        </d-form-item>
        <d-form-item label="硬盘容量:" prop="diskCapacity" style="width: 60%">
          <d-input v-model="addData.diskCapacity"></d-input>
        </d-form-item>
        <d-form-item label="内存容量:" prop="memCapacity" style="width: 60%">
          <d-input v-model="addData.memCapacity"></d-input>
        </d-form-item>
        <!--<d-form-item>-->
          <!--<d-button type="primary" @click="submitForm('addData')">确定</d-button>-->
          <!--<d-button @click="resetForm('addData')">重置</d-button>-->
        <!--</d-form-item>-->
      </d-form>
    </my-dialog>

    <my-dialog :dialog="editDialog" @on-dialog-confirm="submitForm('editData')" @on-dialog-cancel="editCancel()">
      <d-form :model="editData" :rules="rules" ref="editData" label-width="120px" class="demo-ruleForm">
        <d-form-item label="ip:" prop="ip" style="width: 60%">
          <d-input v-model="editData.ip"></d-input>
        </d-form-item>
        <d-form-item label="品牌:" prop="vendor" style="width: 60%">
          <d-input v-model="editData.vendor"></d-input>
        </d-form-item>
        <d-form-item label="cpu核数:" prop="cpuCapacity" style="width: 60%">
          <d-input v-model="editData.cpuCapacity"></d-input>
        </d-form-item>
        <d-form-item label="硬盘容量:" prop="diskCapacity" style="width: 60%">
          <d-input v-model="editData.diskCapacity"></d-input>
        </d-form-item>
        <d-form-item label="内存容量:" prop="memCapacity" style="width: 60%">
          <d-input v-model="editData.memCapacity"></d-input>
        </d-form-item>
        <!--<d-form-item>-->
          <!--<d-button type="primary" @click="submitForm('editData')">确定</d-button>-->
          <!--<d-button @click="resetForm('editData')">恢复</d-button>-->
        <!--</d-form-item>-->
      </d-form>
    </my-dialog>

    <!--新建主机-->
    <!--<my-dialog :dialog="addDialog" @on-dialog-confirm="addConfirm()" @on-dialog-cancel="addCancel()">-->
      <!--<grid-row class="mb10">-->
        <!--<grid-col :span="8" class="label">ip:</grid-col>-->
        <!--<grid-col :span="16" class="val">-->
          <!--<d-input v-model="addData.ip"></d-input>-->
        <!--</grid-col>-->
      <!--</grid-row>-->
      <!--<grid-row class="mb10">-->
        <!--<grid-col :span="8" class="label">品牌:</grid-col>-->
        <!--<grid-col :span="16" class="val">-->
          <!--<d-input v-model="addData.vendor"></d-input>-->
        <!--</grid-col>-->
      <!--</grid-row>-->
      <!--<grid-row class="mb10">-->
        <!--<grid-col :span="8" class="label">cpu核数:</grid-col>-->
        <!--<grid-col :span="16" class="val">-->
          <!--<d-input v-model="addData.cpuCapacity"></d-input>-->
        <!--</grid-col>-->
      <!--</grid-row>-->
      <!--<grid-row class="mb10">-->
        <!--<grid-col :span="8" class="label">硬盘容量:</grid-col>-->
        <!--<grid-col :span="16" class="val">-->
          <!--<d-input v-model="addData.diskCapacity"></d-input>-->
        <!--</grid-col>-->
      <!--</grid-row>-->
      <!--<grid-row class="mb10">-->
        <!--<grid-col :span="8" class="label">内存容量:</grid-col>-->
        <!--<grid-col :span="16" class="val">-->
          <!--<d-input v-model="addData.memCapacity"></d-input>-->
        <!--</grid-col>-->
      <!--</grid-row>-->
    <!--</my-dialog>-->
    <!--编辑主机-->
    <!--<my-dialog :dialog="editDialog" @on-dialog-confirm="editConfirm()" @on-dialog-cancel="editCancel()">-->
      <!--<grid-row class="mb10">-->
        <!--<grid-col :span="8" class="label">ip:</grid-col>-->
        <!--<grid-col :span="16" class="val">-->
          <!--<d-input v-model="editData.ip"></d-input>-->
        <!--</grid-col>-->
      <!--</grid-row>-->
      <!--<grid-row class="mb10">-->
        <!--<grid-col :span="8" class="label">品牌:</grid-col>-->
        <!--<grid-col :span="16" class="val">-->
          <!--<d-input v-model="editData.vendor"></d-input>-->
        <!--</grid-col>-->
      <!--</grid-row>-->
      <!--<grid-row class="mb10">-->
        <!--<grid-col :span="8" class="label">cpu核数:</grid-col>-->
        <!--<grid-col :span="16" class="val">-->
          <!--<d-input v-model="editData.cpuCapacity"></d-input>-->
        <!--</grid-col>-->
      <!--</grid-row>-->
      <!--<grid-row class="mb10">-->
        <!--<grid-col :span="8" class="label">硬盘容量:</grid-col>-->
        <!--<grid-col :span="16" class="val">-->
          <!--<d-input v-model="editData.diskCapacity"></d-input>-->
        <!--</grid-col>-->
      <!--</grid-row>-->
      <!--<grid-row class="mb10">-->
        <!--<grid-col :span="8" class="label">内存容量:</grid-col>-->
        <!--<grid-col :span="16" class="val">-->
          <!--<d-input v-model="editData.memCapacity"></d-input>-->
        <!--</grid-col>-->
      <!--</grid-row>-->
    <!--</my-dialog>-->
  </div>
</template>

<script>
import apiRequest from '../../utils/apiRequest.js'
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
      searchData: {
        ip: ''
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
            title: 'IP',
            key: 'ip'
          },
          {
            title: '机架',
            key: 'rack'
          },
          {
            title: '交换机',
            key: 'switchboard'
          },
          {
            title: '品牌',
            key: 'vendor'
          },
          {
            title: 'cpu核数',
            key: 'cpuCapacity'
          },
          {
            title: '磁盘容量',
            key: 'diskCapacity'
          },
          {
            title: '内存容量',
            key: 'memCapacity'
          },
          {
            title: '标签',
            key: 'labels'
          },
          {
            title: '状态',
            key: 'status',
            render: (h, params) => {
              var txt = ''
              let color = ''
              switch (params.item.status) {
                case 0:
                  txt = '禁用'
                  color = 'error'
                  break
                case 1:
                  txt = '启用'
                  color = 'warning'
                  break
                case 2:
                  txt = '已生成Broker'
                  color = 'success'
                  break
              }
              return h('DButton', {
                props: {
                  size: 'small',
                  borderless: true,
                  color: color
                }
              }, txt)
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
          // {
          //   text: '编辑标签',
          //   method: 'on-editTag'
          // }
        ]
      },
      multipleSelection: [],
      addDialog: {
        visible: false,
        title: '新建主机',
        showFooter: true
      },
      addData: {
        ip: '',
        vendor: '',
        serverType: '',
        cpuCapacity: '',
        diskType: '',
        memCapacity: '',
        diskCapacity: ''
      },
      editDialog: {
        visible: false,
        title: '编辑主机',
        showFooter: true
      },
      editData: {},
      rules: {
        ip: [{required: true, message: '请填写ip', trigger: 'change'}],
        vendor: [{required: true, message: '请填写品牌', trigger: 'change'}],
        memCapacity: [{required: true, message: '请填写内存容量', trigger: 'change'}],
        cpuCapacity: [{required: true, message: '请填写cpu核数', trigger: 'change'}],
        diskCapacity: [{ required: true, message: '请填写磁盘容量', trigger: 'change' }]
      }
    }
  },
  methods: {
    openDialog (dialog) {
      this[dialog].visible = true
    },
    // goDetail(item){
    //   this.$router.push({name: '/setting/hosts/detail', query:{id: item.id, name:item.code}});
    // },
    generateBroker () {
      let editData = this.multipleSelection
      if (!editData) {
        this.$Dialog.warning({
          content: '请先选择数据！'
        })
        return
      }
      var ids = ''
      for (var i = 0; i < editData.length; i++) {
        if (editData[i].status === 2) {
          this.$Dialog.warning({
            content: 'ID为[' + editData[i].id + ']的数据已经生成Broker，请重新选择！'
          })
          return
        }
        if (i === 0) {
          ids = editData[i].id
        } else {
          ids = ids + ',' + editData[i].id
        }
      }
      apiRequest.put(this.urlOrigin.generateBroker + '/' + ids, {}, editData).then((data) => {
        this.editDialog.visible = false
        this.$Dialog.success({
          content: '生成成功'
        })
        this.getList()
      })
    },
    syncHosts () {
      apiRequest.put(this.urlOrigin.sync, {}, {}).then((data) => {
        this.$Dialog.success({
          content: '同步成功'
        })
        this.getList()
      })
    },
    submitForm (formName) {
      this.$refs[formName].validate((valid) => {
        if (valid) {
          if (formName === 'addData') {
            this.addConfirm()
          } else {
            if (formName === 'editData') {
              this.editConfirm()
            }
          }
          this.getList()
          // this.$Message.info('提交成功!');
        } else {
          this.$Message.error('参数异常')
          return false
        }
      })
    },
    beforeEdit () {
      return new Promise((resolve, reject) => {
        resolve({
          id: this.editData.id,
          ip: this.editData.ip,
          vendor: this.editData.vendor,
          cpuCapacity: this.editData.cpuCapacity,
          diskCapacity: this.editData.diskCapacity,
          memCapacity: this.editData.memCapacity
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
