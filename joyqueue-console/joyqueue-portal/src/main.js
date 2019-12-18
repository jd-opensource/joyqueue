// The Vue build version to load with the `import` command
// (runtime-only or standalone) has been set in webpack.base.conf with an alias.
import './assets/css/dui.css'
import './assets/css/iconfont.css'
import './assets/css/iconfont.js'
import Promise from 'es6-promise'
import Vue from 'vue'
import store from './store'
import router from './router'
import App from './App'
import DUI from './components'
import './filter' // 过滤器

import i18n from './i18n'

DUI.i18n((key, value) => i18n.t(key, value))

Promise.polyfill()
Vue.use(DUI)

Vue.config.productionTip = false

/* eslint-disable no-new */
new Vue({
  el: '#app',
  router,
  store,
  i18n,
  components: { App },
  template: '<App/>'
})
