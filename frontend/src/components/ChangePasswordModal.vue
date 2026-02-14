<template>
  <div class="modal-overlay" v-if="visible">
    <div class="modal-content">
      <div class="modal-header">
        <h2>Change Default Password</h2>
        <p class="text-muted">You are using the default password. Please change it for security reasons.</p>
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

const authStore = useAuthStore();
const currentPassword = ref("");
const newPassword = ref("");
const confirmPassword = ref("");
const errorMessage = ref("");
const successMessage = ref("");

const isLoading = computed(() => authStore.isLoading);
const visible = computed(() => authStore.requiresPasswordChange);

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
      errorMessage.value = "";
      successMessage.value = "";
    }, 2000);
  } catch {
    errorMessage.value = authStore.error || "Failed to change password";
  }
}
</script>

<style scoped>
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-content {
  background: white;
  border-radius: 8px;
  padding: 30px;
  max-width: 400px;
  width: 90%;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
}

.modal-header {
  margin-bottom: 24px;
}

.modal-header h2 {
  margin: 0 0 8px 0;
  color: #333;
  font-size: 20px;
}

.text-muted {
  margin: 8px 0 0 0;
  color: #666;
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
  color: #333;
}

.form-group input {
  width: 100%;
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
  box-sizing: border-box;
  transition: border-color 0.2s;
}

.form-group input:focus {
  outline: none;
  border-color: #4CAF50;
  box-shadow: 0 0 0 2px rgba(76, 175, 80, 0.1);
}

.form-group input:disabled {
  background-color: #f5f5f5;
  cursor: not-allowed;
}

.error-message {
  padding: 10px;
  margin-bottom: 16px;
  background-color: #ffebee;
  color: #c62828;
  border-radius: 4px;
  font-size: 14px;
}

.success-message {
  padding: 10px;
  margin-bottom: 16px;
  background-color: #e8f5e9;
  color: #2e7d32;
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
  background-color: #4CAF50;
  color: white;
  border: none;
  border-radius: 4px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: background-color 0.2s;
}

.btn-primary:hover:not(:disabled) {
  background-color: #45a049;
}

.btn-primary:disabled {
  background-color: #cccccc;
  cursor: not-allowed;
}
</style>
