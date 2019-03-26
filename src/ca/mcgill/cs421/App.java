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
        if(db.connect()) {

            //Menu
            while (!quit) {
                System.out.println("|-----Welcome to the Pharmacy Database------|");
                System.out.println("|---------------- Main Menu ----------------|");
                System.out.println("| A) ADD a new prescription                 |");
                System.out.println("| B) UPDATE prescription status             |");
                System.out.println("| C) VIEW prescriptions of a patient        |");
                System.out.println("| D) VIEW drugs in selected pharmacy        |");
                System.out.println("| E) UPDATE drug inventory                  |");
                System.out.println("| Q) QUIT application                       |");
                System.out.println("|___________________________________________|");
                System.out.println(" Enter the letter of the action to perform:  ");

                input = sc.next().toUpperCase();
                switch (input) {
                    case "A":
                        db.addPrescription();
                        break;
                    case "B":
                        db.updatePrescriptionStatus();
                        break;
                    case "C":
                        db.displayPrescriptions();
                        break;
                    case "D":
                        db.displayDrugs();
                        break;
                    case "E":
                        db.updateDrugInventory();
                        break;
                    case "Q":
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
}
