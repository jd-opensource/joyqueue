<template>
    <ul :class="cls"><slot></slot></ul>
</template>
<script>
import Config from '../../config'
export default {
  name: `${Config.namePrefix}DropdownMenu`,
  data () {
    return {
      cls: `${Config.clsPrefix}dropdown-menu`
    }
  }
}
</script>
