import java.util.HashMap;
import java.util.Map;

public class Blaze {

    private final String[] availableMaps = {"Giant's Shadow", "Monte Grappa", "River Somme", "Cape Helles", "Zeebrugge", "Fao Fortress", "Soissons", "Volga River", "St Quentin Scar", "Ballroom Blitz", "Łupków Pass", "Prise de Tahure", "Verdun Heights"};

    private static final HashMap<String,String> QType = new HashMap<>();

    static{
        QType.put("0", "Integer");
        QType.put("1", "String");
        QType.put("2", "Blob");
        QType.put("3", "Struct");
        QType.put("4", "List");
        QType.put("5", "Map");
        QType.put("6", "Union");
        QType.put("7", "IntList");
        QType.put("8", "ObjectType");
        QType.put("9", "ObjectId");
        QType.put("10", "Float");
    }


    private byte[] packet;
    public static boolean readable = false;



    public Map<String, Object> decode(boolean readableFlag) {
        byte[] byteData = this.packet;
        Blaze.readable = readableFlag;
        int offset = 16;

        int length = toInt(byteData, 0, 4) + toInt(byteData, 4, 2);
        int qTypeByte = byteData[13] & 0xFF;
        Object type = QType.get(String.valueOf(qTypeByte));
        if (type == null) type = qTypeByte;

        int component = toInt(byteData, 6, 2);
        int command = toInt(byteData, 8, 2);
        int id = toInt(byteData, 11, 2);

        String method;
        if ("KeepAlive".equals(type) || "Pong".equals(type)) {
            method = type.toString();
        } else {
            String componentName = Components.get(component); //
            if (componentName == null) componentName = String.valueOf(component);
            String typeKey = QType.get(qTypeByte, "Command");
            Map<String, String> commandMap = Commands.get(componentName).get(typeKey);
            String commandStr = commandMap != null ? commandMap.getOrDefault(String.valueOf(command), String.valueOf(command)) : String.valueOf(command);
            method = componentName + "." + commandStr;
        }

        Map<String, Object> structData;
        if (offset < byteData.length) {
            structData = parseStruct(byteData, offset, Blaze.readable);
        } else {
            structData = new HashMap<>();
        }

        Map<String, Object> result = new HashMap<>();
        result.put("method", method);
        result.put("type", type);
        result.put("id", id);
        result.put("length", length);
        result.put("data", structData);

        return result;
    }

    private int toInt(byte[] data, int start, int length) {
        int value = 0;
        for (int i = 0; i < length; i++) {
            value = (value << 8) | (data[start + i] & 0xFF);
        }
        return value;
    }

    private Map<String, Object> parseStruct(byte[] data, int offset, boolean readable) {
        return new HashMap<>();
    }
}

