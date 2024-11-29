package step_definations;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.simple.parser.ParseException;

import class_files.APIMethod;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.testng.Assert;

public class step_definationapi {

    APIMethod apimethod = new APIMethod();
    Map<String, String> pathParams = new HashMap<>();
    Map<String, Object> latestResponseDetails = new HashMap<>();

    @Given("a post with ID {string} {string} exists in post")
    public void validate_post(String id, String condition) throws IOException, ParseException {
        pathParams.put("id", id);
        apimethod.get_endpoint_config("getPostById");
        latestResponseDetails = apimethod.makeApiCall(pathParams, null);
        apimethod.print_response(latestResponseDetails);

        if (condition.equals("do")) {
            assert_response_field(latestResponseDetails, "id", id);
        }
    }

    @When("I DELETE the post where:")
    public void I_DELETE_the_post_where(DataTable dataTable) throws IOException, ParseException {
        List<String> data = dataTable.asList();
        String postId = data.get(1);
        pathParams.put("id", postId);
        apimethod.get_endpoint_config("deletePostById");
        latestResponseDetails = apimethod.makeApiCall(pathParams, null);
        apimethod.print_response(latestResponseDetails);
    }

    @Then("the post with ID {string} should no longer exist")
    public void the_post_with_ID_should_no_longer_exist(String id) throws IOException, ParseException {
        pathParams.put("id", id);
        apimethod.get_endpoint_config("getPostById");
        latestResponseDetails = apimethod.makeApiCall(pathParams, null);
        Assert.assertEquals(latestResponseDetails.get("statusCode"), 404, "Post still exists!");
    }

    @When("I GET the post {string}")
    public void get_post(String id) throws IOException, ParseException {
        pathParams.put("id", id);
        apimethod.get_endpoint_config("getPostById");
        latestResponseDetails = apimethod.makeApiCall(pathParams, null);
        apimethod.print_response(latestResponseDetails);
        assert_response_field(latestResponseDetails,"title","Updated title");
    }

    @When("I PATCH the post {string} with the following details:")
    public void update_the_post(String id, DataTable dataTable) throws IOException, ParseException {
        List<Map<String, String>> data = dataTable.asMaps(String.class, String.class);

        Map<String, Object> payload = data.stream()
            .flatMap(row -> row.entrySet().stream())
            .filter(entry -> !entry.getKey().equalsIgnoreCase("status"))
            .collect(HashMap::new, (map, entry) -> map.put(entry.getKey(), entry.getValue()), HashMap::putAll);

        String statusCode = data.get(0).get("status");
        pathParams.put("id", id);

        apimethod.get_endpoint_config("updatePostById");
        latestResponseDetails = apimethod.makeApiCall(pathParams, payload);

        apimethod.print_response(latestResponseDetails);
        Assert.assertEquals(latestResponseDetails.get("statusCode"), Integer.parseInt(statusCode), "Status code mismatch!");
    }

    @Then("the response should have status code {string}")
    public void the_response_should_have_status_code(String expectedStatusCode) {
        int actualStatusCode = (int) latestResponseDetails.get("statusCode");
        Assert.assertEquals(actualStatusCode, Integer.parseInt(expectedStatusCode), "Status code mismatch!");
    }

    public void assert_response_field(Map<String, Object> responseDetails, String key, String expectedValue) {
        Map<String, Object> jsonResponse = (Map<String, Object>) responseDetails.get("responseBody");
        String actualValue = jsonResponse.get(key).toString();
        Assert.assertEquals(actualValue, expectedValue, "Mismatch for key: " + key);
    }
}
