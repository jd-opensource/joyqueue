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
import { oneOf } from '../utils/assist'

export default {
  props: {
    to: {
      type: [Object, String]
    },
    replace: {
      type: Boolean,
      default: false
    },
    target: {
      type: String,
      validator (value) {
        return oneOf(value, ['_blank', '_self', '_parent', '_top'])
      },
      default: '_self'
    }
  },
  computed: {
    linkUrl () {
      const type = typeof this.to
      return type === 'string' ? this.to : null
    }
  },
  methods: {
    handleClick (newWindow = false) {
      if (newWindow) {
        window.open(this.to)
      } else {
        const isRoute = this.$router
        if (isRoute) {
          this.replace ? this.$router.replace(this.to) : this.$router.push(this.to)
        } else {
          window.location.href = this.to
        }
      }
    },
    handleCheckClick (event, newWindow = false) {
      if (this.to) {
        if (this.target === '_blank') {
          return false
        } else {
          event.preventDefault()
          this.handleClick(newWindow)
        }
      }
    }
  }
}
