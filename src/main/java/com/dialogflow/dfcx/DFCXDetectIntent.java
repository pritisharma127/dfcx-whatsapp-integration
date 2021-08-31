package com.dialogflow.dfcx;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.api.gax.rpc.ApiException;
import com.google.cloud.dialogflow.cx.v3beta1.DetectIntentRequest;
import com.google.cloud.dialogflow.cx.v3beta1.DetectIntentResponse;
import com.google.cloud.dialogflow.cx.v3beta1.QueryInput;
import com.google.cloud.dialogflow.cx.v3beta1.QueryResult;
import com.google.cloud.dialogflow.cx.v3beta1.SessionName;
import com.google.cloud.dialogflow.cx.v3beta1.SessionsClient;
import com.google.cloud.dialogflow.cx.v3beta1.SessionsSettings;
import com.google.cloud.dialogflow.cx.v3beta1.TextInput;

/**
 * @author pritisharma
 * This is the integration with Google DialogFlow CX SDK. DFCX's detectIntent() method is used to find what user said/typed.
 *
 */
public class DFCXDetectIntent {
	
	private static Logger logger = LoggerFactory.getLogger(DFCXDetectIntent.class);

	// Add GOOGLE_APPLICATION_CREDENTIALS=<AbsolutePath>/dfcx-whatsapp-service.json in environment while running this
	/**
	 * For testing ONLY
	 */
	public static void main(String[] args) throws Exception{
		detectIntentWhatsApp("civil-cascade-320214", "us-central1", "df9a41ec-53b3-4bef-b417-1759c96320cb", "txt123", "डाउनलोड", "hi-IN");
	}

	/**
	 * @param projectId Google DFX project id from CX console
	 * @param locationId Location selected while creating Google DFCX bot
	 * @param agentId DFCX agent id
	 * @param sessionId session id
	 * @param text User input
	 * @param languageCode en-US or hi-IN based on user selection
	 * @return Array of String containing intent and response which will be displayed back to the user
	 * @throws IOException
	 * @throws ApiException
	 */
	@SuppressWarnings("deprecation")
	public static String[] detectIntentWhatsApp(String projectId, String locationId, String agentId, String sessionId, String text,
			String languageCode) throws IOException, ApiException {

		logger.info("detectIntentWhatsApp() - Start");
		String[] responseArr = new String[2];
		SessionsSettings.Builder sessionsSettingsBuilder = SessionsSettings.newBuilder();
		if (locationId.equals("global")) {
			sessionsSettingsBuilder.setEndpoint("dialogflow.googleapis.com:443");
		} else {
			sessionsSettingsBuilder.setEndpoint(locationId + "-dialogflow.googleapis.com:443");
		}
		SessionsSettings sessionsSettings = sessionsSettingsBuilder.build();

		// 1. Instantiates a client
		try (SessionsClient sessionsClient = SessionsClient.create(sessionsSettings)) {
			
			// 2. Set the session name using the projectID (my-project-id), locationID (global), agentID (UUID), and sessionId (UUID).
			SessionName session = SessionName.of(projectId, locationId, agentId, sessionId);

			// 3. Set the text (hello) for the query.
			TextInput.Builder textInput = TextInput.newBuilder().setText(text);

			// 4. Build the query with the TextInput and language code (en-US).
			QueryInput queryInput = QueryInput.newBuilder().setText(textInput).setLanguageCode(languageCode).build();

			// 5. Build the DetectIntentRequest with the SessionName and QueryInput.
			DetectIntentRequest request = DetectIntentRequest.newBuilder()
					.setSession(session.toString())
					.setQueryInput(queryInput)
					.build();

			// 6. Performs the detect intent request.
			DetectIntentResponse response = sessionsClient.detectIntent(request);

			// 7. Display the query result.
			QueryResult queryResult = response.getQueryResult();
			String playPrompt = queryResult.getResponseMessagesList().get(0).getText().getText(0);
			logger.info("detectIntentWhatsApp() - playPrompt = " + playPrompt);
			String intent = queryResult.getIntent().getDisplayName();
			logger.info("detectIntentWhatsApp() - intent =" + intent);
			responseArr[0] = playPrompt;
			responseArr[1] = intent;
		} catch (Exception e) {
			logger.info("detectIntentWhatsApp() - Error occured while executing Google Dialogflow detectIntent(), " + e.getMessage());
		}
		logger.info("detectIntentWhatsApp() - End");
		return responseArr;
	}
}