package org.apidb.apicommon.model.report;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.gusdb.wdk.model.Utilities;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.answer.AnswerValue;
import org.gusdb.wdk.model.answer.factory.AnswerValueFactory;
import org.gusdb.wdk.model.answer.spec.AnswerSpec;
import org.gusdb.wdk.model.query.spec.QueryInstanceSpec;
import org.gusdb.wdk.model.query.spec.QueryInstanceSpecBuilder;
import org.gusdb.wdk.model.question.Question;
import org.gusdb.wdk.model.record.Field;
import org.gusdb.wdk.model.record.FieldScope;
import org.gusdb.wdk.model.report.Reporter;
import org.gusdb.wdk.model.report.config.StandardConfig;
import org.gusdb.wdk.model.report.util.ReporterFactory;
import org.gusdb.wdk.model.user.StepContainer;
import org.gusdb.wdk.model.user.User;

/**
 * @author xingao
 * 
 */
@Deprecated
public class RecordDumper {

    private static final Logger logger = Logger.getLogger(RecordDumper.class);

    /**
     * @param args
     */
    public static void main(String[] args) throws Exception {
        if (args.length != 6 && args.length != 8) {
            System.err.println("Invalid parameters.");
            printUsage();
            System.exit(-1);
        }
        Map<String, String> cmdArgs = new HashMap<String, String>();
        for (int i = 0; i < args.length - 1; i += 2) {
            cmdArgs.put(args[i].trim().toLowerCase(), args[i + 1].trim());
        }

        // get params
        String modelName = cmdArgs.get("-model");
        String organismArg = cmdArgs.get("-organism");
        String typeArg = cmdArgs.get("-type");
        String baseDir = cmdArgs.get("-dir");
        if (modelName == null || organismArg == null) {
            System.err.println("Missing parameters.");
            printUsage();
            System.exit(-1);
        }
        if (baseDir == null || baseDir.length() == 0) baseDir = ".";

        // TEST
        System.out.println("Initializing....");

        // construct wdkModel
        String gusHome = System.getProperty(Utilities.SYSTEM_PROPERTY_GUS_HOME);
        try (WdkModel model = WdkModel.construct(modelName, gusHome)) {

          // get type list
          String[] types = typeArg.split(",");
  
          String[] organisms = organismArg.split(",");
          for (String organism : organisms) {
              for (String type : types) {
                  dumpOrganism(model, organism.trim(), type.trim(), baseDir);
              }
          }
          System.out.println("Finished.");
        }
    }

    private static void dumpOrganism(WdkModel wdkModel, String organism,
            String type, String baseDir) throws WdkUserException, WdkModelException, IOException {
        long start = System.currentTimeMillis();

        // TEST
        logger.info("Dumping " + type + " records for " + organism + "...");

        // decide which question to use, and the name of the parameter.
        Question question = null;
        final String organismParam = "gff_organism";
        String reporterName = null;
        if (type.equalsIgnoreCase("gene")) {
            question = (Question) wdkModel.resolveReference("GeneDumpQuestions.GeneDumpQuestion");
            reporterName = "fullRecordDump";
        } else if (type.equalsIgnoreCase("sequence")) {
            question = (Question) wdkModel.resolveReference("SequenceDumpQuestions.SequenceDumpQuestion");
            reporterName = "fullRecord";
        } else {
            String camelRecordType = type.substring(0, 1).toUpperCase()
                    + type.substring(1).toLowerCase();
            question = (Question) wdkModel.resolveReference(camelRecordType
                    + "DumpQuestions." + camelRecordType + "DumpQuestion");
            if (type.equalsIgnoreCase("popset")) {
                reporterName = "fullRecordDump";
            } else {
                reporterName = "fullRecord";
            }
        }

        // get report maker attributes and tables
        Map<String, Field> fields = FieldScope.REPORT_MAKER.filter(question.getFields());
        StringBuffer sbFields = new StringBuffer();
        for (String fieldName : fields.keySet()) {
            if (sbFields.length() > 0) sbFields.append(",");
            sbFields.append(fieldName);
        }

        // make the configuration for the reporter
        Map<String, String> config = new LinkedHashMap<String, String>();
        config.put(StandardConfig.ATTACHMENT_TYPE, "text");
        config.put(StandardConfig.SELECTED_ATTRS, sbFields.toString());
        config.put(StandardConfig.INCLUDE_EMPTY_TABLES, "yes");

        // ask the question
        User user = wdkModel.getSystemUser();
        QueryInstanceSpecBuilder params = QueryInstanceSpec.builder();
        params.put(organismParam, organism);

        AnswerValue sqlAnswer = AnswerValueFactory.makeAnswer(
            AnswerSpec.builder(wdkModel)
            .setQuestionFullName(question.getFullName())
            .setQueryInstanceSpec(params)
            .buildRunnable(user, StepContainer.emptyContainer()));

        // decide the path-file name
        File dir = new File(baseDir, organism.replace(' ', '_'));
        if (!dir.exists() || !dir.isDirectory()) dir.mkdirs();
        int pos = organism.indexOf(" ");
        String fileName = organism;
        if (pos >= 0)
            fileName = organism.substring(0, 1).toLowerCase() + "_"
                    + organism.substring(pos + 1);
        fileName += "_" + type + ".txt";
        File file = new File(dir, fileName);

        // output the result
        OutputStream out = new FileOutputStream(file);
        Reporter seqReport = ReporterFactory.getReporter(sqlAnswer, reporterName, config);
        seqReport.report(out);
        out.close();

        // TEST
        System.out.println("Dump file saved at " + file.getAbsolutePath() + ".");

        long end = System.currentTimeMillis();
        System.out.println("Time spent " + ((end - start) / 1000.0)
                + " seconds.");
    }

    public static void printUsage() {
        System.out.println();
        System.out.println("Usage: wdkRecordDump -model <model_name> -organism "
                + "<organism_list> -type <record_type_list> [-dir <base_dir>]");
        System.out.println();
        System.out.println("\t\t<model_name>:\tThe name of WDK supported model");
        System.out.println("\t\t<organism_list>: a list of organism names, "
                + "delimited by a comma;");
        System.out.println("\t\t<record_type_list>: a list of record types, "
                + "current support: gene, sequence.");
        System.out.println("\t\t<base_dir>: Optional, the base directory for "
                + "the output files. If not specified, the current directory "
                + "will be used.");
        System.out.println();
    }
}
