<template>
  <transition :name="clsPrefix + 'zoom-in-top'" @after-enter="handleEnter" @after-leave="handleLeave">
    <div
      v-show="visible"
      :class="wrapClasses">
      <div :class="[pickerPanelCls + '__body-wrapper']">
        <slot name="sidebar" :class="[pickerPanelCls + '__sidebar']"></slot>
        <div :class="[pickerPanelCls + '__sidebar']" v-if="shortcuts">
          <button
            type="button"
            :class="[pickerPanelCls + '__shortcut']"
            v-for="(shortcut, key) in shortcuts"
            :key="key"
            @click="handleShortcutClick(shortcut)">{{ shortcut.text }}</button>
        </div>
        <div :class="[pickerPanelCls + '__body']">
          <div :class="[datePickerCls + '__time-header']" v-if="showTime">
            <span :class="[datePickerCls + '__editor-wrap']">
              <d-input
                :placeholder="t(localePrefix + 'selectDate')"
                :value="visibleDate"
                size="small"
                @input="val => userInputDate = val"
                @on-change="handleVisibleDateChange" />
            </span>
            <span :class="[datePickerCls + '__editor-wrap']" v-click-outside="handleTimePickClose">
              <d-input
                ref="input"
                @on-focus="timePickerVisible = true"
                :placeholder="t(localePrefix + 'selectTime')"
                :value="visibleTime"
                size="small"
                @input="val => userInputTime = val"
                @on-change="handleVisibleTimeChange" />
              <time-picker
                ref="timepicker"
                :time-arrow-control="arrowControl"
                @pick="handleTimePick"
                :visible="timePickerVisible"
                @mounted="proxyTimePickerDataProperties">
              </time-picker>
            </span>
          </div>
          <div
            :class="[datePickerCls + '__header', { headerBorderedCls: currentView === 'year' || currentView === 'month' }]"
            v-show="currentView !== 'time'">
            <button
              type="button"
              @click="prevYear"
              :aria-label="t(localePrefix + 'prevYear')"
              :class="[pickerPanelCls + '__icon-btn', datePickerCls + '__prev-btn']">
              <Icon name="chevrons-left"></Icon>
            </button>
            <button
              type="button"
              @click="prevMonth"
              v-show="currentView === 'date'"
              :aria-label="t(localePrefix + 'prevMonth')"
              :class="[pickerPanelCls + '__icon-btn', datePickerCls + '__prev-btn']">
              <Icon name="chevron-left"></Icon>
            </button>
            <span
              @click="showYearPicker"
              role="button"
              :class="[datePickerCls + '__header-label']">{{ yearLabel }}</span>
            <span
              @click="showMonthPicker"
              v-show="currentView === 'date'"
              role="button"
              :class="[datePickerCls + '__header-label', { active: currentView === 'month' }]">{{t(`${localePrefix}month${ month + 1 }`)}}</span>
            <button
              type="button"
              @click="nextYear"
              :aria-label="t(localePrefix + 'nextYear')"
              :class="[pickerPanelCls + '__icon-btn', datePickerCls + '__next-btn']">
              <Icon name="chevrons-right"></Icon>
            </button>
            <button
              type="button"
              @click="nextMonth"
              v-show="currentView === 'date'"
              :aria-label="t(localePrefix + 'nextMonth')"
              :class="[pickerPanelCls + '__icon-btn', datePickerCls + '__next-btn']">
              <Icon name="chevron-right"></Icon>
            </button>
          </div>

          <div :class="[pickerPanelCls + '__content']">
            <date-table
              v-show="currentView === 'date'"
              @pick="handleDatePick"
              :selection-mode="selectionMode"
              :first-day-of-week="firstDayOfWeek"
              :value="value"
              :default-value="defaultValue ? new Date(defaultValue) : null"
              :date="date"
              :disabled-date="disabledDate">
            </date-table>
            <year-table
              v-show="currentView === 'year'"
              @pick="handleYearPick"
              :value="value"
              :default-value="defaultValue ? new Date(defaultValue) : null"
              :date="date"
              :disabled-date="disabledDate">
            </year-table>
            <month-table
              v-show="currentView === 'month'"
              @pick="handleMonthPick"
              :value="value"
              :default-value="defaultValue ? new Date(defaultValue) : null"
              :date="date"
              :disabled-date="disabledDate">
            </month-table>
          </div>
        </div>
      </div>

      <div
        :class="[pickerPanelCls + '__footer']"
        v-show="footerVisible && currentView === 'date'">
        <d-button
          size="small"
          type="borderless"
          :class="[pickerPanelCls + '__link-btn']"
          @click="changeToNow"
          v-show="selectionMode !== 'dates'">
          {{ t(localePrefix + 'now') }}
        </d-button>
        <d-button
          size="small"
          type="primary"
          :class="[pickerPanelCls + '__link-btn']"
          @click="confirm">
          {{ t(localePrefix + 'confirm') }}
        </d-button>
      </div>
    </div>
  </transition>
</template>

<script type="text/babel">
import Config from '../../../config'
import {
  formatDate,
  parseDate,
  getWeekNumber,
  isDate,
  modifyDate,
  modifyTime,
  modifyWithTimeString,
  clearMilliseconds,
  clearTime,
  prevYear,
  nextYear,
  prevMonth,
  nextMonth,
  changeYearMonthAndClampDate,
  extractDateFormat,
  extractTimeFormat
} from '../util'
import {directive as ClickOutside} from 'v-click-outside-x'
import Locale from '../../../mixins/locale'
import Icon from '../../icon'
import DInput from '../../input/input.vue'
import DButton from '../../button/button.vue'
import TimePicker from './time'
import YearTable from '../basic/year-table'
import MonthTable from '../basic/month-table'
import DateTable from '../basic/date-table'

const clsPrefix = Config.clsPrefix
const localePrefix = `${Config.localePrefix}.datepicker.`

export default {
  components: {
    TimePicker, YearTable, MonthTable, DateTable, Icon, DInput, DButton
  },

  mixins: [Locale],

  directives: { ClickOutside },

  watch: {
    showTime (val) {
      /* istanbul ignore if */
      if (!val) return
      this.$nextTick(_ => {
        const inputElm = this.$refs.input.$el
        if (inputElm) {
          this.pickerWidth = inputElm.getBoundingClientRect().width + 10
        }
      })
    },

    value (val) {
      if (this.selectionMode === 'dates' && this.value) return
      if (isDate(val)) {
        this.date = new Date(val)
      } else {
        this.date = this.getDefaultValue()
      }
    },

    defaultValue (val) {
      if (!isDate(this.value)) {
        this.date = val ? new Date(val) : new Date()
      }
    },

    timePickerVisible (val) {
      if (val) this.$nextTick(() => this.$refs.timepicker.adjustSpinners())
    },

    selectionMode (newVal) {
      if (newVal === 'month') {
        /* istanbul ignore next */
        if (this.currentView !== 'year' || this.currentView !== 'month') {
          this.currentView = 'month'
        }
      } else if (newVal === 'dates') {
        this.currentView = 'date'
      }
    }
  },

  methods: {
    proxyTimePickerDataProperties () {
      const format = timeFormat => { this.$refs.timepicker.format = timeFormat }
      const value = value => { this.$refs.timepicker.value = value }
      const date = date => { this.$refs.timepicker.date = date }

      this.$watch('value', value)
      this.$watch('date', date)

      format(this.timeFormat)
      value(this.value)
      date(this.date)
    },

    handleClear () {
      this.date = this.getDefaultValue()
      this.$emit('pick', null)
    },

    emit (value, ...args) {
      if (!value) {
        this.$emit('pick', value, ...args)
      } else if (Array.isArray(value)) {
        const dates = value.map(date => this.showTime ? clearMilliseconds(date) : clearTime(date))
        this.$emit('pick', dates, ...args)
      } else {
        this.$emit('pick', this.showTime ? clearMilliseconds(value) : clearTime(value), ...args)
      }
      this.userInputDate = null
      this.userInputTime = null
    },

    // resetDate() {
    //   this.date = new Date(this.date);
    // },

    showMonthPicker () {
      this.currentView = 'month'
    },

    showYearPicker () {
      this.currentView = 'year'
    },

    // XXX: 没用到
    // handleLabelClick() {
    //   if (this.currentView === 'date') {
    //     this.showMonthPicker();
    //   } else if (this.currentView === 'month') {
    //     this.showYearPicker();
    //   }
    // },

    prevMonth () {
      this.date = prevMonth(this.date)
    },

    nextMonth () {
      this.date = nextMonth(this.date)
    },

    prevYear () {
      if (this.currentView === 'year') {
        this.date = prevYear(this.date, 10)
      } else {
        this.date = prevYear(this.date)
      }
    },

    nextYear () {
      if (this.currentView === 'year') {
        this.date = nextYear(this.date, 10)
      } else {
        this.date = nextYear(this.date)
      }
    },

    handleShortcutClick (shortcut) {
      if (shortcut.onClick) {
        shortcut.onClick(this)
      }
    },

    handleTimePick (value, visible, first) {
      if (isDate(value)) {
        const newDate = this.value
          ? modifyTime(this.value, value.getHours(), value.getMinutes(), value.getSeconds())
          : modifyWithTimeString(this.getDefaultValue(), this.defaultTime)
        this.date = newDate
        this.emit(this.date, true)
      } else {
        this.emit(value, true)
      }
      if (!first) {
        this.timePickerVisible = visible
      }
    },

    handleTimePickClose () {
      this.timePickerVisible = false
    },

    handleMonthPick (month) {
      if (this.selectionMode === 'month') {
        this.date = modifyDate(this.date, this.year, month, 1)
        this.emit(this.date)
      } else {
        this.date = changeYearMonthAndClampDate(this.date, this.year, month)
        // TODO: should emit intermediate value ??
        // this.emit(this.date);
        this.currentView = 'date'
      }
    },

    handleDatePick (value) {
      if (this.selectionMode === 'day') {
        this.date = this.value
          ? modifyDate(this.value, value.getFullYear(), value.getMonth(), value.getDate())
          : modifyWithTimeString(value, this.defaultTime)
        this.emit(this.date, this.showTime)
      } else if (this.selectionMode === 'week') {
        this.emit(value.date)
      } else if (this.selectionMode === 'dates') {
        this.emit(value, true) // set false to keep panel open
      }
    },

    handleYearPick (year) {
      if (this.selectionMode === 'year') {
        this.date = modifyDate(this.date, year, 0, 1)
        this.emit(this.date)
      } else {
        this.date = changeYearMonthAndClampDate(this.date, year, this.month)
        // TODO: should emit intermediate value ??
        // this.emit(this.date, true);
        this.currentView = 'month'
      }
    },

    changeToNow () {
      // NOTE: not a permanent solution
      //       consider disable "now" button in the future
      if (!this.disabledDate || !this.disabledDate(new Date())) {
        this.date = new Date()
        this.emit(this.date)
      }
    },

    confirm () {
      if (this.selectionMode === 'dates') {
        this.emit(this.value)
      } else {
        // value were emitted in handle{Date,Time}Pick, nothing to update here
        // deal with the scenario where: user opens the picker, then confirm without doing anything
        const value = this.value
          ? this.value
          : modifyWithTimeString(this.getDefaultValue(), this.defaultTime)
        this.date = new Date(value) // refresh date
        this.emit(value)
      }
    },

    resetView () {
      if (this.selectionMode === 'month') {
        this.currentView = 'month'
      } else if (this.selectionMode === 'year') {
        this.currentView = 'year'
      } else {
        this.currentView = 'date'
      }
    },

    handleEnter () {
      document.body.addEventListener('keydown', this.handleKeydown)
    },

    handleLeave () {
      this.$emit('dodestroy')
      document.body.removeEventListener('keydown', this.handleKeydown)
    },

    handleKeydown (event) {
      const keyCode = event.keyCode
      const list = [38, 40, 37, 39]
      if (this.visible && !this.timePickerVisible) {
        if (list.indexOf(keyCode) !== -1) {
          this.handleKeyControl(keyCode)
          event.stopPropagation()
          event.preventDefault()
        }
        if (keyCode === 13 && this.userInputDate === null && this.userInputTime === null) { // Enter
          this.emit(this.date, false)
        }
      }
    },

    handleKeyControl (keyCode) {
      const mapping = {
        'year': {
          38: -4, 40: 4, 37: -1, 39: 1, offset: (date, step) => date.setFullYear(date.getFullYear() + step)
        },
        'month': {
          38: -4, 40: 4, 37: -1, 39: 1, offset: (date, step) => date.setMonth(date.getMonth() + step)
        },
        'week': {
          38: -1, 40: 1, 37: -1, 39: 1, offset: (date, step) => date.setDate(date.getDate() + step * 7)
        },
        'day': {
          38: -7, 40: 7, 37: -1, 39: 1, offset: (date, step) => date.setDate(date.getDate() + step)
        }
      }
      const mode = this.selectionMode
      const year = 3.1536e10
      const now = this.date.getTime()
      const newDate = new Date(this.date.getTime())
      while (Math.abs(now - newDate.getTime()) <= year) {
        const map = mapping[mode]
        map.offset(newDate, map[keyCode])
        if (typeof this.disabledDate === 'function' && this.disabledDate(newDate)) {
          continue
        }
        this.date = newDate
        this.$emit('pick', newDate, true)
        break
      }
    },

    handleVisibleTimeChange (value) {
      const time = parseDate(value, this.timeFormat)
      if (time) {
        this.date = modifyDate(time, this.year, this.month, this.monthDate)
        this.userInputTime = null
        this.$refs.timepicker.value = this.date
        this.timePickerVisible = false
        this.emit(this.date, true)
      }
    },

    handleVisibleDateChange (value) {
      const date = parseDate(value, this.dateFormat)
      if (date) {
        if (typeof this.disabledDate === 'function' && this.disabledDate(date)) {
          return
        }
        this.date = modifyTime(date, this.date.getHours(), this.date.getMinutes(), this.date.getSeconds())
        this.userInputDate = null
        this.resetView()
        this.emit(this.date, true)
      }
    },

    isValidValue (value) {
      return value && !isNaN(value) && (
        typeof this.disabledDate === 'function'
          ? !this.disabledDate(value)
          : true
      )
    },

    getDefaultValue () {
      // if default-value is set, return it
      // otherwise, return now (the moment this method gets called)
      return this.defaultValue ? new Date(this.defaultValue) : new Date()
    }
  },

  data () {
    return {
      clsPrefix: clsPrefix,
      localePrefix: localePrefix,
      pickerPanelCls: `${clsPrefix}picker-panel`,
      datePickerCls: `${clsPrefix}date-picker`,
      headerBorderedCls: `${clsPrefix}picker-panel__header--bordered`,
      popperClass: '',
      date: new Date(),
      value: '',
      defaultValue: null, // use getDefaultValue() for time computation
      defaultTime: null,
      showTime: false,
      selectionMode: 'day',
      shortcuts: '',
      visible: false,
      currentView: 'date',
      disabledDate: '',
      firstDayOfWeek: 7,
      showWeekNumber: false,
      timePickerVisible: false,
      format: '',
      arrowControl: false,
      userInputDate: null,
      userInputTime: null
    }
  },

  computed: {
    wrapClasses () {
      return [
        this.pickerPanelCls,
        this.datePickerCls,
        `${clsPrefix}popper`,
        {
          'has-sidebar': this.$slots.sidebar || this.shortcuts,
          'has-time': !!this.showTime
        },
        this.popperClass
      ]
    },

    year () {
      return this.date.getFullYear()
    },

    month () {
      return this.date.getMonth()
    },

    week () {
      return getWeekNumber(this.date)
    },

    monthDate () {
      return this.date.getDate()
    },

    footerVisible () {
      return this.showTime || this.selectionMode === 'dates'
    },

    visibleTime () {
      if (this.userInputTime !== null) {
        return this.userInputTime
      } else {
        return formatDate(this.value || this.defaultValue, this.timeFormat)
      }
    },

    visibleDate () {
      if (this.userInputDate !== null) {
        return this.userInputDate
      } else {
        return formatDate(this.value || this.defaultValue, this.dateFormat)
      }
    },

    yearLabel () {
      const yearTranslation = this.t(localePrefix + 'year')
      if (this.currentView === 'year') {
        const startYear = Math.floor(this.year / 10) * 10
        if (yearTranslation) {
          return startYear + ' ' + yearTranslation + ' - ' + (startYear + 9) + ' ' + yearTranslation
        }
        return startYear + ' - ' + (startYear + 9)
      }
      return this.year + ' ' + yearTranslation
    },

    timeFormat () {
      if (this.format) {
        return extractTimeFormat(this.format)
      } else {
        return 'HH:mm:ss'
      }
    },

    dateFormat () {
      if (this.format) {
        return extractDateFormat(this.format)
      } else {
        return 'yyyy-MM-dd'
      }
    }
  }
}
</script>
