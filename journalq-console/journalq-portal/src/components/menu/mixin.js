import Config from '../../config'
import { findComponentUpward, findComponentsUpward } from '../../utils/assist'
export default {
  data () {
    return {
      menu: findComponentUpward(this, `${Config.namePrefix}Menu`)
    }
  },
  computed: {
    hasParentSubmenu () {
      return !!findComponentUpward(this, `${Config.namePrefix}Submenu`)
    },
    parentSubmenuNum () {
      return findComponentsUpward(this, `${Config.namePrefix}Submenu`).length
    },
    mode () {
      return this.menu.mode
    }
  }
}
