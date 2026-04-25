const loginPanel = document.querySelector(".login-panel");
const message = document.querySelector("#login-message");
const googleClientId = loginPanel.dataset.googleClientId;
const appleClientId = loginPanel.dataset.appleClientId;

const setMessage = (text) => {
  message.textContent = text;
};

const login = async (idToken, provider) => {
  setMessage("로그인 중");

  const response = await fetch("/admin/login", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify({ idToken, provider }),
  });

  if (response.status === 204) {
    window.location.assign("/admin");
    return;
  }

  if (response.status === 403) {
    setMessage("어드민 권한이 없습니다");
    return;
  }

  setMessage("로그인에 실패했습니다");
};

window.addEventListener("load", () => {
  if (googleClientId && window.google?.accounts?.id) {
    google.accounts.id.initialize({
      client_id: googleClientId,
      callback: (response) => login(response.credential, "GOOGLE"),
    });
    google.accounts.id.renderButton(document.querySelector("#google-login"), {
      theme: "outline",
      size: "large",
      width: 280,
    });
  }

  const appleButton = document.querySelector("#apple-login");
  if (!appleClientId || !window.AppleID?.auth) {
    appleButton.disabled = true;
    return;
  }

  AppleID.auth.init({
    clientId: appleClientId,
    scope: "name email",
    redirectURI: window.location.origin + "/admin/login",
    usePopup: true,
  });

  appleButton.addEventListener("click", async () => {
    try {
      const response = await AppleID.auth.signIn();
      await login(response.authorization.id_token, "APPLE");
    } catch (error) {
      setMessage("로그인이 취소되었습니다");
    }
  });
});
