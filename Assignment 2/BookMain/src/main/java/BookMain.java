
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;


public class BookMain {

    static final String BASE_URL = "http://localhost:8080/books";
    static ObjectMapper mapper = new ObjectMapper();
    static Scanner scanner = new Scanner(System.in);

    static String readNonEmpty(String fieldName) {
        while (true) {
            System.out.print(fieldName + ": ");
            String input = scanner.nextLine().trim();

            if (!input.isEmpty()) {
                return input;
            }

            System.out.println("⚠ " + fieldName + " cannot be empty.");
        }
    }

    static String readDescription() {
        while (true) {
            System.out.print("Description (max 500 chars): ");
            String input = scanner.nextLine();

            if (input.length() <= 500) {
                return input;
            }

            System.out.println("⚠ Description too long. Max 500 characters.");
        }
    }

    public static void main(String[] args) throws Exception {
        while (true) {
            System.out.println("""
                \n--- BOOK CLIENT ---
                1. Get all books
                2. Get book by id
                3. Get books by category
                4. Create a book
                5. Delete a book
                6. Exit
                """);

            System.out.print("Choose option: ");

            int option;

            if (scanner.hasNextInt()) {
                option = scanner.nextInt();
                scanner.nextLine(); // limpiar buffer
            } else {
                System.out.println("⚠ Please enter a valid number.");
                scanner.nextLine(); // descartar entrada inválida
                continue; // vuelve al menú
            }

            try {
                switch (option) {
                    case 1 -> getAllBooks();
                    case 2 -> getBookById();
                    case 3 -> getBooksByCategory();
                    case 4 -> createBook();
                    case 5 -> deleteBook();
                    case 6 -> System.exit(0);
                    default -> System.out.println("Invalid option");
                }
            } catch (Exception e) {
                System.out.println("⚠ Operation failed: " + e.getMessage());
            }
        }
    }

    static void getAllBooks() throws Exception {
        sendGet(BASE_URL);
    }

    static void getBookById() throws Exception {
        System.out.print("Enter book id: ");
        String id = scanner.nextLine();
        sendGet(BASE_URL + "/" + id);
    }

    static void getBooksByCategory() throws Exception {
        System.out.print("Enter category (IT, MATH, PHYSICS): ");
        String cat = scanner.nextLine();
        sendGet(BASE_URL + "/category/" + cat);
    }

    static void createBook() throws Exception {
        String title = readNonEmpty("Title");
        String desc = readDescription();
        String author = readNonEmpty("Author");

        System.out.print("Year: ");
        String year = scanner.nextLine();

        System.out.print("Category (IT, MATH, PHYSICS): ");
        String category = scanner.nextLine();

        String json = """
            {
              "title": "%s",
              "description": "%s",
              "publishedYear": "%s",
              "author": "%s",
              "category": "%s"
            }
            """.formatted(title, desc, year, author, category);

        sendPost(json);
    }

    static void deleteBook() throws Exception {
        System.out.print("Enter book id to delete: ");
        String id = scanner.nextLine();

        URL url = new URL(BASE_URL + "/" + id);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("DELETE");

        System.out.println("Response code: " + con.getResponseCode());
    }

    static void sendGet(String urlStr) throws Exception {
        URL url = new URL(urlStr);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");

        printResponse(con);
    }

    static void sendPost(String json) throws Exception {
        URL url = new URL(BASE_URL);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/json");
        con.setDoOutput(true);

        try (OutputStream os = con.getOutputStream()) {
            os.write(json.getBytes());
        }

        printResponse(con);
    }

    static void printResponse(HttpURLConnection con) {
        try {
            int code = con.getResponseCode();
            System.out.println("Response code: " + code);

            InputStream stream = (code >= 400)
                    ? con.getErrorStream()
                    : con.getInputStream();

            if (stream == null) return;

            // Leer todo el JSON
            String response = new BufferedReader(new InputStreamReader(stream))
                    .lines()
                    .reduce("", (a, b) -> a + b);

            // Pretty print JSON
            Object json = mapper.readValue(response, Object.class);
            String prettyJson = mapper.writerWithDefaultPrettyPrinter()
                    .writeValueAsString(json);

            System.out.println(prettyJson);

        } catch (Exception e) {
            System.out.println("⚠ Error: " + e.getMessage());
        }
    }


}