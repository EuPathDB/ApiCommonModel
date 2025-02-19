package org.apidb.apicommon.model.datasetInjector;

import org.apidb.apicommon.datasetPresenter.DatasetInjector;

public class RNASeq extends  DatasetInjector {
  
  /*
   * getPropValues() gets the property values provided by the datasetPresenter
   * xml file. they are validated against the names provided in
   * getPropertyNames(). in the code below, the whole bundle is passed to each
   * of the templates even though a given template might not need all of them.
   * this is just convenience, rather than tailoring the list per template, it
   * is safe to pass all in, because unneeded ones will be ignored.
   */

    private String profileSetParamQueryTemplate = "rnaSeqProfileSetParamQuery";


    protected void setProfileSetParamQueryTemplate(String profileSetParamQueryTemplate) {
        this.profileSetParamQueryTemplate = profileSetParamQueryTemplate;
    }

    private String pctProfileSetParamQueryTemplate = "rnaSeqPctProfileSetParamQuery";

    protected void setPctProfileSetParamQueryTemplate(String pctProfileSetParamQueryTemplate) {
        this.pctProfileSetParamQueryTemplate = pctProfileSetParamQueryTemplate;
    } 

    private String antisenseSamplesParamQueryTemplate = "antisenseSamplesParamQuery";

    protected void setAntisenseSamplesParamQueryTemplate(String antisenseSamplesParamQueryTemplate) {
        this.antisenseSamplesParamQueryTemplate = antisenseSamplesParamQueryTemplate;
    }

    protected void setProfileSamplesHelp() {
        String profileSamplesHelp = "Transcript levels of fragments per kilobase of exon model per million mapped reads (FPKM)";

        setPropValue("profileSamplesHelp", profileSamplesHelp);
    }

    protected void setExprMetric() {
        setPropValue("exprMetric", "tpm");
    }

    protected void setGraphYAxisDescription() {
        setPropValue("graphYAxisDescription", "Transcript levels of fragments per kilobase of exon model per million mapped reads (FPKM). The percentile graph shows the ranking of expression for this gene compared to all others in this experiment.");
    }


  @Override
  public void injectTemplates() {
      setShortAttribution();

      String projectName = getPropValue("projectName");
      //String presenterId = getPropValue("presenterId");
      String datasetName = getDatasetName();
      
      // TODO - remove this condition, to add templates for specific dataset(s) 
      if (!datasetName.equals("pfal3D7_Newbold_ebi_rnaSeq_RSRC")) { 
	  return;
      }
      Boolean switchStrandsGBrowse = getPropValueAsBoolean("switchStrandsGBrowse");
      Boolean switchStrandsProfiles = getPropValueAsBoolean("switchStrandsProfiles");

      setPropValue("metadataFileSuffix","NA");
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

      if(getPropValueAsBoolean("isDESeq")) {
	  if(getPropValueAsBoolean("isStrandSpecific")) {
	      setPropValue("stranded", "Strand Specific ");
	      if (switchStrandsProfiles) {
		  setPropValue("antisense","firststrand") ;
		  setPropValue("sense","secondstrand") ;
	      } 
	      else {
		  setPropValue("sense","firststrand") ;
		  setPropValue("antisense","secondstrand") ;
	      }
		
	  }
	  injectTemplate("DESeqProfileSetParamQuery");
	    
      }
      
      if(getPropValueAsBoolean("isDEGseq")) {
	  injectTemplate("DEGseqProfileSetParamQuery");
      }
      if(!getPropValueAsBoolean("jbrowseTracksOnly")) {
	  setExprMetric();
	  //setPropValue("exprMetric", "fpkm");

	  if(getPropValue("graphYAxisDescription") == null) {
	      setGraphYAxisDescription();
	      //              setPropValue("graphYAxisDescription", "Transcript levels of fragments per kilobase of exon model per million mapped reads (FPKM). Note that Non-Unique reads are ignored in the expression graphs.  This means that the expression of duplicated genes (or gene families) might be underrepresented.  Please consult the non-unique aligned read tracks in the genome browser to determine if your gene of interest contains non-uniquely aligned reads. The percentile graph shows the ranking of expression for this gene compared to all others in this experiment.");
       //       setPropValue("graphYAxisDescription", "Transcript levels of fragments per kilobase of exon model per million mapped reads (FPKM). The percentile graph shows the ranking of expression for this gene compared to all others in this experiment.");
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
	  } else {
	      //	      setPropValue("sense","unstranded") ;

	      setPropValue("graphVisibleParts", exprMetric);
	      injectTemplate("pathwayGraphs");

	      setPropValue("graphVisibleParts", exprMetric + ",percentile");


	      String exprGraphAttr = datasetName + "_expr_graph";
	      String pctGraphAttr = datasetName + "_pct_graph";

	      String pathwayGraphAttr = datasetName + "_expr_graph_pr";

	      setPropValue("exprGraphAttr", exprGraphAttr);
	      setPropValue("pctGraphAttr", pctGraphAttr);
	      injectTemplate("rnaSeqExpressionGraphAttributes");
	      injectTemplate("rnaSeqExpressionGraphAttributesPathwayRecord");

	      injectTemplate(this.profileSetParamQueryTemplate);
	      injectTemplate("datasetUrlParamQuery");
	      injectTemplate(this.pctProfileSetParamQueryTemplate);
	      injectTemplate("profileMinMaxAttributesCategory");
	      injectTemplate("profileMinMaxAttributeRef");
	      injectTemplate("profileMinMaxAttributeQueries");

              // Add text attribute to the categories ontology
	      setPropValue("graphTextAttrName", exprGraphAttr);
	      injectTemplate("graphTextAttributeCategory");

	      setPropValue("graphTextAttrName", pctGraphAttr);
	      injectTemplate("graphTextAttributeCategory");

	      setPropValue("graphTextAttrName", pathwayGraphAttr);
	      injectTemplate("graphTextAttributeCategoryPathwayRecord");
	      injectTemplate("rnaSeqGraph");
	      if(!projectName.equals("EuPathDB")) {
                  injectTemplate("profileSampleAttributesCategory");
                  injectTemplate("profileAttributeQueries");
                  injectTemplate("profileAttributeRef");
	      }
	  }

	  if(getPropValueAsBoolean("hasMultipleSamples")) {
	      injectTemplate("rnaSeqFoldChangeQuestion");
              //              injectTemplate("rnaSeqFoldChangeCategories");
	      setPropValue("searchCategory", "searchCategory-transcriptomics-fold-change");
	      setPropValue("questionName", "GeneQuestions.GenesByRNASeq" + getDatasetName());
	      injectTemplate("internalGeneSearchCategory");
	      if(getPropValueAsBoolean("isStrandSpecific")) {
		  injectTemplate("strandSpecificGraph");
		  injectTemplate(this.antisenseSamplesParamQueryTemplate);
		  injectTemplate("rnaSeqSenseAntisenseQuestion");
		  //              injectTemplate("rnaSeqSenseAntisenseCategories");
		  setPropValue("searchCategory", "searchCategory-transcriptomics-sense-antisense");
		  setPropValue("questionName", "GeneQuestions.GenesByRNASeq" + getDatasetName() + "SenseAntisense");
		  injectTemplate("internalGeneSearchCategory");
	      }
	  }

	  injectTemplate("rnaSeqPercentileQuestion");
          //injectTemplate("rnaSeqPercentileCategories");
	  setPropValue("searchCategory", "searchCategory-transcriptomics-percentile");
	  setPropValue("questionName", "GeneQuestions.GenesByRNASeq" + getDatasetName() + "Percentile");
	  injectTemplate("internalGeneSearchCategory");

	  if(getPropValueAsBoolean("isDESeq")) {
	      injectTemplate("rnaSeqDESeqQuestion");
	      setPropValue("searchCategory", "searchCategory-transcriptomics-differential-expression");
	      setPropValue("questionName", "GeneQuestions.GenesByRNASeq" + getDatasetName() + "DESeq");
	      injectTemplate("internalGeneSearchCategory");
	  }
	  if(getPropValueAsBoolean("isDEGseq")) {
	      injectTemplate("rnaSeqDEGseqQuestion");
	      setPropValue("searchCategory", "searchCategory-transcriptomics-differential-expression-degseq");
	      setPropValue("questionName", "GeneQuestions.GenesByRNASeq" + getDatasetName() + "DEGseq");
	      injectTemplate("internalGeneSearchCategory");
	  }

	  if(getPropValueAsBoolean("includeProfileSimilarity")) {
	      injectTemplate("rnaSeqProfileSimilarityQuestion");
	      injectTemplate("rnaSeqProfileSimilarityParamQuery");
	      injectTemplate("rnaSeqProfileSimilarityTimeShiftParamQuery");
	      setPropValue("searchCategory", "searchCategory-similarity");
	      setPropValue("questionName", "GeneQuestions.GenesByRNASeq" + getDatasetName() + "ProfileSimilarity");

	      injectTemplate("internalGeneSearchCategory");
	      injectTemplate("similarityGraph");
	  }


	  if(getPropValue("graphPriorityOrderGrouping").equals("")) {
	      setPropValue("graphPriorityOrderGrouping", "1");
	  }

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
      //      setPropValue("gbrowseTrackName", getDatasetName() + "Coverage");
      //      injectTemplate("gbrowseTrackCategory");

      injectTemplate("rnaSeqCoverageTrackUnlogged");
      //      setPropValue("gbrowseTrackName", getDatasetName() + "CoverageUnlogged");
      //      injectTemplate("gbrowseTrackCategory");



      String showIntronJunctions = getPropValue("showIntronJunctions");
      setPropValue("intronSizeLimit", "0");
      if(Boolean.parseBoolean(showIntronJunctions)) {
        
          String experimentName = datasetName.replace("_rnaSeq_RSRC", "");

          //String experimentName = experimentRsrc.replaceFirst("RSRC", "");
          setPropValue("experimentName", experimentName);

          // String organismAbbrev = getPropValue("organismAbbrev");
          
          /** junctions now use unified track.  no need for sample list
          List<String> sampleNames = getSampleList();
          String subtracks = "";
          for (int i=0; i<sampleNames.size(); i++) {
              String subtrack = sampleNames.get(i);
              if (i == sampleNames.size() -1) {
                  subtracks = subtracks + "'" + subtrack + "';";
              }
              else {
                  subtracks = subtracks + "'" + subtrack + "';\n                  ";
              }
          }
          setPropValue("subtracks", subtracks);
          **/


          if(projectName.equals("HostDB")) {
              // <10% human introns greater than 11000 bp
              setPropValue("intronSizeLimit", "20000");
          }
          else {
              // this matches the refind value in unified junctions
              setPropValue("intronSizeLimit", "3000");
          }

          injectTemplate("rnaSeqJunctionsTrack");
          //          setPropValue("gbrowseTrackName", getDatasetName() + "Junctions");
          //          injectTemplate("gbrowseTrackCategory");
      }

      // String studyName = getPropValue("name");
      // setPropValue("studyName", studyName);
      setPropValue("summary", getPropValue("summary").replaceAll("\n", " "));
      injectTemplate("jbrowseRNASeqBuildProps");
  }


  @Override
  public void addModelReferences() {
      setProfileSamplesHelp();

      if(!getPropValueAsBoolean("jbrowseTracksOnly")) {

	  setPropValue("graphModule", "RNASeq");
	  /*  if(getPropValueAsBoolean("isStrandSpecific")) {
              setPropValue("graphModule", "RNASeq::StrandSpecific");
          } else {
              setPropValue("graphModule", "RNASeq::StrandNonSpecific");
	      } */
	  addWdkReference("TranscriptRecordClasses.TranscriptRecordClass", "profile_graph", getPropValue("graphModule") + getDatasetName() ); 
	  addWdkReference("GeneRecordClasses.GeneRecordClass", "table", "ExpressionGraphs");
	  addWdkReference("GeneRecordClasses.GeneRecordClass", "table", "TranscriptionSummary");
          if(getPropValueAsBoolean("hasMultipleSamples")) {
              addWdkReference("TranscriptRecordClasses.TranscriptRecordClass", "question",
                              "GeneQuestions.GenesByRNASeq" + getDatasetName());

	      if(getPropValueAsBoolean("isStrandSpecific")) {
		  addWdkReference("TranscriptRecordClasses.TranscriptRecordClass", "question",
				  "GeneQuestions.GenesByRNASeq" + getDatasetName() + "SenseAntisense");
	      }
          }

	  if(getPropValueAsBoolean("includeProfileSimilarity")) {
	      addWdkReference("TranscriptRecordClasses.TranscriptRecordClass", "question", "GeneQuestions.GenesByRNASeq" +getDatasetName() +"ProfileSimilarity");
	  }
	  if (getPropValueAsBoolean("isDESeq")) {
	      addWdkReference("TranscriptRecordClasses.TranscriptRecordClass", "question", "GeneQuestions.GenesByRNASeq" +getDatasetName() +"DESeq");
	  }
	  if (getPropValueAsBoolean("isDEGseq")) {
	      addWdkReference("TranscriptRecordClasses.TranscriptRecordClass", "question", "GeneQuestions.GenesByRNASeq" +getDatasetName() +"DEGseq");
	  }

          addWdkReference("TranscriptRecordClasses.TranscriptRecordClass", "question",
                          "GeneQuestions.GenesByRNASeq" + getDatasetName() + "Percentile");

          if (getPropValueAsBoolean("showIntronJunctions") && getPropValueAsBoolean("includeInUnifiedJunctions")){
	      addWdkReference("TranscriptRecordClasses.TranscriptRecordClass", "question", "GeneQuestions.GenesByIntronJunctions");
	   }


          // this question does not display on Host or Trich
          String projectName = getPropValue("projectName");
          if (getPropValueAsBoolean("showIntronJunctions") && !projectName.equals("HostDB") && !projectName.equals("TrichDB") && !projectName.equals("TriTrypDB"))
            {
          	    addWdkReference("DynSpanRecordClasses.DynSpanRecordClass", "question", "SpanQuestions.IntronJunctionDynamicSearch");
            }

      }
  }
  
    // declare properties required beyond those inherited from the datasetPresenter
    // second column is for documentation
    @Override
    public String[][] getPropertiesDeclaration() {
	String [][] declaration = {
                                 {"isEuPathDBSite", ""},
                                 {"switchStrandsProfiles", ""},
                                 {"switchStrandsGBrowse", ""},
                                 {"graphType", "one of bar or line"},
                                 {"graphColor", ""},
                                 {"graphSampleLabels", ""},
                                 {"graphBottomMarginSize", ""},
                                 {"showIntronJunctions", ""},
                                 {"includeInUnifiedJunctions", ""},
                                 {"jbrowseTracksOnly", ""},
                                 {"hasMultipleSamples", ""},
                                 {"graphXAxisSamplesDescription", "will show up on the gene record page next to the graph"},
                                 {"graphPriorityOrderGrouping", "numeric grouping / ordering of graphs on the gene record page"},
                                 {"optionalQuestionDescription", "html text to be appended to the descriptions of all questions"},
                                 {"graphForceXLabelsHorizontal", "should the x axis labels be always horiz"},
				 {"isDESeq", ""},
				 {"isDEGseq",""},
                                 {"includeProfileSimilarity", "inject Profile Similarity Question"},
                                 {"profileTimeShift", "for Profile Similarity Question"},
      };

    return declaration;
  }
}
