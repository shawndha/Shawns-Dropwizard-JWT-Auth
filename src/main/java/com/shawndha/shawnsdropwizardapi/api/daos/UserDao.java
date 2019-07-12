package com.shawndha.shawnsdropwizardapi.api.daos;

import com.shawndha.shawnsdropwizardapi.api.models.User;
import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

public interface UserDao {


    @SqlQuery("select id, email, firstName, lastName, dateCreated from users where email = :email")
    @RegisterBeanMapper(User.class)
    User findUser(@Bind("email") String email);

    @SqlQuery("select id, email, firstName, lastName, password, dateCreated from users where email = :email")
    @RegisterBeanMapper(User.class)
    User getUserAccountDetails(@Bind("email") String email);

    @SqlUpdate("insert into users (email, firstName, lastName, password) values (:email, :firstName, :lastName, :password)")
    void insert(@Bind("email") String email, @Bind("firstName") String firstName, @Bind("lastName") String lastName, @Bind("password") String password);

}