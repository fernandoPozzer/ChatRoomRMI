package ChatRoomRMI;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Scanner;

public class UserChat extends UnicastRemoteObject implements IUserChat
{
    protected UserChat(String name) throws RemoteException
    {
        super();
        this.name = name;
    }

    private static String name;
    private static UserChat userChat;
    //private static RoomChat room = null;

    @Override
    public void deliverMsg(String senderName, String msg) throws RemoteException
    {
        System.out.println(" - " + senderName + ": " + msg);
    }

    public static void main(String[] args) throws Exception
    {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Digite seu nome: ");
        String line = scanner.nextLine();
        name = line;

        String serverIP = "localhost";
        String serverPort = "2020";
        String serverName = "Servidor";

        try
        {
            // IServerChat server = (IServerChat) Naming.lookup("rmi://"+ serverIP + ":" + serverPort + "/" + serverName);
            IServerChat server = (IServerChat) Naming.lookup("rmi://localhost:2020/Servidor");

            System.out.println("ROOMS:");
            System.out.println(server.getRooms());

            System.out.println("Criando sala...");
            server.createRoom("SALA-TESTE");
            System.out.println(server.getRooms());

            String roomToEnter = server.getRooms().get(0);

            System.out.println("Room to enter: " + roomToEnter);

            IRoomChat room = (IRoomChat) Naming.lookup("rmi://localhost:2020/" + roomToEnter);
            userChat = new UserChat(name);
            room.joinRoom(name, userChat);
            
            room.sendMsg(name, "Ola mundo");

        
          
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }

        scanner.close();
    }
}
