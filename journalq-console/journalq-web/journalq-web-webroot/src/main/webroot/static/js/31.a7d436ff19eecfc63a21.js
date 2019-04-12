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
webpackJsonp([31],{XB5r:function(t,e,a){"use strict";Object.defineProperty(e,"__esModule",{value:!0});var n=a("1a0f"),r=a("T0gc"),o=a("95hR"),i={name:"",components:{myTable:r.a},props:{data:{type:Object,default:function(){return{}}},group:{type:Object,default:function(){return{}}}},mixins:[o.a],data:function(){return{urls:{search:"/broker/search",addBroker:"/broker/update"},searchData:{brokerGroupId:-1},tableData:{rowData:[{}],colData:[{title:"ID",key:"id"},{title:"IP",key:"ip"},{title:"端口",key:"port"},{title:"分组",key:"group.code"}],btns:[{txt:"添加",method:"on-add",bindKey:"status",bindVal:1}]},multipleSelection:[]}},computed:{addBrokerUrl:function(){return this.urlOrigin.addBroker}},methods:{addBroker:function(t){var e={id:t.id,group:this.group},a=this;this.$Dialog.confirm({title:"提示",content:"确定要添加吗？"}).then(function(){n.a.put(a.addBrokerUrl+"/"+t.id,null,e).then(function(t){a.getList()})}).catch(function(){})}},mounted:function(){}},c={render:function(){var t=this,e=t.$createElement,a=t._self._c||e;return a("div",[a("div",{staticClass:"ml20 mt10"},[a("d-input",{staticClass:"left",staticStyle:{width:"60%"},attrs:{placeholder:"请输入要查询的IP/名称"},model:{value:t.searchData.keyword,callback:function(e){t.$set(t.searchData,"keyword",e)},expression:"searchData.keyword"}},[a("icon",{attrs:{slot:"suffix",name:"search",size:"14",color:"#CACACA"},on:{click:t.getList},slot:"suffix"})],1)],1),t._v(" "),a("my-table",{attrs:{data:t.tableData,showPin:t.showTablePin,page:t.page},on:{"on-size-change":t.handleSizeChange,"on-current-change":t.handleCurrentChange,"on-selection-change":t.handleSelectionChange,"on-add":t.addBroker}})],1)},staticRenderFns:[]};var d=a("VU/8")(i,c,!1,function(t){a("fR0Z")},"data-v-7a3b429c",null);e.default=d.exports},fR0Z:function(t,e){}});
//# sourceMappingURL=31.a7d436ff19eecfc63a21.js.map