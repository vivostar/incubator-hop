/*! ******************************************************************************
 *
 * Hop : The Hop Orchestration Platform
 *
 * http://www.project-hop.org
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.apache.hop.pipeline;

import org.apache.hop.core.exception.HopException;
import org.apache.hop.pipeline.engine.IPipelineEngine;

/**
 * A started listener for pipeline engines
 *
 * @param <T> The executing object/subject (PipelineMeta)
 */
public interface IExecutionStartedListener<T> {

  /**
   * The execution started
   *
   * @param engine
   * @throws HopException
   */
  void started( IPipelineEngine<T> engine ) throws HopException;

}