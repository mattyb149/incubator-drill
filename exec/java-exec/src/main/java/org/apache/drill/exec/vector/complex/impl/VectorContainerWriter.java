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
package org.apache.drill.exec.vector.complex.impl;

import org.apache.drill.common.types.TypeProtos.MajorType;
import org.apache.drill.exec.exception.SchemaChangeException;
import org.apache.drill.exec.physical.impl.OutputMutator;
import org.apache.drill.exec.record.MaterializedField;
import org.apache.drill.exec.vector.ValueVector;
import org.apache.drill.exec.vector.complex.MapVector;
import org.apache.drill.exec.vector.complex.writer.BaseWriter;
import org.apache.drill.exec.vector.complex.writer.BaseWriter.ComplexWriter;

public class VectorContainerWriter extends AbstractFieldWriter implements ComplexWriter {
  static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(VectorContainerWriter.class);

  SingleMapWriter mapRoot;
  SpecialMapVector mapVector;
  OutputMutator mutator;

  public VectorContainerWriter(OutputMutator mutator) {
    super(null);
    this.mutator = mutator;
    this.mapVector = new SpecialMapVector();
    this.mapRoot = new SingleMapWriter(mapVector, this);
  }

  public MaterializedField getField() {
    return mapVector.getField();
  }

  public void checkValueCapacity(){
    inform(mapVector.getValueCapacity() > idx());
  }

  public MapVector getMapVector() {
    return mapVector;
  }

  public void reset() {
    setPosition(0);
    resetState();
  }

  public void clear() {
    mapRoot.clear();
  }

  public SingleMapWriter getWriter() {
    return mapRoot;
  }

  public void setValueCount(int count) {
    mapRoot.setValueCount(count);
  }

  public void setPosition(int index) {
    super.setPosition(index);
    mapRoot.setPosition(index);
  }

  public MapWriter directMap() {
    return mapRoot;
  }

  @Override
  public void allocate() {
    mapRoot.allocate();
  }

  private class SpecialMapVector extends MapVector {

    public SpecialMapVector() {
      super("", null);
    }

    @Override
    public <T extends ValueVector> T addOrGet(String name, MajorType type, Class<T> clazz) {
      try {
        ValueVector v = mutator.addField(MaterializedField.create(name, type), clazz);
        this.put(name, v);
        return this.typeify(v, clazz);
      } catch (SchemaChangeException e) {
        throw new IllegalStateException(e);
      }

    }

  }

  @Override
  public MapWriter rootAsMap() {
    return mapRoot;
  }

  @Override
  public ListWriter rootAsList() {
    throw new UnsupportedOperationException(
        "Drill doesn't support objects whose first level is a scalar or array.  Objects must start as maps.");
  }
}
