import setLang from '../lang'
import Config from '../../config'

const lang = {}

lang[Config.localePrefix] = {
  locale: 'en-US',
  select: {
    placeholder: 'Select',
    noMatch: 'No matching data',
    loading: 'Loading'
  },
  cascader: {
    noMatch: 'No matching data',
    loading: 'Loading',
    placeholder: 'Select'
  },
  table: {
    noDataText: 'No Data',
    noFilteredDataText: 'No filter data',
    confirmFilter: 'Confirm',
    resetFilter: 'Reset',
    clearFilter: 'All'
  },
  datepicker: {
    now: 'Now',
    today: 'Today',
    cancel: 'Cancel',
    clear: 'Clear',
    confirm: 'OK',
    selectDate: 'Select date',
    selectTime: 'Select time',
    startDate: 'Start Date',
    startTime: 'Start Time',
    endDate: 'End Date',
    endTime: 'End Time',
    prevYear: 'Previous Year',
    nextYear: 'Next Year',
    prevMonth: 'Previous Month',
    nextMonth: 'Next Month',
    year: '',
    month1: 'January',
    month2: 'February',
    month3: 'March',
    month4: 'April',
    month5: 'May',
    month6: 'June',
    month7: 'July',
    month8: 'August',
    month9: 'September',
    month10: 'October',
    month11: 'November',
    month12: 'December',
    // week: 'week',
    weeks: {
      sun: 'Sun',
      mon: 'Mon',
      tue: 'Tue',
      wed: 'Wed',
      thu: 'Thu',
      fri: 'Fri',
      sat: 'Sat'
    },
    months: {
      jan: 'Jan',
      feb: 'Feb',
      mar: 'Mar',
      apr: 'Apr',
      may: 'May',
      jun: 'Jun',
      jul: 'Jul',
      aug: 'Aug',
      sep: 'Sep',
      oct: 'Oct',
      nov: 'Nov',
      dec: 'Dec'
    }
  },
  transfer: {
    titles: {
      source: 'Source',
      target: 'Target'
    },
    filterPlaceholder: 'Search here',
    notFoundText: 'Not Found'
  },
  dialog: {
    info: 'Info',
    success: 'Success',
    warning: 'Warning',
    error: 'Error',
    okText: 'OK',
    cancelText: 'Cancel'
  },
  popover: {
    okText: 'OK',
    cancelText: 'Cancel'
  },
  pagination: {
    prevText: 'Previous Page',
    nextText: 'Next Page',
    total: 'Total',
    item: 'item',
    items: 'items',
    pageSize: '/ page',
    goto: 'Goto',
    pageText: '',
    prev5Text: 'Previous 5 Pages',
    next5Text: 'Next 5 Pages'
  },
  rate: {
    star: 'Star',
    stars: 'Stars'
  },
  tree: {
    emptyText: 'No Data'
  },
  loading: {
    loadingText: 'Loading...'
  },
  upload: {
    deleteTip: 'press delete to remove',
    delete: 'Delete',
    preview: 'Preview',
    continue: 'Continue'
  },
  charts: {
    noDataText: 'No Data',
    otherText: 'Other'
  }
}

setLang(lang)

export default lang
