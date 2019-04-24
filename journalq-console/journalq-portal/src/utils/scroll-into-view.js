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
import Vue from 'vue'

export default function scrollIntoView (container, selected) {
  if (Vue.prototype.$isServer) return

  if (!selected) {
    container.scrollTop = 0
    return
  }

  const offsetParents = []
  let pointer = selected.offsetParent
  while (pointer && container !== pointer && container.contains(pointer)) {
    offsetParents.push(pointer)
    pointer = pointer.offsetParent
  }
  const top = selected.offsetTop + offsetParents.reduce((prev, curr) => (prev + curr.offsetTop), 0)
  const bottom = top + selected.offsetHeight
  const viewRectTop = container.scrollTop
  const viewRectBottom = viewRectTop + container.clientHeight

  if (top < viewRectTop) {
    container.scrollTop = top
  } else if (bottom > viewRectBottom) {
    container.scrollTop = bottom - container.clientHeight
  }
}
