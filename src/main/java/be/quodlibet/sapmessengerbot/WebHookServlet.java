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
    private final String VALIDATION_TOKEN = (System.getenv("VALIDATION_TOKEN") == null) ? "swordfish" : System.getenv("VALIDATION_TOKEN");
    private final String PAGE_ACCESS_TOKEN = (System.getenv("PAGE_ACCESS_TOKEN") == null) ? "EAAOXlscoeoUBADZACITQiKmQi6EABQfy6U1iOsHj9xqy4ZA7NFQYOeVC7XZCP867drPqs2h8fJJgaLZAKwfLbYNo5NjiV0KTHZAB5ZAIkNoHJsNHzz5JUZA0plIDq8xTD4ZAEckERkWYXeJAQ6KMNjwl1TRW1w0ZCuLGww9mVZCar2ZCgZDZD" : System.getenv("PAGE_ACCESS_TOKEN");


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
        logger.info("Environment Variable ENVVAR1 = " + System.getenv("ENVVAR1"));
        logger.debug("Page Access token = " + PAGE_ACCESS_TOKEN);
        JSONObject message = getRequestBodyJson(request);
        logger.debug("Incoming Message :");
        logger.debug(message.toString(2));
        String textMessage = message.getJSONArray("entry").getJSONObject(0).getJSONArray("messaging").getJSONObject(0).getJSONObject("message").getString("text");
        String sender = message.getJSONArray("entry").getJSONObject(0).getJSONArray("messaging").getJSONObject(0).getJSONObject("sender").getString("id");
        //Simply send the user back what he said
        sendAPI.sendTextMessage("You said : " + textMessage, sender, PAGE_ACCESS_TOKEN);
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
