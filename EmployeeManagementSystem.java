package boots;

import java.util.*;
import java.util.stream.Collectors;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.io.*;

class Employee implements Serializable, Comparable<Employee> {
    private static final long serialVersionUID = 1L;
    private static int nextId = 1;
    
    private int id;
    private String name;
    private String department;
    private double salary;
    private LocalDate hireDate;
    private String email;
    
    public Employee(String name, String department, double salary, String email) {
        this.id = nextId++;
        this.name = name;
        this.department = department;
        this.salary = salary;
        this.hireDate = LocalDate.now();
        this.email = email;
    }
    
    public Employee(int id, String name, String department, double salary, LocalDate hireDate, String email) {
        this.id = id;
        this.name = name;
        this.department = department;
        this.salary = salary;
        this.hireDate = hireDate;
        this.email = email;
        if (id >= nextId) nextId = id + 1;
    }
    
    public int getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    public double getSalary() { return salary; }
    public void setSalary(double salary) { this.salary = salary; }
    public LocalDate getHireDate() { return hireDate; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public int getYearsOfService() {
        return LocalDate.now().getYear() - hireDate.getYear();
    }
    
    @Override
    public int compareTo(Employee other) {
        return this.name.compareToIgnoreCase(other.name);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Employee employee = (Employee) obj;
        return id == employee.id;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return String.format("Employee{id=%d, name='%s', dept='%s', salary=%.2f, hireDate=%s, email='%s'}", 
                           id, name, department, salary, hireDate, email);
    }
    
    public String toCsvString() {
        return String.format("%d,%s,%s,%.2f,%s,%s", id, name, department, salary, hireDate, email);
    }
}

class EmployeeNotFoundException extends Exception {
    public EmployeeNotFoundException(String message) {
        super(message);
    }
}

class DatabaseConnection {
    private static DatabaseConnection instance;
    private boolean connected;
    
    private DatabaseConnection() {
        connected = true;
        System.out.println("Database connection established.");
    }
    
    public static synchronized DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }
    
    public boolean isConnected() {
        return connected;
    }
    
    public void disconnect() {
        connected = false;
        System.out.println("Database connection closed.");
    }
}

public class EmployeeManagementSystem {
    private List<Employee> employees;
    private Map<String, List<Employee>> departmentMap;
    private DatabaseConnection dbConnection;
    
    public EmployeeManagementSystem() {
        employees = new ArrayList<>();
        departmentMap = new HashMap<>();
        dbConnection = DatabaseConnection.getInstance();
        initializeSampleData();
    }
    
    private void initializeSampleData() {
        addEmployee(new Employee("John Doe", "Engineering", 75000, "john.doe@company.com"));
        addEmployee(new Employee("Jane Smith", "Marketing", 65000, "jane.smith@company.com"));
        addEmployee(new Employee("Mike Johnson", "Engineering", 80000, "mike.johnson@company.com"));
        addEmployee(new Employee("Sarah Wilson", "HR", 60000, "sarah.wilson@company.com"));
        addEmployee(new Employee("David Brown", "Finance", 70000, "david.brown@company.com"));
        addEmployee(new Employee("Lisa Davis", "Engineering", 85000, "lisa.davis@company.com"));
    }
    
    public void addEmployee(Employee employee) {
        employees.add(employee);
        departmentMap.computeIfAbsent(employee.getDepartment(), k -> new ArrayList<>()).add(employee);
        System.out.println("Added: " + employee.getName());
    }
    
    public Employee findEmployeeById(int id) throws EmployeeNotFoundException {
        return employees.stream()
                .filter(emp -> emp.getId() == id)
                .findFirst()
                .orElseThrow(() -> new EmployeeNotFoundException("Employee with ID " + id + " not found"));
    }
    
    public void updateEmployeeSalary(int id, double newSalary) throws EmployeeNotFoundException {
        Employee employee = findEmployeeById(id);
        double oldSalary = employee.getSalary();
        employee.setSalary(newSalary);
        System.out.printf("Updated %s's salary from %.2f to %.2f%n", 
                         employee.getName(), oldSalary, newSalary);
    }
    
    public boolean removeEmployee(int id) {
        Employee toRemove = null;
        for (Employee emp : employees) {
            if (emp.getId() == id) {
                toRemove = emp;
                break;
            }
        }
        
        if (toRemove != null) {
            employees.remove(toRemove);
            departmentMap.get(toRemove.getDepartment()).remove(toRemove);
            System.out.println("Removed: " + toRemove.getName());
            return true;
        }
        return false;
    }
    
    public List<Employee> getEmployeesByDepartment(String department) {
        return departmentMap.getOrDefault(department, new ArrayList<>());
    }
    
    public List<Employee> getAllEmployeesSorted() {
        return employees.stream()
                .sorted()
                .collect(Collectors.toList());
    }
    
    public Map<String, Double> getAverageSalaryByDepartment() {
        return employees.stream()
                .collect(Collectors.groupingBy(
                    Employee::getDepartment,
                    Collectors.averagingDouble(Employee::getSalary)
                ));
    }
    
    public List<Employee> getHighEarners(double threshold) {
        return employees.stream()
                .filter(emp -> emp.getSalary() > threshold)
                .sorted((e1, e2) -> Double.compare(e2.getSalary(), e1.getSalary()))
                .collect(Collectors.toList());
    }
    
    public void printDepartmentStatistics() {
        System.out.println("\n=== DEPARTMENT STATISTICS ===");
        Map<String, Long> headCount = employees.stream()
                .collect(Collectors.groupingBy(Employee::getDepartment, Collectors.counting()));
        
        Map<String, Double> avgSalaries = getAverageSalaryByDepartment();
        
        headCount.forEach((dept, count) -> {
            double avgSalary = avgSalaries.get(dept);
            System.out.printf("Department: %s | Employees: %d | Avg Salary: $%.2f%n", 
                             dept, count, avgSalary);
        });
    }
    
    public List<Employee> searchEmployeesByName(String searchTerm) {
        return employees.stream()
                .filter(emp -> emp.getName().toLowerCase().contains(searchTerm.toLowerCase()))
                .collect(Collectors.toList());
    }
    
    public void saveToFile(String filename) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filename))) {
            oos.writeObject(employees);
            System.out.println("Employee data saved to " + filename);
        } catch (IOException e) {
            System.err.println("Error saving data: " + e.getMessage());
        }
    }
    
    @SuppressWarnings("unchecked")
    public void loadFromFile(String filename) {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filename))) {
            employees = (List<Employee>) ois.readObject();
            rebuildDepartmentMap();
            System.out.println("Employee data loaded from " + filename);
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error loading data: " + e.getMessage());
        }
    }
    
    public void loadFromCsvFile(String filename) {
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            boolean isHeader = true;
            employees.clear();
            departmentMap.clear();
            
            while ((line = br.readLine()) != null) {
                if (isHeader) {
                    isHeader = false;
                    continue;
                }
                
                String[] parts = line.split(",");
                if (parts.length >= 6) {
                    try {
                        int id = Integer.parseInt(parts[0].trim());
                        String name = parts[1].trim();
                        String department = parts[2].trim();
                        double salary = Double.parseDouble(parts[3].trim());
                        LocalDate hireDate = LocalDate.parse(parts[4].trim());
                        String email = parts[5].trim();
                        
                        Employee emp = new Employee(id, name, department, salary, hireDate, email);
                        addEmployee(emp);
                    } catch (Exception e) {
                        System.err.println("Error parsing line: " + line + " - " + e.getMessage());
                    }
                }
            }
            System.out.println("CSV data loaded successfully from " + filename);
            System.out.println("Total employees loaded: " + employees.size());
        } catch (IOException e) {
            System.err.println("Error reading CSV file: " + e.getMessage());
        }
    }
    
    public void exportToCsv(String filename) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(filename))) {
            pw.println("ID,Name,Department,Salary,HireDate,Email");
            for (Employee emp : employees) {
                pw.println(emp.toCsvString());
            }
            System.out.println("Employee data exported to CSV: " + filename);
        } catch (IOException e) {
            System.err.println("Error exporting to CSV: " + e.getMessage());
        }
    }
    
    public void loadFromTextFile(String filename) {
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            employees.clear();
            departmentMap.clear();
            
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty() || line.startsWith("#")) continue;
                
                String[] parts = line.split("\\|");
                if (parts.length >= 4) {
                    try {
                        String name = parts[0].trim();
                        String department = parts[1].trim();
                        double salary = Double.parseDouble(parts[2].trim());
                        String email = parts[3].trim();
                        
                        Employee emp = new Employee(name, department, salary, email);
                        addEmployee(emp);
                    } catch (Exception e) {
                        System.err.println("Error parsing line: " + line + " - " + e.getMessage());
                    }
                }
            }
            System.out.println("Text file data loaded successfully from " + filename);
            System.out.println("Total employees loaded: " + employees.size());
        } catch (IOException e) {
            System.err.println("Error reading text file: " + e.getMessage());
        }
    }
    
    private void rebuildDepartmentMap() {
        departmentMap.clear();
        employees.forEach(emp -> 
            departmentMap.computeIfAbsent(emp.getDepartment(), k -> new ArrayList<>()).add(emp));
    }
    
    public void generateEmployeeReport() {
        System.out.println("\n=== EMPLOYEE REPORT ===");
        System.out.println("Total Employees: " + employees.size());
        
        double totalSalary = employees.stream().mapToDouble(Employee::getSalary).sum();
        double avgSalary = employees.stream().mapToDouble(Employee::getSalary).average().orElse(0.0);
        
        System.out.printf("Total Salary Budget: $%.2f%n", totalSalary);
        System.out.printf("Average Salary: $%.2f%n", avgSalary);
        
        Employee highestPaid = employees.stream()
                .max(Comparator.comparingDouble(Employee::getSalary))
                .orElse(null);
        
        if (highestPaid != null) {
            System.out.println("Highest Paid: " + highestPaid.getName() + " ($" + highestPaid.getSalary() + ")");
        }
        
        printDepartmentStatistics();
    }
    
    private void displayMenu() {
        System.out.println("\n=== EMPLOYEE MANAGEMENT SYSTEM ===");
        System.out.println("1. Add Employee");
        System.out.println("2. View All Employees");
        System.out.println("3. Search Employee by ID");
        System.out.println("4. Update Employee Salary");
        System.out.println("5. Remove Employee");
        System.out.println("6. View Employees by Department");
        System.out.println("7. Search Employees by Name");
        System.out.println("8. View High Earners (>$70,000)");
        System.out.println("9. Generate Employee Report");
        System.out.println("10. Save Data to Binary File");
        System.out.println("11. Load Data from Binary File");
        System.out.println("12. Load Data from CSV File");
        System.out.println("13. Export Data to CSV File");
        System.out.println("14. Load Data from Text File (pipe-separated)");
        System.out.println("0. Exit");
        System.out.print("Enter your choice: ");
    }
    
    public void run() {
        Scanner scanner = new Scanner(System.in);
        
        while (true) {
            displayMenu();
            
            try {
                int choice = scanner.nextInt();
                scanner.nextLine();
                
                switch (choice) {
                    case 1:
                        System.out.print("Enter name: ");
                        String name = scanner.nextLine();
                        System.out.print("Enter department: ");
                        String dept = scanner.nextLine();
                        System.out.print("Enter salary: ");
                        double salary = scanner.nextDouble();
                        scanner.nextLine();
                        System.out.print("Enter email: ");
                        String email = scanner.nextLine();
                        addEmployee(new Employee(name, dept, salary, email));
                        break;
                        
                    case 2:
                        System.out.println("\n=== ALL EMPLOYEES (Sorted by Name) ===");
                        getAllEmployeesSorted().forEach(System.out::println);
                        break;
                        
                    case 3:
                        System.out.print("Enter employee ID: ");
                        int searchId = scanner.nextInt();
                        try {
                            Employee found = findEmployeeById(searchId);
                            System.out.println("Found: " + found);
                        } catch (EmployeeNotFoundException e) {
                            System.out.println("Error: " + e.getMessage());
                        }
                        break;
                        
                    case 4:
                        System.out.print("Enter employee ID: ");
                        int updateId = scanner.nextInt();
                        System.out.print("Enter new salary: ");
                        double newSalary = scanner.nextDouble();
                        try {
                            updateEmployeeSalary(updateId, newSalary);
                        } catch (EmployeeNotFoundException e) {
                            System.out.println("Error: " + e.getMessage());
                        }
                        break;
                        
                    case 5:
                        System.out.print("Enter employee ID to remove: ");
                        int removeId = scanner.nextInt();
                        if (!removeEmployee(removeId)) {
                            System.out.println("Employee not found!");
                        }
                        break;
                        
                    case 6:
                        System.out.print("Enter department name: ");
                        String deptName = scanner.nextLine();
                        List<Employee> deptEmployees = getEmployeesByDepartment(deptName);
                        if (deptEmployees.isEmpty()) {
                            System.out.println("No employees found in " + deptName);
                        } else {
                            System.out.println("\n=== EMPLOYEES IN " + deptName.toUpperCase() + " ===");
                            deptEmployees.forEach(System.out::println);
                        }
                        break;
                        
                    case 7:
                        System.out.print("Enter name to search: ");
                        String searchName = scanner.nextLine();
                        List<Employee> searchResults = searchEmployeesByName(searchName);
                        if (searchResults.isEmpty()) {
                            System.out.println("No employees found matching: " + searchName);
                        } else {
                            System.out.println("\n=== SEARCH RESULTS ===");
                            searchResults.forEach(System.out::println);
                        }
                        break;
                        
                    case 8:
                        System.out.println("\n=== HIGH EARNERS (>$70,000) ===");
                        getHighEarners(70000).forEach(System.out::println);
                        break;
                        
                    case 9:
                        generateEmployeeReport();
                        break;
                        
                    case 10:
                        System.out.print("Enter filename to save (e.g., employees.dat): ");
                        String saveFile = scanner.nextLine();
                        saveToFile(saveFile);
                        break;
                        
                    case 11:
                        System.out.print("Enter filename to load (e.g., employees.dat): ");
                        String loadFile = scanner.nextLine();
                        loadFromFile(loadFile);
                        break;
                        
                    case 12:
                        System.out.print("Enter CSV filename to load (e.g., employees.csv): ");
                        String csvFile = scanner.nextLine();
                        loadFromCsvFile(csvFile);
                        break;
                        
                    case 13:
                        System.out.print("Enter CSV filename to export (e.g., export.csv): ");
                        String exportFile = scanner.nextLine();
                        exportToCsv(exportFile);
                        break;
                        
                    case 14:
                        System.out.print("Enter text filename to load (e.g., employees.txt): ");
                        String textFile = scanner.nextLine();
                        loadFromTextFile(textFile);
                        break;
                        
                    case 0:
                        System.out.println("Exiting system...");
                        dbConnection.disconnect();
                        scanner.close();
                        return;
                        
                    default:
                        System.out.println("Invalid choice! Please try again.");
                }
                
            } catch (InputMismatchException e) {
                System.out.println("Invalid input! Please enter a number.");
                scanner.nextLine();
            } catch (Exception e) {
                System.out.println("An error occurred: " + e.getMessage());
            }
        }
    }
    
    public static void main(String[] args) {
        System.out.println("Initializing Employee Management System...");
        
        EmployeeManagementSystem ems = new EmployeeManagementSystem();
        
        System.out.println("\n=== DEMO MODE - Showcasing Capabilities ===");
        
        System.out.println("\nDemonstrating Stream Operations:");
        ems.getHighEarners(70000).forEach(emp -> 
            System.out.println(emp.getName() + " - $" + emp.getSalary()));
        
        ems.generateEmployeeReport();
        
        System.out.println("\nStarting Interactive Mode...");
        System.out.println("File formats supported:");
        System.out.println("- CSV: ID,Name,Department,Salary,HireDate,Email");
        System.out.println("- Text: Name|Department|Salary|Email (pipe-separated)");
        System.out.println("- Binary: Java serialized objects (.dat files)");
        
        ems.run();
    }
}