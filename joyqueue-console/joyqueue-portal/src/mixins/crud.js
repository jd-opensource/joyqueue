// 增删改查方法，包括查询的getList, 分页的改变，以及新增，编辑，删除
// 新增编辑都直接使用固定的弹框显示信息对象createDiag，同样的数据绑定对象createData，内容各不相同

import apiRequest from '../utils/apiRequest.js'
import apiUrl from '../utils/apiUrl.js'
import LangConfig from '../i18n/langs.json'
import {deepCopy} from '../utils/assist'

export default {
  computed: {
    routeName () {
      let rName = this.$route.name
      const languages = Object.keys(LangConfig)
      languages.forEach(language => {
        rName = rName.replace(`/${language}/`, '/')
      })
      return rName
    },
    urlOrigin () {
      if (this.urls) {
        return this.urls
      } else {
        return apiUrl[this.routeName]
      }
    }
  },
  data () {
    return {
      page: {
        page: 1,
        size: 10,
        total: 0
      },
      multipleSelection: [],
      showTablePin: false
    }
  },
  methods: {
    handleSizeChange (val) {
      this.page.size = val
      this.getList()
    },
    handleCurrentChange (val) {
      this.page.page = val
      this.getList()
    },
    handleSelectionChange (val) {
      this.multipleSelection = val
    },
    handleSelectionAll (val) {
      this.multipleSelection = val
    },
    openDialog (dialogName) {
      this[dialogName].visible = true
    },
    // 各组件可以公用的方法写这里，注意数据结构， 增删改查列表查详情
    getSearchVal () {
      let obj = {
        pagination: {
          page: this.page.page,
          size: this.page.size
        },
        query: {}
      }
      for (let key in this.searchData) {
        if (this.searchData.hasOwnProperty(key)) {
          obj.query[key] = this.searchData[key]
        }
      }
      if (typeof (this.beforeSearch) === 'function') {
        obj = this.beforeSearch(obj)
      }
      return obj
    },
    // 查询
    getList () {
      this.showTablePin = true
      let data = this.getSearchVal()
      apiRequest.post(this.urlOrigin.search, {}, data).then((data) => {
        if (data === '') {
          return
        }
        data.data = data.data || []
        if (typeof (this.sortData) === 'function') {
          data.data = this.sortData(data.data)
        }
        data.pagination = data.pagination || {
          totalRecord: data.data.length
        }
        this.page.total = data.pagination.totalRecord
        this.page.page = data.pagination.page
        this.page.size = data.pagination.size
        this.tableData.rowData = data.data
        this.showTablePin = false
      })
    },
    beforeAdd () {
      return new Promise((resolve, reject) => {
        if (this.$refs.addForm === undefined) { // no add form
          resolve(this.addData)
        }
        this.$refs.addForm.submitForm().then(data => {
          resolve(data)
        }).catch(error => {
          reject(error)
        })
      })
    },
    // 添加
    addConfirm () {
      this.beforeAdd().then(data => {
        this.add(this.urlOrigin.add, data, '添加成功', '添加失败')
      }).catch(error => {
        console.log(error)
        this.$Message.error(error)
      })
    },
    addCancel (index, row) {
      this.addDialog.visible = false
    },
    // 修改
    edit (item) {
      this.openDialog('editDialog')
      if (typeof (this.beforeEditData) === 'function') {
        this.editData = this.beforeEditData(item)
      } else {
        this.editData = deepCopy(item)
      }
    },
    beforeEdit (item) {
      return new Promise((resolve, reject) => {
        if (this.$refs['editForm'] === undefined) {
          resolve(this.editData)
        } else {
          this.$refs['editForm'].submitForm().then(data => {
            resolve(data)// false or formData
          }).catch(error => {
            reject(error)
          })
        }
      })
    },
    editConfirm () {
      this.beforeEdit().then(data => {
        this.update(this.urlOrigin.edit + '/' + this.editData.id, data, '修改成功', '修改失败')
      }).catch(error => {
        this.$Message.error(error)
      })
    },
    editCancel (index, row) {
      this.editDialog.visible = false
    },
    dialogCancel (dialogName) {
      this[dialogName].visible = false
      this.getList()
    },
    // 删除
    del (item, index) {
      let _this = this
      this.$Dialog.confirm({
        title: '提示',
        content: '确定要删除吗？'
      }).then(() => {
        if (typeof (_this.beforeDel) === 'function') {
          _this.beforeDel(item)
        }
        apiRequest.delete(_this.urlOrigin.del + '/' + encodeURIComponent(item.id)).then((data) => {
          if (data.code !== this.$store.getters.successCode) {
            this.$Dialog.error({
              content: '删除失败'
            })
          } else {
            this.$Message.success('删除成功')
            if (typeof (_this.afterDel) === 'function') {
              _this.afterDel(item)
            }
            _this.getList()
          }
        })
      }).catch(() => {
      })
    },
    afterAddOrUpdate (isAdd, data, successMsg, errorMsg) {
      if (data.code === this.$store.getters.successCode) { // ok
        if (isAdd) {
          this.addDialog.visible = false
        } else if (this.editDialog !== undefined) {
          this.editDialog.visible = false
        }
        this.$Message.success(successMsg)
        this.getList()
      } else if (data.code === this.$store.getters.validationCode) { // invalid inputs
        console.log(1)
        let errors = (data.message || '').split('|')
        if (!errors || errors.length === 0) {
          this.$Message.error(errors || errorMsg)
        } else if (errors.length === 1) {
          this.$Message.error(errors[0] || errorMsg)
        } else if (errors.length === 2) {
          let error = {}
          errors[0].split(',').forEach(field => {
            error[field] = errors[1]
          })
          if (isAdd) {
            this.$refs['addForm'].setError(error)
          } else {
            this.$refs['editForm'].setError(error)
          }
          this.$Message.success('验证不通过: ' + data.message)
        }
      } else {
        this.$Message.success(errorMsg + ': ' + data.message)
      }
    },
    update (url, data, successMsg, errorMsg) {
      apiRequest.put(url, {}, data).then((data) => {
        this.afterAddOrUpdate(false, data, successMsg, errorMsg)
      })
    },
    add (url, data, successMsg, errorMsg) {
      apiRequest.post(url, {}, data).then((data) => {
        this.afterAddOrUpdate(true, data, successMsg, errorMsg)
      })
    }
  }
}
