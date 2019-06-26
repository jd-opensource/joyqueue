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
import Config from '../../config'
import { on, off } from '../../utils/assist'
import { renderThumbStyle, BAR_MAP } from './util'

/* istanbul ignore next */
export default {
  name: 'Bar',

  props: {
    vertical: Boolean,
    size: String,
    move: Number
  },

  computed: {
    bar () {
      return BAR_MAP[this.vertical ? 'vertical' : 'horizontal']
    },

    wrap () {
      return this.$parent.wrap
    }
  },

  render (h) {
    const { size, move, bar } = this

    const prefixCls = `${Config.clsPrefix}scrollbar`

    return (
      <div
        class={ [prefixCls + '__bar', 'is-' + bar.key] }
        onMousedown={ this.clickTrackHandler } >
        <div
          ref="thumb"
          class={ prefixCls + '__thumb' }
          onMousedown={ this.clickThumbHandler }
          style={ renderThumbStyle({ size, move, bar }) }>
        </div>
      </div>
    )
  },

  methods: {
    clickThumbHandler (e) {
      this.startDrag(e)
      this[this.bar.axis] = (e.currentTarget[this.bar.offset] - (e[this.bar.client] - e.currentTarget.getBoundingClientRect()[this.bar.direction]))
    },

    clickTrackHandler (e) {
      const offset = Math.abs(e.target.getBoundingClientRect()[this.bar.direction] - e[this.bar.client])
      const thumbHalf = (this.$refs.thumb[this.bar.offset] / 2)
      const thumbPositionPercentage = ((offset - thumbHalf) * 100 / this.$el[this.bar.offset])

      this.wrap[this.bar.scroll] = (thumbPositionPercentage * this.wrap[this.bar.scrollSize] / 100)
    },

    startDrag (e) {
      e.stopImmediatePropagation()
      this.cursorDown = true

      on(document, 'mousemove', this.mouseMoveDocumentHandler)
      on(document, 'mouseup', this.mouseUpDocumentHandler)
      document.onselectstart = () => false
    },

    mouseMoveDocumentHandler (e) {
      if (this.cursorDown === false) return
      const prevPage = this[this.bar.axis]

      if (!prevPage) return

      const offset = ((this.$el.getBoundingClientRect()[this.bar.direction] - e[this.bar.client]) * -1)
      const thumbClickPosition = (this.$refs.thumb[this.bar.offset] - prevPage)
      const thumbPositionPercentage = ((offset - thumbClickPosition) * 100 / this.$el[this.bar.offset])

      this.wrap[this.bar.scroll] = (thumbPositionPercentage * this.wrap[this.bar.scrollSize] / 100)
    },

    mouseUpDocumentHandler (e) {
      this.cursorDown = false
      this[this.bar.axis] = 0
      off(document, 'mousemove', this.mouseMoveDocumentHandler)
      document.onselectstart = null
    }
  },

  destroyed () {
    off(document, 'mouseup', this.mouseUpDocumentHandler)
  }
}
