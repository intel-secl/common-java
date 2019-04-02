/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.shiro.file.cmd;

import com.intel.dcsg.cpg.console.Command;
import com.intel.mtwilson.Folders;
import com.intel.mtwilson.shiro.file.LoginDAO;
import com.intel.mtwilson.shiro.file.model.UserPassword;
import com.intel.mtwilson.shiro.file.model.UserPermission;
import java.io.File;
import java.io.IOException;
import java.util.List;
import org.apache.commons.configuration.Configuration;

public class EraseUsers implements Command {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(EraseUsers.class);
    
    private LoginDAO loginDAO;
    private Configuration options = null;

    @Override
    public void setOptions(Configuration options) {
        this.options = options;
    }
    
    public boolean isDeleteAll() {
        return options.getBoolean("all", false);
    }

    @Override
    public void execute(String[] args) throws Exception {

        File userFile = new File(Folders.configuration()+File.separator+"users.txt");
        File permissionFile = new File(Folders.configuration()+File.separator+"permissions.txt");

        loginDAO = new LoginDAO(userFile, permissionFile);
        if (options.containsKey("user")) {
            String username = options.getString("user");
            removeUser(username);
            removePermissions(username);
        } else if (!isDeleteAll()) {
            for (String username : loginDAO.listUsernames()) {
                removeUser(username);
                removePermissions(username);
            }
        }

    }

    private void removeUser(String username) throws IOException {
        UserPassword existingUser = loginDAO.findUserByName(username);
        if (existingUser != null) {
            loginDAO.deleteUserByName(username);
        }
    }

    private void removePermissions(String username) throws IOException {
        List<UserPermission> existingPermissions = loginDAO.getPermissions(username);
        for (UserPermission existingPermission : existingPermissions) {
            loginDAO.removePermission(username, existingPermission.toString());
        }
    }

}
