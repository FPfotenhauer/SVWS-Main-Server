<template>
  <div class="modal-overlay" v-if="visible || requiresPasswordChange">
    <div class="modal-content">
      <div class="modal-header">
        <h2>{{ requiresPasswordChange ? 'Change Default Password' : 'Change Password' }}</h2>
        <button v-if="!requiresPasswordChange" class="close-btn" @click="closeModal">&times;</button>
        <p class="text-muted">{{ requiresPasswordChange ? 'You are using the default password. Please change it for security reasons.' : 'Enter your current and new password.' }}</p>
      </div>

      <form @submit.prevent="handleChangePassword">
        <div class="form-group">
          <label for="current-password">Current Password:</label>
          <input
            id="current-password"
            v-model="currentPassword"
            type="password"
            placeholder="Enter current password"
            required
            :disabled="isLoading"
          />
        </div>

        <div class="form-group">
          <label for="new-password">New Password:</label>
          <input
            id="new-password"
            v-model="newPassword"
            type="password"
            placeholder="Enter new password"
            required
            :disabled="isLoading"
          />
        </div>

        <div class="form-group">
          <label for="confirm-password">Confirm Password:</label>
          <input
            id="confirm-password"
            v-model="confirmPassword"
            type="password"
            placeholder="Confirm new password"
            required
            :disabled="isLoading"
          />
        </div>

        <div v-if="errorMessage" class="error-message">
          {{ errorMessage }}
        </div>

        <div v-if="successMessage" class="success-message">
          {{ successMessage }}
        </div>

        <div class="button-group">
          <button type="submit" class="btn-primary" :disabled="isLoading || newPassword !== confirmPassword">
            {{ isLoading ? "Changing..." : "Change Password" }}
          </button>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from "vue";
import { useAuthStore } from "../stores/auth";

interface Props {
  visible?: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  visible: false,
});

const emit = defineEmits<{
  close: [];
}>();

const authStore = useAuthStore();
const currentPassword = ref("");
const newPassword = ref("");
const confirmPassword = ref("");
const errorMessage = ref("");
const successMessage = ref("");

const isLoading = computed(() => authStore.isLoading);
const requiresPasswordChange = computed(() => authStore.requiresPasswordChange);

async function handleChangePassword() {
  errorMessage.value = "";
  successMessage.value = "";

  if (newPassword.value !== confirmPassword.value) {
    errorMessage.value = "Passwords do not match";
    return;
  }

  if (newPassword.value.length < 6) {
    errorMessage.value = "Password must be at least 6 characters";
    return;
  }

  try {
    await authStore.changePassword(currentPassword.value, newPassword.value);
    successMessage.value = "Password changed successfully!";
    currentPassword.value = "";
    newPassword.value = "";
    confirmPassword.value = "";
    
    // Close modal after 2 seconds
    setTimeout(() => {
      if (!requiresPasswordChange.value) {
        emit('close');
      }
    }, 2000);
  } catch {
    errorMessage.value = authStore.error || "Failed to change password";
  }
}

function closeModal() {
  emit('close');
}
</script>

<style scoped>
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(15, 23, 42, 0.8);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-content {
  background: #111827;
  border-radius: 16px;
  padding: 30px;
  max-width: 400px;
  width: 90%;
  box-shadow: 0 16px 40px rgba(15, 23, 42, 0.45);
}

.modal-header {
  margin-bottom: 24px;
  position: relative;
}

.close-btn {
  position: absolute;
  top: 0;
  right: 0;
  background: none;
  border: none;
  font-size: 24px;
  cursor: pointer;
  color: #94a3b8;
}

.close-btn:hover {
  color: #f8fafc;
}

.modal-header h2 {
  margin: 0 0 8px 0;
  color: #f8fafc;
  font-size: 20px;
}

.text-muted {
  margin: 8px 0 0 0;
  color: #94a3b8;
  font-size: 14px;
}

.form-group {
  margin-bottom: 16px;
}

.form-group label {
  display: block;
  margin-bottom: 6px;
  font-size: 14px;
  font-weight: 500;
  color: #f8fafc;
}

.form-group input {
  width: 100%;
  padding: 8px 12px;
  border: 1px solid #1f2937;
  border-radius: 4px;
  font-size: 14px;
  background: #1f2937;
  color: #f8fafc;
  box-sizing: border-box;
  transition: border-color 0.2s;
}

.form-group input:focus {
  outline: none;
  border-color: #38bdf8;
  box-shadow: 0 0 0 2px rgba(56, 189, 248, 0.1);
}

.form-group input:disabled {
  background-color: #111827;
  cursor: not-allowed;
}

.error-message {
  padding: 10px;
  margin-bottom: 16px;
  background-color: rgba(239, 68, 68, 0.1);
  color: #ef4444;
  border-radius: 4px;
  font-size: 14px;
}

.success-message {
  padding: 10px;
  margin-bottom: 16px;
  background-color: rgba(34, 197, 94, 0.1);
  color: #22c55e;
  border-radius: 4px;
  font-size: 14px;
}

.button-group {
  display: flex;
  gap: 12px;
  margin-top: 24px;
}

.btn-primary {
  flex: 1;
  padding: 10px 16px;
  background-color: #f97316;
  color: white;
  border: none;
  border-radius: 4px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: background-color 0.2s;
}

.btn-primary:hover:not(:disabled) {
  background-color: #ea580c;
}

.btn-primary:disabled {
  background-color: #1f2937;
  cursor: not-allowed;
}
</style>
