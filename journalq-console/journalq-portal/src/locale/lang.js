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
// using with vue-i18n in CDN
/* eslint-disable */
import Vue from 'vue';

const isServer = Vue.prototype.$isServer;

export default function (lang) {
  if (!isServer) {
    if (typeof window.dui !== 'undefined') {
      if (!('langs' in dui)) {
        dui.langs = {};
      }
      dui.langs[lang.i.locale] = lang;
    }
  }
};
/* eslint-enable */
