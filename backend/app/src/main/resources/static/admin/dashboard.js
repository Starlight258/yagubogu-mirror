const toast = document.querySelector("#toast");
const logoutButton = document.querySelector("#logout-button");
const crawlingForm = document.querySelector("#crawling-form");

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

crawlingForm.addEventListener("submit", async (event) => {
  event.preventDefault();

  const submitButton = crawlingForm.querySelector("button[type='submit']");
  const originalText = submitButton.textContent;
  const gameCodes = crawlingForm.gameCodes.value
    .split(/[\s,]+/)
    .map((gameCode) => gameCode.trim())
    .filter(Boolean);

  if (gameCodes.length === 0) {
    showToast("게임 코드를 입력해주세요", "error");
    return;
  }

  submitButton.disabled = true;
  submitButton.textContent = "처리 중";

  try {
    const response = await fetch("/admin/crawling/games", {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify({
        gameCodes,
        sleepMillis: Number(crawlingForm.sleepMillis.value),
        reviewRetryDelayMinutes: Number(crawlingForm.reviewRetryDelayMinutes.value),
      }),
    });

    if (response.status === 401 || response.status === 403) {
      window.location.assign("/admin/login");
      return;
    }

    if (!response.ok) {
      showToast("실패했습니다", "error");
      return;
    }

    const result = await response.json();
    showToast(
      `저장 ${result.saved}, 스킵 ${result.skipped}, 리뷰 ${result.reviewSaved}, 큐 ${result.reviewQueued}, 실패 ${result.failed}`
    );
  } finally {
    submitButton.disabled = false;
    submitButton.textContent = originalText;
  }
});

logoutButton.addEventListener("click", async () => {
  await fetch("/admin/logout", {
    method: "POST",
  });
  window.location.assign("/admin/login");
});
