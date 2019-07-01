/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
