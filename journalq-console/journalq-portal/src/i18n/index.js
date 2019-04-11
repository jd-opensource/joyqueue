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
import VueI18n from 'vue-i18n'
import LangConfig from './langs.json'
import {mergeDeep} from '../utils/assist'

Vue.use(VueI18n)

// I18n可配置化扩展
export function getI18nOptions (...extendConfigs) {
  // 深度合并配置
  const langConfig = mergeDeep(LangConfig, ...extendConfigs)
  const languages = Object.keys(langConfig)
  const defaultLang = languages[0]

  const pattern = new RegExp('/#/(' + languages.join('|') + ')/')
  const matchArr = window.location.href.match(pattern)
  const urlLang = matchArr && matchArr[1]

  let navigatorLang = window.navigator.language.slice(0, 2)
  if (languages.indexOf(navigatorLang) <= -1) {
    navigatorLang = ''
  }

  const userLang = urlLang || window.localStorage.getItem('journalq-language') || navigatorLang || defaultLang

  const messages = {}
  languages.forEach(language => {
    messages[language] = require('../locale/lang/' + language + '.js').default
  })

  return {
    locale: userLang,
    fallbackLocale: defaultLang,
    messages: messages
  }
}

const i18nOptions = getI18nOptions()

export default new VueI18n(i18nOptions)
