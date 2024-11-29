Feature: End-to-End API Testing for API

  Scenario: Delete a post and validate it is removed
    Given a post with ID "1" "do" exists in post
    When I DELETE the post where:
      | id |
      | 1  |
    Then the post with ID "1" should no longer exist

  Scenario: Fetch a non-existent post and validate the status code
    Given a post with ID "999" "donot" exists in post
    Then the response should have status code "404"

  Scenario: Update a post title and verify changes
    Given a post with ID "3" "do" exists in post
    When I PATCH the post "3" with the following details:
      | title         | status |
      | Updated Title | 200    |
    Then I GET the post "3"
