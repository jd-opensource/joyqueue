<template>
  <transition :name="transitionName" @after-leave="doDestroy">
    <div
      v-show="showPopper"
      :class="wrapClasses"
      :style="{ width: dropdownWidth }"
      role="region">
      <d-scrollbar
        tag="ul"
        :wrap-class="[prefixCls + '__wrap']"
        :view-class="[prefixCls + '__list']">
        <li v-if="!parent.hideLoading && parent.loading"><Icon name="loader"></Icon></li>
        <slot v-else>
        </slot>
      </d-scrollbar>
    </div>
  </transition>
</template>
<script>
import Config from '../../config'
import Icon from '../icon'
import DScrollbar from '../scrollbar'
import Popper from '../../utils/vue-popper'
import Emitter from '../../mixins/emitter'

const prefixCls = `${Config.clsPrefix}autocomplete-suggestion`

export default {
  name: `${Config.namePrefix}AutocompleteSuggestions`,

  mixins: [Popper, Emitter],

  componentName: `${Config.namePrefix}AutocompleteSuggestions`,

  components: { Icon, DScrollbar },

  data () {
    return {
      prefixCls: prefixCls,
      parent: this.$parent,
      dropdownWidth: ''
    }
  },

  props: {
    options: {
      default () {
        return {
          gpuAcceleration: false
        }
      }
    },
    id: String
  },

  computed: {
    transitionName () {
      return `${Config.clsPrefix}-zoom-in-top`
    },
    wrapClasses () {
      return [
        `${prefixCls}`,
        `${Config.clsPrefix}-popper`,
        {
          [`${prefixCls}__loading`]: !this.parent.hideLoading && this.parent.loading
        }
      ]
    }
  },

  methods: {
    select (item) {
      this.dispatch(`${Config.namePrefix}Autocomplete`, 'item-click', item)
    }
  },

  updated () {
    this.$nextTick(_ => {
      this.popperJS && this.updatePopper()
    })
  },

  mounted () {
    this.$parent.popperElm = this.popperElm = this.$el
    this.referenceElm = this.$parent.$refs.input.$refs.input
    this.referenceList = this.$el.querySelector(`.${prefixCls}__list`)
    this.referenceList.setAttribute('role', 'listbox')
    this.referenceList.setAttribute('id', this.id)
  },

  created () {
    this.$on('visible', (val, inputWidth) => {
      this.dropdownWidth = inputWidth + 'px'
      this.showPopper = val
    })
  }
}
</script>
