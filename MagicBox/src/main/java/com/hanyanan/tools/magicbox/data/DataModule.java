package com.hanyanan.tools.magicbox.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by hanyanan on 2014/8/11.
 */
public class DataModule {
    private final List<Node> mAllNodes = new ArrayList<Node>();
    private final HashMap<Object,Node> mNodeMap = new HashMap<Object, Node>();
    public DataModule(List<Node> nodeList){
        mAllNodes.addAll(nodeList);
    }
    public void addNode(Node node){
        mAllNodes.add(node);
        mNodeMap.put(node.getTag(), node);
    }
    public Node getNode(Object tag){
        return mNodeMap.get(tag);
    }

    public void addRegular(Node anchor, Node backNode){
        anchor.addBackwardNode(backNode);
        backNode.addForwardNode(anchor);
    }
}
