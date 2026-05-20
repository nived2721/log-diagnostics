@Service
public class BedrockClient {

    private final BedrockRuntimeClient client;

    public BedrockClient() {
        this.client = BedrockRuntimeClient.builder()
            .region(Region.US_EAST_1)
            .build();
    }

    public String invoke(String prompt) {
        InvokeModelRequest request = InvokeModelRequest.builder()
            .modelId("anthropic.claude-3-sonnet-20240229-v1:0")
            .body(SdkBytes.fromUtf8String(buildPayload(prompt)))
            .build();
        return client.invokeModel(request).body().asUtf8String();
    }
}
