package searchengine.dto.statistics;

import java.util.List;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
public class StatisticsData {
    private List<DetailedStatisticsItem> detailed;
    private TotalStatistics total;

    public List<DetailedStatisticsItem> getDetailed() {
        return detailed;
    }

    public void setDetailed(List<DetailedStatisticsItem> detailed) {
        this.detailed = detailed;
    }

    public TotalStatistics getTotal() {
        return total;
    }

    public void setTotal(TotalStatistics total) {
        this.total = total;
    }
}