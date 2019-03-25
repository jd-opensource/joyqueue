<template>
  <form :class="wrapClasses">
    <slot></slot>
  </form>
</template>
<script>
import Config from '../../config'
import { merge } from '../../utils/assist';

const prefixCls = `${Config.clsPrefix}form`;
const localePrefix = `${Config.localePrefix}.form.`;

export default {
  name: `${Config.namePrefix}Form`,

  componentName: `${Config.namePrefix}Form`,

  provide () {
    return {
      duiForm: this
    };
  },

  props: {
    model: Object,
    rules: Object,
    labelPosition: String,
    labelWidth: String,
    labelSuffix: {
      type: String,
      default: ''
    },
    inline: Boolean,
    inlineMessage: Boolean,
    statusIcon: Boolean,
    showMessage: {
      type: Boolean,
      default: true
    },
    size: String,
    disabled: Boolean,
    validateOnRuleChange: {
      type: Boolean,
      default: true
    },
    hideRequiredAsterisk: {
      type: Boolean,
      default: false
    }
  },
  watch: {
    rules () {
      if (this.validateOnRuleChange) {
        this.validate(() => {});
      }
    }
  },
  data () {
    return {
      prefixCls: prefixCls,
      fields: []
    };
  },
  computed: {
    wrapClasses () {
      return [
        this.prefixCls,
        {
          [`${prefixCls}--label-${this.labelPosition}`]: !!this.labelPosition,
          [`${prefixCls}--inline`]: !!this.inline
        }
      ];
    }
  },
  created () {
    this.$on(localePrefix + 'addField', (field) => {
      if (field) {
        this.fields.push(field);
      }
    });
    /* istanbul ignore next */
    this.$on(localePrefix + 'removeField', (field) => {
      if (field.prop) {
        this.fields.splice(this.fields.indexOf(field), 1);
      }
    });
  },
  methods: {
    resetFields () {
      if (!this.model) {
        process.env.NODE_ENV !== 'production' &&
        console.warn('[DUI Warn][Form]model is required for resetFields to work.');
        return;
      }
      this.fields.forEach(field => {
        field.resetField();
      });
    },
    clearValidate (props = []) {
      const fields = props.length
        ? this.fields.filter(field => props.indexOf(field.prop) > -1)
        : this.fields;
      fields.forEach(field => {
        field.clearValidate();
      });
    },
    validate (callback) {
      if (!this.model) {
        console.warn('[DUI Warn][Form]model is required for validate to work!');
        return;
      }

      let promise;
      // if no callback, return promise
      if (typeof callback !== 'function' && window.Promise) {
        promise = new window.Promise((resolve, reject) => {
          callback = function (valid) {
            valid ? resolve(valid) : reject(valid);
          };
        });
      }

      let valid = true;
      let count = 0;
      // 如果需要验证的fields为空，调用验证时立刻返回callback
      if (this.fields.length === 0 && callback) {
        /* eslint-disable */
        callback(true);
      }
      let invalidFields = {};
      this.fields.forEach(field => {
        field.validate('', (message, field) => {
          if (message) {
            valid = false;
          }
          invalidFields = merge({}, invalidFields, field);
          if (typeof callback === 'function' && ++count === this.fields.length) {
            callback(valid, invalidFields);
          }
        });
      });

      if (promise) {
        return promise;
      }
    },
    validateField (prop, cb) {
      let field = this.fields.filter(field => field.prop === prop)[0];
      if (!field) { throw new Error('must call validateField with valid prop string!'); }

      field.validate('', cb);
    }
  }
};
</script>
