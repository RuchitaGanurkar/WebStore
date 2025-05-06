package com.webstore.implementation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.webstore.configuration.WhatsAppConfiguration;
import com.webstore.dto.response.CatalogueResponseDto;
import com.webstore.dto.response.CategoryResponseDto;
import com.webstore.dto.response.ProductResponseDto;
import com.webstore.dto.request.WhatsAppWebhookRequestDto;
import com.webstore.dto.request.WhatsAppMessageRequestDto;
import com.webstore.dto.response.WhatsAppMessageResponseDto;
import com.webstore.service.CatalogueService;
import com.webstore.service.CategoryService;
import com.webstore.service.WhatsAppService;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class WhatsAppServiceImplementation implements WhatsAppService {

    private final WhatsAppConfiguration whatsAppConfiguration;
    private final CatalogueService catalogueService;
    private final CategoryService categoryService;
    private final ObjectMapper objectMapper;
    private final OkHttpClient httpClient;

    // Simple in-memory conversation state (NOT persisted)
    private final Map<String, String> conversationState = new HashMap<>();

    @Autowired
    public WhatsAppServiceImplementation(
            WhatsAppConfiguration whatsAppConfiguration,
            CatalogueService catalogueService,
            CategoryService categoryService,
            ObjectMapper objectMapper) {
        this.whatsAppConfiguration = whatsAppConfiguration;
        this.catalogueService = catalogueService;
        this.categoryService = categoryService;
        this.objectMapper = objectMapper;

        // Configure OkHttp client with reasonable timeouts
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
    }

    @Override
    public void processWebhook(String payload) {
        try {
            WhatsAppWebhookRequestDto webhookRequest = objectMapper.readValue(payload, WhatsAppWebhookRequestDto.class);

            if (webhookRequest.getObject() != null && webhookRequest.getEntry() != null) {
                for (WhatsAppWebhookRequestDto.Entry entry : webhookRequest.getEntry()) {
                    for (WhatsAppWebhookRequestDto.Entry.Change change : entry.getChanges()) {
                        WhatsAppWebhookRequestDto.Entry.Change.Value value = change.getValue();

                        if (value != null && value.getMessages() != null && !value.getMessages().isEmpty()) {
                            for (WhatsAppWebhookRequestDto.Entry.Change.Value.Message message : value.getMessages()) {
                                handleIncomingMessage(message);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error processing webhook: {}", e.getMessage(), e);
        }
    }

    @Override
    public void handleIncomingMessage(WhatsAppWebhookRequestDto.Entry.Change.Value.Message message) {
        String from = message.getFrom();
        String type = message.getType();

        log.info("Received message from: {}, type: {}", from, type);

        // Handle different message types
        switch (type) {
            case "text":
                if (message.getText() != null) {
                    String text = message.getText().getBody();
                    handleTextMessage(text, from);
                }
                break;

            case "interactive":
                handleInteractiveMessage(message, from);
                break;

            default:
                sendTextMessage(from, "I can only process text and interactive messages at the moment.");
                break;
        }
    }

    private void handleTextMessage(String message, String from) {
        String text = message.toLowerCase().trim();
        String state = conversationState.getOrDefault(from, "WELCOME");

        // Handle commands available in any state
        if (text.equals("restart")) {
            sendWelcomeMessage(from);
            conversationState.put(from, "MAIN_MENU");
            return;
        }

        if (text.equals("menu") || text.equals("main menu")) {
            sendMainMenu(from);
            conversationState.put(from, "MAIN_MENU");
            return;
        }

        if (text.equals("help")) {
            sendHelpMessage(from);
            return;
        }

        // Handle state-specific text inputs
        switch (state) {
            case "WELCOME":
                sendWelcomeMessage(from);
                conversationState.put(from, "MAIN_MENU");
                break;

            case "MAIN_MENU":
                if (text.equals("browse") || text.equals("shop") || text.equals("catalogues")) {
                    sendCatalogueList(from);
                    conversationState.put(from, "BROWSING_CATALOGUES");
                } else {
                    sendTextMessage(from, "I don't understand that command. Type 'menu' to see available options.");
                }
                break;

            case "BROWSING_CATALOGUES":
                // Check if text is a number (catalogue selection)
                try {
                    int catalogueId = Integer.parseInt(text);
                    // Verify catalogue exists
                    catalogueService.getCatalogueById(catalogueId);
                    sendCategoryList(from, catalogueId);
                    // Store current catalogue ID
                    conversationState.put(from + "_catalogueId", String.valueOf(catalogueId));
                    conversationState.put(from, "BROWSING_CATEGORIES");
                } catch (NumberFormatException e) {
                    sendTextMessage(from, "Please select a catalogue by sending its number, or type 'menu' to return to the main menu.");
                } catch (Exception e) {
                    sendTextMessage(from, "Catalogue not found. Please select from the available options.");
                    sendCatalogueList(from);
                }
                break;

            case "BROWSING_CATEGORIES":
                if (text.equals("back")) {
                    sendCatalogueList(from);
                    conversationState.put(from, "BROWSING_CATALOGUES");
                    return;
                }

                // Check if text is a number (category selection)
                try {
                    int categoryId = Integer.parseInt(text);
                    // Verify category exists
                    categoryService.getCategoryById(categoryId);
                    sendProductList(from, categoryId);
                    // Store current category ID
                    conversationState.put(from + "_categoryId", String.valueOf(categoryId));
                    conversationState.put(from, "BROWSING_PRODUCTS");
                } catch (NumberFormatException e) {
                    sendTextMessage(from, "Please select a category by sending its number, or type 'back' to return to catalogues.");
                } catch (Exception e) {
                    sendTextMessage(from, "Category not found. Please select from the available options.");
                    String catalogueId = conversationState.get(from + "_catalogueId");
                    if (catalogueId != null) {
                        sendCategoryList(from, Integer.parseInt(catalogueId));
                    } else {
                        sendCatalogueList(from);
                        conversationState.put(from, "BROWSING_CATALOGUES");
                    }
                }
                break;

            case "BROWSING_PRODUCTS":
                if (text.equals("back")) {
                    String catalogueId = conversationState.get(from + "_catalogueId");
                    if (catalogueId != null) {
                        sendCategoryList(from, Integer.parseInt(catalogueId));
                        conversationState.put(from, "BROWSING_CATEGORIES");
                    } else {
                        sendCatalogueList(from);
                        conversationState.put(from, "BROWSING_CATALOGUES");
                    }
                } else {
                    sendTextMessage(from, "Type 'back' to return to categories or 'menu' for the main menu.");
                }
                break;

            default:
                sendTextMessage(from, "I'm not sure what to do. Type 'menu' to return to the main menu.");
                conversationState.put(from, "MAIN_MENU");
                break;
        }
    }

    private void handleInteractiveMessage(WhatsAppWebhookRequestDto.Entry.Change.Value.Message message, String from) {
        if (message.getInteractive() == null) {
            return;
        }

        String interactiveType = message.getInteractive().getType();

        if ("button_reply".equals(interactiveType) && message.getInteractive().getButtonReply() != null) {
            String buttonId = message.getInteractive().getButtonReply().getId();
            handleButtonSelection(buttonId, from);
        } else if ("list_reply".equals(interactiveType) && message.getInteractive().getListReply() != null) {
            String listItemId = message.getInteractive().getListReply().getId();
            handleListSelection(listItemId, from);
        }
    }

    private void handleButtonSelection(String buttonId, String from) {
        if (buttonId.startsWith("catalogue_")) {
            int catalogueId = Integer.parseInt(buttonId.substring(10));
            sendCategoryList(from, catalogueId);
            // Store current catalogue ID
            conversationState.put(from + "_catalogueId", String.valueOf(catalogueId));
            conversationState.put(from, "BROWSING_CATEGORIES");
        } else if (buttonId.startsWith("category_")) {
            int categoryId = Integer.parseInt(buttonId.substring(9));
            sendProductList(from, categoryId);
            // Store current category ID
            conversationState.put(from + "_categoryId", String.valueOf(categoryId));
            conversationState.put(from, "BROWSING_PRODUCTS");
        } else if (buttonId.equals("menu")) {
            sendMainMenu(from);
            conversationState.put(from, "MAIN_MENU");
        } else if (buttonId.equals("browse")) {
            sendCatalogueList(from);
            conversationState.put(from, "BROWSING_CATALOGUES");
        } else if (buttonId.equals("back_to_catalogues")) {
            sendCatalogueList(from);
            conversationState.put(from, "BROWSING_CATALOGUES");
        }
    }

    private void handleListSelection(String listItemId, String from) {
        // Similar to button selection, but for list items
        if (listItemId.startsWith("catalogue_")) {
            int catalogueId = Integer.parseInt(listItemId.substring(10));
            sendCategoryList(from, catalogueId);
            // Store current catalogue ID
            conversationState.put(from + "_catalogueId", String.valueOf(catalogueId));
            conversationState.put(from, "BROWSING_CATEGORIES");
        } else if (listItemId.startsWith("category_")) {
            int categoryId = Integer.parseInt(listItemId.substring(9));
            sendProductList(from, categoryId);
            // Store current category ID
            conversationState.put(from + "_categoryId", String.valueOf(categoryId));
            conversationState.put(from, "BROWSING_PRODUCTS");
        }
    }

    @Override
    public boolean sendTextMessage(String to, String message) {
        try {
            WhatsAppMessageRequestDto messageRequest = new WhatsAppMessageRequestDto();
            messageRequest.setMessagingProduct("whatsapp");
            messageRequest.setRecipientType("individual");
            messageRequest.setTo(to);
            messageRequest.setType("text");

            WhatsAppMessageRequestDto.TextMessage textMessage = new WhatsAppMessageRequestDto.TextMessage();
            textMessage.setBody(message);
            messageRequest.setText(textMessage);

            return sendRequest(messageRequest);
        } catch (Exception e) {
            log.error("Error sending text message: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean sendWelcomeMessage(String to) {
        String welcomeText = "Hello Customer! 👋\n\n" +
                "Welcome to our Web Store. How can I help you today?\n\n" +
                "Type 'browse' to see our catalogues\n" +
                "Type 'help' for more information";

        // Create simple buttons for browsing and help
        List<WhatsAppMessageRequestDto.Interactive.Button> buttons = new ArrayList<>();

        WhatsAppMessageRequestDto.Interactive.Button.Reply browseReply = new WhatsAppMessageRequestDto.Interactive.Button.Reply();
        browseReply.setId("browse");
        browseReply.setTitle("Browse Catalogues");

        WhatsAppMessageRequestDto.Interactive.Button browseButton = new WhatsAppMessageRequestDto.Interactive.Button();
        browseButton.setType("reply");
        browseButton.setReply(browseReply);
        buttons.add(browseButton);

        WhatsAppMessageRequestDto.Interactive.Button.Reply helpReply = new WhatsAppMessageRequestDto.Interactive.Button.Reply();
        helpReply.setId("help");
        helpReply.setTitle("Get Help");

        WhatsAppMessageRequestDto.Interactive.Button helpButton = new WhatsAppMessageRequestDto.Interactive.Button();
        helpButton.setType("reply");
        helpButton.setReply(helpReply);
        buttons.add(helpButton);

        return sendButtonMessage(to, "Welcome!", welcomeText, "Tap a button to get started", buttons);
    }

    private boolean sendMainMenu(String to) {
        String menuText = "What would you like to do today?";

        // Create buttons for main menu options
        List<WhatsAppMessageRequestDto.Interactive.Button> buttons = new ArrayList<>();

        WhatsAppMessageRequestDto.Interactive.Button.Reply browseReply = new WhatsAppMessageRequestDto.Interactive.Button.Reply();
        browseReply.setId("browse");
        browseReply.setTitle("Browse Products");

        WhatsAppMessageRequestDto.Interactive.Button browseButton = new WhatsAppMessageRequestDto.Interactive.Button();
        browseButton.setType("reply");
        browseButton.setReply(browseReply);
        buttons.add(browseButton);

        WhatsAppMessageRequestDto.Interactive.Button.Reply helpReply = new WhatsAppMessageRequestDto.Interactive.Button.Reply();
        helpReply.setId("help");
        helpReply.setTitle("Get Help");

        WhatsAppMessageRequestDto.Interactive.Button helpButton = new WhatsAppMessageRequestDto.Interactive.Button();
        helpButton.setType("reply");
        helpButton.setReply(helpReply);
        buttons.add(helpButton);

        return sendButtonMessage(to, "Main Menu", menuText, "Select an option", buttons);
    }

    private boolean sendHelpMessage(String to) {
        String helpText = "📱 *Available Commands*\n\n" +
                "• 'browse' - Browse our catalogues\n" +
                "• 'menu' - Return to main menu\n" +
                "• 'back' - Go back to previous screen\n" +
                "• 'restart' - Start a new conversation\n" +
                "• 'help' - Show this help message\n\n" +
                "Need more assistance? Contact our support team at support@webstore.com";

        return sendTextMessage(to, helpText);
    }

    @Override
    public boolean sendCatalogueList(String to) {
        try {
            List<CatalogueResponseDto> catalogues = catalogueService.getAllCatalogues();

            if (catalogues.isEmpty()) {
                return sendTextMessage(to, "No catalogues found at the moment. Please check back later.");
            }

            // Create a list message with catalogues
            List<WhatsAppMessageRequestDto.Interactive.Action.Section> sections = new ArrayList<>();
            WhatsAppMessageRequestDto.Interactive.Action.Section section = new WhatsAppMessageRequestDto.Interactive.Action.Section();
            section.setTitle("Available Catalogues");

            List<WhatsAppMessageRequestDto.Interactive.Action.Section.Row> rows = new ArrayList<>();

            for (CatalogueResponseDto catalogue : catalogues) {
                WhatsAppMessageRequestDto.Interactive.Action.Section.Row row = new WhatsAppMessageRequestDto.Interactive.Action.Section.Row();
                row.setId("catalogue_" + catalogue.getCatalogueId());
                row.setTitle(catalogue.getCatalogueName());
                row.setDescription(catalogue.getCatalogueDescription());
                rows.add(row);
            }

            section.setRows(rows);
            sections.add(section);

            return sendListMessage(
                    to,
                    "Our Catalogues",
                    "Please select a catalogue to browse:",
                    "Tap to select a catalogue",
                    "View Catalogues",
                    sections
            );
        } catch (Exception e) {
            log.error("Error sending catalogue list: {}", e.getMessage(), e);
            return sendTextMessage(to, "Sorry, I couldn't load the catalogues right now. Please try again later.");
        }
    }

    @Override
    public boolean sendCategoryList(String to, Integer catalogueId) {
        try {
            CatalogueResponseDto catalogue = catalogueService.getCatalogueById(catalogueId);

            // Get categories for this catalogue
            // This uses the extension service to get categories by catalogue ID
            List<CategoryResponseDto> categories = catalogueService.getCategoriesByCatalogueId(catalogueId);

            if (categories.isEmpty()) {
                return sendTextMessage(to, "No categories found in the " + catalogue.getCatalogueName() + " catalogue.\n\nType 'back' to return to catalogues.");
            }

            // Create a list message with categories
            List<WhatsAppMessageRequestDto.Interactive.Action.Section> sections = new ArrayList<>();
            WhatsAppMessageRequestDto.Interactive.Action.Section section = new WhatsAppMessageRequestDto.Interactive.Action.Section();
            section.setTitle("Categories in " + catalogue.getCatalogueName());

            List<WhatsAppMessageRequestDto.Interactive.Action.Section.Row> rows = new ArrayList<>();

            for (CategoryResponseDto category : categories) {
                WhatsAppMessageRequestDto.Interactive.Action.Section.Row row = new WhatsAppMessageRequestDto.Interactive.Action.Section.Row();
                row.setId("category_" + category.getCategoryId());
                row.setTitle(category.getCategoryName());
                row.setDescription(category.getCategoryDescription());
                rows.add(row);
            }

            section.setRows(rows);
            sections.add(section);

            // Add navigation options
            WhatsAppMessageRequestDto.Interactive.Action.Section backSection = new WhatsAppMessageRequestDto.Interactive.Action.Section();
            backSection.setTitle("Navigation");

            List<WhatsAppMessageRequestDto.Interactive.Action.Section.Row> backRows = new ArrayList<>();
            WhatsAppMessageRequestDto.Interactive.Action.Section.Row backRow = new WhatsAppMessageRequestDto.Interactive.Action.Section.Row();
            backRow.setId("back_to_catalogues");
            backRow.setTitle("Back to Catalogues");
            backRow.setDescription("Return to catalogue list");
            backRows.add(backRow);

            backSection.setRows(backRows);
            sections.add(backSection);

            return sendListMessage(
                    to,
                    catalogue.getCatalogueName(),
                    "Please select a category to browse:",
                    "Tap to select a category or type 'back' to return to catalogues",
                    "View Categories",
                    sections
            );
        } catch (Exception e) {
            log.error("Error sending category list: {}", e.getMessage(), e);
            return sendTextMessage(to, "Sorry, I couldn't load the categories right now. Please try again later.");
        }
    }

    @Override
    public boolean sendProductList(String to, Integer categoryId) {
        try {
            CategoryResponseDto category = categoryService.getCategoryById(categoryId);

            // Get products for this category
            List<ProductResponseDto> products = category.getProducts();

            if (products == null || products.isEmpty()) {
                return sendTextMessage(to, "No products found in the " + category.getCategoryName() + " category.\n\nType 'back' to return to categories.");
            }

            StringBuilder message = new StringBuilder("*Products in " + category.getCategoryName() + "*\n\n");

            for (ProductResponseDto product : products) {
                message.append("*").append(product.getProductId()).append("*. ")
                        .append(product.getProductName()).append("\n")
                        .append(product.getProductDescription()).append("\n");

                // Add price information if available
                if (product.getPrices() != null && !product.getPrices().isEmpty()) {
                    message.append("Price: ");
                    product.getPrices().forEach(price ->
                            message.append(price.getCurrencySymbol())
                                    .append(price.getPriceAmount())
                                    .append(" ")
                    );
                    message.append("\n");
                }

                message.append("\n");
            }

            message.append("Type 'back' to return to categories.");

            return sendTextMessage(to, message.toString());
        } catch (Exception e) {
            log.error("Error sending product list: {}", e.getMessage(), e);
            return sendTextMessage(to, "Sorry, I couldn't load the products right now. Please try again later.");
        }
    }

    /**
     * Send an interactive message with buttons
     */
    private boolean sendButtonMessage(String to, String headerText, String bodyText, String footerText, List<WhatsAppMessageRequestDto.Interactive.Button> buttons) {
        try {
            WhatsAppMessageRequestDto messageRequest = new WhatsAppMessageRequestDto();
            messageRequest.setMessagingProduct("whatsapp");
            messageRequest.setRecipientType("individual");
            messageRequest.setTo(to);
            messageRequest.setType("interactive");

            WhatsAppMessageRequestDto.Interactive interactive = new WhatsAppMessageRequestDto.Interactive();
            interactive.setType("button");

            if (headerText != null) {
                WhatsAppMessageRequestDto.Interactive.Header header = new WhatsAppMessageRequestDto.Interactive.Header();
                header.setType("text");
                header.setText(headerText);
                interactive.setHeader(header);
            }

            WhatsAppMessageRequestDto.Interactive.Body body = new WhatsAppMessageRequestDto.Interactive.Body();
            body.setText(bodyText);
            interactive.setBody(body);

            if (footerText != null) {
                WhatsAppMessageRequestDto.Interactive.Footer footer = new WhatsAppMessageRequestDto.Interactive.Footer();
                footer.setText(footerText);
                interactive.setFooter(footer);
            }

            interactive.setButtons(buttons);
            messageRequest.setInteractive(interactive);

            return sendRequest(messageRequest);
        } catch (Exception e) {
            log.error("Error sending button message: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Send an interactive message with a list of options
     */
    private boolean sendListMessage(String to, String headerText, String bodyText, String footerText, String buttonText, List<WhatsAppMessageRequestDto.Interactive.Action.Section> sections) {
        try {
            WhatsAppMessageRequestDto messageRequest = new WhatsAppMessageRequestDto();
            messageRequest.setMessagingProduct("whatsapp");
            messageRequest.setRecipientType("individual");
            messageRequest.setTo(to);
            messageRequest.setType("interactive");

            WhatsAppMessageRequestDto.Interactive interactive = new WhatsAppMessageRequestDto.Interactive();
            interactive.setType("list");

            if (headerText != null) {
                WhatsAppMessageRequestDto.Interactive.Header header = new WhatsAppMessageRequestDto.Interactive.Header();
                header.setType("text");
                header.setText(headerText);
                interactive.setHeader(header);
            }

            WhatsAppMessageRequestDto.Interactive.Body body = new WhatsAppMessageRequestDto.Interactive.Body();
            body.setText(bodyText);
            interactive.setBody(body);

            if (footerText != null) {
                WhatsAppMessageRequestDto.Interactive.Footer footer = new WhatsAppMessageRequestDto.Interactive.Footer();
                footer.setText(footerText);
                interactive.setFooter(footer);
            }

            WhatsAppMessageRequestDto.Interactive.Action action = new WhatsAppMessageRequestDto.Interactive.Action();
            action.setButton(buttonText);
            action.setSections(sections);
            interactive.setAction(action);

            messageRequest.setInteractive(interactive);

            return sendRequest(messageRequest);
        } catch (Exception e) {
            log.error("Error sending list message: {}", e.getMessage(), e);
            return false;
        }
    }

    private boolean sendRequest(WhatsAppMessageRequestDto messageRequest) throws IOException {
        try {
            String url = whatsAppConfiguration.getApiUrl() + "/" + whatsAppConfiguration.getPhoneNumberId() + "/messages";

            // Manually create a JSON object to ensure all required fields are included
            JSONObject jsonRequest = new JSONObject();
            jsonRequest.put("messaging_product", "whatsapp"); // Ensure this is explicitly included
            jsonRequest.put("recipient_type", messageRequest.getRecipientType());
            jsonRequest.put("to", messageRequest.getTo());
            jsonRequest.put("type", messageRequest.getType());

            if (messageRequest.getText() != null) {
                JSONObject textObj = new JSONObject();
                textObj.put("body", messageRequest.getText().getBody());
                jsonRequest.put("text", textObj);
            }

            if (messageRequest.getInteractive() != null) {
                JSONObject interactiveObj = new JSONObject();
                interactiveObj.put("type", messageRequest.getInteractive().getType());

                // Convert header
                if (messageRequest.getInteractive().getHeader() != null) {
                    JSONObject headerObj = new JSONObject();
                    headerObj.put("type", messageRequest.getInteractive().getHeader().getType());
                    headerObj.put("text", messageRequest.getInteractive().getHeader().getText());
                    interactiveObj.put("header", headerObj);
                }

                // Convert body
                if (messageRequest.getInteractive().getBody() != null) {
                    JSONObject bodyObj = new JSONObject();
                    bodyObj.put("text", messageRequest.getInteractive().getBody().getText());
                    interactiveObj.put("body", bodyObj);
                }

                // Convert footer
                if (messageRequest.getInteractive().getFooter() != null) {
                    JSONObject footerObj = new JSONObject();
                    footerObj.put("text", messageRequest.getInteractive().getFooter().getText());
                    interactiveObj.put("footer", footerObj);
                }

                // Convert action for lists
                if (messageRequest.getInteractive().getAction() != null) {
                    JSONObject actionObj = new JSONObject();
                    actionObj.put("button", messageRequest.getInteractive().getAction().getButton());

                    if (messageRequest.getInteractive().getAction().getSections() != null) {
                        JSONArray sectionsArray = new JSONArray();
                        for (WhatsAppMessageRequestDto.Interactive.Action.Section section : messageRequest.getInteractive().getAction().getSections()) {
                            JSONObject sectionObj = new JSONObject();
                            sectionObj.put("title", section.getTitle());

                            JSONArray rowsArray = new JSONArray();
                            for (WhatsAppMessageRequestDto.Interactive.Action.Section.Row row : section.getRows()) {
                                JSONObject rowObj = new JSONObject();
                                rowObj.put("id", row.getId());
                                rowObj.put("title", row.getTitle());
                                if (row.getDescription() != null) {
                                    rowObj.put("description", row.getDescription());
                                }
                                rowsArray.put(rowObj);
                            }

                            sectionObj.put("rows", rowsArray);
                            sectionsArray.put(sectionObj);
                        }
                        actionObj.put("sections", sectionsArray);
                    }

                    interactiveObj.put("action", actionObj);
                }

                // Convert buttons
                if (messageRequest.getInteractive().getButtons() != null) {
                    JSONArray buttonsArray = new JSONArray();
                    for (WhatsAppMessageRequestDto.Interactive.Button button : messageRequest.getInteractive().getButtons()) {
                        JSONObject buttonObj = new JSONObject();
                        buttonObj.put("type", button.getType());

                        if (button.getReply() != null) {
                            JSONObject replyObj = new JSONObject();
                            replyObj.put("id", button.getReply().getId());
                            replyObj.put("title", button.getReply().getTitle());
                            buttonObj.put("reply", replyObj);
                        }

                        buttonsArray.put(buttonObj);
                    }
                    interactiveObj.put("buttons", buttonsArray);
                }

                jsonRequest.put("interactive", interactiveObj);
            }

            // Use the manually created JSON
            String requestBody = jsonRequest.toString();

            // Log the request for debugging
            log.debug("WhatsApp API Request: {}", requestBody);

            RequestBody body = RequestBody.create(
                    requestBody,
                    MediaType.parse("application/json")
            );

            Request request = new Request.Builder()
                    .url(url)
                    .post(body)
                    .addHeader("Authorization", "Bearer " + whatsAppConfiguration.getAccessToken())
                    .addHeader("Content-Type", "application/json")
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    String responseBody = response.body() != null ? response.body().string() : "";
                    log.debug("WhatsApp API Response: {}", responseBody);
                    return true;
                } else {
                    String responseBody = response.body() != null ? response.body().string() : "No response body";
                    log.error("Error sending message: {} - {}", response.code(), responseBody);
                    return false;
                }
            }
        } catch (Exception e) {
            log.error("Exception when sending message: {}", e.getMessage(), e);
            return false;
        }
    }
}