package searchengine.dto.statistics;

import lombok.Data;

@Data
public class StatisticsResponse {
    private StatisticsData statistics;
    private boolean result;

    public boolean isResult() {
        return result;
    }

    public void setResult(boolean result) {
        this.result = result;
    }

    public StatisticsData getStatistics() {
        return statistics;
    }

    public void setStatistics(StatisticsData statistics) {
        this.statistics = statistics;
    }
}
