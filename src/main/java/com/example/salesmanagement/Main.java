package com.example.salesmanagement;

import com.example.salesmanagement.user.Encryptor;
import com.example.salesmanagement.user.Role;
import com.example.salesmanagement.user.User;
import com.example.salesmanagement.user.UserRepository;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main extends Application {
    public static void main(String[] args) {
        Logger.getLogger(Main.class.getName()).log(Level.INFO, "Starting Application");
        launch();
    }

    @Override
    public void start(Stage stage) throws IOException {
        // Load the splash screen layout from the FXML file
        AnchorPane anchorPane = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("splash-screen.fxml")));
        Scene sceneSplash = new Scene(anchorPane);
        Stage stageSplash = new Stage(StageStyle.UNDECORATED);

        // Task to initialize Hibernate in the background
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() {
                Logger.getLogger(Main.class.getName()).log(Level.INFO, "Initializing Hibernate");
                HibernateUtil.getSessionFactory();
                return null;
            }
        };

        // Handle the task completion or failure
        task.setOnSucceeded(e -> {
            Logger.getLogger(Main.class.getName()).log(Level.INFO, "Hibernate initialized successfully");

            // check for admin default user
            Logger.getLogger(Main.class.getName()).log(Level.INFO, "Checking for ADMIN user");
            List<User> admins = UserRepository.findAllByRole(Role.ADMIN);
            if (admins.isEmpty()) {
                User adminUser = new User();
                adminUser.setUsername("admin");
                adminUser.setFirstName("admin");
                adminUser.setLastName("admin");
                adminUser.setPassword(Encryptor.encryptPassword("admin"));
                adminUser.setRole(Role.ADMIN);

                UserRepository.save(adminUser);

                Logger.getLogger(Main.class.getName()).log(Level.INFO, "Default ADMIN User created successfully", adminUser);
            }

            Platform.runLater(() -> {
                stage.setResizable(false);
                stage.setTitle("Gestion Commercial");

                stage.setOnCloseRequest(event -> {
                    HibernateUtil.shutdown();
                    stage.close();
                });

                try {
                    VBox pane = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("form-login.fxml")));
                    Scene scene = new Scene(pane);
                    stage.setScene(scene);
                } catch (IOException ex) {
                    ex.printStackTrace();
                    // Display error dialog or handle gracefully
                }

                stageSplash.close();
                stage.show();
            });
        });

        task.setOnFailed(e -> {
            Throwable ex = task.getException();
            ex.printStackTrace();
            // Handle the error case, like showing an alert or logging
        });

        // Event handler for when the stage is fully rendered
        stageSplash.addEventHandler(WindowEvent.WINDOW_SHOWN, event -> {
            // Start the Hibernate initialization in a new thread
            new Thread(task).start();
        });

        // Configure stage properties
        stageSplash.setScene(sceneSplash);
        stageSplash.setResizable(false);
        stageSplash.setAlwaysOnTop(true);

        // Show the splash screen
        stageSplash.show();
    }
}
