/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.drill.exec.dotdrill;

import org.apache.drill.common.config.DrillConfig;
import org.apache.drill.exec.store.dfs.shim.DrillFileSystem;
import org.apache.drill.exec.store.dfs.shim.DrillInputStream;
import org.apache.hadoop.fs.FileStatus;

import com.google.common.base.Preconditions;

public class DotDrillFile {

  private FileStatus status;
  private DotDrillType type;
  private DrillFileSystem fs;

  public static DotDrillFile create(DrillFileSystem fs, FileStatus status){
    for(DotDrillType d : DotDrillType.values()){
      if(!status.isDir() && d.matches(status)){
        return new DotDrillFile(fs, status, d);
      }
    }
    return null;
  }

  private DotDrillFile(DrillFileSystem fs, FileStatus status, DotDrillType type){
    this.fs = fs;
    this.status = status;
    this.type = type;
  }

  public DotDrillType getType(){
    return type;
  }

  public View getView(DrillConfig config) throws Exception{
    Preconditions.checkArgument(type == DotDrillType.VIEW);
    try(DrillInputStream is = fs.open(status.getPath())){
      return config.getMapper().readValue(is.getInputStream(), View.class);
    }
  }
}
