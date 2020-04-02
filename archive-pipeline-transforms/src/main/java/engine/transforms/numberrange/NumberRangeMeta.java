/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
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

package org.apache.hop.pipeline.transforms.numberrange;

import org.apache.hop.core.CheckResult;
import org.apache.hop.core.CheckResultInterface;
import org.apache.hop.core.Const;
import org.apache.hop.core.exception.HopTransformException;
import org.apache.hop.core.exception.HopXMLException;
import org.apache.hop.core.row.IRowMeta;
import org.apache.hop.core.row.IValueMeta;
import org.apache.hop.core.row.value.ValueMetaString;
import org.apache.hop.core.variables.iVariables;
import org.apache.hop.core.xml.XMLHandler;
import org.apache.hop.metastore.api.IMetaStore;
import org.apache.hop.pipeline.Pipeline;
import org.apache.hop.pipeline.PipelineMeta;
import org.apache.hop.pipeline.transform.BaseTransformMeta;
import org.apache.hop.pipeline.transform.ITransformData;
import org.apache.hop.pipeline.transform.ITransform;
import org.apache.hop.pipeline.transform.TransformMeta;
import org.apache.hop.pipeline.transform.TransformMetaInterface;
import org.w3c.dom.Node;

import java.util.LinkedList;
import java.util.List;

/**
 * Configuration for the NumberRangePlugin
 *
 * @author ronny.roeller@fredhopper.com
 */

public class NumberRangeMeta extends BaseTransformMeta implements TransformMetaInterface {

  private String inputField;

  private String outputField;

  private String fallBackValue;

  private List<NumberRangeRule> rules;

  public NumberRangeMeta() {
    super();
  }

  public void emptyRules() {
    rules = new LinkedList<NumberRangeRule>();
  }

  public NumberRangeMeta( Node transformNode, IMetaStore metaStore ) throws HopXMLException {
    loadXML( transformNode, metaStore );
  }

  @Override
  public String getXML() {
    StringBuilder retval = new StringBuilder();

    retval.append( "    " ).append( XMLHandler.addTagValue( "inputField", inputField ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "outputField", outputField ) );
    retval.append( "    " ).append( XMLHandler.addTagValue( "fallBackValue", getFallBackValue() ) );

    retval.append( "    <rules>" ).append( Const.CR );
    for ( NumberRangeRule rule : rules ) {
      retval.append( "      <rule>" ).append( Const.CR );
      retval.append( "        " ).append( XMLHandler.addTagValue( "lower_bound", rule.getLowerBound() ) );
      retval.append( "        " ).append( XMLHandler.addTagValue( "upper_bound", rule.getUpperBound() ) );
      retval.append( "        " ).append( XMLHandler.addTagValue( "value", rule.getValue() ) );
      retval.append( "      </rule>" ).append( Const.CR );
    }
    retval.append( "    </rules>" ).append( Const.CR );

    return retval.toString();
  }

  @Override
  public void getFields( IRowMeta row, String name, IRowMeta[] info, TransformMeta nextTransform,
                         iVariables variables, IMetaStore metaStore ) throws HopTransformException {
    IValueMeta mcValue = new ValueMetaString( outputField );
    mcValue.setOrigin( name );
    mcValue.setLength( 255 );
    row.addValueMeta( mcValue );
  }

  @Override
  public Object clone() {
    Object retval = super.clone();
    return retval;
  }

  @Override
  public void loadXML( Node transformNode, IMetaStore metaStore ) throws HopXMLException {
    try {
      inputField = XMLHandler.getTagValue( transformNode, "inputField" );
      outputField = XMLHandler.getTagValue( transformNode, "outputField" );

      emptyRules();
      String fallBackValue = XMLHandler.getTagValue( transformNode, "fallBackValue" );
      setFallBackValue( fallBackValue );

      Node fields = XMLHandler.getSubNode( transformNode, "rules" );
      int count = XMLHandler.countNodes( fields, "rule" );
      for ( int i = 0; i < count; i++ ) {

        Node fnode = XMLHandler.getSubNodeByNr( fields, "rule", i );

        String lowerBoundStr = XMLHandler.getTagValue( fnode, "lower_bound" );
        String upperBoundStr = XMLHandler.getTagValue( fnode, "upper_bound" );
        String value = XMLHandler.getTagValue( fnode, "value" );

        double lowerBound = Double.parseDouble( lowerBoundStr );
        double upperBound = Double.parseDouble( upperBoundStr );
        addRule( lowerBound, upperBound, value );
      }

    } catch ( Exception e ) {
      throw new HopXMLException( "Unable to read transform info from XML node", e );
    }
  }

  @Override
  public void setDefault() {
    emptyRules();
    setFallBackValue( "unknown" );
    addRule( -Double.MAX_VALUE, 5, "Less than 5" );
    addRule( 5, 10, "5-10" );
    addRule( 10, Double.MAX_VALUE, "More than 10" );
    inputField = "";
    outputField = "range";
  }

  @Override
  public void check( List<CheckResultInterface> remarks, PipelineMeta pipelineMeta, TransformMeta transforminfo,
                     IRowMeta prev, String[] input, String[] output, IRowMeta info, iVariables variables,
                     IMetaStore metaStore ) {
    CheckResult cr;
    if ( prev == null || prev.size() == 0 ) {
      cr =
        new CheckResult(
          CheckResult.TYPE_RESULT_WARNING, "Not receiving any fields from previous transforms!", transforminfo );
      remarks.add( cr );
    } else {
      cr =
        new CheckResult( CheckResult.TYPE_RESULT_OK, "Transform is connected to previous one, receiving "
          + prev.size() + " fields", transforminfo );
      remarks.add( cr );
    }

    // See if we have input streams leading to this transform!
    if ( input.length > 0 ) {
      cr = new CheckResult( CheckResult.TYPE_RESULT_OK, "Transform is receiving info from other transforms.", transforminfo );
      remarks.add( cr );
    } else {
      cr = new CheckResult( CheckResult.TYPE_RESULT_ERROR, "No input received from other transforms!", transforminfo );
      remarks.add( cr );
    }
  }

  @Override
  public ITransform getTransform( TransformMeta transformMeta, ITransformData iTransformData, int cnr,
                                PipelineMeta pipelineMeta, Pipeline disp ) {
    return new NumberRange( transformMeta, iTransformData, cnr, pipelineMeta, disp );
  }

  @Override
  public ITransformData getTransformData() {
    return new NumberRangeData();
  }

  public String getInputField() {
    return inputField;
  }

  public String getOutputField() {
    return outputField;
  }

  public void setOutputField( String outputField ) {
    this.outputField = outputField;
  }

  public List<NumberRangeRule> getRules() {
    return rules;
  }

  public String getFallBackValue() {
    return fallBackValue;
  }

  public void setInputField( String inputField ) {
    this.inputField = inputField;
  }

  public void setFallBackValue( String fallBackValue ) {
    this.fallBackValue = fallBackValue;
  }

  public void addRule( double lowerBound, double upperBound, String value ) {
    NumberRangeRule rule = new NumberRangeRule( lowerBound, upperBound, value );
    rules.add( rule );
  }

  public void setRules( List<NumberRangeRule> rules ) {
    this.rules = rules;
  }

  @Override
  public boolean supportsErrorHandling() {
    return true;
  }
}