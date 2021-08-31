# dfcx-whatsapp-integration
Integration of Google Dialogflow CX with WhatsApp through Twilio Platform

## How to setup and run CovMate WhatsApp Bot?
1. Clone the dfcx-whatsapp-integration project in your favorite IDE.
2. Set environment variable in run configuration of your IDE. 
If you are running in Eclipse, right click on DialogflowWhatsAppApplication.java > Run As > Run Configurations... > "Environment" tab > New... > Set Name = GOOGLE\_APPLICATION\_CREDENTIALS > Set Value = Absolute path of "dfcx-whatsapp-service.json" > Save > Apply.
<br>For IDEA, please refer <a href="https://www.jetbrains.com/help/objc/add-environment-variables-and-program-arguments.html#add-environment-variables">this</a>.
3. Make sure twilio.accountsid and twilio.authtoken are present in application.yml and are uncommented.
4. Start SpringBoot application by right clicking on DialogflowWhatsAppApplication.java > Run As > Java Application. Application will start on port number 4567.
5. http://localhost:4567 needs to be exposed as external URL for to be configured in Twilio platform. We use ngrok for this. Download ngrok <a href="https://ngrok.com/download">here</a>.
6. In cmd or terminal run<br>
./ngrok http 4567
<br>This will give you url like http://57b6-2401-4900-502b-523d-c845-c90d-b7a8-e87c.ngrok.io.
7. Login to <a href="https://console.twilio.com/">Twilio console</a>. Set the above URL for WhatsApp webhook in Twilio for redirection.
8. In WhatsApp, send "join found-certainly" to Twilio number +1 (415) 523-8886 to connect with Twilio sandbox.
9. Send Hi to start the conversation with CovMate WhatsApp Bot.