/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.shiro.file;

import com.intel.dcsg.cpg.io.FileResource;
import com.intel.dcsg.cpg.io.Resource;
import com.intel.mtwilson.shiro.file.model.UserPermission;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.IOUtils;
import org.apache.shiro.authz.permission.WildcardPermission;
import org.apache.shiro.util.StringUtils;

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
public class PermissionDAO {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(PermissionDAO.class);
    private static final Charset UTF8 = Charset.forName("UTF-8");
    private final Resource permissionResource;
    private final Map<String, List<UserPermission>> permissions;

    public PermissionDAO(Resource permissionResource) throws IOException {
        this.permissionResource = permissionResource;
        this.permissions = new HashMap<>();
        load();
    }

    /**
     *
     * @param permissionFile not required to exist, will be replaced or created
     * on save
     * @throws IOException
     */
    public PermissionDAO(File permissionFile) throws IOException {
        this(new FileResource(permissionFile));
    }

    public List<String> listUsernames() {
        ArrayList<String> list = new ArrayList<>();
        list.addAll(permissions.keySet());
        return list;
    }

    public void addPermission(String username, String permissionText) throws IOException {
        List<UserPermission> list = permissions.get(username);
        if (list == null) {
            list = new ArrayList<>();
        }
        log.debug("adding permission for user {}", username);
        list.add(UserPermission.parse(permissionText));
        permissions.put(username, list);
        save();
    }

    public void removeAll(String username) throws IOException {
        log.debug("removing all permissions for user {}", username);
        permissions.remove(username);
        save();
    }

    public void removePermission(String username, String permissionText) throws IOException {
        List<UserPermission> list = permissions.get(username);
        if (list == null) {
            return;
        }
        ArrayList<UserPermission> accepted = new ArrayList<>(); // new list of permissions for the user
        WildcardPermission removed = new WildcardPermission(permissionText);
        for (UserPermission permission : list) {
            WildcardPermission item = new WildcardPermission(permission.toString());
            if (!item.implies(removed)) {
                accepted.add(permission); // adding all permissions that are NOT the one we are removing
            }
        }
        if( accepted.isEmpty() ) {
            permissions.remove(username);
        }
        else {
            permissions.put(username, accepted);
        }
        save();
    }

    public List<UserPermission> getPermissions(String username) {
        List<UserPermission> list = permissions.get(username);
        if (list == null) {
            list = Collections.EMPTY_LIST;
        }
        return list;
    }

    private void save() throws IOException {
        // store permissions
        ArrayList<String> permissionLines = new ArrayList<>();
        for (String username : permissions.keySet()) {
            log.debug("Saving permissions for {}", username);
            List<UserPermission> permissionList = permissions.get(username);
            ArrayList<String> permissionTextList = new ArrayList<>();
            for (UserPermission permission : permissionList) {
                permissionTextList.add(permission.toString());
            }
            CommaSeparatedValues csv = new CommaSeparatedValues(permissionTextList);
            KeyValuePair line = new KeyValuePair(username, csv.toString());
            permissionLines.add(line.toString());
        }
        try (OutputStream out = permissionResource.getOutputStream()) {
            IOUtils.writeLines(permissionLines, IOUtils.LINE_SEPARATOR_UNIX, out, UTF8);
        }
    }

    private void load() throws IOException {
        // load permissions
        InputStream permissionInputStream = permissionResource.getInputStream();
        if (permissionInputStream == null) {
            log.debug("Permission file does not exist"); // not an error because we can have all the users loaded without permissions, then add permissions and save later
            return;
        }
        try (InputStream in = permissionInputStream) {
            List<String> permissionLines = IOUtils.readLines(in, UTF8);
            int lineNumber = 0;
            for (String line : permissionLines) {
                lineNumber++;
                try {
                    KeyValuePair userPermission = KeyValuePair.parse(line);
                    String user = userPermission.getKey();
                    CommaSeparatedValues permissionList = CommaSeparatedValues.parse(userPermission.getValue());
                    ArrayList<UserPermission> list = new ArrayList<>();
                    for (String item : permissionList.getValues()) {
                        list.add(UserPermission.parse(item));
                    }
                    permissions.put(user, list);
                } catch (Exception e) {
                    log.error("Cannot parse line {} of {}: {}", lineNumber, permissionResource, e.getMessage());
                }
            }
        }
    }

    public static class KeyValuePair {

        private String key;
        private String value;

        public KeyValuePair(String key, String value) {
            this.key = key;
            this.value = value;
        }

        public String getKey() {
            return key;
        }

        public String getValue() {
            return value;
        }

        @Override
        public String toString() {
            return String.format("%s=%s", key, value);
        }

        public static KeyValuePair parse(String text) {
            List<String> pair = Arrays.asList(text.split("\\s*=\\s*"));
            if (pair.size() != 2) {
                throw new IllegalArgumentException("Invalid format: " + text);
            }
            return new KeyValuePair(pair.get(0), pair.get(1));
        }
    }

    public static class CommaSeparatedValues {

        private final List<String> values;

        public CommaSeparatedValues(String... array) {
            this.values = Arrays.asList(array);
        }

        public CommaSeparatedValues(List<String> list) {
            this.values = list;
        }

        public List<String> getValues() {
            return values;
        }

        @Override
        public String toString() {
            return StringUtils.join(values.iterator(), ",");
        }

        public static CommaSeparatedValues parse(String text) {
            List<String> values = Arrays.asList(text.split("\\s*,\\s*"));
            return new CommaSeparatedValues(values);
        }
    }
}
