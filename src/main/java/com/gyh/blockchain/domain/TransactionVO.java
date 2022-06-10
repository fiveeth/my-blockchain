package com.gyh.blockchain.domain;

/**
 * @description: 交易请求
 * @author: guoyihua
 * @date: 2022/06/10
 */
public class TransactionVO {

    /**
     * 发送方的地址
     */
    private String sender;
    /**
     * 接收方的地址
     */
    private String recipient;
    /**
     * 交易数量
     */
    private Long amount;

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getRecipient() {
        return recipient;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    public Long getAmount() {
        return amount;
    }

    public void setAmount(Long amount) {
        if(amount == null) {
            this.amount = Long.parseLong("0");
        }
        this.amount = amount;
    }
}
