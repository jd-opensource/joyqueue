<template>
  <table @click="handleMonthTableClick" :class="prefixCls">
    <tbody>
    <tr>
      <td :class="getCellStyle(0)">
        <a class="cell">{{ t( localePrefix + 'jan') }}</a>
      </td>
      <td :class="getCellStyle(1)">
        <a class="cell">{{ t(localePrefix + 'feb') }}</a>
      </td>
      <td :class="getCellStyle(2)">
        <a class="cell">{{ t(localePrefix + 'mar') }}</a>
      </td>
      <td :class="getCellStyle(3)">
        <a class="cell">{{ t(localePrefix + 'apr') }}</a>
      </td>
    </tr>
    <tr>
      <td :class="getCellStyle(4)">
        <a class="cell">{{ t(localePrefix + 'may') }}</a>
      </td>
      <td :class="getCellStyle(5)">
        <a class="cell">{{ t(localePrefix + 'jun') }}</a>
      </td>
      <td :class="getCellStyle(6)">
        <a class="cell">{{ t(localePrefix + 'jul') }}</a>
      </td>
      <td :class="getCellStyle(7)">
        <a class="cell">{{ t(localePrefix + 'aug') }}</a>
      </td>
    </tr>
    <tr>
      <td :class="getCellStyle(8)">
        <a class="cell">{{ t(localePrefix + 'sep') }}</a>
      </td>
      <td :class="getCellStyle(9)">
        <a class="cell">{{ t(localePrefix + 'oct') }}</a>
      </td>
      <td :class="getCellStyle(10)">
        <a class="cell">{{ t(localePrefix + 'nov') }}</a>
      </td>
      <td :class="getCellStyle(11)">
        <a class="cell">{{ t(localePrefix + 'dec') }}</a>
      </td>
    </tr>
    </tbody>
  </table>
</template>

<script type="text/babel">
import Config from '../../../config'
import Locale from '../../../mixins/locale'
import { isDate, range, getDayCountOfMonth, nextDate } from '../util'
import { hasClass, arrayFindIndex, coerceTruthyValueToArray } from '../../../utils/assist'

const prefixCls = `${Config.clsPrefix}month-table`
const localePrefix = `${Config.localePrefix}.datepicker.months.`
const datesInMonth = (year, month) => {
  const numOfDays = getDayCountOfMonth(year, month)
  const firstDay = new Date(year, month, 1)
  return range(numOfDays).map(n => nextDate(firstDay, n))
}

export default {
  props: {
    disabledDate: {},
    value: {},
    defaultValue: {
      validator (val) {
        // null or valid Date Object
        return val === null || (val instanceof Date && isDate(val))
      }
    },
    date: {}
  },
  data () {
    return {
      prefixCls: prefixCls,
      localePrefix: localePrefix
    }
  },
  mixins: [Locale],
  methods: {
    getCellStyle (month) {
      const style = {}
      const year = this.date.getFullYear()
      const today = new Date()

      style.disabled = typeof this.disabledDate === 'function'
        ? datesInMonth(year, month).every(this.disabledDate)
        : false
      style.current = arrayFindIndex(coerceTruthyValueToArray(this.value), date => date.getFullYear() === year && date.getMonth() === month) >= 0
      style.today = today.getFullYear() === year && today.getMonth() === month
      style.default = this.defaultValue &&
        this.defaultValue.getFullYear() === year &&
        this.defaultValue.getMonth() === month

      return style
    },

    handleMonthTableClick (event) {
      const target = event.target
      if (target.tagName !== 'A') return
      if (hasClass(target.parentNode, 'disabled')) return
      const column = target.parentNode.cellIndex
      const row = target.parentNode.parentNode.rowIndex
      const month = row * 4 + column

      this.$emit('pick', month)
    }
  }
}
</script>
