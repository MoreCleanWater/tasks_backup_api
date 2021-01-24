package com.techreturners.mcw.handler;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.techreturners.mcw.model.User;

public class UserListHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

	private static final Logger LOG = LogManager.getLogger(Handler.class);
	private Connection connection = null;
	private PreparedStatement statement = null;
	private ResultSet resultset = null;

	@Override
	public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent request, Context context) {

		List<User> users = new ArrayList<>();
		

		try {

			Class.forName("com.mysql.cj.jdbc.Driver");
			//connection = DriverManager.getConnection(
					//"jdbc:mysql://morecleanwaterdb.cuhoqx2nwsa7.eu-west-2.rds.amazonaws.com/morecleanwaterpro?user=root&password=Bis0786!");
			connection = DriverManager.getConnection(String.format("jdbc:mysql://%s/%s?user=%s&password=%s",
					System.getenv("DB_HOST"),
					System.getenv("DB_NAME"),
					System.getenv("DB_USER"),
					System.getenv("DB_PASSWORD")));

			statement = connection.prepareStatement("Select * from user");
		    resultset = statement.executeQuery();
		
			while (resultset.next()) {
				User user = new User(resultset.getLong("user_id"),resultset.getInt("postcode_id"), resultset.getString("first_name"),
						resultset.getString("last_name"), resultset.getString("password"), resultset.getString("salt_value"),
						 resultset.getString("email"),resultset.getBoolean("is_admin"), resultset.getBoolean("is_remove"));
				users.add(user);
			}
			LOG.info("end= ");
		} catch (Exception e) {
			LOG.error("Unable to open database connection in User List= ", e);

			//LOG.error(String.format("unable to get query databse for users list %s", userid), e);
		} finally {
			closeConnection();
		}

		APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
		response.setStatusCode(200);
		ObjectMapper userMapper = new ObjectMapper();

		try {
			String responseBody = userMapper.writeValueAsString(users);
			response.setBody(responseBody);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
			LOG.info("error in getting user List Json= ", e.getMessage());
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
			LOG.error("Unable to close database connection in User List= ", ex.getMessage());
		}

	}

}