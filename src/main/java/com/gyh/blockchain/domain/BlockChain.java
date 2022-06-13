package com.gyh.blockchain.domain;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.gyh.blockchain.utils.Encrypt;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

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
    /**
     * 节点
     */
    private Set<String> nodes;
    private static BlockChain blockChain = null;

    private BlockChain() {
        //初始化区块链以及当前的交易信息列表
        chain = new ArrayList<Map<String, Object>>();
        currentTransactions = new ArrayList<Map<String, Object>>();
        // 用于存储网络中其他节点的集合
        nodes = new HashSet<String>();
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

    public Set<String> getNodes() {
        return nodes;
    }

    /**
     * 注册节点
     *
     * @param address 节点地址
     * @throws MalformedURLException
     */
    public void registerNode(String address) {
        URL url = null;
        try {
            url = new URL(address);
            String node = url.getHost() + ":" + (url.getPort() == -1 ? url.getDefaultPort() : url.getPort());
            nodes.add(node);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    /**
     * 得到区块链中的最后一个区块
     *
     * @return
     */
    public Map<String, Object> lastBlock() {
        return getChain().get(getChain().size() - 1);
    }

    /**
     * 在区块链上新建一个区块
     *
     * @param proof        新区块的工作量证明
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
     *
     * @param sender    发送方的地址
     * @param recipient 接收方的地址
     * @param amount    交易数量
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
     *
     * @param block 区块
     * @return 返回该区块的hash
     */
    public static Object hash(Map<String, Object> block) {
        return new Encrypt().getSHA256(new JSONObject(block).toString());
    }

    /**
     * 简单的工作量证明: 查找一个 p' 使得 hash(pp') 以4个0开头
     * p是上一个块的证明，p'是当前的证明
     *
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
     *
     * @param lastProof 上一个块的证明
     * @param proof     当前的证明
     * @return 以4个0开头返回true，否则返回false
     */
    public boolean validProof(long lastProof, long proof) {
        String guess = lastProof + "" + proof;
        String guessHash = new Encrypt().getSHA256(guess);
        return guessHash.startsWith("0000");
    }

    /**
     * 检查是否是有效链，遍历每个区块验证hash和proof，来确定一个给定的区块链是否有效
     *
     * @param chain
     * @return
     */
    public boolean validChain(List<Map<String, Object>> chain) {
        Map<String, Object> lastBlock = chain.get(0);
        int currentIndex = 1;
        while (currentIndex < chain.size()) {
            Map<String, Object> block = chain.get(currentIndex);
            // 主要是检查下一个区块的previousHash是否是前一个区块的hash值
            if (!block.get("previousHash").equals(hash(lastBlock))) {
                return false;
            }
            lastBlock = block;
            currentIndex++;
        }
        return true;
    }

    /**
     * 共识算法解决冲突，使用网络中最长的链。遍历所有的邻居节点，并用上一个方法检查链的有效性，如果发现有效更长链，就替换掉自己的链
     *
     * @return 如果链被取代返回true, 否则返回false
     * @throws IOException
     */
    public boolean resolveConflicts() throws IOException {
        Set<String> neighbours = this.nodes;
        List<Map<String, Object>> newChain = null;
        //寻找最长的区块链
        long maxLength = this.chain.size();
        OkHttpClient okHttpClient = new OkHttpClient();
        //获取并验证网络中的所有节点的区块链
        for (String node : neighbours) {
            String url = "http://" + node + "/chain";
            Request request = new Request.Builder()
                    .url(url)
                    .get()
                    .build();
            Response response = okHttpClient.newCall(request).execute();
            if(response.code() == 200) {
                JSONObject jsonObject = JSON.parseObject(response.body().string());
                Long length = jsonObject.getLong("length");
                List<Map<String, Object>> chain = jsonObject.getJSONArray("chain").stream()
                        .map(s -> JSONObject.parseObject(JSONObject.toJSONString(s)).getInnerMap())
                        .collect(Collectors.toList());

                //检查长度是否长，链是否有效
                if (length > maxLength && validChain(chain)) {
                    maxLength = length;
                    newChain = chain;
                }
            }
        }
        //如果发现一个新的有效链比我们的长，就替换当前的链
        if (newChain != null) {
            this.chain = newChain;
            return true;
        }
        return false;
    }
}
