package fileserver.hyperion360;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class CommandLine implements Serializable {

    public static final String TYPE_RESULT = "RESULT";

    public static final String TYPE_INFORM = "INFORM";
    public static final String TYPE_EXEC = "EXEC";

    public static final String TYPE_UNKNOWN = "UNKNOWN";

    private String type;

    private String command;

    Object value;
}
