<template>
  <d-form :model="formData" :rules="rules" ref="form" enctype="multipart/form-data" label-width="100px" style="height: 350px; overflow-y:auto; width: 100%; padding-right: 20px">
    <d-form-item label="文件:" prop="type" style="width: 60%">
      <d-input style="width: 249px" type="file" v-model="formData.file" ></d-input>
    </d-form-item>
  </d-form>
</template>

<script>
export default {
  name: 'executor-upload-form'
}
</script>

<style scoped>

</style>
