package org.apidb.apicommon.model.datasetInjector.custom.FungiDB;

import org.apidb.apicommon.model.datasetInjector.RNASeqMetaCycle;

public class RNASeqMetaCycle_ncraOR74A_Hurley extends RNASeqMetaCycle {

  // NOTE: override from RNASeq module, for injectTemplates, as SenseAntisense Q 
  // is to be removed for bld-62. If/when that Q is to be added back, custom injector
  // will NOT be needed.

    private String profileSetParamQueryTemplate = "rnaSeqProfileSetParamQuery";


    @Override
    protected void setProfileSetParamQueryTemplate(String profileSetParamQueryTemplate) {
        this.profileSetParamQueryTemplate = profileSetParamQueryTemplate;
    }

    private String pctProfileSetParamQueryTemplate = "rnaSeqPctProfileSetParamQuery";

    @Override
    protected void setPctProfileSetParamQueryTemplate(String pctProfileSetParamQueryTemplate) {
        this.pctProfileSetParamQueryTemplate = pctProfileSetParamQueryTemplate;
    } 

  @Override
  public void injectTemplates() { }
  public void TODO_injectTemplates() {
      setShortAttribution();

      String projectName = getPropValue("projectName");
      String datasetName = getDatasetName();
      
      Boolean switchStrandsGBrowse = getPropValueAsBoolean("switchStrandsGBrowse");
      Boolean switchStrandsProfiles = getPropValueAsBoolean("switchStrandsProfiles");

      setPropValue("metadataFileSuffix","");
      setOrganismAbbrevFromDatasetName();


      if(getPropValueAsBoolean("isEuPathDBSite")) {
          setPropValue("includeProjects", projectName + ",EuPathDB,UniDB");
      } else {
          setPropValue("includeProjects", projectName + ",UniDB");
      }
      setPropValue("includeProjectsExcludeEuPathDB", projectName + ",UniDB");

      setPropValue("graphModule", "RNASeq");
      setPropValue("stranded", "");

      String datasetShortDisplayName = getPropValue("datasetShortDisplayName");
      if (datasetShortDisplayName == null || datasetShortDisplayName.equals("")) {
        throw new RuntimeException(datasetName + " datasetShortDisplayName cannot be null");
      }

      injectTemplate("datasetCategory");
      
      if(!getPropValueAsBoolean("jbrowseTracksOnly")) {
	  setExprMetric();

	  if(getPropValue("graphYAxisDescription") == null) {
	      setGraphYAxisDescription();
	  }

	  String exprMetric = getPropValue("exprMetric");

	  if(getPropValueAsBoolean("isStrandSpecific")) {
	      setPropValue("stranded", "Strand Specific ");
	      if (switchStrandsGBrowse) {
		  setPropValue("metadataFileSuffix","_alt");
	      } 

	      if (switchStrandsProfiles) {
		  setPropValue("antisense","firststrand") ;
		  setPropValue("sense","secondstrand") ;
	      } else { 
		  setPropValue("sense","firststrand") ;
		  setPropValue("antisense","secondstrand") ;
	      }

	      setPropValue("graphVisibleParts", exprMetric + "_sense");
	      injectTemplate("pathwayGraphs");

	      injectTemplate("profileSampleAttributesCategory");
	      injectTemplate("profileAttributeQueriesStrandSpecificRNASeq");
	      injectTemplate("profileAttributeRef");

	      injectTemplate("profileMinMaxAttributesRnaSenseCategory");
	      injectTemplate("profileMinMaxAttributeRnaSenseRef");
	      injectTemplate("profileMinMaxAttributeRnaSenseQueries");

	      injectTemplate("profileMinMaxAttributesRnaAntisenseCategory");
	      injectTemplate("profileMinMaxAttributeRnaAntisenseRef");
	      injectTemplate("profileMinMaxAttributeRnaAntisenseQueries");

	      String senseExprGraphAttr = datasetName + "_sense_expr_graph";
	      String antisenseExprGraphAttr = datasetName + "_antisense_expr_graph";
	      String Both_strandsExprGraphAttr = datasetName + "_Both_strands_expr_graph";

	      String sensePctGraphAttr = datasetName + "_sense_pct_graph";
	      String antisensePctGraphAttr = datasetName + "_antisense_pct_graph";

	      String pathwayGraphAttr = datasetName + "_ss_expr_graph_pr";

              // These are used for the question
	      setPropValue("exprGraphAttr", senseExprGraphAttr + "," + antisenseExprGraphAttr);
	      setPropValue("pctGraphAttr", sensePctGraphAttr + "," + antisensePctGraphAttr);

	      injectTemplate("rnaSeqSsExpressionGraphAttributes");
	      injectTemplate("rnaSeqSsExpressionGraphAttributesPathwayRecord");

              // Inject the text attributes into the ontology for the results summary page
	      setPropValue("graphTextAttrName", senseExprGraphAttr);
	      injectTemplate("graphTextAttributeCategory");
	      setPropValue("graphTextAttrName", antisenseExprGraphAttr);
	      injectTemplate("graphTextAttributeCategory");

	      setPropValue("graphTextAttrName", Both_strandsExprGraphAttr);
	      injectTemplate("graphTextAttributeCategory");
	      
	      setPropValue("graphTextAttrName", sensePctGraphAttr);
	      injectTemplate("graphTextAttributeCategory");

	      setPropValue("graphTextAttrName", antisensePctGraphAttr);
	      injectTemplate("graphTextAttributeCategory");

	      setPropValue("graphTextAttrName", pathwayGraphAttr);
	      injectTemplate("graphTextAttributeCategoryPathwayRecord");


	      injectTemplate(this.profileSetParamQueryTemplate);
	      injectTemplate(this.pctProfileSetParamQueryTemplate);
	      injectTemplate("datasetUrlParamQuery");

	      injectTemplate("rnaSeqGraph");
	  }

	  if(getPropValueAsBoolean("hasMultipleSamples")) {
	      injectTemplate("rnaSeqFoldChangeQuestion");
	      setPropValue("searchCategory", "searchCategory-transcriptomics-fold-change");
	      setPropValue("questionName", "GeneQuestions.GenesByRNASeq" + getDatasetName());
	      injectTemplate("internalGeneSearchCategory");
	      if(getPropValueAsBoolean("isStrandSpecific")) {
		  injectTemplate("strandSpecificGraph");
	      }
	  }

	  injectTemplate("rnaSeqPercentileQuestion");
	  setPropValue("searchCategory", "searchCategory-transcriptomics-percentile");
	  setPropValue("questionName", "GeneQuestions.GenesByRNASeq" + getDatasetName() + "Percentile");
	  injectTemplate("internalGeneSearchCategory");

	  // need to make sure there are no single quotes in the descrip
	  String datasetDescrip = getPropValue("datasetDescrip");
	  setPropValue("datasetDescrip", datasetDescrip.replace("'", ""));
  
	  setPropValue("isGraphCustom", "false");
	  injectTemplate("genePageGraphDescriptions");
	  injectTemplate("transcriptionSummaryGraph");
	  injectTemplate("datasetExampleGraphDescriptions");
      }

      // remove ':' which is gbrowse category separator.
      String organismAbbrevDisplay = getPropValue("organismAbbrevDisplay");
      setPropValue("organismAbbrevDisplay", organismAbbrevDisplay.replace(":", ""));

      injectTemplate("rnaSeqCoverageTrack");
      injectTemplate("rnaSeqCoverageTrackUnlogged");


      String showIntronJunctions = getPropValue("showIntronJunctions");
      setPropValue("intronSizeLimit", "0");
      if(Boolean.parseBoolean(showIntronJunctions)) {
        
          String experimentName = datasetName.replace("_rnaSeq_RSRC", "");
          setPropValue("experimentName", experimentName);

	  // this matches the refind value in unified junctions
	  setPropValue("intronSizeLimit", "3000");
          injectTemplate("rnaSeqJunctionsTrack");
      }
      String studyName = getPropValue("name");
      setPropValue("studyName", studyName);



      // the MetaCycle part
      injectTemplate("metaCycleQuestion");

      setPropValue("searchCategory", "searchCategory-transcriptomics-metacycle");
      setPropValue("questionName", "GeneQuestions.GenesByMetaCycle" + getDatasetName());

      injectTemplate("internalGeneSearchCategory");
  }


  @Override
  public void addModelReferences() {
      setProfileSamplesHelp();

      setPropValue("graphModule", "RNASeq");

      addWdkReference("TranscriptRecordClasses.TranscriptRecordClass", "profile_graph", getPropValue("graphModule") + getDatasetName() ); 
      addWdkReference("GeneRecordClasses.GeneRecordClass", "table", "ExpressionGraphs");
      addWdkReference("GeneRecordClasses.GeneRecordClass", "table", "TranscriptionSummary");
      if(getPropValueAsBoolean("hasMultipleSamples")) {
	  addWdkReference("TranscriptRecordClasses.TranscriptRecordClass", "question",
			  "GeneQuestions.GenesByRNASeq" + getDatasetName());
      }
      addWdkReference("TranscriptRecordClasses.TranscriptRecordClass", "question",
		      "GeneQuestions.GenesByRNASeq" + getDatasetName() + "Percentile");

      addWdkReference("TranscriptRecordClasses.TranscriptRecordClass", "question", "GeneQuestions.GenesByMetaCycle" + getDatasetName()); 

      }


}
