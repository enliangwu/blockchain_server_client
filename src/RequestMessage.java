import com.alibaba.fastjson.JSONObject;

/**
 * name: Enliang Wu
 * email: enliangw@andrew.cmu.edu
 */
public class RequestMessage {
    private int type;
    private int difficulty;
    private String data;
    private int index;

    public RequestMessage() {
    }

    public RequestMessage(int type) {
        this.type = type;
    }

    public RequestMessage(int type, int difficulty, String data, int index) {
        this.type = type;
        this.difficulty = difficulty;
        this.data = data;
        this.index = index;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(int difficulty) {
        this.difficulty = difficulty;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    @Override
    public String toString() {
        return JSONObject.toJSONString(this);
    }
}
