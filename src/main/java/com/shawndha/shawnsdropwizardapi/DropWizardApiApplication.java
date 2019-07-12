package com.shawndha.shawnsdropwizardapi;

import com.github.toastshaman.dropwizard.auth.jwt.JwtAuthFilter;
import com.shawndha.shawnsdropwizardapi.api.models.User;
import com.shawndha.shawnsdropwizardapi.api.daos.UserDao;
import com.shawndha.shawnsdropwizardapi.resources.auth.LoginResource;
import com.shawndha.shawnsdropwizardapi.resources.auth.RegisterResource;
import com.shawndha.shawnsdropwizardapi.resources.account.UserResource;
import io.dropwizard.Application;
import io.dropwizard.auth.AuthDynamicFeature;
import io.dropwizard.auth.AuthValueFactoryProvider;
import io.dropwizard.auth.Authenticator;
import io.dropwizard.jdbi3.JdbiFactory;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.eclipse.jetty.servlets.CrossOriginFilter;
import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.EnumSet;
import java.util.Optional;

import org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature;
import org.jdbi.v3.core.Jdbi;
import org.jose4j.jwt.MalformedClaimException;
import org.jose4j.jwt.consumer.JwtConsumer;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;
import org.jose4j.jwt.consumer.JwtContext;
import org.jose4j.keys.HmacKey;


public class DropWizardApiApplication extends Application<DropWizardApiConfiguration> {

    public static void main(final String[] args) throws Exception {
        new DropWizardApiApplication().run(args);
    }

    @Override
    public String getName() {
        return "DropWizardApi";
    }

    @Override
    public void run(final DropWizardApiConfiguration configuration,
                    final Environment environment) throws UnsupportedEncodingException{

        final JdbiFactory factory = new JdbiFactory();
        final Jdbi jdbi = factory.build(environment, configuration.getDataSourceFactory(), "mysql");
        UserDao userDao = jdbi.onDemand(UserDao.class);

        final byte[] key = configuration.getJwtTokenSecret();

        final JwtConsumer consumer = new JwtConsumerBuilder()
                .setAllowedClockSkewInSeconds(30) // allow some leeway in validating time based claims to account for clock skew
                .setRequireExpirationTime() // the JWT must have an expiration time
                .setRequireSubject() // the JWT must have a subject claim
                .setVerificationKey(new HmacKey(key)) // verify the signature with the public key
                .setRelaxVerificationKeyValidation() // relaxes key length requirement
                .build(); // create the JwtConsumer instance

        environment.jersey().register(new AuthDynamicFeature(
                new JwtAuthFilter.Builder<User>()
                        .setJwtConsumer(consumer)
                        .setRealm("realm")
                        .setPrefix("Bearer")
                        .setAuthenticator(new UserAuthenticator(userDao))
                        .buildAuthFilter()));

        environment.jersey().register(new AuthValueFactoryProvider.Binder<>(Principal.class));
        environment.jersey().register(RolesAllowedDynamicFeature.class);

        configureCors(environment);

        environment.jersey().register(new RegisterResource(userDao, configuration.getJwtTokenSecret()));
        environment.jersey().register(new LoginResource(userDao, configuration.getJwtTokenSecret()));
        environment.jersey().register(new UserResource(userDao));

    }

    private void configureCors(Environment environment) {
        FilterRegistration.Dynamic filter = environment.servlets().addFilter("CORS", CrossOriginFilter.class);
        filter.addMappingForUrlPatterns(EnumSet.allOf(DispatcherType.class), true, "/*");
        filter.setInitParameter(CrossOriginFilter.ALLOWED_METHODS_PARAM, "GET,PUT,POST,DELETE,OPTIONS");
        filter.setInitParameter(CrossOriginFilter.ALLOWED_ORIGINS_PARAM, "*");
        filter.setInitParameter(CrossOriginFilter.ACCESS_CONTROL_ALLOW_ORIGIN_HEADER, "*");
        filter.setInitParameter("allowedHeaders", "Content-Type,Authorization,X-Requested-With,Content-Length,Accept,Origin");
        filter.setInitParameter("allowCredentials", "true");
    }

    private static class UserAuthenticator implements Authenticator<JwtContext, User> {

        private UserDao userDao;

        public UserAuthenticator(UserDao userDAO) {
            this.userDao = userDAO;
        }

        @Override
        public Optional<User> authenticate(JwtContext context) {
            try {
                final String subject = context.getJwtClaims().getSubject();
                if(userDao.findUser(subject) != null) {
                    User user = userDao.findUser(subject);
                    return Optional.of(user);
                }
                return Optional.empty();
            }
            catch (MalformedClaimException e) { return Optional.empty(); }
        }
    }

    @Override
    public void initialize(final Bootstrap<DropWizardApiConfiguration> bootstrap) {
        // TODO: application
    }

}
