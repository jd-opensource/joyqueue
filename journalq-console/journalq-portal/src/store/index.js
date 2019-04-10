import Vue from 'vue'
import Vuex from 'vuex'
import apiRequest from '../utils/apiRequest.js'
import apiUrl from '../utils/apiUrl.js'

Vue.use(Vuex)
/* 配置全局数据 */
const state = {
  loginUserName: '',
  loginUserRole: '',
  isAdmin: false,
  iconMenu: false,
  isTest: false,
  testHostName: 'test.mq.jd.com',
  subscribeType: {
    producerType: 1,
    consumerType: 2
  },
  formType: {
    addFormType: 0,
    editFormType: 1
  },
  responseCode: {
    noTipCode: 100,
    successCode: 200,
    validationCode: 300
  },
  form: {
    placeholder: {
      code: '仅支持字母、数字、- 和 _ ，首字符为字母',
      name: '仅支持中文、数字、字母、-和_，首字符汉字或字母',
      subscribeGroup: '可输入，支持大小写字母、数字、_  -，首字母,长度至少3位'
    },
    pattern: {
      subscribeGroup: /^[a-zA-Z]+[a-zA-Z0-9_-]{1,20}[a-zA-Z0-9]+$/
    }
  },
  page: {
    maxSize: 0x7fffffff
  },
  cookieName: 'sso.jd.com',
  urls: {
    performance: [
      {'ump2.1.1+': 'http://ump.jd.com/performanceReport/initPage.action?queryMap.accessKey=jmq-servers.client.(consumer.)send.[app].[topic]'},
      {'ump2.1.0-': 'http://ump.jd.com/performanceReport/initPage.action?queryMap.appName=jmq-server&queryMap.appId=5357&queryMap.accessKey=jmq-server.client.send.(consumer.)[app].[topic]&queryMap.analysisFrequency=null&queryMap.departCode=3616&queryMap.groupId='}
    ]
  }
}

const getters = {
  loginUserName: state => {
    return state.loginUserName
  },
  loginUserRole: state => {
    return state.loginUserRole
  },
  isAdmin: state => {
    return state.loginUserRole === 1
  },
  isTest: state => {
    return window.location.hostname === state.testHostName
  },
  iconMenu: state => {
    return state.iconMenu
  },
  producerType: state => {
    return state.subscribeType.producerType
  },
  consumerType: state => {
    return state.subscribeType.consumerType
  },
  addFormType: state => {
    return state.formType.addFormType
  },
  editFormType: state => {
    return state.formType.editFormType
  },
  noTipCode: state => {
    return state.responseCode.noTipCode
  },
  successCode: state => {
    return state.responseCode.successCode
  },
  validationCode: state => {
    return state.responseCode.validationCode
  },
  placeholder: state => {
    return state.form.placeholder
  },
  maxPageSize: state => {
    return state.page.maxSize
  },
  cookieName: state => {
    return state.cookieName
  },
  pattern: state => {
    return state.form.pattern
  },
  urls: state => {
    return state.urls
  }
}
const mutations = {
  setLoginUser (state, data) {
    state.loginUserName = data.loginUserName
    state.loginUserRole = data.loginUserRole
  },
  setIconMenu (state, data) {
    state.iconMenu = data
  }
}
const actions = {
  getUserInfo ({commit, state}) {
    return new Promise((resolve, reject) => {
      // commit('setLoginUser', {loginUserName: 'test', loginUserRole: 1})
      apiRequest.get(apiUrl.userInfo).then(function (data) {
        if (data && data.data) {
          commit('setLoginUser', {loginUserName: data.data.code, loginUserRole: data.data.role})
          resolve({loginUserName: data.data.code, loginUserRole: data.data.role})
        }
      }, function () {

      })
    })
  }
}
export default new Vuex.Store({
  state,
  getters,
  mutations,
  actions
})

export {state, getters, mutations, actions}
