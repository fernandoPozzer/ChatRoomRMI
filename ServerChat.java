package ChatRoomRMI;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class ServerChat extends UnicastRemoteObject implements IServerChat
{
    private Map<String, IRoomChat> roomList;

    public ServerChat() throws RemoteException
    {
        super();
        roomList = new HashMap<>();
    }

    @Override
    public ArrayList<String> getRooms() throws RemoteException
    {
        ArrayList<String> roomNames = new ArrayList<String>();

        // roomNames.add("SALA-TESTE");

        for (String s : roomList.keySet())
        {
            roomNames.add(s);
        }

        return roomNames;
    }

    @Override
    public void createRoom(String roomName) throws RemoteException
    {
        if (roomList.containsKey(roomName))
        {
            // Servidor ja tem sala com esse nome
            return;
        }

        try
        {
            RoomChat room = new RoomChat();
            room.setRoomName(roomName);
            roomList.put(roomName, room);
            Naming.rebind("rmi://localhost:2020/" + roomName, room);
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws Exception
    {
        try
        {
            System.out.println("Iniciando...");
            ServerChat server = new ServerChat();

            LocateRegistry.createRegistry(2020);
            Naming.rebind("rmi://localhost:2020/Servidor", server);

            System.out.println("Servidor no ar.");
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }
}