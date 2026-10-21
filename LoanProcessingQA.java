public class LoanProcessingQA {

    private static int passed = 0;
    private static int failed = 0;

    private static void check(String name, boolean condition) {
        if (condition) {
            passed++;
            System.out.println("PASS: " + name);
        } else {
            failed++;
            System.out.println("FAIL: " + name);
        }
    }

    private static void expectException(String name, Runnable action) {
        try {
            action.run();
            check(name, false);
        } catch (IllegalArgumentException e) {
            check(name, true);
        }
    }

    private static LoanProcessingSystem.LoanResult loan(
            int age, double salary, double existingLoan,
            int score, int employment, double amount, int years) {
        return LoanProcessingSystem.processLoan(
                "TEST001", age, salary, existingLoan,
                score, employment, amount, years);
    }

    public static void main(String[] args) {

        System.out.println("Starting Loan Processing QA Tests\n");

        // Age boundary tests
        check("Minimum age 21 is eligible",
                loan(21, 50000, 0, 750, 1, 100000, 10).approved);

        check("Maximum age 60 is eligible",
                loan(60, 50000, 0, 750, 1, 100000, 10).approved);

        check("Age below 21 is rejected",
                !loan(20, 50000, 0, 750, 1, 100000, 10).approved);

        check("Age above 60 is rejected",
                !loan(61, 50000, 0, 750, 1, 100000, 10).approved);

        // Salary and input validation
        expectException("Zero salary is invalid", () ->
                loan(30, 0, 0, 750, 1, 100000, 10));

        expectException("Negative salary is invalid", () ->
                loan(30, -50000, 0, 750, 1, 100000, 10));

        // Credit score tests
        check("Poor credit score is rejected",
                !loan(30, 50000, 0, 500, 1, 100000, 10).approved);

        expectException("Credit score below 300 is invalid", () ->
                loan(30, 50000, 0, 200, 1, 100000, 10));

        expectException("Credit score above 850 is invalid", () ->
                loan(30, 50000, 0, 900, 1, 100000, 10));

        // Existing loan and DTI
        check("High existing loan causes rejection",
                !loan(30, 50000, 300000, 750, 1, 100000, 10).approved);

        check("DTI calculation is correct",
                Math.abs(LoanProcessingSystem.calculateDTI(
                        50000, 120000) - 20.0) < 0.001);

        // Employment categories and interest rates
        check("Permanent employment interest rate",
                LoanProcessingSystem.calculateInterestRate(750, 1) == 8.0);

        check("Contract employment interest rate",
                LoanProcessingSystem.calculateInterestRate(750, 2) == 9.0);

        check("Self-employed interest rate",
                LoanProcessingSystem.calculateInterestRate(750, 3) == 9.5);

        expectException("Invalid employment type is rejected", () ->
                loan(30, 50000, 0, 750, 4, 100000, 10));

        // Eligible amount and EMI
        check("Eligible amount calculation is correct",
                LoanProcessingSystem.calculateEligibleAmount(
                        50000, 100000) == 900000.0);

        check("EMI is positive",
                LoanProcessingSystem.calculateEMI(
                        100000, 8, 10) > 0);

        expectException("Zero loan amount is invalid", () ->
                LoanProcessingSystem.calculateEMI(0, 8, 10));

        expectException("Invalid tenure is rejected", () ->
                loan(30, 50000, 0, 750, 1, 100000, 0));

        // Invalid customer ID
        expectException("Empty customer ID is invalid", () ->
                LoanProcessingSystem.processLoan(
                        "", 30, 50000, 0, 750, 1, 100000, 10));

        System.out.println("\n------------------------------");
        System.out.println("Passed: " + passed);
        System.out.println("Failed: " + failed);

        if (failed == 0) {
            System.out.println("QA RESULT: ALL TESTS PASSED");
        } else {
            System.out.println("QA RESULT: TESTS FAILED");
            System.exit(1);
        }
    }
}
