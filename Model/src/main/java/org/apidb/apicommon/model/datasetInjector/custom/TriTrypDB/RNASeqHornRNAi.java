package org.apidb.apicommon.model.datasetInjector.custom.TriTrypDB;

import org.apidb.apicommon.datasetPresenter.DatasetInjector;

public class RNASeqHornRNAi extends DatasetInjector {

  @Override
  public void injectTemplates() { }
  public void TODO_injectTemplates() {
      setPropValue("datasetClassCategoryIri", "http://edamontology.org/topic_3298");
      setPropValue("includeProjectsExcludeEuPathDB", "TriTrypDB,UniDB");
      setShortAttribution();
      String organismAbbrevDisplay = "T. brucei brucei TREU927"; //getPropValue("organismAbbrevDisplay");
      setPropValue("organismAbbrevDisplay", organismAbbrevDisplay.replace(":", ""));

      injectTemplate("datasetCategory");
      injectTemplate("profileSampleAttributesCategory");
      injectTemplate("profileAttributeQueries");
      injectTemplate("profileAttributeRef");
  }

  @Override
  public void addModelReferences() {
      addWdkReference("TranscriptRecordClasses.TranscriptRecordClass", "profile_graph", "Horn::TbRNAiRNASeq"); 
        addWdkReference("GeneRecordClasses.GeneRecordClass", "table", "PhenotypeGraphs");	
      addWdkReference("TranscriptRecordClasses.TranscriptRecordClass", "question", "GeneQuestions.GenesByHighThroughputPhenotyping"); 
  }

  // second column is for documentation
  @Override
  public String[][] getPropertiesDeclaration() {
    String[][] propertiesDeclaration = {};
    return propertiesDeclaration;
  }


}
