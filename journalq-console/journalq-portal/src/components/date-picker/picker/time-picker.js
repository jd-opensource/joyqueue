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
import TimePanel from '../panel/time'
import TimeRangePanel from '../panel/time-range'
import Config from '../../../config'

export default {
  mixins: [Picker],

  name: `${Config.namePrefix}TimePicker`,

  props: {
    isRange: Boolean,
    arrowControl: Boolean
  },

  data () {
    return {
      type: ''
    }
  },

  watch: {
    isRange (isRange) {
      if (this.picker) {
        this.unmountPicker()
        this.type = isRange ? 'timerange' : 'time'
        this.panel = isRange ? TimeRangePanel : TimePanel
        this.mountPicker()
      } else {
        this.type = isRange ? 'timerange' : 'time'
        this.panel = isRange ? TimeRangePanel : TimePanel
      }
    }
  },

  created () {
    this.type = this.isRange ? 'timerange' : 'time'
    this.panel = this.isRange ? TimeRangePanel : TimePanel
  }
}
