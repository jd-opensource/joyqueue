<template>
    <div class="footer">
      <div class="container">
        <div class="footer-contact">
          <dl>
            <dt>
              <img src="http://img11.360buyimg.com/pop/s400x400_jfs/t24844/198/1322547321/2502/8d993e11/5b9102afN2337981f.png">
            </dt>
            <dd>jmq-mid@jd.com</dd>
          </dl>
          <dl>
            <dt>
              <img src="http://img11.360buyimg.com/pop/s400x400_jfs/t26686/116/438072226/2555/e55a242/5b9102b2N7bdb20b6.png">
            </dt>
            <dd>Timeline:jmq-support</dd>
          </dl>
        </div>
        <div class="footer-desc mt10 mb10">
          <div class="footer-copyright">
            {{langConfig.copyright}}
            <a href="//tig.jd.com" target="_blank"> {{langConfig.department}}</a>
          </div>
        </div>
      </div>
    </div>
</template>
<script>
import compoLang from '../../i18n/components.json'
export default {
  data () {
    return {
    }
  },
  computed: {
    curLang () {
      return this.$i18n.locale
    },
    langConfig () {
      return compoLang[this.curLang]['footer']
    }
  }
}
</script>
<style scoped>
  .footer {
    padding: 15px 0;
    color: #6190e8;
    text-align: center;
    background: #2c405a;
    width: 100%;
    box-sizing: border-box;
    margin-top:33px;
    position: absolute;
    bottom:0px;
  }
  .footer .container {
    position: relative;
    width: 90%;
  }
  .footer-contact {
    text-align: center;
    font-size: 20px;
    color: #fff;
  }
  .footer-contact dl, .footer-contact dl dt {
    display: inline-flex;
  }
  .footer-contact dl {
    padding: 0 80px;
  }
  .footer-contact dl dt img {
    width: 50px;
    height: 50px;
  }
  .footer-contact dl dd {
    display: inline-flex;
    line-height: 50px;
    padding: 0 10px;
  }
  .footer-desc {
    text-align: center;
    font-size: 12px;
    color: #6190e8;
  }
  .footer-copyright {
    letter-spacing: 1px;
    font-weight: 300;
  }
</style>
