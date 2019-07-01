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
import Spin from './spin.vue'
import Config from '../../config'

const fullscreenCls = `${Config.clsPrefix}spin-fullscreen`

Spin.newInstance = properties => {
  const _props = properties || {}

  const Instance = new Vue({
    data: Object.assign({}, _props, {

    }),
    render (h) {
      let vnode = ''
      if (this.render) {
        vnode = h(Spin, {
          props: {
            fix: true,
            fullscreen: true
          }
        }, [this.render(h)])
      } else {
        vnode = h(Spin, {
          props: {
            size: 'large',
            fix: true,
            fullscreen: true
          }
        })
      }
      const classes = `${fullscreenCls} ${fullscreenCls}-wrapper`
      return h('div', {
        'class': classes
      }, [vnode])
    }
  })

  const component = Instance.$mount()
  document.body.appendChild(component.$el)
  const spin = Instance.$children[0]

  return {
    show () {
      spin.visible = true
    },
    remove (cb) {
      spin.visible = false
      setTimeout(function () {
        spin.$parent.$destroy()
        if (document.getElementsByClassName(`${fullscreenCls}`)[0] !== undefined) {
          document.body.removeChild(document.getElementsByClassName(`${fullscreenCls}`)[0])
        }
        cb()
      }, 500)
    },
    component: spin
  }
}

export default Spin
