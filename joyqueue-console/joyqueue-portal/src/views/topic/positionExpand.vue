<template>
  <div>
    <d-table :columns="tableData.colData" :data="tableData.rowData"></d-table>
    <!--{{tableData.rowData}}-->
  </div>

</template>

<script>
import MyTable from '../../components/common/myTable'
import crud from '../../mixins/crud.js'
export default {
  name: 'position-expand',
  components: {MyTable},
  mixins: [crud],
  props: {
    rowData:{
      type: Array
    }
  },
  data () {
    return {
      tableData: {
        rowData: this.rowData,
        colData: [
          {
            title: '分片',
            key: 'partition'
          },
          {
            title: '位置',
            key: 'rightPosition'
          },
          {
            title: '位移差距',
            key: 'rightPositionInterval'
          }
        ],
      }

    }
  },
  methods: {
  },
  mounted () {
  }
}
</script>

<style scoped>
  .expand-row{
    margin-bottom: 16px;
  }
</style>
