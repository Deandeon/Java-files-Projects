/*
 * Code Written by 
 * @David Kweku Amissah Orhin 
 * Student ID 20522026
 * 
 *  A compound Interest calculator that takes in arguments to compute its data.
 */

// Importing the Scanner class from the java.util package
import java.util.Scanner;

//  public class named compoundTaxCalculator
public class compoundTaxCalculator{

    // Main method, entry point of the program
    public static void main(String[] args){

        // Unicode character representing the Ghanaian cedi symbol
        char cediSign = '\u20B5';
        
        // Creating a Scanner object to read input from the console
        Scanner Input = new Scanner(System.in);

        // Prompting the user to input the principal amount
        System.out.println("Please input the Principal");
        
        // Scanning the principal amount input bu the user 
        double principal = Input.nextDouble();

        // Prompt for the  user to input the interest rate
        System.out.println("Please input the rate");
        
        // Scanning the interest rate input 
        double rate = Input.nextDouble();

        // Scanning  the user to input the number of periods per year
        System.out.println("Please input the Periods per year");
        // Scanning the number of periods per year input by the user
        double periodsPerYear = Input.nextDouble();

        // Prompting the user to input the monthly deposit amount
        System.out.println("Please input the montly deposit");
        
        // Scanning the monthly deposit amount input by the user
        double deposit = Input.nextDouble();

        // Prompting the user to input the total number of periods
        System.out.println("Please input the total number of periods");
        // Scanning the total number of periods input by the user
        int totalPeriods = Input.nextInt();

        // Calling the computeInvestmentValue method with the provided input arguments and storing the result
        double value = computeInvestmentValue(principal,rate,periodsPerYear,deposit,totalPeriods);
        
        // Displaying the total investment value using the cedi symbol and formatting it to two decimal places
        System.out.printf("Total Investment: %c  %.2f",cediSign,value);
    }

    // Method to compute the total investment value
    public static double computeInvestmentValue( 
        double principal,
        double rate,
        double periodsPerYear,
        double deposit, 
        int totalPeriods ) {
    
        // Defining a Unicode character representing the Ghanaian cedi
        char cediSign = '\u20B5';

        // Calculating the interest rate per period by dividing the inital rate by the period per year
        double rate1 = rate/periodsPerYear;
        // Initializing variables to store various values
        double totalAmount = 0;
        double totalInvestment = 0;
        double totalPrincipal = 0;
        double interest = 0;
        int periodCounter = 0;

        // Calculating the interest earned on the principal amount
        interest =  principal * (rate1/100);
        // Calculating the total principal amount after adding the interest
        totalPrincipal = principal + interest;

        // Looping through each period and calculating the total amount after each period
        for(int i = 1; i < totalPeriods; i++){
            // Resetting the interest value to 0 to allow it to accumulate new interest on each loop
            interest = 0;
            // Adding the monthly deposit to the total principal amount continously
            totalPrincipal += deposit;
            // Calculating the interest earned on the updated principal amount
            interest = totalPrincipal * (rate1/100);
            // Calculating the total amount at the end of the period
            totalAmount = totalPrincipal + interest;
            // Updating the total principal amount for the next period
            totalPrincipal = totalAmount;
            // Increasing  the period counter by one to keep track 
            periodCounter += 1;
        }

        // Calculating the total investment value 
        totalInvestment = totalAmount ;

        // Calculating the final interest earned
        double newInterest = totalAmount -( principal + (deposit * periodCounter));

        // Displaying the total interest earned using the cedi symbol 
        
        System.out.printf("Total Interest: %c  %.2f",cediSign,newInterest);// formatting it to two decimal places using the printf statement
        // Printing a newline for  a better output reading
        System.out.println(" ");
    
        // Returning the total investment value
        return totalInvestment;
    }
    
}






/*
 * /*
 * PLANNING/STEPS
 * Principal - your original investment (500)

Rate - Interest rate - 28.8854

peroids per year- No of perirods for your investment to occur (Eg. In the example it said 3 months = 5 % since 20/4 = 5

Deposit = roll over( added money to the investment eg (¢100 every three
months after the initial investment (so in April, July and October 2023) = 100 *3 =300 )

total periods = No of times the period would cuumulate over (Quarterly , montly. half yearly)
 
500- january - april investment
36.105625 - J-A interest

 Interest for this - A-J interest - 45
(536.105625 +100 = 636.105625) - A - J Investment

782.04- J- O investment
56 - J-O interest

938.6859506- O-J investments   etc # note that some of these calculations are wrong
56 - OJ interest

total periods = no of periods eg 3



Return
- Total value (Amount)

-Interest = (Amount - Principal invested)
where principal invested = ( Original principal * (No of deposits added) *( the fix deposit)


 * 
 * 
 */


