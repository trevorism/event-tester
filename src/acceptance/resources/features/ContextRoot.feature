Feature: Context Root of this API
  In order to use the Event Tester API, it must be available

  Scenario: ContextRoot https
    Given the testing application is alive
    When I navigate to https://event-tester.testing.trevorism.com
    Then the API returns a link to the help page

  Scenario: Ping https
    Given the testing application is alive
    When I navigate to /ping on https://event-tester.testing.trevorism.com
    Then pong is returned, to indicate the service is alive
