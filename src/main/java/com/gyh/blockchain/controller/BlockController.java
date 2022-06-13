package com.gyh.blockchain.controller;

import com.gyh.blockchain.domain.BlockChain;
import com.gyh.blockchain.domain.TransactionVO;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @description: 区块接口
 * @author: gyh
 * @date: 2022/06/10
 */
@RestController
public class BlockController {
    private final BlockChain blockChain = BlockChain.getInstance();

    /**
     * 发送交易
     * @param transactionVO
     * @return
     */
    @RequestMapping("/transactions/new")
    public int newTransaction(@RequestBody TransactionVO transactionVO) {
        return blockChain.newTransactions(transactionVO.getSender(), transactionVO.getRecipient(), transactionVO.getAmount());
    }

    /**
     * 挖矿
     * @return
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

    /**
     * 获取整个区块链
     * @return
     */
    @RequestMapping("/chain")
    public Map<String, Object> chain() {
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("chain", blockChain.getChain());
        result.put("length", blockChain.getChain().size());
        return result;
    }

    /**
     * 节点注册
     * @return
     */
    @RequestMapping("/register")
    public Set<String> register(@RequestParam("address") String address) {
        blockChain.registerNode(address);
        return blockChain.getNodes();
    }

    @RequestMapping("/resolve")
    public Boolean resolve() {
        try {
            blockChain.resolveConflicts();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
}
