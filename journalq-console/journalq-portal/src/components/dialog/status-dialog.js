import Vue from 'vue'
import Config from '../../config'
import Dialog from './dialog.vue'
import {t} from '../../locale'

const DialogConstructer = Vue.extend(Dialog)

let currentModal
let instance
let dialogQueue = []

const defaults = {
  title: '',
  content: '',
  type: ''
}

const defultCallback = action => {
  if (currentModal) {
    const callback = currentModal.callback
    if (typeof callback === 'function') {
      if (instance.showInput) {
        callback(instance.inputValue, action)
      } else {
        callback(action)
      }
    }

    if (currentModal.resolve) {
      const type = currentModal.options.type
      if (type === 'confirm' || type === 'prompt') {
        if (action === 'confirm') {
          if (instance.showInput) {
            currentModal.resolve({ value: instance.inputValue, action })
          } else {
            currentModal.resolve(action)
          }
        } else if (action === 'cancel' && currentModal.reject) {
          currentModal.reject(action)
        }
      } else {
        currentModal.resolve(action)
      }
    }
  }
}

const initInstance = () => {
  instance = new DialogConstructer({
    el: document.createElement('div')
  })

  instance.callback = defultCallback
}

const showNextModal = () => {
  /**
   * TODO: 给每个框添加 fid ，避免不停的新建对话框
   */
  // if (!instance) {
  //   initInstance()
  // }
  initInstance()
  instance.action = ''

  if (!instance.visible && dialogQueue.length) {
    currentModal = dialogQueue.shift()

    const options = currentModal.options
    for (const prop in options) {
      if (options.hasOwnProperty(prop)) {
        instance[prop] = options[prop]
      }
    }

    if (typeof options.callback !== 'function') {
      instance.callback = defultCallback
    }

    const oldCallback = instance.callback
    instance.callback = (action, instance) => {
      oldCallback(action, instance)
      showNextModal()
    }

    // ['maskClosable', 'showCancelButton', 'showClose', 'showCancelButton'].forEach(prop => {
    //   if (typeof instance[prop] === 'undefined') {
    //     instance[prop] = true
    //   }
    // })

    document.body.appendChild(instance.$el)

    Vue.nextTick(() => {
      instance.visible = true
    })
  }
}

const StatusDialog = (options, callback) => {
  if (Vue.prototype.$isServer) return
  if (options.callback && !callback) {
    callback = options.callback
  }

  if (typeof Promise !== 'undefined') {
    return new Promise((resolve, reject) => {
      dialogQueue.push({
        options: Object.assign({}, defaults, options),
        callback,
        resolve,
        reject
      })

      showNextModal()
    })
  }

  dialogQueue.push({
    options: Object.assign({}, defaults, options),
    callback
  })

  showNextModal()
}

StatusDialog.close = () => {
  instance.visible = false
  dialogQueue = []
  currentModal = null
}

/**
 * Such like window.alert
 */
StatusDialog.alert = (content, title, options) => {
  if (typeof content === 'object') {
    options = content
    content = options.content
    title = options.title || ''
  }

  return StatusDialog(Object.assign({
    title,
    content,
    type: 'alert',
    maskClosable: false,
    showCancelButton: false,
    autoClosable: true
  }, options))
}

/**
 * Such like window.confirm
 */
StatusDialog.confirm = (content, title, options) => {
  if (typeof content === 'object') {
    options = content
    content = options.content
    title = options.title || ''
  }

  return StatusDialog(Object.assign({
    title,
    content,
    type: 'confirm',
    autoClosable: true
  }, options))
}

/**
 * Such like window.prompt
 */
StatusDialog.prompt = (content, title, options) => {
  if (typeof content === 'object') {
    options = content
    content = options.content
    title = options.title || ''
  }

  return StatusDialog(Object.assign({
    title,
    content,
    type: 'prompt',
    showInput: true,
    autoClosable: true
  }, options))
}

/**
 * Status Dialog
 */
function createStatusDialog (type) {
  return (content, title, options) => {
    if (typeof content === 'object') {
      options = content
      content = options.content
      title = options.title || t(`${Config.localePrefix}.dialog.${type}`)
    }

    return StatusDialog(Object.assign({
      title,
      content,
      type,
      maskClosable: false,
      showCancelButton: false,
      showClose: false,
      autoClosable: true
    }, options))
  }
}

StatusDialog.info = createStatusDialog('info')
StatusDialog.success = createStatusDialog('success')
StatusDialog.warning = createStatusDialog('warning')
StatusDialog.error = createStatusDialog('error')

export default StatusDialog
