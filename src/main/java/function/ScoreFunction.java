package function;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.mysql.cj.jdbc.Driver;

public class ScoreFunction implements RequestHandler<APIGatewayProxyRequestEvent, Object> {

    private static final String VALID_RESPONSE = "200 OK";
    private static final String URL = "jdbc:mysql://rds-mysql-highscores.cnwjcorvpmbi.us-east-1.rds.amazonaws.com:3306/highscoresdb";
    private static final String USER = "";
    private static final String PASS = "";

    private static final String SQL_INSERT = "INSERT INTO highscores (username, score) VALUES (?,?)";
    private static final String SQL_SELECT = "SELECT * FROM highscores ORDER BY SCORE DESC LIMIT 10";

    public Object handleRequest(APIGatewayProxyRequestEvent input, Context context) {
        LambdaLogger logger = context.getLogger();
        if(input.getHttpMethod().equals("POST")) {
            Map<String, String> pathParameters = input.getQueryStringParameters();
            String score = pathParameters.get("score");
            logger.log("Getting score... " + score);
            String username = pathParameters.get("username");
            logger.log("Getting username... " + username);
            try {
                logger.log("Creating statement... ");
                createInsertStatement(username, score, logger);
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return VALID_RESPONSE;
        } else {
            try {
                return getTopScores(logger);
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }
        return null;
    }

    private static Connection getConnection(LambdaLogger logger) {
        try {
            logger.log("registering driver... ");
            DriverManager.registerDriver(new Driver());
            logger.log("returning DB connection... ");
            return DriverManager.getConnection(URL, USER, PASS);
        } catch (SQLException ex) {
            throw new RuntimeException("Error connecting to the database", ex);
        }
    }

    private void createInsertStatement(String user, String score, LambdaLogger logger) throws SQLException {
        PreparedStatement preparedStatement = getConnection(logger).prepareStatement(SQL_INSERT);
        logger.log("preparing statement... ");
        preparedStatement.setString(1, user);
        preparedStatement.setString(2, score);
        preparedStatement.execute();
    }

    private List<Player> getTopScores(LambdaLogger logger) throws SQLException {
        Statement statement = getConnection(logger).createStatement();
        ResultSet resultSet = statement.executeQuery(SQL_SELECT);
        return convertResultsToList(resultSet);
    }

    private List<Player> convertResultsToList(ResultSet resultSet) throws SQLException {
        List<Player> players = new ArrayList<>();
        Map<String, String> resultMap = new HashMap<>();
        while (resultSet.next()) {
            String usernameColumn = resultSet.getString("username");
            String scoreColumn = resultSet.getString("score");
            players.add(new Player(usernameColumn, scoreColumn));
        }
        return players;
    }




}