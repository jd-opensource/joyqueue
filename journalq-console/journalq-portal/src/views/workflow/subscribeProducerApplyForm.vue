<template>
  <div>
    <d-steps :current='current'>
      <d-step title="步骤1" description="应用信息"></d-step>
      <d-step title="步骤2" description="主题信息"></d-step>
      <d-step title="步骤3" description="Broker列表"></d-step>
    </d-steps>
    <div class="steps-content" style="margin-top: 15px; border: 1px solid #e9e9e9; border-radius: 6px;background-color:
    #fafafa; min-height: 200px; text-align: left; padding: 20px 30px 40px 50px;">
        <div class="step1" v-show="current==0">
          <div class="stepForm1">
            <d-form ref="form1" :model="formData" :rules="rules.rule1" label-width="110px" style="padding-right: 20px">
              <d-form-item label="应用：" prop="app.code">
                <d-autocomplete
                  class="inline-input"
                  v-model="formData.app.code"
                  :error="error.appCode"
                  :fetch-suggestions="queryApp"
                  placeholder="请输入应用英文名"
                  @select="handleAppSelect"
                  style="width: 80%"
                >
                  <icon
                    name="edit-1"
                    slot="suffix">
                  </icon>
                  <template slot-scope="{ item }">
                    <div class="code" style="text-overflow: ellipsis;overflow: hidden;">{{ item.code }}</div>
                    <span class="id" style="font-size: 12px;color: #b4b4b4;">{{ 'id: '+item.id+', name: '+item.name}}</span>
                  </template>
                </d-autocomplete>
              </d-form-item>
              <!--<d-form-item label="预计上线时间：" prop="launchDate">-->
                <!--<d-date-picker-->
                  <!--v-model="formData.launchDate"-->
                  <!--align="right"-->
                  <!--type="date"-->
                  <!--placeholder="选择日期"-->
                  <!--:picker-options="pickerOptions"-->
                  <!--style="width: 60%"/>-->
              <!--</d-form-item>-->
              <d-form-item label="客户端类型：" prop="clientType">
                <d-select v-model.number="formData.clientType" style="width: 80%">
                  <d-option :value="0" >Jmq</d-option>
                  <d-option :value="1" >Kafka</d-option>
                  <d-option :value="2" >Mqtt</d-option>
                  <d-option :value="10" >Others</d-option>
                </d-select>
              </d-form-item>
            </d-form>
          </div>
          <div class="step-actions" style="text-align: center">
            <d-button type="primary" @click="next">下一步</d-button>
          </div>
        </div>
      <div class="step2" v-show="current==1">
        <div class="stepForm2">
          <d-form ref="form2" :model="formData" :rules="rules.rule2" label-width="110px" style="height: 350px; overflow-y:auto; width: 100%">
            <d-form-item label="主题英文名：" :error="error.topicCode" prop="topic.code">
              <d-input v-model="formData.topic.code" placeholder="仅支持英文字母大小写、数字、-、_和/" style="width: 70%"></d-input>
            </d-form-item>
            <!--<d-form-item label="主题中文名：" prop="topic.name">-->
              <!--<d-input v-model="formData.topicWiki.name" :placeholder="$store.getters.placeholder.name" style="width: 70%"></d-input>-->
            <!--</d-form-item>-->
            <!--<d-form-item label="命名空间：" prop="topic.namespace.code">-->
              <!--<d-select v-model="formData.topic.namespace.code" style="width: 50%">-->
                <!--<d-option v-for="item in namespaceList" :value="{id:item.id,code:item.code}" :key="item.id">{{ item.code }}</d-option>-->
              <!--</d-select>-->
            <!--</d-form-item>-->
            <d-form-item label="队列数量：" prop="topic.partitions">
              <d-input v-model.number="formData.topic.partitions" style="width: 70%"></d-input>
            </d-form-item>
            <d-form-item label="主题类型：" prop="topic.type">
              <d-select v-model.number="formData.topic.type" style="width: 70%">
                <d-option :value="0" >普通主题</d-option>
                <!--<d-option :value="1" >广播主题</d-option>-->
                <d-option :value="2" >顺序主题</d-option>
              </d-select>
            </d-form-item>
            <div v-if="formType">
              <d-form-item label="选举类型：" prop="topic.electType">
                <d-select v-model="formData.topic.electType" :value="0" style="width: 70%">
                  <d-option :value="0" >Raft</d-option>
                  <d-option :value="1" >Fix</d-option>
                </d-select>
              </d-form-item>
            </div>
            <d-form-item label="分组：" prop="topic.brokerGroup" >
              <d-select v-model="formData.topic.brokerGroup.id" name="brokerGroup" style="width: 70%" @on-change="handlerBrokerGroupChange">
                <d-option v-for="item in brokerGroupList" :value="item.id" :key="item.id">
                  <span>{{ item.name }}</span>
                  <span style="float:right;color:#ccc">{{ item.code }}</span>
                </d-option>
              </d-select>
            </d-form-item>
            <!--<d-form-item label="Broker：" prop="topic.brokers">-->
            <!--<d-checkbox-group v-model="formData.topic.brokers" name="brokers">-->
            <!--<d-checkbox v-for="item in brokerList" label="item.id" key="item.id">{{item.name}}</d-checkbox>-->
            <!--</d-checkbox-group>-->
            <!--</d-form-item>-->
            <d-form-item label="消息体大小：" prop="topicWiki.msgSize">
              <d-select v-model.number="formData.topicWiki.msgSize" :value="0" style="width: 70%">
                <d-option :value="0" >1k以下</d-option>
                <d-option :value="1" >1k到5k</d-option>
                <d-option :value="2" >5k到10k</d-option>
                <d-option :value="3" >10k到100k</d-option>
                <d-option :value="4" >100k到500k</d-option>
                <d-option :value="5" >500k以上</d-option>
              </d-select>
            </d-form-item>
            <d-form-item label="消息量(条/天)：" prop="topicWiki.msgAmount">
              <d-input v-model.number="formData.topicWiki.msgAmount" style="width: 70%"/>
            </d-form-item>
            <d-form-item label="备注：" prop="topic.description">
              <d-input type="textarea" rows="2" v-model="formData.topic.description"
                           placeholder="请输入备注说明，例如用途等" style="width: 70%"/>
            </d-form-item>
          </d-form>
        </div>
        <div class="step-actions" style="text-align: center">
          <d-button type="primary" @click="prev">上一步</d-button>
          <d-button type="primary" @click="next">下一步</d-button>
        </div>
      </div>
      <div class="step3" v-show="current==2">
        <div class="stepForm3">
          <d-form ref="form" :model="formData" :rules="rules.rule3" label-position="top"
                  label-width="100px" style="overflow-y:auto; height:350px">
            <d-form-item label=" " prop="topic.brokers">
              <add-broker ref="brokers" :model="formData.topic.brokers" @on-choosed-broker="choosedBroker" :data="formData.topic"/>
            </d-form-item>
          </d-form>
        </div>
        <div class="step-actions" style="text-align: center">
          <d-button type="primary" @click="prev">上一步</d-button>
          <d-button type="primary" @click="confirm">确定</d-button>
        </div>
        </div>
      </div>
    </div>
  </template>

<script>
import apiRequest from '../../utils/apiRequest.js'
import {deepCopy} from '../../utils/assist.js'
import {getNameRule} from '../../utils/common.js'
import addBroker from '../topic/addBroker.vue'
import apply from '../../mixins/apply.js'

export default {
  name: 'subscribe-producer-apply-form',
  mixins: [ apply ],
  components: {
    addBroker
  },
  props: {
    formType: 0, // 0:add form, 1: edit form
    data: {
      type: Object,
      default: function () {
        return {
          id: 0,
          app: {
            code: '',
            name: ''
          },
          topic: {
            code: '',
            name: '',
            type: 0,
            namespace: {
              id: '0',
              code: ''
            },
            partitions: 5,
            brokerGroup: {
              id: 0,
              code: '',
              name: ''
            },
            electType: 0,
            description: '',
            brokers: []
          },
          topicWiki: {
            msgSize: 0,
            msgAmount: 100
          },
          launchDate: '',
          clientType: 0,
          subscribeType: this.$store.getters.producerType
        }
      }
    }
  },
  data () {
    var validateBroker = (rule, value, callback) => {
      if (this.formData.topic.partitions !== undefined && this.formData.topic.brokers !== undefined &&
          this.formData.topic.brokers.length > this.formData.topic.partitions) {
        callback(new Error('勾选的broker数量不能大于队列数量'))
      } else {
        callback()
      }
    }
    return {
      current: 0,
      subscribeType: this.$store.getters.producerType,
      formData: this.data,
      dataCenterList: [],
      namespaceList: [],
      brokerGroupList: [],
      brokerList: [],
      urls: {
        findApp: `/application/search`,
        findAllDataCenter: `/datacenter/findAll`,
        findAllNamespace: `/namespace/findAll`,
        findAllBrokerGroup: `/brokerGroup/findAll`,
        searchBroker: '/broker/search',
        // addBroker: '/broker/update',
        add: '/subscribe/producer/apply',
        edit: '/subscribe/producer/update'
      },
      rules: {
        rule1: {
          'app.code': [
            {required: true, message: '请选择应用英文名', trigger: 'change'}
          ],
          // "launchDate": [
          //   {required: true, message: '请选择上线日期', trigger: 'change'}
          // ],
          clientType: [
            {required: true, message: '请选择客户端类型', trigger: 'change'}
          ]
        },
        rule2: {
          'topic.code': [
            {required: true, message: '请输入topic英文名', trigger: 'change'},
            {pattern: /^[a-zA-Z/]+[a-zA-Z0-9/_-]{1,120}[a-zA-Z0-9/]+$/, message: '英文名格式不匹配', trigger: 'change'}
          ],
          'topic.name': getNameRule(),
          'topic.partitions': [
            {required: true, message: '队列数量不能为空'},
            {type: 'number', message: '队列数量必须为数字值'}
          ],
          'topic.brokerGroup': [
            {required: true, message: '请选择一个分组', trigger: 'change'}
          ],
          'topicWiki.msgAmount': [
            {required: true, message: '请输入消息数量'},
            {type: 'number', message: '消息数量必须为数字'}
          ],
          'topic.type': [
            {required: true, message: '请输入主题类型', trigger: 'change'}
          ]
        },
        rule3: {
          'topic.brokers': [
            {type: 'array', required: true, message: '请至少选择一个Broker', trigger: 'change'},
            {validator: validateBroker, trigger: 'blur'}
          ]
        }
      },
      error: {
        appCode: '',
        topicCode: ''
      }
      // pickerOptions: {
      //   disabledDate(time) {
      //     return time.getTime() > Date.now();
      //   },
      //   shortcuts: [{
      //     text: '今天',
      //     onClick(picker) {
      //       picker.$emit('pick', new Date());
      //     }
      //   }, {
      //     text: '一周后',
      //     onClick(picker) {
      //       const date = new Date();
      //       date.setTime(date.getTime() + 3600 * 1000 * 24 * 7);
      //       picker.$emit('pick', date);
      //     }
      //   }, {
      //     text: '两周后',
      //     onClick(picker) {
      //       const date = new Date();
      //       date.setTime(date.getTime() + 3600 * 1000 * 24 * 14);
      //       picker.$emit('pick', date);
      //     }
      //   }, {
      //     text: '30天后',
      //     onClick(picker) {
      //       const date = new Date();
      //       date.setTime(date.getTime() + 3600 * 1000 * 24 * 30);
      //       picker.$emit('pick', date);
      //     }
      //   },]
      // },
    }
  },
  methods: {
    prev () {
      // Jump
      this.current = this.current - 1
    },
    next () {
      // Validate
      this.$refs['form' + (this.current + 1)].validate((valid) => {
        if (valid) {
          // Jump
          this.current = this.current + 1
        } else {
          this.$Message.error('验证不通过，请重新填写！')
        }
      })
    },
    choosedBroker (val) {
      this.formData.topic.brokers = val
    },
    beforeConfirm () {
      let copyData = deepCopy(this.formData)
      // let brokerGroups = copyData.topic.brokerGroups;
      // copyData.topic.brokerGroups = transStringListToObject(brokerGroups);
      return copyData
    },
    handlerBrokerGroupChange (data) {
      // this.formData.topic.brokerGroups = [data];
      this.$refs.brokers.getListByGroup(data)
    },
    getNamespaces () {
      apiRequest.get(this.urls.findAllNamespace).then((data) => {
        this.namespaceList = data.data || []
      })
    },
    getBrokerGroups () {
      apiRequest.get(this.urls.findAllBrokerGroup).then((data) => {
        this.brokerGroupList = []
        // admin user push item named all into list
        if (this.$store.getters.isAdmin) {
          let allItem = {id: 0, code: '', name: '全部'}
          this.brokerGroupList.push(allItem)
        }
        // push items of data into list
        (data.data || []).forEach(item => {
          this.brokerGroupList.push(item)
        })
        if (this.formType === this.$store.getters.editFormType) {
          this.fillBrokerGroup()
        }
      })
    },
    fillBrokerGroup () {
      for (var brokerGroup of this.brokerGroupList) {
        if (brokerGroup.id === this.formData.topic.brokerGroup.id) {
          this.formData.topic.brokerGroup.code = brokerGroup.code
          this.formData.topic.brokerGroup.name = brokerGroup.name
          // 选中broker
          this.$refs.brokers.getListByGroupAndbrokers(this.formData.topic.brokerGroup.id, this.formData.topic.brokers)
        }
      }
    }
  },
  mounted () {
    this.getApps()
    this.getNamespaces()
    this.getBrokerGroups()
  }
}
</script>

<style scoped>

</style>
