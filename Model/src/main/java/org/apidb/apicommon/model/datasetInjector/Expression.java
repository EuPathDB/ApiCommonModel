package org.apidb.apicommon.model.datasetInjector;

import org.apidb.apicommon.datasetPresenter.DatasetInjector;

public abstract class Expression extends DatasetInjector {

    protected String dataType;

    protected void setDataType(String dataType) {
        this.dataType = dataType;
        setPropValue("dataType", dataType);
    }

    protected String getDataType() {
        return this.dataType;
    }

    protected abstract void setDataType();
    protected abstract void setExprGraphVisiblePart();
    protected abstract void setGraphModule();
    protected abstract void setExprPlotPartModule();

    protected abstract void setGraphYAxisDescription();
    protected abstract void setProfileSamplesHelp();

    protected String getSearchCategoryType() {
        //	System.out.println("dataset="+getDatasetName());
	String lcDatasetClassCategory=getPropValue("datasetClassCategory").toLowerCase();
	if (lcDatasetClassCategory.equals("proteomics")) {
            //  System.out.println(lcDatasetClassCategory);
	    return lcDatasetClassCategory;
	}
	return "transcriptomics";
    }
	
    protected void setProfileSetFilterPattern() {
        setPropValue("profileSetFilterPattern", "%");
    }


    protected void setProteinCodingProps() {
        setPropValue("defaultProteinCodingOnly", "yes");
        setPropValue("proteinCodingParamVisible", "true");
    }

    protected void setCleanGraphModule() {
        String cleanGraphModule = getPropValue("graphModule").replace(":","");
        setPropValue("cleanGraphModule", cleanGraphModule);
    }

    /***
     *
     *  used by the model (0=false, 1=true)
     *
     */
    protected void setIsLogged() {
        setPropValue("isLogged", "1"); 
    }


    /***
     *   list of key value pairs
     *      key1=value1;key2=value2
     *
     *    @rv String like 'key1', 'value1', 'key2', 'value2'
     */
    protected static String makeDecodeMappingStrings(String namesMap) {
        String decode = "";

        if(namesMap.equals("")) {
            return decode;
        }

        String[] pairs = namesMap.split(";");

        for(int i = 0; i < pairs.length; i++) {
            String[] profileMap = pairs[i].split("=");

            decode = decode + "'" + profileMap[0] + "', '" + profileMap[1] + "',\n";
        }

        return decode;
    }

    @Override
    public void addModelReferences() {

      String className = this.getClass().getSimpleName();
      setPropValue("templateInjectorClassName", className);

      setGraphModule();
      setProfileSamplesHelp();


      addWdkReference("TranscriptRecordClasses.TranscriptRecordClass", "profile_graph", getPropValue("graphModule") + getDatasetName() ); 
    }

    @Override
    public void injectTemplates() {



        // perl packages disallow some characters in the package name... use this to name the graphs
        setGraphDatasetName();
	setShortAttribution();
        setOrganismAbbrevFromDatasetName();

        String projectName = getPropValue("projectName");



        if(getPropValueAsBoolean("isEuPathDBSite")) {
            setPropValue("includeProjects", projectName + ",EuPathDB,UniDB");
        } else {
            setPropValue("includeProjects", projectName + ",UniDB");
        }

        setPropValue("includeProjectsExcludeEuPathDB", projectName + ",UniDB");


        setIsLogged();
        setDataType();
        setExprGraphVisiblePart();
        setGraphModule();
        setCleanGraphModule();
        setExprPlotPartModule();
        setGraphYAxisDescription();
        setProteinCodingProps();


        String lcDataType = getPropValue("dataType").toLowerCase();

        injectTemplate("datasetCategory");

        setPropValue("graphTextAttrName", "exprGraphAttr" + getDatasetName() + "_expr_graph");

        injectTemplate("expressionGraphAttributesExpression");
        injectTemplate("graphTextAttributeCategory");

        setProfileSamplesHelp();

        // TODO:  add this back !!
        // injectTemplate("profileSampleAttributesCategory");
        // injectTemplate("profileAttributeQueries");
        // injectTemplate("profileAttributeRef");

        // injectTemplate("profileMinMaxAttributesCategory");
        // injectTemplate("profileMinMaxAttributeRef");
        // injectTemplate("profileMinMaxAttributeQueries");

        if(getPropValueAsBoolean("hasPercentileData")) {
            setPropValue("graphTextAttrName", "pctGraphAttr" + getDatasetName() + "_pct_graph");
            injectTemplate("expressionGraphAttributesPercentile");
            injectTemplate("graphTextAttributeCategory");
        }


        setPropValue("graphTextAttrName", "exprGraphAttr" + getDatasetName() + "_expr_graph_pr");
        injectTemplate("expressionGraphAttributesPathwayRecord");
        injectTemplate("graphTextAttributeCategoryPathwayRecord");


        String exprGraphVp = getPropValue("exprGraphVisiblePart");

        setPropValue("graphVisibleParts", exprGraphVp);
        injectTemplate("pathwayGraphs");

        if(getPropValueAsBoolean("hasPercentileData")) {        
            setPropValue("graphVisibleParts", exprGraphVp + ",percentile");
        } else {
            setPropValue("graphVisibleParts", exprGraphVp);
        }

        // these are universal for injected expression experiments
        setPropValue("graphGenePageSection", "expression");

        if(getPropValue("graphPriorityOrderGrouping").equals("")) {
            setPropValue("graphPriorityOrderGrouping", "1");
        }
        
        // need to make sure there are no single quotes in the descrip
        String datasetDescrip = getPropValue("datasetDescrip");
        setPropValue("datasetDescrip", datasetDescrip.replace("'", ""));

        setPropValue("isGraphCustom", "false");

        injectTemplate(lcDataType + "GraphDescriptions");

        injectTemplate("datasetExampleGraphDescriptions");

        String excludeProfileSets = getPropValue("excludedProfileSets");

        String excludedProfileSetsList = "'INTERNAL'";
        if(!excludeProfileSets.equals("")) {
            String[] excludedProfileSetsArr = excludeProfileSets.split(";");

            for(int i = 0; i < excludedProfileSetsArr.length; i++) {
                excludedProfileSetsList = excludedProfileSetsList + ", '" + excludedProfileSetsArr[i] + "'";
            }
        }

        setPropValue("excludedProfileSetsList", excludedProfileSetsList);
    }

    @Override
    public String[][] getPropertiesDeclaration() {
        String [][] declaration = {
                                   {"hasPageData", ""},
                                   {"isEuPathDBSite", ""},
                                   {"graphSampleLabels", "OPTIONAL Prop...provide if you want to override the sample names in the db"},
                                   {"graphColors", "semicolon sep list of colors"},
                                   {"excludedProfileSets", "OPTIONAL PROP... extra profile sets are loaded which we need to exclude from the graphs and questions"},
                                   {"graphBottomMargin", "Optionally override the default bottom margin size"},
                                   {"graphType", "one of bar or line"},
                                   {"graphForceXLabelsHorizontal", "true/false.  NOTE:  Only valid when graphType is bar"},
                                   {"optionalQuestionDescription", "This text will be appended to fold change and percentile questions"},
                                   {"graphXAxisSamplesDescription", "will show up on the gene record page next to the graph"},
                                   {"graphPriorityOrderGrouping", "numeric grouping / ordering of graphs on the gene record page"},
                                   {"linePlotXAxisLabel", "Sets the x-axis label for line graphs that don't override the xaxis tick mark labels."},
                                   {"profileSetNameMap", "Optionally replace profileset names"}
        };

        
        return declaration;
  }
}

