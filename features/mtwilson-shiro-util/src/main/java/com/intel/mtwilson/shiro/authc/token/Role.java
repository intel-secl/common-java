/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.intel.mtwilson.shiro.authc.token;

public class Role {
   private String service;
   private String name;
   private String context;
   public Role(){}

   public String getService() {
      return service;
   }
   public void setService(String service) {
      this.service = service;
   }
   public String getName() {
      return name;
   }
   public void setName(String name) {
      this.name = name;
   }
   public String getContext() {
      return context;
   }
   public void setContext(String context) {
      this.context = context;
   }
}
