package be.quodlibet.sapmessengerbot;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Dries Horions <dries@quodlibet.be>
 */
@WebServlet(name = "WebHookServlet", urlPatterns = {"/webhook"})
public class WebHookServlet extends HttpServlet
{

    //TODO : check why vm parameters from hcp are not accessible this way
    private final String VALIDATION_TOKEN = (System.getProperty("VALIDATION_TOKEN") == null) ? "swordfish" : System.getProperty("VALIDATION_TOKEN");
    private final String PAGE_ACCESS_TOKEN = (System.getProperty("PAGE_ACCESS_TOKEN") == null) ? "UNKNOWN" : System.getProperty("PAGE_ACCESS_TOKEN");


    Logger logger = LoggerFactory.getLogger(WebHookServlet.class);
    /**
     * Verify the Subscribe Token
     *
     * @param request
     * @param response
     * @throws ServletException
     * @throws IOException
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {

        if (request.getParameter("hub.mode").equals("subscribe")
            && request.getParameter("hub.verify_token").equals(VALIDATION_TOKEN)) {
            logger.info("Validated Webhook");
            response.setStatus(200);
            response.getWriter().write(request.getParameter("hub.challenge"));
        }
        else {
            logger.info("Failed validation. Make sure the validation tokens match.");
            response.setStatus(403);
        }
    }

    /**
     * Respond to all webhooks from messenger API
     * https://developers.facebook.com/docs/messenger-platform/webhook-reference
     *
     * @param request
     * @param response
     * @throws ServletException
     * @throws IOException
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {
        logger.debug("Page Access token = " + PAGE_ACCESS_TOKEN);
        JSONObject message = getRequestBodyJson(request);
        logger.debug("Incoming Message :");
        logger.debug(message.toString(2));
        JSONObject entry = message.getJSONArray("entry").getJSONObject(0);
        for (Object messaging : entry.getJSONArray("messaging")) {
            JSONObject m = (JSONObject) messaging;
            String sender = m.getJSONObject("sender").getString("id");
            if (m.has("message")) {
                //We received a text message
                String textMessage = m.getJSONObject("message").getString("text");
                sendAPI.sendTextMessage("You said : " + textMessage, sender, PAGE_ACCESS_TOKEN);
            }
        }
       response.setStatus(200);

    }

    private JSONObject getRequestBodyJson(HttpServletRequest request)
    {
        logger.debug("Converting request body to json");
        String requestBody = "";
        try {
            String line;
            while ((line = request.getReader().readLine()) != null) {
                requestBody += line;
            }
        }
        catch (IOException ex) {
            logger.error("failed to convert request body to json : " + ex.getMessage());

        }
        logger.debug("Converted request body to json");
        JSONObject obj = new JSONObject(requestBody);
        return obj;
    }



}
