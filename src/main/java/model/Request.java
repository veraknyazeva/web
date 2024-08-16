package model;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Request {
    private String httpMethod;
    private String path;
    private String protocol;
    private Map<String, String> headers;
    private String body;
    private final Map<String, String> queryParams = new HashMap<>();

    public Request() {

    }

    //URI path: "/messages?param1=123&param2=1234&param3=12345"
    public Map<String, String> getQueryParams() {
        return queryParams;
    }

    public String getQueryParam(String name) {
        return queryParams.get(name);
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public void setHttpMethod(String httpMethod) {
        this.httpMethod = httpMethod;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        //  /what/time?param1=123&param2=234&param3=%D0%BF%D1%80%D0%B8%D0%B2%D0%B5%D1%82
        String[] splitPath = path.split("\\?");
        if (splitPath.length == 2) {
            List<NameValuePair> parse = URLEncodedUtils.parse(splitPath[1], StandardCharsets.UTF_8);
            for (int i = 0; i < parse.size(); i++) {
                queryParams.put(parse.get(i).getName(), parse.get(i).getValue());
            }
        }
        this.path = splitPath[0];
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }
}
