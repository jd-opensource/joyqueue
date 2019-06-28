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
// used for Dialog & $Spin
import {getScrollBarSize} from '../utils/assist'

export default {
  methods: {
    checkScrollBar () {
      let fullWindowWidth = window.innerWidth
      if (!fullWindowWidth) { // workaround for missing window.innerWidth in IE8
        const documentElementRect = document.documentElement.getBoundingClientRect()
        fullWindowWidth = documentElementRect.right - Math.abs(documentElementRect.left)
      }
      this.bodyIsOverflowing = document.body.clientWidth < fullWindowWidth
      if (this.bodyIsOverflowing) {
        this.scrollBarWidth = getScrollBarSize()
      }
    },
    setScrollBar () {
      if (this.bodyIsOverflowing && this.scrollBarWidth !== undefined) {
        document.body.style.paddingRight = `${this.scrollBarWidth}px`
      }
    },
    resetScrollBar () {
      document.body.style.paddingRight = ''
    },
    addScrollEffect () {
      this.checkScrollBar()
      this.setScrollBar()
      document.body.style.overflow = 'hidden'
    },
    removeScrollEffect () {
      document.body.style.overflow = ''
      this.resetScrollBar()
    }
  }
}
