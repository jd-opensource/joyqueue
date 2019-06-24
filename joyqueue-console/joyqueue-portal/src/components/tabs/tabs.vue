<template>
  <div :class="classes">
    <div :class="[prefixCls + '__header']">
      <div :class="[prefixCls + '__extra']" v-if="$slots.extra">
        <slot name="extra"></slot>
      </div>
      <div :class="[prefixCls + '__nav']">
        <!-- S prev btn -->
        <span v-if="scrollable"
              :class="preClasses"
              @click="scrollPrev">
          <Icon name="chevron-left"></Icon>
        </span>
        <!-- E prev btn -->
        <!-- S next btn -->
        <span v-if="scrollable"
              :class="nextClasses"
              @click="scrollNext">
          <Icon name="chevron-right"></Icon>
        </span>
        <!-- E next btn -->
        <!-- S Tab nav -->
        <div :class="[prefixCls + '__nav-wrap']">
          <div :class="[prefixCls + '__nav-scroll']" ref="navScroll">
            <div :class="[prefixCls + '-nav']" ref="nav" :style="navStyle">
              <div :class="[
                      `${navItemCls}`,
                      {
                        [`${navItemCls}--active`]: index === activeIndex,
                        [`${navItemCls}--disabled`]: item.disabled,
                        [`${navItemCls}--closable`]: item.closable
                      }
                    ]"
                   v-for="(item, index) in navList" :key="index"
                   v-show="item.visible"
                   @click="setNavByIndex(index)">
                <!-- S icon -->
                <Icon v-if="item.icon"
                   :class="[prefixCls + '-nav__icon']"
                   :name="item.icon">
                </Icon>{{ item.label }}
                <!-- E icon -->
                <!-- S close btn -->
                <span :class="[prefixCls + '-nav__close']"
                      v-if="item.closable"
                      @click.stop="removeHandle(index)">
                  <Icon name="x"></Icon>
                </span>
                <!-- E close btn -->
              </div>
            </div>
          </div>
        </div>
        <!-- E Tab nav -->
      </div>
    </div>
    <div :class="[prefixCls + '__body']" :style="tabsBodyTranslateStyle">
      <slot></slot>
    </div>
  </div>
</template>

<script>
import Config from '../../config'
import { oneOf } from '../../utils/assist'
import Icon from '../icon/icon.vue'

const prefixCls = `${Config.clsPrefix}tabs`
const navItemCls = `${prefixCls}-nav__item`

export default {
  name: `${Config.namePrefix}Tabs`,
  components: {
    Icon
  },
  props: {
    value: {
      type: String
    },
    type: {
      type: String,
      default: 'line',
      validator: val => oneOf(val, ['line', 'card'])
    },
    size: {
      type: String,
      default: 'default',
      validator: val => oneOf(val, ['default', 'small'])
    },
    closable: {
      type: Boolean,
      default: false
    },
    animated: {
      type: Boolean,
      default: true
    }
  },
  data () {
    return {
      prefixCls: prefixCls,
      navItemCls: navItemCls,
      navList: [],
      activeKey: this.value,
      navOffset: 0,
      navStyle: {
        transform: ''
      },
      nextable: false,
      prevable: false
    }
  },
  watch: {
    value (val) {
      this.activeKey = val
    },
    activeKey (val) {
      this.$emit('input', val)
      this.$emit('on-change', {
        index: this.activeIndex,
        name: this.activeKey
      })
      this.$nextTick(() => {
        this.scrollToActiveTab()
      })
    }
  },
  computed: {
    classes () {
      return [
        `${prefixCls}`,
        {
          [`${prefixCls}--small`]: this.size === 'small',
          [`${prefixCls}--card`]: this.type === 'card',
          [`${prefixCls}--scroll`]: this.scrollable
        }
      ]
    },
    preClasses () {
      const preCls = `${prefixCls}__prev`
      return [
        `${preCls}`,
        {
          [`${preCls}--disabled`]: !this.prevable
        }
      ]
    },
    nextClasses () {
      const nextCls = `${prefixCls}__next`
      return [
        `${nextCls}`,
        {
          [`${nextCls}--disabled`]: !this.nextable
        }
      ]
    },
    scrollable () {
      return this.prevable || this.nextable
    },
    activeIndex () {
      let firstVisibleIndex = -1
      const navList = this.navList
      for (let i = 0, len = navList.length; i < len; i++) {
        const item = navList[i]
        if (item.visible) {
          if (firstVisibleIndex < 0) {
            firstVisibleIndex = i
          }
          if (item.name === this.activeKey) {
            return i
          }
        }
      }
      return firstVisibleIndex
    },
    tabsBodyTranslateStyle () {
      const activeIndex = this.activeIndex
      const translateValue = this.animated ? `${-activeIndex * 100}%` : 0

      return {
        transform: `translate3d(${translateValue}, 0, 0)`
      }
    }
  },
  methods: {
    scrollPrev () {
      if (!this.prevable || !this.$refs.navScroll) return

      const containerWidth = this.$refs.navScroll.offsetWidth
      const currentOffset = this.getCurrentScrollOffset()

      if (currentOffset === 0) return

      const newOffset = currentOffset > containerWidth ? currentOffset - containerWidth : 0

      this.setOffset(newOffset)
    },
    scrollNext () {
      if (!this.nextable || !this.$refs.navScroll || !this.$refs.nav) return

      const containerWidth = this.$refs.navScroll.offsetWidth
      const currentOffset = this.getCurrentScrollOffset()
      const navWidth = this.$refs.nav.offsetWidth

      if (navWidth - currentOffset <= containerWidth) return

      const newOffset = (navWidth - currentOffset > containerWidth * 2) ? currentOffset + containerWidth : navWidth - containerWidth

      this.setOffset(newOffset)
    },
    scrollToActiveTab () {
      if (!this.scrollable || !this.$refs.navScroll) return

      const activeTab = this.$el.querySelector(`.${Config.clsPrefix}tabs-nav__item--active`)
      const navScroll = this.$refs.navScroll

      const activeTabBounds = activeTab.getBoundingClientRect()
      const navScrollBounds = navScroll.getBoundingClientRect()
      const currentOffset = this.getCurrentScrollOffset()
      let newOffset = currentOffset

      if (activeTabBounds.left < navScrollBounds.left) {
        newOffset = currentOffset - (navScrollBounds.left - activeTabBounds.left)
      }

      if (activeTabBounds.right > navScrollBounds.right) {
        newOffset = currentOffset + (activeTabBounds.right - navScrollBounds.right)
      }

      this.setOffset(newOffset)
    },
    getCurrentScrollOffset () {
      return this.navOffset
    },
    setOffset (value) {
      this.navOffset = Math.abs(value)
      this.navStyle.transform = `translate3d(-${this.navOffset}px, 0, 0)`
    },
    getTabs () {
      return this.$children.filter(item =>
        item.$options.name === `${Config.namePrefix}TabPane`
      )
    },
    removeHandle (index) {
      const tabs = this.getTabs()
      const tab = tabs[index]
      let activeKey = ''

      if (tab.disabled) return

      this.navList.splice(index, 1)

      this.$emit('on-tab-remove', {
        index,
        name: tab.currentName
      })

      this.$nextTick(() => {
        this.updateNav()
      })

      if (tab.currentName === this.activeKey) {
        const newTabs = this.getTabs()

        if (newTabs.length) {
          const nextAbleTabs = tabs.filter((item, itemIndex) =>
            !item.disabled && itemIndex > index
          )

          const prevAbleTabs = tabs.filter((item, itemIndex) =>
            !item.disabled && itemIndex < index
          )

          if (nextAbleTabs.length) {
            activeKey = nextAbleTabs[0].currentName
          } else if (prevAbleTabs.length) {
            activeKey = prevAbleTabs[prevAbleTabs.length - 1].currentName
          } else {
            activeKey = newTabs[0].currentName
          }
        }

        this.activeKey = activeKey
      }
    },
    updateNav () {
      this.navList = []

      this.getTabs().forEach((item, index) => {
        this.navList.push({
          label: item.label,
          name: item.currentName || index,
          disabled: item.disabled,
          icon: item.icon,
          closable: item.isClosable,
          visible: item.visible
        })

        if (!item.currentName) {
          item.currentName = index
        }

        // 取第一个可见的pane
        if (item.visible && !this.activeKey) {
          this.activeKey = item.currentName || index
        }

        if (!this.animated) {
          this.switchTabsWithNoAnimated()
        }
      })
    },
    setNavByIndex (index) {
      const currentName = this.navList[index]

      if (!currentName.disabled) {
        this.activeKey = currentName.name

        if (!this.animated) {
          this.switchTabsWithNoAnimated()
        }
      }
    },
    switchTabsWithNoAnimated () {
      const tabs = this.getTabs()

      tabs.forEach((item, index) => {
        item.show = (item.currentName === this.activeKey) || (index === this.activeIndex)
      })
    },
    updateHandle () {
      if (!this.$refs.nav || !this.$refs.navScroll) return

      const navWidth = this.$refs.nav.offsetWidth
      const containerWidth = this.$refs.navScroll.offsetWidth
      const currentOffset = this.getCurrentScrollOffset()

      if (containerWidth < navWidth) {
        this.prevable = currentOffset !== 0
        this.nextable = currentOffset + containerWidth < navWidth
        if (navWidth - currentOffset < containerWidth) {
          this.setOffset(navWidth - containerWidth)
        }
      } else {
        this.nextable = false
        this.prevable = false
        if (currentOffset > 0) {
          this.setOffset(0)
        }
      }
    }
  },
  mounted () {
    window.addEventListener('resize', this.updateHandle, false)

    this.updateNav()
    setTimeout(() => {
      this.scrollToActiveTab()
    }, 0)
  },
  updated () {
    this.updateHandle()
  }
}
</script>
