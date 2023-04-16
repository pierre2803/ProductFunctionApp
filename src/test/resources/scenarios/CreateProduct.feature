Feature: Create Product for a product

  Scenario: Create a valid Product
    Given a request for product creation with a name ProductName
    Given a request to get a default thing
    When the user submit the product creation request
    Then the response status is 201
    Then the response should contains a location header for the newly created product
    Then the new product is persisted
    Then the new product is returned

  Scenario: Create a invalid product
    Given a request for an invalid product
    When  the user submit the product creation request
    Then  the response status is 400
    Then  the following response is returned:
    """
     { "errors": [ {"code": "PRD_POST_10001", "message": "No Product name provided."} ] }
    """

   Scenario: Create a duplicate product name
     Given a thing with name NoName exists
     Given a product with name ProductName exists
     Given a request for product creation with a name ProductName
     When  the user submit the product creation request
     Then  the response status is 409
     Then  the following response is returned:
     """
       { "errors": [ {"code": "PRD_POST_20001", "message": "A Product with name 'ProductName' already exists."} ] }
     """
