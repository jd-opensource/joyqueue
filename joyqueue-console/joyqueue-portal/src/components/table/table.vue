<template>
  <div
    :class="classes"
    :style="tableStyles">

    <!-- S Content -->
    <div :class="[prefixCls + '__content']" :style="contentStyle">
      <!-- S Header -->
      <div :class="[prefixCls + '__header']" v-if="height">
        <table>
          <colgroup>
            <col v-if="optional">
            <col v-for="(column, index) in columnsData" :key="index" :width="setCellWidth(column, index)">
          </colgroup>
          <thead :class="[prefixCls + '__thead']" ref="header">
          <tr>
            <!-- S Checkbox -->
            <th v-if="optional" :class="selectionClasses">
              <Checkbox v-model="isSelectAll" @click.native="handleSelectAll"></Checkbox>
            </th>
            <!-- E Checkbox -->
            <!-- S Column th -->
            <th
              v-for="(column, index) in columnsData" :key="index"
              :class="[prefixCls + '__cell', prefixCls + '__column', column.className]"
              :style="{
                  cursor: column.sortType ? 'pointer' : 'text'
                }"
              @click="column.sortType && handleSort(index)">
              {{ column.title || '' }}
              <template v-if="column.sortType">
                <div :class="[prefixCls + '__column-sorter', column._sortType === 'asc' ? 'sort-asc': 'sort-desc']">
                  <span :class="[prefixCls + '__column-sorter-up']" @click.stop="handleSort(index, 'asc')">
                    <Icon name="chevron-up"></Icon>
                  </span>
                  <span :class="[prefixCls + '__column-sorter-down']" @click.stop="handleSort(index, 'desc')">
                    <Icon name="chevron-down"></Icon>
                  </span>
                </div>
              </template>
            </th>
            <!-- E Column th -->
          </tr>
          </thead>
        </table>
      </div>
      <!-- E Header -->

      <!-- S Body -->
      <div :class="[prefixCls + '__body']" :style="bodyStyle">
        <table>
          <colgroup>
            <col v-if="optional">
            <col v-for="(column, index) in columnsData" :key="index" :width="setCellWidth(column, index)">
          </colgroup>
          <thead :class="[prefixCls + '__thead']" v-if="!height" ref="header">
          <tr>
            <!-- S Checkbox -->
            <th v-if="optional" :class="selectionClasses">
              <Checkbox v-model="isSelectAll" @click.native.prevent="handleSelectAll"></Checkbox>
            </th>
            <!-- E Checkbox -->
            <!-- S Column th -->
            <th
              v-for="(column, index) in columnsData" :key="index"
              :class="[prefixCls + '__cell', prefixCls + '__column', column.className]"
              :style="{
                  cursor: column.sortType ? 'pointer' : 'text'
                }"
              @click="column.sortType && handleSort(index)">
              {{ column.title || '' }}
              <template v-if="column.sortType">
                <div :class="[prefixCls + '__column-sorter', column._sortType === 'asc' ? 'sort-asc': 'sort-desc']">
                  <span :class="[prefixCls + '__column-sorter-up']" @click.stop="handleSort(index, 'asc')">
                    <Icon name="chevron-up"></Icon>
                  </span>
                  <span :class="[prefixCls + '__column-sorter-down']" @click.stop="handleSort(index, 'desc')">
                    <Icon name="chevron-down"></Icon>
                  </span>
                </div>
              </template>
            </th>
            <!-- E Column th -->
          </tr>
          </thead>

          <tbody :class="[prefixCls + '__tbody']" v-if="sortData.length" ref="body">
          <template v-for="(item, index) in sortData">
            <tr :key="index">
              <td v-if="optional" :class="selectionClasses">
                <Checkbox v-model="objData[index].isChecked" @on-change="changeRowSelection"></Checkbox>
              </td>
              <td v-for="(column, cindex) in columns" :key="cindex" :class="[prefixCls + '__cell']">
                <template v-if="column.type === 'expand' && !item.disableExpand">
                  <div :class="expandCls(index)" @click="toggleExpand(item.index)">
                    <Icon name="chevron-right"></Icon>
                  </div>
                </template>
                <template v-else-if="column.render">
                  <Cell :item="item" :column="column" :index="index" :render="column.render"></Cell>
                </template>
                <template v-else>
                  {{ getColumnValue(item, column, cindex) }}
                </template>
              </td>
            </tr>
            <tr :key="item.index + '-expand'" v-if="rowExpanded(item.index)">
              <td :colspan="columns.length" :class="prefixCls + '-expanded-cell'">
                <Expand :row="item" :render="expandRender" :index="item.index"></Expand>
              </td>
            </tr>
          </template>
          </tbody>

          <tbody :class="[prefixCls + '__tbody']" v-else>
          <tr>
            <td :class="noDataClasses" :colspan="optional ? columns.length + 1 : columns.length">
              <slot name="emptyText">{{ t(localePrefix + 'noDataText') }}</slot>
            </td>
          </tr>
          </tbody>
        </table>
      </div>
      <!-- E Body -->
    </div>
    <!-- E Content -->

    <!-- S Pagination -->
    <div v-if="pagination && total" :class="[prefixCls + '__footer']" ref="footer">
      <Pagination
        :current="currentPage"
        :size="size === 'default' ? 'small' : size"
        :total="total"
        :page-size="pageSize"
        :show-total="showPageTotal"
        :show-sizer="showPageSizer"
        :show-quickjump="showPageQuickjump"
        @page-change="pageChange"
        @pagesize-change="pageSizeChange"></Pagination>
    </div>
    <!-- E Pagination -->
  </div>
</template>

<script>
import Config from '../../config'
import Cell from './render'
import Expand from './expand'
import Locale from '../../mixins/locale'
import {oneOf, getStyle, deepCopy, getPropByPath} from '../../utils/assist'
import Icon from '../icon/icon.vue'
import Checkbox from '../checkbox'
import Pagination from '../pagination'

const prefixCls = `${Config.clsPrefix}table`
const localePrefix = `${Config.localePrefix}.table.`

export default {
  name: `${Config.namePrefix}Table`,
  components: {
    Icon,
    Checkbox,
    Pagination,
    Cell,
    Expand
  },
  mixins: [Locale],
  props: {
    size: {
      default: 'default',
      validator (value) {
        return oneOf(value, ['large', 'default', 'small'])
      }
    },
    stripe: {
      type: Boolean,
      default: false
    },
    border: {
      type: Boolean,
      default: false
    },
    data: {
      type: Array,
      default () {
        return []
      }
    },
    columns: {
      type: Array,
      default () {
        return []
      }
    },
    optional: {
      type: Boolean,
      default: false
    },
    pagination: {
      type: Boolean,
      default: false
    },
    pageSize: {
      type: Number,
      default: 10
    },
    showPageTotal: {
      type: Boolean,
      default: true
    },
    showPageSizer: {
      type: Boolean,
      default: false
    },
    showPageQuickjump: {
      type: Boolean,
      default: false
    },
    height: {
      type: [Number, String]
    }
  },
  data () {
    return {
      prefixCls: prefixCls,
      localePrefix: localePrefix,
      objData: this.makeObjData(), // checkbox or expand row
      sortData: [], // use for sort or paginate
      allData: [],
      cloneData: deepCopy(this.data), // when Cell has a button to delete row data, clickCurrentRow will throw an error, so clone a data
      columnsData: this.makeColumns(),
      total: 0,
      bodyHeight: 0,
      pageCurSize: this.pageSize,
      columnsWidth: {},
      currentPage: 1
    }
  },
  watch: {
    height () {
      this.calculateBodyHeight()
    },
    allData () {
      this.total = this.allData.length
    },
    sortData () {
      this.handleResize()
    },
    pageCurSize () {
      this.sortData = this.makeDataWithPaginate()
    },
    data () {
      this.objData = this.makeObjData()
      this.sortData = this.makeDataWithSortAndPage()
    },
    columns () {
      this.columnsData = this.makeColumns()
    }
  },
  computed: {
    classes () {
      return [
        `${prefixCls}`,
        {
          [`${prefixCls}--fixHeight`]: !!this.height,
          [`${prefixCls}--stripe`]: !!this.stripe,
          [`${prefixCls}--${this.size}`]: !!this.size,
          [`${prefixCls}--border`]: !!this.border
        }
      ]
    },
    tableStyles () {
      const styles = {}

      if (this.height) {
        styles.height = `${this.height}px`
      }
      if (this.width) {
        styles.width = `${this.width}px`
      }

      return styles
    },
    selectionClasses () {
      return [
        `${prefixCls}__cell`,
        `${prefixCls}__column-selection`
      ]
    },
    noDataClasses () {
      return [
        `${prefixCls}__cell`,
        `${prefixCls}__cell--nodata`
      ]
    },
    isSelectAll () {
      let isAll = true
      if (!this.sortData.length) {
        isAll = false
      }
      for (let i = 0, len = this.sortData.length; i < len; i++) {
        if (!this.objData[this.sortData[i].index].isChecked) {
          isAll = false
          break
        }
      }

      return isAll
    },
    bodyStyle () {
      const styles = {}
      if (this.bodyHeight !== 0) {
        const headerHeight = parseInt(getStyle(this.$refs.header, 'height')) || 0
        styles.height = `${this.bodyHeight}px`
        styles.marginTop = `${headerHeight}px`
      }
      return styles
    },
    contentStyle () {
      const styles = {}
      if (this.bodyHeight !== 0) {
        const headerHeight = parseInt(getStyle(this.$refs.header, 'height')) || 0
        styles.height = `${this.bodyHeight + headerHeight}px`
      }
      return styles
    },
    expandRender () {
      let render = function () {
        return ''
      }
      for (let i = 0; i < this.columns.length; i++) {
        const column = this.columns[i]
        if (column.type && column.type === 'expand') {
          if (column.render) render = column.render
        }
      }
      return render
    }
  },
  methods: {
    getColumnValue (row, column, index) {
      const property = column.key
      const value = property && getPropByPath(row, property).v
      if (column && column.formatter) {
        return column.formatter(row, column, value, index)
      }
      return value
    },
    calculateBodyHeight () {
      if (this.height) {
        this.$nextTick(() => {
          const headerHeight = parseInt(getStyle(this.$refs.header, 'height')) || 0
          const footerHeight = parseInt(getStyle(this.$refs.footer, 'height')) || 0

          this.bodyHeight = this.height - headerHeight - footerHeight
        })
      } else {
        this.bodyHeight = 0
      }
    },
    makeColumns () {
      const columns = deepCopy(this.columns)
      columns.forEach((column, idx) => {
        column._index = idx
        column._sortType = 'normal'

        if (column.sortType) {
          column._sortType = column.sortType
          column.sortType = column.sortType
        }
      })
      return columns
    },
    makeData () {
      const data = deepCopy(this.data)
      data.forEach((row, idx) => {
        row.index = idx
      })
      return data
    },
    makeObjData () {
      const rowData = {}
      this.data.forEach((row, index) => {
        const newRow = deepCopy(row)

        newRow.isChecked = !!newRow.isChecked
        newRow.isExpanded = !!newRow.isExpanded

        rowData[index] = newRow
      })

      return rowData
    },
    makeDataWithSortAndPage (pageNum) {
      let data = []
      let allData = []

      allData = this.makeDataWithSort()
      this.allData = allData

      data = this.makeDataWithPaginate(pageNum)
      return data
    },
    makeDataWithPaginate (page) {
      page = page || 1
      const pageStart = (page - 1) * this.pageCurSize
      const pageEnd = pageStart + this.pageCurSize
      let pageData = []

      if (this.pagination) {
        pageData = this.allData.slice(pageStart, pageEnd)
      } else {
        pageData = this.allData
      }
      return pageData
    },
    makeDataWithSort () {
      let data = this.makeData()
      let sortType = 'normal'
      let sortIndex = -1

      for (let i = 0, len = this.columnsData.length; i < len; i++) {
        if (this.columnsData[i].sortType && this.columnsData[i].sortType !== 'normal') {
          sortType = this.columnsData[i].sortType
          sortIndex = i
          break
        }
      }

      if (sortType !== 'normal') {
        data = this.sort(data, sortType, sortIndex)
      }

      return data
    },
    handleSelectAll () {
      const status = !this.isSelectAll

      for (const data of this.sortData) {
        this.objData[data.index].isChecked = status
      }

      const selection = this.getSelection()

      status && this.$emit('on-select-all', selection)
      this.$emit('on-selection-change', selection)
    },
    handleSort (index, type) {
      const key = this.columnsData[index].key
      const sortType = this.columnsData[index]._sortType
      const sortNameArr = ['normal', 'desc', 'asc']

      if (this.columnsData[index].sortType) {
        if (!type) {
          const tmpIdx = sortNameArr.indexOf(sortType)
          if (tmpIdx >= 0) {
            type = sortNameArr[(tmpIdx + 1) > 2 ? 0 : tmpIdx + 1]
          }
        }
        if (type === 'normal') {
          this.sortData = this.makeDataWithSortAndPage(this.currentPage)
        } else {
          this.sortData = this.sort(this.sortData, type, index)
        }
      }
      this.columnsData[index]._sortType = type

      this.$emit('on-sort-change', {
        column: JSON.parse(JSON.stringify(this.columns[this.columnsData[index]._index])),
        order: type,
        key
      })
    },
    sort (data, type, index) {
      const key = this.columnsData[index].key
      data.sort((a, b) => {
        if (this.columnsData[index].sortMethod) {
          return this.columnsData[index].sortMethod(a[key], b[key], type)
        } else if (type === 'asc') {
          return a[key] > b[key] ? 1 : -1
        }
        return a[key] < b[key] ? 1 : -1
      })
      return data
    },
    getSelection () {
      const selectionIndexArray = []
      for (const i in this.objData) {
        if (this.objData[i].isChecked) {
          selectionIndexArray.push(i | 0)
        }
      }
      return JSON.parse(JSON.stringify(this.data.filter((data, index) => selectionIndexArray.indexOf(index) > -1)))
    },
    changeRowSelection () {
      const selection = this.getSelection()
      this.$emit('on-selection-change', selection)
    },
    pageChange (page) {
      this.$emit('on-page-change', page)
      this.currentPage = page
      this.sortData = this.makeDataWithPaginate(page)
    },
    pageSizeChange (size) {
      this.$emit('on-page-size-change', size)
      this.pageCurSize = size
    },
    handleResize () {
      this.$nextTick(() => {
        const columnsWidth = {}

        if (this.data.length && this.$refs.body) {
          const $td = this.$refs.body.querySelectorAll('tr')[0].querySelectorAll('td')

          for (let i = 0; i < $td.length; i++) {
            const column = this.columnsData[i]
            let width = parseInt(getStyle($td[i], 'width'))

            if (column) {
              if (column.width) {
                width = column.width
              }
              columnsWidth[column._index] = {width}
            }
          }
        }

        this.columnsWidth = columnsWidth
      })
    },
    setCellWidth (column, index) {
      let width = ''

      if (column.width) {
        width = column.width
      } else if (this.columnsWidth[column._index]) {
        width = this.columnsWidth[column._index].width
      }

      width = width === '0' ? '' : width
      return width
    },
    toggleExpand (index) {
      let data = {}

      for (let i in this.objData) {
        if (parseInt(i) === index) {
          data = this.objData[i]
          break
        }
      }
      const status = !data.isExpanded
      this.objData[index].isExpanded = status
      let copyData = deepCopy(this.data)
      this.$emit('on-expand', JSON.parse(JSON.stringify(copyData[index])), status)
    },
    expandCls (index) {
      return [
        `${this.prefixCls}__cell-expand`,
        {
          [`${this.prefixCls}__cell-expand-expanded`]: this.rowExpanded(index)
        }
      ]
    },
    rowExpanded (index) {
      return this.objData[index] && this.objData[index].isExpanded
    }
  },
  created () {
    this.sortData = this.makeDataWithSortAndPage()
  },
  mounted () {
    this.calculateBodyHeight()
    window.addEventListener('resize', this.handleResize)
  },
  beforeDestory () {
    window.removeEventListener('resize', this.handleResize)
  }
}
</script>
