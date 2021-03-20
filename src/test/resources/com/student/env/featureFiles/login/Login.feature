@Smoke
Feature: Login
  I want to use Login feature

  @Login @SmokeTest
  Scenario Outline: Login to para bank dummy application
    Given I open the application
    When I enter the <username> and <password>
    Then I verify the login is successful

    Examples: 
      | username | password |
      | john     | demo     |
      | a        | a        |
      | b        | b        |
      | admin    | admin    |
