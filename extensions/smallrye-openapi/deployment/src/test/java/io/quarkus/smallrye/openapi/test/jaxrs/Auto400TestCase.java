package io.quarkus.smallrye.openapi.test.jaxrs;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.quarkus.test.QuarkusUnitTest;
import io.restassured.RestAssured;

class Auto400TestCase {
    @RegisterExtension
    static QuarkusUnitTest runner = new QuarkusUnitTest()
            .withApplicationRoot((jar) -> jar
                    .addClasses(Auto400Resource.class));

    @Test
    void test400InOpenApi() {
        RestAssured.given().header("Accept", "application/json")
                .when()
                .get("/q/openapi")
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(200)
                .body("paths.'/auto/400'.post.responses.400.description", Matchers.is("Bad Request"));
    }

    @Test
    void test400ProvidedInOpenApi() {
        RestAssured.given().header("Accept", "application/json")
                .when()
                .get("/q/openapi")
                .then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(200)
                .body("paths.'/auto/provided/400'.post.responses.400.description", Matchers.is("Invalid bean supplied"));
    }

}
