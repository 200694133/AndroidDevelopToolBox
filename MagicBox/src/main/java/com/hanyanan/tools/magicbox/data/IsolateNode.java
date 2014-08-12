package com.hanyanan.tools.magicbox.data;

import java.util.List;

/**
 * Created by Administrator on 2014/8/11.
 */
public class IsolateNode<T> extends Node<T>{
    public IsolateNode(Object tag, boolean isPositive) {
        super(tag, isPositive);
    }

    @Override
    public void fetchData() {

    }
    public void onBackwardNodeUpdates(Node node){
        //do nothing
    }
    public final void addBackwardNode(Node node){
        throw new UnsupportedOperationException("It's forbid to addBackwardNode for IsolateNode");
    }
    public final List<Node> getBackwardNodes(){
        return null;
    }
    public final void addForwardNode(Node node){
        throw new UnsupportedOperationException("It's forbid to addForwardNode for IsolateNode");
    }
    public final List<Node> getForwardNodes(){
        return null;
    }
}
