<template>
  <div>
    <div class="ml20 mt30">
      <d-input v-model="searchData.keyword" :placeholder="langConfig.placeholder1" class="left mr10" style="width:213px">
        <icon name="search" size="14" color="#CACACA" slot="suffix" @click="getList"></icon>
      </d-input>
      <d-button type="primary" @click="openDialog('addDialog')">新建应用<icon name="plus-circle" style="margin-left: 5px;"></icon></d-button>
    </div>
    <my-table :data="tableData" :showPin="showTablePin" :page="page" @on-size-change="handleSizeChange" @on-current-change="handleCurrentChange"
              @on-selection-change="handleSelectionChange" @on-del="del" @on-view-detail="goDetail" >
    </my-table>

    <!--新建弹出框-->
    <my-dialog :dialog="addDialog" @on-dialog-confirm="addConfirm()" @on-dialog-cancel="addCancel('addDialog')">
      <app-add-form ref="addForm" :type="$store.getters.addFormType" />
    </my-dialog>
  </div>
</template>

<script>
import apiRequest from '../../utils/apiRequest.js'
import myTable from '../../components/common/myTable.vue'
import myDialog from '../../components/common/myDialog.vue'
import appAddForm from './appAddForm.vue'
import crud from '../../mixins/crud.js'
import viewLang from '../../i18n/views.json'

export default {
  name: 'application',
  components: {
    myTable,
    myDialog,
    appAddForm
  },
  mixins: [ crud ],
  data () {
    const curLangConfig = viewLang[this.$i18n.locale]['application']
    return {
      searchData: {
        keyword: ''
      },
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
      addDialog: {
        visible: false,
        title: '新建应用',
        showFooter: true
      }
    }
  },
  computed: {
    addUrl () {
      return this.urlOrigin.add
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
    goDetail (item) {
      this.$router.push({
        name: `/${this.curLang}/application/detail`,
        query: {id: item.id, code: item.code}})
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
