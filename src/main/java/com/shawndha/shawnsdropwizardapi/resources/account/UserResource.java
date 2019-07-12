package com.shawndha.shawnsdropwizardapi.resources.account;

import com.shawndha.shawnsdropwizardapi.api.models.User;
import com.shawndha.shawnsdropwizardapi.api.daos.UserDao;
import io.dropwizard.auth.Auth;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.security.Principal;

@Path("/user")
public class UserResource {

    private UserDao userDao;

    public UserResource(UserDao userDao) {
        this.userDao = userDao;
    }

    @GET
    public Response accountDetails(@Auth Principal user){
        User userAccountDetails = userDao.findUser(user.getName());
        return Response.status(Response.Status.OK).type(MediaType.APPLICATION_JSON).entity(userAccountDetails).build();
    }
}
