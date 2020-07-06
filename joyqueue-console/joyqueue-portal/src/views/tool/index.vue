<template>
  <layout-container>
    <layout-sider class="layout-sider" width="180px">
      <d-menu :active-name="activeName" :theme="theme" width="auto">
        <d-menu-item name="operateHistory" icon="target" :size="iconSize" :to="`/${ curLang }/tool/operateHistory`">
          <span class="layout-text">{{ langConfig.operateHistory }}</span>
        </d-menu-item>
        <d-menu-item name="msgFilter" icon="filter" :size="iconSize" :to="`/${ curLang }/tool/msgFilter`">
          <span class="layout-text">{{ langConfig.msgFilter }}</span>
        </d-menu-item>
      </d-menu>
    </layout-sider>
    <layout-content>
      <router-view></router-view>
    </layout-content>
  </layout-container>

</template>
<script>
import submenu from '../../mixins/submenu.js'

export default {
  components: {},
  mixins: [ submenu ],
  data () {
    return {
      menu: 'tool'
    }
  },
  computed: {
    curLang () {
      return this.$i18n.locale
    }
  }
}

</script>

<style scoped>
  .layout{
    border: 1px solid #d7dde4;
    background: #f5f7f9;
    position: relative;
  }
  .layout-logo-left{
    width: 90%;
    height: 0px;
    line-height: 20px;
    color: #ffffff;
    background: #ffffff;
    border-radius: 3px;
    /*margin: 15px auto;*/
    text-align: center;
  }
  .toggle-btn{
    margin-left: 15px;
  }
  .layout-sider {
    min-height: calc(100vh - 70px);
    background: #fff;
    /*padding-bottom: 999em;*/
    /*margin-bottom: -999em;*/
  }
  .layout-header {
    padding-top: 10px;
    padding-left: 20px;
    box-sizing: border-box;
    flex-shrink: 0;
    /*background: #ffffff;*/
  }
  .layout-breadcrumb{
    padding: 10px 15px 0;
    display: inline-block;
  }
  .layout-content{
    padding-top: 10px;
    padding-bottom: 15px;
    overflow: hidden;
    /*background: #ffffff;*/
    border-radius: 4px;
    min-height: calc(100vh - 70px);
  }
  .layout-hide-text .layout-text {
    display: none;
  }
  .dui-menu-dark.dui-menu-vertical .dui-menu-item-active:not(.dui-menu-submenu) {
    background: rgb(84, 92, 100);
  }
</style>
