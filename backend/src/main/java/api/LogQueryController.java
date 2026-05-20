@RestController
@RequestMapping("/api")
public class LogQueryController {

    private final VectorSearchService vectorSearchService;

    public LogQueryController(VectorSearchService vectorSearchService) {
        this.vectorSearchService = vectorSearchService;
    }

    @PostMapping(value = "/query", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> query(@RequestBody QueryRequest request) {
        return vectorSearchService.search(request.getQuestion());
    }
}
