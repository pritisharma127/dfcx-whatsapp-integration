package com.dialogflow.controller;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dialogflow.dfcx.DFCXDetectIntent;
import com.dialogflow.service.CowinService;
import com.google.common.hash.Hashing;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;

/**
 * @author pritisharma
 * Controller class which communicates with Twilio as a webhook, integrates with DFCX SDK and sends back Twilio supported response
 *
 */
@RestController
public class DFCXWhatsAppController {

	private static Logger logger = LoggerFactory.getLogger(DFCXWhatsAppController.class);
	
	public static final String ACCOUNT_SID = "AC84040e7fc899fc50296d5f922731a64e";//Arun account +1-385-336-0573
	public static final String AUTH_TOKEN = "2d6cb82f7c2082036d41aeaa47a8f107"; //Arun Account +1-385-336-0573
	private String resourceBaseUrl = "http://57b6-2401-4900-502b-523d-c845-c90d-b7a8-e87c.ngrok.io/";

	public Map<String, Long> userSessionInfo = new HashMap<>();
	public Map<String, String> otpTrxnIdMap = new HashMap<>();
	public Map<String, String> otpTokenMap = new HashMap<>();
	public Map<String, String> latestUserIntentMap = new HashMap<>();
	private String language = "en-US";

	List<String> resetConversationList = new ArrayList<String>() {
		private static final long serialVersionUID = 1L;
		{
			add("reset");
			add("restart");
			add("start");
			add("menu");
			add("begin");
			add("back");
			add("first");
			add("main menu");
			add("mainmenu");
			add("firstmenu");
			add("first menu");
			add("प्रारंभ");
			add("शुरुआत");
			add("पहला");
			add("वापस");
			add("पीछे");
			add("शुरु");
		}};

		@Autowired
		CowinService cowinService;

		@PostMapping(path="/cowin",produces="application/xml", consumes="application/x-www-form-urlencoded")
		public ResponseEntity<Void> whatsAppBot(UserParam message) throws Exception {

			logger.info("== START ==");
			Long currentTime= System.currentTimeMillis();
			String userPhoneNumber = message.getFrom().substring(12, message.getFrom().length());
			Twilio.init(ACCOUNT_SID, AUTH_TOKEN);

			String trxnId = "";
			// Very first message
			if(userSessionInfo.get(userPhoneNumber) == null) {
				latestUserIntentMap.put(userPhoneNumber, "start");
				// Setup session with the incoming message
				userSessionInfo.put(userPhoneNumber, System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(8));
				//First dialog
				Message.creator( 
						new com.twilio.type.PhoneNumber(message.getFrom()), 
						new com.twilio.type.PhoneNumber("whatsapp:+14155238886"),  
						"✅ Hello! I am CovMate, welcome to *CoWin* WhatsApp Service ✅"
								+ "\n" + "Please reply with 1 for English. हिंदी के लिए 2 भेजें")
				.setMediaUrl(Arrays.asList(URI.create(resourceBaseUrl + "CovMate.jpeg")))
				.create();
			}
			else if (resetConversationList.contains(message.getBody().trim().toLowerCase())) {//Restart conversation
				// Clear and start from beginning
				userSessionInfo.clear();
				otpTrxnIdMap.clear();
				otpTokenMap.clear();
				latestUserIntentMap.clear();
				userSessionInfo.put(userPhoneNumber, System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(8));
				Message.creator( 
						new com.twilio.type.PhoneNumber(message.getFrom()), 
						new com.twilio.type.PhoneNumber("whatsapp:+14155238886"),  
						"OK. Let us start from the beginning 🔙"
								+ "\n" + "Please reply with 1️⃣ for English. हिंदी के लिए 2️⃣ भेजें")      
				.create();
			} 
			//immediate after first dialog
			else {
				logger.info("User input = " + message.getBody());
				List<String> langKey = new ArrayList<String>();
				langKey.add("ENGLISH");
				langKey.add("1");
				langKey.add("2");
				langKey.add("२");
				langKey.add("TWO");
				langKey.add("ONE");
				langKey.add("ENG");
				langKey.add("HIN");
				langKey.add("HINDI");
				langKey.add("हिंदी");
				langKey.add("हिन्दी");
				if(currentTime < userSessionInfo.get(userPhoneNumber) && (langKey.contains(message.getBody().trim().toUpperCase()))){ //Language selection
					logger.info("1. Inside language selection");
					if(message.getBody().trim().contains("HIN") || message.getBody().trim().equals("2") || message.getBody().trim().equalsIgnoreCase("TWO")) {
						language = "hi-IN";
						Message.creator( 
								new com.twilio.type.PhoneNumber(message.getFrom()), 
								new com.twilio.type.PhoneNumber("whatsapp:+14155238886"),  
								"मैं टीकाकरण प्रमाणपत्र डाउनलोड 📥 करने और CoWin से संबंधित प्रश्नों में आपकी सहायता कर सकता हूं"
										+ "\n" + "तो, मैं आज आपकी कैसे सहायता कर सकता हूँ❓")      
						.create();
					} else if(message.getBody().trim().contains("ENG") || message.getBody().trim().equals("1") || message.getBody().trim().equalsIgnoreCase("ONE")) { // English
						language = "en-US";
						Message.creator( 
								new com.twilio.type.PhoneNumber(message.getFrom()), 
								new com.twilio.type.PhoneNumber("whatsapp:+14155238886"),  
								"I can help you to download 📥 vaccination certificate and FAQs related to CoWin."
										+ "\n" + "So, how can I assist you today❓")      
						.create();
					}
					logger.info("Inside language selection, language selected = " + language);
				} else { // User sent anything other than language selection (Language defaults to English)
					logger.info("Language selected for detectIntent API = " + language);
					// Call detectIntent() here
					String[] recResult = DFCXDetectIntent.detectIntentWhatsApp(
							"civil-cascade-320214", "us-central1", "df9a41ec-53b3-4bef-b417-1759c96320cb", "txt123", message.getBody().toLowerCase(), language);
					// Arrays 0th element is response to be show to the user
					String displayMessage = recResult[0];
					// Arrays 1st element is the intent recognized by Google DFCX which indicates what user wants
					String intent = recResult[1];

					if (currentTime < userSessionInfo.get(userPhoneNumber) && intent.contains("download")) { // Download intent detected + Send OTP
						logger.info("2. Download intent detected, sending OTP now");
						latestUserIntentMap.put(userPhoneNumber, "download");
						Message.creator( 
								new com.twilio.type.PhoneNumber(message.getFrom()), 
								new com.twilio.type.PhoneNumber("whatsapp:+14155238886"), 
								displayMessage)
						.create();
						/*Call COWIN OTP API here*/
						trxnId = cowinService.generateOTP(userPhoneNumber);
						trxnId = trxnId.replace("{\"txnId\":\"", "");
						trxnId = trxnId.replace("\"}", "");
						otpTrxnIdMap.put(userPhoneNumber, trxnId);

						if("hi-IN".equalsIgnoreCase(language)) {
							Message.creator( 
									new com.twilio.type.PhoneNumber(message.getFrom()), 
									new com.twilio.type.PhoneNumber("whatsapp:+14155238886"),  
									"🔑 OTP प्राप्त करने के बाद, कृपया इसे यहां भेजें")
							.create();
						} else {
							Message.creator( 
									new com.twilio.type.PhoneNumber(message.getFrom()), 
									new com.twilio.type.PhoneNumber("whatsapp:+14155238886"),  
									"Once you have received the OTP 🔑, please go ahead and send it here.")      
							.create();
						}
					} else if ((currentTime  < userSessionInfo.get(userPhoneNumber)) && (message.getBody().trim().length() == 6) 
							&& latestUserIntentMap.get(userPhoneNumber).contains("download")) {// validateOTP

						logger.info("3. OTP received, asking for beneficiary id now");
						try {
							int otp = Integer.parseInt(message.getBody().trim());
							/* Call Validate OTP here */
							String encodedOTP = Hashing.sha256().hashString(otp+"", StandardCharsets.UTF_8).toString();
							String token = cowinService.validateOTP(encodedOTP, otpTrxnIdMap.get(userPhoneNumber));
							token = token.replace("{\"token\":\"", "");
							token = token.replace("\"}", "");
							otpTokenMap.put(userPhoneNumber, token);
							latestUserIntentMap.put(userPhoneNumber, "otp");
							if("hi-IN".equalsIgnoreCase(language)) {
								Message.creator( 
										new com.twilio.type.PhoneNumber(message.getFrom()), 
										new com.twilio.type.PhoneNumber("whatsapp:+14155238886"),  
										"✅ मैंने आपको प्रमाणित कर दिया है! अब कृपया मुझे अपनी 14 अंकों की लाभार्थी आईडी (beneficiary id) भेजें")
								.create();
							} else {
								Message.creator( 
										new com.twilio.type.PhoneNumber(message.getFrom()), 
										new com.twilio.type.PhoneNumber("whatsapp:+14155238886"),  
										"✅ I have authenticated you! Now please send me your beneficiary id.")      
								.create();
							}

						} catch (NumberFormatException nfe) {
							if("hi-IN".equalsIgnoreCase(language)) {
								Message.creator( 
										new com.twilio.type.PhoneNumber(message.getFrom()), 
										new com.twilio.type.PhoneNumber("whatsapp:+14155238886"),  
										"❌ आपके द्वारा दर्ज किया गया OTP गलत है! कृपया फिर से enter करें")
								.create();
							} else {
								Message.creator( 
										new com.twilio.type.PhoneNumber(message.getFrom()), 
										new com.twilio.type.PhoneNumber("whatsapp:+14155238886"),  
										"❌ OTP is invalid! please enter again!")      
								.create();
							}
						}
					} else if((currentTime  < userSessionInfo.get(userPhoneNumber)) && (message.getBody().trim().length() == 14) 
							&& latestUserIntentMap.get(userPhoneNumber).equals("otp")) {// Get BenId and download cert
						logger.info("4. Beneficiary id received, downloading certificate now");
						try {
							long benId = Long.parseLong(message.getBody().trim());
							cowinService.downloadCertificate(benId+"", otpTokenMap.get(userPhoneNumber));
							latestUserIntentMap.put(userPhoneNumber, "beneid");
							Thread.sleep(1000);
							if("hi-IN".equalsIgnoreCase(language)) {
								Message.creator( 
										new com.twilio.type.PhoneNumber(message.getFrom()), 
										new com.twilio.type.PhoneNumber("whatsapp:+14155238886"),  
										"📌 ये लिजिये आपका प्रमाणपत्र 👍👍")
								.create();
							} else {
								Message.creator( 
										new com.twilio.type.PhoneNumber(message.getFrom()), 
										new com.twilio.type.PhoneNumber("whatsapp:+14155238886"),  
										"📌 Here is your certificate 👍👍")
								.create();
							}
							Message.creator(new com.twilio.type.PhoneNumber(message.getFrom()), 
									new com.twilio.type.PhoneNumber("whatsapp:+14155238886"),
									"Cert_" + benId + ".pdf").
							setMediaUrl(Arrays.asList(URI.create(resourceBaseUrl + "Cert_" + benId + ".pdf"))).
							create();
							logger.info("5. Download complete!");
						} catch (NumberFormatException nfe) {
							if("hi-IN".equalsIgnoreCase(language)) {
								Message.creator( 
										new com.twilio.type.PhoneNumber(message.getFrom()), 
										new com.twilio.type.PhoneNumber("whatsapp:+14155238886"),  
										"❌ आपके द्वारा दर्ज किया गया लाभार्थी आईडी (beneficiary id) गलत है! कृपया फिर से enter करें")
								.create();
							} else {
								Message.creator( 
										new com.twilio.type.PhoneNumber(message.getFrom()), 
										new com.twilio.type.PhoneNumber("whatsapp:+14155238886"),  
										"❌ Beneficiery id you entered is invalid, please enter again")      
								.create();
							}
						}
					} else {// Other intents
						String pdfMessage = "";
						if("hi-IN".equalsIgnoreCase(language)) {
							pdfMessage = "📄 अधिक जानकारी के लिए कृपया इस PDF को पढ़ें";
						} else {
							pdfMessage = "📄 For more information, please refer to this PDF";
						}
						logger.info("Other intent detected");
						if (currentTime < userSessionInfo.get(userPhoneNumber)) {
							Message.creator( 
									new com.twilio.type.PhoneNumber(message.getFrom()), 
									new com.twilio.type.PhoneNumber("whatsapp:+14155238886"), 
									displayMessage)
							.create();
						}
						if (currentTime < userSessionInfo.get(userPhoneNumber) && intent.contains("Default Welcome Intent")) {
							// Do nothing, we already displayed language selection message
						} else if (currentTime < userSessionInfo.get(userPhoneNumber) && intent.contains("contactus")) {
							if("hi-IN".equalsIgnoreCase(language)) {
								pdfMessage = " 👉 सुझाव और अधिक जानकारी के लिए कृपया उस 🏥 टीकाकरण केंद्र से भी संपर्क करें जहां आपने कोविड की खुराक ली थी।";
							} else {
								pdfMessage = "👉 Please contact the 🏥 Vaccination Centre where you took covid vaccination, suggestions & more information";
							}
							Message.creator(new com.twilio.type.PhoneNumber(message.getFrom()), 
									new com.twilio.type.PhoneNumber("whatsapp:+14155238886"),
									pdfMessage).
							setMediaUrl(Arrays.asList(URI.create(resourceBaseUrl + "ContactUs.pdf"))).
							create();
						} else if (currentTime < userSessionInfo.get(userPhoneNumber) && intent.contains("registration")) {
							if("hi-IN".equalsIgnoreCase(language)) {
								pdfMessage = "📄 पंजीकरण और स्लॉट जानकारी के बारे में अन्य जानकारी के लिए, कृपया इस पीडीएफ को देखें।";
							} else {
								pdfMessage = "📄 For other information on registration and slot information, please refer to this PDF";
							}
							Message.creator(new com.twilio.type.PhoneNumber(message.getFrom()), 
									new com.twilio.type.PhoneNumber("whatsapp:+14155238886"),
									pdfMessage).
							setMediaUrl(Arrays.asList(URI.create(resourceBaseUrl + "Registration.pdf"))).
							create();
						} else if (currentTime < userSessionInfo.get(userPhoneNumber) && intent.contains("donts")) {
							if("hi-IN".equalsIgnoreCase(language)) {
								pdfMessage = " 📃 काम करने और न करने के बारे में अधिक जानकारी के लिए, कृपया इस पीडीएफ को देखें";
							} else {
								pdfMessage = "📃 For more information on Doing and not doing things, please refer to this PDF";
							}
							Message.creator(new com.twilio.type.PhoneNumber(message.getFrom()), 
									new com.twilio.type.PhoneNumber("whatsapp:+14155238886"),
									pdfMessage).
							setMediaUrl(Arrays.asList(URI.create(resourceBaseUrl + "Dos_and_Donts.pdf"))).
							create();
						} else if (currentTime < userSessionInfo.get(userPhoneNumber) && intent.contains("sideeffect")) {
							if("hi-IN".equalsIgnoreCase(language)) {
								pdfMessage = " 👉 टीके से किसी अन्य दुष्प्रभाव के लिए, कृपया इस पीडीएफ को देखें";
							} else {
								pdfMessage = "👉 For any other side effects from vaccine, please refer to this PDF";
							}
							Message.creator(new com.twilio.type.PhoneNumber(message.getFrom()), 
									new com.twilio.type.PhoneNumber("whatsapp:+14155238886"),
									pdfMessage).
							setMediaUrl(Arrays.asList(URI.create(resourceBaseUrl + "VaccineEffects.pdf"))).
							create();
						} else if (currentTime < userSessionInfo.get(userPhoneNumber) && intent.contains("grievance")) {
							if("hi-IN".equalsIgnoreCase(language)) {
								pdfMessage = " 👉 यदि आप COWIN पोर्टल से संबंधित शिकायत, शिकायत या कोई तकनीकी समस्या दर्ज करना चाहते हैं, तो कृपया इस पीडीएफ को देखें";
							} else {
								pdfMessage = "👉 In case if you want to register Grievance, complaint or any technical issue related to COWIN portal, please refer to this PDF";
							}
							Message.creator(new com.twilio.type.PhoneNumber(message.getFrom()), 
									new com.twilio.type.PhoneNumber("whatsapp:+14155238886"),
									pdfMessage).
							setMediaUrl(Arrays.asList(URI.create(resourceBaseUrl + "ContactUs.pdf"))).
							create();
						} else if (currentTime < userSessionInfo.get(userPhoneNumber) && intent.contains("verifyvaccinecertificate")) {
							if("hi-IN".equalsIgnoreCase(language)) {
								pdfMessage = " 📃 प्रमाणपत्र क्यूआर कोड को स्कैन या सत्यापित करने के तरीके जैसी जानकारी के लिए, कृपया इस पीडीएफ को देखें";
							} else {
								pdfMessage = "📃 For information like how to scan or verify the certificate QR code, please refer to this PDF";
							}
							Message.creator(new com.twilio.type.PhoneNumber(message.getFrom()), 
									new com.twilio.type.PhoneNumber("whatsapp:+14155238886"),
									pdfMessage).
							setMediaUrl(Arrays.asList(URI.create(resourceBaseUrl + "VerifyCertificate.pdf"))).
							create();
						} else if (currentTime < userSessionInfo.get(userPhoneNumber) && intent.contains("availability")) {
							if("hi-IN".equalsIgnoreCase(language)) {
								pdfMessage = " 👉 स्लॉट, बुकिंग, अपॉइंटमेंट लेने के बारे में अधिक जानने के लिए, कृपया इस पीडीएफ को देखें";
							} else {
								pdfMessage = "👉 To know more about slots, booking, taking appointment, please refer to this PDF";
							}
							Message.creator(new com.twilio.type.PhoneNumber(message.getFrom()), 
									new com.twilio.type.PhoneNumber("whatsapp:+14155238886"),
									pdfMessage).
							setMediaUrl(Arrays.asList(URI.create(resourceBaseUrl + "Registration.pdf"))).
							create();
						} else if (currentTime < userSessionInfo.get(userPhoneNumber) && intent.contains("goodbye")) {
							Message.creator( 
									new com.twilio.type.PhoneNumber(message.getFrom()), 
									new com.twilio.type.PhoneNumber("whatsapp:+14155238886"), 
									"THANK YOU!").
							setMediaUrl(Arrays.asList(URI.create(resourceBaseUrl + "ThankYou.jpeg"))).
							create();
						} else { // nomatch
							otpTrxnIdMap.clear();
							otpTokenMap.clear();
							latestUserIntentMap.clear();
							latestUserIntentMap.put(userPhoneNumber, "start");
							if(currentTime > userSessionInfo.get(userPhoneNumber)) {
								logger.info("WhatsApp session timed out! Start again.");
								userSessionInfo.put(userPhoneNumber, System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(8));
								Message.creator( 
										new com.twilio.type.PhoneNumber(message.getFrom()), 
										new com.twilio.type.PhoneNumber("whatsapp:+14155238886"),  
										"Your WhatsApp session expired! Let us start from the beginning 🔙"
												+ "\n" + "Please reply with 1️⃣ for English. हिंदी के लिए 2️⃣ भेजें")      
								.create();
							} else {
								logger.info("No intent matched! Start again.");
								if("hi-IN".equalsIgnoreCase(language)) {
									Message.creator( 
											new com.twilio.type.PhoneNumber(message.getFrom()), 
											new com.twilio.type.PhoneNumber("whatsapp:+14155238886"),  
											"📩 मैं टीकाकरण प्रमाणपत्र डाउनलोड करने और CoWin से संबंधित प्रश्नों में आपकी सहायता कर सकता हूं"
													+ "\n" + "तो, मैं आज आपकी कैसे सहायता कर सकता हूँ?")      
									.create();
								} else { // English
									Message.creator( 
											new com.twilio.type.PhoneNumber(message.getFrom()), 
											new com.twilio.type.PhoneNumber("whatsapp:+14155238886"),  
											"📩 I can help you to download vaccination certificate and FAQs related to CoWin."
													+ "\n" + "So, how can I assist you today?")      
									.create();
								}
							}
						}
					}
				}
			}//end else big
			return new ResponseEntity<Void>(HttpStatus.OK);
		}
}