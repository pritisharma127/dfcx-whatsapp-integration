# dfcx-whatsapp-integration
Integration of Google Dialogflow CX with WhatsApp through Twilio Platform

## How to setup and run CovMate WhatsApp Bot?
1. Clone the dfcx-whatsapp-integration project in your favorite IDE.
(https://github.com/pritisharma127/dfcx-whatsapp-integration)
2. Set the following environment variable in run configuration of your IDE to access the app developed in Google Dialogflow CX. 
If you are running in Eclipse, right click on DialogflowWhatsAppApplication.java > Run As > Run Configurations... > "Environment" tab > New... > Set Name = GOOGLE_APPLICATION_CREDENTIALS > Set Value = Absolute path of "dfcx-whatsapp-service.json" > Save > Apply.
For IDEA, please refer <a href="https://www.jetbrains.com/help/objc/add-environment-variables-and-program-arguments.html#add-environment-variables">this</a>.
3. Start SpringBoot application by right clicking on DialogflowWhatsAppApplication.java > Run As > Java Application. Application will start on port number 4567.
4. http://localhost:4567 needs to be exposed as external URL for to be configured in Twilio platform. We use ngrok for this. Download ngrok from https://ngrok.com/download.
5. In cmd or terminal run:
./ngrok http 4567
This will give you url like http://57b6-2401-4900-502b-523d-c845-c90d-b7a8-e87c.ngrok.io (https as well).
6. Login to Twilio console at https://console.twilio.com/. Set the above URL for WhatsApp webhook in Twilio for redirection.
7. In WhatsApp, send "join found-certainly" to Twilio number +1 (415) 523-8886 to connect with Twilio sandbox.
8. Send Hi to start the conversation with CovMate WhatsApp Bot.
9. Google DialogFlow CX bot agent export : https://github.com/pritisharma127/dfcx-whatsapp-integration/tree/main/DialogflowCXBotExport
10. Technical details, flow diagram please refer this : https://github.com/pritisharma127/dfcx-whatsapp-integration/blob/main/TechSpecs/DesignDocument_CovMate_DFCX.pdf
11. Please use this link to see WhatsApp - Google DialogFlow CX integration : https://github.com/pritisharma127/dfcx-whatsapp-integration/tree/main/DemoVideo