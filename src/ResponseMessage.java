import com.alibaba.fastjson.JSONObject;

/**
 * name: Enliang Wu
 * email: enliangw@andrew.cmu.edu
 */
public class ResponseMessage {
    private boolean success;
    private String data;
    private long executionTime;
    private String errMsg;

    public ResponseMessage() {
    }

    public ResponseMessage(boolean success, String data, long executionTime, String errMsg) {
        this.success = success;
        this.data = data;
        this.executionTime = executionTime;
        this.errMsg = errMsg;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public long getExecutionTime() {
        return executionTime;
    }

    public void setExecutionTime(long executionTime) {
        this.executionTime = executionTime;
    }

    public String getErrMsg() {
        return errMsg;
    }

    public void setErrMsg(String errMsg) {
        this.errMsg = errMsg;
    }

    @Override
    public String toString() {
        return JSONObject.toJSONString(this);
    }
}
