/**
 * Copyright (C) 2018-2019 Expedia, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.expediagroup.apiary.extensions.events.metastore.consumer.privilegesgrantor.core;

import org.apache.hadoop.hive.metastore.IMetaStoreClient;
import org.apache.hadoop.hive.metastore.api.HiveObjectPrivilege;
import org.apache.hadoop.hive.metastore.api.HiveObjectRef;
import org.apache.hadoop.hive.metastore.api.HiveObjectType;
import org.apache.hadoop.hive.metastore.api.PrincipalType;
import org.apache.hadoop.hive.metastore.api.PrivilegeBag;
import org.apache.hadoop.hive.metastore.api.PrivilegeGrantInfo;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.expediagroup.apiary.extensions.events.metastore.consumer.common.exception.HiveClientException;

/**
 * Grants privileges to a table using Hive Metastore Thrift Client.
 */
public class PrivilegesGrantor {

  private static final Logger log = LoggerFactory.getLogger(PrivilegesGrantor.class);
  private final IMetaStoreClient client;

  public PrivilegesGrantor(IMetaStoreClient client) {
    this.client = client;
  }

  /**
   * Grants Select privilege to a Public Principal.
   *
   * @param tableName
   */
  public void grantSelectPrivileges(String dbName, String tableName) {
    log.info("Granting Public Select Privileges to the table: " + tableName);
    try {
      if (isSelectPrivilegeGranted(dbName, tableName)) {
        log.info("Skipping Granting Public privileges, the privilege is already granted on the table: " + tableName);
      } else {
        PrivilegeBag privilegeBag = new PrivilegeBag();
        privilegeBag.addToPrivileges(getPublicSelectPrivilege(dbName, tableName));
        client.grant_privileges(privilegeBag);
        log.info("Successfully granted Public Select Privileges to the table: " + tableName);
      }
    } catch (TException e) {
      throw new HiveClientException(("Error Granting Public Select Privileges to the table: " + tableName), e);
    }
  }

  /**
   * Validates if select privilege is granted to a public principal.
   *
   * @param tableName
   * @param dbName
   * @return boolean
   */
  public boolean isSelectPrivilegeGranted(String dbName, String tableName) {
    HiveObjectRef hiveObjectRef = new HiveObjectRef();
    hiveObjectRef.setDbName(dbName);
    hiveObjectRef.setObjectType(HiveObjectType.TABLE);
    hiveObjectRef.setObjectName(tableName);

    try {
      return client
          .list_privileges(PrincipalName.PUBLIC.toString(), PrincipalType.ROLE, hiveObjectRef)
          .stream()
          .anyMatch(privilege -> privilege.getGrantInfo().getPrivilege().equals(Privilege.SELECT.toString()));
    } catch (TException e) {
      throw new HiveClientException(("Error checking if Select Privilege is granted on the table: " + tableName), e);
    }
  }

  private HiveObjectPrivilege getPublicSelectPrivilege(String dbName, String tableName) {
    HiveObjectRef hiveObjectRef = new HiveObjectRef();
    hiveObjectRef.setDbName(dbName);
    hiveObjectRef.setObjectType(HiveObjectType.TABLE);
    hiveObjectRef.setObjectName(tableName);

    PrivilegeGrantInfo privilegeGrantInfo = new PrivilegeGrantInfo(Privilege.SELECT.toString(), 0,
        Grantor.APIARY_PRIVILEGE_GRANTOR.toString(), PrincipalType.ROLE, false);

    HiveObjectPrivilege hiveObjectPrivilege = new HiveObjectPrivilege();
    hiveObjectPrivilege.setHiveObject(hiveObjectRef);
    hiveObjectPrivilege.setPrincipalName(PrincipalName.PUBLIC.toString());
    hiveObjectPrivilege.setPrincipalType(PrincipalType.ROLE);
    hiveObjectPrivilege.setGrantInfo(privilegeGrantInfo);
    return hiveObjectPrivilege;
  }
}
