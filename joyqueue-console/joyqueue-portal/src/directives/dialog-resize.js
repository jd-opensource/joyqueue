import Config from '../config'

const prefixCls = `${Config.clsPrefix}dialog`

export default {
  bind (el, binding, vnode, oldVnode) {
    if (!binding.value.resizable) return

    const minWidth = Number(binding.value.minWidth)
    const minHeight = Number(binding.value.minHeight)
    const dialogHeaderEl = el.querySelector(`.${prefixCls}__header`)
    const dialogFooterEl = el.querySelector(`.${prefixCls}__footer`)
    const dialogResizeEl = el.querySelector(`.${prefixCls}__resize`)
    const dragDom = el.querySelector(`.${prefixCls}`)

    dialogResizeEl.onmousedown = (e) => {
      e.preventDefault() // 移动时禁用默认事件

      // 鼠标按下，计算当前元素距离可视区的距离
      const disX = e.clientX - dialogResizeEl.offsetLeft + dragDom.offsetWidth / 2
      const disY = e.clientY - dialogResizeEl.offsetTop

      document.onmousemove = function (e) {
        e.preventDefault() // 移动时禁用默认事件

        // 通过事件委托，计算移动的距离
        let width = 2 * (e.clientX - disX + dialogResizeEl.offsetWidth)
        if (width < minWidth) {
          width = minWidth
        }
        let height = e.clientY - disY + dialogResizeEl.offsetHeight
        if (height < minHeight) {
          height = minHeight
        }
        dragDom.style.width = `${width}px`
        dragDom.style.height = `${height}px`
        const headerHeight = dialogHeaderEl ? dialogHeaderEl.offsetHeight : 0
        const footerHeight = dialogFooterEl ? dialogFooterEl.offsetHeight : 0
        const dialogBodyEl = el.querySelector(`.${prefixCls}__body`)
        dialogBodyEl.style.height = `${height - headerHeight - footerHeight}px`
        dialogBodyEl.style.paddingBottom = '16px'
      }

      document.onmouseup = function (e) {
        document.onmousemove = null
        document.onmouseup = null
      }
    }
  }
}
