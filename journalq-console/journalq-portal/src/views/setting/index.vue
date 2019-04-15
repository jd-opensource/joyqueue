<template>
  <layout-container>
    <layout-sider class="layout-sider" width="180px">
      <d-menu :active-name="activeName" :theme="theme" width="auto">
        <div class="layout-logo-left"></div>
        <d-menu-item name="user" icon="users" :size="iconSize" :to="`/${ curLang }/setting/user`"><!--color="#98D358"-->
          <span class="layout-text">{{ langConfig.user }}</span>
        </d-menu-item>
        <d-menu-item name="dataCenter" icon="radio" :size="iconSize" :to="`/${ curLang }/setting/dataCenter`"><!--color="#FFD306"-->
          <span class="layout-text">{{ langConfig.dataCenter }}</span>
        </d-menu-item>
        <d-menu-item name="config" icon="edit" :size="iconSize" :to="`/${ curLang }/setting/config`"><!--color="#7FB3D5"-->
          <span class="layout-text">{{ langConfig.config }}</span>
        </d-menu-item>
        <d-menu-item name="brokerGroup" icon="layers" :size="iconSize" :to="`/${ curLang }/setting/brokerGroup`"><!--color="#58D68D"-->
          <span class="layout-text">{{ langConfig.brokerGroup }}</span>
        </d-menu-item>
        <d-menu-item name="broker" icon="cpu" :size="iconSize" :to="`/${ curLang }/setting/broker`"><!--color="#F5B7B1"-->
          <span class="layout-text">{{ langConfig.broker }}</span>
        </d-menu-item>
        <d-menu-item name="metric" icon="tag" :size="iconSize" :to="`/${ curLang }/setting/metric`">
          <span class="layout-text">{{ langConfig.metric }}</span>
        </d-menu-item>
        <d-menu-item name="namespace" icon="feather" :size="iconSize" :to="`/${ curLang }/setting/namespace`"><!--color="#73C6B6"-->
          <span class="layout-text">{{ langConfig.namespace }}</span>
        </d-menu-item>
      </d-menu>
    </layout-sider>
    <layout-content>
      <router-view></router-view>
    </layout-content>
  </layout-container>

  <!--<div class="layout" :class="{'layout-hide-text': spanLeft === spanLeftFold}">
    <grid-row>
      <grid-col :span="spanLeft"  :style="style">
        <div v-on:mouseover="expandMenu" v-on:mouseleave="foldMenu">
        </div>
      </grid-col>
      <grid-col :span="spanRight">
        <div class="layout-header">
          <d-button type="borderless" icon="menu" class="toggle-btn" @click="toggleClick"></d-button>
          <div class="layout-breadcrumb">
            <d-breadcrumb>
              <d-breadcrumb-item href="#">{{langHeaderConfig.setting}}</d-breadcrumb-item>
              <d-breadcrumb-item>{{submenu}}</d-breadcrumb-item>
            </d-breadcrumb>
          </div>
        </div>
        <div class="layout-content">
          <router-view></router-view>
        </div>
      </grid-col>
    </grid-row>
  </div>-->
</template>

<script>
// import compoLang from '../../i18n/components.json';
import submenu from '../../mixins/submenu.js'

export default {
  components: {},
  mixins: [ submenu ],
  data () {
    return {
      menu: 'setting'
    }
  },
  mounted () {
    // this.submenu = compoLang[this.curLang][this.menu]['user'];
    // this.activeName = 'user';
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
    height: 100%;
    background: #fff;
    padding-bottom: 999em;
    margin-bottom: -999em;
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
    padding-top: 20px;
    padding-bottom: 15px;
    overflow: hidden;
    /*background: #ffffff;*/
    border-radius: 4px;
    height: calc(100% - 50px);
  }
  .layout-hide-text .layout-text {
    display: none;
  }
  .dui-menu-dark.dui-menu-vertical .dui-menu-item-active:not(.dui-menu-submenu) {
    background: rgb(84, 92, 100);
  }
</style>
