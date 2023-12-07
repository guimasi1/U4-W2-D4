package guidom;

import com.github.javafaker.Faker;
import guidom.entities.Category;
import guidom.entities.Customer;
import guidom.entities.Order;
import guidom.entities.Product;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class Application {

    public static void main(String[] args) {
        Supplier<Integer> randomNumbers = () -> {
            Random rnd = new Random();
            return rnd.nextInt(1, 200);
        };

        Faker faker = new Faker(Locale.ITALY);

        Supplier<Product> booksSupplier = () -> new Product(faker.book().title(), Category.Books.toString(), randomNumbers.get());
        Supplier<Product> babyProdsSupplier = () -> new Product(faker.pokemon().name(), Category.Baby.toString(), randomNumbers.get());
        Supplier<Product> boysProdsSupplier = () -> new Product(faker.commerce().productName(), Category.Boys.toString(), randomNumbers.get());

        // CLIENTI
        Supplier<Customer> customerSupplier = () -> new Customer("Pippo", 1);
        Supplier<Customer> customer2Supplier = () -> new Customer("Franco", 2);

        // CLIENTI TIER 1
        Customer customer1 = customerSupplier.get();
        Customer customer4 = new Customer("Leopoldo", 1);

        // CLIENTI TIER 2
        Customer customer2 = customer2Supplier.get();
        Customer customer3 = new Customer("Giacomo", 2);

        // TUTTI I CLIENTI
        List<Customer> allCustomers = new ArrayList<>();
        allCustomers.add(customer1);
        allCustomers.add(customer2);
        allCustomers.add(customer3);
        allCustomers.add((customer4));
        // PRODOTTI

        // 1. LIBRI
        List<Product> books = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            books.add(booksSupplier.get());
        }

        // 2. PER BAMBINI

        List<Product> babyProducts = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            babyProducts.add(babyProdsSupplier.get());
        }

        // 3. PER RAGAZZI

        List<Product> boysProducs = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            boysProducs.add(boysProdsSupplier.get());
        }

        // TUTTI I PRODOTTI

        List<Product> allProducts = new ArrayList<>();
        allProducts.addAll(boysProducs);
        allProducts.addAll(babyProducts);
        allProducts.addAll(books);


        LocalDate today = LocalDate.now();
        LocalDate secondFeb = LocalDate.parse("2021-02-02");
        LocalDate thirtyMarch = LocalDate.parse("2021-03-30");
        LocalDate twentyMarch = LocalDate.parse("2021-03-20");
        LocalDate orderDate1 = LocalDate.parse("2023-10-10");
        LocalDate feb1 = LocalDate.parse("2021-02-01");
        LocalDate april1 = LocalDate.parse("2021-04-01");
        Supplier<Order> deliveredOrdersBabySupplier = () -> new Order("DELIVERED", secondFeb, thirtyMarch, babyProducts, customer2);
        Order order1 = deliveredOrdersBabySupplier.get();
        Order order2 = new Order("DELIVERED", twentyMarch, thirtyMarch, boysProducs, customer3);
        Order order3 = new Order("DELIVERED", orderDate1, today, books, customer1);
        Order order4 = new Order("DELIVERED", orderDate1, today, babyProducts, customer4);
        List<Order> allOrders = new ArrayList<>();
        allOrders.add(order1);
        allOrders.add(order2);
        allOrders.add(order3);
        allOrders.add(order4);

        // ESERCIZIO 1 - raggruppare gli ordini per cliente utilizzando Stream e Lambda Expressions,
        // la mappa deve essere: chiave - cliente, valore - lista di ordini effettuati dal cliente.

        Map<Customer, List<Order>> ordersByClient = allOrders.stream().collect(Collectors.groupingBy(Order::getCustomer));
        ordersByClient.forEach((customer, list) -> System.out.println(" customer" + customer + "lista di ordini: " + list));

        // ESERCIZIO 2 - dato un elenco di ordini, calcola il totale delle vendite per ogni cliente
        // utilizzando Stream e Lambda Expressions. La mappa è cosi composta: chiave - cliente, valore - importo totale.

        Map<Customer, Double> totalPriceByClient = allOrders.stream().collect(Collectors.groupingBy(Order::getCustomer, Collectors.summingDouble(order -> order.getProducts().stream()
                .mapToDouble(Product::getPrice)
                .sum())));
        System.out.println("Clienti e prezzo totale");
        totalPriceByClient.forEach((customer, total) -> System.out.println(customer + " total: " + total));

        // ESERCIZIO 3 - dato un elenco di prodotti, trova i prodotti più costosi

        List<Product> mostExpensiveProducts = allProducts.stream().sorted(Comparator.comparingDouble(Product::getPrice).reversed()).toList();
        System.out.println("Prodotti più costosi");
        mostExpensiveProducts.forEach(System.out::println);

        // ESERCIZIO 4 - dato un elenco di ordini, calcola la media degli importi degli ordini

        double average = allOrders.stream().flatMap(order -> order.getProducts().stream()).collect(Collectors.averagingDouble(Product::getPrice));
        System.out.println("Media del costo dei prodotti all'interno degli ordini: " + average);

        // ESERCIZIO 5 - dato un elenco di prodotti, raggruppa i prodotti per categoria e calcola la somma
        // degli importi per ogni categoria.

        Map<String, List<Product>> productsByCategory = allProducts.stream().collect(Collectors.groupingBy(Product::getCategory));
        System.out.println("Prodotti divisi per categoria.");
        productsByCategory.forEach((category, products) -> System.out.println("category: " + category + "products: " + products));

        Map<String, Double> totalByCategory = allProducts.stream().collect(Collectors.groupingBy(Product::getCategory, Collectors.summingDouble(Product::getPrice)));
        System.out.println("Somma del prezzo dei prodotti divisi per categoria.");
        totalByCategory.forEach((categ, total ) -> System.out.println("Category: " + categ + ", total: " + total));

        // ESERCIZIO 6 - Usando la classe Apache IO file utils implementare un metodo salvaProdottiSuDisco che salvi su
        // disco un file contenente la lista dei prodotti.

        File file = new File("src/file.txt");

        try {
            for (Product product : allProducts) {
                System.out.println(product.getCategory());
                FileUtils.writeStringToFile(file, (product.getName()) + "@" + product.getCategory() + "@" + product.getPrice() + "@", StandardCharsets.UTF_8, true);
            }

        } catch(IOException e) {
            throw new RuntimeException(e);
        }

        // ESERCIZIO 7 - implementare un metodo leggiProdottiDaDisco che riempia un ArrayList con il contenuto
        // del file salvato al punto 6.
        String contenuto;
        try {

            contenuto = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
            System.out.println("contenuto file: " + contenuto);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        String[] strings = contenuto.split("@");
        for (int i = 0; i < strings.length; i++) {
           // System.out.println(strings[i]);
        }

        List<Product> productsFromFile = new ArrayList<>();

        for (int i = 0; i < strings.length; i += 3) {
            Product product = new Product(strings[i], strings[i + 1], Double.parseDouble(strings[i + 2]));
            productsFromFile.add(product);
        }

         productsFromFile.forEach(System.out::println);
    }
}

