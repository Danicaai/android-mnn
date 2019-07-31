package cn.sample.mnn.detect;

/**
 * Created by GDD on 2019/4/18 .
 */

public class FaceDetectResult {
    private String detectstatus;

    private String fakescore;

    public String getDetecttatus() {
        return detectstatus;
    }

    public void setDetectstatus(String detectstatus) {
        this.detectstatus = detectstatus;
    }

    public String getFakescore() {
        return fakescore;
    }

    public void setFakescore(String fakescore) {
        this.fakescore = fakescore;
    }

}
