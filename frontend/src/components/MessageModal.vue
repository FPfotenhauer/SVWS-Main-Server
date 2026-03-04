<template>
  <div class="modal-overlay" v-if="visible" @click.self="close">
    <div class="modal-content message-modal">
      <div class="modal-header">
        <h2>{{ title }}</h2>
        <button class="close-btn" @click="close">&times;</button>
      </div>
      <div class="modal-body">
        <p v-html="message"></p>
      </div>
      <div class="modal-footer">
        <button type="button" @click="close" class="primary">OK</button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
defineProps<{
  visible: boolean;
  title: string;
  message: string;
}>();

const emit = defineEmits<{
  close: [];
}>();

const close = () => {
  emit('close');
};
</script>

<style scoped>
.message-modal {
  max-width: 500px;
  width: 90%;
}

.modal-body {
  padding: 24px;
  max-height: 400px;
  overflow-y: auto;
}

.modal-body p {
  margin: 0;
  line-height: 1.6;
  white-space: pre-line;
  word-break: break-word;
}

.modal-footer {
  padding: 16px 24px;
  border-top: 1px solid rgba(148, 163, 184, 0.2);
  display: flex;
  justify-content: flex-end;
}
</style>
