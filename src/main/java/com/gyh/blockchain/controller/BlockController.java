package com.gyh.blockchain.controller;

import com.gyh.blockchain.domain.BlockChain;
import com.gyh.blockchain.domain.TransactionVO;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @description: 区块接口
 * @author: guoyihua
 * @date: 2022/06/10
 */
@RestController
public class BlockController {
    private final BlockChain blockChain = BlockChain.getInstance();

    @RequestMapping("/transactions/new")
    public int newTransaction(@RequestBody TransactionVO transactionVO) {
        return blockChain.newTransactions(transactionVO.getSender(), transactionVO.getRecipient(), transactionVO.getAmount());
    }

    /**
     * 挖矿
     */
    @RequestMapping("/mime")
    public Map<String, Object> mime() {
        Map<String, Object> lastBlock = blockChain.lastBlock();
        long lastProof = Long.parseLong(lastBlock.get("proof") + "");
        long proof = blockChain.proofOfWork(lastProof);
        // 给工作量证明的节点提供奖励，发送者为 "0" 表明是新挖出的币
        String uuid = System.getenv("uuid");
        blockChain.newTransactions("0", uuid, 1);
        // 构建新的区块
        Map<String, Object> newBlock = blockChain.newBlock(proof, null);
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("message", "New Block Forged");
        result.put("index", newBlock.get("index"));
        result.put("transactions", newBlock.get("transactions"));
        result.put("proof", newBlock.get("proof"));
        result.put("previousHash", newBlock.get("previousHash"));
        return result;
    }

    @RequestMapping("/chain")
    public Map<String, Object> chain() {
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("chain", blockChain.getChain());
        result.put("length", blockChain.getChain().size());
        return result;
    }
}
