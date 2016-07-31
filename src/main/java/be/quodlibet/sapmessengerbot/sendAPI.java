package be.quodlibet.sapmessengerbot;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import javax.net.ssl.HttpsURLConnection;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Dries Horions <dries@quodlibet.be>
 */
public class sendAPI
{

    static Logger logger = LoggerFactory.getLogger(sendAPI.class);
    public static void sendTextMessage(String text, String recipient, String PAGE_ACCESS_TOKEN)
    {
        JSONObject textMessage = new JSONObject(
                "{\n"
                + "  \"recipient\":{\n"
                + "    \"id\":\"\"\n"
                + "  },\n"
                + "  \"message\":{\n"
                + "    \"text\":\"\"\n"
                + "  }\n"
                + "}"
        );
        //Set the recipient
        ((JSONObject) textMessage.get("recipient")).put("id", recipient);
        //Set the text message
        ((JSONObject) textMessage.get("message")).put("text", text);
        send(textMessage, PAGE_ACCESS_TOKEN);
    }

    private static void send(JSONObject message, String PAGE_ACCESS_TOKEN)
    {
        try {
            URL u = new URL("https://graph.facebook.com/v2.6/me/messages?access_token=" + PAGE_ACCESS_TOKEN);
            HttpsURLConnection con = (HttpsURLConnection) u.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json");
            con.setDoOutput(true);
            DataOutputStream wr = new DataOutputStream(con.getOutputStream());
            wr.writeBytes(message.toString());
            wr.flush();
            wr.close();
            int responseCode = con.getResponseCode();
            //Log the response
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()));
            String inputLine;
            String response = "";

            while ((inputLine = in.readLine()) != null) {
                response += inputLine;
            }
            logger.debug("Facebook Messenger response : \n\t" + response);
            in.close();

        }
        catch (MalformedURLException ex) {
            logger.error(ex.getMessage());
        }
        catch (IOException ex) {
            logger.error(ex.getMessage());
        }

    }
}
