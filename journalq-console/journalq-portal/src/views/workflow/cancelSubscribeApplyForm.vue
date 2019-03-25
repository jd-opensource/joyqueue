<template>
  <div>
    <d-form ref="form" :model="formData" :rules="rules" label-width="110px" style="padding-left: 10px;">
      <d-form-item label="订阅类型：" prop="subscribeType">
        <d-select v-model="formData.subscribeType" style="width: 80%">
          <d-option :value="$store.getters.producerType" >生产者</d-option>
          <d-option :value="$store.getters.consumerType" >消费者</d-option>
        </d-select>
      </d-form-item>
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
            <div class="code" style="text-overflow: ellipsis;overflow: hidden;">{{ item.code }}</div>
            <!--<span class="id" style="font-size: 12px;color: #b4b4b4;display:block">-->
                  <!--{{ 'namespace_id: '+item.namespace.id+', namespace_code: '+item.namespace.code}}-->
            <!--</span>-->
            <!--<span class="id" style="font-size: 12px;color: #b4b4b4;display:block">-->
                  <!--{{ 'name: '+item.name}}-->
            <!--</span>-->
          </template>
        </d-autocomplete>
      </d-form-item>
      <div v-if="formData.subscribeType==$store.getters.consumerType">
        <d-form-item label="订阅分组：" :error="error.subscribeGroup" prop="subscribeGroup">
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
      </div>
      <d-form-item label="审核人ERP：" :error="error.auditorCode" prop="auditorCode">
        <d-input v-model="formData.auditorCode" style="width: 80%" placeholder="仅限一人，不能选择自己"/>
      </d-form-item>
      <d-form-item label="取消订阅原因：" :error="error.reason" prop="reason">
        <d-input type="textarea" rows="2" v-model="formData.reason" style="width: 80%" placeholder="请输入，不超过512字符"/>
      </d-form-item>
      <div class="actions" style="text-align: center">
        <d-button type="primary" @click="confirm">确定</d-button>
      </div>
    </d-form>
  </div>
</template>

<script>
import apply from '../../mixins/apply.js'
import apiRequest from '../../utils/apiRequest.js'

export default {
  name: 'cancel-subscribe-apply-form',
  mixins: [ apply ],
  props: {
    formType: 0, // 0:add or edit form
    data: {
      type: Object,
      default: function () {
        return {
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
            }
          },
          subscribeGroup: '',
          subscribeType: this.$store.getters.producerType,
          auditorCode: '',
          reason: ''
        }
      }
    }
  },
  computed: {
    subscribeType () {
      return this.formData.subscribeType
    }
  },
  data () {
    // var validateSubscribeGroup = (rule, value, callback) => {
    //   if (this.formData.subscribeType === this.$store.getters.consumerType && this.formData.subscribeGroup === '') {
    //     callback(new Error('请输入订阅分组'));
    //   } else {
    //     callback();
    //   }
    // };
    return {
      urls: {
        findApp: `/application/searchSubscribed`,
        findTopic: `/topic/search`,
        findSubscribeGroup: `/consumer/findAllSubscribeGroups`,
        add: '/cancel/subscribe/apply',
        edit: '/cancel/subscribe/update'
      },
      rules: {
        'app.code': [
          {required: true, message: '请选择应用英文名', trigger: 'change'}
        ],
        'topic.code': [
          {required: true, message: '请选择主题英文名', trigger: 'change'}
        ],
        subscribeType: [
          {type: 'number', required: true, message: '请选择订阅类型', trigger: 'change'}
        ],
        auditorCode: [
          {required: true, message: '请输入审核人ERP,不能填写自己的账号，仅限一人', trigger: 'change'},
          {pattern: /^[a-zA-Z]+[a-zA-Z0-9]{1,60}[a-zA-Z0-9]+$/, message: 'ERP格式不匹配', trigger: 'change'}
        ],
        reason: [
          {required: true, message: '请输入原因', trigger: 'change'},
          { max: 512, message: '长度在 512 个字符以内', trigger: 'blur' }
        ]
        // subscribeGroup: [
        //   {validator: validateSubscribeGroup, trigger: 'blur'}
        // ]
      },
      subscribeGroupList: [],
      error: {
        appCode: '',
        topicCode: '',
        auditorCode: '',
        reason: '',
        subscribeGroup: ''
      }

    }
  },
  methods: {
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
    }
  },
  mounted () {
    this.getApps()
    this.getTopics()
    this.getSubscribeGroups()
  }
}
</script>

<style scoped>

</style>
