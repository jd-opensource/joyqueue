import Picker from '../picker'
import Panel from '../panel/time-select'
import Config from '../../../config'

export default {
  mixins: [Picker],

  name: `${Config.namePrefix}TimeSelect`,

  componentName: `${Config.namePrefix}TimeSelect`,

  props: {
    type: {
      type: String,
      default: 'time-select'
    }
  },

  beforeCreate () {
    this.panel = Panel
  }
}
