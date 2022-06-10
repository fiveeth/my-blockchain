package com.gyh.blockchain;

import com.alibaba.fastjson.JSONObject;
import com.gyh.blockchain.domain.BlockChain;
import com.gyh.blockchain.utils.Encrypt;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;
import java.util.Map;

@SpringBootTest
class BlockchainApplicationTests {

    @Test
    void contextLoads() {
    }

    @Test
    public void getHash() {
        Encrypt encrypt = new Encrypt();
        System.out.println(encrypt.getSHA256("520287"));
    }

    @Test
    public void mining() {
        BlockChain blockChain = BlockChain.getInstance();
        //一个区块中可以不包含任何交易记录
        Map<String, Object> block = blockChain.newBlock(300, null);
        System.out.println(new JSONObject(block));
        //一个区块中可以包含一笔交易记录
        blockChain.newTransactions("123", "222", 33);
        Map<String, Object> block1 = blockChain.newBlock(500, null);
        System.out.println(new JSONObject(block1));
        //一个区块中可以包含多笔交易记录
        blockChain.newTransactions("321", "555", 133);
        blockChain.newTransactions("000", "111", 10);
        blockChain.newTransactions("789", "369", 65);
        Map<String, Object> block2 = blockChain.newBlock(600, null);
        System.out.println(new JSONObject(block2));
        //查看整个区块链
        Map<String, Object> chain = new HashMap<String, Object>();
        chain.put("chain", blockChain.getChain());
        chain.put("length", blockChain.getChain().size());
        System.out.println(new JSONObject(chain));
    }

    @Test
    public void testProof() {
        int x = 5;
        int y = 0;
        while (!new Encrypt().getSHA256((x * y) + "").endsWith("0")) {
            y++;
        }
        System.out.println("y=" + y);
    }

    @Test
    public void proofOfWork() {
        BlockChain blockChain = BlockChain.getInstance();
        Long result = blockChain.proofOfWork(5);
        System.out.println(result);
    }
}
