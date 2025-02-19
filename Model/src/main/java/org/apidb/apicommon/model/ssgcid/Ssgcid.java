package org.apidb.apicommon.model.ssgcid;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;

import org.gusdb.fgputil.db.platform.SupportedPlatform;
import org.gusdb.fgputil.db.pool.ConnectionPoolConfig;
import org.gusdb.fgputil.db.pool.DatabaseInstance;
import org.gusdb.fgputil.db.pool.SimpleDbConfig;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

// requires Jackson http://wiki.fasterxml.com/JacksonDownload
// javac -classpath jackson-core-2.1.4.jar:jackson-databind-2.1.4.jar:commons-lang3-3.1.jar NetClientGet.java
// jackson annotations JsonAutoDetect is required at runtime
// java -classpath .:jackson-core-2.1.4.jar:jackson-databind-2.1.4.jar:jackson-annotations-2.1.4.jar:commons-lang3-3.1.jar NetClientGet

public class Ssgcid {

  public static void createTuningTable(Connection dbc, String suffix) throws SQLException {
    String createSql = new String("create table ssgcid" + suffix + "(\n" +
        "target             varchar(13),\n" +
        "eupathdb           varchar(32),\n" +
        "status             varchar(30),\n" +
        "selection_criteria varchar(81),\n" +
        "has_clone          varchar(5),\n" +
        "has_protein        varchar(5)\n" + ")");
    try (Statement createStmt = dbc.createStatement()) {
      createStmt.executeUpdate(createSql.toString());
    }
  }

  public static void main(String[] args) throws Exception {

    if (args.length != 1) {
      System.err.println("usage: java Ssgcid <suffix>");
      System.exit(1);
    }
    String suffix = args[0];

    String instance = "";
    String schema = "";
    String username = "";
    String password = "";

    BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
    instance = in.readLine();
    schema = in.readLine();
    username = in.readLine();
    password = in.readLine();

    String dbname = instance.split(";")[0].split("=")[1];
    String host = instance.split(";")[1].split("=")[1];

    ConnectionPoolConfig config = SimpleDbConfig.create(SupportedPlatform.POSTGRESQL,
        "jdbc:postgresql://" + host + "/" + dbname, username, password);
    try (DatabaseInstance db = new DatabaseInstance(config);
         Connection dbc = db.getDataSource().getConnection()) {

      dbc.createStatement().execute("SET search_path TO "+ schema);
      createTuningTable(dbc, suffix);

      String insertSql = new String("insert into ssgcid" + suffix +
          " (target, eupathdb, status, selection_criteria, has_clone, has_protein)" +
          "values (?, ?, ?, ?, ?, ?)");
      PreparedStatement insertStatement = dbc.prepareStatement(insertSql.toString());

      // URL url = new URL("http://www.ssgcid.org/eupath/list_all?format=json");
      // ObjectMapper mapper = new ObjectMapper();
      // ArrayNode root = (ArrayNode) mapper.readTree(url);

      String filename = new String("/tmp/ssgcid" + suffix + ".json");
      // Runtime.getRuntime().exec("wget http://www.ssgcid.org/eupath/list_all?format=json --output-document " + filename);
      File file = new File(filename);
      ObjectMapper mapper = new ObjectMapper();
      ArrayNode root = (ArrayNode) mapper.readTree(file);

      Iterator<JsonNode> jsonNodeIterator = root.elements();
      while (jsonNodeIterator.hasNext()) {
        JsonNode jsonNode = jsonNodeIterator.next();
        // int identifier = jsonNode.get("pk").intValue();
        JsonNode fieldsNode = jsonNode.get("fields");

        insertStatement.setString(1, jsonNode.get("pk").textValue());
        insertStatement.setString(2, fieldsNode.get("eupathdb").textValue());
        insertStatement.setString(3, fieldsNode.get("status").textValue());
        insertStatement.setString(4, fieldsNode.get("selection_criteria").textValue());
        insertStatement.setString(5, fieldsNode.get("has_clone").booleanValue() ? "true" : "false");
        insertStatement.setString(6, fieldsNode.get("has_protein").booleanValue() ? "true" : "false");
        insertStatement.executeUpdate();
      }
    }
  }

}
