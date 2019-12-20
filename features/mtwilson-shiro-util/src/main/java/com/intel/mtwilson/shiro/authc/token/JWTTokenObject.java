/*
 * Copyright (C) 2014 Intel Corporation
 * All rights reserved.
 */

package com.intel.mtwilson.shiro.authc.token;

public class JWTTokenObject {
   private long expirytime;
   private Role[] roles;
   public JWTTokenObject(){}
   public long getExp() {
      return expirytime;
   }
   public void setExp(long expirytime) {
      this.expirytime = expirytime;
   }
   public Role[] getRoles() {
      return roles;
   }
   public void setRoles(Role[] roles) {
      this.roles = roles;
   }
   /*public String toString(){
      return "JWTToken [ name: "+name+", age: "+ age+ " ]";
   }*/
}
