package searchengine.dto.statistics;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
public class StatisticsResponse {
    private StatisticsData statistics;
    private boolean result;

    public StatisticsData getStatistics() {
        return statistics;
    }

    public void setStatistics(StatisticsData statistics) {
        this.statistics = statistics;
    }

    public boolean isResult() {
        return result;
    }

    public void setResult(boolean result) {
        this.result = result;
    }
}