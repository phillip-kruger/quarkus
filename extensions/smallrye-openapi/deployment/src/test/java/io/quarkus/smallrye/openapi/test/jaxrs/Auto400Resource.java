package io.quarkus.smallrye.openapi.test.jaxrs;

import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;

import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;

@Path("/auto")
public class Auto400Resource {

    @POST
    @Path("/400")
    public void addBar(MyBean myBean) {
        System.out.println(myBean.bar);
    }

    @POST
    @Path("/provided/400")
    @APIResponses({
            @APIResponse(responseCode = "204", description = "Successful"),
            @APIResponse(responseCode = "400", description = "Invalid bean supplied")
    })
    public void addProvidedBar(MyBean myBean) {
        System.out.println(myBean.bar);
    }

    private static class MyBean {
        public String bar;
    }

}
