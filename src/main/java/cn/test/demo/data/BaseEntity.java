package cn.test.demo.data;

import org.springframework.batch.item.ItemWriter;

import java.util.List;

public class BaseEntity {

    private String auditComment;

    private boolean isOrder = true;

    private List<ItemWriter<? super BaseEntity>> delegates;

    public String getAuditComment() {
        return auditComment;
    }

    public void setAuditComment(String auditComment) {
        this.auditComment = auditComment;
    }

    public boolean isOrder() {
        return isOrder;
    }

    public void setOrder(boolean order) {
        isOrder = order;
    }

    public List<ItemWriter<? super BaseEntity>> getDelegates() {
        return delegates;
    }

    public void setDelegates(List<ItemWriter<? super BaseEntity>> delegates) {
        this.delegates = delegates;
    }
}
