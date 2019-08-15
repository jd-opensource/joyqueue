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
