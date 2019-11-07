/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.intel.mtwilson.shiro.file;

/**
 *
 * @author ascrawfo
 */
public interface UserEventHook2 {
    void beforeCreateUser(String username, char[] password); 
    void afterCreateUser(String username, char[] password); 
    void beforeChangePassword(String username, char[] oldPassword, char[] newPassword);
    void afterChangePassword(String username, char[] oldPassword, char[] newPassword);
    void beforeDeleteUser(String username);
}
