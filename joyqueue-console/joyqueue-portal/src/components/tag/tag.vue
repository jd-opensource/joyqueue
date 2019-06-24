/*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
<template>
  <transition name="fade">
    <span
      :class="colorClass"
      :style="colorStyle"
    >
      <span :class="textClass"><slot></slot></span>
      <icon name="x" :class="closeClass" v-if="closable" @click="closeAction"></icon>
    </span>
  </transition>
</template>

<script>
import Config from '../../config'
import Icon from '../icon';
import { oneOf, isColor } from '../../utils/assist';

const prefixCls = `${Config.clsPrefix}tag`;
const sizeArr = ['large', 'small', 'default'];
const colorArr = ['default', 'primary', 'success', 'error', 'warning', 'info'];

export default {
  name: `${Config.namePrefix}Tag`,
  components: {Icon},
  props: {
    name: {
      type: [String, Number]
    },
    color: {
      type: String,
      default: 'default'
    },
    closable: {
      type: Boolean,
      default: false
    },
    size: {
      default: 'default',
      validator (value) {
        return oneOf(value, sizeArr);
      }
    }
  },
  computed: {
    textClass () {
      return [
        `${prefixCls}__text`,
        {
          [`${prefixCls}__text--${this.size}`]: !!this.size
        }
      ]
    },
    closeClass () {
      return [
        `${prefixCls}__close`,
        {
          [`${prefixCls}__close--${this.size}`]: !!this.size
        }
      ]
    },
    colorClass () {
      return [
        `${prefixCls}`,
        {
          [`${prefixCls}--${this.color}`]: oneOf(this.color, colorArr),
          [`${prefixCls}--${this.size}`]: !!this.size
        }
      ]
    },
    colorStyle () {
      if (oneOf(this.color, colorArr) || !isColor(this.color)) return '';
      return {
        borderColor: this.color,
        backgroundColor: this.color
      }
    }
  },
  methods: {
    closeAction (evt) {
      if (typeof this.name === 'undefined') {
        this.$emit('on-close', evt)
      } else {
        this.$emit('on-close', evt, this.name)
      }
    }
  }
}
</script>
