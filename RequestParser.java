import java.util.Arrays;

public class RequestParser {

    private String type;
    private String[] data;

    public RequestParser(String message){
        String[] temp = Arrays.stream(message.split("¤"))
                .map(String::trim)
//                .map(String::toLowerCase)
                .toArray(String[]::new);

        type = temp[0];

        data = new String[temp.length-1];
        System.arraycopy(temp, 1, data, 0, temp.length - 1);
        System.out.println("Captured Request type: " + this.type + "\nCaptured Request Data: " + Arrays.toString(data));
    }

    public String getType(){
        return type;
    }

    public String[] getData(){
        return data;
    }
}

