package ChatRoomRMI;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class RoomChat extends UnicastRemoteObject implements IRoomChat
{
    private Map<String, IUserChat> userList;
    private String name;

    protected RoomChat(String name) throws RemoteException
    {
        super();

        this.name = name;
        userList = new HashMap<>();
    }

    @Override
    public void sendMsg(String usrName, String msg) throws RemoteException
    {
        for (IUserChat user : userList.values())
        {
            user.deliverMsg(usrName, msg);
        }
    }

    @Override
    public void joinRoom(String userName, IUserChat user) throws RemoteException
    {
        userList.put(userName, user);
        sendMsg("Servidor", userName + " entrou na sala.");
    }

    @Override
    public void leaveRoom(String usrName) throws RemoteException
    {
        userList.remove(usrName);
        sendMsg("Servidor", usrName + " saiu da sala.");
    }

    @Override
    public String getRoomName() throws RemoteException
    {
        return name;
    }

    @Override
    public void closeRoom() throws RemoteException
    {
        if (!userList.isEmpty())
        {
            sendMsg("Servidor", "Sala fechada pelo servidor.");
        }

        userList.clear();
    }
}