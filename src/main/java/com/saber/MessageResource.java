package com.saber;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/amqp")
@ApplicationScoped

public class MessageResource {

    @Inject
    private MessageService service;

    @POST
    @Path("messaging")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    public Response sendMessage(MessageDto messageDto) {
        service.send(messageDto.getMessage());
        return Response.accepted().build();
    }

    @POST
    @Path("movie")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    public Response sendMovie(Movie movie) {
        service.sendMovie(movie);
        return Response.accepted("your message send success ").build();
    }
}
