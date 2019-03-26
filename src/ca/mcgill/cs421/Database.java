package ca.mcgill.cs421;

import java.sql.*;
import java.util.Scanner;
import java.util.*;

public class Database {

    private static final String URL = "jdbc:postgresql://comp421.cs.mcgill.ca:5432/cs421";
    private static final String USERNAME = "cs421g27";
    private static final String PASSWORD = "HelloCS421";

    private Connection db;


    public Database() {}

    /*---------------------- CONNECTION METHODS --------------------------*/

    public Boolean connect(){
        System.out.println("Attempt to connect ....");
        try {
            db = DriverManager.getConnection(URL, USERNAME, PASSWORD);
            System.out.println("Connection successfully opened");
            return true;
        } catch (SQLException e) {
            System.out.println("Connection failed");
            System.out.println("Error : " +e.getMessage());
            return false;
        }
    }

    public void disconnect(){
        System.out.println("Attempt to disconnect ....");
        try {

            db.close();
        } catch (SQLException e) {
            System.out.println("Disconnection failed");
            System.out.println("Error :" +e.getMessage());
        }
        System.out.println("Connection successfully closed");

    }

    /*----------------------ADD PRESCRIPTIONS--------------------------*/

    /*
    * Description:
    * User will be asked to enter a patientID and a doctorID. This is to ensure that
    * the specified patient and specified doctor are in the database. If not, the user will need
    * to add the patient or the doctor in to the database because a prescription needs to be associated
    * to existing patient and doctor.
    * */

    public void addPrescription(){
        Scanner sc = new Scanner(System.in);
        int patientID;
        int doctorID;
        int drugID;
        int suggestedID;
        try {
            System.out.println("Please Enter the drugID");
            drugID = sc.nextInt();
            if (!findQueryDrugs(drugID)) {
                System.out.println("Prescription can't be added");
                return;
            }
            System.out.println("Please Enter the patientID");
            patientID = sc.nextInt();
            if (!findQueryPatients(patientID)) {
                suggestedID = suggestID("patients","patientid");
                System.out.println("Suggested patientid = "+ (++suggestedID));
                addQuery("patients");
            }
            System.out.println("Please Enter the doctorID");
            doctorID = sc.nextInt();
            if (!findQueryDoctors(doctorID)) {
                suggestedID = suggestID("doctors","doctorid");
                System.out.println("Suggested doctorid = "+ (++suggestedID));
                addQuery("doctors");
            }
            suggestedID = suggestID("prescriptions","pid");
            System.out.println("Suggested pid = "+ (++suggestedID));
            addQuery("prescriptions");

            addQuery("prescriptionofdrugs");

            System.out.println("Prescription is successfully added");

        }catch (SQLException e){
            System.out.println(e.getMessage());
        }
    }


    public boolean findQueryPatients(int patientID) throws SQLException{

        Boolean isFound =true;
        Statement st = db.createStatement();
        ResultSet rs = st.executeQuery("SELECT * FROM patients WHERE patientid ="+patientID);
        if (!rs.next()){
            System.out.println("Patient NOT FOUND");
            isFound = false;
        }
        rs.close();
        st.close();
        return isFound;
    }

    public boolean findQueryDrugs(int drugID) throws SQLException{

        Boolean isFound =true;
        Statement st = db.createStatement();
        ResultSet rs = st.executeQuery("SELECT * FROM drugs WHERE did ="+drugID);
        if (!rs.next()){
            System.out.println("DRUG NOT FOUND");
            isFound = false;

        }
        rs.close();
        st.close();
        return isFound;
    }

    public boolean findQueryDoctors(int doctorID) throws SQLException{

        Boolean isFound =true;
        Statement st = db.createStatement();
        ResultSet rs = st.executeQuery("SELECT * FROM doctors WHERE doctorid ="+doctorID);
        if (!rs.next()){
            System.out.println("Doctor NOT FOUND");
            isFound = false;

        }
        rs.close();
        st.close();
        return isFound;
    }

    public void addQuery(String tableName) throws SQLException{
        Scanner sc = new Scanner(System.in);

        Statement st = db.createStatement();
        ResultSet rs = st.executeQuery("SELECT * FROM "+tableName);
        ResultSetMetaData rsmd = rs.getMetaData();
        int numCol = rsmd.getColumnCount();

        String attribute = "(";
        String values ="(";

        for(int i =1; i <= numCol; i++){
            printFormat(tableName,rsmd.getColumnName(i),rsmd.getColumnClassName(i));
            attribute += rsmd.getColumnName(i);
            attribute += ",";
            if(rsmd.getColumnTypeName(i).contains("char") || rsmd.getColumnTypeName(i).contains("date")){
                values += "'" + sc.nextLine() + "'";
                values += ",";
                continue;
            }
            values += sc.nextLine();
            values += ",";
        }
        attribute = attribute.substring(0,attribute.length()-1);
        values = values.substring(0,values.length()-1);
        attribute += ")";
        values += ")";

        String query = "INSERT INTO "+tableName +" "+ attribute + " VALUES " + values;
        st.executeUpdate(query);
        rs.close();
        st.close();
    }

    public int suggestID(String tableName, String pk) throws SQLException{
        int id = 0;
        Statement st = db.createStatement();
        ResultSet rs = st.executeQuery("SELECT " + pk +" from "+tableName+" WHERE "+pk+ " >= ALL(SELECT "+pk+" FROM "+tableName+");");
        if(rs.next()){
            id = Integer.valueOf(rs.getString(pk));
        }
        st.close();
        rs.close();
        return id;
    }

    public void printFormat(String tableName, String columnName, String className ) throws SQLException{
        String format="";
        if(columnName.contains("gender")){
            format = "M or F";
        } else if(className.contains("Integer")){
            format = "integer";
        } else if(className.contains("String")) {
            format = "string";
            if(columnName.contains("phone")){
                format += " X-XXX-XXX-XXXX";
            }else if(columnName.contains("address")){
                format += " Ex. 123 St-Laurent St. Montreal QC H3R 1S3";
            }
        } else if(className.contains("Date")) {
            format = "date YYYY-MM-DD";
        }
        System.out.println("Enter "+ columnName +"  Hint:("+ format+")");

    }

    /*----------------------UPDATE PRESCRIPTION STATUS--------------------------*/

    /*
    * Description:
    * One utility of this database is to track the status of prescriptions during its
    * preparation and its delivery. This option allows a user to update the status in both
    * situations.
    * */

    public boolean findQueryPrescriptions(String tablename,int pid) throws SQLException{

        Boolean isFound =true;
        Statement st = db.createStatement();
        ResultSet rs = st.executeQuery("SELECT * FROM "+tablename+ " WHERE pid ="+pid);
        if (!rs.next()){
            System.out.println("Prescription NOT FOUND");
            isFound = false;
        }
        rs.close();
        st.close();
        return isFound;
    }

    @SuppressWarnings("Duplicates")
    public void updatePreparationStatus() throws SQLException{
        int pid;
        int input;
        String status;
        Scanner sc = new Scanner(System.in);

        System.out.println("Enter prescription id (pid)");
        pid = sc.nextInt();
        sc.nextLine();
        if(!findQueryPrescriptions("preparesprescriptions",pid)){
            return;
        }

        Statement st = db.createStatement();
        ResultSet rs = st.executeQuery("SELECT * FROM preparesprescriptions WHERE pid = "+ pid );
        if(rs.next()) {
            System.out.println("Current status : " + rs.getString("preparation_status"));
        }
        System.out.println("Choose a status (#)");
        System.out.println("1) Incomplete");
        System.out.println("2) In Progress");
        System.out.println("3) Finished");
        System.out.println("4) Cancel update");
        input = sc.nextInt();
        switch(input){
            case 1: status = "'Incomplete'";break;
            case 2: status = "'In Progress'";break;
            case 3: status = "'Finished'";break;
            case 4: return;
            default: System.out.println("Invalid input"); return;
        }

        String query = "UPDATE preparesprescriptions SET preparation_status = "+status+" WHERE pid = "+pid;
        st.executeUpdate(query);
        st.close();
        rs.close();
        System.out.println("Prescription preparation status is successfully updated");
    }
    @SuppressWarnings("Duplicates")
    public void updateDeliveryStatus() throws SQLException{
        int pid;
        int input;
        String status;
        Scanner sc = new Scanner(System.in);

        System.out.println("Enter prescription id (pid)");
        pid = sc.nextInt();
        sc.nextLine();
        if(!findQueryPrescriptions("deliversprescriptions",pid)){
            return;
        }

        Statement st = db.createStatement();
        ResultSet rs = st.executeQuery("SELECT * FROM deliversprescriptions WHERE pid = "+ pid );
        if(rs.next()) {
            System.out.println("Current status : " + rs.getString("delivery_status"));
        }

        System.out.println("Choose a status (#)");
        System.out.println("1) Not delivered");
        System.out.println("2) Delivered");
        System.out.println("3) Cancel update");

        input = sc.nextInt();
        switch(input){
            case 1: status = "'Not delivered'";break;
            case 2: status = "'Delivered'";break;
            case 3: return;
            default: System.out.println("Invalid input"); return;
        }

        String query = "UPDATE deliversprescriptions SET delivery_status = "+status+" WHERE pid = "+pid;
        st.executeUpdate(query);
        st.close();
        rs.close();
        System.out.println("Prescription delivery status is successfully updated");

    }

    public void updatePrescriptionStatus() {
        Scanner sc = new Scanner(System.in);
        String input="";
        try {
            System.out.println("Enter P for Preparation or D for Delivery?");
            input = sc.nextLine().toUpperCase();
            switch (input) {
                case ("P"):
                    updatePreparationStatus();
                    break;
                case ("D"):
                    updateDeliveryStatus();
                    break;
                default:
                    System.out.println("Invalid input");
                    break;
            }
        } catch (SQLException e){
            System.out.println(e.getMessage());
        }
    }

    /*----------------------DISPLAY DRUGS--------------------------*/

    /*
    * Description:
    * User will be asked to choose a pharmacy chain and a pharmacy.
    * Then, the list of drugs of this particular pharmacy will be displayed.
    * did, drug_name, exp_date, dmid, price, quantity
    * */

    @SuppressWarnings("Duplicates")
    public void displayDrugs() {

        String pchain ="";
        String pharmacy="";

        try {

            System.out.println("Select a pharmacy chain (#)");
            String query = "SELECT * FROM pharmacychains";
            pchain = getValue("chain_name", query);
            if (pchain == null) {
                return;
            }
            System.out.println("Select a pharmacy (#)");
            query = "SELECT * FROM pharmacies WHERE chain_name ='" + pchain + "'";
            pharmacy = getValue("pharm_name", query);
            if (pharmacy == null) {
                return;
            }

            query = "SELECT did,drug_name,exp_date,dmid,price,quantity FROM drugs d NATURAL JOIN sellsdrugs s WHERE pharm_name ='" + pharmacy + "'AND chain_name='" + pchain + "'";

            int[] attribute_size = {8, 30, 14, 8, 8, 12};
            Statement st = db.createStatement();
            ResultSet rs = st.executeQuery(query);
            ResultSetMetaData rsmd = rs.getMetaData();

            //Print attributes
            int numCol = rsmd.getColumnCount();
            System.out.print("|");
            for (int i = 1; i <= numCol; i++) {
                print(rsmd.getColumnLabel(i), attribute_size[i - 1]);
                System.out.print("|");
            }
            System.out.println();

            //Print tuples
            while (rs.next()) {
                System.out.print("|");
                for (int i = 1; i <= numCol; i++) {
                    print(rs.getString(i), attribute_size[i - 1]);
                    System.out.print("|");
                }
                System.out.println();
            }
            System.out.println();
            st.close();
            rs.close();

        }catch(SQLException e){
            System.out.println(e.getMessage());
            return;
        }
    }

    public String getValue(String colName,String query)  throws SQLException{
        Scanner sc = new Scanner(System.in);
        int input;
        List<String> list = new ArrayList<String>();

        Statement st = db.createStatement();
        ResultSet rs = st.executeQuery(query);

        int i = 1;
        while (rs.next()) {
            int col = rs.findColumn(colName);
            list.add(rs.getString(col));
            System.out.println(i+")"+list.get(i-1));
            i++;

        }
        rs.close();
        st.close();

        input = sc.nextInt();
        if(input > list.size() || input == 0){
            System.out.println("Invalid input");
            return null;
        }
        return list.get(input-1);
    }

    public void print(String text,int size){
        int lspace = 0, rspace = 0;
        int space = size-text.length();
        if(space%2 == 0){
            lspace = space/2;
            rspace = space/2;
        }
        else{
            lspace = space/2;
            rspace = space/2+1;
        }
        for(int i =0; i <lspace;i++){
            System.out.print(" ");
        }
        System.out.print(text);
        for(int i =0; i <rspace;i++){
            System.out.print(" ");
        }
    }

    /*----------------------DISPLAY PRESCRIPTION OF A PATIENT--------------------------*/

    /*
     * Description:
     * User will be asked to enter a patientID. If the patientID is invalid (no patient has this
     * id in the database) then the user will be notified and will be back on the main menu.
     * Else, the list of prescriptions related to the specified patient will be displayed
     * */


    @SuppressWarnings("Duplicates")
    public void displayPrescriptions(){
        Scanner sc = new Scanner(System.in);
        int patientID;
        try {
            System.out.println("Please enter a patientID");
            patientID = sc.nextInt();
            if (!findQueryPatients(patientID)) {
                return;
            }

            String query ="SELECT a.pid,complaint,a.did,drug_name,exp_date, prescription_date FROM prescriptionofdrugs a,prescriptions b, drugs d "+ "WHERE a.pid= b.pid AND a.did= d.did AND b.patientid=" + patientID;

            int[] attribute_size = {8, 25, 8, 25, 12, 20};
            Statement st = db.createStatement();
            ResultSet rs = st.executeQuery(query);
            ResultSetMetaData rsmd = rs.getMetaData();

            //Print attributes
            int numCol = rsmd.getColumnCount();
            System.out.print("|");
            for (int i = 1; i <= numCol; i++) {
                print(rsmd.getColumnLabel(i), attribute_size[i - 1]);
                System.out.print("|");
            }
            System.out.println();

            //Print tuples
            while (rs.next()) {
                System.out.print("|");
                for (int i = 1; i <= numCol; i++) {
                    print(rs.getString(i), attribute_size[i - 1]);
                    System.out.print("|");
                }
                System.out.println();
            }
            System.out.println();
            rs.close();
            st.close();

        }catch(SQLException e){
            System.out.println(e.getMessage());
            return;
        }

    }

    /*----------------------------UDPATE DRUG INVENTORY--------------------------------*/

    /*
     * Description:
     * When restock happens, it is important to update drug inventory. User will be asked to
     * enter a drug id. If the drugID is not valid (no drugs has this id in the database) then the
     * user will be notified and will be back on the main menu. Else, user can update the following
     * information: price, quantity, exp_date
     * */

    @SuppressWarnings("Duplicates")
    public void updateDrugInventory(){
        Scanner sc = new Scanner(System.in);
        int drugID = 0;
        String pchain;
        String pharmacy;

        try{
            System.out.println("Select a pharmacy chain (#)");
            String query = "SELECT * FROM pharmacychains";
            pchain = getValue("chain_name", query);
            if (pchain == null) {
                return;
            }
            System.out.println("Select a pharmacy (#)");
            query = "SELECT * FROM pharmacies WHERE chain_name ='" + pchain + "'";
            pharmacy = getValue("pharm_name", query);
            if (pharmacy == null) {
                return;
            }

            System.out.println("Select a drug id (#)");
            query = "SELECT * FROM drugs NATURAL JOIN sellsdrugs WHERE chain_name ='" + pchain + "' AND pharm_name ='" + pharmacy + "'" ;
            String option = getValue("did", query);
            if(option != null){
                drugID = Integer.valueOf(option);
            } else{
                return;
            }

            Statement st = db.createStatement();
            ResultSet rs = null;
            while(true){
                String q;
                System.out.println("1) Update exp_date");
                System.out.println("2) Update quantity");
                System.out.println("3) Update price");
                System.out.println("4) End update");
                int choice = sc.nextInt();
                sc.nextLine();
                switch(choice){
                    case 1:
                        q = "SELECT * FROM drugs WHERE did = "+ drugID;
                        rs = st.executeQuery(q);
                        if(rs.next()){
                            System.out.println("Current exp_date :" + rs.getString("exp_date"));
                        }
                        System.out.println("Enter a new exp_date YYYY-MM-DD ");
                        String date = sc.nextLine();
                        q = "UPDATE drugs SET exp_date = '"+date+"' WHERE did = " + drugID;
                        st.executeUpdate(q);
                        System.out.println("Exp_date successfully updated");
                        break;
                    case 2:
                        q = "SELECT * FROM sellsdrugs WHERE did = " + drugID + " AND chain_name ='" + pchain + "' AND pharm_name ='" + pharmacy + "'";
                        rs = st.executeQuery(q);
                        if(rs.next()){
                            System.out.println("Current quantity :" + rs.getString("quantity"));
                        }
                        System.out.println("Current quantity :");
                        System.out.println("Enter a new quantity (integer)");
                        int quantity = Integer.valueOf(sc.nextLine());
                        q = "UPDATE sellsdrugs SET quantity = "+quantity+" WHERE did = " + drugID + " AND chain_name ='" + pchain + "' AND pharm_name ='" + pharmacy + "'";
                        st.executeUpdate(q);
                        System.out.println("Quantity successfully updated");
                        break;
                    case 3:
                        q = "SELECT * FROM sellsdrugs WHERE did = " + drugID + " AND chain_name ='" + pchain + "' AND pharm_name ='" + pharmacy + "'";
                        rs = st.executeQuery(q);
                        if(rs.next()){
                            System.out.println("Current price :" + rs.getString("price"));
                        }
                        System.out.println("Current price :");
                        System.out.println("Enter a new price (integer)");
                        int price = Integer.valueOf(sc.nextLine());
                        q = "UPDATE sellsdrugs SET price = "+price+" WHERE did = " + drugID + " AND chain_name ='" + pchain + "' AND pharm_name ='" + pharmacy + "'";
                        st.executeUpdate(q);
                        System.out.println("Price successfully updated");
                        break;
                    case 4:
                        rs.close();
                        st.close();
                        return;
                    default: System.out.println("Invalid input"); break;
                }
            }
        } catch(SQLException e){
            System.out.println(e.getMessage());
        }
    }
}
