<template>
  <div>
    <div class="ml20 mt30">
      <d-input v-model="searchData.keyword" :placeholder="langConfig.placeholder1" class="left mr10" style="width: 12%">
        <icon name="search" size="14" color="#CACACA" slot="suffix" @click="getList"></icon>
      </d-input>
      <d-button type="primary" @click="openSyncDialog">{{langConfig.syncApp}}<icon name="download" style="margin-left: 5px;"></icon></d-button>
      <!--<d-button type="primary" @click="openNewDialog" v-if="$store.getters.isAdmin">新建应用<icon name="plus-circle" style="margin-left: 5px;"></icon></d-button>-->
      <!--<d-button type="primary" @click="openNewDialog">新建应用<icon name="plus-circle" style="margin-left: 5px;"></icon></d-button>-->
    </div>
    <my-table :data="tableData" :showPin="showTablePin" :page="page" @on-size-change="handleSizeChange" @on-current-change="handleCurrentChange"
              @on-selection-change="handleSelectionChange" @on-del="del" @on-view-detail="goDetail" >
    </my-table>

    <!--同步弹出框-->
    <my-dialog :dialog="syncDialog" @on-dialog-confirm="syncConfirm('syncForm')" @on-dialog-cancel="syncCancel()">
      <d-form ref="syncForm" :model="syncDialogData" :error="syncDialog.error.code" :rules="syncDialog.rules" label-width="100px">
        <d-form-item label="英文名称：" prop="code">
          <d-input placeholder="仅支持英文字母大小写和数字，首字母" v-model="syncDialogData.code" style="width: 80%"/>
        </d-form-item>
        <d-form-item label="应用来源:" v-if="!isNew">
          <grid-row>
            <grid-col :span="6" prop="aliasCode">

              <d-select v-model.number="syncDialogData.source" placeholder="来源" value="1" style="width: 100%"
                        @on-change="change()">
                <d-option :value="1">JONE</d-option>
                <d-option :value="2">JDOS</d-option>
                <d-option :value="3">SURE</d-option>
                <d-option :value="0">其他来源</d-option>
              </d-select>
            </grid-col>
            <grid-col class="line" :span="1"/>
            <grid-col :span="12">
              <d-input v-if="!isOthers" placeholder="应用代码" v-model="syncDialogData.aliasCode" style="width: 100%"></d-input>
              <d-input v-else-if="$store.getters.isAdmin || $store.getters.isTest" placeholder="负责人erp" v-model="syncDialogData.owner.code" style="width: 100%"></d-input>
              <d-input v-else placeholder="请联系管理员jmq_support" disabled v-model="syncDialogData.owner.code" style="width: 100%"></d-input>
            </grid-col>
          </grid-row>
        </d-form-item>
      </d-form>
    </my-dialog>
  </div>
</template>

<script>
import apiRequest from '../../utils/apiRequest.js'
import myTable from '../../components/common/myTable.vue'
import myDialog from '../../components/common/myDialog.vue'
import crud from '../../mixins/crud.js'
import {getCodeRule2} from '../../utils/common.js'
import viewLang from '../../i18n/views.json'

export default {
  name: 'application',
  components: {
    myTable,
    myDialog
    // subscribe
  },
  mixins: [ crud ],
  data () {
    const curLangConfig = viewLang[this.$i18n.locale]['application']
    return {
      searchData: {
        keyword: ''
      },
      searchRules: {
      },
      isOthers: false, // 其他来源
      isNew: false,
      tableData: {
        rowData: [],
        colData: [
          {
            title: curLangConfig['colData']['id'],
            key: 'id'
          },
          {
            title: curLangConfig['colData']['code'],
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
                      name: `/${this.curLang}/application/detail`,
                      query: {id: params.item.id, code: params.item.code}
                    })
                  }
                }
              }, params.item.code)
            }
          },
          {
            title: curLangConfig['colData']['department'],
            key: 'department'
          },
          {
            title: curLangConfig['colData']['ownerCode'],
            key: 'owner.code'
          },
          {
            title: curLangConfig['colData']['system'],
            key: 'system',
            formatter (row) {
              let sourceName = ''
              switch (row.source) {
                case 1:
                  sourceName = 'JONE'
                  break
                case 2:
                  sourceName = 'JDOS'
                  break
                case 3:
                  sourceName = 'SURE'
                  break
                default:
                  sourceName = '其他来源'
                  break
              }
              return sourceName + '/' + row.system + '/' + row.aliasCode
            }
          }
        ],
        btns: [
          {
            txt: curLangConfig['colData']['btnDetail'],
            method: 'on-view-detail'
          },
          {
            txt: curLangConfig['colData']['btnDelete'],
            method: 'on-del'
          }
        ]
      },
      syncDialog: {
        visible: false,
        title: '同步应用',
        showFooter: true,
        rules: {
          code: getCodeRule2(),
          source: [
            { type: 'number', required: true, message: '请选择应用来源', trigger: 'change' }
          ],
          aliasCode: [
            { required: true, message: '请选择来源应用代码', trigger: 'change' }
          ]
        },
        error: {
          code: ''
        }
      },
      syncDialogData: {
        aliasCode: '',
        source: 1,
        sourceName: '',
        code: '',
        owner: {
          code: ''
        }
      }
    }
  },
  computed: {
    syncUrl () {
      return this.urlOrigin.sync
    },
    curLang () {
      return this.$i18n.locale
    },
    langConfig () {
      return viewLang[this.curLang]['application']
    }
  },
  watch: {
    curLang () {
      this.tableData = {
        rowData: [],
        colData: [
          {
            title: this.langConfig['colData']['id'],
            key: 'id'
          },
          {
            title: this.langConfig['colData']['code'],
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
                      name: `/${this.curLang}/application/detail`,
                      query: {id: params.item.id, code: params.item.code}
                    })
                  }
                }
              }, params.item.code)
            }
          },
          {
            title: this.langConfig['colData']['department'],
            key: 'department'
          },
          {
            title: this.langConfig['colData']['ownerCode'],
            key: 'owner.code'
          },
          {
            title: this.langConfig['colData']['system'],
            key: 'system',
            formatter (row) {
              let sourceName = ''
              switch (row.source) {
                case 1:
                  sourceName = 'JONE'
                  break
                case 2:
                  sourceName = 'JDOS'
                  break
                case 2:
                  sourceName = 'SURE'
                  break
                default:
                  sourceName = '其他来源'
                  break
              }
              return sourceName + '/' + row.system + '/' + row.aliasCode
            }
          }
        ],
        btns: [
          {
            txt: this.langConfig['colData']['btnDetail'],
            method: 'on-view-detail'
          },
          {
            txt: this.langConfig['colData']['btnDelete'],
            method: 'on-del'
          }
        ]
      }
    }
  },
  methods: {
    // 删除
    del (item, index) {
      let _this = this
      this.$Dialog.confirm({
        title: '提示',
        content: '删除时会自动将关联的用户和令牌删除，确定要删除吗？'
      }).then(() => {
        apiRequest.delete(_this.urlOrigin.del + '/' + item.id).then((data) => {
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
    },
    change () {
      if (this.syncDialogData.source === 0) {
        this.isOthers = true
      } else {
        this.isOthers = false
      }
    },
    goDetail (item) {
      this.$router.push({
        name: `/${this.curLang}/application/detail`,
        query: {id: item.id, code: item.code}})
    },
    resetSyncDialog () {
      this.syncDialogData.code = ''
      this.syncDialogData.source = 1
      this.syncDialogData.sourceName = ''
      this.syncDialogData.aliasCode = ''
      this.syncDialogData.owner.code = ''
    },
    openNewDialog () {
      this.resetSyncDialog()
      this.isNew = true
      this.syncDialog.visible = true
      this.syncDialogData.source = 0
      this.syncDialogData.sourceName = '其他来源'
      this.syncDialog.title = '新建应用'
    },
    openSyncDialog () {
      this.resetSyncDialog()
      this.isOthers = false
      this.isNew = false
      this.syncDialog.visible = true
      this.syncDialog.source = 1
      this.syncDialog.title = '同步应用'
    },
    syncConfirm (formName) {
      if (this.isOthers && this.syncDialogData.owner.code === '') {
        this.$Message.error('新建应用,请咚咚联系jmq-support!')
        return
      }
      // validate
      this.$refs[formName].validate((valid) => {
        if (valid) {
          // post第二个参数param，第三个参数data
          apiRequest.post(this.syncUrl, {}, this.syncDialogData).then(() => {
            this.syncDialog.visible = false
            this.getList()
          })
        } else {
          this.$Message.error('验证不通过！')
          return false
        }
      })
    },
    syncCancel () {
      this.syncDialog.visible = false
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
