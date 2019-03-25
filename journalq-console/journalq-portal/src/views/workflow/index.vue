<template>
  <layout-container>
    <layout-sider class="layout-sider" width="180px">
      <d-menu :active-name="activeName" :theme="theme" width="auto">
        <div class="layout-logo-left"></div>
        <d-menu-item name="apply" icon="file-plus" :size="iconSize" :to="`/${ curLang }/workflow/apply`">
          <span class="layout-text">{{ langConfig.apply }}</span>
        </d-menu-item>
        <d-menu-item name="audit" icon="check-square" :size="iconSize" :to="`/${ curLang }/workflow/audit`">
          <span class="layout-text">{{ langConfig.audit }}</span>
        </d-menu-item>
        <d-menu-item name="apiToken" icon="box" :size="iconSize" :to="`/${ curLang }/workflow/apiToken`">
          <span class="layout-text">{{ langConfig.apiToken }}</span>
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
          <d-menu :active-name="activeName" :theme="theme" width="auto">
            <div class="layout-logo-left"></div>
            <d-menu-item name="apply" icon="file-plus" color="#98D358" :size="iconSize" :to="`/${ curLang }/workflow/apply`">
              <span class="layout-text">{{ langConfig.apply }}</span>
            </d-menu-item>
            <d-menu-item name="audit" icon="check-square" color="#7FB3D5" :size="iconSize" :to="`/${ curLang }/workflow/audit`">
              <span class="layout-text">{{ langConfig.audit }}</span>
            </d-menu-item>
            <d-menu-item name="apiToken" :to="`/${ curLang }/workflow/apiToken`">
              <icon name="box" color="#7FB3D5" :size="iconSize"/>
              <span class="layout-text">{{ langConfig.apiToken }}</span>
            </d-menu-item>
          </d-menu>
        </div>
      </grid-col>
      <grid-col :span="spanRight">
        <div class="layout-header">
          <d-button type="borderless" icon="menu" class="toggle-btn" @click="toggleClick"></d-button>
          <div class="layout-breadcrumb">
            <d-breadcrumb>
              <d-breadcrumb-item href="#">{{langHeaderConfig["workflow"]}}</d-breadcrumb-item>
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
      menu: 'workflow'
    }
  },
  mounted () {
    // this.submenu = compoLang[this.curLang][this.menu]['apply'];
    // this.activeName = 'apply';
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
