@Component
public class KafkaLogConsumer {

    private final VectorSearchService vectorSearchService;

    public KafkaLogConsumer(VectorSearchService vectorSearchService) {
        this.vectorSearchService = vectorSearchService;
    }

    @KafkaListener(topics = "application-logs", groupId = "log-ingestion")
    public void consume(String logEvent) {
        vectorSearchService.embedAndStore(logEvent);
    }
}
