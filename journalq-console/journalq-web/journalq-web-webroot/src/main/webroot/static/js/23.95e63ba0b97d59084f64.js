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
webpackJsonp([23],{"+UHt":function(t,e){},O6CP:function(t,e,a){"use strict";Object.defineProperty(e,"__esModule",{value:!0});var n=a("T0gc"),i=a("fo4W"),o=a("95hR"),r={name:"application",components:{myTable:n.a,myDialog:i.a},mixins:[o.a],data:function(){return{urls:{search:"/application/"+this.$route.query.id+"/config_privileges/search"},searchRules:{},tableData:{rowData:[],colData:[{title:"配置",key:"configuration.code"},{title:"权限",key:"authority"},{title:"生效时间",key:"effectiveTime"},{title:"失效时间",key:"expirationTime"}]}}},computed:{},methods:{},mounted:function(){}},c={render:function(){var t=this,e=t.$createElement,a=t._self._c||e;return a("div",[a("div",{staticClass:"ml20 mt30"},[a("d-button",{attrs:{type:"primary"},on:{click:t.getList}},[t._v("刷新"),a("icon",{staticStyle:{"margin-left":"5px"},attrs:{name:"rotate-cw"}})],1)],1),t._v(" "),a("my-table",{attrs:{data:t.tableData,showPin:t.showTablePin,page:t.page},on:{"on-size-change":t.handleSizeChange,"on-current-change":t.handleCurrentChange}})],1)},staticRenderFns:[]};var l=a("VU/8")(r,c,!1,function(t){a("+UHt")},"data-v-fad6cbd8",null);e.default=l.exports}});
//# sourceMappingURL=23.95e63ba0b97d59084f64.js.map