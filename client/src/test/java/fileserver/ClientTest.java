package fileserver;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import fileserver.hyperion360.CommandLine;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClientTest {

    @InjectMocks
    Client client;

    @Mock
    Socket clientSocketCommand;

    @Mock
    ObjectOutputStream oosCommand;

    @Mock
    ObjectInputStream oisCommand;

    @Test
    @DisplayName("Test sendMessage")
    public void testSendMessage() throws IOException, ClassNotFoundException, InterruptedException {

        List<String> listFiles = new ArrayList<>();
        listFiles.add("1.docx"); listFiles.add("2.pdf");
        String msg = "index";
        CommandLine serverCommandLine = CommandLine.builder()
                                                   .type(CommandLine.TYPE_RESULT)
                                                   .command("result")
                                                   .value(listFiles)
                                                   .build();
        doNothing().when(oosCommand).writeObject(any(CommandLine.class));
        when(oisCommand.readObject()).thenReturn(serverCommandLine);
        client.sendMessage(msg);

    }
}