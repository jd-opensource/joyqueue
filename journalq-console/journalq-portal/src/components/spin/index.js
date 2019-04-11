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
import Spin from './spin.js'

let spinInstance

function getSpinInstance (render = undefined) {
  spinInstance = spinInstance || Spin.newInstance({
    render: render
  })

  return spinInstance
}

function loading (options) {
  const render = ('render' in options) ? options.render : undefined
  let instance = getSpinInstance(render)

  instance.show(options)
}

Spin.show = function (props = {}) {
  return loading(props)
}
Spin.hide = function () {
  if (!spinInstance) return false

  const instance = getSpinInstance()

  instance.remove(() => {
    spinInstance = null
  })
}

export default Spin
