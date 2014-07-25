package com.hanyanan.tools.datapersistence;

import java.io.Serializable;

/**
 * Created by hanyanan on 2014/7/25.
 */
public interface IBaseDataEditorExtendWorkStation extends IBaseDataEditorWorkStation{
    public interface Editor extends IBaseDataEditorWorkStation.Editor{
        public Editor put(String key, Serializable object, long expireTime);
    }
}
