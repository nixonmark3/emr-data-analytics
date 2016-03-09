package services;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

public class WebClient {

    public static String post(String url, List<NameValuePair> parameters){
        try {
            CloseableHttpClient client = HttpClients.createDefault();
            HttpPost post = new HttpPost(url);
            post.setEntity(new UrlEncodedFormEntity(parameters));

            CloseableHttpResponse response = client.execute(post);
            HttpEntity httpEntity = response.getEntity();
            String result = streamToString(httpEntity.getContent());

            response.close();
            client.close();

            return result;
        }
        catch(IOException ex){
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
    }

    public static String post(String url, String json){

        try {
            CloseableHttpClient client = HttpClients.createDefault();
            HttpPost post = new HttpPost(url);

            StringEntity entity = new StringEntity(json);
            post.setEntity(entity);
            post.setHeader("Content-type", "application/json");

            CloseableHttpResponse response = client.execute(post);
            HttpEntity httpEntity = response.getEntity();
            String result = streamToString(httpEntity.getContent());

            response.close();
            client.close();

            return result;
        }
        catch(IOException ex){
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
    }

    public static String streamToString(InputStream stream) {

        try {

            BufferedReader reader = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
            StringBuilder builder = new StringBuilder();

            String input;
            while ((input = reader.readLine()) != null)
                builder.append(String.format("%s\n", input));

            return builder.toString();
        }
        catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
    }
}
