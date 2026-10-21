public class LoanProcessingSystem {

    public static class LoanResult {
        public boolean approved;
        public double dti;
        public double eligibleAmount;
        public double interestRate;
        public double emi;
        public String message;

        public LoanResult(boolean approved, double dti,
                double eligibleAmount, double interestRate,
                double emi, String message) {
            this.approved = approved;
            this.dti = dti;
            this.eligibleAmount = eligibleAmount;
            this.interestRate = interestRate;
            this.emi = emi;
            this.message = message;
        }
    }

    public static double calculateDTI(double salary,
                                      double existingLoan) {
        return existingLoan / (salary * 12.0) * 100.0;
    }

    public static double calculateEligibleAmount(double salary,
                                                  double existingLoan) {
        return Math.max(0, salary * 20.0 - existingLoan);
    }

    // Employment: 1 = Permanent, 2 = Contract, 3 = Self-employed
    public static double calculateInterestRate(int creditScore,
                                                int employmentType) {
        double rate;

        if (creditScore >= 750) {
            rate = 8.0;
        } else if (creditScore >= 650) {
            rate = 10.0;
        } else {
            rate = 12.0;
        }

        if (employmentType == 2) {
            rate += 1.0;
        } else if (employmentType == 3) {
            rate += 1.5;
        }

        return rate;
    }

    public static double calculateEMI(double principal,
                                      double annualRate,
                                      int tenureYears) {
        if (principal <= 0 || annualRate < 0 || tenureYears <= 0) {
            throw new IllegalArgumentException(
                    "Invalid loan amount, interest rate, or tenure.");
        }

        int months = tenureYears * 12;
        double monthlyRate = annualRate / 1200.0;

        if (monthlyRate == 0) {
            return principal / months;
        }

        double factor = Math.pow(1 + monthlyRate, months);
        return principal * monthlyRate * factor / (factor - 1);
    }

    public static LoanResult processLoan(String customerId,
            int age, double salary, double existingLoan,
            int creditScore, int employmentType,
            double requestedLoan, int tenureYears) {

        if (customerId == null || customerId.trim().isEmpty()) {
            throw new IllegalArgumentException("Customer ID is required.");
        }

        if (age <= 0 || salary <= 0 || existingLoan < 0
                || creditScore < 300 || creditScore > 850
                || employmentType < 1 || employmentType > 3
                || requestedLoan <= 0 || tenureYears <= 0) {
            throw new IllegalArgumentException("Invalid input values.");
        }

        double dti = calculateDTI(salary, existingLoan);
        double eligible = calculateEligibleAmount(salary, existingLoan);
        double rate = calculateInterestRate(creditScore, employmentType);
        double emi = calculateEMI(requestedLoan, rate, tenureYears);

        boolean approved = age >= 21 && age <= 60
                && creditScore >= 600
                && dti <= 40
                && requestedLoan <= eligible
                && emi <= salary * 0.40;

        String message = approved ? "Loan Approved" : "Loan Rejected";

        return new LoanResult(approved, dti, eligible, rate, emi, message);
    }

    public static void main(String[] args) {
        try {
            String customerId;
            int age;
            double salary;
            double existingLoan;
            int creditScore;
            int employmentType;
            double requestedLoan;
            int tenureYears;

            if (args.length == 8) {
                customerId = args[0];
                age = Integer.parseInt(args[1]);
                salary = Double.parseDouble(args[2]);
                existingLoan = Double.parseDouble(args[3]);
                creditScore = Integer.parseInt(args[4]);
                employmentType = Integer.parseInt(args[5]);
                requestedLoan = Double.parseDouble(args[6]);
                tenureYears = Integer.parseInt(args[7]);
            } else {
                java.util.Scanner sc = new java.util.Scanner(System.in);

                System.out.print("Customer ID: ");
                customerId = sc.nextLine();

                System.out.print("Age: ");
                age = Integer.parseInt(sc.nextLine());

                System.out.print("Monthly salary: ");
                salary = Double.parseDouble(sc.nextLine());

                System.out.print("Existing loan amount: ");
                existingLoan = Double.parseDouble(sc.nextLine());

                System.out.print("Credit score (300-850): ");
                creditScore = Integer.parseInt(sc.nextLine());

                System.out.print(
                    "Employment (1=Permanent, 2=Contract, 3=Self-employed): ");
                employmentType = Integer.parseInt(sc.nextLine());

                System.out.print("Requested loan amount: ");
                requestedLoan = Double.parseDouble(sc.nextLine());

                System.out.print("Tenure in years: ");
                tenureYears = Integer.parseInt(sc.nextLine());

                sc.close();
            }

            LoanResult result = processLoan(customerId, age, salary,
                    existingLoan, creditScore, employmentType,
                    requestedLoan, tenureYears);

            System.out.println("\n--- Loan Assessment ---");
            System.out.println("Customer ID: " + customerId);
            System.out.printf("Debt-to-Income Ratio: %.2f%%%n", result.dti);
            System.out.printf("Eligible Loan Amount: %.2f%n",
                    result.eligibleAmount);
            System.out.printf("Interest Rate: %.2f%%%n",
                    result.interestRate);
            System.out.printf("Monthly EMI: %.2f%n", result.emi);
            System.out.println("Decision: " + result.message);

        } catch (Exception e) {
            System.err.println("ERROR: " + e.getMessage());
            System.exit(1);
        }
    }
}
