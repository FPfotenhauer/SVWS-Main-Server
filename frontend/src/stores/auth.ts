import { defineStore } from "pinia";
import api, { setAccessToken } from "../services/api";
import { handleRedirect, startLogin } from "../services/oidc";

const isOidcEnabled = () => {
  return !!(
    import.meta.env.VITE_OIDC_AUTH_URL &&
    import.meta.env.VITE_OIDC_TOKEN_URL &&
    import.meta.env.VITE_OIDC_CLIENT_ID
  );
};

export const useAuthStore = defineStore("auth", {
  state: () => ({
    accessToken: "",
    error: "",
    isLoading: false,
    requiresPasswordChange: false
  }),
  getters: {
    isAuthenticated: (state) => Boolean(state.accessToken),
    authMethod: () => (isOidcEnabled() ? "oidc" : "password")
  },
  actions: {
    async login() {
      this.error = "";
      if (isOidcEnabled()) {
        await startLogin();
      }
    },
    async passwordLogin(username: string, password: string) {
      this.error = "";
      this.isLoading = true;
      try {
        const response = await api.post("/api/auth/login", {
          username,
          password
        });
        const { accessToken } = response.data;
        this.accessToken = accessToken;
        setAccessToken(accessToken);
      } catch (err) {
        this.error = err instanceof Error ? err.message : "Login failed";
        throw err;
      } finally {
        this.isLoading = false;
      }
    },
    async changePassword(currentPassword: string, newPassword: string) {
      this.error = "";
      this.isLoading = true;
      try {
        await api.post("/api/auth/change-password", {
          currentPassword,
          newPassword
        });
        this.requiresPasswordChange = false;
        return true;
      } catch (err) {
        this.error = err instanceof Error ? err.message : "Password change failed";
        throw err;
      } finally {
        this.isLoading = false;
      }
    },
    logout() {
      this.accessToken = "";
      this.requiresPasswordChange = false;
      setAccessToken("");
    },
    async handleRedirect() {
      try {
        const token = await handleRedirect();
        if (token) {
          this.accessToken = token;
          setAccessToken(token);
        }
      } catch (err) {
        this.error = err instanceof Error ? err.message : "Login failed";
      }
    }
  }
});
