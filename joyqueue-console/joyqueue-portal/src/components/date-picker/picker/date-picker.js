import Picker from '../picker'
import DatePanel from '../panel/date'
import DateRangePanel from '../panel/date-range'
import Config from '../../../config'

const getPanel = function (type) {
  if (type === 'daterange' || type === 'datetimerange') {
    return DateRangePanel
  }
  return DatePanel
}

export default {
  mixins: [Picker],

  name: `${Config.namePrefix}DatePicker`,

  props: {
    type: {
      type: String,
      default: 'date'
    },
    timeArrowControl: Boolean
  },

  watch: {
    type (type) {
      if (this.picker) {
        this.unmountPicker()
        this.panel = getPanel(type)
        this.mountPicker()
      } else {
        this.panel = getPanel(type)
      }
    }
  },

  created () {
    this.panel = getPanel(this.type)
  }
}
