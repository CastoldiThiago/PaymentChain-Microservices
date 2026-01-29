/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.paymentchain.service;
import com.auth0.jwk.GuavaCachedJwkProvider;
import com.auth0.jwk.Jwk;
import com.auth0.jwk.JwkProvider;
import com.auth0.jwk.SigningKeyNotFoundException;
import com.auth0.jwk.UrlJwkProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;
/**
 *
 * @author sotobotero
 */
@Service
public class JwtService {

    @Value("${keycloak.jwk-set-uri}")
    private String jwksUrl;

    @Value("${keycloak.certs-id}")
    private String certsId;

    @Cacheable(value = "jwkCache")
    public Jwk getJwk() throws Exception {
        URL url = new URL(jwksUrl);
        UrlJwkProvider urlJwkProvider = new UrlJwkProvider(url);
       Jwk get = urlJwkProvider.get(certsId.trim());  
        return get;
    }
}

