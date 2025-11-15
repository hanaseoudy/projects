package view.stages;

import ai.AIHandler;
import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;
import multiplayer.packet.ChatPacket;
import multiplayer.packet.Packet;
import multiplayer.packet.PacketType;
import view.middleware.Middleware;
import view.middleware.frontend.JackarooController;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class ChatBox extends VBox {
    private static ChatBox instance;
    private ScrollPane scrollPane;
    private VBox messagesContainer;
    private HBox inputContainer;
    private TextField messageInput;
    private Button sendButton;
    private boolean isManuallyOpen = false;
    private boolean isVisible = false;
    private PauseTransition autoHideTransition;
    private FadeTransition fadeInTransition;
    private FadeTransition fadeOutTransition;
    
    // List to store chat messages
    private List<ChatMessage> chatMessages;
    
    // Message class to store message data
    private static class ChatMessage {
        String username;
        String message;
        LocalDateTime timestamp;
        
        public ChatMessage(String username, String message) {
            this.username = username;
            this.message = message;
            this.timestamp = LocalDateTime.now();
        }
    }
    
    public ChatBox() {
        instance = this;
        chatMessages = new ArrayList<>();
        initializeUI();
        setupAnimations();

        // Initially hidden
        setVisible(false);
        setOpacity(0);
    }
    
    public static ChatBox getInstance() {
        if (instance == null) {
            instance = new ChatBox();
        }
        return instance;
    }
    
    private void initializeUI() {
        // Set up the main container
        setMaxWidth(350);
        setPrefWidth(350);
        setMaxHeight(400);
        setPrefHeight(400);
        setSpacing(5);
        setPadding(new Insets(10));
        
        // Style the chat box
        setStyle("-fx-background-color: rgba(0, 0, 0, 0.8); " +
                "-fx-background-radius: 10; " +
                "-fx-border-color: #444444; " +
                "-fx-border-width: 1; " +
                "-fx-border-radius: 10;");
        
        // Title
        Label titleLabel = new Label("Chat");
        titleLabel.setTextFill(Color.WHITE);
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
        
        // Messages container
        messagesContainer = new VBox();
        messagesContainer.setSpacing(5);
        messagesContainer.setPadding(new Insets(5));
        
        // Scroll pane for messages
        scrollPane = new ScrollPane(messagesContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setStyle("-fx-background: transparent; " +
                           "-fx-background-color: rgba(255, 255, 255, 0.1); " +
                           "-fx-background-radius: 5;");
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
        
        // Input container
        inputContainer = new HBox();
        inputContainer.setSpacing(5);
        inputContainer.setAlignment(Pos.CENTER);
        
        // Message input field
        messageInput = new TextField();
        messageInput.setPromptText("Type your message...");
        messageInput.setStyle("-fx-background-color: rgba(255, 255, 255, 0.9); " +
                             "-fx-background-radius: 5; " +
                             "-fx-text-fill: black;");
        HBox.setHgrow(messageInput, Priority.ALWAYS);
        
        // Send button
        sendButton = new Button("Send");
        sendButton.setStyle("-fx-background-color: #4CAF50; " +
                           "-fx-text-fill: white; " +
                           "-fx-background-radius: 5; " +
                           "-fx-font-weight: bold;");
        
        // Add components to input container
        inputContainer.getChildren().addAll(messageInput, sendButton);
        
        // Add all components to main container
        getChildren().addAll(titleLabel, scrollPane, inputContainer);
        
        // Setup event handlers
        setupEventHandlers();
    }
    
    private void setupEventHandlers() {
        // Send message on button click
        sendButton.setOnAction(e -> sendMessage());
        
        // Send message on Enter key
        messageInput.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                sendMessage();
            }
        });
        
        // Handle escape key to close chat
        setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ESCAPE) {
                hideChatBox();
            }
        });
        
        // Focus on input when chat opens
        visibleProperty().addListener((obs, wasVisible, isNowVisible) -> {
            if (isNowVisible && isManuallyOpen) {
                Platform.runLater(() -> messageInput.requestFocus());
            }
        });
    }
    
    private void setupAnimations() {
        // Fade in animation
        fadeInTransition = new FadeTransition(Duration.millis(300), this);
        fadeInTransition.setFromValue(0);
        fadeInTransition.setToValue(1);
        
        // Fade out animation
        fadeOutTransition = new FadeTransition(Duration.millis(300), this);
        fadeOutTransition.setFromValue(1);
        fadeOutTransition.setToValue(0);
        fadeOutTransition.setOnFinished(e -> {
            setVisible(false);
            isVisible = false;
        });
        
        // Auto-hide transition (5 seconds)
        autoHideTransition = new PauseTransition(Duration.seconds(5));
        autoHideTransition.setOnFinished(e -> {
            if (!isManuallyOpen) {
                fadeOut();
            }
        });
    }
    
    private void sendMessage() {
        String message = messageInput.getText().trim();
        if (!message.isEmpty()) {
            addMessage("You", message);
            messageInput.clear();

            if (JackarooController.room != null) {
                try {
                    JackarooController.room.broadcast(new Packet(
                            new ChatPacket(JackarooController.room.getUsername(), message),
                            PacketType.CHAT
                    ));
                } catch (IOException e) {
                    Platform.runLater(() -> new JackarooError(e.getMessage()));
                }
            } else if (JackarooController.multiplayerHandler != null) {
                JackarooController.multiplayerHandler.sendChatMessage(message);
            } else {
                new Thread(() -> {
                    final String messages = AIHandler.getInstance().respond(message);
                    addAIMessages(messages);
                }).start();
            }
        }
    }

    private void addAIMessages(final String messages) {
        if (!messages.contains("\n")) {
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    final String[] split = messages.split(": ");
                    addMessage(split[0], split[1]);
                }
            }, new Random().nextInt(4500) + 500);
            return;
        }

        final String[] messagesSplit = messages.split("\n");
        new Thread(() -> {
            for (final String message : messagesSplit) {
                final String[] split = message.split(": ");
                addMessage(split[0], split[1]);

                try {
                    Thread.sleep(new Random().nextInt(4500) + 500);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();

    }

    public void addMessage(String username, String message) {
        Platform.runLater(() -> {
            ChatMessage chatMessage = new ChatMessage(username, message);
            chatMessages.add(chatMessage);
            
            // Create message UI element
            VBox messageBox = createMessageElement(chatMessage);
            messagesContainer.getChildren().add(messageBox);
            
            // Auto-scroll to bottom
            Platform.runLater(() -> scrollPane.setVvalue(1.0));
            
            // Show chat box temporarily if not manually open
            if (!isManuallyOpen) {
                showChatBoxTemporarily();
            }
        });
    }
    
    private VBox createMessageElement(ChatMessage chatMessage) {
        VBox messageBox = new VBox();
        messageBox.setSpacing(2);
        
        // Username and timestamp
        HBox headerBox = new HBox();
        headerBox.setSpacing(10);
        headerBox.setAlignment(Pos.CENTER_LEFT);
        
        Label usernameLabel = new Label(chatMessage.username);
        usernameLabel.setTextFill(Color.LIGHTBLUE);
        usernameLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
        
        Label timestampLabel = new Label(chatMessage.timestamp.format(DateTimeFormatter.ofPattern("HH:mm")));
        timestampLabel.setTextFill(Color.LIGHTGRAY);
        timestampLabel.setFont(Font.font("System", 10));
        
        headerBox.getChildren().addAll(usernameLabel, timestampLabel);
        
        // Message content
        Label messageLabel = new Label(chatMessage.message);
        messageLabel.setTextFill(Color.WHITE);
        messageLabel.setFont(Font.font("System", 12));
        messageLabel.setWrapText(true);
        
        messageBox.getChildren().addAll(headerBox, messageLabel);
        
        // Add some styling based on message type
        if (chatMessage.username.equals("System")) {
            messageLabel.setTextFill(Color.YELLOW);
            usernameLabel.setTextFill(Color.ORANGE);
        } else if (chatMessage.username.equals("You")) {
            messageLabel.setTextFill(Color.LIGHTGREEN);
            usernameLabel.setTextFill(Color.GREEN);
        }
        
        return messageBox;
    }
    
    public void toggleChatBox() {
        if (isVisible) {
            hideChatBox();
        } else {
            showChatBoxManually();
        }
    }
    
    public void showChatBoxManually() {
        isManuallyOpen = true;
        if (autoHideTransition != null) {
            autoHideTransition.stop();
        }
        fadeIn();
        Platform.runLater(() -> messageInput.requestFocus());
    }
    
    public void hideChatBox() {
        isManuallyOpen = false;
        if (autoHideTransition != null) {
            autoHideTransition.stop();
        }
        fadeOut();
    }
    
    private void showChatBoxTemporarily() {
        if (!isVisible && !isManuallyOpen) {
            fadeIn();
            autoHideTransition.playFromStart();
        }
    }
    
    private void fadeIn() {
        if (!isVisible) {
            setVisible(true);
            isVisible = true;
            fadeInTransition.playFromStart();
        }
    }
    
    private void fadeOut() {
        if (isVisible) {
            fadeOutTransition.playFromStart();
        }
    }
    
    // Method to clear all messages
    public void clearMessages() {
        Platform.runLater(() -> {
            chatMessages.clear();
            messagesContainer.getChildren().clear();
        });
    }
    
    // Method to get all messages (for saving/loading)
    public List<String> getAllMessages() {
        return chatMessages.stream()
                .map(msg -> String.format("[%s] %s: %s", 
                    msg.timestamp.format(DateTimeFormatter.ofPattern("HH:mm")),
                    msg.username, 
                    msg.message))
                .toList();
    }
}