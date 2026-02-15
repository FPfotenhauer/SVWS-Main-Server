const encoder = new TextEncoder();

const base64Url = (input: ArrayBuffer) => {
  const bytes = new Uint8Array(input);
  let binary = "";
  bytes.forEach((b) => (binary += String.fromCharCode(b)));
  return btoa(binary).replace(/\+/g, "-").replace(/\//g, "_").replace(/=+$/, "");
};

const sha256 = async (input: string) => {
  const data = encoder.encode(input);
  return crypto.subtle.digest("SHA-256", data);
};

const randomString = (length: number) => {
  const charset = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
  let result = "";
  const values = new Uint32Array(length);
  crypto.getRandomValues(values);
  values.forEach((value) => (result += charset[value % charset.length]));
  return result;
};

const getConfig = () => {
  return {
    authUrl: import.meta.env.VITE_OIDC_AUTH_URL as string,
    tokenUrl: import.meta.env.VITE_OIDC_TOKEN_URL as string,
    clientId: import.meta.env.VITE_OIDC_CLIENT_ID as string,
    redirectUri: import.meta.env.VITE_OIDC_REDIRECT_URI as string,
    scope: (import.meta.env.VITE_OIDC_SCOPE as string) || "openid profile"
  };
};

export const startLogin = async () => {
  const { authUrl, clientId, redirectUri, scope } = getConfig();
  if (!authUrl || !clientId || !redirectUri) {
    throw new Error("OIDC config missing");
  }

  const verifier = randomString(64);
  const challenge = base64Url(await sha256(verifier));
  sessionStorage.setItem("pkce_verifier", verifier);

  const params = new URLSearchParams({
    response_type: "code",
    client_id: clientId,
    redirect_uri: redirectUri,
    scope,
    code_challenge: challenge,
    code_challenge_method: "S256"
  });

  window.location.assign(`${authUrl}?${params.toString()}`);
};

export const handleRedirect = async (): Promise<string | null> => {
  const url = new URL(window.location.href);
  const code = url.searchParams.get("code");
  const error = url.searchParams.get("error");

  if (error) {
    throw new Error(error);
  }

  if (!code) {
    return null;
  }

  const { tokenUrl, clientId, redirectUri } = getConfig();
  if (!tokenUrl) {
    throw new Error("OIDC token URL missing");
  }

  const verifier = sessionStorage.getItem("pkce_verifier");
  if (!verifier) {
    throw new Error("PKCE verifier missing");
  }

  const body = new URLSearchParams({
    grant_type: "authorization_code",
    client_id: clientId,
    code,
    redirect_uri: redirectUri,
    code_verifier: verifier
  });

  const response = await fetch(tokenUrl, {
    method: "POST",
    headers: {
      "Content-Type": "application/x-www-form-urlencoded"
    },
    body
  });

  if (!response.ok) {
    throw new Error("Failed to exchange token");
  }

  const payload = (await response.json()) as { access_token?: string };
  if (!payload.access_token) {
    throw new Error("No access token in response");
  }

  url.searchParams.delete("code");
  window.history.replaceState({}, document.title, url.toString());
  return payload.access_token;
};
