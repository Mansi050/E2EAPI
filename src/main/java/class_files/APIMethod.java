package class_files;

import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.testng.Assert;

public class APIMethod {

    private static JSONObject endpointObject;
    private static String baseUrl;

    public APIMethod() {
    }

    public void get_endpoint_config(String endpointName) throws IOException, ParseException {
        JSONParser parser = new JSONParser();
        try (FileReader reader = new FileReader("src/main/resources/endpoint.json")) {
            JSONObject jsonObject = (JSONObject) parser.parse(reader);
            JSONObject endpointsConfig = (JSONObject) jsonObject.get("endpoints");
            endpointObject = (JSONObject) endpointsConfig.get(endpointName);
            baseUrl = (String) jsonObject.get("base_url");

            if (endpointObject == null) {
                throw new IllegalArgumentException("Endpoint configuration not found for: " + endpointName);
            }
        }
    }

    public static String constructUri(String baseUrl, String endpoint, Map<String, String> pathParams) {
        for (Map.Entry<String, String> entry : pathParams.entrySet()) {
            endpoint = endpoint.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        return baseUrl + endpoint;
    }

    public Map<String, Object> makeApiCall(Map<String, String> pathParams, 
                                           Map<String, Object> payload) throws IOException {
        String method = (String) endpointObject.get("method");
        String endpoint = (String) endpointObject.get("endpoint");
        String uri = constructUri(baseUrl, endpoint, pathParams);

        RequestSpecification request = RestAssured.given();

        Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json; charset=UTF-8");
                request.headers(headers);
                

        if (payload != null && !payload.isEmpty()) {
            ObjectMapper objectMapper = new ObjectMapper();
            String jsonPayload = objectMapper.writeValueAsString(payload);
            request.body(jsonPayload);
        }

        Response response;
        switch (method.toUpperCase()) {
            case "GET":
                response = request.get(uri);
                break;
            case "POST":
                response = request.post(uri);
                break;
            case "PUT":
                response = request.put(uri);
                break;
            case "DELETE":
                response = request.delete(uri);
                break;
            case "PATCH":
                response = request.patch(uri);
                break;
            default:
                throw new IllegalArgumentException("Unsupported HTTP method: " + method);
        }

        return processResponse(response);
    }

    private Map<String, Object> processResponse(Response response) {
        int statusCode = response.getStatusCode();
        String responseBody = response.getBody().asString();

        Map<String, Object> responseDetails = new HashMap<>();
        responseDetails.put("statusCode", statusCode);

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            if (responseBody.startsWith("{")) {
                responseDetails.put("responseBody", objectMapper.readValue(responseBody, Map.class));
            } else if (responseBody.startsWith("[")) {
                responseDetails.put("responseBody", objectMapper.readValue(responseBody, List.class));
            } else {
                responseDetails.put("responseBody", responseBody);
            }
        } catch (IOException e) {
            responseDetails.put("responseBody", responseBody);
        }

        return responseDetails;
    }

    public void print_response(Map<String, Object> responseDetails) {
        int statusCode = (int) responseDetails.get("statusCode");
        System.out.println("Status Code: " + statusCode);

        Object response = responseDetails.get("responseBody");
        System.out.println("Response Body: " + response);
    }

    public void assert_response_field(Map<String, Object> responseDetails, String key, String expectedValue) {
        Map<String, Object> responseBody = (Map<String, Object>) responseDetails.get("responseBody");
        String actualValue = responseBody.get(key).toString();
        Assert.assertEquals(actualValue, expectedValue, "Mismatch for key: " + key);
    }
}
