package com.android.vidrebany.models;

public class ModelStandbyProcess {
    String code, process, mark;
    Long startedDate;

    public ModelStandbyProcess() {

    }

    public ModelStandbyProcess(String code, String process, Long startedDate, String mark) {
        this.code = code;
        this.startedDate = startedDate;
        this.process = process;
        this.mark = mark;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Long getStartedDate() {
        return startedDate;
    }

    public String getProcess() {
        return process;
    }

    public void setProcess(String process) {
        this.process = process;
    }

    public String getMark() {
        return mark;
    }

    public void setMark(String mark) {
        this.mark = mark;
    }

}
