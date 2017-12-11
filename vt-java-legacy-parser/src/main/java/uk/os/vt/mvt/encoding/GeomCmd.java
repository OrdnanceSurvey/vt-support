/*
 * Copyright (C) 2017 Weather Decision Technologies
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.os.vt.mvt.encoding;

/**
 * MVT draw command types.
 *
 * @see GeomCmdHdr
 */
public enum GeomCmd {
  MoveTo(1, 2),
  LineTo(2, 2),
  ClosePath(7, 0);

  /**
   * Unique command ID.
   */
  private final int cmdId;

  /**
   * Amount of parameters that follow the command.
   */
  private final int paramCount;

  GeomCmd(int cmdId, int paramCount) {
    this.cmdId = cmdId;
    this.paramCount = paramCount;
  }

  /**
   * @return unique command ID.
   */
  public int getCmdId() {
    return cmdId;
  }

  /**
   * @return amount of parameters that follow the command.
   */
  public int getParamCount() {
    return paramCount;
  }


  /**
   * Return matching {@link GeomCmd} for the provided cmdId, or null if there is not
   * a matching command.
   *
   * @param cmdId command id to find match for
   * @return command with matching id, or null if there is not a matching command
   */
  public static GeomCmd fromId(int cmdId) {
    final GeomCmd geomCmd;
    switch (cmdId) {
      case 1:
        geomCmd = MoveTo;
        break;
      case 2:
        geomCmd = LineTo;
        break;
      case 7:
        geomCmd = ClosePath;
        break;
      default:
        geomCmd = null;
    }
    return geomCmd;
  }
}
