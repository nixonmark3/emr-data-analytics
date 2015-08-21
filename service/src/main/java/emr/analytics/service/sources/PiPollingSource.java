package emr.analytics.service.sources;

import com.fasterxml.jackson.databind.ObjectMapper;

import emr.analytics.models.messages.StreamingSourceRequest;
import emr.analytics.service.sources.serializers.PiResponse;

import org.json.JSONObject;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PiPollingSource extends DataSource implements StreamingSource {

    private final String url;
    private final List<String> keys;

    public PiPollingSource(StreamingSourceRequest request) {

        this.url = request.getStreamingSource().getUrl();
        this.keys = request.getStreamingSource().getKeys();
    }

    public SourceValues<Double> read() {

        SourceValues<Double> values = new SourceValues<Double>();

        try {

            long tNow = Instant.now().toEpochMilli();

            List<String> uriList = new ArrayList<>();

            keys.stream().forEach((key) -> {

                uriList.add(String.format("http://%s:8003/rawhistory/%s/%s?tag=%s", url, tNow, tNow, key));
            });

            ExecutorService executorService = Executors.newFixedThreadPool(10);

            List<Callable<Optional<JSONObject>>> tasks = new ArrayList<>();

            uriList.stream().forEach((uri) -> {

                tasks.add(() -> getDataFromWebService(uri));

            });

            executorService.invokeAll(tasks)
                    .stream()
                    .map(future -> {

                        try {

                            return future.get();
                        }
                        catch (Exception exception) {

                            throw new IllegalStateException(exception);
                        }
                    })
                    .forEach(jsonResponse -> {

                        try {

                            if (jsonResponse.isPresent()) {

                                PiResponse response = new ObjectMapper().readValue(jsonResponse.get().toString(), PiResponse.class);
                                values.add(response.getTag(), response.getData().get(0).get(1));
                            }
                        }
                        catch (Exception exception) {

                            exception.printStackTrace();
                        }
                    });

            executorService.shutdown();
        }
        catch (Exception e) {

            e.printStackTrace();
        }

        return values;
    }
}
