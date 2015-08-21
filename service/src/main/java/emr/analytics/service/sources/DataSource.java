package emr.analytics.service.sources;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Optional;

public abstract  class DataSource {


    protected Optional<JSONObject> convertInputStreamToJson(InputStream stream) {

        JSONObject json = null;

        try {

            BufferedReader streamReader = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
            StringBuilder responseStrBuilder = new StringBuilder();

            String inputString;
            while ((inputString = streamReader.readLine()) != null) {

                responseStrBuilder.append(inputString);
            }

            json = new JSONObject(responseStrBuilder.toString());
        }
        catch (Exception exception) {

            exception.printStackTrace();
        }

        return Optional.of(json);
    }

    protected Optional<JSONObject> getDataFromWebService(String uri) {

        Optional<JSONObject> jsonResponse = Optional.empty();

        try {

            HttpGet get = new HttpGet(uri);

            CloseableHttpClient client = HttpClientBuilder.create().build();
            CloseableHttpResponse response = client.execute(get);
            HttpEntity httpEntity = response.getEntity();
            jsonResponse = convertInputStreamToJson(httpEntity.getContent());
            response.close();
            client.close();
        }
        catch (Exception exception) {

            exception.printStackTrace();
        }

        return jsonResponse;
    }
}
