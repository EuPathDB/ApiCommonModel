package org.apidb.apicommon.model.report;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.BaseCLI;
import org.gusdb.wdk.model.Utilities;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.answer.AnswerValue;
import org.gusdb.wdk.model.answer.factory.AnswerValueFactory;
import org.gusdb.wdk.model.answer.spec.AnswerSpec;
import org.gusdb.wdk.model.query.Column;
import org.gusdb.wdk.model.query.Query;
import org.gusdb.wdk.model.query.QuerySet;
import org.gusdb.wdk.model.query.SqlQuery;
import org.gusdb.wdk.model.question.Question;
import org.gusdb.wdk.model.question.QuestionSet;
import org.gusdb.wdk.model.record.Field;
import org.gusdb.wdk.model.record.FieldScope;
import org.gusdb.wdk.model.record.RecordClass;
import org.gusdb.wdk.model.report.Reporter;
import org.gusdb.wdk.model.report.config.StandardConfig;
import org.gusdb.wdk.model.report.reporter.FullRecordReporter;
import org.gusdb.wdk.model.report.util.TableCache;
import org.gusdb.wdk.model.user.StepContainer;
import org.gusdb.wdk.model.user.User;

/**
 * @author xingao
 * 
 *         This command create detail file for the download site from the detail
 *         table.
 */
public class FullRecordFileCreator extends BaseCLI {

    private static final String ARG_SQL_FILE = "sqlFile";
    private static final String ARG_RECORD = "record";
    private static final String ARG_CACHE_TABLE = "cacheTable";
    private static final String ARG_DUMP_FILE = "dumpFile";

    private static final Logger logger = Logger
            .getLogger(FullRecordFileCreator.class);

    public static void main(String[] args) throws Exception {
        String cmdName = System.getProperty("cmdName");
        if (cmdName == null)
            cmdName = FullRecordFileCreator.class.getName();
        FullRecordFileCreator writer = new FullRecordFileCreator(cmdName,
                "Create the Dump File from dump table");
        try {
            writer.invoke(args);
        } finally {
            System.exit(0);
        }
    }

    /**
     * @param command
     * @param description
     */
    protected FullRecordFileCreator(String command, String description) {
        super(command, description);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.fgputil.BaseCLI#declareOptions()
     */
    @Override
    protected void declareOptions() {
        addSingleValueOption(ARG_PROJECT_ID, true, null, "The ProjectId, which"
                + " should match the directory name under $GUS_HOME, where "
                + "model-config.xml is stored.");

        addSingleValueOption(ARG_SQL_FILE, true, null, "The file that contains"
                + " an sql statement that returns the primary key columns of the records");

        addSingleValueOption(ARG_RECORD, true, null, "The full name of the "
                + "record class to be dumped.");

        addSingleValueOption(ARG_CACHE_TABLE, true, null, "The name of the "
                + "cache table where the cached results are stored. ");

        addSingleValueOption(ARG_DUMP_FILE, false, null, "The name of the"
                + " output dump file. If not supplied, the dump_table name "
                + " will be used with a '.txt' extension, and saved at the "
                + " current location.");
    }

    @Override
    public void execute() throws Exception {
        long start = System.currentTimeMillis();

        String projectId = (String) getOptionValue(ARG_PROJECT_ID);
        String sqlFile = (String) getOptionValue(ARG_SQL_FILE);
        String recordClassName = (String) getOptionValue(ARG_RECORD);
        String cacheTable = (String) getOptionValue(ARG_CACHE_TABLE);
        String dumpFile = (String) getOptionValue(ARG_DUMP_FILE);

        String gusHome = System.getProperty(Utilities.SYSTEM_PROPERTY_GUS_HOME);
        try (WdkModel wdkModel = WdkModel.construct(projectId, gusHome)) {

          String idSql = loadIdSql(sqlFile);
          RecordClass recordClass = wdkModel.getRecordClassByFullName(recordClassName).orElseThrow(
              () -> new WdkModelException("No record class exists with name '" + recordClassName + "'."));
  
          if (cacheTable == null)
              cacheTable = "wdk" + recordClass.getDisplayName() + "Dump";
          if (dumpFile == null)
              dumpFile = cacheTable + ".txt";
  
          User user = wdkModel.getSystemUser();
          Question question = createQuestion(wdkModel, projectId, recordClass, idSql);
          AnswerValue answerValue = AnswerValueFactory.makeAnswer(
              AnswerSpec.builder(wdkModel).setQuestionFullName(question.getFullName())
                .buildRunnable(user, StepContainer.emptyContainer()));
  
          OutputStream out = new FileOutputStream(dumpFile);
          Reporter reporter = createReporter(answerValue, cacheTable);
          reporter.report(out);
          out.close();
  
          long end = System.currentTimeMillis();
          logger.info("full record dump took " + ((end - start) / 1000.0)
                  + " seconds");
        }
    }

    private String loadIdSql(String sqlFile) throws IOException {
        File file = new File(sqlFile);
        StringBuffer sql = new StringBuffer();
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line;
        while ((line = reader.readLine()) != null) {
            sql.append(line).append("\n");
        }
        reader.close();
        String idSql = sql.toString().trim();
        if (idSql.endsWith(";"))
            idSql = idSql.substring(0, idSql.length() - 1);
        return idSql;
    }

    private Question createQuestion(WdkModel wdkModel, String projectId, RecordClass recordClass,
            String idSql) throws WdkModelException {
        String name = recordClass.getFullName().replaceAll("\\W", "_");
        QuestionSet questionSet = wdkModel.getQuestionSet(Utilities.INTERNAL_QUESTION_SET).get();
        Query query = createQuery(wdkModel, recordClass, idSql);
        Question question = new Question();
        question.setName(name + "_dump");
        question.setRecordClass(recordClass);
        question.setQueryRef(query.getFullName());
        question.excludeResources(projectId);
        question.resolveReferences(wdkModel);
        questionSet.addQuestion(question);
        return question;
    }

    private SqlQuery createQuery(WdkModel wdkModel, RecordClass recordClass, String idSql)
            throws WdkModelException {
        String name = recordClass.getFullName().replaceAll("\\W", "_");
        QuerySet querySet = wdkModel.getQuerySet(Utilities.INTERNAL_QUERY_SET);
        SqlQuery query = new SqlQuery();
        query.setName(name + "_dump");
        query.setIsCacheable(false);
        query.setSql(idSql);
        querySet.addQuery(query);
        String[] columnNames = recordClass.getPrimaryKeyDefinition().getColumnRefs();
        Column[] columns = new Column[columnNames.length];
        for (int i = 0; i < columns.length; i++) {
            Column column = new Column();
            column.setName(columnNames[i]);
            column.setQuery(query);
            query.addColumn(column);
        }
        query.resolveReferences(wdkModel);
        return query;
    }

    private Reporter createReporter(AnswerValue answerValue, String cacheTable)
            throws WdkUserException, WdkModelException {
        Question question = answerValue.getQuestion();
        Map<String, Field> fields = FieldScope.REPORT_MAKER.filter(question.getFields());
        StringBuffer sbFields = new StringBuffer();
        for (String fieldName : fields.keySet()) {
            if (sbFields.length() > 0) {
              sbFields.append(",");
            }
            sbFields.append(fieldName);
        }

        Map<String, String> properties = new LinkedHashMap<String, String>();
        properties.put(TableCache.PROPERTY_TABLE_CACHE, cacheTable);

        Map<String, String> config = new LinkedHashMap<String, String>();
        config.put(StandardConfig.ATTACHMENT_TYPE, "text");
        config.put(StandardConfig.SELECTED_FIELDS, sbFields.toString());
        config.put(StandardConfig.INCLUDE_EMPTY_TABLES, "yes");

        return new FullRecordReporter()
          .setProperties(() -> properties)
          .setAnswerValue(answerValue)
          .configure(config);
    }
}
