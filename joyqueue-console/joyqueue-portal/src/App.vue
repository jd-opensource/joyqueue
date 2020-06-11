<template>
  <div class="wrapper">
    <main-header @log-out="logout" @log-in="login"></main-header>
    <div class="transition-main main-content">
      <router-view></router-view>
    </div>
  </div>
</template>

<script>
import { mapGetters } from 'vuex'
import mainHeader from './components/common/header.vue'
import cookie from './utils/cookie.js'
import apiRequest from './utils/apiRequest.js'
import apiUrl from './utils/apiUrl.js'

export default {
  components: {
    mainHeader
  },
  computed: {
    ...mapGetters([
      'loginUsername'
    ])
  },
  methods: {
    logout () {
      cookie.del('sso.jd.com', '/', '.jd.com')
      apiRequest.get(apiUrl.logout).then(data => {
        this.$store.dispatch('clearUserInfo').then(data => {
          this.login()
          sessionStorage.removeItem('username')
          sessionStorage.removeItem('role')
        })
      })
    },
    login () {
      this.$router.push({
        name: `/${this.$i18n.locale}/login`,
        query: {
        }
      })
    }
  },
  mounted () {
  }
}
</script>

<style>
  @import "assets/css/reset.css";
  @import "assets/css/common.css";
  @import "assets/css/main.css";
  .wrapper{
    min-height: 100vh;
    display: flex;
    flex-direction: column;
  }
</style>
