import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import DBUtility.DBUtility;

/**
 * Stores account information locally and interacts with database
 *
 * @author Brian and Josh
 * 
 * @param id stores account id
 * @param type stores account type 1: checking 2: savings
 * @param balance stores funds in account
 * @param customer stores customer id of owner
 */

public class Account {
	private int id;
	private String type;
	private double balance;
	private int customer;
	private static Connection con = null;
	private static String classForName = "oracle.jdbc.driver.OracleDriver";
	private static String connectionPath = "jdbc:oracle:thin:ora1/ora1@localhost:1521:orcl";
	
	
	public Account(String t, int c,int b){
		type=t;
		customer=c;
		balance=b;
	}
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public double getBalance() {
		return balance;
	}

	public void setBalance(double balance) {
		this.balance = balance;
	}

	public int getCustomer() {
		return customer;
	}

	public void setCustomer(int customer) {
		this.customer = customer;
	}

	/**
	 * Uses transactions from database to calculate account balance including fees
	 * and update local balance
	 *
	 * @return void
	 */
	public void calculateBalance(){
		balance=0;
		try{
			ResultSet rs = null;
			Class.forName(classForName);
			con = DriverManager.getConnection(connectionPath);
			Statement sql = con.createStatement();
			String query = "select transactiontype, amount, dot, overdraw from dtransactions where accountid="+id+" order by dot";
			rs=sql.executeQuery(query);
			while(rs.next()){
				int ttype = rs.getInt(1);
				String date = rs.getDate(3).toString();
				int overdrawn = rs.getInt(4);
				if(ttype==1||ttype==3){
					balance+=rs.getDouble(2);
				}
				else{
					balance-=rs.getDouble(2);
					if(balance<0&&overdrawn==0){
						String setO = "update dtransactions set overdraw=1 where accountid="+id+" and dot=to_date('"+date+"','yyyy/mm/dd')";
						sql.executeUpdate(setO);
						if(type.equals("1")){
							query = "select accountid from daccounts where customerid="+customer+" and accounttype=2";
							ResultSet savings = sql.executeQuery(query);
							if(savings.next()){
								Transaction t = new Transaction(5,50,date,savings.getInt(1));
								t.pushToDB();
							}
							else {
								balance-=35;
								Transaction t = new Transaction(5,35,date,id,1);
								t.pushToDB();
							}
						}
						else {
							balance-=35;
							Transaction t = new Transaction(5,35,date,id,1);
							t.pushToDB();
						}
					}
				}
			}
			
		}catch (SQLException e) {
			e.printStackTrace();
		}catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Pushes the local calculated balance to the database
	 *
	 * @return void
	 */
	public void pushBalance(){
		try{
			Class.forName(classForName);
			con = DriverManager.getConnection(connectionPath);
			Statement sql = con.createStatement();
			String query = "update daccounts set balance="+balance+" where accountid="+id;
			sql.executeUpdate(query);
			
		}catch (SQLException e) {
			e.printStackTrace();
		}catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Creates a new row in daccounts table based on local variables
	 *
	 * @return void
	 */
	public void pushToDB(){
		String[] columns = {"accounttype","balance","customerid"};
		String[] values = {type,""+balance,""+customer};
		boolean[] isString = {false,false,false};
		
		DBUtility.insertIntoDB("daccounts",columns,values,isString);
	}

	/**
	 * Takes an account number and PIN and verifies login
	 *
	 * @param pin pin provided by user
	 * @param acct account provided by user
	 *
	 * @return true if pin is correct login else false
	 */
	public static boolean authorize(int pin,int acct){
		try{
			ResultSet rs = null;
			Class.forName(classForName);
			con = DriverManager.getConnection(connectionPath);
			Statement sql = con.createStatement();
			String query = "select pin from dcustomers inner join daccounts on dcustomers.customerid=daccounts.customerid where accountid="+acct;
			rs=sql.executeQuery(query);
			if(rs.next()){
				return(pin==rs.getInt(1));
			}
		}catch (SQLException e) {
			e.printStackTrace();
		}catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return false;
	}
	

	/**
	 * gets the customer id of an account and stores locally
	 *
	 * @return void
	 */
	public void pullCustomer(){
		try{
			ResultSet rs = null;
			Class.forName(classForName);
			con = DriverManager.getConnection(connectionPath);
			Statement sql = con.createStatement();
			String query = "select customerid from daccounts where accountid="+id;
			rs=sql.executeQuery(query);
			if(rs.next())customer=rs.getInt(1);
		}catch (SQLException e) {
			e.printStackTrace();
		}catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
	

	/**
	 * Calculates number of transactions, deposits, withdrawals and total amounts
	 *
	 * @return String containing information
	 */
	public String printSummary(){
		String output="";
		try{
			int numtransactions=0;
			int numdeposits=0;
			int numwithdrawal=0;
			double depositamt=0;
			double withdrawalamt=0;
			double totalfees=0;
			ResultSet rs = null;
			Class.forName(classForName);
			con = DriverManager.getConnection(connectionPath);
			Statement sql = con.createStatement();
			String query = "select transactiontype,  amount  from dtransactions where accountid="+id;
			rs=sql.executeQuery(query);
			while(rs.next()){
				int ttype=rs.getInt(1);
				if(ttype==1){
					depositamt+=rs.getDouble(2);
					numdeposits++;
				}
				if(ttype==2){
					withdrawalamt+=rs.getDouble(2);
					numwithdrawal++; 
				}
				if(ttype==5){
					totalfees+=rs.getDouble(2);
				}
				numtransactions++;
			}
			output ="Total number of deposits: "+numdeposits+
					": \nTotal deposit amount: "+depositamt+
					": \nTotal number of withdrawals: "+numwithdrawal+
					": \nTotal withdrawal amount: "+withdrawalamt+
					": \nTotal Fees charged: "+totalfees+
					": \nTotal Transactions: "+numtransactions;
		}catch (SQLException e) {
			e.printStackTrace();
		}catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return output;
	}
	/**
	 * Calculates number of transactions, deposits, withdrawals and total amounts
	 * for a customer
	 *
	 * @return String containing information
	 */
	public static String printcustomer(int customerid){

		String output="";
		try{
			int numtransactions=0;
			int numdeposits=0;
			int numwithdrawal=0;
			double depositamt=0;
			double withdrawalamt=0;
			double totalfees=0;
			int numchk=0;
			int numsave=0;
			ResultSet rs = null;
			Class.forName(classForName);
			con = DriverManager.getConnection(connectionPath);
			Statement sql = con.createStatement();
			String query = "select accountid, accounttype  from daccounts where customerid="+customerid;
			rs=sql.executeQuery(query);
			while(rs.next()){
					Account a= new Account(""+rs.getInt(2),customerid ,0);
					a.setId(rs.getInt(1));
					String s=a.printSummary();
					String[] sa=s.split(": ");
					numdeposits+=Integer.parseInt(sa[1]);
					depositamt+=Double.parseDouble(sa[3]);
					numwithdrawal+=Integer.parseInt(sa[5]);
					withdrawalamt+=Double.parseDouble(sa[7]);
					totalfees+=Double.parseDouble(sa[9]);
					numtransactions+=Integer.parseInt(sa[11]);
					int atype=rs.getInt(2);
					if (atype==1||atype==3){
						numchk++;
					}
					if(atype==2||atype==4){
						numsave++;
					}
					
			}
			output ="Total number of deposits: "+numdeposits+
					"\nTotal deposit amount: "+depositamt+
					"\n Total number of withdrawals: "+numwithdrawal+
					"\nTotal withdrawal amount: "+withdrawalamt+
					"\nTotal Fees charged: "+totalfees+
					"\nTotal number of checking accounts: "+numchk+
					"\nTotal number of savings accounts: "+numsave+
					"\nTotal Transactions: "+numtransactions;
		}catch (SQLException e) {
			e.printStackTrace();
		}catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return output;
		
	}
}
