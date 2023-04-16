Feature: Retrieve a Product

  Scenario: Get a product by its Id
    Given the default product
    Given a request to get a default product
    When the user submits the product retrieval request
    Then the response status is 200
    Then the product is returned

  Scenario: Get a disabled product by its Id
    Given the disabled product
    When the user submits the product retrieval request
    Then the response status is 403
    Then the following response is returned:
    """
      { "errors": [ {"code": "PRD_GET_1001", "message": "Not allowed to access product 'ffffffff-0941-46ea-81a5-ee392dfaa167' or the product does not exist."} ] }
    """

  Scenario: Get an unknown product
    Given the default product
    Given a request to get the unknown product UNKNOWN_PRODUCT_ID
    When the user submits the product retrieval request
    Then the response status is 403
    Then the following response is returned:
    """
      { "errors": [ {"code": "PRD_GET_1001", "message": "Not allowed to access product '33c5fe4e-04be-436a-88df-fc12ae043ace' or the product does not exist."} ] }
    """