// 增删改查方法，包括查询的getList, 分页的改变，以及新增，编辑，删除
// 新增编辑都直接使用固定的弹框显示信息对象createDiag，同样的数据绑定对象createData，内容各不相同

import { deepCopy } from '../utils/assist.js'
import apiRequest from '../utils/apiRequest.js'
import apiUrl from '../utils/apiUrl.js'
// import toast from '../utils/toast.js'

export default {
  computed: {
    detailDelUrl () {
      let routeName = this.$route.name
      return apiUrl[routeName].detailDel
    },
    detailUrl () {
      let routeName = this.$route.name
      return apiUrl[routeName].detail
    }
  },
  methods: {
    handleDtlSizeChange (val) {
      this.detailPage.pageSize = val
      this.getDetailList(val)
    },
    handleDtlCurrentChange (val) {
      this.detailPage.currentPage = val
      this.getDetailList(val)
    },
    handleDtlSelectionChange (val) {
      this.multipleDtlSelection = val
    },
    detailDel (index, row) {
      let _this = this
      /* toast.msgConfirm ('确定要删除吗', '提示', 'warning', {
        confirmFunc: () => {
          apiRequest.get(this.detailDelUrl+'?id='+row.id).then((data) => {
            this.getDetailList();
          })
        },
        cancelFunc: () => {}
      }) */
    }
  }
}
