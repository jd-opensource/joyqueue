<template>
  <div
    :class="prefixCls"
    v-click-outside="close"
    aria-haspopup="listbox"
    role="combobox"
    :aria-expanded="suggestionVisible"
    :aria-owns="id"
  >
    <d-input
      ref="input"
      v-bind="[$props, $attrs]"
      @on-change="handleChange"
      @on-focus="handleFocus"
      @on-blur="handleBlur"
      @keydown.up.native.prevent="highlight(highlightedIndex - 1)"
      @keydown.down.native.prevent="highlight(highlightedIndex + 1)"
      @keydown.enter.native="handleKeyEnter"
      @keydown.native.tab="close"
    >
      <template slot="prepend" v-if="$slots.prepend">
        <slot name="prepend"></slot>
      </template>
      <template slot="append" v-if="$slots.append">
        <slot name="append"></slot>
      </template>
      <template slot="prefix" v-if="$slots.prefix">
        <slot name="prefix"></slot>
      </template>
      <template slot="suffix" v-if="$slots.suffix">
        <slot name="suffix"></slot>
      </template>
    </d-input>
    <d-autocomplete-suggestions
      visible-arrow
      :class="[popperClass ? popperClass : '']"
      :popper-options="popperOptions"
      :append-to-body="popperAppendToBody"
      ref="suggestions"
      :placement="placement"
      :id="id">
      <li
        v-for="(item, index) in suggestions"
        :key="index"
        :class="{'highlighted': highlightedIndex === index}"
        @click="select(item)"
        :id="`${id}-item-${index}`"
        role="option"
        :aria-selected="highlightedIndex === index"
      >
        <slot :item="item">
          {{ item[valueKey] }}
        </slot>
      </li>
    </d-autocomplete-suggestions>
  </div>
</template>
<script>
import Config from '../../config'
import debounce from 'throttle-debounce/debounce'
import DInput from '../input/input.vue'
import {directive as ClickOutside} from 'v-click-outside-x'
import DAutocompleteSuggestions from './autocomplete-suggestions.vue'
import Emitter from '../../mixins/emitter'
import Migrating from '../../mixins/migrating'
import { generateId } from '../../utils/assist'
import Focus from '../../mixins/focus'

const componentName = `${Config.namePrefix}Autocomplete`
const prefixCls = `${Config.clsPrefix}autocomplete`

export default {
  name: componentName,

  mixins: [Emitter, Focus('input'), Migrating],

  inheritAttrs: false,

  componentName: componentName,

  components: {
    DInput,
    DAutocompleteSuggestions
  },

  directives: { ClickOutside },

  props: {
    valueKey: {
      type: String,
      default: 'value'
    },
    popperClass: String,
    popperOptions: Object,
    placeholder: String,
    disabled: Boolean,
    name: String,
    size: String,
    value: String,
    maxlength: Number,
    minlength: Number,
    autofocus: Boolean,
    fetchSuggestions: Function,
    triggerOnFocus: {
      type: Boolean,
      default: true
    },
    customItem: String,
    selectWhenUnmatched: {
      type: Boolean,
      default: false
    },
    prefixIcon: String,
    suffixIcon: String,
    label: String,
    debounce: {
      type: Number,
      default: 300
    },
    placement: {
      type: String,
      default: 'bottom-start'
    },
    hideLoading: Boolean,
    popperAppendToBody: {
      type: Boolean,
      default: true
    }
  },
  data () {
    return {
      prefixCls: prefixCls,
      activated: false,
      suggestions: [],
      loading: false,
      highlightedIndex: -1,
      suggestionDisabled: false
    }
  },
  computed: {
    suggestionVisible () {
      const suggestions = this.suggestions
      let isValidData = Array.isArray(suggestions) && suggestions.length > 0
      return (isValidData || this.loading) && this.activated
    },
    id () {
      return `${prefixCls}-${generateId()}`
    }
  },
  watch: {
    suggestionVisible (val) {
      this.broadcast(`${Config.namePrefix}AutocompleteSuggestions`, 'visible', [val, this.$refs.input.$refs.input.offsetWidth])
    }
  },
  methods: {
    getMigratingConfig () {
      return {
        props: {
          'custom-item': 'custom-item is removed, use scoped slot instead.',
          'props': 'props is removed, use value-key instead.'
        }
      }
    },
    getData (queryString) {
      if (this.suggestionDisabled) {
        return
      }
      this.loading = true
      this.fetchSuggestions(queryString, (suggestions) => {
        this.loading = false
        if (this.suggestionDisabled) {
          return
        }
        if (Array.isArray(suggestions)) {
          this.suggestions = suggestions
        } else {
          console.error('[Autocomplete]autocomplete suggestions must be an array')
        }
      })
    },
    handleChange (event) {
      let value = event.target.value
      this.$emit('input', value)
      this.suggestionDisabled = false
      if (!this.triggerOnFocus && !value) {
        this.suggestionDisabled = true
        this.suggestions = []
        return
      }
      this.debouncedGetData(value)
    },
    handleFocus (event) {
      this.activated = true
      this.$emit('focus', event)
      if (this.triggerOnFocus) {
        this.debouncedGetData(this.value)
      }
    },
    handleBlur (event) {
      this.$emit('blur', event)
    },
    close (e) {
      this.activated = false
    },
    handleKeyEnter (e) {
      if (this.suggestionVisible && this.highlightedIndex >= 0 && this.highlightedIndex < this.suggestions.length) {
        e.preventDefault()
        this.select(this.suggestions[this.highlightedIndex])
      } else if (this.selectWhenUnmatched) {
        this.$emit('select', {value: this.value})
        this.$nextTick(_ => {
          this.suggestions = []
          this.highlightedIndex = -1
        })
      }
    },
    select (item) {
      this.$emit('input', item[this.valueKey])
      this.$emit('select', item)
      this.$nextTick(_ => {
        this.suggestions = []
        this.highlightedIndex = -1
      })
    },
    highlight (index) {
      if (!this.suggestionVisible || this.loading) { return }
      if (index < 0) {
        this.highlightedIndex = -1
        return
      }
      if (index >= this.suggestions.length) {
        index = this.suggestions.length - 1
      }
      const suggestion = this.$refs.suggestions.$el.querySelector(`.${prefixCls}-suggestion__wrap`)
      const suggestionList = suggestion.querySelectorAll(`.${prefixCls}-suggestion__list li`)

      let highlightItem = suggestionList[index]
      let scrollTop = suggestion.scrollTop
      let offsetTop = highlightItem.offsetTop

      if (offsetTop + highlightItem.scrollHeight > (scrollTop + suggestion.clientHeight)) {
        suggestion.scrollTop += highlightItem.scrollHeight
      }
      if (offsetTop < scrollTop) {
        suggestion.scrollTop -= highlightItem.scrollHeight
      }
      this.highlightedIndex = index
      this.$el.querySelector(`.${Config.clsPrefix}input__inner`).setAttribute('aria-activedescendant', `${this.id}-item-${this.highlightedIndex}`)
    }
  },
  mounted () {
    this.debouncedGetData = debounce(this.debounce, this.getData)
    this.$on('item-click', item => {
      this.select(item)
    })
    let $input = this.$el.querySelector(`.${Config.clsPrefix}input-base`)
    $input.setAttribute('role', 'textbox')
    $input.setAttribute('aria-autocomplete', 'list')
    $input.setAttribute('aria-controls', 'id')
    $input.setAttribute('aria-activedescendant', `${this.id}-item-${this.highlightedIndex}`)
  },
  beforeDestroy () {
    this.$refs.suggestions.$destroy()
  }
}
</script>
