package com.shawndha.shawnsdropwizardapi;

import io.dropwizard.Configuration;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.db.DataSourceFactory;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.Valid;
import javax.validation.constraints.*;
import java.io.UnsupportedEncodingException;

public class DropWizardApiConfiguration extends Configuration {
    @Valid
    @NotNull
    private DataSourceFactory database = new DataSourceFactory();

    @JsonProperty("database")
    public void setDataSourceFactory(DataSourceFactory factory) {
        this.database = factory;
    }

    @JsonProperty("database")
    public DataSourceFactory getDataSourceFactory() {
        return database;
    }

    @NotEmpty
    private String jwtTokenSecret = "changethistokenyeo";

    public byte[] getJwtTokenSecret() throws UnsupportedEncodingException {
        byte[] tokenSecret;
        try {
            tokenSecret =  jwtTokenSecret.getBytes("UTF-8");
        }catch(UnsupportedEncodingException e) {
            tokenSecret = null;
            //log exceeption
        };
        return tokenSecret;
    }
}
