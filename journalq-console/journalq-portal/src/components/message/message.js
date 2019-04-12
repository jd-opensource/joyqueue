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
import MessageVue from './message.vue'

const MessageConstructor = Vue.extend(MessageVue)

const messageType = ['info', 'success', 'warning', 'error', 'loading']
const instances = []
let seed = 1
let zindexSeed = 1010

const Message = options => {
  if (Vue.prototype.$isServer) return
  options = options || {}

  if (typeof options === 'string') {
    options = {
      message: options
    }
  }

  const customCloseFunc = options.onClose
  const id = `message_${seed++}`

  options.onClose = () => {
    Message.close(id, customCloseFunc)
  }

  const instance = new MessageConstructor({
    data: options
  })

  instance.id = id
  instance.vm = instance.$mount()
  document.body.appendChild(instance.vm.$el)
  instance.vm.visible = true
  instance.dom = instance.vm.$el
  instance.dom.style.zIndex = zindexSeed++

  const offset = 0
  const len = instances.length
  let topDist = offset

  for (let i = 0; i < len; i++) {
    topDist += instances[i].$el.offsetHeight + 8
  }

  topDist += 8
  instance.top = topDist

  instances.push(instance)

  // 返回关闭方法，用于手动消除
  return function () {
    instance.vm.close(id)
  }
}

Message.close = (id, customCloseFunc) => {
  const len = instances.length
  let index, removedHeight

  for (let i = 0; i < len; i++) {
    if (id === instances[i].id) {
      if (typeof customCloseFunc === 'function') {
        customCloseFunc(instances[i])
      }
      index = i
      removedHeight = instances[i].dom.offsetHeight
      instances.splice(i, 1)
      break
    }
  }

  if (len > 1) {
    for (let i = index; i < len - 1; i++) {
      instances[i].dom.style.top = `${parseInt(instances[i].dom.style.top) - removedHeight - 8}px`
    }
  }
}

Message.closeAll = () => {
  instances.forEach((elem, idx) => {
    elem.close()
  })
}

messageType.forEach(type => {
  Message[type] = options => {
    if (typeof options === 'string') {
      options = {
        message: options
      }
    }
    options.type = type
    return Message(options)
  }
})

export default Message
