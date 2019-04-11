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
webpackJsonp([4],{q4K0:function(t,e,a){"use strict";Object.defineProperty(e,"__esModule",{value:!0});var n={components:{},mixins:[a("yFfS").a],data:function(){return{menu:"workflow"}},mounted:function(){},computed:{curLang:function(){return this.$i18n.locale}}},o={render:function(){var t=this,e=t.$createElement,a=t._self._c||e;return a("div",{staticClass:"layout",class:{"layout-hide-text":t.spanLeft===t.spanLeftFold}},[a("grid-row",[a("grid-col",{style:t.style,attrs:{span:t.spanLeft}},[a("div",{on:{mouseover:t.expandMenu,mouseleave:t.foldMenu}},[a("d-menu",{attrs:{"active-name":t.activeName,theme:t.theme,width:"auto"}},[a("div",{staticClass:"layout-logo-left"}),t._v(" "),a("d-menu-item",{attrs:{name:"apply",icon:"file-plus",color:"#98D358",size:t.iconSize,to:"/"+t.curLang+"/workflow/apply"}},[a("span",{staticClass:"layout-text"},[t._v(t._s(t.langConfig.apply))])]),t._v(" "),a("d-menu-item",{attrs:{name:"audit",icon:"check-square",color:"#7FB3D5",size:t.iconSize,to:"/"+t.curLang+"/workflow/audit"}},[a("span",{staticClass:"layout-text"},[t._v(t._s(t.langConfig.audit))])]),t._v(" "),a("d-menu-item",{attrs:{name:"apiToken",to:"/"+t.curLang+"/workflow/apiToken"}},[a("icon",{attrs:{name:"box",color:"#7FB3D5",size:t.iconSize}}),t._v(" "),a("span",{staticClass:"layout-text"},[t._v(t._s(t.langConfig.apiToken))])],1)],1)],1)]),t._v(" "),a("grid-col",{attrs:{span:t.spanRight}},[a("div",{staticClass:"layout-header"},[a("d-button",{staticClass:"toggle-btn",attrs:{type:"borderless",icon:"menu"},on:{click:t.toggleClick}}),t._v(" "),a("div",{staticClass:"layout-breadcrumb"},[a("d-breadcrumb",[a("d-breadcrumb-item",{attrs:{href:"#"}},[t._v(t._s(t.langHeaderConfig.workflow))]),t._v(" "),a("d-breadcrumb-item",[t._v(t._s(t.submenu))])],1)],1)],1),t._v(" "),a("div",{staticClass:"layout-content"},[a("router-view")],1)])],1)],1)},staticRenderFns:[]};var s=a("VU/8")(n,o,!1,function(t){a("s8C3")},"data-v-570faa04",null);e.default=s.exports},s8C3:function(t,e){}});
//# sourceMappingURL=4.5ff8a61713b8b4e2dd26.js.map