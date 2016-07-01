import java.util.ArrayList;
import java.util.Scanner;
import DBUtility.DBUtility;

/**
 * Bank ATM Application
 *
 * @author Brian and Josh
 */

public class Bank {
	
	/**
	 * Main interacts with user and prints results of queries
	 */
	public static void main(String[] args){
		
		Scanner in = new Scanner(System.in);
		ArrayList<Account> session = new ArrayList<Account>();
		int customerid =0;
		
		System.out.println("Welcome to Dalton Corp Savings and Loan");
		System.out.println("1: Returning customer 2: New Customer");
		String input = in.nextLine();
		if(input.equals("2")){
			System.out.println("Enter customer name:");
			String name = in.nextLine();
			System.out.println("Enter desired PIN: ");
			String pin = in.nextLine();
			
			String[] columns = {"pin","name"};
			String[] values = {pin,name};
			boolean[] isString = {false,true};
			
			DBUtility.insertIntoDB("dcustomers",columns,values,isString);
			customerid=DBUtility.getIntWhere("dcustomers", "name", name, "customerid").get(0);
			
			
			System.out.println("Create a 1:checking or 2:savings?");
			String type = in.nextLine();
			Account acct = new Account(type,customerid,0);
			acct.pushToDB();
		}
		
		System.out.println("Enter 1 to access existing accounts");
		System.out.println("Enter 2 to create new accounts");
		input = in.nextLine();
		
		while(input.equals("2")){
			System.out.println("Create a checking or savings?");
			String type = in.nextLine();
			customerid =1009;
			Account acct = new Account(type,customerid,0);
			acct.pushToDB();
			//get acct#
			//print acct#
			System.out.println("Enter 1 to access existing accounts");
			System.out.println("Enter 2 to create another account");
			input=in.nextLine();
		}
		
		while(true){
			boolean auth = false;
			while(auth==false){
				System.out.println("Enter account number");
				String acctNum = in.nextLine();
				System.out.println("Enter PIN");
				String pin = in.nextLine();
				
				auth=Account.authorize(Integer.parseInt(pin), Integer.parseInt(acctNum));
				
				if(auth){
					System.out.println("Login successful");
					System.out.println("Account " + acctNum +" added to session");
					
					Account added = new Account(""+DBUtility.getIntWhere("daccounts", "accountid", acctNum, "accounttype").get(0),customerid,Integer.parseInt(acctNum));
					added.setId(Integer.parseInt(acctNum));
					added.pullCustomer();
					session.add(added);
				}
				else{
					System.out.println("Incorrect account/pin");
				}
			}
			System.out.println("Current accounts "+session.get(0).getId());
			System.out.println("Enter 1 to proceed");
			System.out.println("Enter 2 to add another account");
			input = in.nextLine();
			if(input.equals("1"))break;
		}
		
		//print accounts and balances
		
		//prompt for transactions
		//type,amount,date
		while(true){
			System.out.println("Enter 1 to add transaction");
			System.out.println("Enter 2 to print balance");
			System.out.println("Enter 3 to read summary");
			System.out.println("Enter 4 to exit");
			input=in.nextLine();
			if(input.equals("4"))break;
			if(input.equals("1")){
				int acctNum;
				if(session.size()>1){
					System.out.println("Transaction for which account?");
					for(Account acct:session){
						System.out.println(acct.getId()+" ");
					}
					acctNum=in.nextInt();
					in.nextLine();
				}
				else acctNum=session.get(0).getId();
				System.out.println("Enter 1: deposit 2: withdrawal 3: check 4:debit");
				int type = in.nextInt();
				in.nextLine();
				System.out.println("Enter transaction amount: ");
				double amount = in.nextDouble();
				in.nextLine();
				System.out.println("Enter date YYYY/MM/DD");
				String date = in.nextLine();
				Transaction t = new Transaction(type,amount,date,acctNum);
				t.pushToDB();
			}
			else if(input.equals("2")){
				for(int i = 0; i < session.size();i++){
					Account a = session.get(i);
					a.calculateBalance();
					a.pushBalance();
					System.out.println("Account "+a.getId()+" balance: "+a.getBalance());
				}
			}else if(input.equals("3")){
				System.out.println("Summary for 0: customer or account number for account summary");
				input=in.nextLine();
				if (input.equals("0")){
					System.out.println(Account.printcustomer(session.get(0).getCustomer()));
				}else {
					Account n=session.get(0);
					for (Account a: session){
						if (a.getId()==Integer.parseInt(input)){
							n=a;
						}
					}
					System.out.println(n.printSummary());
				}
			}
			else System.out.println("Unrecognized input");
		}
		System.out.println("Thank you for banking with us!");
		
	}
	
}
