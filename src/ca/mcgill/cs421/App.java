package ca.mcgill.cs421;

import java.sql.SQLException;
import java.util.Scanner;

public class App {


    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        String input="";
        Boolean quit = false;
        Database db = new Database();

        //Database connection
        db.connect();

        //Menu
        while(!quit) {
            System.out.println("|-----Welcome to the Pharmacy Database------|");
            System.out.println("|---------------- Main Menu ----------------|");
            System.out.println("| A) ADD a new prescription                 |");
            System.out.println("| B) DELETE ???                             |");
            System.out.println("| C) UPDATE prescription status             |");
            System.out.println("| D) View drugs in selected pharmacy        |");
            System.out.println("| E) ????                                   |");
            System.out.println("| F) QUIT application                       |");
            System.out.println("|___________________________________________|");
            System.out.println(" Enter the letter of the action to perform:  ");

            input = sc.next().toUpperCase();
            switch(input){
                case "A":
                    System.out.println("ADD");
                    try {
                        db.addPrescription();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                    break;
                case "B":
                    System.out.println("DELETE");
                    break;
                case "C":
                    System.out.println("UPDATE");
                    try {
                        db.updatePrescriptionStatus();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                    break;
                case "D":
                    System.out.println("VIEW");
                    try {
                        db.displayDrugs();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                    break;
                case "E":
                    System.out.println("SELECT");
                    break;
                case "F":
                    System.out.println("QUIT");
                    quit = true;
                    break;

                default:
                    System.out.println("Invalid Input !!!");
                    break;
            }

        }

        //Close database connection
        db.disconnect();


    }
}
