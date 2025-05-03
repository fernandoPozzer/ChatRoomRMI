package ChatRoomRMI;

public class RoomChat implements IRoomChat
{
    @Override
    public void sendMsg(String usrName, String msg)
    {
        throw new UnsupportedOperationException("Unimplemented method 'sendMsg'");
    }

    @Override
    public void joinRoom(String userName, IUserChat user)
    {
        throw new UnsupportedOperationException("Unimplemented method 'joinRoom'");
    }

    @Override
    public void leaveRoom(String usrName)
    {
        throw new UnsupportedOperationException("Unimplemented method 'leaveRoom'");
    }

    @Override
    public String getRoomName()
    {
        throw new UnsupportedOperationException("Unimplemented method 'getRoomName'");
    }

    @Override
    public void closeRoom()
    {
        throw new UnsupportedOperationException("Unimplemented method 'closeRoom'");
    }    
}
