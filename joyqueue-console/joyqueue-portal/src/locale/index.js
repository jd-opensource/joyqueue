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
import defaultLang from './lang/zh-CN'
import Vue from 'vue'
import deepmerge from 'deepmerge'
import Format from './format'

const format = Format(Vue)
let lang = defaultLang
let merged = false
let i18nHandler = function () {
  const vuei18n = Object.getPrototypeOf(this || Vue).$t
  if (typeof vuei18n === 'function' && !!Vue.locale) {
    if (!merged) {
      merged = true
      Vue.locale(
        Vue.config.lang,
        deepmerge(lang, Vue.locale(Vue.config.lang) || {}, {clone: true})
      )
    }
    return vuei18n.apply(this, arguments)
  }
}

export const t = function (path, options) {
  let value = i18nHandler.apply(this, arguments)
  if (value !== null && value !== undefined) return value

  const array = path.split('.')
  let current = lang

  for (let i = 0, j = array.length; i < j; i++) {
    const property = array[i]
    value = current[property]
    if (i === j - 1) return format(value, options)
    if (!value) return ''
    current = value
  }
  return ''
}

export const use = function (l) {
  lang = l || lang
}

export const i18n = function (fn) {
  i18nHandler = fn || i18nHandler
}

export default {use, t, i18n}
