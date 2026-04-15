package yagubogu.crawling.game.service.crawler.page;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.WaitForSelectorState;
import com.microsoft.playwright.options.WaitUntilState;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import yagubogu.crawling.game.config.KboCrawlerProperties;
import yagubogu.crawling.game.dto.HitterRecordDto;
import yagubogu.crawling.game.dto.PitcherRecordDto;

@Slf4j
public class KboReviewPage extends BaseKboPage {

    private final String gameCode;

    public KboReviewPage(final Page page, final KboCrawlerProperties properties, final String gameCode) {
        super(page, properties);
        this.gameCode = gameCode;
    }

    /**
     * Main.aspx에 gameDate/gameId/section 파라미터를 넘겨 직접 이동.
     * JS가 게임 선택과 리뷰 탭 로드를 자동 처리한다.
     */
    @Override
    public void navigateTo() {
        String url = buildReviewUrl();
        log.info("[REVIEW] 직접 이동: {}", url);

        long navTimeout = properties.getCrawler().getNavigationTimeout().toMillis();
        long waitTimeout = properties.getCrawler().getWaitTimeout().toMillis();

        page.navigate(url, new Page.NavigateOptions()
                .setTimeout(navTimeout)
                .setWaitUntil(WaitUntilState.NETWORKIDLE));

        page.waitForSelector("#tblAwayHitter1 tbody tr",
                new Page.WaitForSelectorOptions()
                        .setTimeout(waitTimeout)
                        .setState(WaitForSelectorState.VISIBLE));

        log.info("[REVIEW] 페이지 로딩 완료");
    }

    /**
     * gameCode(예: 20260412SKLG0)에서 gameDate(앞 8자리)를 파생하여 URL 구성.
     * Main.aspx의 JS가 gameId/section 파라미터를 읽어 리뷰 탭을 자동 로드한다.
     */
    private String buildReviewUrl() {
        String gameDate = gameCode.substring(0, 8);
        return properties.getCrawler().getGameCenterUrl()
                + "?gameDate=" + gameDate
                + "&gameId=" + gameCode
                + "&section=REVIEW";
    }

    @Override
    protected String getBaseUrl() {
        return properties.getCrawler().getGameCenterUrl();
    }

    @Override
    protected void waitForContentUpdate(final long timeout) {
        // navigateTo()에서 테이블 대기 처리
    }

    /**
     * 타자 기록 추출
     *
     * 중복 타순 처리: 같은 타순 번호가 여러 행이면 첫 번째 행만 선택
     * table1: 타순, 포지션, 선수명
     * table3: 타수, 안타, 타점, 득점 (타율 제외)
     *
     * @param table1Id tblAwayHitter1 or tblHomeHitter1
     * @param table3Id tblAwayHitter3 or tblHomeHitter3
     */
    public List<HitterRecordDto> extractHitterRecords(final String table1Id, final String table3Id) {
        Locator table1Rows = page.locator("#" + table1Id + " tbody tr");
        Locator table3Rows = page.locator("#" + table3Id + " tbody tr");

        int rowCount = table1Rows.count();
        int table3RowCount = table3Rows.count();
        List<HitterRecordDto> records = new ArrayList<>();
        Set<Integer> seenOrders = new HashSet<>();

        for (int i = 0; i < rowCount; i++) {
            Locator row1 = table1Rows.nth(i);
            Locator thElements = row1.locator("th");

            if (thElements.count() < 2) {
                log.debug("table1 행 구조 불일치, 건너뜀: table={}, i={}", table1Id, i);
                continue;
            }

            int battingOrder = parseIntSafe(thElements.nth(0).textContent().trim());

            if (seenOrders.contains(battingOrder)) {
                continue;
            }
            seenOrders.add(battingOrder);

            String position = thElements.nth(1).textContent().trim();
            String playerName = row1.locator("td").textContent().trim();

            if (i >= table3RowCount) {
                log.warn("table3 행 부족으로 건너뜀: table={}, i={}, table3RowCount={}", table3Id, i, table3RowCount);
                continue;
            }

            Locator row3 = table3Rows.nth(i);
            Locator tdElements = row3.locator("td");
            int tdCount = tdElements.count();

            if (tdCount < 4) {
                log.warn("table3 td 부족으로 건너뜀: table={}, i={}, tdCount={}", table3Id, i, tdCount);
                continue;
            }

            int atBats = parseIntSafe(tdElements.nth(0).textContent().trim());
            int hits = parseIntSafe(tdElements.nth(1).textContent().trim());
            int rbi = parseIntSafe(tdElements.nth(2).textContent().trim());
            int runs = parseIntSafe(tdElements.nth(3).textContent().trim());
            // index 4: 타율 - 제외

            records.add(new HitterRecordDto(battingOrder, position, playerName, atBats, hits, rbi, runs));
        }

        log.debug("타자 기록 추출 완료: table={}, count={}", table1Id, records.size());
        return records;
    }

    /**
     * 투수 기록 추출
     *
     * 컬럼 순서: 선수명(0), 등판(1), 결과(2), 승(3), 패(4), 세(5),
     *           이닝(6), 타자(7), 투구수(8), 타수(9), 피안타(10),
     *           홈런(11), 4사구(12), 삼진(13), 실점(14), 자책(15), 평균자책점(16) - 제외
     *
     * @param tableId tblAwayPitcher or tblHomePitcher
     */
    public List<PitcherRecordDto> extractPitcherRecords(final String tableId) {
        Locator rows = page.locator("#" + tableId + " tbody tr");
        int rowCount = rows.count();
        List<PitcherRecordDto> records = new ArrayList<>();

        for (int i = 0; i < rowCount; i++) {
            Locator tdElements = rows.nth(i).locator("td");

            String playerName = tdElements.nth(0).textContent().trim();
            String result = emptyToNull(tdElements.nth(2).textContent().trim());
            String innings = tdElements.nth(6).textContent().trim();
            int battersFaced = parseIntSafe(tdElements.nth(7).textContent().trim());
            int pitchCount = parseIntSafe(tdElements.nth(8).textContent().trim());
            int atBats = parseIntSafe(tdElements.nth(9).textContent().trim());
            int hitsAllowed = parseIntSafe(tdElements.nth(10).textContent().trim());
            int homeRunsAllowed = parseIntSafe(tdElements.nth(11).textContent().trim());
            int walksAndHbp = parseIntSafe(tdElements.nth(12).textContent().trim());
            int strikeouts = parseIntSafe(tdElements.nth(13).textContent().trim());
            int runsAllowed = parseIntSafe(tdElements.nth(14).textContent().trim());
            int earnedRuns = parseIntSafe(tdElements.nth(15).textContent().trim());
            // index 16: 평균자책점 - 제외

            records.add(new PitcherRecordDto(playerName, result, innings, battersFaced, pitchCount,
                    atBats, hitsAllowed, homeRunsAllowed, walksAndHbp, strikeouts,
                    runsAllowed, earnedRuns));
        }

        log.debug("투수 기록 추출 완료: table={}, count={}", tableId, records.size());
        return records;
    }

    private int parseIntSafe(final String text) {
        Integer value = parseNullableInt(text);
        return value != null ? value : 0;
    }
}
