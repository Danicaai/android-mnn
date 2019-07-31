package cn.sample.mnn.detect;

import java.util.List;

/**
 * Created by GDD on 2019/4/18 .
 */

public class Faceresults {
    private List<ResultsBean> results;

    public List<ResultsBean> getResults() {
        return results;
    }

    public void setResults(List<ResultsBean> results) {
        this.results = results;
    }

    public static class ResultsBean {
        private int x;
        private int y;
        private int w;
        private int h;
        private int status;
        private String cropdata;
        private String srcdata;

        public int getX() {
            return x;
        }

        public void setX(int x) {
            this.x = x;
        }

        public int getY() {
            return y;
        }

        public void setY(int y) {
            this.y = y;
        }

        public int getW() {
            return w;
        }

        public void setW(int w) {
            this.w = w;
        }

        public int getH() {
            return h;
        }

        public void setH(int h) {
            this.h = h;
        }

        public int getStatus() {
            return status;
        }

        public void setStatus(int status) {
            this.status = status;
        }

        public String getCropdata() {
            return cropdata;
        }

        public void setCropdata(String cropdata) {
            this.cropdata = cropdata;
        }

        public String getSrcdata() {
            return srcdata;
        }

        public void setSrcdata(String srcdata) {
            this.srcdata = srcdata;
        }
    }
}
