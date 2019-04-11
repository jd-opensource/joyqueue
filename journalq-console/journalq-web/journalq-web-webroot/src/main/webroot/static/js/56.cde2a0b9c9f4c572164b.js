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
webpackJsonp([56],{JjQz:function(e,t){},a4Fw:function(e,t,i){"use strict";Object.defineProperty(t,"__esModule",{value:!0});var r=i("xXvx"),c=i("X2Oc"),n={name:"producer",components:{producerBase:r.default},props:{search:{type:Object}},data:function(){var e=this;return{keywordTip:"请输入应用",colData:[{title:"应用",key:"app.code",formatter:function(e){return Object(c.d)(e.app,e.subscribeGroup)}},{title:"主题",key:"topic.code",formatter:function(e){return Object(c.i)(e.topic,e.namespace)}},{title:"连接数",key:"connections"},{title:"入队数",key:"enQuence.count"},{title:"生产权重",key:"config.weight"},{title:"限制IP发送",key:"config.blackList"},{title:"单线程发送",key:"config.single",render:function(e,t){return Object(c.o)(e,void 0===t.item.config?void 0:t.item.config.single)}},{title:"归档",key:"config.archive",render:function(e,t){return Object(c.j)(e,void 0===t.item.config?void 0:t.item.config.archive)}},{title:"就近发送",key:"config.nearBy",render:function(e,t){return Object(c.j)(e,void 0===t.item.config?void 0:t.item.config.nearBy)}},{title:"客户端类型",key:"clientType",render:function(e,t){return Object(c.b)(e,t.item.clientType)}}],subscribeDialog:{colData:[{title:"ID",key:"id"},{title:"应用代码",key:"code"},{title:"客户端类型",key:"clientType",render:function(t,i){return Object(c.c)(t,i,e.$refs.producerBase.$refs.subscribe)}}],urls:{add:"/producer/add",search:"/application/unsubscribed/search"}},detailDialog:{partition:{colData:[]}}}},methods:{getList:function(){this.$refs.producerBase.getList()}}},o={render:function(){var e=this.$createElement,t=this._self._c||e;return t("div",[t("producer-base",{ref:"producerBase",attrs:{keywordTip:this.keywordTip,colData:this.colData,subscribeDialogColData:this.subscribeDialog.colData,search:this.search,subscribeUrls:this.subscribeDialog.urls}})],1)},staticRenderFns:[]};var a=i("VU/8")(n,o,!1,function(e){i("JjQz")},"data-v-16c32354",null);t.default=a.exports}});
//# sourceMappingURL=56.cde2a0b9c9f4c572164b.js.map