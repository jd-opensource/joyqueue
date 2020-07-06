<template>
  <div>
    <grid-row>
      <grid-col span="3">消息格式: </grid-col>
      <grid-col span="21">
          <d-radio v-for="(supportedMessageType, index) in messageTypes" :key="index" :value="messageType" name="messageTypeRadio" :label="supportedMessageType"  @on-change="$emit('update:messageType', $event)">{{supportedMessageType}}</d-radio>
      </grid-col>
    </grid-row>
    <grid-row  style="padding-top:20px;">
      <d-input type="textarea" rows="5" v-model="messageText"></d-input>
    </grid-row>
  </div>
</template>

<script>
import apiRequest from '../../utils/apiRequest.js'

export default {
  name: 'preview',
  props: {
    messageTypes: Array, // 支持的消息格式
    query: Object, // 消息查询条件
    url: String, // 查询并解析消息的URL
    messageType: String // 消息格式

  },
  data () {
    return {
      messageText: ''
    }
  },
  watch: {
    messageType: function () {
      this.requestPreview()
    }
  },
  mounted () {
    this.requestPreview()
  },
  methods: {
    requestPreview: function () {
      let querydata = Object.assign(this.query, {messageType: this.messageType})
      apiRequest.get(this.url, querydata)
        .then(data => {
          if (data.data) {
            this.messageText = data.data
          }
        })
    }
  }
}
</script>
