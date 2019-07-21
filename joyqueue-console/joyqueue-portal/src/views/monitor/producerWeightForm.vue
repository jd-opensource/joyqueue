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
          <d-input v-model.number="item.weight"  placeholder="请输入"></d-input>
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
    producerId: {
      type: String,
      default: ''
    },
    weights: {
      type: Array
    },
    search: {
      type: String,
      default: ''
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
    }
  },
  data () {
    return {
      searchData: {
        topic: this.topic,
        namespace: this.namespace,
        keyword: ''
      },
      urls: {
        search: this.search
      },
      rules: {
      },
      formData: this.data

    }
  },
  methods: {
    getList () {
      apiRequest.get(this.urlOrigin.search + '/' + this.producerId, {}).then(data => {
        this.formData = data.data
      })
    },
    getWeights () {
      let weight = ''
      for (let i = 0; i < this.formData.length; i++) {
        let item = this.formData[i]
        weight += item.groupNo + ':' + item.weight + ','
      }
      return weight
    }
  },
  mounted () {
    // this.getList()
  }
}
</script>

<style scoped>

</style>
