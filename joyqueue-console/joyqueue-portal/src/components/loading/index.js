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
import Loading from './loading.js'

let loadingInstance
let width = '100%'
let height = '100%'
let top = 0
let showPercent = false
let timer

function getLoadingInstance (props) {
  props = props || {}
  loadingInstance = loadingInstance || Loading.newInstance({
    width: props.width || width,
    height: props.height || height,
    top: props.top || top,
    showPercent: props.showPercent || showPercent
  })
  return loadingInstance
}

function update (options, props) {
  let instance = getLoadingInstance(props)
  instance.update(options)
}

function hide () {
  setTimeout(() => {
    update({
      show: false
    })
    setTimeout(() => {
      update({
        percent: 0
      })
    }, 200)
  }, 400)
}

function clearTimer () {
  if (timer) {
    clearInterval(timer)
    timer = null
  }
}

const prefixName = 'Luban'
export default {
  name: prefixName + `Loading`,
  props: {

  },
  start (props) {
    if (timer) return
    let percent = 0
    update({
      percent: percent,
      show: true
    }, props)
  },
  update (percent) {
    clearTimer()
    update({
      percent: percent,
      show: true
    })
  },
  finish () {
    clearTimer()
    // update({
    //   percent: 100,
    //   show: true
    // });
    if (!loadingInstance) {
      return
    }
    hide()
  },
  error () {
    clearTimer()
    // update({
    //   percent: 100,
    //   show: true
    // });
    hide()
  },
  config (options) {
    if (options.width) {
      width = options.width
    }
    if (options.height) {
      height = options.height
    }
    if (options.top) {
      top = options.top
    }
    if (options.showPercent) {
      showPercent = options.showPercent
    }
  },
  destroy () {
    clearTimer()
    let instance = getLoadingInstance()
    loadingInstance = null
    instance.destroy()
  }
}
