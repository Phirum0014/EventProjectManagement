import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

class Event {
    String name;
    String date;
    String location;

    public Event(String name, String date, String location) {
        this.name = name;
        this.date = date;
        this.location = location;
    }
}

class Ticket {
    String eventName;
    String username;
    int ticketNumber;
    String eventDate;
    String eventLocation;

    public Ticket(String eventName, String username, int ticketNumber, String eventDate, String eventLocation) {
        this.eventName = eventName;
        this.username = username;
        this.ticketNumber = ticketNumber;
        this.eventDate = eventDate;
        this.eventLocation = eventLocation;
    }
}

public class EventProjectManagement {
    private static ArrayList<Event> events = new ArrayList<>();
    private static HashMap<String, String> users = new HashMap<>();
    private static ArrayList<Ticket> tickets = new ArrayList<>();
    private static HashMap<String, ArrayList<String>> notifications = new HashMap<>();
    private static HashMap<String, String> userRoles = new HashMap<>(); // Stores user roles (admin/user)
    private static Scanner scanner = new Scanner(System.in);
    private static String loggedInUser = null;
    private static final String USERS_FILE = "users.txt";
    private static final String EVENTS_FILE = "events.txt";
    private static final String TICKETS_FILE = "tickets.txt";

    public static void main(String[] args) {
        loadUsers();
        loadEvents();
        loadTickets();
        while (true) {
            if (loggedInUser == null) {
                System.out.println("1. Register");
                System.out.println("2. Login");
                System.out.println("3. Exit");
                System.out.print("Enter choice: ");
                String input = scanner.nextLine();

                if (!input.matches("\\d")) {
                    System.out.println("Invalid choice. Please enter a number.");
                    continue;
                }

                int choice = Integer.parseInt(input);
                if (choice == 1) register();
                else if (choice == 2) login();
                else if (choice == 3) {
                    System.out.println("Exiting...");
                    break;
                } else {
                    System.out.println("Invalid choice.");
                }
            } else {
                System.out.println("+-------------------------+");
                System.out.println("|     1. Create Event     |");
                System.out.println("|     2. View Events      |");
                System.out.println("|     3. RSVP Event       |");
                System.out.println("|     4. View Tickets     |");
                System.out.println("|     5. Delete Event     |");
                System.out.println("|  6. View Notifications  |");
                System.out.println("|     7. Delete User      |");
                System.out.println("|       8. Log Out        |");
                System.out.println("+-------------------------+");
                System.out.print("Enter choice: ");
                String input = scanner.nextLine();

                if (!input.matches("\\d")) {
                    System.out.println("Invalid choice. Please enter a number.");
                    continue;
                }

                int choice = Integer.parseInt(input);
                if (choice == 1) createEvent();
                else if (choice == 2) viewEvents();
                else if (choice == 3) rsvpEvent();
                else if (choice == 4) viewTickets();
                else if (choice == 5) deleteEvent();
                else if (choice == 6) viewNotifications();
                else if (choice == 7) deleteUser();
                else if (choice == 8) {
                    System.out.println("Logging out...");
                    loggedInUser = null;
                } else {
                    System.out.println("Invalid choice.");
                }
            }
        }
        scanner.close();
    }

    private static void register() {
        System.out.print("Enter username: ");
        String username = scanner.nextLine();
        if (users.containsKey(username)) {
            System.out.println("Username already exists.");
            return;
        }

        System.out.print("Enter password: ");
        String password = scanner.nextLine();

        System.out.print("Are you registering as an admin? (yes/no): ");
        String roleInput = scanner.nextLine().toLowerCase();
        String role = roleInput.equals("yes") ? "admin" : "user";

        users.put(username, hashPassword(password));
        userRoles.put(username, role);

        saveUsers();
        System.out.println("Registered successfully as " + role + ". Please log in.");
    }

    private static void login() {
        System.out.print("Enter username: ");
        String username = scanner.nextLine();
        System.out.print("Enter password: ");
        String password = scanner.nextLine();

        if (users.containsKey(username) && users.get(username).equals(hashPassword(password))) {
            System.out.println("Login successful!");
            loggedInUser = username;
        } else {
            System.out.println("Invalid credentials.");
        }
    }

private static void createEvent() {
    System.out.print("Enter event name: ");
    String name = scanner.nextLine();

    LocalDate eventDate;
    while (true) {
        System.out.print("Enter event date (YYYY-MM-DD): ");
        String dateInput = scanner.nextLine();
        
        try {
            eventDate = LocalDate.parse(dateInput, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            if (eventDate.isBefore(LocalDate.now())) {
                System.out.println("Invalid date. You cannot create an event in the past. Please enter a future date.");
            } else {
                break; // Valid date, exit loop
            }
        } catch (DateTimeParseException e) {
            System.out.println("Invalid date format. Please enter the date in YYYY-MM-DD format.");
        }
    }

    System.out.print("Enter event location: ");
    String location = scanner.nextLine();

    events.add(new Event(name, eventDate.toString(), location));
    saveEvents();
    System.out.println("Event created successfully.");
    notifyUsers(name, eventDate.toString(), location);
}

    private static void viewEvents() {
        if (events.isEmpty()) {
            System.out.println("No events available.");
        } else {
            System.out.println("+----+----------------------+------------+----------------------+");
            System.out.println("| No | Event Name           | Date       | Location             |");
            System.out.println("+----+----------------------+------------+----------------------+");
            int index = 1;
            for (Event event : events) {
                System.out.printf("| %-2d | %-20s | %-10s | %-20s |%n", index++, event.name, event.date, event.location);
            }
            System.out.println("+----+----------------------+------------+----------------------+");
        }
    }

    private static void rsvpEvent() {
        System.out.print("Enter event number to RSVP: ");
        String input = scanner.nextLine();

        if (!input.matches("\\d+")) {
            System.out.println("Invalid input. Please enter a number.");
            return;
        }

        int eventNumber = Integer.parseInt(input);
        if (eventNumber < 1 || eventNumber > events.size()) {
            System.out.println("Invalid event number.");
            return;
        }

        Event event = events.get(eventNumber - 1);
    LocalDate eventDate = LocalDate.parse(event.date, DateTimeFormatter.ofPattern("yyyy-MM-dd"));

    if (eventDate.isBefore(LocalDate.now())) {
        System.out.println("You cannot RSVP for a past event.");
        return;
    }

    int ticketNumber = tickets.size() + 1;
    tickets.add(new Ticket(event.name, loggedInUser, ticketNumber, event.date, event.location));
    saveTickets();
    System.out.println("RSVP successful. Your ticket number is " + ticketNumber + ". Event: " + event.name + " on " + event.date + " at " + event.location);
    }

    private static void viewTickets() {
        System.out.println("+----+----------------------+------------+----------------------+----------------------+");
        System.out.println("| No | Event Name           | Date       | Location             | Ticket Number        |");
        System.out.println("+----+----------------------+------------+----------------------+----------------------+");
        int index = 1;
        for (Ticket ticket : tickets) {
            if (ticket.username.equals(loggedInUser)) {
                System.out.printf("| %-2d | %-20s | %-10s | %-20s | %-20d |%n", index++, ticket.eventName, ticket.eventDate, ticket.eventLocation, ticket.ticketNumber);
            }
        }
        System.out.println("+----+----------------------+------------+----------------------+----------------------+");
    }

    private static void deleteEvent() {
        System.out.print("Enter event number to delete: ");
        String input = scanner.nextLine();

        if (!input.matches("\\d+")) {
            System.out.println("Invalid input. Please enter a number.");
            return;
        }

        int eventNumber = Integer.parseInt(input);
        if (eventNumber < 1 || eventNumber > events.size()) {
            System.out.println("Invalid event number.");
            return;
        }

        Event event = events.remove(eventNumber - 1);
        saveEvents();

        // Remove tickets associated with the event
        tickets.removeIf(ticket -> ticket.eventName.equals(event.name));
        saveTickets();

        System.out.println("Event and associated tickets deleted successfully.");
    }

    private static void deleteUser() {
        if (loggedInUser == null) {
            System.out.println("You must be logged in to delete a user.");
            return;
        }

        if (userRoles.get(loggedInUser).equals("admin")) {
            System.out.print("Enter the username of the user to delete: ");
            String usernameToDelete = scanner.nextLine();

            if (!users.containsKey(usernameToDelete)) {
                System.out.println("User not found.");
                return;
            }

            users.remove(usernameToDelete);
            userRoles.remove(usernameToDelete);
            tickets.removeIf(ticket -> ticket.username.equals(usernameToDelete));

            saveUsers();
            saveTickets();

            System.out.println("User " + usernameToDelete + " and their associated tickets have been deleted.");
        } else {
            System.out.println("You do not have permission to delete other users.");
            System.out.print("Do you want to delete your own account? (yes/no): ");
            String confirmation = scanner.nextLine().toLowerCase();

            if (confirmation.equals("yes")) {
                users.remove(loggedInUser);
                userRoles.remove(loggedInUser);
                tickets.removeIf(ticket -> ticket.username.equals(loggedInUser));

                saveUsers();
                saveTickets();

                System.out.println("Your account has been deleted.");
                loggedInUser = null;
            } else {
                System.out.println("Account deletion canceled.");
            }
        }
    }

    private static String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashedBytes = md.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hashedBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Hashing error occurred.");
        }
    }

    private static void saveUsers() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(USERS_FILE))) {
            for (String username : users.keySet()) {
                writer.write(username + "," + users.get(username) + "," + userRoles.get(username));
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error saving users: " + e.getMessage());
        }
    }

    private static void loadUsers() {
        try (BufferedReader reader = new BufferedReader(new FileReader(USERS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 3) {
                    users.put(parts[0], parts[1]);
                    userRoles.put(parts[0], parts[2]);
                    if (!notifications.containsKey(parts[0])) {
                        notifications.put(parts[0], new ArrayList<>());
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Error loading users: " + e.getMessage());
        }
    }

    private static void saveEvents() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(EVENTS_FILE))) {
            for (Event event : events) {
                writer.write(event.name + "," + event.date + "," + event.location);
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error saving events: " + e.getMessage());
        }
    }

    private static void loadEvents() {
        try (BufferedReader reader = new BufferedReader(new FileReader(EVENTS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 3) {
                    events.add(new Event(parts[0], parts[1], parts[2]));
                }
            }
        } catch (IOException e) {
            System.out.println("Error loading events: " + e.getMessage());
        }
    }

    private static void saveTickets() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(TICKETS_FILE))) {
            for (Ticket ticket : tickets) {
                writer.write(ticket.eventName + "," + ticket.username + "," + ticket.ticketNumber + "," + ticket.eventDate + "," + ticket.eventLocation);
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error saving tickets: " + e.getMessage());
        }
    }

    private static void loadTickets() {
        try (BufferedReader reader = new BufferedReader(new FileReader(TICKETS_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 5) {
                    tickets.add(new Ticket(parts[0], parts[1], Integer.parseInt(parts[2]), parts[3], parts[4]));
                }
            }
        } catch (IOException e) {
            System.out.println("Error loading tickets: " + e.getMessage());
        }
    }

    private static void notifyUsers(String eventName, String eventDate, String eventLocation) {
        for (String username : users.keySet()) {
            String notification = "New event created - " + eventName + " on " + eventDate + " at " + eventLocation;
            notifications.get(username).add(notification);
            System.out.println("Notification sent to " + username + ": " + notification);
        }
    }

    private static void viewNotifications() {
        ArrayList<String> userNotifications = notifications.get(loggedInUser);
        if (userNotifications.isEmpty()) {
            System.out.println("No notifications.");
        } else {
            System.out.println("+-------------------------+");
            System.out.println("|      Notifications      |");
            System.out.println("+-------------------------+");
            for (String notification : userNotifications) {
                System.out.println("| " + notification);
            }
            System.out.println("+-------------------------+");
        }
    }
}
