<template>
  <div
    @click.stop="handleClick"
    @contextmenu="($event) => this.handleContextMenu($event)"
    v-show="node.visible"
    :class="classes"
    role="treeitem"
    tabindex="-1"
    :aria-expanded="expanded"
    :aria-disabled="node.disabled"
    :aria-checked="node.checked"
    :draggable="tree.draggable"
    @dragstart.stop="handleDragStart"
    @dragover.stop="handleDragOver"
    @dragend.stop="handleDragEnd"
    @drop.stop="handleDrop"
    ref="node"
  >
    <div :class="[prefixCls + '__content']"
      :style="{ 'padding-left': (node.level - 1) * tree.indent + 'px' }">
      <Icon
        @click.stop="handleExpandIconClick"
        name="chevron-right"
        :class="expandIconCls"></Icon>
      <Checkbox
        v-if="showCheckbox"
        v-model="node.checked"
        :indeterminate="node.indeterminate"
        :disabled="!!node.disabled"
        @click.native.stop
        @on-change="handleCheckChange"
      >
      </Checkbox>
      <Icon v-if="node.loading" name="loader" :class="[prefixCls + '__loading-icon']"></Icon>
      <Icon v-if="tree.icon" :name="tree.icon" :class="[prefixCls + '__icon']"></Icon>
      <node-content :node="node"></node-content>
    </div>
    <collapse-transition>
      <div
        :class="[prefixCls + '_children']"
        v-if="!renderAfterExpand || childNodeRendered"
        v-show="expanded"
        role="group"
        :aria-expanded="expanded"
      >
        <d-tree-node
          :render-content="renderContent"
          v-for="child in node.childNodes"
          :render-after-expand="renderAfterExpand"
          :key="getNodeKey(child)"
          :node="child"
          @node-expand="handleChildNodeExpand">
        </d-tree-node>
      </div>
    </collapse-transition>
  </div>
</template>

<script type="text/jsx">
import Config from '../../../config'
import CollapseTransition from '../../../transitions/collapse-transition'
import Icon from '../../icon'
import Checkbox from '../../checkbox'
import emitter from '../../../mixins/emitter'
import { getNodeKey } from './model/util'

const prefixCls = `${Config.clsPrefix}tree-node`
const componentName = `${Config.namePrefix}TreeNode`

export default {
  name: componentName,

  componentName: componentName,

  mixins: [emitter],

  props: {
    node: {
      default () {
        return {}
      }
    },
    props: {},
    renderContent: Function,
    renderAfterExpand: {
      type: Boolean,
      default: true
    }
  },

  components: {
    CollapseTransition,
    Icon,
    Checkbox,
    NodeContent: {
      props: {
        node: {
          required: true
        }
      },
      render (h) {
        const parent = this.$parent
        const tree = parent.tree
        const node = this.node
        const { data, store } = node
        return (
          parent.renderContent
            ? parent.renderContent.call(parent._renderProxy, h, { _self: tree.$vnode.context, node, data, store })
            : tree.$scopedSlots.default
              ? tree.$scopedSlots.default({ node, data })
              : <span class={ `${prefixCls}__label` }>{ node.label }</span>
        )
      }
    }
  },

  data () {
    return {
      prefixCls: prefixCls,
      tree: null,
      expanded: false,
      childNodeRendered: false,
      showCheckbox: false,
      oldChecked: null,
      oldIndeterminate: null
    }
  },

  watch: {
    'node.indeterminate' (val) {
      this.handleSelectChange(this.node.checked, val)
    },

    'node.checked' (val) {
      this.handleSelectChange(val, this.node.indeterminate)
    },

    'node.expanded' (val) {
      this.$nextTick(() => { this.expanded = val })
      if (val) {
        this.childNodeRendered = true
      }
    }
  },

  computed: {
    classes () {
      return [
        `${prefixCls}`,
        {
          'is-expanded': this.expanded,
          'is-current': this.tree.store.currentNode === this.node,
          'is-hidden': !this.node.visible,
          'is-focusable': !this.node.disabled,
          'is-checked': !this.node.disabled && this.node.checked
        }
      ]
    },

    hasVisibleChild () {
      let hasVisible = false
      if (this.node.childNodes) {
        const childNode = this.node.childNodes.find(child => {
          return child.visible === true
        })
        if (childNode) {
          hasVisible = true
        }
      }
      return hasVisible
    },

    expandIconCls () {
      return [
        {
          'is-leaf': this.node.isLeaf || !this.hasVisibleChild,
          'expanded': !this.node.isLeaf && this.expanded
        },
        `${prefixCls}__expand-icon`
      ]
    }
  },

  methods: {
    getNodeKey (node) {
      return getNodeKey(this.tree.nodeKey, node.data)
    },

    handleSelectChange (checked, indeterminate) {
      if (this.oldChecked !== checked && this.oldIndeterminate !== indeterminate) {
        this.tree.$emit('check-change', this.node.data, checked, indeterminate)
      }
      this.oldChecked = checked
      this.indeterminate = indeterminate
    },

    handleClick () {
      const store = this.tree.store
      store.setCurrentNode(this.node)
      this.tree.$emit('current-change', store.currentNode ? store.currentNode.data : null, store.currentNode)
      this.tree.currentNode = this
      if (this.tree.expandOnClickNode) {
        this.handleExpandIconClick()
      }
      if (this.tree.checkOnClickNode && !this.node.disabled) {
        this.handleCheckChange(null, {
          target: { checked: !this.node.checked }
        })
      }
      this.tree.$emit('node-click', this.node.data, this.node, this)
    },

    handleContextMenu (event) {
      if (this.tree._events['node-contextmenu'] && this.tree._events['node-contextmenu'].length > 0) {
        event.stopPropagation()
        event.preventDefault()
      }
      this.tree.$emit('node-contextmenu', event, this.node.data, this.node, this)
    },

    handleExpandIconClick () {
      if (this.node.isLeaf) return
      if (this.expanded) {
        this.tree.$emit('node-collapse', this.node.data, this.node, this)
        this.node.collapse()
      } else {
        this.node.expand()
        this.$emit('node-expand', this.node.data, this.node, this)
      }
    },

    handleCheckChange (value, ev) {
      this.node.setChecked(ev.target.checked, !this.tree.checkStrictly)
      this.$nextTick(() => {
        const store = this.tree.store
        this.tree.$emit('check', this.node.data, {
          checkedNodes: store.getCheckedNodes(),
          checkedKeys: store.getCheckedKeys(),
          halfCheckedNodes: store.getHalfCheckedNodes(),
          halfCheckedKeys: store.getHalfCheckedKeys()
        })
      })
    },

    handleChildNodeExpand (nodeData, node, instance) {
      this.broadcast(componentName, 'tree-node-expand', node)
      this.tree.$emit('node-expand', nodeData, node, instance)
    },

    handleDragStart (event) {
      if (!this.tree.draggable) return
      this.tree.$emit('tree-node-drag-start', event, this)
    },

    handleDragOver (event) {
      if (!this.tree.draggable) return
      this.tree.$emit('tree-node-drag-over', event, this)
      event.preventDefault()
    },

    handleDrop (event) {
      event.preventDefault()
    },

    handleDragEnd (event) {
      if (!this.tree.draggable) return
      this.tree.$emit('tree-node-drag-end', event, this)
    }
  },

  created () {
    const parent = this.$parent

    if (parent.isTree) {
      this.tree = parent
    } else {
      this.tree = parent.tree
    }

    const tree = this.tree
    if (!tree) {
      console.warn('Can not find node\'s tree.')
    }

    const props = tree.props || {}
    const childrenKey = props['children'] || 'children'

    this.$watch(`node.data.${childrenKey}`, () => {
      this.node.updateChildren()
    })

    this.showCheckbox = tree.showCheckbox

    if (this.node.expanded) {
      this.expanded = true
      this.childNodeRendered = true
    }

    if (this.tree.accordion) {
      this.$on('tree-node-expand', node => {
        if (this.node !== node) {
          this.node.collapse()
        }
      })
    }
  }
}
</script>
