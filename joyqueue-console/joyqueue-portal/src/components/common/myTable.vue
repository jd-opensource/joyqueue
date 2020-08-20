<template>
  <div class="table">
    <d-table class="tablePadding"
             :optional="optional"
             :columns="ColOpData"
             :data="rowData"
             :height="height"
             @row-click="handleRowClick"
             @on-selection-change="handleSelectionChange"
             @on-select-all="handleSelectionAll">
      <d-table-column v-if="data.selectable"
                      type="selection"
                      width="55">
      </d-table-column>
    </d-table>
    <div class="blank" :style="blankStyle"></div>
    <d-pagination  class="right mr20"
                   v-if="showPagination"
                   show-total show-sizer show-quickjump
                   :page-size-opts="[10, 20, 50, 100]"
                   :current="page.page"
                   :page-size="page.size"
                   :total="page.total"
                   @pagesize-change="handleSizeChange"
                   @page-change="handleCurrentChange">
    </d-pagination>
    <d-spin fix size="large" v-if="showPin"/>
  </div>
</template>

<script>
import viewLang from '../../i18n/views.json'
import {deepCopy} from '../../utils/assist.js'

export default {
  name: 'myTable',
  props: {
    optional: {
      type: Boolean,
      default: false
    },
    showPagination: {
      type: Boolean,
      default: true
    },
    showPin: {
      type: Boolean,
      default: false
    },
    height: {
      type: String,
      default: ''
    },
    data: {
      rowData: [],
      colData: [],
      inputs: [],
      btns: [],
      operates: [],
      btnGroups: []
    },
    operationColumnWidth: {
      type: Number
    },
    page: {
      page: 1,
      size: 10,
      total: 100
    }
  },
  data () {
    return {
    }
  },
  computed: {
    curLang () {
      return this.$i18n.locale
    },
    langConfig () {
      return viewLang[this.curLang]['common']
    },
    rowData () {
      return this.data.rowData
    },
    ColOpData () {
      let inputsOrigin = this.data.inputs || []
      let btnsOrigin = this.data.btns || []
      let operatesOrigin = this.data.operates || []
      let btnGroupsOrigin = this.data.btnGroups || {}
      let colOpData = deepCopy(this.data.colData) || []
      // 处理input的
      for (let i = 0; i < inputsOrigin.length; i++) {
        let title = inputsOrigin[i].title
        colOpData.push({
          title: title,
          render: (h, params) => {
            let vm = this
            let key = params.item
            let keyArr = inputsOrigin[i].key.split('.')
            for (let i = 0; i < keyArr.length - 1; i++) {
              if (key[keyArr[i]] === undefined) {
                key[keyArr[i]] = {}
              }
              key = key[keyArr[i]]
            }
            return h('DInput', {
              domProps: {
                value: ''
              },
              on: {
                input (event) {
                  // 值改变时
                  // 将渲染后的值重新赋值给单元格值
                  params.item[keyArr[keyArr.length - 1]] = event.target.value
                  vm.data[params.index] = params.item
                }
              }
            }, key)
          }
        })
      }
      // 处理操作的
      if (!btnsOrigin.length && !operatesOrigin.length && !btnGroupsOrigin) {
        return this.data.colData
      }
      // 渲染操作
      colOpData.push({
        title: this.langConfig['operate'],
        width: this.operationColumnWidth,
        render: (h, params) => {
          let btns = []
          for (let i = 0; i < btnsOrigin.length; i++) {
            let btn = btnsOrigin[i]
            if (btn.bindKey && btn.bindVal) { // 数据权限控制
              let show
              let valType = typeof (btn.bindVal)
              if (!btn.bindCond || btn.bindCond === '=') { // 默认绑定条件是bindKey的值=bindVal中任何一个值时，展示按钮
                show = false
                if (valType === 'number' || valType === 'string' || valType === 'boolean') {
                  if (params.item[btn.bindKey] === btn.bindVal) {
                    show = true
                  }
                } else if (Array.isArray(btn.bindVal)) {
                  for (let val of btn.bindVal) {
                    if (params.item[btn.bindKey] === val) {
                      show = true
                      break
                    }
                  }
                }
              } else if (btn.bindCond === '!=') { // 绑定条件是bindKey的值!=bindVal所有的值时，展示按钮
                show = true

                if (valType === 'number' || valType === 'string' || valType === 'boolean') {
                  if (params.item[btn.bindKey] === btn.bindVal) {
                    show = false
                  }
                } else if (Array.isArray(btn.bindVal)) {
                  for (let val of btn.bindVal) {
                    if (params.item[btn.bindKey] === val) {
                      show = false
                      break
                    }
                  }
                }
              }

              if (!show) {
                continue
              }
            } else if (btn.bindKey && !btn.bindVal) {
              let show = false
              if (!btn.bindCond || btn.bindCond === '=') {
                if (params.item[btn.bindKey]) {
                  show = false
                } else {
                  show = true
                }
              } else if (btn.bindCond && btn.bindCond === '!=') {
                if (params.item[btn.bindKey]) {
                  show = true
                } else {
                  show = false
                }
              } else {
                show = false
              }

              if (!show) {
                continue
              }
            }

            if (btn.isAdmin && !this.$store.getters.isAdmin) { // 登录用户权限控制,admin:1,user:0
              continue
            }

            btns.push(h('label', {
              style: {
                cursor: 'pointer',
                color: '#3366FF',
                textAlign: 'left',
                marginRight: '5px',
                minWidth: '40px',
                display: 'inline-block'
              },
              on: {
                click: () => {
                  // this.$emit(btn.method, params.item, params.index)
                  if (typeof btn.method === 'function') {
                    btn.method.call(this, params.item, params.index)
                  } else {
                    this.$emit(btn.method, params.item, params.index)
                  }
                }
              }
            }, btn.txt))
          }

          for (let key in btnGroupsOrigin) {
            let group = btnGroupsOrigin[key]
            let title = group.title
            let groupBtns = group.btns || []
            let btnItem = []
            for (let j = 0; j < groupBtns.length; j++) {
              let btn = groupBtns[j]
              if (btn.bindKey && params.item[btn.bindKey] !== btn.bindVal) { // 数据权限控制
                continue
              }
              if (btn.isAdmin) { // 登录用户权限控制,admin:1,user:0
                continue
              }
              btnItem.push(h('DDropdownItem', {
                nativeOn: {
                  click: () => {
                    // this.$emit(operate.method, params.item, params.index)
                    if (typeof btn.method === 'function') {
                      btn.method.call(this, params.item, params.index)
                    } else {
                      this.$emit(btn.method, params.item, params.index)
                    }
                  }
                }
              }, btn.txt))
            }
            if (btnItem.length) {
              btns.push(h('DDropdown',
                {
                  props: {
                    trigger: 'click'
                  }
                },
                [
                  h('a', {
                    props: {
                      size: 'small'
                    }
                  }, [
                    title,
                    h('Icon', {
                      props: {
                        name: 'chevron-down',
                        size: '14',
                        slot: 'suffix'
                      }
                    })
                  ]),
                  h('DDropdownMenu', {
                    slot: 'list'
                  }, btnItem)
                ]
              ))
            }
          }

          let items = []
          for (let i = 0; i < operatesOrigin.length; i++) {
            let operate = operatesOrigin[i]
            if (operate.bindKey && params.item[operate.bindKey] !== operate.bindVal) { // 数据权限控制
              continue
            }
            if (operate.isAdmin && !this.$store.getters.isAdmin) { // 登录用户权限控制,admin:1,user:0
              continue
            }
            items.push(h('DDropdownItem', {
              nativeOn: {
                click: () => {
                  // this.$emit(operate.method, params.item, params.index)
                  if (typeof operate.method === 'function') {
                    operate.method.call(this, params.item, params.index)
                  } else {
                    this.$emit(operate.method, params.item, params.index)
                  }
                }
              }
            }, operate.txt))
          }
          if (items.length) {
            btns.push(h('DDropdown',
              {
                props: {
                  trigger: 'click'
                }
              },
              [
                h('a', {
                  props: {
                    size: 'small'
                  }
                }, [
                  '更多',
                  h('Icon', {
                    props: {
                      name: 'chevron-down',
                      size: '14',
                      slot: 'suffix'
                    }
                  })
                ]),
                h('DDropdownMenu', {
                  slot: 'list'
                }, items)
              ]
            ))
          }

          return h('div', btns)
        }
      })
      return colOpData
    },
    blankStyle () {
      if (!this.data.operates || this.data.operates === []) {
        return { 'margin-top': `25px` }
      } else {
        let height = 25 * this.data.operates.length + 25
        return { 'margin-top': `${height}px` }
      }
    }
  },
  methods: {
    showOperate (operate, row) {
      let show = true
      if (operate.bindKey && (row[operate.bindKey] !== operate.bindVal)) { show = false }
      return show
    },
    tableRowClassName ({row, rowIndex}) {
      if (row.className) {
        return row.className
      }
      return ''
    },
    handleRowClick (row) {
      this.$emit('on-row-click', row)
    },
    handleSelectionChange (val) {
      this.$emit('on-selection-change', val)
    },
    handleSelectionAll (val) {
      this.$emit('on-selection-all', val)
    },
    handleOperate (method, index, row) {
      this.$emit(method, index, row)
    },
    handleSizeChange (val) {
      this.$emit('on-size-change', val)
    },
    handleCurrentChange (val) {
      this.$emit('on-current-change', val)
    }
  },
  mounted () {
  }
}
</script>

<style lang="scss" scoped>
  .table{
    width:99%;
    position: relative;
    padding-bottom: 30px;

    & .tablePadding{
      padding:20px 20px 20px 0;
      word-wrap:break-word;
      word-break:break-all
    }
    & .blank{
      display: block;
    }
  }

</style>
