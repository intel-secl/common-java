/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.shiro.file;

import com.intel.dcsg.cpg.http.MutableQuery;
import com.intel.dcsg.cpg.http.Query;
import com.intel.dcsg.cpg.io.FileResource;
import com.intel.dcsg.cpg.io.Resource;
import com.intel.mtwilson.shiro.file.model.UserPassword;
import com.intel.mtwilson.shiro.file.model.UserPermission;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;

/**
 * Change in format: Prior to KPL m7, the format was one user per line, where
 * each line looks like this:
 *
 * username:SHA256:waxw1ePem7NH7WGws0zDvg==:jm1BbbPtGrCltNte2F4PnLbiz7gJTj2rqm6EeITGmno=
 *
 * This is 4 fields separated by colon,
 * `username:algorithm:base64(salt):base64(hashed-password)` with an assumed
 * iteration count of 1. When parsing this line, we split on `:` and then take
 * the first four items in that order. This is forward compatible with anything
 * that has the same first four parameters and additional data after a fourth
 * `:`.
 *
 * Starting in KPL m7, the format is:
 *
 * username:SHA256:waxw1ePem7NH7WGws0zDvg==:jm1BbbPtGrCltNte2F4PnLbiz7gJTj2rqm6EeITGmno=:i=100000
 *
 * This is 5 fields separated by colon,
 * `username:algorithm:base64(salt):base64(hashed-password):parameters`. which
 * adds a new backward-compatible `parameters` field. When parsing this line, we
 * split on `:` and take the first four items as they are, and if the fifth item
 * `parameters` is present we parse it as a query string which is a set of
 * `key=value` pairs separated by `&` where keys and values are url-encoded. The
 * `:` character is allowed in pre-encoded keys and values because url-encoding
 * changes it to %3A so it would not interfere with the use of `:` as the
 * general delimiter.
 *
 * To help with unit testing, new constructors and methods are introduced which
 * accept a Resource for the user file and permission file, so the class can now
 * read files from disk, test resources, or any input stream.
 *
 * Starting in KPL m8, the PermissionDAO is now a separate class which is used
 * by LoginDAO. This provides flexibility in tracking permissions for users who
 * have a password login and possibly another login method like HMAC, Token,
 * or X509.
 *
 * @author jbuhacoff
 */
public class LoginDAO  {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LoginDAO.class);

    private static final Charset UTF8 = Charset.forName("UTF-8");
    private final Resource userResource;
    private final PermissionDAO permissionDAO;
    private final Map<String, UserPassword> users;

    public LoginDAO(Resource userResource) throws IOException {
        this.userResource = userResource;
        this.permissionDAO = null;
        this.users = new HashMap<>();
        load();
    }

    public LoginDAO(Resource userResource, Resource permissionResource) throws IOException {
        this.userResource = userResource;
        this.permissionDAO = new PermissionDAO(permissionResource);
        this.users = new HashMap<>();
        load();
    }

    /**
     *
     * @param userFile not required to exist, an empty database will be used if
     * the file is not found on load
     * @param permissionFile not required to exist, will be replaced or created
     * on save
     * @throws IOException
     */
    public LoginDAO(File userFile, File permissionFile) throws IOException {
        this(new FileResource(userFile), new FileResource(permissionFile));
    }
    
    private String toString(UserPassword userLoginPassword) {
        String parameters = toQueryParameters("i",String.valueOf(userLoginPassword.getIterations()));
        return String.format("%s:%s:%s:%s:%s",
                userLoginPassword.getUsername(),
                userLoginPassword.getAlgorithm(),
                Base64.encodeBase64String(userLoginPassword.getSalt()),
                Base64.encodeBase64String(userLoginPassword.getPasswordHash()),
                parameters
                );
    }

    /**
     *
     * @param args key1,value1,key2,value2,...  if a value is null it must be included as `null`
     * @return
     */
    private String toQueryParameters(String... args) {
        MutableQuery query = new MutableQuery();
        for(int i=0; i<args.length; i+=2) {
            query.add(args[i], args[i+1]);
        }
        return query.toString();
    }

    private Map<String,String> fromQueryParameters(String queryParameters) {
        return Query.parseSinglevalued(queryParameters);
    }
    
    private UserPassword toUserLoginPassword(String text) {
        String[] parts = text.split(":");
        UserPassword userLoginPassword = new UserPassword();
        userLoginPassword.setUsername(parts[0]);
        userLoginPassword.setAlgorithm(parts[1]);
        userLoginPassword.setSalt(Base64.decodeBase64(parts[2]));
        userLoginPassword.setPasswordHash(Base64.decodeBase64(parts[3]));
        if( parts.length > 4 ) {
            Map<String,String> parameters = fromQueryParameters(parts[4]);
            String iterations = parameters.get("i");
            if( iterations != null ) {
                userLoginPassword.setIterations(Integer.valueOf(iterations));
            }
        }
        return userLoginPassword;
    }

    public void createUser(UserPassword userLoginPassword) throws IOException {
        if( users.containsKey(userLoginPassword.getUsername())) {
            throw new IllegalArgumentException("User already exists");
        }
        users.put(userLoginPassword.getUsername(), userLoginPassword);
        save();
    }
    
    public void storeUser(UserPassword userLoginPassword) throws IOException {
        if( !users.containsKey(userLoginPassword.getUsername())) {
            throw new IllegalArgumentException("User does not exist");
        }
        users.put(userLoginPassword.getUsername(), userLoginPassword);
        save();
    }
    
    public UserPassword findUserByName(String username) {
        return users.get(username);
    }
    
    public List<String> listUsernames() {
        ArrayList<String> list = new ArrayList<>();
        list.addAll(users.keySet());
        return list;
    }
    
    public void deleteUserByName(String username) throws IOException {
        if( !users.containsKey(username)) {
            throw new IllegalArgumentException("User does not exist");
        }
        users.remove(username);
        save();
        if( permissionDAO != null ) {
            permissionDAO.removeAll(username);
        }
    }

    /**
     *
     * @param username
     * @param permissionText
     * @throws IOException
     * @throws NullPointerException if LoginDAO was not initialized with a permission resource or permission file
     */
    public void addPermission(String username, String permissionText) throws IOException {
        permissionDAO.addPermission(username, permissionText);
    }

    /**
     *
     * @param username
     * @param permissionText
     * @throws IOException
     * @throws NullPointerException if LoginDAO was not initialized with a permission resource or permission file
     */
    public void removePermission(String username, String permissionText) throws IOException {
        permissionDAO.removePermission(username, permissionText);
    }

    /**
     *
     * @param username
     * @return
     * @throws NullPointerException if LoginDAO was not initialized with a permission resource or permission file
     */
    public List<UserPermission> getPermissions(String username) {
        return permissionDAO.getPermissions(username);
    }

    private void save() throws IOException {
        ArrayList<String> userLines = new ArrayList<>();
        // store users
        for(String username : users.keySet()) {
            UserPassword userLoginPassword = users.get(username);
            String line = toString(userLoginPassword);
            userLines.add(line);
        }
        try (OutputStream out = userResource.getOutputStream()) {
            IOUtils.writeLines(userLines, IOUtils.LINE_SEPARATOR_UNIX, out, UTF8);
        }
    }
    
    private void load() throws IOException {
        InputStream userInputStream = userResource.getInputStream();
        if (userInputStream == null) {
            log.debug("Password file does not exist");
            return; // not an error because caller can add users and then call save() to create the password file; and since we don't have any users defined, there isn't any need to continue and read the permission file either
        }
        // load users
        try (InputStream in = userInputStream) {
            List<String> userLines = IOUtils.readLines(in, UTF8);
            for (String line : userLines) {
                UserPassword userLoginPassword = toUserLoginPassword(line);
                users.put(userLoginPassword.getUsername(), userLoginPassword);
            }
        }
    }
}
