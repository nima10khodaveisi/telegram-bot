import javafx.util.Pair;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendAudio;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;

public class Tamrin extends TelegramLongPollingBot {

    HashMap<Long, ArrayList<Pair<String , String> > >  playLists = new HashMap<>() ;
    // name , file
    HashMap<Long , String> commands = new HashMap<>() ;
    String nameOfPlayList = null ;
    ArrayList< Message > history = new ArrayList<>() ;

    public void change(long chatId , String command) {
        commands.remove(chatId) ;
        commands.put(chatId , command) ;
    }

    public void clear_history(String chat) {
        if(history.isEmpty())
            return ;
        for(Message message : history) {
            String chatId = message.getChatId().toString();
            if(!chatId.equals(chat)) continue ;
            Integer messageId = message.getMessageId() ;
            DeleteMessage deleteMessage = new DeleteMessage(chatId , messageId) ;
            try {
                execute(deleteMessage) ;
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
        history.clear();
    }

    public void check_history_time() {
        if(history.isEmpty())
            return ;
        for(Message message : history) {
            Date now = new Date() ;
            System.out.println("time " + now.getTime() + " " + message.getDate()) ;
            if((now.getTime() - message.getDate()) / 1000 >= 60) {
                String chatId = message.getChatId().toString();
                Integer messageId = message.getMessageId() ;
                DeleteMessage deleteMessage = new DeleteMessage(chatId , messageId) ;
                try {
                    execute(deleteMessage) ;
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void onUpdateReceived(Update update) {
        check_history_time() ;
        System.out.println(update.getMessage().getDate());
        System.out.println("user : " + update.getMessage().getFrom().getUserName());
        long chatId = update.getMessage().getChatId() ;
        if(!commands.containsKey(chatId)) {
            commands.put(chatId , null) ;
        }
        String command = commands.get(chatId) ;
        if(command == null) {
            String str = update.getMessage().getText() ;
            if ("/create".equals(str) || "/add".equals(str)) {
                change(chatId, "/create");
            } else if ("/get".equals(str)) {
                change(chatId, "/get");
            } else if ("/list".equals(str)) {
                change(chatId , null) ;
                HashSet<String> nameOfPlaylists = new HashSet<>() ;
                for(Pair<String , String> x : playLists.get(chatId)) {
                    nameOfPlaylists.add(x.getKey()) ;
                }
                StringBuilder messageText = new StringBuilder() ;
                messageText.append("Your Playlists:\n") ;
                for(String string : nameOfPlaylists) {
                    messageText.append(string) ;
                    messageText.append("\n") ;
                }
                System.out.println("list " + messageText.toString());
                SendMessage message = new SendMessage() ;
                message.setText(messageText.toString()) ;
                message.setChatId(chatId) ;
                try {
                    execute(message) ;
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            } else if(str.equals("/start")) {
                SendMessage message = new SendMessage() ;
                message.setChatId(update.getMessage().getChatId()) ;
                String stringBuilder = "Hey welcome to playlist bot , this is a demo version of bot\n" +
                        "contact me : @nima10khodaveisi";
                message.setText(stringBuilder) ;
                try {
                    execute(message) ;
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            }
        } else if(command.equals("/create")) {
            String name = update.getMessage().getText() ;
            System.out.println("Name of playlist: " + name);
            nameOfPlayList = name ;
            change(chatId , "name") ;
        } else if(command.equals("name")) {
            if(update.getMessage().getAudio() == null) {
                change(chatId , null) ;
                return ;
            }
            String audio = update.getMessage().getAudio().getFileId() ;
            System.out.println("Audio " + update.getMessage().getAudio().getTitle());
            if(!playLists.containsKey(chatId)) {
                playLists.put(chatId , new ArrayList<>()) ;
            }
            ArrayList<Pair<String , String > > cur = playLists.get(chatId) ;
            history.add(update.getMessage()) ;
            if(playLists.get(chatId) != null) {
                cur = playLists.get(chatId) ;
            }
            Pair<String , String> pair = new Pair<>(nameOfPlayList , audio) ;
            cur.add(pair) ;
            playLists.remove(chatId) ;
            playLists.put(chatId , cur) ;
        } else if(command.equals("/get")) {
            change(chatId , null) ;
            String name = update.getMessage().getText() ;
            System.out.println("get " + name);
            clear_history(update.getMessage().getChatId().toString());
            for(Pair<String , String> x : playLists.get(chatId)) {
                if(x.getKey().equals(name)) {
                    SendAudio message = new SendAudio() ;
                    message.setAudio(x.getValue()) ;
                    message.setChatId(chatId) ;
                    try {
                        Message message1 = execute(message) ;
                        history.add(message1);
                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    @Override
    public String getBotUsername() {
        return "MyOwnPlayLists_bot" ;
    }

    @Override
    public String getBotToken() {
        return "928487559:AAEvAZnXgaV5aw8Wzq9kPV1QtW85Lgwl0l8" ;
    }
}