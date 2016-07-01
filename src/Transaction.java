import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import DBUtility.DBUtility;



/**
 *Transaction
 * @author Brian and Josh
 * @param type takes an integer for the type of transaction
 *@param amount a double for the amount of money in the transaction
 *@param acct is an integer us for the account number in the database
 *@param overdraw is an int representing whether or not the transaction is an overdrawn one 
 *
 * 
 */
public class Transaction {
	private int type;
	private double amount;
	private String date;
	private int acct;
	private int overdraw;
	private static Connection con = null;
	private static String classForName = "oracle.jdbc.driver.OracleDriver";
	private static String connectionPath = "jdbc:oracle:thin:ora1/ora1@localhost:1521:orcl";
	
	
	public Transaction(int t, double amt, String d,int acct){
		type = t;
		amount=amt;
		date=d;
		this.acct=acct;
		overdraw=0;
	}
	
	public Transaction(int t, double amt, String d,int acct,int overdraw){
		type = t;
		amount=amt;
		date=d;
		this.acct=acct;
		this.overdraw=overdraw;
	}
	
	/**
	 * pushToDB pushes the current transaction to the database
	 */
	public void pushToDB(){
		try{
			ResultSet rs = null;
			Class.forName(classForName);
			con = DriverManager.getConnection(connectionPath);
			Statement sql = con.createStatement();
			String update = "insert into dtransactions (transactiontype,accountid,amount,dot,overdraw) values ("+type+","+acct+","+amount+",to_date('"+date+"','yyyy/mm/dd'),"+overdraw+")";
			sql.executeUpdate(update);
		}catch (SQLException e) {
			e.printStackTrace();
		}catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	public double getAmount() {
		return amount;
	}
	public void setAmount(double amount) {
		this.amount = amount;
	}
	public String getDate() {
		return date;
	}
	public void setDate(String date) {
		this.date = date;
	}
}
