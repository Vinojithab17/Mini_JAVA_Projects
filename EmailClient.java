
// 190650B 
import java.util.Scanner;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.time.LocalDate;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Properties;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Store;
import java.io.*;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/*=====================================================================================*/
abstract class Email_Client {
 public static void main(String[] args) {
 Control_Unit.Wish(); //To send wish 
 //shared object to receiving mail
 // acts as a blocking queue and a observable
 Email_receiver mailQueue = new Email_receiver();
 // obsevers
 EmailStatPrinter printer = new EmailStatPrinter();
 EmailStatRecorder recoder = new EmailStatRecorder();
 mailQueue.addObserver(printer);//adding observers
 mailQueue.addObserver(recoder);
 // to get mail and put it in the queue
 Thread producer1 = new Thread(new MailChecker(mailQueue), "receiver_1");
 producer1.start();
 // cosumer gets the received mail and serializes it
 Thread Consumer1 = new Thread(new consumer(mailQueue), "consumer_1");
 Consumer1.start();
 Scanner input = new Scanner(System.in);
 while(true){
 
System.out.println("\n<<<===========================================================>>>\n"
 +"\nAvailable Options :\n\n"
 +"1 - Adding New Recipients\n"
 +"2 - Sending an E-mail\n"
 +"3 - Print all Recipients who have Birthdays on a given date\n"
 +"4 - Prints Details Of All Sent E-mails on a given date\n"
 +"5 - Print Total Recipients\n"
 +"0 - To close the Email client!\n"
 +"\n<<<===========================================================>>>\n");
 System.out.println("\nEnter Option Type : ");
 try {
 int option = input.nextInt();
 switch(option){
 case 1://Adding New Recipients
 System.out.println("\nInput formats: "
 +"\nOfficial: <name>,<email>,<designation>"
 +"\nOffice_friend: 
<name>,<email>,<designation>,<birthday-yyyy/mm/dd>"
 +"\nPersonal: <name>,<nick-name>,<email>,<birthday-yyyy/mm/dd>");
 System.out.print("\nEnter Details : ");
 String type = input.next();
 String PersonDetail = input.nextLine();
 Control_Unit.AddRecipeint(type+PersonDetail);
 break;
 
 case 2://Sending an E-mail
 try {
 System.out.println("\nEnter Email, Subject, Content : ");
 String Email = (input.next().split(","))[0];
 String Sub = (input.next().split(","))[0];
 String Cont = input.nextLine();
 Control_Unit.SendMail(Email, Sub, Cont);
 break;
 
 } catch (Exception e) {
 System.out.println("\nEnter details properly!\n");
 break;
 }
 
 case 3:
 //Birthdays members on a given date
 System.out.print("\nEnter date-(yyyy/mm/dd) : ");
 String date1 = input.next();
 Control_Unit.Birthday_Person(date1);
 break;
 
 case 4:// sent mail details
 try {
 System.out.print("\nEnter date-(yyyy/mm/dd) : ");
 String day = input.next();
 String year = day.split("/")[0];
 String month = day.split("/")[1];
 String date = day.split("/")[2];
 Control_Unit.Sent_mail_Details(year+"-"+month+"-"+date);
 break;
 } catch (Exception e) {
 System.out.println("Enter date properly\n");
 break;
 }
 
 case 5:
 //total recipients
 Control_Unit.getTotalMembers();
 break;
 
 case 0:
 // to exit the programe
 input.close();
 System.out.println("Closing Email client!!!");
 System.exit(1);
 default:
 System.out.println("\nOut of options!\nEnter numbers from 1 to 
5!\n");
 break;
 } 
 } catch (Exception e) {
 e.printStackTrace();
 input.close();
 System.out.println("\nWrong Inputs!\nEnter Proper Inputs!\n");
 }
 }
 
 }

}

/*
 * =============================================================================
 * ========
 */
class Email_receiver { // acts as a blocking queue and a observable

    private ArrayList<observer> observer_list;// list of observers
    private ArrayList<G_mail> mails;// to implement blocking queue
    private boolean mail_added;

    public Email_receiver() {
        // observer list
        this.observer_list = new ArrayList<observer>();
        // arraylist of maximum 10
        this.mails = new ArrayList<>(10);
    }

    public void addObserver(observer o) {// to add observers
        observer_list.add(o);
    }

    public void clearObservers() {// to clear all observers
        observer_list.clear();
    }

 public void updateAll(G_mail new_mail){
 // to notify all observers
 for(observer o :observer_list ){
 o.update("a mail Arrived on : "+ new_mail.getReceivedDate()+", at : 
"+new_mail.gettime());
 }
 }

    public synchronized void addmail(G_mail new_mail) {
        while (mail_added) {
            try {
                wait();
            } catch (Exception e) {
            }
        }
        updateAll(new_mail);// updates all obervers
        mails.add(new_mail);// adding it to mailqueue
        mail_added = true;
        notifyAll();// notifies all consumers
    }

    public synchronized G_mail getMail() {
        while (!mail_added) {
            try {
                wait();
            } catch (Exception e) {
            }
        }

        G_mail saved = mails.get(0);
        mails.remove(0);// removes form the queue
        mail_added = false;
        notifyAll();
        return saved;// returns the recieved mail

    }
}

/*
 * =============================================================================
 * ========
 */
class MailChecker implements Runnable {// to check mails continuesly
    private G_mail received_mail;
    private Email_receiver new_queue;// acts as blocking queue
    private String Received_folder; // received mail serialized folder
    private int totalMAils; // total mails in the serialized folder

    public MailChecker(Email_receiver new_queue) {
        this.new_queue = new_queue;
        Received_folder = "C:\\Users\\HP\\Documents\\Email_Client\\Received";
    }

    public void readMail() {
        try {
            Properties prop = new Properties();
            prop.put("mail.pop3.host", "pop.gmail.com");
            prop.put("mail.pop3.host", "995");
            prop.put("mail.pop3.starttls.enable", "true");
            Session emailSession = Session.getDefaultInstance(prop);
            Store store = emailSession.getStore("pop3s");
            store.connect("pop.gmail.com", "javatest190650b@gmail.com", "Password@123");
            Folder emailFolder = store.getFolder("INBOX");
            emailFolder.open(Folder.READ_ONLY);
            Message messages[] = emailFolder.getMessages();
            // total mails in the inbox
            int inboxMails = ((messages.length) - 1);
            if (inboxMails > totalMAils) {
                for (int newmail = totalMAils + 1; newmail <= inboxMails; newmail++) {
                    Message message = messages[newmail];
                    String sub = message.getSubject();// gets the subject
                    String from = ((message.getFrom()[0]).toString()).split(" ")[2];
                    from = from.replace("<", "");
                    from = from.replace(">", "");
                    // to separate the email address
                    String con = message.getContent().toString();// to get the contant
                    String[] datas = message.getSentDate().toString().split(" ");
                    received_mail = new G_mail(from, sub, con);// creating mail object
                    received_mail.setTime(datas[3]); // setting received time
                    received_mail.setReceivedDate(datas[1] + " " + datas[2] + " " + datas[5]);
                    new_queue.addmail(received_mail);// adding to queue
                    totalMAils++;
                }
            }
            emailFolder.close(true);
            store.close();
        } catch (Exception e) {
            System.out.println("Error!");
        }
    }

    public void run() {
        totalMails(); // to get the total mails in the serialized folder
        while (true) {
            readMail();
        } // will start to check for new mails continuesly

    }

    // to get the total mails in the serialized folder
    public void totalMails() {
        File folder = new File(Received_folder);
        File[] allSubFiles = folder.listFiles();
        for (File file : allSubFiles) {
            totalMAils++;
        }
    }
}

/*
 * =============================================================================
 * ========
 */
// consumer object will get the mails from the queue and serialize it
class consumer implements Runnable {
    private Email_receiver queue;

    public consumer(Email_receiver queue) {
        this.queue = queue;
    }

    public void run() {
        while (true) {
            // to serialize received mails
            SaveToHardisk.Save_Mails(queue.getMail(), "Received\\");
        }

    }
}

/*
 * =============================================================================
 * ========
 */
// to write the client list file
abstract class Add_To_TextFile {
    public static void AddDetails(String location, String data) {
        try (FileWriter writer = new FileWriter(location, true);
                BufferedWriter Buffer = new BufferedWriter(writer)) {
            Recipient NewRecip = FindRecipent.getRecipient(data);
            if (NewRecip == null) {
                System.out.println("\nWrong recipient Details!"
                        + "\nEnter Proper Details!\n");
            } else {
                Buffer.write(data + "\n");
                System.out.println("New Client Added!");
            }
        } catch (IOException e) {
            System.out.println("Error While Updating to File!");
        }
    }
}

/*
 * =============================================================================
 * ========
 */
abstract class Control_Unit {
    // to add new member
    public static void AddRecipeint(String data) {
        Add_To_TextFile.AddDetails("ClientList.txt", data);
    }

    // to get totel members
    public static void getTotalMembers() {
        GetInfo.getTotal();
    }

    // to get members who have birthday on the given date
    public static void Birthday_Person(String date) {
        try {
            GetInfo.Get_Birthday_Person(date);
        } catch (Exception e) {
            System.out.println("\nEnter date correctly!\n");
        }

    }

    // to send mail
    public static void SendMail(String MailID, String subject, String content) {
        G_mail NewMail = new G_mail(MailID, subject, content);
        Send_A_Mail.Send(NewMail);
    }

    // to send birthday wish
    public static void Wish() {
        GetInfo.SendWish();

    }

    // to get sent mails details
    public static void Sent_mail_Details(String day) {
        Sent_Mails.De_Serialize_this_mail(day);
    }
}

/*
 * =============================================================================
 * ========
 */
/* observer super class */
abstract class observer {
    // abstract method
    public abstract void update(String message);
}

/*
 * =============================================================================
 * ========
 */
class EmailStatPrinter extends observer {
    /* saves the message to a text file */
    public void update(String message) {
        try (FileWriter new_writer = new FileWriter("mail_details.txt", true);
                BufferedWriter new_Buffer = new BufferedWriter(new_writer)) {
            new_Buffer.write(message + "\n");
        } catch (IOException e) {
            System.out.println("Error While Updating to File!");
        }
    }
}

/*
 * =============================================================================
 * ========
 */
class EmailStatRecorder extends observer {
    // print the message to the console
    public void update(String message) {
        System.out.println(message);
    }
}

/*
 * =============================================================================
 * ========
 */
// to find the type of the recipent and create recipient object
abstract class FindRecipent {
    public static Recipient getRecipient(String data) {
        try {
            String[] list = data.split(",");
            String[] type = (list[0]).split(":");
            if ((type[0].trim()).equals("Official")) {
                return new Official(type[1].trim(), list[1], list[2]);
            } else if ((type[0].trim()).equals("Personal")) {
                return new Personal(type[1].trim(), list[1], list[2], list[3]);
            } else if ((type[0].trim()).equals("Office_friend")) {
                return new OfficeFriend(type[1].trim(), list[1], list[2], list[3]);
            } else {
                return null;
            }
        } catch (Exception e) {
            System.out.println("Wrong inputs in" + data);
            return null;
        }

    }
}

/*
 * =============================================================================
 * ========
 */
// to create mail object
class G_mail implements Serializable {
    private static final long serialVersionUID = 1L;
    private String MailID;
    private String subject;
    private String content;
    private String time;
    private final String date = LocalDate.now().toString();
    private String received_date;

    public G_mail(String MailID, String subject, String content) {
        this.MailID = MailID;
        this.subject = subject;
        this.content = content;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public void setReceivedDate(String date) {
        this.received_date = date;
    }

    public String getID() {
        return MailID;
    }

    public String getSubject() {
        return subject;
    }

    public String getContent() {
        return content;
    }

    public String getDate() {
        return date;
    }

    public String getReceivedDate() {
        return received_date;
    }

    public String gettime() {
        return time;
    }
}

/*
 * =============================================================================
 * ========
 */
abstract class GetInfo {
    // total number of recipent
    private static int TotalRecipients;
    // list of recipints who have birthdays
    private static ArrayList<Recipient> birthdayList;
    // total recipient list
    private static ArrayList<Recipient> recipientList;
    private static Recipient recip;

    // to get informations from the text file
    public static void readFile() {
        try {
            File mylist = new File("ClientList.txt");
            Scanner myReader = new Scanner(mylist);
            birthdayList = new ArrayList<Recipient>();
            recipientList = new ArrayList<Recipient>();
            while (myReader.hasNextLine()) {
                String data = myReader.nextLine();
                recip = FindRecipent.getRecipient(data);
                if (recip instanceof Personal) {
                    birthdayList.add((Personal) recip);
                    recipientList.add((Personal) recip);
                } else if (recip instanceof OfficeFriend) {
                    birthdayList.add((OfficeFriend) recip);
                    recipientList.add((OfficeFriend) recip);
                } else {
                    recipientList.add((Official) recip);
                }
            }
            myReader.close();
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred While reading File!"
                    + "\nWrong Input! Or No Text file found!");
            // e.printStackTrace();
        }
    }

    // to get total members
    public static void getTotal() {
        readFile();
        TotalRecipients = recipientList.size();
        System.out.println("Total members : " + TotalRecipients);
    }

    // to get members who have birthday on the given date
    public static void Get_Birthday_Person(String date) {
        readFile();
        boolean noBirthDay = true;
        String month = date.split("/")[1];
        String day = date.split("/")[2];
        for (Recipient Bboy : birthdayList) {
            String BirthM = (((HasBirthday) Bboy).getBirthDay()).split("/")[1];
            String BirthD = (((HasBirthday) Bboy).getBirthDay()).split("/")[2];
            if (month.equals(BirthM) && day.equals(BirthD)) {
                System.out.println(Bboy.getName());
                noBirthDay = false;
            }

        }
        if (noBirthDay) {
            System.out.println("\nNo birthday on this date !\n");
        }
    }

    // to send birthday wish
 public static void SendWish(){
 readFile();
 boolean noWish = true;
 G_mail BirthdayMail;
 try {
 String Month = (LocalDate.now().toString()).split("-")[1];
 String Date = (LocalDate.now().toString()).split("-")[2];
 for (Recipient Bboy: birthdayList){
 String BirthM = (((HasBirthday)Bboy).getBirthDay()).split("/")[1];
 String BirthD = (((HasBirthday)Bboy).getBirthDay()).split("/")[2];
 if(Month.equals(BirthM) && Date.equals(BirthD)){
 String Email = Bboy.getEmail();
 String Subject = "Birthday Wish";
 if (Bboy instanceof OfficeFriend){
 String Content = "Wish Your Happy Birthday.\nVinojith";
 System.out.println("Birthday wish has been sent to 
:"+Bboy.getName());
 BirthdayMail = new G_mail(Email, Subject, Content);
 Send_A_Mail.Send(BirthdayMail);
 
 noWish = false;
 }else if (Bboy instanceof Personal){
 noWish = false;
 String Content = "Hugs and Love on your Birthday.\nVinojith";
 BirthdayMail = new G_mail(Email, Subject, Content);
 Send_A_Mail.Send(BirthdayMail);
 System.out.println("Birthday wish has been sent to 
:"+Bboy.getName());
 }
 }
 }
 
 } catch (Exception e) {
 System.out.println("\nBirthday wish not sent!\n");
 }if (noWish){
 System.out.println("\nNo Birthday Today!");
 }
 
 }
}

/*
 * =============================================================================
 * ========
 */
// to implement reciplients who have birthdays
interface HasBirthday {
    public String getBirthDay();
}

/*
 * =============================================================================
 * ========
 */
// to create Official_Friend
class OfficeFriend extends Official implements HasBirthday {
    private String BirthDate;
    private String Job;

    public OfficeFriend(String Name, String Email, String Job, String Date) {
        super(Name, Email, Date);
        this.BirthDate = Date;
        this.Job = Job;
    }

    public String getBirthDay() {
        return BirthDate;
    }

    public String getJob() {
        return Job;
    }
}

/*
 * =============================================================================
 * ========
 */
// to create Official recipients
class Official extends Recipient {
    private String Job;

    public Official(String Name, String Email, String Job) {
        super(Name, Email);
        this.Job = Job;
    }

    public String getJob() {
        return Job;
    }
}

/*
 * =============================================================================
 * ========
 */
// to create Personal recipients
class Personal extends Recipient implements HasBirthday {
    private String BirthDate;
    private String NickName;

    public Personal(String Name, String Nick, String Email, String Date) {
        super(Name, Email);
        this.BirthDate = Date;
        this.NickName = Nick;
    }

    public String getBirthDay() {
        return BirthDate;
    }

    public String getNickName() {
        return NickName;
    }

}

/*
 * =============================================================================
 * ========
 */
// to create recipients
class Recipient {
    private String Name;
    private String Email;

    public Recipient(String Name, String Email) {
        this.Name = Name;
        this.Email = Email;
    }

    public String getName() {
        return Name;
    }

    public String getEmail() {
        return Email;
    }
}

/*
 * =============================================================================
 * ========
 */
class SaveToHardisk {
    private static final String path = "C:\\Users\\HP\\Documents\\Email_Client\\";

    public static void Save_Mails(G_mail new_mail, String folder) {
        try {
            FileOutputStream fileout = new FileOutputStream(path + folder
                    + new_mail.getID() + "_" +
                    new_mail.getSubject() + ".ser");
            ObjectOutputStream out = new ObjectOutputStream(fileout);
            out.writeObject(new_mail);
            out.close();
            fileout.close();
            System.out.println("Mail Saved to Hardisk");
        } catch (Exception e) {
            System.out.println("Received Mail not Saved");
            e.fillInStackTrace();
        }
    }
}

/*
 * =============================================================================
 * ========
 */
abstract class Send_A_Mail {
    private final static String username = "javatest190650b@gmail.com";
    private final static String password = "vinojithABD@17";

    public static void Send(G_mail New_mail) {
        Properties prop = new Properties();
        prop.put("mail.smtp.host", "smtp.gmail.com");
        prop.put("mail.smtp.port", "587");
        prop.put("mail.smtp.auth", "true");
        prop.put("mail.smtp.starttls.enable", "true");
        Session session = Session.getInstance(prop, new javax.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });
        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username));
            message.setRecipients(
                    Message.RecipientType.TO,
                    InternetAddress.parse(New_mail.getID()));
            message.setSubject(New_mail.getSubject());
            message.setText(New_mail.getContent()
                    + "\n\nBest regards,"
                    + "\nClient Management System.");
            Transport.send(message);
            System.out.println("Mail sent successfully!");
            SaveToHardisk.Save_Mails(New_mail, "Serialized\\");
        } catch (MessagingException e) {
            System.out.println("Mail Not sent!\nNo a Proper MailID");
        }
    }
}

/*
 * =============================================================================
 * ========
 */
abstract class Sent_Mails {
    private static final String mails_folder = "C:\\Users\\HP\\Documents\\Email_Client\\Serialized";

    public static void De_Serialize_this_mail(String day) {
        File folder = new File(mails_folder);
        File[] allSubFiles = folder.listFiles();
        boolean no_mail = true;
        System.out.println("\nSent mails on: " + day + "\n");
        for (File file : allSubFiles) {
            String filename = (file.getAbsolutePath().toString());
            G_mail sentMail;
            try {
                FileInputStream filein = new FileInputStream(filename);
                ObjectInputStream in = new ObjectInputStream(filein);
                sentMail = (G_mail) in.readObject();
                if (day.equals(sentMail.getDate())) {
                    System.out.println("Recipient: " + sentMail.getID());
                    System.out.println("Subject: " + sentMail.getSubject() + "\n");
                    no_mail = false;
                    in.close();
                    filein.close();
                }
                in.close();
                filein.close();
            } catch (Exception e) {
                e.fillInStackTrace();
                System.out.println("\nError while de_serialization!");

            }
        }
        if (no_mail) {
            System.out.println("No mails on this date");
        }
    }
}
/*
 * =============================================================================
 * ========
 */