const toast = document.querySelector("#toast");
const logoutButton = document.querySelector("#logout-button");
const crawlingForm = document.querySelector("#crawling-form");
const startDateInput = document.querySelector("#start-date");
const endDateInput = document.querySelector("#end-date");
const sleepMillisInput = document.querySelector("#sleep-millis");
const reviewDelayInput = document.querySelector("#review-delay");

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
  const startDate = startDateInput.value;
  const endDate = endDateInput.value;

  if (!startDate || !endDate) {
    showToast("날짜 범위를 입력해주세요", "error");
    return;
  }

  if (startDate > endDate) {
    showToast("시작 날짜는 종료 날짜보다 클 수 없습니다", "error");
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
        startDate,
        endDate,
        sleepMillis: Number(sleepMillisInput.value),
        reviewRetryDelayMinutes: Number(reviewDelayInput.value),
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
  } catch (error) {
    showToast("요청 중 오류가 발생했습니다", "error");
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
