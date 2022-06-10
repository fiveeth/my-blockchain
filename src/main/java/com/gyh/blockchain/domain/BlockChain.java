package com.gyh.blockchain.domain;

import com.alibaba.fastjson.JSONObject;
import com.gyh.blockchain.utils.Encrypt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @description: 区块基类
 * @author: gyh
 * @date: 2022/06/10
 */
public class BlockChain {
    /**
     * 存储区块链
     */
    private List<Map<String, Object>> chain;
    /**
     * 该实例变量用于当前的交易信息列表
     */
    private List<Map<String, Object>> currentTransactions;
    private static BlockChain blockChain = null;

    private BlockChain() {
        //初始化区块链以及当前的交易信息列表
        chain = new ArrayList<Map<String, Object>>();
        currentTransactions = new ArrayList<Map<String, Object>>();
        //创建创世区块
        newBlock(100, "0");
    }

    /**
     * 创建单例对象
     *
     * @return
     */
    public static BlockChain getInstance() {
        if (blockChain == null) {
            synchronized (BlockChain.class) {
                if (blockChain == null) {
                    blockChain = new BlockChain();
                }
            }
        }
        return blockChain;
    }

    public List<Map<String, Object>> getChain() {
        return chain;
    }

    public void setChain(List<Map<String, Object>> chain) {
        this.chain = chain;
    }

    public List<Map<String, Object>> getCurrentTransactions() {
        return currentTransactions;
    }

    public void setCurrentTransactions(List<Map<String, Object>> currentTransactions) {
        this.currentTransactions = currentTransactions;
    }

    /**
     * 得到区块链中的最后一个区块
     * @return
     */
    public Map<String, Object> lastBlock() {
        return getChain().get(getChain().size() - 1);
    }

    /**
     * 在区块链上新建一个区块
     * @param proof 新区块的工作量证明
     * @param previousHash 上一个区块的hash值
     * @return 返回新建的区块
     */
    public Map<String, Object> newBlock(long proof, String previousHash) {
        Map<String, Object> block = new HashMap<String, Object>();
        block.put("index", getChain().size() + 1);
        block.put("timestamp", System.currentTimeMillis());
        block.put("transactions", getCurrentTransactions());
        block.put("proof", proof);
        //如果没有传递上一个区块的hash就计算出区块链中最后一个区块的hash
        block.put("previousHash", previousHash != null ? previousHash : hash(getChain().get(getChain().size() - 1)));
        //重置当前的交易信息列表
        setCurrentTransactions(new ArrayList<Map<String, Object>>());
        getChain().add(block);
        return block;
    }

    /**
     * 生成新交易信息，信息将加入到下一个待挖的区块中
     * @param sender 发送方的地址
     * @param recipient 接收方的地址
     * @param amount 交易数量
     * @return 返回该交易事务的块的索引
     */
    public int newTransactions(String sender, String recipient, long amount) {
        Map<String, Object> transaction = new HashMap<String, Object>();
        transaction.put("sender", sender);
        transaction.put("recipient", recipient);
        transaction.put("amount", amount);
        getCurrentTransactions().add(transaction);
        return (Integer) lastBlock().get("index") + 1;
    }

    /**
     * 生成区块的 SHA-256格式的 hash值
     * @param block 区块
     * @return 返回该区块的hash
     */
    public static Object hash(Map<String, Object> block) {
        return new Encrypt().getSHA256(new JSONObject(block).toString());
    }

    /**
     * 简单的工作量证明: 查找一个 p' 使得 hash(pp') 以4个0开头
     * p是上一个块的证明，p'是当前的证明
     * @param last_proof 上一个块的证明
     * @return
     */
    public long proofOfWork(long last_proof) {
        long proof = 0;
        while (!validProof(last_proof, proof)) {
            proof += 1;
        }
        return proof;
    }

    /**
     * 验证证明: 是否hash(last_proof, proof)以4个0开头?
     * @param lastProof 上一个块的证明
     * @param proof 当前的证明
     * @return 以4个0开头返回true，否则返回false
     */
    public boolean validProof(long lastProof, long proof) {
        String guess = lastProof + "" + proof;
        String guessHash = new Encrypt().getSHA256(guess);
        return guessHash.startsWith("0000");
    }
}
