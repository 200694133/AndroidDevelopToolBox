package com.hanyanan.tools.data;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hanyanan on 2014/8/11.
 */
public abstract class Node <T>{
    protected final Object mTag;
    protected Object mHolder;
    protected boolean isPositive = false;
    protected final List<Node> mForwardNodes = new ArrayList<Node>();
    protected final List<Node> mBackwardNodes = new ArrayList<Node>();
    protected final List<Node> mChildrenNodes = new ArrayList<Node>();
    protected DataChangeHandler mDataChangeHandler;
    protected T mData;
    public abstract void fetchData();
    public Node(Object tag, boolean isPositive){
        mTag = tag;
        this.isPositive = isPositive;
    }

    public void onBackwardNodeUpdates(Node node){
        if(isPositive){
            fetchData();
        }
    }

    public void notifyForwardNodes(){
        List<Node> nodes = new ArrayList<Node>();
        synchronized (this){
            nodes.addAll(mForwardNodes);
        }
        for(Node node:nodes){
            node.onBackwardNodeUpdates(this);
        }
    }
    public void setNewData(T data){
        synchronized (this){
            mData = data;
        }
        notifyForwardNodes();
        notifyDataChange();
    }
    private void notifyDataChange(){
        if(null != mDataChangeHandler){
            mDataChangeHandler.notifyDataChange(mData);
        }
    }
    public void setDataChangeHandler(DataChangeHandler handler){
        synchronized (this){
            mDataChangeHandler = handler;
        }
    }
    public void addBackwardNode(Node node){
        synchronized (this){
            mBackwardNodes.add(node);
        }
    }
    public List<Node> getBackwardNodes(){
        synchronized (this){
            return new ArrayList<Node>(mBackwardNodes);
        }
    }
    public void addForwardNode(Node node){
        synchronized (this){
            mForwardNodes.add(node);
        }
    }
    public void addChild(Node node){
        synchronized (this){
            mChildrenNodes.add(node);
        }
    }
    public List<Node> getChildrenNodes(){
        synchronized (this){
            return new ArrayList<Node>(mChildrenNodes);
        }
    }
    public List<Node> getForwardNodes(){
        synchronized (this){
            return new ArrayList<Node>(mForwardNodes);
        }
    }
    public void setHolder(Object holder){
        mHolder = holder;
    }
    public Object getHolder(){
        return mHolder;
    }
    public boolean isPositive(){
        return isPositive;
    }
    public void setPositive(boolean positive){
        isPositive = positive;
    }
    public Object getTag(){return mTag;}
    public static interface OnDataChangedListener<T> {
        public void onDataChanged(T data);
    }
}
