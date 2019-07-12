package com.shawndha.shawnsdropwizardapi.resources.auth;

import com.shawndha.shawnsdropwizardapi.api.models.User;
import com.shawndha.shawnsdropwizardapi.api.daos.UserDao;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.keys.HmacKey;
import org.jose4j.lang.JoseException;
import org.json.simple.JSONObject;
import org.mindrot.jbcrypt.BCrypt;

import static org.jose4j.jws.AlgorithmIdentifiers.HMAC_SHA256;


@Path("/register")
@Consumes({MediaType.APPLICATION_JSON})
public class RegisterResource {
    private UserDao userDao;
    private final byte[] tokenSecret;

    public RegisterResource(UserDao userDao, byte[] tokenSecret) {
        this.tokenSecret = tokenSecret;
        this.userDao = userDao;
    }

    @POST
    public Response registerUser (User userSubmit) {
        if(userSubmit != null){
            if(userDao.findUser(userSubmit.getEmail()) == null){
                JSONObject requestResponse = new JSONObject();

                //Register user
                String hashedAndSaltedpw = BCrypt.hashpw(userSubmit.getPassword(), BCrypt.gensalt());
                userDao.insert(userSubmit.getEmail(), userSubmit.getFirstName(), userSubmit.getLastName(), hashedAndSaltedpw);
                User newUser = userDao.findUser(userSubmit.getEmail());

                // Create token
                final JwtClaims claims = new JwtClaims();
                claims.setSubject(userSubmit.getEmail());
                claims.setExpirationTimeMinutesInTheFuture(30);
                final JsonWebSignature jws = new JsonWebSignature();
                jws.setPayload(claims.toJson());
                jws.setAlgorithmHeaderValue(HMAC_SHA256);
                jws.setKey(new HmacKey(tokenSecret));

                //Configure response
                try {
                    requestResponse.put("token", jws.getCompactSerialization());
                    requestResponse.put("user", newUser.getEmail());
                    return Response.ok(requestResponse, MediaType.APPLICATION_JSON).build();
                }
                catch (JoseException e) {
                    requestResponse.put("error", "Internal server error"+e);
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).type(MediaType.APPLICATION_JSON).entity(requestResponse).build();

                }

            }
        } else {
            return Response.status(Response.Status.BAD_REQUEST).type(MediaType.APPLICATION_JSON).build();
        }

        //User already registered
        User user = userDao.findUser(userSubmit.getEmail());
        String errorMessage = user.getEmail().equals(userSubmit.getEmail()) ? "This email is already registered":"This username is already registered";
        JSONObject error = new JSONObject();
        error.put("error", errorMessage);
        return Response.status(Response.Status.CONFLICT).type(MediaType.APPLICATION_JSON).entity(error).build();
    }

    @OPTIONS
    public Response returnOptions (User userSubmit) {
        return Response.ok().build();

    }
}