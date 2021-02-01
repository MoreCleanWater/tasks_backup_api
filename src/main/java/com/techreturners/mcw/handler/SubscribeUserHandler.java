package com.techreturners.mcw.handler;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.techreturners.mcw.learning.Handler;

public class SubscribeUserHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

	private static final Logger LOG = LogManager.getLogger(Handler.class);
	private Connection connection = null;
	private PreparedStatement statement = null;
	private ResultSet resultset = null;
	APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();

	@Override
	public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent request, Context context) {
		String user_id = request.getPathParameters().get("userId");

		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
			connection = DriverManager
					.getConnection(String.format("jdbc:mysql://%s/%s?user=%s&password=%s", System.getenv("DB_HOST"),
							System.getenv("DB_NAME"), System.getenv("DB_USER"), System.getenv("DB_PASSWORD")));
		    String query = "update user set is_subscriber= ? where user_id = ?";
            statement = connection.prepareStatement(query);
			statement.setBoolean(1, true);
			statement.setString(2, user_id);
            statement.executeUpdate();
			response.setStatusCode(200);

			LOG.debug("subscribing User = " + user_id);
			LOG.info("User info=", user_id);

			response.setBody("User is subscribed successfully");
		} catch (Exception e) {
			LOG.error("Unable to open database connection in Subscribe User", e);
		} finally {
			closeConnection();
		}
		return response;
	}

	private void closeConnection() {
		try {
			if (resultset != null) {
				resultset.close();
			}
			if (statement != null) {
				statement.close();
			}
			if (connection != null) {
				connection.close();
			}
		} catch (Exception ex) {
			LOG.error("Unable to close database connection in User subscribe", ex.getMessage());
		}
	}
}
