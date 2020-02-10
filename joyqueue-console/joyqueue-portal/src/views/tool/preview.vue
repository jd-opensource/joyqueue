<template>
  <div>
    <grid-row>
      <grid-col span="2">消息格式: </grid-col>
      <grid-col span="22">
          <d-radio v-for="(supportedMessageType, index) in messageTypes" :key="index" v-model="messageType" name="messageTypeRadio" :label="supportedMessageType"  @on-change="requetPreview">{{supportedMessageType}}</d-radio>
      </grid-col>
    </grid-row>
    <grid-row>
      <d-input type="textarea" rows="5" v-model="messageText" placeholder="请输入"></d-input>
    </grid-row>
  </div>  
</template>

<script>
import apiRequest from '../../utils/apiRequest.js'

export default {
  props: {
    messageTypes: Array, // 支持的消息格式
    query: Object, // 消息查询条件
    url: String // 查询并解析消息的URL
  },
  data () {
    return {
      messageType: undefined,
      messageText: undefined
    }
  },
  mounted () {
    // eslint-disable-next-line no-undef
    if (typeof this.messageTypes !== 'undefined' && this.messageTypes.length > 0) {
      if (typeof this.messageType === 'undefined') {
        this.messageType = this.messageTypes[0]
      }
    } else {
      console.error('Property message-types can not be empty!')
    }
    this.requetPreview()
  },
  methods: {
    requetPreview: () => {
      this.messageText = 'Preview: ' + this.messageType
    }
  }
}
</script>
