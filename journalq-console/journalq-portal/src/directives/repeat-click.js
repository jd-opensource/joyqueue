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
import {once, on} from '../utils/assist'

export default {
  bind (el, binding, vnode) {
    let interval = null
    let startTime
    const handler = () => vnode.context[binding.expression].apply()
    const clear = () => {
      if (new Date() - startTime < 100) {
        handler()
      }
      clearInterval(interval)
      interval = null
    }

    on(el, 'mousedown', (e) => {
      if (e.button !== 0) return
      startTime = new Date()
      once(document, 'mouseup', clear)
      clearInterval(interval)
      interval = setInterval(handler, 100)
    })
  }
}
