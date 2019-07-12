package com.shawndha.shawnsdropwizardapi.resources.auth;

import com.shawndha.shawnsdropwizardapi.api.models.User;
import com.shawndha.shawnsdropwizardapi.api.daos.UserDao;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.keys.HmacKey;
import org.jose4j.lang.JoseException;
import org.json.simple.JSONObject;
import org.mindrot.jbcrypt.BCrypt;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static org.jose4j.jws.AlgorithmIdentifiers.HMAC_SHA256;

@Path("/login")
public class LoginResource {

    private final byte[] tokenSecret;
    private UserDao userDao;

    public LoginResource(UserDao userDao, byte[] tokenSecret) {
        this.userDao = userDao;
        this.tokenSecret = tokenSecret;
    }

    @POST
    public Response login(User userSubmit) {
        JSONObject requestResponse = new JSONObject();
        if(userSubmit != null){
            User userAccountDetails = userDao.getUserAccountDetails(userSubmit.getEmail());
            if(userAccountDetails != null){
                if(BCrypt.checkpw(userSubmit.getPassword(), userAccountDetails.getPassword())){

                    //Create token
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
                        requestResponse.put("user", userAccountDetails.getEmail());
                        return Response.ok(requestResponse, MediaType.APPLICATION_JSON).build();
                    }
                    catch (JoseException e) {
                        requestResponse.put("error", "Internal server error"+e);
                        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).type(MediaType.APPLICATION_JSON).entity(requestResponse).build();

                    }

                }
            }
        }
        requestResponse.put("error", "Invalid username or password");
        return Response.status(Response.Status.FORBIDDEN).type(MediaType.APPLICATION_JSON).entity(requestResponse).build();
    }
}
