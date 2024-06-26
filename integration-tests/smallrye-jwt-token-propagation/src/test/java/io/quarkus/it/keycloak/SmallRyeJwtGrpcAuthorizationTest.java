package io.quarkus.it.keycloak;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import io.quarkus.test.common.WithTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;

@QuarkusTest
@WithTestResource(value = KeycloakRealmResourceManager.class, restrictToAnnotatedClass = false)
public class SmallRyeJwtGrpcAuthorizationTest {

    @Test
    public void test() {
        RestAssured.given().auth().oauth2(KeycloakRealmResourceManager.getAccessToken("john"))
                .when().get("/hello/admin")
                .then()
                .statusCode(500);
        RestAssured.given().auth().oauth2(KeycloakRealmResourceManager.getAccessToken("john"))
                .when().get("/hello/tester")
                .then()
                .statusCode(200)
                .body(Matchers.is("Hello Severus from john"));
    }
}
