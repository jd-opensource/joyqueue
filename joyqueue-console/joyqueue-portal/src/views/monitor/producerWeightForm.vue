<template>
  <d-form ref="form"  label-width="100px" style="height: 250px; overflow-y:auto; width: 80%; padding-right: 20px; padding-left:20px">
    <grid-row :gutter=8  v-for="(item, index) in formData" :key="index">
      <grid-col span="6" offset="2">
        <d-form-item label="分区组:">
          {{item.groupNo}}
        </d-form-item>
      </grid-col>
      <grid-col span="8" >
        <d-form-item label="权重:">
          <d-input v-model.number="item.weight" oninput="value = value.trim()" placeholder="请输入"></d-input>
        </d-form-item>
      </grid-col>
    </grid-row>
  </d-form>
</template>

<script>
import crud from '../../mixins/crud.js'
import apiRequest from '../../utils/apiRequest.js'
export default {
  name: 'producer-weight-form',
  components: {},
  mixins: [crud],
  props: {
    urls: {
      type: Object,
      default: function () {
        return {
          search: '/producer/weight',
          update: '/'
        }
      }
    },
    data: {
      type: Array,
      default: function () {
        return [{
          groupNo: '',
          weight: ''
        }
        ]
      }
    },
    producerId: {
      type: String
    }
  },
  data () {
    return {
      searchData: {
        topic: this.topic,
        namespace: this.namespace,
        keyword: ''
      },
      rules: {
      },
      formData: this.data
    }
  },
  methods: {
    getList () {
      apiRequest.get(this.urls.search + '/' + this.producerId, {}).then(data => {
        this.formData = data.data
        for (let i in this.formData) {
          if (this.formData.hasOwnProperty(i)) {
            if (this.formData[i].weight <= 0) {
              this.formData[i].weight = undefined
            }
          }
        }
      })
    },
    getWeights () {
      let weight = ''
      for (let i = 0; i < this.formData.length; i++) {
        let item = this.formData[i]
        if (item.weight && item.weight > 0) {
          weight += item.groupNo + ':' + item.weight + ','
        }
      }
      return weight
    }
  },
  mounted () {
    this.getList()
  }
}
</script>

<style scoped>

</style>
