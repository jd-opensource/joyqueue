<template>
  <div>
    <d-steps :current='current'>
      <d-step title="步骤1" description="应用信息"></d-step>
      <d-step title="步骤2" description="主题信息"></d-step>
    </d-steps>
    <div class="steps-content" style="margin-top: 15px; border: 1px solid #e9e9e9; border-radius: 6px;background-color:
    #fafafa; min-height: 200px; text-align: left; padding: 20px 30px 40px 50px;">
      <span style="color: red;text-align: center;display: block;padding-bottom: 15px">注意：请先订阅生产者，再订阅消费者！</span>
      <div class="step1" v-show="current==0">
        <div class="stepForm1">
          <d-form ref="form1" :model="formData" :rules="rules.rule1" label-width="110px" style="padding-right: 20px">
            <d-form-item label="应用：" :error="error.appCode" prop="app.code">
              <d-autocomplete
                class="inline-input"
                v-model="formData.app.code"
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
            <d-form-item label="消息类型：" prop="topicType">
              <d-select v-model.number="formData.topicType" style="width: 80%">
                <d-option :value="0" >普通消息</d-option>
                <d-option :value="1" >广播消息</d-option>
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
          <d-form ref="form" :model="formData" :rules="rules.rule2" label-width="110px" style="padding-right: 20px">
            <d-form-item label="主题：" :error="error.topicCode" prop="topic.code">
              <d-autocomplete
                class="inline-input"
                v-model="formData.topic.code"
                :fetch-suggestions="queryTopic"
                placeholder="请输入主题英文名"
                @select="handleTopicSelect"
                style="width: 80%"
              >
                <icon
                  name="edit-1"
                  slot="suffix">
                </icon>
                <template slot-scope="{ item }">
                  <span>{{ item.code }}</span>
                  <!--<div class="code" style="text-overflow: ellipsis;overflow: hidden;">{{ item.code }}</div>-->
                  <span style="float:right;color:#ccc">全名:{{item.id}}</span>
                  <!--<span class="id" style="font-size: 12px;color: #b4b4b4;">-->
                  <!--{{ 'namespace_id: '+item.namespace.id+', namespace_code: '+item.namespace.code}}-->
                  <!--</span>-->
                  <!--<span class="id" style="font-size: 12px;color: #b4b4b4;">-->
                  <!--{{ 'name: '+item.name}}-->
                  <!--</span>-->
                </template>
              </d-autocomplete>
            </d-form-item>
            <d-form-item :label="subscribeGroupLabel" :error="error.subscribeGroup" prop="subscribeGroup">
              <d-autocomplete
                class="inline-input"
                v-model="formData.subscribeGroup"
                :fetch-suggestions="querySubscribeGroup"
                :placeholder="$store.getters.placeholder.subscribeGroup"
                @select="handleSubscribeGroupSelect"
                style="width: 80%"
              >
                <icon
                  name="edit-1"
                  slot="suffix">
                </icon>
                <template slot-scope="{ item }">
                  <div class="code" style="text-overflow: ellipsis;overflow: hidden;">{{ item }}</div>
                </template>
              </d-autocomplete>

            </d-form-item>
            <d-form-item label="消费Tps(条)：" prop="topicWiki.consumerTps">
              <d-input v-model.number="formData.topicWiki.consumerTps" style="width: 80%"/>
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
import {getTopicCode} from '../../utils/common.js'
import apply from '../../mixins/apply.js'

export default {
  name: 'subscribe-consumer-apply-form',
  mixins: [ apply ],
  components: {
  },
  props: {
    formType: 0, // add or edit form
    data: {
      type: Object,
      default: function () {
        return {
          id: 0,
          app: {
            id: 0,
            code: ''
          },
          topic: {
            id: '0',
            code: '',
            namespace: {
              id: '0',
              code: ''
            },
            subscribeGroupExist: false
          },
          topicWiki: {
            consumerTps: 100
          },
          // launchDate: '',
          clientType: 0,
          topicType: 0,
          subscribeGroup: '',
          subscribeType: this.$store.getters.consumerType
        }
      }
    }
  },
  data () {
    var validateSubscribeGroup = (rule, value, callback) => {
      if (this.formData.topic.subscribeGroupExist && !this.formData.subscribeGroup) {
        callback(new Error('请输入订阅分组'))
      } else {
        callback()
      }
    }
    return {
      current: 0,
      subscribeType: this.$store.getters.consumerType,
      formData: this.data,
      timeout: null,
      urls: {
        findApp: `/application/search`,
        findTopic: `/topic/unsubscribed/search`,
        findSubscribeGroup: `/consumer/findAllSubscribeGroups`,
        add: '/subscribe/consumer/apply',
        edit: '/subscribe/consumer/update'
      },
      rules: {
        rule1: {
          'app.code': [
            {required: true, message: '请选择应用英文名', trigger: 'change'}
          ],
          // launchDate: [
          //   {required: true, message: '请选择上线日期', trigger: 'change'}
          // ],
          clientType: [
            {type: 'number', required: true, message: '请选择客户端类型', trigger: 'change'}
          ],
          topicType: [
            {type: 'number', required: true, message: '请选择消息类型', trigger: 'change'}
          ]
        },
        rule2: {
          'topic.code': [
            {required: true, message: '请选择主题英文名', trigger: 'change'}
          ],
          'topicWiki.consumerTps': [
            {required: true, message: '请输入消费Tps', trigger: 'change'},
            {type: 'number', message: '消费Tps必须为数字值'}
          ],
          subscribeGroup: [
            // {required: true, message: '请输入英文名', trigger: 'change'},
            {validator: validateSubscribeGroup, trigger: 'blur'},
            {pattern: this.$store.getters.pattern.subscribeGroup, message: '格式不匹配，长度至少3位', trigger: 'change'}
          ]
        }
      },
      subscribeGroupList: [],
      error: {
        appCode: '',
        topicCode: '',
        subscribeGroup: ''
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
          // query topic
          if (this.current === 0) {
            this.getTopics()
          }
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
    querySubscribeGroup (queryString, callback) {
      var subscribeGroups = this.subscribeGroupList
      var results = queryString ? subscribeGroups.filter(subscribeGroup => {
        return subscribeGroup.toLowerCase().indexOf(queryString.toLowerCase().trim()) === 0
      }) : subscribeGroups
      // 调用 callback 返回建议列表的数据
      callback(results)
    },
    handleSubscribeGroupSelect (item) {
      this.formData.subscribeGroup = item
    },
    getSubscribeGroups () {
      apiRequest.get(this.urls.findSubscribeGroup).then((data) => {
        this.subscribeGroupList = data.data || []
      })
    },
    handleAppSelect (item) {
      this.formData.app.id = item.id
      this.formData.app.code = item.code
    },
    getTopics (keyword) {
      return new Promise((resolve, reject) => {
        let obj = {
          pagination: {
            page: 1,
            size: 100
          },
          query: {
            subscribeType: this.$store.getters.consumerType,
            app: {
              id: this.formData.app.id,
              code: this.formData.app.code
            }
          }
        }
        if (!keyword) {
          obj.query.keyword = keyword
        }
        apiRequest.post(this.urls.findTopic, {}, obj).then((data) => {
          if (keyword === undefined || keyword.trim() === '') {
            this.topicList = (data.data || []).map(topic => {
              topic.code = getTopicCode(topic, topic.namespace)
              return topic
            })
          }
          if (data.code === this.$store.getters.successCode) {
            resolve((data.data || []).map(topic => {
              topic.code = getTopicCode(topic, topic.namespace)
              return topic
            }))
          } else {
            resolve([])
          }
        }).catch(error => {
          reject(error)
        })
      })
    },
    handleTopicSelect (item) {
      this.formData.topic.id = item.id
      this.formData.topic.code = item.code
      this.formData.topic.namespace.id = item.namespace.id
      this.formData.topic.namespace.code = item.namespace.code
      this.formData.topic.subscribeGroupExist = item.subscribeGroupExist
    },
    queryTopic (queryString, callback) {
      this.error.topicCode = ''
      // this.formData.topic.subscribeGroupExist = false;
      if (!queryString) {
        callback(this.topicList)
      } else {
        clearTimeout(this.timeout)
        this.getTopics(queryString).then(data => {
          this.timeout = setTimeout(() => {
            callback(data)
          }, 500 * Math.random())
        })
      }
    }
  },
  computed: {
    subscribeGroupLabel () {
      if (this.formData.topic.subscribeGroupExist) {
        return '订阅分组【必填】'
      } else {
        return '订阅分组【选填】'
      }
    }
  },
  mounted () {
    this.getApps()
    // this.getTopics();
    this.getSubscribeGroups()
  }
}
</script>

<style scoped>

</style>
