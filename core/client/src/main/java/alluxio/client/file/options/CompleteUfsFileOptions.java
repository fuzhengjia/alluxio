/*
 * The Alluxio Open Foundation licenses this work under the Apache License, version 2.0
 * (the "License"). You may not use this work except in compliance with the License, which is
 * available at www.apache.org/licenses/LICENSE-2.0
 *
 * This software is distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied, as more fully set forth in the License.
 *
 * See the NOTICE file distributed with this work for information regarding copyright ownership.
 */

package alluxio.client.file.options;

import alluxio.Constants;
import alluxio.annotation.PublicApi;
import alluxio.client.ClientContext;
import alluxio.security.authorization.PermissionStatus;
import alluxio.thrift.CompleteUfsFileTOptions;

import com.google.common.base.Objects;
import com.google.common.base.Throwables;

import java.io.IOException;

import javax.annotation.concurrent.NotThreadSafe;

/**
 * Options for completing a UFS file. Currently we do not allow users to set arbitrary user and
 * group options. The user and group will be set to the user login.
 */
@PublicApi
@NotThreadSafe
public final class CompleteUfsFileOptions {
  /** The ufs user this file should be owned by. */
  private final String mUser;
  /** The ufs group this file should be owned by. */
  private final String mGroup;
  /** The ufs permission in short format, e.g. 0777. */
  private final short mPermission;

  /**
   * @return the default {@link CompleteUfsFileOptions}
   */
  public static CompleteUfsFileOptions defaults() {
    PermissionStatus ps = PermissionStatus.defaults();
    try {
      // Set user and group from user login module.
      ps.setUserFromLoginModule(ClientContext.getConf()).applyFileUMask(ClientContext.getConf());
      // TODO(chaomin): set permission based on the alluxio file. Not needed for now since the
      // file is always created with default permission.
    } catch (IOException e) {
      throw Throwables.propagate(e);
    }
    return new CompleteUfsFileOptions(ps.getUserName(), ps.getGroupName(),
        ps.getPermission().toShort());
  }

  private CompleteUfsFileOptions(String user, String group, short permission) {
    mUser = user;
    mGroup = group;
    mPermission = permission;
  }

  /**
   * @return the group which should own the file
   */
  public String getGroup() {
    return mGroup;
  }

  /**
   * @return the user who should own the file
   */
  public String getUser() {
    return mUser;
  }

  /**
   * @return the ufs permission in short format, e.g. 0777
   */
  public short getPermission() {
    return mPermission;
  }

  /**
   * @return if the group has been set
   */
  public boolean hasGroup() {
    return mGroup != null;
  }

  /**
   * @return if the user has been set
   */
  public boolean hasUser() {
    return mUser != null;
  }

  /**
   * @return if the posixPerm has been set
   */
  public boolean hasPosixPerm() {
    return mPermission != Constants.INVALID_PERMISSION;
  }

  @Override
  public boolean equals(Object o) {
    return this == o || o instanceof CompleteUfsFileOptions;
  }

  @Override
  public int hashCode() {
    return 0;
  }

  @Override
  public String toString() {
    return Objects.toStringHelper(this).toString();
  }

  /**
   * @return Thrift representation of the options
   */
  public CompleteUfsFileTOptions toThrift() {
    CompleteUfsFileTOptions options = new CompleteUfsFileTOptions();
    if (hasGroup()) {
      options.setGroup(mGroup);
    }
    if (hasUser()) {
      options.setUser(mUser);
    }
    if (hasPosixPerm()) {
      options.setPermission(mPermission);
    }
    return options;
  }
}
