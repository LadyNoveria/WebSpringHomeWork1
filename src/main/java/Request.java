import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.io.BufferedOutputStream;
import java.io.InputStream;
import java.util.List;

@Getter
@Setter
@Builder
public class Request {

    private BufferedOutputStream out;
    private String httpMethod;
    private String path;
    private List<String> headers;
    private InputStream body;
}
