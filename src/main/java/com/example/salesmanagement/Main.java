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
import javafx.scene.control.Alert;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
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
                Logger.getLogger(Main.class.getName()).log(Level.INFO, "Trying to connect to the database server");
                HibernateUtil.getSessionFactory();
                return null;
            }
        };

        // ExecutorService to schedule timeout
        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

        // Schedule a timeout task that will run after 15 seconds
        Future<?> timeoutFuture = executorService.schedule(() -> {
            if (!task.isDone()) {
                task.cancel(true); // Cancel the task if it's still running

                Platform.runLater(() -> {
                    // Hide the splash screen before displaying the alert
                    stageSplash.close();

                    // Display the alert after hiding the splash screen
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Délai de connexion dépassé");
                    alert.setHeaderText("Échec de la connexion à la base de données");
                    alert.setContentText("La connexion à la base de données n'a pas pu être établie. Veuillez suivre ces étapes pour diagnostiquer le problème :\n\n"
                            + "1. Assurez-vous que le serveur de base de données est en cours d'exécution.\n"
                            + "2. Vérifiez les paramètres de connexion (adresse, port, nom d'utilisateur, mot de passe).\n"
                            + "3. Consultez les journaux pour plus de détails sur l'erreur.\n\n"
                            + "Si le problème persiste, contactez votre administrateur.");
                    alert.showAndWait(); // Wait for the user to dismiss the alert

                    // Exit the application after the alert is dismissed
                    Platform.exit();
                    System.exit(0);
                });

                Logger.getLogger(Main.class.getName()).log(Level.WARNING, "Database connection timeout");
            }
        }, 15, TimeUnit.SECONDS);

        // Handle the task completion or failure
        task.setOnSucceeded(e -> {
            Logger.getLogger(Main.class.getName()).log(Level.INFO, "Connection to the database server succeeded");
            Logger.getLogger(Main.class.getName()).log(Level.INFO, "Hibernate initialized successfully");
            timeoutFuture.cancel(false); // Cancel the timeout if task succeeds

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

                stageSplash.close(); // Close the splash screen after the login form is ready
                stage.show();
            });
        });

        task.setOnFailed(e -> {
            timeoutFuture.cancel(false); // Cancel the timeout if task fails

            Throwable ex = task.getException();
            ex.printStackTrace();

            Platform.runLater(() -> {
                // Hide the splash screen before displaying the alert
                stageSplash.close();

                // Display the alert after hiding the splash screen
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Initialization Failed");
                alert.setHeaderText("Hibernate Initialization Error");
                alert.setContentText("Failed to initialize Hibernate. Please check the logs.");
                alert.showAndWait(); // Wait for the user to dismiss the alert

                // Exit the application after the alert is dismissed
                Platform.exit();
                System.exit(0);
            });

            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, "Hibernate initialization failed", ex);
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
