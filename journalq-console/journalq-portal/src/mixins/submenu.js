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
import compoLang from '../i18n/components.json'

export default {
  data () {
    return {
      menu: '',
      styleDark: 'min-height: 400px; background: #2C405A;',
      styleLight: 'min-height: 400px; background: #ffffff; padding-bottom: 15px;',
      style: 'min-height: 400px; background: #2C405A;',
      theme: 'light',
      submenu: '',
      activeName: '',
      menuFixedExpand: false,
      spanLeftFold: 1,
      spanLeftExpand: 4,
      spanLeft: 1,
      spanRightFold: 23,
      spanRightExpand: 20,
      spanRight: 23
    }
  },
  computed: {
    iconSize () {
      return this.spanLeft === this.spanLeftExpand ? 14 : 18
    },
    curLang () {
      return this.$i18n.locale
    },
    langConfig () {
      return compoLang[this.curLang][this.menu]
    },
    langHeaderConfig () {
      return compoLang[this.curLang]['header']
    }
  },
  methods: {
    toggleClick () {
      if (this.menuFixedExpand) {
        this.menuFixedExpand = false
        this.foldMenu()
      } else {
        this.menuFixedExpand = true
        this.theme = 'light'
        this.style = this.styleLight
        this.spanLeft = this.spanLeftExpand
        this.spanRight = this.spanRightExpand
      }
    },
    expandMenu () {
      if (!this.menuFixedExpand) {
        this.theme = 'dark'
        this.style = this.styleDark
        this.spanLeft = this.spanLeftExpand
        this.spanRight = this.spanRightExpand
      }
    },
    foldMenu () {
      if (!this.menuFixedExpand) {
        this.theme = 'dark'
        this.style = this.styleDark
        this.spanLeft = this.spanLeftFold
        this.spanRight = this.spanRightFold
      }
    }
  },
  watch: {
    $route: {
      handler (to, from) {
        this.submenu = compoLang[this.curLang][this.menu][to.meta.title]
        this.activeName = to.meta.title
      }
    },
    '$i18n.locale': {
      handler (cur, old) {
        this.submenu = compoLang[cur][this.menu][this.$route.meta.title]
      }
    }
  },
  mounted () {
    this.submenu = compoLang[this.curLang][this.menu][this.$route.meta.title]
    this.activeName = this.$route.meta.title
  }
}
