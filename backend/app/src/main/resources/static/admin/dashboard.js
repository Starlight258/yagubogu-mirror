const toast = document.querySelector("#toast");
const logoutButton = document.querySelector("#logout-button");

const showToast = (message, type = "success") => {
  toast.textContent = message;
  toast.dataset.type = type;
  toast.classList.add("is-visible");
  window.setTimeout(() => toast.classList.remove("is-visible"), 3200);
};

document.querySelectorAll("[data-admin-action]").forEach((button) => {
  button.addEventListener("click", async () => {
    const originalText = button.textContent;
    button.disabled = true;
    button.textContent = "처리 중";

    try {
      const response = await fetch(button.dataset.adminAction, {
        method: "POST",
      });

      if (response.status === 401 || response.status === 403) {
        window.location.assign("/admin/login");
        return;
      }

      if (!response.ok) {
        showToast("실패했습니다", "error");
        return;
      }

      const body = await response.text();
      showToast(body ? `완료: ${body}` : "완료되었습니다");
    } finally {
      button.disabled = false;
      button.textContent = originalText;
    }
  });
});

logoutButton.addEventListener("click", async () => {
  await fetch("/admin/logout", {
    method: "POST",
  });
  window.location.assign("/admin/login");
});
