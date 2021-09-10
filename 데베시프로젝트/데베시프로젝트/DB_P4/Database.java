package DB_P4;
import java.sql.*;
import java.util.Scanner;

public class Database {
	    String driver = "org.mariadb.jdbc.Driver";
	    Connection c;
	    static ResultSet rs;
	 
	    public Database() {
	         try {
	            Class.forName(driver);
	            String url = "jdbc:mariadb://localhost:3306/db4";
	        	String user="scy0723";
	        	String pwd="scy890612";
	        	c=DriverManager.getConnection(url, user, pwd);
	            if( c != null ) {
	                System.out.print("음원 스트리밍 서비스 DB 접속 성공");
	            }
	            

	        } catch (SQLException e) {
	            System.out.println("접속 실패: SQLException");
	            e.printStackTrace();
	        } catch (ClassNotFoundException e) {
	        	System.out.println("접속 실패: ClassNotFoundException");
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    }
	    
	    public static void main(String[] args) throws SQLException{
	    	Scanner s= new Scanner (System.in);
	        Database d = new Database();
	        Statement stmt=d.c.createStatement();
	        while(true) {
	        	int cmd;
	        	System.out.println("\n0. Exit\n"
	        			+ "1. User menu\n"
	        			+ "2. Manager menu");
	        	System.out.print("Enter command: ");
	        	cmd=s.nextInt();
	        	switch (cmd) {
///////////////////////////////////////////////////////////////////////////////////EXIT
	        	case 0: 
	        		//exit
	        		System.out.println("\nEXIT");
	        		if(rs!=null) rs.close();
	        		d.c.close();
	      	        stmt.close();
	      	       
	      	        
	        		System.exit(0);
	        		
///////////////////////////////////////////////////////////////////////////////////USER
	        	case 1://User menu
	        		System.out.print("Enter your user number (ex: U001): ");
	        		s.nextLine();
	        		String Unum=s.nextLine();
	        		d.rs=stmt.executeQuery("select exists (select * from db4.`user` u\r\n" + 
	        				"where u.Unum=\'"+Unum+"\')as success;");
	        		rs.next();
	        		if(rs.getString(1).equals("0")) {
		        		System.out.println("Error: No such user!");
		        		continue;
	        		}
	        
	        		int u_cmd=9999;
	        		while(u_cmd!=0) {
	        			System.out.println("\n0. Return to previous menu\n"
	        					+ "1. User information\n"
	        					+ "2. My playlist\n"
	        					+ "3. Play songs\n"
	        					+ "4. Purchase songs\n"
	        					+ "5. Top 10 Music Chart\n"
	        					+ "6. Edit user information");
	        			System.out.print("Enter command: ");
	        			u_cmd=s.nextInt();
	        			
	        			switch (u_cmd) {
	    	        	case 0: //Return to previous menu 
	    	        		continue;
	    	        	///////////////////////////////////
	    	        	case 1: //View user information
	    	        		d.rs=stmt.executeQuery("select u.Fname,u.Lname, u.phoneNum, chargeMnum,m.lname,m.fname,ba.accNum\r\n" + 
	    	        				"from db4.`user` u ,db4.bank_account ba,db4.manager m \r\n" + 
	    	        				"where u.Unum =ba.ownerUNum and u.Unum =\'"+Unum+" \'and u.chargeMnum =m.mNum ");
	    	        		while(rs.next()) {
	    	        			System.out.println("\nFirst Name: "+ rs.getString(1)+
	    	        					"\nLast Name: "+rs.getString(2)
	    	        					+"\nPhone number: "+rs.getString(3)
	    	        					+"\nManager in charge: "+rs.getString(4)+" "+rs.getString(5)+" "+rs.getString(6)
	    	        					+"\nBank account: "+rs.getString(7));
	    	        		}
	    	        		break;
	    	        	//////////////////////////////////	
	    	        	case 2://View user playlist
	    	        		d.rs=stmt.executeQuery("select Pnum, Pname, creationDate from db4.playlist p \r\n" + 
	    	        				"where p.ownerUNum =\'"+Unum+"\'");
	    	  
	    	        		while(rs.next()) {
	    	        			System.out.println( "\nPlaylist number: "+rs.getString("Pnum")+
	    	        					"\nName: "+rs.getString("Pname")
	    	        					+"\nCreation date: "+rs.getString("creationDate"));
	    	        		}
	    	        		System.out.println("\n0. Return to previous menu"
	    	        				+ "\n1. Show playlist");
	    	        		s.nextLine();
	    	        		System.out.print("Enter command: ");
	    	        		String p_command=s.nextLine();
	    	        		if(p_command.equals("0")) break;               //Return to previous menu
	    	        		else if(p_command.equals("1")) {               //Show playlist
	    	        			System.out.print("Enter playlist number (ex: P001):");
	    	        			String pNum=s.nextLine();
	    	        			d.rs=stmt.executeQuery("select ss.Snum, ss.Sname ,ss.artist\r\n" + 
	    	        					"from db4.sound_source ss ,db4.`contains` c \r\n" + 
	    	        					"where ss.Snum =c.Snum and c.Pnum =\'"+pNum+"\';" + 
	    	        					"");
	    	        			System.out.println("----------------------------");
		    	        		while(rs.next()) {
		    	        			System.out.print("\n"+rs.getString("Snum")+" "+rs.getString("Sname")+"-"+rs.getString("artist"));
		    	        		}
		    	        		System.out.println();
		    	        		System.out.println("----------------------------");
		    	        		System.out.println("1) Play songs\n"
		    	        				+ "2) Edit playlist\n"
		    	        				+ "3) Return\n");
		    	        		System.out.print("Enter command: ");
		    	        		p_command=s.nextLine();
		    	        		if(p_command.equals("1")) {           //play songs
		    	        			System.out.print("\nEnter sound source number to play(ex: S00001): ");
		    	        			p_command=s.nextLine();
		    	        			d.rs=stmt.executeQuery("select exists (select * from db4.`sound_source` ss\r\n" + 
		        	        				"where ss.Snum=\'"+p_command+"\')as success;");
		        	        		rs.next();
		        	        		if(rs.getString(1).equals("0")) {
		        		        		System.out.println("Error: No such song!");
		        		        		continue;
		        	        		}
		        	        		else {
		    	        			stmt.executeUpdate("update db4.plays \r\n" +               //play
		    	        					"set nTimes = nTimes+1\r\n" + 
		    	        					"where Unum=\'"+Unum+"\'and Snum=\'"+p_command+"\';");
		    	        			System.out.println("Playing "+p_command+"...");
		        	        		}
		    	        		}
		    	        		else if(p_command.equals("2")){          //edit playlist
		    	        			System.out.print("\nEnter A(add) / D(delete) / DP(delete all from playlist) : ");
		    	        			p_command=s.nextLine();
		    	        			if(p_command.equals("A")){                         //add
		    	        				System.out.print("Enter sound source number to add (ex: S00001): ");
		    	        				String ss=s.nextLine();
		    	        				d.rs=stmt.executeQuery("\nselect exists (select * from db4.`sound_source` ss\r\n" + 
			        	        				"where ss.Snum=\'"+ss+"\')as success;");
			        	        		rs.next();
			        	        		if(rs.getString(1).equals("0")) {
			        		        		System.out.println("Error: No such song!");
			        		        		continue;
			        	        		}
			        	        		else {
		    	        				stmt.executeUpdate("insert into db4.`contains` values\r\n" + 
		    	        						"(\'"+pNum+"\',\'"+ss+"\');");
		    	        				System.out.println("added "+ss+" to "+pNum);
			        	        		}
		    	        			}
		    	        			else if(p_command.equals("D")){                    //delete
		    	        				System.out.println("Enter sound source number to delete (ex: S00001): ");
		    	        				String ss=s.nextLine();
		    	        				d.rs=stmt.executeQuery("select exists (select * from db4.`sound_source` ss\r\n" + 
			        	        				"where ss.Snum=\'"+p_command+"\')as success;");
			        	        		rs.next();
			        	        		if(rs.getString(1).equals("0")) {
			        		        		System.out.println("Error: No such song!");
			        		        		continue;
			        	        		}else {
		    	        				stmt.executeUpdate("delete from db4.`contains`\r\n" + 
		    	        						"where Snum =\'"+ss+"\' and Pnum= \'"+pNum+"\'");
		    	        				System.out.println("deleted "+ss+" from "+pNum);
			        	        		}
		    	        			}
		    	        			else if(p_command.equals("DP")) {                //delete playlist
		    	        				System.out.print("Do you want to delete all songs in your playlist?(Y/N): ");
		    	        				String c=s.nextLine();
		    	        				if(c.equals("Y")) {
		    	        					stmt.executeUpdate("delete from db4.`contains` \r\n" + 
		    	        							"where Pnum=\'"+pNum+"\';\r\n");
		    	        				}
		    	        		}
		    	       
		    	        	
		    	        		else {
		    	        			System.out.println("Error: Wrong command\nReturning to previous menu...");
		    	        		}
	    	        		}
		    	        	else if(p_command.equals("3")){                     //return
		    	        			continue;
		    	        	}
	    	        		else System.out.println("Error: Wrong command\r\n" + 
	    	        				"Returning to previous menu...");
	    	        		System.out.println();
	    	        		break;
	    	        	    }
	    	        		else {
	    	        			System.out.println("Error: Wrong command\r\n" + 
		    	        				"Returning to previous menu...");
		    	        		System.out.println();
	    	        		}
	    	        		break;
	    	        	///////////////////////////////////		    	  
	    	        	case 3:// Play sound sources
	    	        		System.out.println();
	    	        		d.rs=stmt.executeQuery("select ss.Snum ,ss.Sname ,ss.artist \r\n" + 
	    	        				"from db4.sound_source ss \r\n" + 
	    	        				"");
	    	        		while(rs.next()) {
	    	        			System.out.println(rs.getString("Snum")+" "+rs.getString("Sname")+"-"+rs.getString("artist"));
	    	        		}
	    	        		System.out.print("\n1) Enter X to return to previous menu\n"
	    	        				+ "2) Enter sound source number to play(ex: S00001): ");
	    	        		s.nextLine();
    	        			p_command=s.nextLine();
    	        			if(p_command.equals("X")) continue;             //return
    	        			else {
    	        				d.rs=stmt.executeQuery("select exists (select * from db4.`sound_source` ss\r\n" + 
	        	        				"where ss.Snum=\'"+p_command+"\')as success;");
	        	        		rs.next();
	        	        		if(rs.getString(1).equals("0")) {
	        		        		System.out.println("Error: No such song!");
	        		        		continue;
	        	        		}
    	        			stmt.executeUpdate("update db4.plays \r\n" +               //play
    	        					"set nTimes = nTimes+1\r\n" + 
    	        					"where Unum=\'"+Unum+"\'and Snum=\'"+p_command+"\';");
    	        			System.out.println("Playing "+p_command+"...");
    	        			}
    	        			break;
    	        		///////////////////////////////////
	    	        	case 4: //Buy sound sources
	    	        		System.out.println("");
	    	        		d.rs=stmt.executeQuery("select ss.Snum, ss.Sname, ss.artist, ss.price \r\n" + 
	    	        				"from db4.sound_source ss\r\n" + 
	    	        				"where ss.price >0;");
	    	        		while(rs.next()) {
	    	        			System.out.println(rs.getString("Snum")+" "+rs.getString("Sname")+" - "
	    	        		+rs.getString("artist")+ "   Price: "+rs.getString("price"));
	    	        		}
	    	        		System.out.print("1) Enter X to return to previous menu\n"
	    	        				+ "2) Enter sound source number to purchase (ex: S00001): ");
	    	        		s.nextLine();
    	        			p_command=s.nextLine();
    	        			if(p_command.equals("X")) continue;                     //return
    	        			else {
    	        				d.rs=stmt.executeQuery("select exists (select * from db4.`sound_source` ss\r\n" + 
	        	        				"where ss.Snum=\'"+p_command+"\')as success;");
	        	        		rs.next();
	        	        		if(rs.getString(1).equals("0")) {
	        		        		System.out.println("Error: No such song!");
	        	        		}
	        	        		else {
	        	        			d.rs=stmt.executeQuery("select exists (select * from db4.purchases p\r\n" + 
	        	        					"where p.Snum=\'"+ p_command+"\'and p.Unum=\'"+Unum+"\')as success");
	        	        			rs.next();
	        	        			if(rs.getString(1).equals("1")) {
	        	        				System.out.println("You already have this song!");
	        	        			}
	        	        			else {
	        	        				stmt.executeUpdate("insert into db4.purchases values\r\n" +        //purchase
	        	        						"(\'"+Unum+"\',\'"+p_command+"\');");
	        	        				System.out.println("Purchase completed! ("+p_command+")");
	        	       				}
	        	      			}
	        	       		
    	        			}	
    	        			break;
    	        		///////////////////////////////////
	    	        	case 5://top 10
    	        			d.rs=stmt.executeQuery("select ss.Snum ,ss.Sname,ss.artist,SUM(nTimes)\r\n" + 
	        						"from db4.sound_source ss join db4.plays p on ss.Snum =p.Snum \r\n" + 
	        						"group by ss.Snum\r\n" + 
	        						"order by SUM(nTimes) desc\r\n" + 
	        						"");
	        				int i=0;
	        				System.out.println();
	        				System.out.println("--------Top 10 Music Chart--------");
	        				while(rs.next() && i<=9) {
	        					i++;
	        					System.out.println(i+". "+rs.getString("Snum")+" "+rs.getString("Sname")
	        					+" - "+rs.getString("artist")+"    Play count: "+rs.getString("Sum(nTimes)"));
	        				}
	        				System.out.println("--------------------------------");
	        				System.out.print("\n1) Enter X to return to previous menu\n"
	    	        				+ "2) Enter sound source number to play(ex: S00001): ");
	    	        		s.nextLine();
    	        			p_command=s.nextLine();
    	        			if(p_command.equals("X")) continue;             //return
    	        			else {
    	        				d.rs=stmt.executeQuery("select exists (select * from db4.`sound_source` ss\r\n" + 
	        	        				"where ss.Snum=\'"+p_command+"\')as success;");
	        	        		rs.next();
	        	        		if(rs.getString(1).equals("0")) {
	        		        		System.out.println("Error: No such song!");
	        	        		}
	        	        		else {
    	        			stmt.executeUpdate("update db4.plays \r\n" +               //play
    	        					"set nTimes = nTimes+1\r\n" + 
    	        					"where Unum=\'"+Unum+"\'and Snum=\'"+p_command+"\';");
    	        			System.out.println("Playing "+p_command+"...");
	        	        		}
    	        			}
	        				break;
    	        		///////////////////////////////////
	    	        	case 6://Edit user information
	    	        		System.out.println("1. Edit phoneNumber\n"
	    	        				+ "2. Edit bank account");
	    	        		System.out.print("Enter command: ");
	    	        		s.nextLine();
	    		        	String e_command=s.nextLine();
	    		        	if(e_command.equals("1")) {                 //phone number
	    		        		d.rs=stmt.executeQuery("select u.phoneNum \r\n" + 
	    		        				"	    		        		from db4.`user` u\r\n" + 
	    		        				"	    		        		where u.Unum =\'"+Unum+"\';");
		    	        		while(rs.next()) {
		    	        			System.out.println("Original phone number: "+rs.getString("phoneNum"));
		    	        		}
		    	        		System.out.println("1) Enter X to cancel\n"    
		    	        				+ "2) Enter new phone number: ");
		    	        		e_command=s.nextLine();
		    	        		if(e_command.equals("X")) continue;
		    	        		else {
		    	        			stmt.executeUpdate("update db4.`user` u\r\n" + 
		    	        					"set u.phoneNum =\'"+e_command+"\'\r\n" + 
		    	        					"where u.Unum =\'"+Unum+"\';");
		    	        		}
	    		        	}
	    		        	else if(e_command.equals("2")) {                 //account number
	    		        		d.rs=stmt.executeQuery("select accNum\r\n" + 
	    		        				"from db4.bank_account ba \r\n" + 
	    		        				"where ba.ownerUNum =\'"+Unum+"\';");
		    	        		while(rs.next()) {
		    	        			System.out.println("Original bank account: "+rs.getString("accNum"));
		    	        		}
		    	        		System.out.println("1) Enter X to cancel\n"
		    	        				+ "2) Enter new bank account: ");
		    	        		e_command=s.nextLine();
		    	        		if(e_command.equals("X")) continue;            //return
		    	        		else {
		    	        			stmt.executeUpdate("update db4.bank_account \r\n" + 
		    	        					"set accNum =\'"+e_command+"\'\r\n" + 
		    	        					"where ownerUNum =\'"+Unum+"\'");
		    	        		}
	    		        	}
	    		        	break;
	    		        default: System.out.println("Error: Wrong command!");;
	        		}

	        	}
	        	break;
///////////////////////////////////////////////////////////////////////////////////MANAGER
	        	case 2: //Manager menu
	        		System.out.print("Enter your manager number (ex: M001): ");
	        		s.nextLine();
	        		String Mnum=s.nextLine();
	        		d.rs=stmt.executeQuery("select exists (select * from db4.`manager` m\r\n" + 
	        				"where m.Mnum=\'"+Mnum+"\')as success;");
	        		rs.next();
	        		if(rs.getString(1).equals("0")) {
		        		System.out.println("Error: No such manager!");
		        		continue;
	        		}
	        		int m_cmd=9999;
	        		while(m_cmd!=0) {
	        			System.out.println("\n0. Return to previous menu\n"
	        					+ "1. Manager information\n"
	        					+ "2. Managing songs\n"
	        					+ "3. Managing users\n"
	        					+ "4. Add songs\n"
	        					+ "5. Delete songs\n"
	        					+ "6. Top 10 Music chart\n"
	        					+ "7. Edit manager information");
	        			System.out.print("Enter command: ");
	        			m_cmd=s.nextInt();
	        			switch (m_cmd) {
	    	        	case 0: //Return to previous menu 
	    	        		continue;
	    	        	///////////////////////////////////
	    	        	case 1: //View manager information
	    	        		d.rs=stmt.executeQuery("select m.Fname,m.Lname, m.phoneNum\r\n" + 
	    	        				"from db4.`manager` m " +
	    	        				"where m.Mnum =\'"+Mnum+"\'");
	    	        		while(rs.next()) {
	    	        			System.out.println("\nFirst Name: "+ rs.getString(1)+
	    	        					"\nLast Name: "+rs.getString(2)
	    	        					+"\nPhone number: "+rs.getString(3));
	    	        		}
	    	        		break;
	        			
	        			case 2: //View sound sources
	        				d.rs=stmt.executeQuery("select Snum, Sname, artist, releaseDate, lengthSec, price\r\n" + 
	        						"from db4.sound_source ss " + 
	        						"where ss.chargeMnum =\'"+Mnum+"\';\r\n" );
	        				System.out.println();
	        				while(rs.next()) {
	    	        			System.out.println(rs.getString("Snum")+" "+rs.getString("Sname")+" - "+rs.getString("artist")
	    	        						+ "\nPrice: "+rs.getString("price")
	    	        						+ "\nTotal length: "+rs.getString("lengthSec")+" sec\n");
	    	        		}
	        				break;
	        			case 3: //view user
	        				d.rs=stmt.executeQuery("select u.Fname ,u.Lname, u.phoneNum ,u.Unum \r\n" + 
	        						"from db4.`user` u \r\n" + 
	        						"where u.chargeMnum =\'"+Mnum+"\';");
	        				System.out.println();
	        				while(rs.next()) {
	        					System.out.println(rs.getString("Unum")+" "+rs.getString("Lname")+" "
	        				+rs.getString("Fname")+" Phone number: "+rs.getString("phoneNum"));
	        				}
	        				break;
	        			case 4: //add sound source
	        				s.nextLine();
	        				System.out.print("Add new sound source? (Y/N): ");
	        				String string=s.nextLine();
	        				if(string.equals("Y")) {
	        					System.out.print("\nNumber: ");
	        					String Snum=s.nextLine();
	        					System.out.print("\nName: ");
	        					String Sname=s.nextLine();
	        					System.out.print("\nArtist: ");
	        					String artist=s.nextLine();
	        					System.out.print("\nPrice: ");
	        					String price=s.nextLine();
	        					System.out.print("\nLength(sec): ");
	        					String lengthSec=s.nextLine();
	        					System.out.print("\nRelease date: ");
	        					String releaseDate=s.nextLine();
	        					stmt.executeUpdate("insert into db4.sound_source values\r\n" + 
	        							"(\'"+Snum+"\',\'"+Sname+"\',\'"+price+"\',\'"+lengthSec
	        							+"\',\'"+Mnum+"\',\'"+releaseDate+"\',\'"+artist+"\');");
	        				}
	        				else continue;
	        				break;      
	        			case 5:
	        				s.nextLine();
	        				System.out.print("Delete existing sound source? (Y/N): ");
	        				String str=s.nextLine();
	        				System.out.println();
	        				if(str.equals("Y")) {
	        					System.out.print("Enter Song number to delete (ex.S00001) : ");
	        					String Snum=s.nextLine();
	        					
	        	        		d.rs=stmt.executeQuery("select exists (select * from db4.`sound_source` ss\r\n" + 
	        	        				"where ss.Snum=\'"+Snum+"\')as success;");
	        	        		rs.next();
	        	        		if(rs.getString(1).equals("0")) {
	        		        		System.out.println("Error: No such song!");
	        		        		continue;
	        	        		}
	        					
	        					stmt.executeUpdate("delete from db4.purchases where Snum=\'"+Snum+"\';");
	        					stmt.executeUpdate("delete from db4.plays where Snum=\'"+Snum+"\';");
	        					stmt.executeUpdate("delete from db4.`contains` where Snum=\'"+Snum+"\';");
	        					stmt.executeUpdate("delete from db4.sound_source where Snum=\'"+Snum+"\';");
	        				}
	        				else continue;
	        				break;
	        			case 6:
	        				d.rs=stmt.executeQuery("select ss.Snum ,ss.Sname,ss.artist,SUM(nTimes)\r\n" + 
	        						"from db4.sound_source ss join db4.plays p on ss.Snum =p.Snum \r\n" + 
	        						"group by ss.Snum\r\n" + 
	        						"order by SUM(nTimes) desc\r\n" + 
	        						"");
	        				int i=0;
	        				System.out.println();
	        				System.out.println("--------Top 10 Music Chart--------");
	        				while(rs.next() && i<=9) {
	        					i++;
	        					System.out.println(i+". "+rs.getString("Snum")+" "+rs.getString("Sname")
	        					+" - "+rs.getString("artist")+"    Play count: "+rs.getString("Sum(nTimes)"));
	        				}
	        				System.out.println("--------------------------------");
	        				break;
	        				
	        			case 7:              //edit manager information
	    		        		d.rs=stmt.executeQuery("select m.phoneNum \r\n" + 
	    		        				"	    		        		from db4.`manager` m\r\n" + 
	    		        				"	    		        		where m.Mnum =\'"+Mnum+"\';");
		    	        		while(rs.next()) {
		    	        			System.out.println("\nOriginal phone number: "+rs.getString("phoneNum"));
		    	        		}
		    	        		System.out.println("1) Enter X to cancel\n"    
		    	        				+ "2) Enter new phone number: ");
		    	        		s.nextLine();
		    	        		String c=s.nextLine();
		    	        		if(c.equals("X")) continue;
		    	        		else {
		    	        			stmt.executeUpdate("update db4.`manager` m\r\n" + 
		    	        					"set m.phoneNum =\'"+c+"\'\r\n" + 
		    	        					"where m.Mnum =\'"+Mnum+"\';");
		    	        		}
		    	        		break;
	    		        }

	        		}
	        		break;
	        		
///////////////////////////////////////////////////////////////////////////////////	
	        	default: 
	        		System.out.println("Error: Wrong command");
	        }
	    }
	}
}



