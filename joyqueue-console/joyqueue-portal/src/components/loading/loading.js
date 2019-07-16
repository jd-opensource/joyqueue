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
import Loading from './loading.vue'
import Config from '../../config'

Loading.newInstance = properties => {
  const _props = properties || {}
  const Instance = new Vue({
    data: _props,
    render (h) {
      return h(Loading, {
        props: _props
      })
    }
  })
  const component = Instance.$mount()
  document.body.appendChild(component.$el)
  const loading = Instance.$children[0]

  return {
    update (options) {
      if ('percent' in options) {
        loading.percent = options.percent
      }
      if ('show' in options) {
        loading.show = options.show
      }
    },
    component: loading,
    destroy () {
      document.body.removeChild(document.getElementsByClassName(`${Config.clsPrefix}loading`)[0])
    }
  }
}

export default Loading
